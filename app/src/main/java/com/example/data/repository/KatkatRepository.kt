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
    val masterCatalog = listOf(
      // === PAGE 1 (Profiles 1 - 20) ===
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
        location = "Whitefield, Bengaluru (18 km away)",
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
      ),
      ProfileEntity(
        id = "profile_dev_7",
        name = "Dev Malhotra",
        age = 30,
        gender = "Men",
        occupation = "Venture Partner & Triathlete",
        company = "Peak Velocity Fund",
        education = "Stanford University",
        location = "Devanahalli, Bengaluru (32 km away)",
        latitude = 13.2483,
        longitude = 77.7126,
        bio = "Training for my next triathlon 🏃‍♂️ Tech nerd at heart, lover of good sushi and deep conversations over pour-over coffee.",
        photosJoined = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "A life goal of mine is...",
        promptAnswer = "Run a marathon on every continent and build a sustainable tech foundation 🌍",
        passionsJoined = "Triathlon|||Venture Capital|||Sushi|||Running|||Books",
        zodiac = "Aries",
        height = "6'2\" (188 cm)",
        datingIntention = "Long-term relationship",
        drinking = "On special occasions",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Adventure of a Lifetime",
        anthemArtist = "Coldplay",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_elena_8",
        name = "Elena Roy",
        age = 26,
        gender = "Women",
        occupation = "Wildlife Biologist & Writer",
        company = "Ecosphere Institute",
        education = "Oxford University",
        location = "Electronic City, Bengaluru (18 km away)",
        latitude = 12.8399,
        longitude = 77.6770,
        bio = "Documenting bird migrations and writing field guides 🦅 Plant mom to 20+ succulents and always looking for weekend trail partners.",
        photosJoined = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "The best way to spend a Saturday...",
        promptAnswer = "Morning trail run, iced matcha, and exploring an antique bookstore 🌿",
        passionsJoined = "Wildlife|||Hiking|||Reading|||Sustainability|||Writing",
        zodiac = "Sagittarius",
        height = "5'7\" (170 cm)",
        datingIntention = "Looking for love",
        drinking = "Socially",
        smoking = "Never",
        pets = "Love all pets",
        anthemSong = "Holocene",
        anthemArtist = "Bon Iver",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_samira_9",
        name = "Samira Sen",
        age = 28,
        gender = "Women",
        occupation = "Astrophysics Researcher",
        company = "Cosmic Observations Lab",
        education = "Cambridge University",
        location = "Nandi Hills, Bengaluru (45 km away)",
        latitude = 13.3702,
        longitude = 77.6835,
        bio = "Mapping stellar clusters by night 🔭 Stargazing trips, sci-fi marathons, and board game nights are my favorite things.",
        photosJoined = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "I won't shut up about...",
        promptAnswer = "James Webb Space Telescope discoveries and the mysteries of dark matter 🌌",
        passionsJoined = "Astronomy|||Sci-Fi|||Board Games|||Coffee|||Hiking",
        zodiac = "Aquarius",
        height = "5'8\" (173 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Never",
        smoking = "Never",
        pets = "Cat person",
        anthemSong = "Space Oddity",
        anthemArtist = "David Bowie",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_ananya_10",
        name = "Ananya Bose",
        age = 25,
        gender = "Women",
        occupation = "Illustrator & Animator",
        company = "PixelBloom Studio",
        education = "Srishti Institute of Art",
        location = "Frazer Town, Bengaluru (5 km away)",
        latitude = 12.9981,
        longitude = 77.6142,
        bio = "Sketching cute webcomics and petting neighborhood street cats 🎨 Tell me your favorite animated movie!",
        photosJoined = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "Together we could...",
        promptAnswer = "Paint watercolors in Cubbon Park on Sunday afternoons 🍃",
        passionsJoined = "Illustration|||Animation|||Cats|||Tea|||Comics",
        zodiac = "Cancer",
        height = "5'4\" (163 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Cat lover",
        anthemSong = "Bags",
        anthemArtist = "Clairo",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_kabir_11",
        name = "Kabir Varma",
        age = 29,
        gender = "Men",
        occupation = "Chef & Culinary Explorer",
        company = "Artisan Kitchen",
        education = "Culinary Institute of America",
        location = "MG Road, Bengaluru (3 km away)",
        latitude = 12.9756,
        longitude = 77.6066,
        bio = "Fermenting hot sauces & hosting private tasting dinners 🌶️ Let's find Bengaluru's hidden food gems.",
        photosJoined = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "My best culinary secret...",
        promptAnswer = "A pinch of smoked sea salt turns everything into gourmet perfection 🍲",
        passionsJoined = "Cooking|||Foodie|||Wine|||Travel|||Hospitality",
        zodiac = "Gemini",
        height = "6'0\" (183 cm)",
        datingIntention = "Looking for love",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Passionfruit",
        anthemArtist = "Drake",
        isVerified = true,
        likedMe = true,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_tara_12",
        name = "Tara Nair",
        age = 27,
        gender = "Women",
        occupation = "Marine Conservationist",
        company = "Blue Ocean Foundation",
        education = "James Cook University",
        location = "Jayanagar, Bengaluru (7 km away)",
        latitude = 12.9308,
        longitude = 77.5838,
        bio = "Scuba dive instructor & coral reef protector 🤿 Happiness is clear ocean waters and warm sunny days.",
        photosJoined = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "I feel most alive when...",
        promptAnswer = "Diving with manta rays and listening to ocean swells 🌊",
        passionsJoined = "Scuba Diving|||Ocean|||Sustainability|||Surfing|||Nature",
        zodiac = "Pisces",
        height = "5'7\" (170 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Love animals",
        anthemSong = "Beyond",
        anthemArtist = "Leon Bridges",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_neil_13",
        name = "Neil Sengupta",
        age = 28,
        gender = "Men",
        occupation = "Robotics Engineer",
        company = "Autonomous Dynamics",
        education = "Carnegie Mellon",
        location = "Domlur, Bengaluru (5 km away)",
        latitude = 12.9609,
        longitude = 77.6387,
        bio = "Teaching machines to navigate the world 🤖 Coffee enthusiast, amateur tennis player, and board game geek.",
        photosJoined = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "My typical Sunday looks like...",
        promptAnswer = "Morning tennis match, pour-over brew, and tinkering with open-source code 🎾",
        passionsJoined = "Robotics|||Tennis|||Coffee|||Chess|||Tech",
        zodiac = "Libra",
        height = "5'10\" (178 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Electric Feel",
        anthemArtist = "MGMT",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_kavya_14",
        name = "Kavya Menon",
        age = 26,
        gender = "Women",
        occupation = "Classical Dancer & Choreographer",
        company = "Natya Collective",
        education = "Kalakshetra Foundation",
        location = "Malleswaram, Bengaluru (6 km away)",
        latitude = 13.0031,
        longitude = 77.5643,
        bio = "Bharatanatyam dancer reimagining classical arts 💃 Coffee dates, temple architecture, and soulful Carnatic fusions.",
        photosJoined = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "The key to my heart is...",
        promptAnswer = "Appreciating live music, deep conversations, and sharing filter coffee ☕",
        passionsJoined = "Dance|||Music|||Culture|||Yoga|||Art",
        zodiac = "Taurus",
        height = "5'6\" (168 cm)",
        datingIntention = "Looking for love",
        drinking = "Never",
        smoking = "Never",
        pets = "Cat lover",
        anthemSong = "Nagada Sang Dhol",
        anthemArtist = "Shreya Ghoshal",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_arjun_15",
        name = "Arjun Kapoor",
        age = 31,
        gender = "Men",
        occupation = "Landscape Photographer",
        company = "Wild Horizon",
        education = "National Geographic Expeditions",
        location = "Hebbal, Bengaluru (12 km away)",
        latitude = 13.0358,
        longitude = 77.5970,
        bio = "Chasing mountain sunsets and fog in the Western Ghats 🏔️ 4x4 road trips, campfires, and storytelling under the stars.",
        photosJoined = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "Best road trip tip...",
        promptAnswer = "Never rush the scenic route and always stop for local roadside tea stalls 🚙",
        passionsJoined = "Photography|||Camping|||Trekking|||Road Trips|||Mountains",
        zodiac = "Leo",
        height = "6'1\" (185 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog lover",
        anthemSong = "Society",
        anthemArtist = "Eddie Vedder",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_riya_16",
        name = "Riya Singhania",
        age = 25,
        gender = "Women",
        occupation = "Fashion Stylist & Creative Director",
        company = "Vogue Aesthetics",
        education = "London College of Fashion",
        location = "Lavelle Road, Bengaluru (2 km away)",
        latitude = 12.9720,
        longitude = 77.5985,
        bio = "Vintage silhouettes & modern streetwear 👗 Let's grab iced lattes and check out the new design exhibits.",
        photosJoined = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "Dating me is like...",
        promptAnswer = "Always having the best dressed partner and knowing the most aesthetic spots in town ✨",
        passionsJoined = "Fashion|||Design|||Coffee|||Travel|||Pop Culture",
        zodiac = "Libra",
        height = "5'8\" (173 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Levitating",
        anthemArtist = "Dua Lipa",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_vikram_17",
        name = "Vikram Reddy",
        age = 29,
        gender = "Men",
        occupation = "Product Manager & Coffee Roaster",
        company = "Roast & Flow Labs",
        education = "ISB Hyderabad",
        location = "Koramangala 4th Block, Bengaluru (5 km away)",
        latitude = 12.9340,
        longitude = 77.6290,
        bio = "Cupping specialty beans on Saturday mornings ☕ Crafting digital experiences by weekday. Let's do a blind coffee tasting!",
        photosJoined = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "My non-negotiable is...",
        promptAnswer = "High emotional intelligence and a good sense of humor 😊",
        passionsJoined = "Coffee|||Product|||Running|||Cooking|||Podcasts",
        zodiac = "Virgo",
        height = "5'11\" (180 cm)",
        datingIntention = "Looking for love",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Sunday Best",
        anthemArtist = "Surfaces",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_zoya_18",
        name = "Zoya Merchant",
        age = 27,
        gender = "Women",
        occupation = "Environmental Lawyer",
        company = "Green Justice Alliance",
        education = "NLSIU Bengaluru",
        location = "Richmond Town, Bengaluru (3 km away)",
        latitude = 12.9644,
        longitude = 77.5991,
        bio = "Defending forests and waterways by day 🌿 Weekend marathon runner and indie film lover. Let's debate anything and everything.",
        photosJoined = "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "I value...",
        promptAnswer = "Integrity, curiosity, empathy, and making a meaningful impact on our planet 🌍",
        passionsJoined = "Law|||Environment|||Running|||Books|||Debate",
        zodiac = "Scorpio",
        height = "5'7\" (170 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Cat lover",
        anthemSong = "Dog Days Are Over",
        anthemArtist = "Florence + The Machine",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_siddharth_19",
        name = "Siddharth Rao",
        age = 28,
        gender = "Men",
        occupation = "Game Developer & Pixel Artist",
        company = "IndieForge Games",
        education = "DigiPen Institute",
        location = "JP Nagar, Bengaluru (9 km away)",
        latitude = 12.9063,
        longitude = 77.5857,
        bio = "Creating cozy indie RPG games 🎮 Synth music, retro arcades, and making the best homemade sourdough pizza.",
        photosJoined = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "Let's debate...",
        promptAnswer = "What makes a game truly unforgettable: gameplay loop or emotional narrative? 🕹️",
        passionsJoined = "Gaming|||Game Dev|||Pizza|||Music|||Sci-Fi",
        zodiac = "Aquarius",
        height = "5'10\" (178 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Never",
        smoking = "Never",
        pets = "Cat person",
        anthemSong = "Resonance",
        anthemArtist = "HOME",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_natasha_20",
        name = "Natasha D'Souza",
        age = 26,
        gender = "Women",
        occupation = "Sommelier & Mixologist",
        company = "The Botanist Lounge",
        education = "Court of Master Sommeliers",
        location = "Indiranagar 100ft Rd, Bengaluru (4 km away)",
        latitude = 12.9733,
        longitude = 77.6408,
        bio = "Curating craft cocktails with local botanical infusions 🍸 Great food, jazz vinyl, and spontaneous travels.",
        photosJoined = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "First round is on me if...",
        promptAnswer = "You can recommend a cocktail ingredient I haven't experimented with yet 🍹",
        passionsJoined = "Mixology|||Wine|||Jazz|||Dining|||Travel",
        zodiac = "Leo",
        height = "5'6\" (168 cm)",
        datingIntention = "Looking for love",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Feeling Good",
        anthemArtist = "Nina Simone",
        isVerified = true,
        likedMe = true,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),

      // === PAGE 2 (Profiles 21 - 40) ===
      ProfileEntity(
        id = "profile_ishaan_21",
        name = "Ishaan Joshi",
        age = 27,
        gender = "Men",
        occupation = "Neurologist & Marathoner",
        company = "NIMHANS Research",
        education = "AIIMS New Delhi",
        location = "Bannerghatta Rd, Bengaluru (11 km away)",
        latitude = 12.8942,
        longitude = 77.5982,
        bio = "Studying brain neuroplasticity by day 🧠 Training for the Boston Marathon on mornings. Deep thinker with an easy laugh.",
        photosJoined = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "I'm fascinated by...",
        promptAnswer = "How memory works and how music triggers profound emotional connections 🎶",
        passionsJoined = "Medicine|||Running|||Science|||Coffee|||Philosophy",
        zodiac = "Capricorn",
        height = "6'0\" (183 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Fix You",
        anthemArtist = "Coldplay",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_tanvi_22",
        name = "Tanvi Hegde",
        age = 24,
        gender = "Women",
        occupation = "Botanist & Urban Farmer",
        company = "Verdant Roots",
        education = "UAS Bengaluru",
        location = "Yelahanka, Bengaluru (16 km away)",
        latitude = 13.1007,
        longitude = 77.5963,
        bio = "Growing heirloom tomatoes and creating rooftop greenhouse sanctuaries 🍅 Seed collector, tea lover, and nature advocate.",
        photosJoined = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1517841905240-472988babdf9?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "My dream weekend...",
        promptAnswer = "Visiting an organic farm, foraging fresh berries, and cooking an outdoor feast 🌿",
        passionsJoined = "Botany|||Gardening|||Cooking|||Sustainability|||Tea",
        zodiac = "Taurus",
        height = "5'5\" (165 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Never",
        smoking = "Never",
        pets = "Love all pets",
        anthemSong = "Bloom",
        anthemArtist = "The Paper Kites",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_karan_23",
        name = "Karan Bhatia",
        age = 30,
        gender = "Men",
        occupation = "Aerospace Systems Architect",
        company = "SkyOrbit Technologies",
        education = "ISRO & Purdue",
        location = "HAL Airport Rd, Bengaluru (6 km away)",
        latitude = 12.9568,
        longitude = 77.6631,
        bio = "Designing satellite telemetry systems 🛰️ Amateur astronomer, mountain trekker, and sourdough baker on Sundays.",
        photosJoined = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "Best stargazing location...",
        promptAnswer = "Hanle observatory under the pitch-black Himalayan skies 🌌",
        passionsJoined = "Space|||Aerospace|||Trekking|||Baking|||Astronomy",
        zodiac = "Sagittarius",
        height = "6'2\" (188 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Cosmic Girl",
        anthemArtist = "Jamiroquai",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_meera_24",
        name = "Meera Krishnan",
        age = 26,
        gender = "Women",
        occupation = "Podcast Host & Journalist",
        company = "Stories of Tomorrow",
        education = "Columbia Journalism School",
        location = "Cunningham Rd, Bengaluru (3 km away)",
        latitude = 12.9856,
        longitude = 77.5950,
        bio = "Interviewing innovators, artists, and climate pioneers 🎙️ Avid reader, specialty coffee lover, and collector of vintage postcards.",
        photosJoined = "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "A podcast you must listen to...",
        promptAnswer = "Anything that challenges how you see the world and leaves you with new empathy 💡",
        passionsJoined = "Podcasting|||Journalism|||Books|||Coffee|||Documentaries",
        zodiac = "Gemini",
        height = "5'6\" (168 cm)",
        datingIntention = "Looking for love",
        drinking = "Socially",
        smoking = "Never",
        pets = "Cat person",
        anthemSong = "Landslide",
        anthemArtist = "Fleetwood Mac",
        isVerified = true,
        likedMe = true,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_yash_25",
        name = "Yash Nambiar",
        age = 28,
        gender = "Men",
        occupation = "Architect & Urban Sketcher",
        company = "Gridworks Design",
        education = "CEPT Ahmedabad",
        location = "Basavanagudi, Bengaluru (6 km away)",
        latitude = 12.9419,
        longitude = 77.5746,
        bio = "Carrying a fountain pen and sketchbook wherever I go ✍️ Heritage walks, old Bengaluru cafes, and thoughtful architecture.",
        photosJoined = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "My favorite city corner...",
        promptAnswer = "Sitting beneath a 100-year-old banyan tree with a warm cup of coffee ☕",
        passionsJoined = "Sketching|||Architecture|||History|||Coffee|||Art",
        zodiac = "Pisces",
        height = "5'11\" (180 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Never",
        smoking = "Never",
        pets = "Cat lover",
        anthemSong = "Harvest Moon",
        anthemArtist = "Neil Young",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_simran_26",
        name = "Simran Kaur",
        age = 27,
        gender = "Women",
        occupation = "Pediatric Surgeon",
        company = "Rainbow Children's Hospital",
        education = "CMC Vellore",
        location = "Marathahalli, Bengaluru (12 km away)",
        latitude = 12.9591,
        longitude = 77.6974,
        bio = "Healing little smiles by day 🩺 Weekend baker, dog foster parent, and classical sitar learner.",
        photosJoined = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "What inspires me...",
        promptAnswer = "Resilience, kindness without condition, and children's pure optimism ✨",
        passionsJoined = "Medicine|||Dogs|||Music|||Baking|||Volunteering",
        zodiac = "Aries",
        height = "5'7\" (170 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog foster mom",
        anthemSong = "Better Together",
        anthemArtist = "Jack Johnson",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_aditya_27",
        name = "Aditya Chawla",
        age = 29,
        gender = "Men",
        occupation = "Fintech Founder",
        company = "CredFlow Labs",
        education = "Wharton School",
        location = "UB City, Bengaluru (2 km away)",
        latitude = 12.9715,
        longitude = 77.5956,
        bio = "Simplifying cross-border payments 🚀 Tennis, squash, espresso martinis, and weekend sailing trips.",
        photosJoined = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "I nerd out on...",
        promptAnswer = "Macroeconomics, high-tempo racquet sports, and Italian cuisine 🍝",
        passionsJoined = "Startups|||Tennis|||Squash|||Sailing|||Wine",
        zodiac = "Leo",
        height = "6'1\" (185 cm)",
        datingIntention = "Looking for love",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Can't Stop",
        anthemArtist = "Red Hot Chili Peppers",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_divya_28",
        name = "Divya Ramesh",
        age = 25,
        gender = "Women",
        occupation = "Cognitive AI Scientist",
        company = "DeepReason AI",
        education = "IISc Bengaluru",
        location = "Sadashivanagar, Bengaluru (4 km away)",
        latitude = 13.0070,
        longitude = 77.5800,
        bio = "Researching neural reasoning models 🧠 Loves indie board games, cozy rainy evenings, and spicy ramen.",
        photosJoined = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "My golden rule...",
        promptAnswer = "Never stop asking 'why' and always be willing to change your mind when presented with better evidence 🔬",
        passionsJoined = "AI|||Science|||Board Games|||Ramen|||Reading",
        zodiac = "Aquarius",
        height = "5'6\" (168 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Never",
        smoking = "Never",
        pets = "Cat person",
        anthemSong = "Paranoid Android",
        anthemArtist = "Radiohead",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_rohit_29",
        name = "Rohit Das",
        age = 27,
        gender = "Men",
        occupation = "Wildlife Filmmaker",
        company = "Jungle Lore Films",
        education = "Bristol Film School",
        location = "Kanakapura Rd, Bengaluru (14 km away)",
        latitude = 12.8752,
        longitude = 77.5521,
        bio = "Tracking leopards and wild elephants through the Nilgiris 🐆 Camping enthusiast, kayaker, and campfire guitarist.",
        photosJoined = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "Craziest experience...",
        promptAnswer = "Spending 48 hours in a canopy treehouse filming hornbill nesting rituals 🦤",
        passionsJoined = "Wildlife|||Filmmaking|||Kayaking|||Guitar|||Nature",
        zodiac = "Scorpio",
        height = "5'11\" (180 cm)",
        datingIntention = "Looking for love",
        drinking = "Socially",
        smoking = "Never",
        pets = "Love all animals",
        anthemSong = "Wild World",
        anthemArtist = "Cat Stevens",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_alisha_30",
        name = "Alisha Roy",
        age = 26,
        gender = "Women",
        occupation = "Architectural Conservator",
        company = "Heritage Trust India",
        education = "Courtauld Institute",
        location = "Ulsoor, Bengaluru (3 km away)",
        latitude = 12.9817,
        longitude = 77.6289,
        bio = "Restoring historical stone monuments & stained glass windows 🏰 Tea collector, pottery student, and amateur botanist.",
        photosJoined = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "I feel most at peace...",
        promptAnswer = "Walking along the quiet shores of Ulsoor Lake at sunrise 🌅",
        passionsJoined = "History|||Art|||Pottery|||Tea|||Architecture",
        zodiac = "Libra",
        height = "5'7\" (170 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Cat lover",
        anthemSong = "Mystery of Love",
        anthemArtist = "Sufjan Stevens",
        isVerified = true,
        likedMe = true,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_varun_31",
        name = "Varun Dixit",
        age = 31,
        gender = "Men",
        occupation = "Renewable Energy Engineer",
        company = "Solaris Grid Systems",
        education = "IIT Madras",
        location = "Electronic City Phase 1, Bengaluru (17 km away)",
        latitude = 12.8452,
        longitude = 77.6602,
        bio = "Building smart solar microgrids for rural communities ☀️ Long-distance cycling, specialty tea, and weekend woodworking.",
        photosJoined = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "A cause close to my heart...",
        promptAnswer = "Clean energy access and climate resilience for every family on the planet 🌍",
        passionsJoined = "Clean Energy|||Cycling|||Woodworking|||Engineering|||Reading",
        zodiac = "Virgo",
        height = "6'0\" (183 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Here Comes The Sun",
        anthemArtist = "The Beatles",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_shreya_32",
        name = "Shreya Deshmukh",
        age = 24,
        gender = "Women",
        occupation = "Contemporary Ceramic Artist",
        company = "Earthen Glaze Studio",
        education = "NID Ahmedabad",
        location = "Cooke Town, Bengaluru (5 km away)",
        latitude = 12.9972,
        longitude = 77.6258,
        bio = "Handcrafting organic pottery bowls & vases 🏺 Coffee, indie folk records, and laughing until my stomach hurts.",
        photosJoined = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "My simple pleasure...",
        promptAnswer = "Morning filter coffee while clay dries in the sunlight ☕",
        passionsJoined = "Ceramics|||Pottery|||Art|||Coffee|||Plants",
        zodiac = "Taurus",
        height = "5'5\" (165 cm)",
        datingIntention = "Looking for love",
        drinking = "Socially",
        smoking = "Never",
        pets = "Cat person",
        anthemSong = "Rivers and Roads",
        anthemArtist = "The Head and the Heart",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_manav_33",
        name = "Manav Chopra",
        age = 28,
        gender = "Men",
        occupation = "Sports Physiotherapist",
        company = "Elite Athletic Institute",
        education = "University of Melbourne",
        location = "Kalyan Nagar, Bengaluru (8 km away)",
        latitude = 13.0189,
        longitude = 77.6432,
        bio = "Rehabilitating pro athletes & training for half-ironman events 🏊‍♂️ Love good espresso, swimming, and lively dinner banter.",
        photosJoined = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "Favorite workout routine...",
        promptAnswer = "Early morning ocean swim followed by warm croissants and pour-over coffee 🥐",
        passionsJoined = "Fitness|||Swimming|||Physiotherapy|||Coffee|||Running",
        zodiac = "Aries",
        height = "6'1\" (185 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Midnight City",
        anthemArtist = "M83",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_pooja_34",
        name = "Pooja Hegde",
        age = 27,
        gender = "Women",
        occupation = "Culinary Anthropologist & Food Writer",
        company = "Spice & Origin Press",
        education = "SOAS University of London",
        location = "Koramangala 3rd Block, Bengaluru (5 km away)",
        latitude = 12.9312,
        longitude = 77.6225,
        bio = "Tracing the history of South Asian spice routes 🌶️ Sourdough baker, cookbook addict, and weekend dinner host.",
        photosJoined = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1517841905240-472988babdf9?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "Best food travel memory...",
        promptAnswer = "Eating freshly steamed momos on a misty morning in Darjeeling 🥟",
        passionsJoined = "Food Writing|||Cooking|||Spices|||Travel|||History",
        zodiac = "Cancer",
        height = "5'6\" (168 cm)",
        datingIntention = "Looking for love",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Sweet Creature",
        anthemArtist = "Harry Styles",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_kunal_35",
        name = "Kunal Kapoor",
        age = 30,
        gender = "Men",
        occupation = "Jazz Pianist & Music Producer",
        company = "Blue Horizon Sessions",
        education = "Manhattan School of Music",
        location = "Indiranagar, Bengaluru (4 km away)",
        latitude = 12.9710,
        longitude = 77.6415,
        bio = "Improvising jazz chords & scoring indie cinema 🎹 Always looking for someone to share good vinyl records and midnight drives.",
        photosJoined = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "Song that gives me chills...",
        promptAnswer = "Miles Davis - So What on a warm summer night 🎺",
        passionsJoined = "Jazz|||Piano|||Cinema|||Vinyl|||Music",
        zodiac = "Gemini",
        height = "5'11\" (180 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Cat person",
        anthemSong = "Take Five",
        anthemArtist = "Dave Brubeck",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_kriti_36",
        name = "Kriti Sanon",
        age = 25,
        gender = "Women",
        occupation = "Sustainable Fashion Designer",
        company = "Khadi Revival Studio",
        education = "NIFT Delhi",
        location = "Commercial Street, Bengaluru (3 km away)",
        latitude = 12.9822,
        longitude = 77.6083,
        bio = "Weaving indigenous organic textiles into contemporary silhouettes 👗 Plant lover, matcha drinker, and film enthusiast.",
        photosJoined = "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "My style philosophy...",
        promptAnswer = "Wear what makes you feel fearless and kind ✨",
        passionsJoined = "Fashion|||Design|||Sustainability|||Textiles|||Art",
        zodiac = "Leo",
        height = "5'8\" (173 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Cat person",
        anthemSong = "Golden",
        anthemArtist = "Harry Styles",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_samarth_37",
        name = "Samarth Jain",
        age = 28,
        gender = "Men",
        occupation = "Applied Mathematician",
        company = "Quantum Dynamics",
        education = "Cambridge & ISI",
        location = "Malleshwaram, Bengaluru (5 km away)",
        latitude = 13.0045,
        longitude = 77.5712,
        bio = "Exploring algebraic geometry & playing tournament chess ♟️ Pour-over coffee connoisseur and lover of classical literature.",
        photosJoined = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "A great conversation starts with...",
        promptAnswer = "An open mind and a question you don't know the answer to ☕",
        passionsJoined = "Math|||Chess|||Books|||Coffee|||Philosophy",
        zodiac = "Capricorn",
        height = "5'10\" (178 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Never",
        smoking = "Never",
        pets = "Dog person",
        anthemSong = "Clair de Lune",
        anthemArtist = "Claude Debussy",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_radhika_38",
        name = "Radhika Apte",
        age = 27,
        gender = "Women",
        occupation = "Theatre Director & Playwright",
        company = "Rangashankara Repertory",
        education = "National School of Drama",
        location = "JP Nagar 2nd Phase, Bengaluru (8 km away)",
        latitude = 12.9102,
        longitude = 77.5891,
        bio = "Staging contemporary theatre & writing scripts 🎭 Chai stall discussions, monologues, and soulful acoustic concerts.",
        photosJoined = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "Theatre taught me...",
        promptAnswer = "How to truly listen and be fully present in every moment ✨",
        passionsJoined = "Theatre|||Writing|||Drama|||Literature|||Chai",
        zodiac = "Virgo",
        height = "5'6\" (168 cm)",
        datingIntention = "Looking for love",
        drinking = "Socially",
        smoking = "Never",
        pets = "Love pets",
        anthemSong = "The Sound of Silence",
        anthemArtist = "Simon & Garfunkel",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_rahul_39",
        name = "Rahul Nambisan",
        age = 29,
        gender = "Men",
        occupation = "Bicycle Frame Builder",
        company = "Velocraft Bicycles",
        education = "Brunel Design School",
        location = "Hennur, Bengaluru (10 km away)",
        latitude = 13.0360,
        longitude = 77.6390,
        bio = "Welding custom steel bike frames & gravel riding through coffee estates 🚴‍♂️ Coffee lover, camper, and jazz enthusiast.",
        photosJoined = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "Best weekend adventure...",
        promptAnswer = "Bikepacking across the Western Ghats with a small tent and a french press 🏕️",
        passionsJoined = "Cycling|||Craftsmanship|||Camping|||Coffee|||Outdoors",
        zodiac = "Sagittarius",
        height = "6'0\" (183 cm)",
        datingIntention = "Long-term relationship",
        drinking = "Socially",
        smoking = "Never",
        pets = "Dog lover",
        anthemSong = "Go Your Own Way",
        anthemArtist = "Fleetwood Mac",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      ),
      ProfileEntity(
        id = "profile_eshita_40",
        name = "Eshita Bhattacharya",
        age = 26,
        gender = "Women",
        occupation = "Ethnomusicologist & Sitarist",
        company = "Raga Heritage Sound",
        education = "Santiniketan Visva-Bharati",
        location = "Sadashivanagar, Bengaluru (5 km away)",
        latitude = 13.0080,
        longitude = 77.5830,
        bio = "Recording folk music traditions across India 🎶 Lover of old books, monsoon rains, and cozy book cafes.",
        photosJoined = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80|||https://images.unsplash.com/photo-1517841905240-472988babdf9?w=900&auto=format&fit=crop&q=80",
        promptQuestion = "What makes life beautiful...",
        promptAnswer = "A cup of hot darjeeling tea, falling rain, and sincere conversations 🌧️",
        passionsJoined = "Music|||Sitar|||Culture|||Books|||Tea",
        zodiac = "Pisces",
        height = "5'7\" (170 cm)",
        datingIntention = "Looking for love",
        drinking = "Never",
        smoking = "Never",
        pets = "Cat person",
        anthemSong = "Raag Yaman",
        anthemArtist = "Pandit Ravi Shankar",
        isVerified = true,
        likedMe = false,
        isLikedByMe = false,
        isPassedByMe = false,
        isSuperLikedByMe = false,
        isMutualMatch = false,
        matchedTimestamp = null
      )
    )

    val startIndex = (page - 1) * pageSize
    val endIndex = startIndex + pageSize

    if (startIndex < masterCatalog.size) {
      val slice = masterCatalog.subList(startIndex, minOf(endIndex, masterCatalog.size))
      if (slice.size >= pageSize || slice.isNotEmpty()) {
        return slice
      }
    }

    // Dynamic generator for higher pages (Page 3: 41-60, Page 4: 61-80, etc.)
    val proceduralCandidates = mutableListOf<ProfileEntity>()
    val firstNames = listOf("Aanya", "Dhruv", "Isha", "Rhea", "Armaan", "Mira", "Adil", "Kiara", "Devansh", "Avani", "Rehan", "Suhana")
    val lastNames = listOf("Verma", "Kapoor", "Nair", "Iyer", "Banerjee", "Fernandes", "Desai", "Rao", "Choudhury", "Bose")
    val occupations = listOf("Architect", "AI Researcher", "Photographer", "Chef", "Product Designer", "Writer", "Data Scientist", "Filmmaker")
    val photos = listOf(
      "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80",
      "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=900&auto=format&fit=crop&q=80",
      "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=80",
      "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=900&auto=format&fit=crop&q=80",
      "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=900&auto=format&fit=crop&q=80",
      "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=900&auto=format&fit=crop&q=80"
    )

    for (i in 0 until pageSize) {
      val profileNum = (page - 1) * pageSize + (i + 1)
      val name = "${firstNames[i % firstNames.size]} ${lastNames[i % lastNames.size]}"
      val gender = if (i % 2 == 0) "Women" else "Men"
      val age = 22 + (profileNum % 10)
      val occ = occupations[i % occupations.size]
      val photo1 = photos[i % photos.size]
      val photo2 = photos[(i + 1) % photos.size]

      proceduralCandidates.add(
        ProfileEntity(
          id = "profile_candidate_$profileNum",
          name = name,
          age = age,
          gender = gender,
          occupation = occ,
          company = "Studio $occ",
          education = "University of Design & Arts",
          location = "Bengaluru (${3 + (profileNum % 12)} km away)",
          latitude = 12.9716 + (profileNum * 0.002),
          longitude = 77.5946 + (profileNum * 0.002),
          bio = "Passionate about $occ & specialty coffee ☕ Exploring the city's hidden art spots and weekend trails.",
          photosJoined = "$photo1|||$photo2",
          promptQuestion = "My simple pleasures...",
          promptAnswer = "Sunday mornings, good music, and deep conversations ✨",
          passionsJoined = "Design|||Coffee|||Travel|||Art|||Music",
          zodiac = "Libra",
          height = "5'8\" (173 cm)",
          datingIntention = "Long-term relationship",
          drinking = "Socially",
          smoking = "Never",
          pets = "Dog person",
          anthemSong = "Sunflower",
          anthemArtist = "Post Malone",
          isVerified = true,
          likedMe = false,
          isLikedByMe = false,
          isPassedByMe = false,
          isSuperLikedByMe = false,
          isMutualMatch = false,
          matchedTimestamp = null
        )
      )
    }
    return proceduralCandidates
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

    // 2. Fetch raw page candidates
    val rawPageCandidates = generatePageCandidates(page, pageSize)

    // 3. Apply all exclusion rules to the page candidates
    val eligibleCandidates = rawPageCandidates.filter { candidate ->
      // Exclusion 1: Not in any excluded IDs (liked, passed, matched, blocked, deleted, own profile)
      if (allExcludedIds.contains(candidate.id)) return@filter false
      if (candidate.isLikedByMe || candidate.isPassedByMe || candidate.isSuperLikedByMe || candidate.isMutualMatch) return@filter false

      // Exclusion 2: Filter by Gender / Interested in Preference
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
          return@filter false
        }
      } else if (effectiveInterestedIn.equals("Men", ignoreCase = true)) {
        if (candidate.gender.isNotBlank() &&
            !candidate.gender.equals("Man", ignoreCase = true) &&
            !candidate.gender.equals("Men", ignoreCase = true) &&
            !candidate.gender.equals("Male", ignoreCase = true)) {
          return@filter false
        }
      }

      // Exclusion 3: Filter by Age Preference
      val minAge = if (currentUser.minAgePreference in 18..100) currentUser.minAgePreference else 18
      val maxAge = if (currentUser.maxAgePreference in 18..100) currentUser.maxAgePreference else 35
      if (candidate.age in 18..100 && (candidate.age < minAge || candidate.age > maxAge)) {
        return@filter false
      }

      true
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

    val myMessage = ChatMessageEntity(
      id = UUID.randomUUID().toString(),
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
      isRead = true
    )
    dao.insertMessage(myMessage)

    // Sync sent message to Cloud Firestore in real time
    if (firestoreManager.isAvailable && myUserId.isNotBlank()) {
      appScope.launch {
        firestoreManager.sendChatMessage(matchId, myMessage.toDomain(), myUserId)

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
    dao.deleteProfileById(matchId)
    deleteMessagesForMatch(matchId)
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

    // 1. Save directly to Cloud Firestore backend as the authoritative source of truth
    if (firestoreManager.isAvailable) {
      try {
        firestoreManager.syncUserProfile(fixedProfile)
        if (fixedProfile.isOnboardingCompleted && fixedProfile.name.isNotBlank()) {
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
    // Delete active user_profile, local cached chats, swipe history, notifications, and profiles deck
    // so no previous user's chat or match data remains on the device.
    dao.deleteUserProfile()
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
    val current = dao.getUserProfileFlow().firstOrNull()?.toDomain()
    if (current != null && firestoreManager.isAvailable) {
      try {
        firestoreManager.syncUserProfile(current.copy(isAccountDisabled = disabled))
      } catch (e: Exception) {
        Log.w("KatkatRepository", "Notice disabling account in Firestore: ${e.message}")
      }
    }
    dao.setAccountDisabled(disabled)
  }

  suspend fun deleteAccount(): Boolean {
    val current = dao.getUserProfileFlow().firstOrNull()?.toDomain()
    if (current != null && firestoreManager.isAvailable) {
      try {
        firestoreManager.deleteUserProfile(current.id)
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
  recipientId = recipientId
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
  recipientId = recipientId
)
