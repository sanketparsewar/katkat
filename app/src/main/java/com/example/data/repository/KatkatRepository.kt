package com.example.data.repository

import com.example.data.local.ChatMessageEntity
import com.example.data.local.Converters
import com.example.data.local.DatingDao
import com.example.data.local.ProfileEntity
import com.example.data.local.SubscriptionEntity
import com.example.data.local.SwipeRecordEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.ChatMessage
import com.example.data.model.DatingProfile
import com.example.data.model.MatchConversation
import com.example.data.model.SubscriptionState
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserProfile
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
  private val appScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

  init {
    appScope.launch {
      // Seed database if empty
      if (dao.getProfilesCount() == 0) {
        dao.insertProfiles(SeedData.getInitialProfiles())
        dao.saveUserProfile(SeedData.getInitialUserProfile())
        dao.saveSubscription(
          SubscriptionEntity(
            id = "current_sub",
            tierName = SubscriptionTier.FREE.name,
            swipesUsedThisMonth = 3,
            currentMonthKey = "2026-09",
            isAnnualBilling = false,
            subscriptionExpiryDate = "Renews Oct 16, 2026"
          )
        )
      }
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
    entity?.toDomain() ?: SeedData.getInitialUserProfile().toDomain()
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
  fun getMessages(matchId: String): Flow<List<ChatMessage>> = dao.getMessagesForMatch(matchId).map { list ->
    list.map { it.toDomain() }
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
    }
  }

  suspend fun markMessagesRead(matchId: String) {
    dao.markMessagesAsRead(matchId)
  }

  // Profile update
  suspend fun saveUserProfile(profile: UserProfile) {
    dao.saveUserProfile(profile.toEntity())
  }

  suspend fun resetDeckForTesting() {
    val initial = SeedData.getInitialProfiles()
    dao.insertProfiles(initial)
  }

  private fun getGreetingForProfile(name: String): String {
    return when (name) {
      "Maya Lin" -> "Hey Alex! Loved your taste in photography and vinyl. Have you visited the modern art museum's rooftop terrace yet? 🎨"
      "Lucas Thorne" -> "Hey! Saw you're into indie records too 🎧 What's the best concert you've been to recently?"
      "Julian Vance" -> "Hey there! Ready for a mountain hike or are you more of a campfire & coffee person? 🌲☕"
      "Sienna Rivera" -> "Omg hi!! Fellow cat lover spotted 🐾 Are you playing any cozy games right now?"
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

// Extensions for Domain/Entity conversions
fun ProfileEntity.toDomain() = DatingProfile(
  id = id,
  name = name,
  age = age,
  occupation = occupation,
  company = company,
  education = education,
  location = location,
  bio = bio,
  photos = Converters.stringToList(photosJoined),
  promptQuestion = promptQuestion,
  promptAnswer = promptAnswer,
  passions = Converters.stringToList(passionsJoined),
  zodiac = zodiac,
  height = height,
  datingIntention = datingIntention,
  drinking = drinking,
  smoking = smoking,
  pets = pets,
  anthemSong = anthemSong,
  anthemArtist = anthemArtist,
  isVerified = isVerified,
  likedMe = likedMe,
  isLikedByMe = isLikedByMe,
  isPassedByMe = isPassedByMe,
  isSuperLikedByMe = isSuperLikedByMe,
  isMutualMatch = isMutualMatch,
  matchedTimestamp = matchedTimestamp
)

fun UserProfileEntity.toDomain() = UserProfile(
  id = id,
  name = name,
  age = age,
  gender = gender,
  pronouns = pronouns,
  bio = bio,
  occupation = occupation,
  education = education,
  hometown = hometown,
  height = height,
  zodiac = zodiac,
  datingIntention = datingIntention,
  drinking = drinking,
  smoking = smoking,
  pets = pets,
  passions = Converters.stringToList(passionsJoined),
  photos = Converters.stringToList(photosJoined),
  promptQuestion = promptQuestion,
  promptAnswer = promptAnswer
)

fun UserProfile.toEntity() = UserProfileEntity(
  id = id,
  name = name,
  age = age,
  gender = gender,
  pronouns = pronouns,
  bio = bio,
  occupation = occupation,
  education = education,
  hometown = hometown,
  height = height,
  zodiac = zodiac,
  datingIntention = datingIntention,
  drinking = drinking,
  smoking = smoking,
  pets = pets,
  passionsJoined = Converters.listToString(passions),
  photosJoined = Converters.listToString(photos),
  promptQuestion = promptQuestion,
  promptAnswer = promptAnswer
)

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
