package com.example.data.repository

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
import com.example.data.model.MatchConversation
import com.example.data.model.SubscriptionState
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserProfile
import android.util.Log
import com.example.data.remote.FirebaseStorageManager
import com.example.data.remote.FirestoreManager
import com.example.data.remote.PhoneAuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import java.util.UUID

class KatkatRepository(
  private val dao: DatingDao,
  private val appScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
  val firestoreManager: FirestoreManager = FirestoreManager(),
  val phoneAuthManager: PhoneAuthManager = PhoneAuthManager(),
  val firebaseStorageManager: FirebaseStorageManager = FirebaseStorageManager()
) {

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

        // Seed default discover profiles if database is fresh
        val totalCount = dao.getProfilesCount()
        if (totalCount == 0) {
          seedDefaultProfiles()
        }

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
              dao.markIncomingLike(senderId)

              // If I already liked this profile and we weren't marked mutual yet -> MATCH!
              if (wasAlreadyLikedByMe && !wasAlreadyMutual) {
                val matchTime = System.currentTimeMillis()
                dao.markMutualMatch(senderId, matchTime)

                // Register mutual match in Firestore so both users share the match record
                firestoreManager.registerMutualMatch(currentUserId, senderId)

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

              val updatedProfile = dao.getProfileById(otherUserId)?.toDomain()
              if (updatedProfile != null) {
                _realtimeMatchEvent.emit(updatedProfile)
              }
            }
          }
        }
      }

      // 3. Observe background chat messages for all mutual matches to update unread counts in real time
      launch {
        dao.getMutualMatches().collect { mutualList ->
          mutualList.forEach { matchProfile ->
            launch {
              firestoreManager.observeChatMessages(matchProfile.id, currentUserId).collect { remoteMsgs ->
                remoteMsgs.forEach { msg ->
                  dao.insertMessage(msg.toEntity())
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
    }
  }

  suspend fun seedDefaultProfiles() {
    val seeds = listOf(
      ProfileEntity(
        id = "profile_maya_1",
        name = "Maya Lin",
        age = 24,
        gender = "Women",
        occupation = "UI/UX Designer",
        company = "Studio Origami",
        education = "Rhode Island School of Design",
        location = "Indiranagar, Bengaluru (4 km away)",
        latitude = 12.9716,
        longitude = 77.5946,
        bio = "Analog photography addict & matcha latte connoisseur 🍵 Always down for an impromptu art gallery stroll or thrift shopping!",
        photosJoined = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1517841905240-472988babdf9?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "My simple pleasures in life...",
        promptAnswer = "Sunday morning coffee, finding a rare vinyl record, and rainy city walks 🌧️",
        passionsJoined = "Photography|||Design|||Art Galleries|||Coffee|||Vinyl Records",
        zodiac = "Libra",
        height = "5'6\" (168 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Cat person",
        anthemSong = "Good Days",
        anthemArtist = "SZA",
        isVerified = true,
        likedMe = true,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_lucas_2",
        name = "Lucas Thorne",
        age = 27,
        gender = "Men",
        occupation = "Sound Designer & Musician",
        company = "Waveform Studios",
        education = "Berklee College of Music",
        location = "Koramangala, Bengaluru (6 km away)",
        latitude = 12.9352,
        longitude = 77.6245,
        bio = "Producing indie tracks by day, testing ramen recipes by night 🍜 Let's exchange Spotify playlists or hit an indie concert.",
        photosJoined = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "Together, we could...",
        promptAnswer = "Build the ultimate synthwave playlist and drive with the windows down at midnight 🌌",
        passionsJoined = "Music Production|||Live Gigs|||Ramen|||Travel|||Guitar",
        zodiac = "Scorpio",
        height = "6'0\" (183 cm)",
        datingIntention = "Long-term relationship",
        drinking = "On special occasions",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Midnight City",
        anthemArtist = "M83",
        isVerified = true,
        likedMe = true,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_chloe_3",
        name = "Chloe Dubois",
        age = 25,
        gender = "Women",
        occupation = "Artisan Baker & Pastry Chef",
        company = "Le Petit Croissant",
        education = "Le Cordon Bleu",
        location = "Lavelle Road, Bengaluru (2 km away)",
        latitude = 12.9719,
        longitude = 77.5997,
        bio = "Sourdough whisperer 🥐 You will always have warm fresh pastries on weekends. Looking for someone who appreciates good food and great humor.",
        photosJoined = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "The hallmark of a great date is...",
        promptAnswer = "Losing track of time because the conversation was that effortless and fun ✨",
        passionsJoined = "Baking|||Cooking|||Wine Tasting|||Book Clubs|||Film",
        zodiac = "Taurus",
        height = "5'5\" (165 cm)",
        datingIntention = "Looking for love",
        drinking = "Socially",
        smoking = "Never",
        pets = "Have pets",
        anthemSong = "La Vie En Rose",
        anthemArtist = "Emily Watts",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_aarav_4",
        name = "Aarav Sharma",
        age = 28,
        gender = "Men",
        occupation = "Tech Lead & Angel Investor",
        company = "HyperScale Labs",
        education = "IIT Delhi",
        location = "HSR Layout, Bengaluru (8 km away)",
        latitude = 12.9121,
        longitude = 77.6446,
        bio = "Building smart tech & climbing boulders on weekends 🧗 Loves espresso, deep philosophical conversations, and spontaneous weekend road trips.",
        photosJoined = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "I geek out on...",
        promptAnswer = "Space exploration tech, vintage mechanical watches, and specialty pour-over coffee ☕",
        passionsJoined = "Rock Climbing|||Startups|||Coffee|||Hiking|||Reading",
        zodiac = "Capricorn",
        height = "5'11\" (180 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Love all pets",
        anthemSong = "Stargazing",
        anthemArtist = "Kygo",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_priya_5",
        name = "Priya Patel",
        age = 26,
        gender = "Women",
        occupation = "Architect & Ceramicist",
        company = "Terra Studio",
        education = "National Institute of Design",
        location = "Sadashivanagar, Bengaluru (5 km away)",
        latitude = 13.0068,
        longitude = 77.5813,
        bio = "Designing sustainable homes & throwing pottery clay on weekends 🏺 Let's find the best sunset viewpoint in the city.",
        photosJoined = "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "My love language is...",
        promptAnswer = "Quality time, homemade artisanal pasta, and making each other laugh till our cheeks hurt 😊",
        passionsJoined = "Ceramics|||Architecture|||Yoga|||Plants|||Travel",
        zodiac = "Virgo",
        height = "5'7\" (170 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Cat person",
        anthemSong = "Sunflower",
        anthemArtist = "Post Malone",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_rohan_6",
        name = "Rohan Mehta",
        age = 29,
        gender = "Men",
        occupation = "Documentary Filmmaker",
        company = "Nomad Media",
        education = "FTII Pune",
        location = "Whitefield, Bengaluru (12 km away)",
        latitude = 12.9698,
        longitude = 77.7500,
        bio = "Telling stories around the world 🎬 Coffee aficionado, vinyl collector, and avid cyclist.",
        photosJoined = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "Best travel memory...",
        promptAnswer = "Watching the Northern Lights from a cozy wooden cabin in Norway ❄️",
        passionsJoined = "Filmmaking|||Cycling|||Coffee|||Cinema|||Photography",
        zodiac = "Leo",
        height = "6'1\" (185 cm)",
        datingIntention = "Open to possibilities",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Dreams",
        anthemArtist = "Fleetwood Mac",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      )
    )
    dao.insertProfiles(seeds)
  }

  // User Profile
  val userProfile: Flow<UserProfile> = dao.getUserProfileFlow().map { entity ->
    entity?.toDomain() ?: UserProfile(isOnboardingCompleted = false)
  }

  // Active Discover Deck (strictly excluding current user's profile and filtered by Discovery Preferences)
  val activeProfiles: Flow<List<DatingProfile>> = userProfile.flatMapLatest { currentUser ->
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

        // 4. Filter by Age Preference
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

        // 6. Filter by Maximum Distance Preference
        if (currentUser.latitude != 0.0 && currentUser.longitude != 0.0 &&
            entity.latitude != 0.0 && entity.longitude != 0.0) {
          val distKm = calculateHaversineDistanceKm(
            currentUser.latitude, currentUser.longitude,
            entity.latitude, entity.longitude
          )
          if (distKm > maxDistance) {
            return@mapNotNull null
          }
        } else if (entity.location.isNotBlank()) {
          // Try to parse mock text like "X miles away" or "X km away"
          val matchMiles = Regex("""(\d+)\s*miles\s*away""", RegexOption.IGNORE_CASE).find(entity.location)
          if (matchMiles != null) {
            val miles = matchMiles.groupValues[1].toDoubleOrNull() ?: 0.0
            val km = miles * 1.60934
            if (km > maxDistance) return@mapNotNull null
          }
          val matchKm = Regex("""(\d+)\s*km\s*away""", RegexOption.IGNORE_CASE).find(entity.location)
          if (matchKm != null) {
            val km = matchKm.groupValues[1].toDoubleOrNull() ?: 0.0
            if (km > maxDistance) return@mapNotNull null
          }
        }

        entity.toDomain()
      }
    }
  }

  // Mutual Matches
  val mutualMatches: Flow<List<DatingProfile>> = dao.getMutualMatches().map { entities ->
    entities.map { it.toDomain() }
  }

  // Profiles Who Liked Current User (for "Likes You" tab)
  val profilesWhoLikedMe: Flow<List<DatingProfile>> = dao.getProfilesWhoLikedMe().map { entities ->
    entities.map { it.toDomain() }
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
    SubscriptionState(
      currentTier = tier,
      swipesUsedThisMonth = subEntity?.swipesUsedThisMonth ?: recordedSwipes,
      currentMonthKey = subEntity?.currentMonthKey ?: "2026-09",
      isAnnualBilling = subEntity?.isAnnualBilling ?: false,
      subscriptionExpiryDate = subEntity?.subscriptionExpiryDate ?: "Renews Oct 16, 2026",
      isRevenueCatConnected = true
    )
  }

  suspend fun processSwipe(
    profileId: String,
    action: SwipeAction
  ): SwipeResult {
    val sub = dao.getSubscriptionFlow().firstOrNull()
    val tier = try {
      SubscriptionTier.valueOf(sub?.tierName ?: SubscriptionTier.FREE.name)
    } catch (_: Exception) {
      SubscriptionTier.FREE
    }
    val currentSwipes = sub?.swipesUsedThisMonth ?: 0

    if (currentSwipes >= tier.monthlySwipes) {
      return SwipeResult.LimitReached(tier, currentSwipes)
    }

    val profile = dao.getProfileById(profileId) ?: return SwipeResult.Error("Profile not found")
    val newSwipeCount = currentSwipes + 1

    // Record swipe
    dao.insertSwipeRecord(
      SwipeRecordEntity(
        profileId = profileId,
        actionType = action.name,
        monthKey = "2026-09"
      )
    )

    // Update subscription record
    dao.saveSubscription(
      SubscriptionEntity(
        id = "current_sub",
        tierName = tier.name,
        swipesUsedThisMonth = newSwipeCount,
        currentMonthKey = "2026-09",
        isAnnualBilling = sub?.isAnnualBilling ?: false,
        subscriptionExpiryDate = sub?.subscriptionExpiryDate ?: "Renews Oct 16, 2026"
      )
    )

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
    if ((action == SwipeAction.LIKE || action == SwipeAction.SUPERLIKE) && firestoreManager.isAvailable) {
      val myUserId = getEffectiveCurrentUserId()
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

              val updatedProfile = dao.getProfileById(profileId)?.toDomain()
              if (updatedProfile != null) {
                _realtimeMatchEvent.emit(updatedProfile)
              }
            }
          } else {
            // It was mutual locally, register in cloud
            firestoreManager.registerMutualMatch(myUserId, profileId)
          }
        }
      }
    }

    if (isMutual) {
      return SwipeResult.MutualMatch(profile.toDomain())
    }

    return SwipeResult.Success(action)
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
    return true
  }

  suspend fun resetSwipeCounter(): Boolean {
    val sub = dao.getSubscriptionFlow().firstOrNull()
    if (sub != null) {
      dao.saveSubscription(sub.copy(swipesUsedThisMonth = 0))
    }
    return true
  }

  // Chat Messages
  fun getMessages(matchId: String): Flow<List<ChatMessage>> {
    if (firestoreManager.isAvailable) {
      appScope.launch {
        val myUserId = getEffectiveCurrentUserId()
        firestoreManager.observeChatMessages(matchId, myUserId).collect { remoteMessages ->
          remoteMessages.forEach { msg ->
            dao.insertMessage(msg.toEntity())
          }
        }
      }
    }
    return dao.getMessagesForMatch(matchId).map { list ->
      list.map { it.toDomain() }
    }
  }

  suspend fun sendMessage(matchId: String, text: String, photoUri: String? = null) {
    val myUserId = getEffectiveCurrentUserId()
    val localUser = dao.getUserProfileFlow().firstOrNull()
    val myName = localUser?.name?.takeIf { it.isNotBlank() } ?: "Alex"

    val myMessage = ChatMessageEntity(
      id = UUID.randomUUID().toString(),
      matchId = matchId,
      senderId = myUserId,
      senderName = myName,
      text = text,
      photoUri = photoUri,
      timestamp = System.currentTimeMillis(),
      isFromMe = true,
      isRead = true
    )
    dao.insertMessage(myMessage)

    // Sync sent message to Cloud Firestore in real time
    if (firestoreManager.isAvailable) {
      appScope.launch {
        firestoreManager.sendChatMessage(matchId, myMessage.toDomain(), myUserId)
      }
    }
  }

  suspend fun markMessagesRead(matchId: String) {
    dao.markMessagesAsRead(matchId)
    if (firestoreManager.isAvailable) {
      val myUserId = getEffectiveCurrentUserId()
      if (myUserId.isNotBlank()) {
        appScope.launch {
          firestoreManager.markChatMessagesAsRead(matchId, myUserId)
        }
      }
    }
  }

  // Reactive conversations stream combining mutual matches with their message threads
  val conversations: Flow<List<MatchConversation>> = combine(
    mutualMatches,
    dao.getAllMessagesFlow()
  ) { matches, allMessages ->
    val messagesByMatch = allMessages.groupBy { it.matchId }
    matches.map { profile ->
      val matchMsgs = messagesByMatch[profile.id].orEmpty().sortedBy { it.timestamp }
      val lastMsg = matchMsgs.lastOrNull()
      val unreadCount = matchMsgs.count { !it.isFromMe && !it.isRead }
      val lastText = when {
        lastMsg?.photoUri != null && !lastMsg.text.isNullOrBlank() -> "📷 ${lastMsg.text}"
        lastMsg?.photoUri != null -> "📷 Sent a photo"
        lastMsg != null -> lastMsg.text
        else -> "New match! Say hello 👋"
      }
      val lastTime = lastMsg?.timestamp ?: (profile.matchedTimestamp ?: (System.currentTimeMillis() - 3600_000L))

      MatchConversation(
        matchProfile = profile,
        matchTimeMillis = profile.matchedTimestamp ?: lastTime,
        lastMessage = lastText,
        lastMessageTimeMillis = lastTime,
        unreadCount = unreadCount,
        isOnline = true
      )
    }.sortedByDescending { it.lastMessageTimeMillis }
  }

  suspend fun deleteMessagesForMatch(matchId: String) {
    dao.deleteMessagesForMatch(matchId)
    if (firestoreManager.isAvailable) {
      val myUserId = getEffectiveCurrentUserId()
      firestoreManager.deleteChatMessages(matchId, myUserId)
    }
  }

  suspend fun unmatch(matchId: String) {
    dao.unmatchProfile(matchId)
    deleteMessagesForMatch(matchId)
  }

  suspend fun blockProfile(matchId: String) {
    dao.deleteProfileById(matchId)
    deleteMessagesForMatch(matchId)
    val myUserId = getEffectiveCurrentUserId()
    if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      appScope.launch {
        firestoreManager.blockUser(myUserId, matchId)
      }
    }
  }

  suspend fun createSimulatedTestMatch(): DatingProfile? {
    val nonMatches = dao.getActiveDeckProfiles().firstOrNull()?.filter { !it.isMutualMatch }
    val candidate = nonMatches?.firstOrNull()
    if (candidate != null) {
      val now = System.currentTimeMillis()
      dao.markLiked(candidate.id, isMutual = true, matchedTimestamp = now)
      return dao.getProfileById(candidate.id)?.toDomain()
    }
    return null
  }

  // Profile update with real-time Firestore sync and community discovery publishing
  suspend fun saveUserProfile(profile: UserProfile) {
    val cleanPhone = profile.phoneNumber.filter { it.isDigit() }
    val authUid = phoneAuthManager.currentUserId
    val uniqueId = if (!authUid.isNullOrBlank()) {
      authUid
    } else if (profile.id.isNotBlank() && profile.id != "my_profile") {
      profile.id
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

    // Save active profile cleanly without transient null emissions
    dao.saveUserProfile(fixedProfile.toEntity())
    dao.deleteOtherUserProfiles(uniqueId)
    if (firestoreManager.isAvailable) {
      appScope.launch {
        firestoreManager.syncUserProfile(fixedProfile)
        // If user completed onboarding, publish as a dating profile for other users
        if (fixedProfile.isOnboardingCompleted && fixedProfile.name.isNotBlank()) {
          firestoreManager.publishUserToDiscovery(fixedProfile)
          syncCommunityRegisteredUsers(fixedProfile.id)
        }
      }
    }
  }

  suspend fun logoutActiveSession() {
    phoneAuthManager.signOut()
    // Delete active user_profile, local cached chats, swipe history, and profiles deck
    // so no previous user's chat or match data remains on the device.
    dao.deleteUserProfile()
    dao.deleteAllMessages()
    dao.deleteAllSwipeRecords()
    dao.deleteAllProfiles()
    Log.d("KatkatRepository", "Logged out active session and cleared local user data cleanly.")
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
   * Syncs all other registered users from Firestore into the local discovery pool.
   * Every registered user becomes a dating profile for other users.
   */
  suspend fun syncCommunityRegisteredUsers(currentUserId: String) {
    if (!firestoreManager.isAvailable) return
    try {
      if (currentUserId.isNotBlank()) {
        dao.deleteProfileById(currentUserId)
      }
      val community = firestoreManager.fetchAllCommunityProfiles(currentUserId)
      val outgoingLikedIds = if (currentUserId.isNotBlank()) firestoreManager.fetchOutgoingLikedUserIds(currentUserId) else emptySet()
      val mutualMatchedIds = if (currentUserId.isNotBlank()) firestoreManager.fetchMutualMatchedUserIds(currentUserId) else emptySet()
      val outgoingPassedIds = if (currentUserId.isNotBlank()) firestoreManager.fetchOutgoingPassedUserIds(currentUserId) else emptySet()
      val blockedOrBlockingIds = if (currentUserId.isNotBlank()) firestoreManager.fetchAllBlockedOrBlockingUserIds(currentUserId) else emptySet()

      // Purge any local profiles or messages for blocked users (two-way)
      blockedOrBlockingIds.forEach { bId ->
        dao.deleteProfileById(bId)
        dao.deleteMessagesForMatch(bId)
      }

      val unblockedCommunity = community.filter { !blockedOrBlockingIds.contains(it.id) }

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
            matchedTimestamp = existing?.matchedTimestamp
          )
        }
        dao.insertProfiles(entities)
        Log.d("KatkatRepository", "Synced ${entities.size} community registered profiles preserving liked, passed, and match states.")
      }
    } catch (e: Exception) {
      Log.w("KatkatRepository", "Notice syncing community users: ${e.message}")
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
    dao.setAccountDisabled(disabled)
    val current = dao.getUserProfileFlow().firstOrNull()?.toDomain()
    if (current != null && firestoreManager.isAvailable) {
      appScope.launch {
        firestoreManager.syncUserProfile(current.copy(isAccountDisabled = disabled))
      }
    }
  }

  suspend fun deleteAccount(): Boolean {
    val current = dao.getUserProfileFlow().firstOrNull()?.toDomain()
    dao.deleteUserProfile()
    dao.deleteAllMessages()
    dao.deleteAllSwipeRecords()
    if (current != null && firestoreManager.isAvailable) {
      appScope.launch {
        firestoreManager.deleteUserProfile(current.id)
      }
    }
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

  suspend fun resetDeckForTesting(currentUserId: String? = null) {
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
  isRead = isRead
)

fun ChatMessage.toEntity() = ChatMessageEntity(
  id = id,
  matchId = matchId,
  senderId = senderId,
  senderName = senderName,
  text = text,
  photoUri = photoUri,
  timestamp = timestamp,
  isFromMe = isFromMe,
  isRead = isRead
)
