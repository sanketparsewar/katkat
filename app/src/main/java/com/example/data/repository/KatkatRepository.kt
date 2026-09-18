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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
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

        // Sync community registered users from Firestore if user session exists
        val current = dao.getUserProfileFlow().firstOrNull()
        if (current != null && current.isOnboardingCompleted && firestoreManager.isAvailable) {
          syncCommunityRegisteredUsers(current.id)
        }
      } catch (_: Exception) {}
    }
  }

  // Active Discover Deck
  val activeProfiles: Flow<List<DatingProfile>> = dao.getActiveDeckProfiles().map { entities ->
    entities.map { it.toDomain() }
  }

  // Mutual Matches
  val mutualMatches: Flow<List<DatingProfile>> = dao.getMutualMatches().map { entities ->
    entities.map { it.toDomain() }
  }

  // Profiles Who Liked Current User (for "Likes You" tab)
  val profilesWhoLikedMe: Flow<List<DatingProfile>> = dao.getProfilesWhoLikedMe().map { entities ->
    entities.map { it.toDomain() }
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
      }
      SwipeAction.SUPERLIKE -> {
        isMutual = true
        dao.markSuperLiked(profileId, matchedTimestamp = now)
      }
    }

    if (isMutual) {
      // Seed initial welcome greeting message from the matched profile
      val initialGreeting = getGreetingForProfile(profile.name)
      dao.insertMessage(
        ChatMessageEntity(
          id = UUID.randomUUID().toString(),
          matchId = profileId,
          senderId = profileId,
          senderName = profile.name,
          text = initialGreeting,
          photoUri = null,
          timestamp = now + 1000,
          isFromMe = false,
          isRead = false
        )
      )
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
        firestoreManager.observeChatMessages(matchId).collect { remoteMessages ->
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
    val myMessage = ChatMessageEntity(
      id = UUID.randomUUID().toString(),
      matchId = matchId,
      senderId = "my_profile",
      senderName = "Alex",
      text = text,
      photoUri = photoUri,
      timestamp = System.currentTimeMillis(),
      isFromMe = true,
      isRead = true
    )
    dao.insertMessage(myMessage)

    // Sync sent message to Cloud Firestore
    if (firestoreManager.isAvailable) {
      appScope.launch {
        firestoreManager.sendChatMessage(matchId, myMessage.toDomain())
      }
    }

    // Simulate realistic real-time 2-way reply from match
    appScope.launch {
      delay(1800)
      val reply = generatePlayfulReply(text)
      val profile = dao.getProfileById(matchId)
      val replyMessage = ChatMessageEntity(
        id = UUID.randomUUID().toString(),
        matchId = matchId,
        senderId = matchId,
        senderName = profile?.name ?: "Match",
        text = reply,
        photoUri = null,
        timestamp = System.currentTimeMillis(),
        isFromMe = false,
        isRead = false
      )
      dao.insertMessage(replyMessage)

      // Sync simulated reply to Cloud Firestore
      if (firestoreManager.isAvailable) {
        firestoreManager.sendChatMessage(matchId, replyMessage.toDomain())
      }
    }
  }

  suspend fun markMessagesRead(matchId: String) {
    dao.markMessagesAsRead(matchId)
  }

  // Profile update with real-time Firestore sync and community discovery publishing
  suspend fun saveUserProfile(profile: UserProfile) {
    val cleanPhone = profile.phoneNumber.filter { it.isDigit() }
    val uniqueId = if (profile.id.startsWith("user_") && profile.id.length > 5) {
      profile.id
    } else if (cleanPhone.isNotBlank()) {
      "user_$cleanPhone"
    } else {
      profile.id.ifBlank { "user_${System.currentTimeMillis()}" }
    }
    val fixedProfile = profile.copy(id = uniqueId)

    // Clear stale rows and ensure active profile is cleanly saved
    dao.deleteUserProfile()
    dao.saveUserProfile(fixedProfile.toEntity())
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
    // Delete active user_profile row in SQLite so memory session resets,
    // while keeping registered profile intact in Firestore.
    dao.deleteUserProfile()
    Log.d("KatkatRepository", "Logged out active session cleanly.")
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
      dao.saveUserProfile(domainUser.toEntity())
      return domainUser
    }

    if (firestoreManager.isAvailable) {
      val cloudUser = firestoreManager.fetchUserByPhone(phoneNumber, countryCode)
      if (cloudUser != null && cloudUser.isOnboardingCompleted && cloudUser.name.isNotBlank()) {
        Log.d("KatkatRepository", "Existing user found in Firestore for phone $phoneNumber: ${cloudUser.name}")
        val completedCloudUser = cloudUser.copy(isOnboardingCompleted = true)
        dao.deleteUserProfile()
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
      val community = firestoreManager.fetchAllCommunityProfiles(currentUserId)
      if (community.isNotEmpty()) {
        val entities = community.map { profile ->
          ProfileEntity(
            id = profile.id,
            name = profile.name,
            age = profile.age,
            occupation = profile.occupation,
            company = profile.company,
            education = profile.education,
            location = profile.location,
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
            likedMe = false,
            isLikedByMe = false,
            isPassedByMe = false,
            isSuperLikedByMe = false,
            isMutualMatch = false,
            matchedTimestamp = null
          )
        }
        dao.insertProfiles(entities)
        Log.d("KatkatRepository", "Synced ${entities.size} community registered profiles into Discover deck")
      }
    } catch (e: Exception) {
      Log.w("KatkatRepository", "Notice syncing community users: ${e.message}")
    }
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
    val newProfile = UserProfile(
      id = "user_${cleanPhone.ifBlank { System.currentTimeMillis().toString() }}",
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
    val freshInitial = SeedData.getInitialUserProfile()
    dao.saveUserProfile(freshInitial)
    return true
  }

  suspend fun resetDeckForTesting() {
    dao.deleteAllProfiles()
    val initial = SeedData.getInitialProfiles()
    dao.insertProfiles(initial)
  }

  private fun getGreetingForProfile(name: String): String {
    return when (name) {
      "Maya Lin" -> "Hey Alex! Loved your taste in photography and vinyl. Have you visited the modern art museum's rooftop terrace yet? 🎨"
      "Lucas Thorne" -> "Hey! Saw you're into indie records too 🎧 What's the best concert you've been to recently?"
      "Chloe Dubois" -> "Bonjour Alex! 🥐 Loved your photos! Ever tried baking fresh brioche from scratch on a cozy Sunday morning?"
      else -> "Hey Alex! So happy we matched! How is your week going? ✨"
    }
  }

  private fun generatePlayfulReply(userText: String): String {
    val lower = userText.lowercase()
    return when {
      lower.contains("coffee") || lower.contains("cafe") ->
        "I know this cozy hidden gem with the most incredible lavender latte! We definitely need to go together ☕✨"
      lower.contains("hi") || lower.contains("hey") || lower.contains("hello") ->
        "Hey! I was just smiling looking at your profile. What kind of music is on your heavy rotation today? 🎶"
      lower.contains("photo") || lower.contains("camera") ->
        "Your photography eye is stunning! I'd love to see some of your favorite film shots sometime 📸"
      lower.contains("cat") || lower.contains("pet") ->
        "Aww, cats make everything better! Mine is currently curled up like a little croissant beside me 🥐🐱"
      lower.contains("date") || lower.contains("meet") || lower.contains("weekend") ->
        "I'd love that! Are you free this Friday evening? There's a vintage vinyl bar I've been dying to try 🍹"
      else ->
        "Haha totally agree! That's so refreshing to hear. What's something that made you genuinely laugh today? 😊"
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
