package com.example.data.repository

import com.example.data.local.AppNotificationEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.Converters
import com.example.data.local.DatingDao
import com.example.data.local.ProfileEntity
import com.example.data.local.SubscriptionEntity
import com.example.data.local.SwipeRecordEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.data.model.ChatMessage
import com.example.data.model.DatingProfile
import com.example.data.model.KatkatNotification
import com.example.data.model.KatkatNotificationType
import com.example.data.model.MatchConversation
import com.example.data.model.SubscriptionState
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserProfile
import android.content.Context
import android.util.Log
import com.example.KatkatApplication
import com.example.util.PushNotificationHelper
import com.example.data.remote.FirebaseStorageManager
import com.example.data.remote.FirestoreManager
import com.example.data.remote.PhoneAuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

class KatkatRepository(
  private val dao: DatingDao,
  private val appScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
  val firestoreManager: FirestoreManager = FirestoreManager(),
  val phoneAuthManager: PhoneAuthManager = PhoneAuthManager(),
  val firebaseStorageManager: FirebaseStorageManager = FirebaseStorageManager()
) {

  // In-memory set of recently processed notification IDs to avoid duplicate processing and duplicate push notifications
  private val recentlyProcessedNotifIds = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

  private suspend fun saveAndPushNotification(notif: KatkatNotification) {
    val existing = dao.getNotificationById(notif.id)
    dao.insertNotification(notif.toEntity())
    // Only post system push notification if this is a newly arrived notification that hasn't been pushed
    if (existing == null && recentlyProcessedNotifIds.add(notif.id)) {
      try {
        PushNotificationHelper.postPushNotification(KatkatApplication.appContext, notif)
      } catch (_: Exception) {}
    }
  }

  // Concurrency guard to ensure duplicate taps on the same profile don't execute simultaneously
  private val pendingSwipeIds = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

  // Event stream for real-time mutual matches (emitted to show celebration dialog on both devices)
  private val _realtimeMatchEvent = MutableSharedFlow<DatingProfile>(extraBufferCapacity = 5)
  val realtimeMatchEvent: SharedFlow<DatingProfile> = _realtimeMatchEvent.asSharedFlow()

  // Event stream for real-time blocks (emitted to immediately close chats if blocked by other user)
  private val _realtimeBlockedEvent = MutableSharedFlow<String>(extraBufferCapacity = 5)
  val realtimeBlockedEvent: SharedFlow<String> = _realtimeBlockedEvent.asSharedFlow()

  private var realTimeSyncJob: Job? = null

  init {
    appScope.launch {
      try {
        if (firestoreManager.isAvailable) {
          firestoreManager.purgeLegacyDiscoveryProfileCollection()
        }

        // Initialize default subscription if missing
        dao.saveSubscription(
          SubscriptionEntity(
            id = "current_sub",
            tierName = SubscriptionTier.FREE.name,
            swipesUsedThisMonth = 0,
            currentMonthKey = "2026-09",
            isAnnualBilling = false,
            subscriptionExpiryDate = "Renews Oct 16, 2026"
          )
        )

        // Delete any mock/sample profiles from local database so everything comes purely from database
        dao.deleteMockProfiles()

        // Start listening to user profile changes to attach real-time likes & match listeners
        dao.getUserProfileFlow().collect { userEntity ->
          if (userEntity != null && userEntity.isOnboardingCompleted && userEntity.id.isNotBlank() && firestoreManager.isAvailable) {
            val currentId = userEntity.id
            syncCommunityRegisteredUsers(currentId)
            startRealtimeCloudSync(currentId)
          }
        }
      } catch (_: Exception) {}
    }
  }

  /**
   * Starts real-time observation for incoming likes and mutual matches from Firestore.
   * Enables instant cross-device matching between Phone 1 and Phone 2.
   */
  fun startRealtimeCloudSync(currentUserId: String) {
    if (!firestoreManager.isAvailable || currentUserId.isBlank()) return
    realTimeSyncJob?.cancel()
    realTimeSyncJob = appScope.launch {
      // 1. Observe incoming likes from other users in real time
      launch {
        firestoreManager.observeIncomingLikes(currentUserId).collect { incomingLikes ->
          for (like in incomingLikes) {
            val senderId = like.fromUserId
            if (senderId.isBlank() || senderId == currentUserId) continue

            // Check if profile exists locally; if not, sync from community
            var profileEntity = dao.getProfileById(senderId)
            if (profileEntity == null) {
              syncCommunityRegisteredUsers(currentUserId)
              profileEntity = dao.getProfileById(senderId)
            }

            if (profileEntity != null) {
              val wasAlreadyLikedByMe = profileEntity.isLikedByMe || profileEntity.isSuperLikedByMe
              val wasAlreadyMutual = profileEntity.isMutualMatch

              // Update local state: sender liked me
              val isFirstLikeNotice = !profileEntity.likedMe
              dao.markIncomingLike(senderId)

              if (isFirstLikeNotice && !wasAlreadyMutual) {
                // Rule: Profile 2 likes Profile 1 -> Profile 1 receives notification "Someone liked you"
                val likeNotif = KatkatNotification(
                  id = "like_${senderId}",
                  userId = currentUserId,
                  type = KatkatNotificationType.PROFILE_ACTIVITY,
                  title = "New Like! ✨",
                  message = "Someone liked you",
                  timestamp = System.currentTimeMillis(),
                  senderProfileId = senderId,
                  senderProfileName = profileEntity.name,
                  senderAvatarUrl = profileEntity.photosJoined.split("|||").firstOrNull(),
                  deepLinkTarget = "likes_you"
                )
                saveAndPushNotification(likeNotif)
              }

              // If I already liked this profile and we weren't marked mutual yet -> MATCH!
              if (wasAlreadyLikedByMe && !wasAlreadyMutual) {
                val matchTime = System.currentTimeMillis()
                dao.markMutualMatch(senderId, matchTime)

                // Register mutual match in Firestore so both users share the match record
                firestoreManager.registerMutualMatch(currentUserId, senderId)

                // Rule: If it becomes mutual: "Yaaa! You have a new match!"
                val matchKey = "${minOf(currentUserId, senderId)}_${maxOf(currentUserId, senderId)}"
                val matchNotif = KatkatNotification(
                  id = "match_$matchKey",
                  userId = currentUserId,
                  type = KatkatNotificationType.NEW_MATCH,
                  title = "It's a Match! 🎉",
                  message = "Yaaa! You have a new match!",
                  timestamp = matchTime,
                  senderProfileId = senderId,
                  senderProfileName = profileEntity.name,
                  senderAvatarUrl = profileEntity.photosJoined.split("|||").firstOrNull(),
                  deepLinkTarget = "chat/$senderId"
                )
                saveAndPushNotification(matchNotif)

                // Trigger celebratory match popup on this device
                val updatedProfile = dao.getProfileById(senderId)?.toDomain()
                if (updatedProfile != null) {
                  _realtimeMatchEvent.emit(updatedProfile)
                }
              }
            }
          }
        }
      }

      // 2. Observe mutual matches in Firestore in real time
      launch {
        firestoreManager.observeMutualMatches(currentUserId).collect { cloudMatches ->
          for (cm in cloudMatches) {
            val otherUserId = if (cm.user1Id == currentUserId) cm.user2Id else cm.user1Id
            if (otherUserId.isBlank() || otherUserId == currentUserId) continue

            var profileEntity = dao.getProfileById(otherUserId)
            if (profileEntity == null) {
              syncCommunityRegisteredUsers(currentUserId)
              profileEntity = dao.getProfileById(otherUserId)
            }

            if (profileEntity != null && !profileEntity.isMutualMatch) {
              dao.markMutualMatch(otherUserId, cm.matchedTimestamp)

              // Rule: "Yaaa! You have a new match!"
              val matchKey = "${minOf(currentUserId, otherUserId)}_${maxOf(currentUserId, otherUserId)}"
              val matchNotif = KatkatNotification(
                id = "match_$matchKey",
                userId = currentUserId,
                type = KatkatNotificationType.NEW_MATCH,
                title = "It's a Match! 🎉",
                message = "Yaaa! You have a new match!",
                timestamp = cm.matchedTimestamp,
                senderProfileId = otherUserId,
                senderProfileName = profileEntity.name,
                senderAvatarUrl = profileEntity.photosJoined.split("|||").firstOrNull(),
                deepLinkTarget = "chat/$otherUserId"
              )
              saveAndPushNotification(matchNotif)

              val updatedProfile = dao.getProfileById(otherUserId)?.toDomain()
              if (updatedProfile != null) {
                _realtimeMatchEvent.emit(updatedProfile)
              }
            }
          }
        }
      }

      // 3. Observe background chat messages for all mutual matches to update unread counts and notifications in real time
      launch {
        dao.getMutualMatches().collect { mutualList ->
          mutualList.forEach { matchProfile ->
            launch {
              firestoreManager.observeChatMessages(matchProfile.id, currentUserId).collect { remoteMsgs ->
                val existingLatest = dao.getLatestMessage(matchProfile.id, currentUserId)
                remoteMsgs.forEach { msg ->
                  val alreadyStored = dao.getMessageById(msg.id) != null
                  dao.insertMessage(msg.toEntity(ownerUserId = currentUserId))
                  // Rule: If a match sends a message: "Profile 2 sent you a message"
                  if (!alreadyStored && msg.senderId != currentUserId && (existingLatest == null || msg.timestamp > existingLatest.timestamp)) {
                    val notif = KatkatNotification(
                      id = "msg_${msg.id}",
                      userId = currentUserId,
                      type = KatkatNotificationType.NEW_MESSAGE,
                      title = "New Message 💬",
                      message = "${msg.senderName.takeIf { it.isNotBlank() } ?: matchProfile.name} sent you a message",
                      timestamp = msg.timestamp,
                      senderProfileId = msg.senderId,
                      senderProfileName = msg.senderName.takeIf { it.isNotBlank() } ?: matchProfile.name,
                      senderAvatarUrl = matchProfile.photosJoined.split("|||").firstOrNull(),
                      deepLinkTarget = "chat/${matchProfile.id}"
                    )
                    saveAndPushNotification(notif)
                  }
                }
              }
            }
          }
        }
      }

      // 4. Observe blocked user IDs (two-way) in real time
      launch {
        firestoreManager.observeBlockedUserIds(currentUserId).collect { blockedIds ->
          blockedIds.forEach { blockedId ->
            dao.deleteProfileById(blockedId)
            dao.deleteMessagesForMatch(blockedId)
            _realtimeBlockedEvent.emit(blockedId)
          }
        }
      }

      // 5. Observe and sync active subscription plan and swipe quota in real time
      launch {
        firestoreManager.observeSubscriptionState(currentUserId).collect { remoteSub ->
          if (remoteSub != null) {
            val localSub = dao.getSubscriptionFlow().firstOrNull()
            if (localSub == null || localSub.tierName != remoteSub.currentTier.name || localSub.swipesUsedThisMonth != remoteSub.swipesUsedThisMonth) {
              dao.saveSubscription(
                SubscriptionEntity(
                  id = "current_sub",
                  tierName = remoteSub.currentTier.name,
                  swipesUsedThisMonth = remoteSub.swipesUsedThisMonth,
                  currentMonthKey = remoteSub.currentMonthKey,
                  isAnnualBilling = remoteSub.isAnnualBilling,
                  subscriptionExpiryDate = remoteSub.subscriptionExpiryDate
                )
              )
            }
          }
        }
      }

      // 6. Observe deleted accounts in real time so deletions are purged immediately without refresh
      launch {
        firestoreManager.observeDeletedAccountIds().collect { deletedIds ->
          deletedIds.forEach { dId ->
            dao.deleteProfileById(dId)
            dao.deleteMessagesForMatch(dId)
          }
        }
      }

      // 6. Observe and stream user profile directly from backend database as the source of truth
      launch {
        firestoreManager.observeUserProfile(currentUserId).collect { remoteUserProfile ->
          if (remoteUserProfile != null && remoteUserProfile.name.isNotBlank()) {
            val localProfile = dao.getUserProfileFlow().firstOrNull()
            if (localProfile == null || localProfile.name != remoteUserProfile.name || localProfile.bio != remoteUserProfile.bio || localProfile.photosJoined != remoteUserProfile.photos.joinToString("|||")) {
              dao.saveUserProfile(remoteUserProfile.toEntity())
            }
          }
        }
      }

      // 7. Observe real-time cloud notifications sent from other users or system
      launch {
        firestoreManager.observeNotifications(currentUserId).collect { remoteNotifications ->
          remoteNotifications.forEach { notif ->
            saveAndPushNotification(notif)
          }
        }
      }

      // 8. Observe community profiles in real-time to immediately sync any profile edits (photos, bio, prompts, VIP, status, etc.) across all users without refresh
      launch {
        firestoreManager.observeCommunityProfiles(currentUserId).collect { remoteProfiles ->
          val blockedOrDeleted = if (firestoreManager.isAvailable) {
            firestoreManager.fetchAllBlockedOrBlockingUserIds(currentUserId) + firestoreManager.fetchAllDeletedAccountUserIds()
          } else {
            emptySet()
          }
          for (remote in remoteProfiles) {
            if (blockedOrDeleted.contains(remote.id)) {
              dao.deleteProfileById(remote.id)
              continue
            }
            val existing = dao.getProfileById(remote.id)
            if (remote.isAccountDisabled) {
              if (existing != null) {
                dao.setProfileDisabledStatus(remote.id, true)
              }
            } else {
              if (existing != null) {
                // Update all profile attributes in real time while preserving the user's existing swipe/match state
                val updatedEntity = remote.toEntity().copy(
                  likedMe = existing.likedMe,
                  isLikedByMe = existing.isLikedByMe,
                  isPassedByMe = existing.isPassedByMe,
                  isSuperLikedByMe = existing.isSuperLikedByMe,
                  isMutualMatch = existing.isMutualMatch,
                  matchedTimestamp = existing.matchedTimestamp,
                  isAccountDisabled = false
                )
                if (existing != updatedEntity) {
                  dao.insertProfile(updatedEntity)
                }
              } else {
                dao.insertProfile(remote.toEntity())
              }
            }
          }
        }
      }
    }
  }

  suspend fun seedDefaultProfiles() {
    loadDiscoverPage(page = 1, pageSize = DEFAULT_PAGE_SIZE)
  }

  /**
   * Generates or retrieves raw candidate profiles for a given page number (1-based).
   * Page 1: 1 - 20
   * Page 2: 21 - 40
   * Page 3: 41 - 60
   * Subsequent pages: procedurally generated high-quality profiles.
   */
  fun generatePageCandidates(page: Int, pageSize: Int = DEFAULT_PAGE_SIZE): List<ProfileEntity> {
    // All mock/sample data removed; candidates are loaded purely from database
    return emptyList()
  }

  /**
   * Paginates Discover profiles for the requested page while applying all strict exclusion rules:
   * Don't return:
   * - liked (isLikedByMe = 1 or isSuperLikedByMe = 1 or outgoing liked)
   * - passed (isPassedByMe = 1 or outgoing passed)
   * - matched (isMutualMatch = 1 or mutual matched)
   * - blocked (in blockedOrBlockingIds)
   * - deleted (in deletedAccountIds or disabled accounts)
   * - user's own profile
   * - candidates failing user's age, gender, or distance preferences
   */
  suspend fun loadDiscoverPage(page: Int, pageSize: Int = DEFAULT_PAGE_SIZE): List<DatingProfile> {
    val currentUser = dao.getUserProfileFlow().firstOrNull()?.toDomain() ?: UserProfile()
    val isVip = dao.getSubscriptionFlow().firstOrNull()?.tierName == com.example.data.model.SubscriptionTier.TIER_2.name
    val myUserId = getEffectiveCurrentUserId()

    // 1. Gather all excluded IDs across local and remote sources:
    val likedIds = dao.getLikedProfileIds().toSet()
    val passedIds = dao.getPassedProfileIds().toSet()
    val matchedIds = dao.getMatchedProfileIds().toSet()
    val blockedIds = if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      firestoreManager.fetchAllBlockedOrBlockingUserIds(myUserId)
    } else {
      emptySet()
    }
    val deletedIds = if (firestoreManager.isAvailable) {
      firestoreManager.fetchAllDeletedAccountUserIds()
    } else {
      emptySet()
    }
    val outgoingLiked = if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      firestoreManager.fetchOutgoingLikedUserIds(myUserId)
    } else {
      emptySet()
    }
    val outgoingPassed = if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      firestoreManager.fetchOutgoingPassedUserIds(myUserId)
    } else {
      emptySet()
    }
    val mutualMatched = if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      firestoreManager.fetchMutualMatchedUserIds(myUserId)
    } else {
      emptySet()
    }

    val allExcludedIds = likedIds + passedIds + matchedIds + blockedIds + deletedIds +
        outgoingLiked + outgoingPassed + mutualMatched + setOf(myUserId, "my_profile")

    // 2. Fetch real candidate profiles purely from database (Firestore discovery_profiles + local Room)
    val rawPageCandidates = mutableListOf<ProfileEntity>()

    if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      try {
        val cloudProfiles = firestoreManager.fetchAllCommunityProfiles(
          excludeUserId = myUserId,
          userLatitude = currentUser.latitude,
          userLongitude = currentUser.longitude,
          maxDistanceKm = if (currentUser.maxDistanceKm > 0) currentUser.maxDistanceKm else 50
        )
        for (cp in cloudProfiles) {
          if (!rawPageCandidates.any { it.id == cp.id }) {
            rawPageCandidates.add(cp.toEntity())
          }
        }
      } catch (e: Exception) {
        android.util.Log.w("KatkatRepository", "Notice fetching cloud community profiles: ${e.message}")
      }
    }

    val localDeckProfiles = dao.getActiveDeckProfilesPaged(
      excludeUserId = myUserId,
      limit = pageSize,
      offset = (page - 1) * pageSize
    )
    for (lp in localDeckProfiles) {
      if (!rawPageCandidates.any { it.id == lp.id }) {
        rawPageCandidates.add(lp)
      }
    }

    val currentPhone = currentUser.phoneNumber.filter { it.isDigit() }
    val currentName = currentUser.name.trim().lowercase()
    val maxDistance = if (currentUser.maxDistanceKm > 0) currentUser.maxDistanceKm else 50

    // 3. Apply all exclusion rules to the page candidates
    val eligibleCandidates = rawPageCandidates.mapNotNull { candidate ->
      // Exclusion 0: Exclude paused / hidden / disabled profiles
      if (candidate.isAccountDisabled) return@mapNotNull null

      // Exclusion 1: Not in any excluded IDs (liked, passed, matched, blocked, deleted, own profile)
      if (allExcludedIds.contains(candidate.id)) return@mapNotNull null
      if (candidate.isLikedByMe || candidate.isPassedByMe || candidate.isSuperLikedByMe || candidate.isMutualMatch) return@mapNotNull null

      // Exclusion 2: Prevent user's own profile from appearing (by ID, phone, or name)
      if (myUserId.isNotBlank() && candidate.id == myUserId) return@mapNotNull null
      if (currentUser.id.isNotBlank() && candidate.id == currentUser.id) return@mapNotNull null
      if (currentPhone.isNotBlank() && (candidate.id.contains(currentPhone) || candidate.id == "user_$currentPhone")) return@mapNotNull null
      if (currentUser.isOnboardingCompleted && currentName.isNotBlank() && candidate.name.trim().lowercase() == currentName) {
        if (currentUser.age > 0 && candidate.age == currentUser.age) return@mapNotNull null
      }

      // Exclusion 3: Filter by Gender / Interested in Preference
      val effectiveInterestedIn = when {
        currentUser.interestedInGender.isNotBlank() -> currentUser.interestedInGender
        currentUser.gender.equals("Man", ignoreCase = true) -> "Women"
        currentUser.gender.equals("Woman", ignoreCase = true) || currentUser.gender.equals("Women", ignoreCase = true) -> "Men"
        else -> "Everyone"
      }
      if (effectiveInterestedIn.equals("Women", ignoreCase = true)) {
        if (candidate.gender.isNotBlank() &&
            !candidate.gender.equals("Woman", ignoreCase = true) &&
            !candidate.gender.equals("Women", ignoreCase = true) &&
            !candidate.gender.equals("Female", ignoreCase = true)) {
          return@mapNotNull null
        }
      } else if (effectiveInterestedIn.equals("Men", ignoreCase = true)) {
        if (candidate.gender.isNotBlank() &&
            !candidate.gender.equals("Man", ignoreCase = true) &&
            !candidate.gender.equals("Men", ignoreCase = true) &&
            !candidate.gender.equals("Male", ignoreCase = true)) {
          return@mapNotNull null
        }
      }

      // Exclusion 4: Filter by Age Preference
      val minAge = if (currentUser.minAgePreference in 18..100) currentUser.minAgePreference else 18
      val maxAge = if (currentUser.maxAgePreference in 18..100) currentUser.maxAgePreference else 35
      if (candidate.age in 18..100 && (candidate.age < minAge || candidate.age > maxAge)) {
        return@mapNotNull null
      }

      // Exclusion 4b: VIP Extra Preference Filtering (Only active for VIP tier)
      if (isVip) {
        if (currentUser.preferDatingIntention.isNotBlank() && !currentUser.preferDatingIntention.equals("Any", ignoreCase = true)) {
          if (candidate.datingIntention.isNotBlank() && !candidate.datingIntention.equals(currentUser.preferDatingIntention, ignoreCase = true)) {
            return@mapNotNull null
          }
        }
        if (currentUser.preferDrinking.isNotBlank() && !currentUser.preferDrinking.equals("Any", ignoreCase = true)) {
          if (candidate.drinking.isNotBlank() && !candidate.drinking.equals(currentUser.preferDrinking, ignoreCase = true)) {
            return@mapNotNull null
          }
        }
        if (currentUser.preferSmoking.isNotBlank() && !currentUser.preferSmoking.equals("Any", ignoreCase = true)) {
          if (candidate.smoking.isNotBlank() && !candidate.smoking.equals(currentUser.preferSmoking, ignoreCase = true)) {
            return@mapNotNull null
          }
        }
        if (currentUser.preferZodiac.isNotBlank() && !currentUser.preferZodiac.equals("Any", ignoreCase = true)) {
          if (candidate.zodiac.isNotBlank() && !candidate.zodiac.equals(currentUser.preferZodiac, ignoreCase = true)) {
            return@mapNotNull null
          }
        }
      }

      // Exclusion 5: Filter by Distance Preference
      val candidateDistanceKm: Double = when {
        currentUser.latitude != 0.0 && currentUser.longitude != 0.0 &&
        candidate.latitude != 0.0 && candidate.longitude != 0.0 -> {
          calculateHaversineDistanceKm(
            currentUser.latitude, currentUser.longitude,
            candidate.latitude, candidate.longitude
          )
        }
        else -> {
          val matchKm = Regex("""(\d+(?:\.\d+)?)\s*km\s*away""", RegexOption.IGNORE_CASE).find(candidate.location)
          if (matchKm != null) {
            matchKm.groupValues[1].toDoubleOrNull() ?: 0.0
          } else {
            val matchMiles = Regex("""(\d+(?:\.\d+)?)\s*miles\s*away""", RegexOption.IGNORE_CASE).find(candidate.location)
            if (matchMiles != null) {
              (matchMiles.groupValues[1].toDoubleOrNull() ?: 0.0) * 1.60934
            } else {
              5.0
            }
          }
        }
      }

      if (candidateDistanceKm > maxDistance) {
        return@mapNotNull null
      }

      // Distance privacy masking & display formatting
      val privacyMaskedLocation = when {
        candidateDistanceKm in 0.01..1.0 -> "Less than 1 km away"
        candidateDistanceKm > 1.0 -> "${Math.round(candidateDistanceKm)} km away"
        else -> candidate.location.ifBlank { "Nearby" }
      }

      candidate.copy(
        location = privacyMaskedLocation
      )
    }

    // 4. Save eligible candidates into local DB so reactive flow emits them
    if (eligibleCandidates.isNotEmpty()) {
      dao.insertProfiles(eligibleCandidates)
    }

    return eligibleCandidates.map { it.toDomain() }
  }

  // User Profile
  val userProfile: Flow<UserProfile> = dao.getUserProfileFlow().map { entity ->
    entity?.toDomain() ?: UserProfile(isOnboardingCompleted = false)
  }

  // Subscription State combined with monthly swipe record counts
  val subscriptionState: Flow<SubscriptionState> = combine(
    dao.getSubscriptionFlow(),
    dao.getMonthlySwipeCountFlow("2026-09")
  ) { subEntity, recordedSwipes ->
    val tier = try {
      SubscriptionTier.valueOf(subEntity?.tierName ?: SubscriptionTier.FREE.name)
    } catch (_: Exception) {
      SubscriptionTier.FREE
    }
    val effectiveSwipesUsed = maxOf(subEntity?.swipesUsedThisMonth ?: 0, recordedSwipes)
    SubscriptionState(
      currentTier = tier,
      swipesUsedThisMonth = effectiveSwipesUsed,
      currentMonthKey = subEntity?.currentMonthKey ?: "2026-09",
      isAnnualBilling = subEntity?.isAnnualBilling ?: false,
      subscriptionExpiryDate = subEntity?.subscriptionExpiryDate ?: "Renews Oct 16, 2026",
      isRevenueCatConnected = true
    )
  }

  // Active Discover Deck (strictly excluding current user's profile and filtered by Discovery Preferences)
  val activeProfiles: Flow<List<DatingProfile>> = combine(
    userProfile,
    subscriptionState
  ) { currentUser, subState ->
    Pair(currentUser, subState)
  }.flatMapLatest { (currentUser, subState) ->
    val isVip = subState.currentTier == SubscriptionTier.TIER_2
    val authUid = phoneAuthManager.currentUserId
    val excludeId = when {
      !authUid.isNullOrBlank() -> authUid
      currentUser.id.isNotBlank() && currentUser.id != "my_profile" -> currentUser.id
      else -> {
        val cleanPhone = currentUser.phoneNumber.filter { it.isDigit() }
        if (cleanPhone.isNotBlank()) "user_$cleanPhone" else currentUser.id.ifBlank { null }
      }
    }
    val currentPhone = currentUser.phoneNumber.filter { it.isDigit() }
    val currentName = currentUser.name.trim().lowercase()

    // Determine effective "Interested In" filter:
    // If explicitly saved in preferences, use it.
    // Otherwise: Man -> Women, Woman/Women -> Men, Else -> Everyone
    val effectiveInterestedIn = when {
      currentUser.interestedInGender.isNotBlank() -> currentUser.interestedInGender
      currentUser.gender.equals("Man", ignoreCase = true) -> "Women"
      currentUser.gender.equals("Woman", ignoreCase = true) || currentUser.gender.equals("Women", ignoreCase = true) -> "Men"
      else -> "Everyone"
    }

    val minAge = if (currentUser.minAgePreference in 18..100) currentUser.minAgePreference else 18
    val maxAge = if (currentUser.maxAgePreference in 18..100) currentUser.maxAgePreference else 35
    val maxDistance = if (currentUser.maxDistanceKm > 0) currentUser.maxDistanceKm else 50

    dao.getActiveDeckProfiles(excludeUserId = excludeId).map { entities ->
      entities.mapNotNull { entity ->
        // Multi-factor exclusion to ensure user's own profile never appears in discover deck:
        // 0. Exclude paused / hidden / disabled profiles
        if (entity.isAccountDisabled) return@mapNotNull null

        // 1. By ID
        if (excludeId != null && entity.id == excludeId) return@mapNotNull null
        if (currentUser.id.isNotBlank() && entity.id == currentUser.id) return@mapNotNull null
        if (!authUid.isNullOrBlank() && entity.id == authUid) return@mapNotNull null
        // 2. By phone number pattern
        if (currentPhone.isNotBlank() && (entity.id.contains(currentPhone) || entity.id == "user_$currentPhone")) return@mapNotNull null
        // 3. By matching completed user's name if identical and user is onboarded
        if (currentUser.isOnboardingCompleted && currentName.isNotBlank() && entity.name.trim().lowercase() == currentName) {
          // If age or bio also matches, exclude to avoid showing self
          if (currentUser.age > 0 && entity.age == currentUser.age) return@mapNotNull null
        }

        // 4. Filter by Age Preference (18 to 75+)
        if (entity.age > 0 && (entity.age < minAge || entity.age > maxAge)) {
          return@mapNotNull null
        }

        // 5. Filter by Interested In / Gender Preference
        if (effectiveInterestedIn.equals("Women", ignoreCase = true)) {
          if (entity.gender.isNotBlank() &&
              !entity.gender.equals("Woman", ignoreCase = true) &&
              !entity.gender.equals("Women", ignoreCase = true) &&
              !entity.gender.equals("Female", ignoreCase = true)) {
            return@mapNotNull null
          }
        } else if (effectiveInterestedIn.equals("Men", ignoreCase = true)) {
          if (entity.gender.isNotBlank() &&
              !entity.gender.equals("Man", ignoreCase = true) &&
              !entity.gender.equals("Men", ignoreCase = true) &&
              !entity.gender.equals("Male", ignoreCase = true)) {
            return@mapNotNull null
          }
        }

        // 6. VIP Extra Preference Filtering (Only active for VIP tier)
        if (isVip) {
          if (currentUser.preferDatingIntention.isNotBlank() && !currentUser.preferDatingIntention.equals("Any", ignoreCase = true)) {
            if (entity.datingIntention.isNotBlank() && !entity.datingIntention.equals(currentUser.preferDatingIntention, ignoreCase = true)) {
              return@mapNotNull null
            }
          }
          if (currentUser.preferDrinking.isNotBlank() && !currentUser.preferDrinking.equals("Any", ignoreCase = true)) {
            if (entity.drinking.isNotBlank() && !entity.drinking.equals(currentUser.preferDrinking, ignoreCase = true)) {
              return@mapNotNull null
            }
          }
          if (currentUser.preferSmoking.isNotBlank() && !currentUser.preferSmoking.equals("Any", ignoreCase = true)) {
            if (entity.smoking.isNotBlank() && !entity.smoking.equals(currentUser.preferSmoking, ignoreCase = true)) {
              return@mapNotNull null
            }
          }
          if (currentUser.preferZodiac.isNotBlank() && !currentUser.preferZodiac.equals("Any", ignoreCase = true)) {
            if (entity.zodiac.isNotBlank() && !entity.zodiac.equals(currentUser.preferZodiac, ignoreCase = true)) {
              return@mapNotNull null
            }
          }
        }

        // 6. Backend Distance Calculation & Filtering
        val candidateDistanceKm: Double = when {
          currentUser.latitude != 0.0 && currentUser.longitude != 0.0 &&
          entity.latitude != 0.0 && entity.longitude != 0.0 -> {
            calculateHaversineDistanceKm(
              currentUser.latitude, currentUser.longitude,
              entity.latitude, entity.longitude
            )
          }
          else -> {
            val matchKm = Regex("""(\d+(?:\.\d+)?)\s*km\s*away""", RegexOption.IGNORE_CASE).find(entity.location)
            if (matchKm != null) {
              matchKm.groupValues[1].toDoubleOrNull() ?: 0.0
            } else {
              val matchMiles = Regex("""(\d+(?:\.\d+)?)\s*miles\s*away""", RegexOption.IGNORE_CASE).find(entity.location)
              if (matchMiles != null) {
                (matchMiles.groupValues[1].toDoubleOrNull() ?: 0.0) * 1.60934
              } else {
                5.0
              }
            }
          }
        }

        // Distance constraint: If candidate distance exceeds user's maximum distance filter (e.g. 32 km > 25 km), exclude
        if (candidateDistanceKm > maxDistance) {
          return@mapNotNull null
        }

        // Privacy protection: The exact location (latitude, longitude, exact address) is not exposed to other users.
        // Instead, the display location is formatted to approximate relative distance (e.g. "5 km away", "18 km away").
        val privacyMaskedLocation = when {
          candidateDistanceKm in 0.01..1.0 -> "Less than 1 km away"
          candidateDistanceKm > 1.0 -> "${Math.round(candidateDistanceKm)} km away"
          else -> entity.location.ifBlank { "Nearby" }
        }

        entity.toDomain().copy(
          location = privacyMaskedLocation,
          latitude = 0.0, // Privacy protection: exact coordinates hidden
          longitude = 0.0 // Privacy protection: exact coordinates hidden
        )
      }
    }
  }

  // Mutual Matches
  val mutualMatches: Flow<List<DatingProfile>> = dao.getMutualMatches().map { entities ->
    entities.map { it.toDomain() }
  }

  // Profiles Who Liked Current User (for "Likes You" tab, with backend exclusion of blocked, deleted, and matched users)
  val profilesWhoLikedMe: Flow<List<DatingProfile>> = combine(
    dao.getProfilesWhoLikedMe(),
    userProfile
  ) { entities, currentUser ->
    val myUserId = getEffectiveCurrentUserId()
    val blockedIds = if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      firestoreManager.fetchAllBlockedOrBlockingUserIds(myUserId)
    } else {
      emptySet()
    }
    val deletedIds = if (firestoreManager.isAvailable) {
      firestoreManager.fetchAllDeletedAccountUserIds()
    } else {
      emptySet()
    }

    entities.mapNotNull { entity ->
      if (entity.isAccountDisabled) return@mapNotNull null
      if (entity.isMutualMatch || entity.isPassedByMe || entity.isLikedByMe) return@mapNotNull null
      if (blockedIds.contains(entity.id) || deletedIds.contains(entity.id)) return@mapNotNull null
      if (entity.id == myUserId || entity.id == currentUser.id || entity.id == "my_profile") return@mapNotNull null
      entity.toDomain()
    }
  }

  suspend fun passFromLikesYou(profileId: String) {
    if (profileId.isBlank()) return
    dao.passFromLikesYou(profileId)
    if (firestoreManager.isAvailable) {
      val myUserId = getEffectiveCurrentUserId()
      if (myUserId.isNotBlank()) {
        appScope.launch {
          firestoreManager.sendPass(myUserId, profileId)
        }
      }
    }
  }

  suspend fun processSwipe(
    profileId: String,
    action: SwipeAction
  ): SwipeResult {
    if (profileId.isBlank()) return SwipeResult.Error("Invalid profile ID")

    // 1. Concurrency guard: Prevent duplicate simultaneous taps on the same profile
    if (!pendingSwipeIds.add(profileId)) {
      Log.d("KatkatRepository", "Concurrent swipe ignored for profile: $profileId")
      val existingProfile = dao.getProfileById(profileId)
      return if (existingProfile != null && existingProfile.isMutualMatch) {
        SwipeResult.MutualMatch(existingProfile.toDomain())
      } else {
        SwipeResult.Success(action)
      }
    }

    try {
      val profile = dao.getProfileById(profileId) ?: return SwipeResult.Error("Profile not found")

      // 2. Idempotency check: If profile was already liked/passed/superliked, do not create duplicate records
      if (profile.isLikedByMe || profile.isPassedByMe || profile.isSuperLikedByMe) {
        Log.d("KatkatRepository", "Idempotent swipe: Profile $profileId was already processed (liked=${profile.isLikedByMe}, passed=${profile.isPassedByMe})")
        return if (profile.isMutualMatch) {
          SwipeResult.MutualMatch(profile.toDomain())
        } else {
          SwipeResult.Success(action)
        }
      }

      val sub = dao.getSubscriptionFlow().firstOrNull()
      val tier = try {
        SubscriptionTier.valueOf(sub?.tierName ?: SubscriptionTier.FREE.name)
      } catch (_: Exception) {
        SubscriptionTier.FREE
      }
      val recordedSwipes = dao.getMonthlySwipeCount("2026-09")
      val currentSwipes = maxOf(sub?.swipesUsedThisMonth ?: 0, recordedSwipes)

      if (currentSwipes >= tier.monthlySwipes) {
        return SwipeResult.LimitReached(tier, currentSwipes)
      }

      val monthKey = "2026-09"
      val existingSwipeRecord = dao.getSwipeRecordForProfile(profileId, monthKey)

      // Only increment monthly quota and create a swipe record if not already recorded
      if (existingSwipeRecord == null) {
        val newSwipeCount = currentSwipes + 1
        dao.insertSwipeRecord(
          SwipeRecordEntity(
            profileId = profileId,
            actionType = action.name,
            monthKey = monthKey
          )
        )

        dao.saveSubscription(
          SubscriptionEntity(
            id = "current_sub",
            tierName = tier.name,
            swipesUsedThisMonth = newSwipeCount,
            currentMonthKey = monthKey,
            isAnnualBilling = sub?.isAnnualBilling ?: false,
            subscriptionExpiryDate = sub?.subscriptionExpiryDate ?: "Renews Oct 16, 2026"
          )
        )
        // Sync updated swipe counts & plan details to Cloud Firestore backend
        appScope.launch {
          syncSubscriptionToCloud()
        }
      }

      val now = System.currentTimeMillis()
      var isMutual = false

      when (action) {
        SwipeAction.LIKE -> {
          isMutual = profile.likedMe
          dao.markLiked(profileId, isMutual = isMutual, matchedTimestamp = if (isMutual) now else null)
        }
        SwipeAction.PASS -> {
          dao.markPassed(profileId)
          if (firestoreManager.isAvailable) {
            val myUserId = getEffectiveCurrentUserId()
            if (myUserId.isNotBlank()) {
              appScope.launch {
                firestoreManager.sendPass(myUserId, profileId)
              }
            }
          }
        }
        SwipeAction.SUPERLIKE -> {
          isMutual = true
          dao.markSuperLiked(profileId, matchedTimestamp = now)
        }
      }

      // Push like to Cloud Firestore for real-time cross-device match synchronization
      // Cloud Firestore uses deterministic docId "${fromUserId}_$toUserId" with SetOptions.merge()
      // to strictly guarantee single idempotent records in the cloud as well.
      if ((action == SwipeAction.LIKE || action == SwipeAction.SUPERLIKE) && firestoreManager.isAvailable) {
        val myUserId = getEffectiveCurrentUserId()
        val localUser = dao.getUserProfileFlow().firstOrNull()
        val myName = localUser?.name?.takeIf { it.isNotBlank() } ?: "Alex"
        val myAvatar = localUser?.photosJoined?.split("|||")?.firstOrNull()

        if (myUserId.isNotBlank()) {
          appScope.launch {
            val isSuper = action == SwipeAction.SUPERLIKE
            firestoreManager.sendLike(myUserId, profileId, isSuper)

            // Double check if other user already liked me in Cloud Firestore
            if (!isMutual) {
              val otherLikedMeCloud = firestoreManager.checkMutualLike(myUserId, profileId)
              if (otherLikedMeCloud || isSuper) {
                val matchTime = System.currentTimeMillis()
                dao.markMutualMatch(profileId, matchTime)
                firestoreManager.registerMutualMatch(myUserId, profileId)

                // Rule: If it becomes mutual: "Yaaa! You have a new match!"
                val matchKey = "${minOf(myUserId, profileId)}_${maxOf(myUserId, profileId)}"
                val matchNotifForPartner = KatkatNotification(
                  id = "match_$matchKey",
                  userId = profileId,
                  type = KatkatNotificationType.NEW_MATCH,
                  title = "It's a Match! 🎉",
                  message = "Yaaa! You have a new match!",
                  timestamp = matchTime,
                  senderProfileId = myUserId,
                  senderProfileName = myName,
                  senderAvatarUrl = myAvatar,
                  deepLinkTarget = "chat/$myUserId"
                )
                firestoreManager.sendNotification(profileId, matchNotifForPartner)

                val matchNotifForMe = KatkatNotification(
                  id = "match_$matchKey",
                  userId = myUserId,
                  type = KatkatNotificationType.NEW_MATCH,
                  title = "It's a Match! 🎉",
                  message = "Yaaa! You have a new match!",
                  timestamp = matchTime,
                  senderProfileId = profileId,
                  senderProfileName = profile.name,
                  senderAvatarUrl = profile.photosJoined.split("|||").firstOrNull(),
                  deepLinkTarget = "chat/$profileId"
                )
                saveAndPushNotification(matchNotifForMe)

                val updatedProfile = dao.getProfileById(profileId)?.toDomain()
                if (updatedProfile != null) {
                  _realtimeMatchEvent.emit(updatedProfile)
                }
              } else {
                // Rule: Profile 2 likes Profile 1 -> Profile 1 receives notification "Someone liked you"
                val likeNotif = KatkatNotification(
                  id = "like_${myUserId}",
                  userId = profileId,
                  type = KatkatNotificationType.PROFILE_ACTIVITY,
                  title = "New Like! ✨",
                  message = "Someone liked you",
                  timestamp = System.currentTimeMillis(),
                  senderProfileId = myUserId,
                  senderProfileName = myName,
                  senderAvatarUrl = myAvatar,
                  deepLinkTarget = "likes_you"
                )
                firestoreManager.sendNotification(profileId, likeNotif)
              }
            } else {
              // It was mutual locally, register in cloud
              val matchTime = System.currentTimeMillis()
              firestoreManager.registerMutualMatch(myUserId, profileId)

              // Push match notification to both
              val matchKey = "${minOf(myUserId, profileId)}_${maxOf(myUserId, profileId)}"
              val matchNotifForPartner = KatkatNotification(
                id = "match_$matchKey",
                userId = profileId,
                type = KatkatNotificationType.NEW_MATCH,
                title = "It's a Match! 🎉",
                message = "Yaaa! You have a new match!",
                timestamp = matchTime,
                senderProfileId = myUserId,
                senderProfileName = myName,
                senderAvatarUrl = myAvatar,
                deepLinkTarget = "chat/$myUserId"
              )
              firestoreManager.sendNotification(profileId, matchNotifForPartner)

              val matchNotifForMe = KatkatNotification(
                id = "match_$matchKey",
                userId = myUserId,
                type = KatkatNotificationType.NEW_MATCH,
                title = "It's a Match! 🎉",
                message = "Yaaa! You have a new match!",
                timestamp = matchTime,
                senderProfileId = profileId,
                senderProfileName = profile.name,
                senderAvatarUrl = profile.photosJoined.split("|||").firstOrNull(),
                deepLinkTarget = "chat/$profileId"
              )
              saveAndPushNotification(matchNotifForMe)
            }
          }
        }
      }

      if (isMutual) {
        return SwipeResult.MutualMatch(profile.toDomain())
      }

      return SwipeResult.Success(action)
    } finally {
      pendingSwipeIds.remove(profileId)
    }
  }

  suspend fun syncSubscriptionToCloud() {
    if (!firestoreManager.isAvailable) return
    val myUserId = getEffectiveCurrentUserId()
    if (myUserId.isBlank()) return
    val currentSubState = subscriptionState.firstOrNull() ?: return
    firestoreManager.syncSubscriptionState(myUserId, currentSubState)
  }

  suspend fun rewindLastSwipe(): Boolean {
    val lastSwipe = dao.getLastSwipeRecord() ?: return false
    dao.rewindSwipe(lastSwipe.profileId)
    dao.deleteSwipeRecord(lastSwipe.id)

    val sub = dao.getSubscriptionFlow().firstOrNull()
    if (sub != null && sub.swipesUsedThisMonth > 0) {
      dao.saveSubscription(
        sub.copy(swipesUsedThisMonth = (sub.swipesUsedThisMonth - 1).coerceAtLeast(0))
      )
      appScope.launch {
        syncSubscriptionToCloud()
      }
    }
    return true
  }

  suspend fun upgradeSubscription(tier: SubscriptionTier, isAnnual: Boolean): Boolean {
    val existing = dao.getSubscriptionFlow().firstOrNull()
    dao.saveSubscription(
      SubscriptionEntity(
        id = "current_sub",
        tierName = tier.name,
        swipesUsedThisMonth = existing?.swipesUsedThisMonth ?: 0,
        currentMonthKey = "2026-09",
        isAnnualBilling = isAnnual,
        subscriptionExpiryDate = if (isAnnual) "Renews Sep 16, 2027" else "Renews Oct 16, 2026"
      )
    )
    appScope.launch {
      syncSubscriptionToCloud()
    }
    return true
  }

  suspend fun resetSwipeCounter(): Boolean {
    val sub = dao.getSubscriptionFlow().firstOrNull()
    if (sub != null) {
      dao.saveSubscription(sub.copy(swipesUsedThisMonth = 0))
      appScope.launch {
        syncSubscriptionToCloud()
      }
    }
    return true
  }

  // Chat Messages
  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  fun getMessages(matchId: String): Flow<List<ChatMessage>> {
    return userProfile.flatMapLatest { profile ->
      val myUserId = profile.id
      if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
        appScope.launch {
          firestoreManager.observeChatMessages(matchId, myUserId).collect { remoteMessages ->
            remoteMessages.forEach { msg ->
              dao.insertMessage(msg.toEntity(ownerUserId = myUserId))
            }
          }
        }
      }
      dao.getMessagesForMatch(matchId, myUserId).map { list ->
        list.map { it.toDomain() }
      }
    }
  }

  suspend fun sendMessage(matchId: String, text: String, photoUri: String? = null) {
    val myUserId = getEffectiveCurrentUserId()
    val localUser = dao.getUserProfileFlow().firstOrNull()
    val myName = localUser?.name?.takeIf { it.isNotBlank() } ?: "Alex"
    val convId = com.example.util.EndToEndEncryptionHelper.getConversationId(myUserId, matchId)

    val msgId = UUID.randomUUID().toString()
    val myMessage = ChatMessageEntity(
      id = msgId,
      conversationId = convId,
      matchId = matchId,
      senderId = myUserId,
      recipientId = matchId,
      currentUserId = myUserId,
      senderName = myName,
      text = text,
      photoUri = photoUri,
      timestamp = System.currentTimeMillis(),
      isFromMe = true,
      isRead = true,
      isSending = firestoreManager.isAvailable,
      isFailed = false
    )
    dao.insertMessage(myMessage)

    // Sync sent message to Cloud Firestore in real time
    if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      appScope.launch {
        val success = try {
          firestoreManager.sendChatMessage(matchId, myMessage.toDomain().copy(isSending = false, isFailed = false), myUserId)
        } catch (e: Exception) {
          Log.w("KatkatRepository", "Error sending cloud message: ${e.message}")
          false
        }

        if (success) {
          dao.insertMessage(myMessage.copy(isSending = false, isFailed = false))
          // Push notification strictly to the recipient's user account collection: "Profile 2 sent you a message"
          val msgNotif = KatkatNotification(
            id = "msg_${myMessage.id}",
            userId = matchId,
            type = KatkatNotificationType.NEW_MESSAGE,
            title = "New Message 💬",
            message = "$myName sent you a message",
            timestamp = myMessage.timestamp,
            senderProfileId = myUserId,
            senderProfileName = myName,
            senderAvatarUrl = localUser?.photosJoined?.split("|||")?.firstOrNull(),
            deepLinkTarget = "chat/$myUserId"
          )
          firestoreManager.sendNotification(matchId, msgNotif)
        } else {
          dao.insertMessage(myMessage.copy(isSending = false, isFailed = true))
        }
      }
    } else {
      dao.insertMessage(myMessage.copy(isSending = false, isFailed = false))
    }
  }

  suspend fun retrySendMessage(messageId: String) {
    val existing = dao.getMessageById(messageId) ?: return
    val myUserId = getEffectiveCurrentUserId()
    val localUser = dao.getUserProfileFlow().firstOrNull()
    val myName = localUser?.name?.takeIf { it.isNotBlank() } ?: "Alex"

    dao.insertMessage(existing.copy(isSending = true, isFailed = false))

    if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      appScope.launch {
        val success = try {
          firestoreManager.sendChatMessage(existing.matchId, existing.toDomain().copy(isSending = false, isFailed = false), myUserId)
        } catch (e: Exception) {
          false
        }

        if (success) {
          dao.insertMessage(existing.copy(isSending = false, isFailed = false))
          val msgNotif = KatkatNotification(
            id = "msg_${existing.id}",
            userId = existing.matchId,
            type = KatkatNotificationType.NEW_MESSAGE,
            title = "New Message 💬",
            message = "$myName sent you a message",
            timestamp = existing.timestamp,
            senderProfileId = myUserId,
            senderProfileName = myName,
            senderAvatarUrl = localUser?.photosJoined?.split("|||")?.firstOrNull(),
            deepLinkTarget = "chat/$myUserId"
          )
          firestoreManager.sendNotification(existing.matchId, msgNotif)
        } else {
          dao.insertMessage(existing.copy(isSending = false, isFailed = true))
        }
      }
    } else {
      dao.insertMessage(existing.copy(isSending = false, isFailed = false))
    }
  }

  suspend fun reportProfile(matchId: String, reason: String, details: String = "") {
    val myUserId = getEffectiveCurrentUserId()
    if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      try {
        firestoreManager.reportUser(myUserId, matchId, reason, details)
      } catch (e: Exception) {
        Log.w("KatkatRepository", "Notice reporting profile: ${e.message}")
      }
    }
  }

  suspend fun markMessagesRead(matchId: String) {
    val myUserId = getEffectiveCurrentUserId()
    dao.markMessagesAsRead(matchId, myUserId)
    if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      val localUser = dao.getUserProfileFlow().firstOrNull()
      val myName = localUser?.name?.takeIf { it.isNotBlank() } ?: "Alex"

      appScope.launch {
        firestoreManager.markChatMessagesAsRead(matchId, myUserId)

        // Rule: MESSAGE_READ notification sent strictly to the original sender's user account
        val readNotif = KatkatNotification(
          id = "read_${myUserId}",
          userId = matchId,
          type = KatkatNotificationType.MESSAGE_READ,
          title = "Message Read 👀",
          message = "$myName read your message",
          timestamp = System.currentTimeMillis(),
          senderProfileId = myUserId,
          senderProfileName = myName,
          senderAvatarUrl = localUser?.photosJoined?.split("|||")?.firstOrNull(),
          deepLinkTarget = "chat/$myUserId"
        )
        firestoreManager.sendNotification(matchId, readNotif)
      }
    }
  }

  // Reactive conversations stream combining mutual matches with their message threads isolated by logged-in user
  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val conversations: Flow<List<MatchConversation>> = userProfile.flatMapLatest { profile ->
    val currentUserId = profile.id
    combine(
      mutualMatches,
      dao.getAllMessagesFlow(currentUserId)
    ) { matches, allMessages ->
      val messagesByMatch = allMessages.groupBy { it.matchId }
      matches.map { matchProfile ->
        val convId = com.example.util.EndToEndEncryptionHelper.getConversationId(currentUserId, matchProfile.id)
        val matchMsgs = messagesByMatch[matchProfile.id].orEmpty().sortedBy { it.timestamp }
        val lastMsg = matchMsgs.lastOrNull()
        val unreadCount = matchMsgs.count { !it.isFromMe && !it.isRead }
        val lastText = when {
          lastMsg?.photoUri != null && !lastMsg.text.isNullOrBlank() -> "📷 ${lastMsg.text}"
          lastMsg?.photoUri != null -> "📷 Sent a photo"
          lastMsg != null -> lastMsg.text
          else -> "New match! Say hello 👋"
        }
        val lastTime = lastMsg?.timestamp ?: (matchProfile.matchedTimestamp ?: (System.currentTimeMillis() - 3600_000L))

        MatchConversation(
          matchProfile = matchProfile,
          conversationId = convId,
          matchTimeMillis = matchProfile.matchedTimestamp ?: lastTime,
          lastMessage = lastText,
          lastMessageTimeMillis = lastTime,
          unreadCount = unreadCount,
          isOnline = true
        )
      }.sortedByDescending { it.lastMessageTimeMillis }
    }
  }

  suspend fun deleteMessagesForMatch(matchId: String) {
    dao.deleteMessagesForMatch(matchId)
    if (firestoreManager.isAvailable) {
      val myUserId = getEffectiveCurrentUserId()
      firestoreManager.deleteChatMessages(matchId, myUserId)
    }
  }

  suspend fun unmatch(matchId: String) {
    val myUserId = getEffectiveCurrentUserId()
    if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      try {
        firestoreManager.unmatchUser(myUserId, matchId)
      } catch (e: Exception) {
        Log.w("KatkatRepository", "Notice unmatching in Firestore: ${e.message}")
      }
    }
    dao.unmatchProfile(matchId)
    deleteMessagesForMatch(matchId)
  }

  suspend fun blockProfile(matchId: String) {
    val myUserId = getEffectiveCurrentUserId()
    if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      try {
        firestoreManager.blockUser(myUserId, matchId)
        firestoreManager.deleteChatMessages(matchId, myUserId)
      } catch (e: Exception) {
        Log.w("KatkatRepository", "Notice blocking user in Firestore: ${e.message}")
      }
    }
    dao.unmatchProfile(matchId)
    dao.deleteProfileById(matchId)
    deleteMessagesForMatch(matchId)
    _realtimeBlockedEvent.emit(matchId)
  }

  suspend fun createSimulatedTestMatch(): DatingProfile? {
    // Mock test matches removed - everything must come from real database
    return null
  }

  fun getProfileFlow(profileId: String): Flow<DatingProfile?> {
    return dao.getProfileByIdFlow(profileId).map { it?.toDomain() }
  }

  // Profile update with real-time Firestore sync and community discovery publishing
  suspend fun saveUserProfile(profile: UserProfile) {
    val cleanPhone = profile.phoneNumber.filter { it.isDigit() }
    val authUid = phoneAuthManager.currentUserId
    val uniqueId = if (profile.id.isNotBlank() && profile.id != "my_profile") {
      profile.id
    } else if (!authUid.isNullOrBlank()) {
      authUid
    } else if (cleanPhone.isNotBlank()) {
      "user_$cleanPhone"
    } else {
      "user_${System.currentTimeMillis()}"
    }

    // Convert any remaining local device photo URIs to Cloud Storage download URLs so they load across all devices
    val hasLocalUris = profile.photos.any { !it.startsWith("http://") && !it.startsWith("https://") }
    val cloudPhotos = if (hasLocalUris) {
      val context = try { com.example.KatkatApplication.appContext } catch (_: Exception) { null }
      if (context != null) {
        profile.photos.map { photoUri ->
          if (photoUri.startsWith("http://") || photoUri.startsWith("https://")) {
            photoUri
          } else {
            try {
              val uploaded = firebaseStorageManager.uploadProfileImage(context, uniqueId, android.net.Uri.parse(photoUri))
              if (uploaded.isNotBlank()) uploaded else photoUri
            } catch (e: Exception) {
              Log.w("KatkatRepository", "Could not upload local photo to Cloud Storage: ${e.message}")
              photoUri
            }
          }
        }.filter { it.isNotBlank() }
      } else {
        profile.photos.filter { it.startsWith("http://") || it.startsWith("https://") }
      }
    } else {
      profile.photos
    }

    val fixedProfile = profile.copy(id = uniqueId, photos = cloudPhotos)

    val currentLocal = dao.getUserProfileFlow().firstOrNull()
    if (currentLocal != null && currentLocal.id.isNotBlank() && currentLocal.id != uniqueId && currentLocal.id != "my_profile") {
      // Switched to a different profile - purge previous user's local chats and swipe cache
      dao.deleteAllMessages()
      dao.deleteAllSwipeRecords()
      dao.deleteAllProfiles()
    }

    // 1. Save directly to Cloud Firestore backend as the authoritative source of truth
    if (firestoreManager.isAvailable) {
      try {
        firestoreManager.syncUserProfile(fixedProfile)
        if (fixedProfile.isAccountDisabled) {
          firestoreManager.unpublishUserFromDiscovery(fixedProfile.id)
        } else if (fixedProfile.isOnboardingCompleted && fixedProfile.name.isNotBlank()) {
          firestoreManager.publishUserToDiscovery(fixedProfile)
          syncCommunityRegisteredUsers(fixedProfile.id)
        }
      } catch (e: Exception) {
        Log.w("KatkatRepository", "Firestore profile sync notice: ${e.message}")
      }
    }

    // 2. Update local database cache to reflect backend data
    dao.saveUserProfile(fixedProfile.toEntity())
    dao.deleteOtherUserProfiles(uniqueId)
  }

  suspend fun logoutActiveSession() {
    realTimeSyncJob?.cancel()
    realTimeSyncJob = null
    recentlyProcessedNotifIds.clear()
    KatkatApplication.activeChatPartnerId = null

    val currentUserId = getEffectiveCurrentUserId()
    phoneAuthManager.signOut()
    // Delete active user_profile, subscription, local cached chats, swipe history, notifications, and profiles deck
    // so no previous user's chat, match, or subscription data remains on the device.
    dao.deleteUserProfile()
    dao.deleteSubscription()
    dao.deleteAllMessages()
    dao.deleteAllNotifications(currentUserId)
    dao.deleteAllSwipeRecords()
    dao.deleteAllProfiles()
    Log.d("KatkatRepository", "Logged out active session and cleared local user data cleanly for user: $currentUserId")
  }

  /**
   * Checks if this phone number already belongs to a registered user with a completed profile.
   * Checks both local database and Firestore cloud database.
   */
  suspend fun checkExistingUserByPhone(phoneNumber: String, countryCode: String): UserProfile? {
    val cleanPhone = phoneNumber.filter { it.isDigit() }
    val cleanCountryCode = countryCode.filter { it.isDigit() }
    val fullWithPlus = if (phoneNumber.startsWith("+")) phoneNumber else "+$cleanCountryCode$cleanPhone"
    val fullWithSpace = "$countryCode $phoneNumber"

    val local = dao.getUserByPhone(phoneNumber)
      ?: dao.getUserByPhone(cleanPhone)
      ?: dao.getUserByPhone(fullWithPlus)
      ?: dao.getUserByPhone(fullWithSpace)
      ?: dao.getUserByPhone("$cleanCountryCode$cleanPhone")

    if (local != null && local.isOnboardingCompleted && local.name.isNotBlank()) {
      Log.d("KatkatRepository", "Existing user found in local DB for phone $phoneNumber: ${local.name}")
      val domainUser = local.toDomain().copy(isOnboardingCompleted = true)
      dao.deleteUserProfile()
      dao.deleteAllMessages()
      dao.deleteAllSwipeRecords()
      dao.deleteAllProfiles()
      dao.saveUserProfile(domainUser.toEntity())
      syncCommunityRegisteredUsers(domainUser.id)
      return domainUser
    }

    if (firestoreManager.isAvailable) {
      val cloudUser = firestoreManager.fetchUserByPhone(phoneNumber, countryCode)
      if (cloudUser != null && cloudUser.isOnboardingCompleted && cloudUser.name.isNotBlank()) {
        Log.d("KatkatRepository", "Existing user found in Firestore for phone $phoneNumber: ${cloudUser.name}")
        val completedCloudUser = cloudUser.copy(isOnboardingCompleted = true)
        dao.deleteUserProfile()
        dao.deleteAllMessages()
        dao.deleteAllSwipeRecords()
        dao.deleteAllProfiles()
        dao.saveUserProfile(completedCloudUser.toEntity())
        syncCommunityRegisteredUsers(completedCloudUser.id)
        return completedCloudUser
      }
    }
    return null
  }

  /**
   * Checks if a previously deleted account exists in deleted_accounts for the given phone number.
   */
  suspend fun checkDeletedAccountByPhone(phoneNumber: String, countryCode: String): UserProfile? {
    if (!firestoreManager.isAvailable) return null
    return firestoreManager.fetchDeletedAccountByPhone(phoneNumber, countryCode)
  }

  /**
   * Restores a previously deleted account into active collections and local database.
   */
  suspend fun restoreDeletedAccount(profile: UserProfile): Boolean {
    val completed = profile.copy(isOnboardingCompleted = true, isAccountDisabled = false)
    if (firestoreManager.isAvailable) {
      firestoreManager.restoreDeletedAccount(completed)
    }
    dao.deleteUserProfile()
    dao.deleteAllMessages()
    dao.deleteAllSwipeRecords()
    dao.deleteAllProfiles()
    dao.saveUserProfile(completed.toEntity())
    syncCommunityRegisteredUsers(completed.id)
    return true
  }

  /**
   * Permanently deletes a previously deleted account from deleted_accounts collection.
   */
  suspend fun permanentlyDeleteDeletedAccount(userId: String, phoneNumber: String, countryCode: String): Boolean {
    if (!firestoreManager.isAvailable) return true
    return firestoreManager.permanentlyDeleteFromDeletedAccounts(userId, phoneNumber, countryCode)
  }

  /**
   * Syncs all other registered users from Firestore into the local discovery pool.
   * Every registered user becomes a dating profile for other users.
   */
  suspend fun syncCommunityRegisteredUsers(currentUserId: String) {
    if (!firestoreManager.isAvailable) return
    try {
      if (currentUserId.isNotBlank()) {
        dao.deleteProfileById(currentUserId)
      }
      val currentUser = dao.getUserProfileFlow().firstOrNull()?.toDomain()
      val community = firestoreManager.fetchAllCommunityProfiles(
        excludeUserId = currentUserId,
        userLatitude = currentUser?.latitude ?: 0.0,
        userLongitude = currentUser?.longitude ?: 0.0,
        maxDistanceKm = currentUser?.maxDistanceKm ?: 0
      )
      val outgoingLikedIds = if (currentUserId.isNotBlank()) firestoreManager.fetchOutgoingLikedUserIds(currentUserId) else emptySet()
      val mutualMatchedIds = if (currentUserId.isNotBlank()) firestoreManager.fetchMutualMatchedUserIds(currentUserId) else emptySet()
      val outgoingPassedIds = if (currentUserId.isNotBlank()) firestoreManager.fetchOutgoingPassedUserIds(currentUserId) else emptySet()
      val blockedOrBlockingIds = if (currentUserId.isNotBlank()) firestoreManager.fetchAllBlockedOrBlockingUserIds(currentUserId) else emptySet()
      val deletedAccountIds = if (firestoreManager.isAvailable) firestoreManager.fetchAllDeletedAccountUserIds() else emptySet()

      // Purge any local profiles or messages for blocked users (two-way) or deleted accounts
      val invalidUserIds = blockedOrBlockingIds + deletedAccountIds
      invalidUserIds.forEach { bId ->
        dao.deleteProfileById(bId)
        dao.deleteMessagesForMatch(bId)
      }

      val unblockedCommunity = community.filter { !invalidUserIds.contains(it.id) }

      if (unblockedCommunity.isNotEmpty()) {
        val entities = unblockedCommunity.map { profile ->
          val existing = dao.getProfileById(profile.id)
          val isAlreadyLiked = existing?.isLikedByMe == true || existing?.isSuperLikedByMe == true || outgoingLikedIds.contains(profile.id) || mutualMatchedIds.contains(profile.id)
          val isAlreadyMutual = existing?.isMutualMatch == true || mutualMatchedIds.contains(profile.id)
          val isAlreadyPassed = existing?.isPassedByMe == true || outgoingPassedIds.contains(profile.id)

          ProfileEntity(
            id = profile.id,
            name = profile.name,
            age = profile.age,
            gender = profile.gender,
            occupation = profile.occupation,
            company = profile.company,
            education = profile.education,
            location = profile.location,
            latitude = profile.latitude,
            longitude = profile.longitude,
            bio = profile.bio,
            photosJoined = profile.photos.joinToString("|||"),
            promptQuestion = profile.promptQuestion,
            promptAnswer = profile.promptAnswer,
            passionsJoined = profile.passions.joinToString("|||"),
            zodiac = profile.zodiac,
            height = profile.height,
            datingIntention = profile.datingIntention,
            drinking = profile.drinking,
            smoking = profile.smoking,
            pets = profile.pets,
            anthemSong = profile.anthemSong,
            anthemArtist = profile.anthemArtist,
            isVerified = profile.isVerified,
            likedMe = existing?.likedMe ?: false,
            isLikedByMe = isAlreadyLiked,
            isPassedByMe = isAlreadyPassed,
            isSuperLikedByMe = existing?.isSuperLikedByMe ?: false,
            isMutualMatch = isAlreadyMutual,
            matchedTimestamp = existing?.matchedTimestamp,
            isAccountDisabled = profile.isAccountDisabled
          )
        }
        dao.insertProfiles(entities)
        Log.d("KatkatRepository", "Synced ${entities.size} community registered profiles preserving liked, passed, and match states.")
      }
    } catch (e: Exception) {
      Log.w("KatkatRepository", "Notice syncing community users: ${e.message}")
    }
  }

  /**
   * Refreshes all incoming likes directly from Firestore into the local database.
   * Invoked when user pulls to sync on the Likes page.
   */
  suspend fun refreshLikesFromDatabase(currentUserId: String) {
    if (!firestoreManager.isAvailable || currentUserId.isBlank()) return
    try {
      syncCommunityRegisteredUsers(currentUserId)
      val incoming = firestoreManager.fetchIncomingLikesNow(currentUserId)
      for (like in incoming) {
        val senderId = like.fromUserId
        if (senderId.isBlank() || senderId == currentUserId) continue
        val profileEntity = dao.getProfileById(senderId)
        if (profileEntity != null) {
          dao.markIncomingLike(senderId)
        }
      }
      Log.d("KatkatRepository", "Refreshed ${incoming.size} incoming likes from Firestore.")
    } catch (e: Exception) {
      Log.w("KatkatRepository", "Error refreshing likes from database: ${e.message}")
    }
  }

  /**
   * Refreshes all mutual matches and conversation messages directly from Firestore.
   * Invoked when user pulls to sync on the Chats / Matches page.
   */
  suspend fun refreshMatchesAndChatsFromDatabase(currentUserId: String) {
    if (!firestoreManager.isAvailable || currentUserId.isBlank()) return
    try {
      syncCommunityRegisteredUsers(currentUserId)
      val cloudMatches = firestoreManager.fetchMutualMatchesNow(currentUserId)
      for (cm in cloudMatches) {
        val otherUserId = if (cm.user1Id == currentUserId) cm.user2Id else cm.user1Id
        if (otherUserId.isBlank() || otherUserId == currentUserId) continue
        val profileEntity = dao.getProfileById(otherUserId)
        if (profileEntity != null && !profileEntity.isMutualMatch) {
          dao.markMutualMatch(otherUserId, cm.matchedTimestamp)
        }
      }
      val currentMatches = dao.getMutualMatchesList()
      for (m in currentMatches) {
        val messages = firestoreManager.fetchChatMessages(m.id, currentUserId)
        messages.forEach { msg ->
          dao.insertMessage(msg.toEntity(ownerUserId = currentUserId))
        }
      }
      Log.d("KatkatRepository", "Refreshed matches and chats from Firestore.")
    } catch (e: Exception) {
      Log.w("KatkatRepository", "Error refreshing matches/chats from database: ${e.message}")
    }
  }

  private fun calculateHaversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
  }

  // Account creation / lookup based on mobile number
  suspend fun getOrInitUserByPhone(phoneNumber: String, countryCode: String): UserProfile {
    val cleanPhone = phoneNumber.filter { it.isDigit() }
    val existingLocal = dao.getUserByPhone(phoneNumber) ?: dao.getUserByPhone(cleanPhone)
    if (existingLocal != null) {
      return existingLocal.toDomain()
    }

    if (firestoreManager.isAvailable) {
      val existingCloud = firestoreManager.fetchUserByPhone(phoneNumber, countryCode)
      if (existingCloud != null) {
        dao.saveUserProfile(existingCloud.toEntity())
        return existingCloud
      }
    }

    // Initialize new account based on mobile number
    val authUid = phoneAuthManager.currentUserId
    val newId = if (!authUid.isNullOrBlank()) {
      authUid
    } else {
      "user_${cleanPhone.ifBlank { System.currentTimeMillis().toString() }}"
    }
    val newProfile = UserProfile(
      id = newId,
      phoneNumber = phoneNumber,
      countryCode = countryCode,
      isPhoneVerified = true,
      isOnboardingCompleted = false
    )
    dao.saveUserProfile(newProfile.toEntity())
    if (firestoreManager.isAvailable) {
      appScope.launch {
        firestoreManager.syncUserProfile(newProfile)
      }
    }
    return newProfile
  }

  suspend fun disableAccount(disabled: Boolean) {
    val current = dao.getUserProfileFlow().firstOrNull()?.toDomain()
    if (current != null) {
      val updated = current.copy(isAccountDisabled = disabled)
      dao.saveUserProfile(updated.toEntity())
      if (firestoreManager.isAvailable) {
        try {
          firestoreManager.syncUserProfile(updated)
          if (disabled) {
            firestoreManager.unpublishUserFromDiscovery(updated.id)
          } else if (updated.isOnboardingCompleted && updated.name.isNotBlank()) {
            firestoreManager.publishUserToDiscovery(updated)
            syncCommunityRegisteredUsers(updated.id)
          }
        } catch (e: Exception) {
          Log.w("KatkatRepository", "Notice updating pause status in Firestore: ${e.message}")
        }
      }
    }
    dao.setAccountDisabled(disabled)
  }

  suspend fun deleteAccount(): Boolean {
    val current = dao.getUserProfileFlow().firstOrNull()?.toDomain()
    val effectiveUserId = getEffectiveCurrentUserId()
    if (firestoreManager.isAvailable) {
      try {
        firestoreManager.deleteUserProfile(effectiveUserId, current)
      } catch (e: Exception) {
        Log.w("KatkatRepository", "Notice deleting user in Firestore: ${e.message}")
      }
    }
    phoneAuthManager.signOut()
    dao.deleteUserProfile()
    dao.deleteAllMessages()
    dao.deleteAllSwipeRecords()
    dao.deleteAllProfiles()
    // Re-create initial fresh un-onboarded entry
    val freshInitial = UserProfileEntity(
      id = "my_profile",
      name = "",
      age = 0,
      gender = "",
      pronouns = "",
      bio = "",
      occupation = "",
      education = "",
      hometown = "",
      height = "",
      zodiac = "",
      datingIntention = "",
      drinking = "",
      smoking = "",
      pets = "",
      passionsJoined = "",
      photosJoined = "",
      promptQuestion = "My simple pleasures in life...",
      promptAnswer = "",
      isOnboardingCompleted = false,
      phoneNumber = "",
      countryCode = "+91",
      email = "",
      dob = "",
      currentLocationCity = "",
      currentLocationCountry = "",
      latitude = 0.0,
      longitude = 0.0,
      isPhoneVerified = false,
      isAccountDisabled = false
    )
    dao.saveUserProfile(freshInitial)
    return true
  }

  suspend fun syncDeck(currentUserId: String? = null) {
    // Sync newly discovered community profiles while keeping all passed and liked profiles filtered
    val uid = currentUserId ?: getEffectiveCurrentUserId()
    if (firestoreManager.isAvailable && uid.isNotBlank()) {
      syncCommunityRegisteredUsers(uid)
    }
  }

  suspend fun getEffectiveCurrentUserId(): String {
    val authUid = phoneAuthManager.currentUserId
    if (!authUid.isNullOrBlank()) return authUid
    val localUser = dao.getUserProfileFlow().firstOrNull()
    if (localUser != null && localUser.id.isNotBlank() && localUser.id != "my_profile") {
      return localUser.id
    }
    val cleanPhone = localUser?.phoneNumber?.filter { it.isDigit() }.orEmpty()
    if (cleanPhone.isNotBlank()) return "user_$cleanPhone"
    return localUser?.id ?: "my_profile"
  }

  // --- Notification Operations ---
  fun getNotificationsFlow(userId: String): Flow<List<KatkatNotification>> {
    return dao.getNotificationsFlow(userId).map { list ->
      list.map { it.toDomain() }
    }
  }

  fun getUnreadNotificationsCountFlow(userId: String): Flow<Int> {
    return dao.getUnreadNotificationsCountFlow(userId)
  }

  suspend fun markNotificationAsRead(id: String) {
    dao.markNotificationAsRead(id)
    val myUserId = getEffectiveCurrentUserId()
    if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      appScope.launch {
        firestoreManager.markNotificationReadInCloud(myUserId, id)
      }
    }
  }

  suspend fun markAllNotificationsAsRead(userId: String) {
    dao.markAllNotificationsAsRead(userId)
    if (firestoreManager.isAvailable && userId.isNotBlank()) {
      appScope.launch {
        firestoreManager.markAllNotificationsReadInCloud(userId)
      }
    }
  }

  suspend fun deleteNotification(id: String) {
    dao.deleteNotification(id)
    val myUserId = getEffectiveCurrentUserId()
    if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      appScope.launch {
        firestoreManager.deleteNotificationInCloud(myUserId, id)
      }
    }
  }

  suspend fun clearAllNotifications(userId: String) {
    dao.deleteAllNotifications(userId)
    if (firestoreManager.isAvailable && userId.isNotBlank()) {
      appScope.launch {
        firestoreManager.clearAllNotificationsInCloud(userId)
      }
    }
  }

  suspend fun postLocalNotification(notification: KatkatNotification) {
    saveAndPushNotification(notification)
  }

  suspend fun postSystemNotification(title: String, message: String, userId: String) {
    val notif = KatkatNotification(
      id = "sys_${System.currentTimeMillis()}",
      userId = userId,
      type = KatkatNotificationType.SYSTEM_NOTIFICATION,
      title = title,
      message = message,
      timestamp = System.currentTimeMillis()
    )
    saveAndPushNotification(notif)
    if (firestoreManager.isAvailable && userId.isNotBlank()) {
      appScope.launch {
        firestoreManager.sendNotification(userId, notif)
      }
    }
  }

  suspend fun saveAndPushNotificationPublic(notif: KatkatNotification) {
    saveAndPushNotification(notif)
  }

  companion object {
    const val DEFAULT_PAGE_SIZE = 20

    @Volatile
    private var INSTANCE: KatkatRepository? = null

    private val appCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun getInstance(context: Context): KatkatRepository {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: KatkatRepository(
          dao = com.example.data.local.KatkatDatabase.getDatabase(context.applicationContext).datingDao(),
          appScope = appCoroutineScope
        ).also { INSTANCE = it }
      }
    }
  }
}

enum class SwipeAction { LIKE, PASS, SUPERLIKE }

sealed class SwipeResult {
  data class Success(val action: SwipeAction) : SwipeResult()
  data class MutualMatch(val profile: DatingProfile) : SwipeResult()
  data class LimitReached(val tier: SubscriptionTier, val currentCount: Int) : SwipeResult()
  data class Error(val message: String) : SwipeResult()
}

fun ChatMessageEntity.toDomain() = ChatMessage(
  id = id,
  matchId = matchId,
  senderId = senderId,
  senderName = senderName,
  text = text,
  photoUri = photoUri,
  timestamp = timestamp,
  isFromMe = isFromMe,
  isRead = isRead,
  conversationId = conversationId,
  currentUserId = currentUserId,
  recipientId = recipientId,
  isSending = isSending,
  isFailed = isFailed
)

fun ChatMessage.toEntity(ownerUserId: String = "") = ChatMessageEntity(
  id = id,
  matchId = matchId,
  senderId = senderId,
  senderName = senderName,
  text = text,
  photoUri = photoUri,
  timestamp = timestamp,
  isFromMe = isFromMe,
  isRead = isRead,
  conversationId = conversationId,
  currentUserId = if (ownerUserId.isNotBlank()) ownerUserId else currentUserId,
  recipientId = recipientId,
  isSending = isSending,
  isFailed = isFailed
)
