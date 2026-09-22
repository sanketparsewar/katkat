package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KatkatDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.DatingProfile
import com.example.data.model.KatkatNotification
import com.example.data.model.KatkatNotificationType
import com.example.data.model.MatchConversation
import com.example.data.model.SubscriptionState
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserProfile
import com.example.data.repository.KatkatRepository
import com.example.data.repository.SwipeAction
import com.example.data.repository.SwipeResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class UiEvent {
  data class ShowToast(val message: String) : UiEvent()
  data class VibrateFeedback(val type: String) : UiEvent()
}

class KatkatViewModel(application: Application) : AndroidViewModel(application) {
  private val database = KatkatDatabase.getDatabase(application)
  private val repository = KatkatRepository(database.datingDao(), viewModelScope)

  val activeProfiles: StateFlow<List<DatingProfile>> = repository.activeProfiles.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val mutualMatches: StateFlow<List<DatingProfile>> = repository.mutualMatches.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val likedMeProfiles: StateFlow<List<DatingProfile>> = repository.profilesWhoLikedMe.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val userProfile: StateFlow<UserProfile> = repository.userProfile.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = UserProfile()
  )

  val notifications: StateFlow<List<KatkatNotification>> = userProfile.flatMapLatest { profile ->
    repository.getNotificationsFlow(profile.id)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val unreadNotificationCount: StateFlow<Int> = userProfile.flatMapLatest { profile ->
    repository.getUnreadNotificationsCountFlow(profile.id)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = 0
  )

  // Session loaded state from SQLite database
  private val _isSessionLoaded = MutableStateFlow(false)
  val isSessionLoaded: StateFlow<Boolean> = _isSessionLoaded.asStateFlow()

  // Photo upload loading state
  private val _isUploadingPhoto = MutableStateFlow(false)
  val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto.asStateFlow()

  // App opening Greeting Splash visibility
  private val _showGreetingSplash = MutableStateFlow(true)
  val showGreetingSplash: StateFlow<Boolean> = _showGreetingSplash.asStateFlow()

  init {
    viewModelScope.launch {
      repository.userProfile.collect {
        _isSessionLoaded.value = true
      }
    }
    // Listen for real-time cross-device matches
    viewModelScope.launch {
      repository.realtimeMatchEvent.collect { matchedProfile ->
        _activeMatchCelebration.value = matchedProfile
        _uiEvents.emit(UiEvent.VibrateFeedback("match"))
      }
    }
    // Listen for real-time blocks to dismiss chat if blocked
    viewModelScope.launch {
      repository.realtimeBlockedEvent.collect { blockedId ->
        if (_selectedChatMatch.value?.id == blockedId) {
          _selectedChatMatch.value = null
          _uiEvents.emit(UiEvent.ShowToast("This conversation is no longer available."))
        }
      }
    }
  }

  fun dismissGreeting() {
    _showGreetingSplash.value = false
  }

  val subscriptionState: StateFlow<SubscriptionState> = repository.subscriptionState.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = SubscriptionState()
  )

  private val _uiEvents = MutableSharedFlow<UiEvent>()
  val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

  // App Theme Mode (default LIGHT)
  private val _themeMode = MutableStateFlow(com.example.ui.theme.AppThemeMode.LIGHT)
  val themeMode: StateFlow<com.example.ui.theme.AppThemeMode> = _themeMode.asStateFlow()

  fun setThemeMode(mode: com.example.ui.theme.AppThemeMode) {
    _themeMode.value = mode
  }

  // Match celebration dialog state
  private val _activeMatchCelebration = MutableStateFlow<DatingProfile?>(null)
  val activeMatchCelebration: StateFlow<DatingProfile?> = _activeMatchCelebration.asStateFlow()

  // Pull-to-refresh state for discover deck
  private val _isRefreshingDeck = MutableStateFlow(false)
  val isRefreshingDeck: StateFlow<Boolean> = _isRefreshingDeck.asStateFlow()

  // Paywall bottom sheet state
  private val _showPaywall = MutableStateFlow(false)
  val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

  // Profile detail bottom sheet
  private val _inspectedProfile = MutableStateFlow<DatingProfile?>(null)
  val inspectedProfile: StateFlow<DatingProfile?> = _inspectedProfile.asStateFlow()

  // Selected chat profile
  private val _selectedChatMatch = MutableStateFlow<DatingProfile?>(null)
  val selectedChatMatch: StateFlow<DatingProfile?> = _selectedChatMatch.asStateFlow()

  // Active chat messages
  val activeChatMessages: StateFlow<List<ChatMessage>> = _selectedChatMatch
    .flatMapLatest { profile ->
      if (profile != null) {
        repository.getMessages(profile.id)
      } else {
        flowOf(emptyList())
      }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  // Reactive match conversations with last messages, unread counts, and timestamps
  val conversations: StateFlow<List<MatchConversation>> = repository.conversations.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // Real-time typing indicators for matches
  private val _typingMatchIds = MutableStateFlow<Set<String>>(emptySet())
  val typingMatchIds: StateFlow<Set<String>> = _typingMatchIds.asStateFlow()

  fun onSwipe(profileId: String, action: SwipeAction) {
    viewModelScope.launch {
      val result = repository.processSwipe(profileId, action)
      when (result) {
        is SwipeResult.MutualMatch -> {
          _activeMatchCelebration.value = result.profile
          _uiEvents.emit(UiEvent.VibrateFeedback("match"))
        }
        is SwipeResult.LimitReached -> {
          _showPaywall.value = true
          _uiEvents.emit(UiEvent.ShowToast("Monthly swipe limit reached (${result.tier.monthlySwipes} swipes). Upgrade to continue!"))
        }
        is SwipeResult.Success -> {
          val feedbackType = when (action) {
            SwipeAction.LIKE -> "swipe_like"
            SwipeAction.PASS -> "swipe_pass"
            SwipeAction.SUPERLIKE -> "swipe_superlike"
          }
          _uiEvents.emit(UiEvent.VibrateFeedback(feedbackType))
        }
        is SwipeResult.Error -> {
          _uiEvents.emit(UiEvent.ShowToast(result.message))
        }
      }
    }
  }

  fun swipeLeft(profileId: String) = onSwipe(profileId, SwipeAction.PASS)
  fun swipeRight(profileId: String) = onSwipe(profileId, SwipeAction.LIKE)
  fun superLike(profileId: String) = onSwipe(profileId, SwipeAction.SUPERLIKE)

  fun rewind() {
    viewModelScope.launch {
      val currentSub = subscriptionState.value
      // Free tier doesn't have unlimited rewind, show paywall or allow with notice
      if (currentSub.currentTier == SubscriptionTier.FREE) {
        _showPaywall.value = true
        _uiEvents.emit(UiEvent.ShowToast("Unlimited Rewind is a Katkat Plus & VIP feature!"))
        return@launch
      }

      val success = repository.rewindLastSwipe()
      if (success) {
        _uiEvents.emit(UiEvent.VibrateFeedback("rewind"))
        _uiEvents.emit(UiEvent.ShowToast("Last swipe undone! ↺"))
      } else {
        _uiEvents.emit(UiEvent.ShowToast("No previous swipes to rewind"))
      }
    }
  }

  fun boostProfile() {
    viewModelScope.launch {
      val currentSub = subscriptionState.value
      if (currentSub.currentTier == SubscriptionTier.FREE) {
        _showPaywall.value = true
        _uiEvents.emit(UiEvent.ShowToast("Profile Boost is a Katkat VIP feature! ⚡"))
      } else {
        _uiEvents.emit(UiEvent.VibrateFeedback("boost"))
        _uiEvents.emit(UiEvent.ShowToast("⚡ Profile Boosted! You are 10x more visible for 30 minutes!"))
      }
    }
  }

  fun openPaywall() {
    _showPaywall.value = true
  }

  fun dismissPaywall() {
    _showPaywall.value = false
  }

  fun selectTier(tier: SubscriptionTier, isAnnual: Boolean) {
    viewModelScope.launch {
      repository.upgradeSubscription(tier, isAnnual)
      _showPaywall.value = false
      _uiEvents.emit(UiEvent.ShowToast("✨ Upgraded to ${tier.title}! Swipe limit expanded to ${tier.monthlySwipes}/month."))
      _uiEvents.emit(UiEvent.VibrateFeedback("upgrade"))
    }
  }

  fun restorePurchases() {
    viewModelScope.launch {
      _uiEvents.emit(UiEvent.ShowToast("RevenueCat: Purchases successfully restored!"))
    }
  }

  fun resetSwipeUsage() {
    viewModelScope.launch {
      repository.resetSwipeCounter()
      _uiEvents.emit(UiEvent.ShowToast("Monthly swipe counter reset for testing!"))
    }
  }

  fun inspectProfile(profile: DatingProfile?) {
    _inspectedProfile.value = profile
  }

  fun dismissMatchCelebration() {
    _activeMatchCelebration.value = null
  }

  fun openChat(profile: DatingProfile) {
    _selectedChatMatch.value = profile
    _activeMatchCelebration.value = null
    viewModelScope.launch {
      repository.markMessagesRead(profile.id)
    }
  }

  fun closeChat() {
    _selectedChatMatch.value = null
  }

  fun sendMessage(text: String, photoUri: String? = null, targetMatchId: String? = null) {
    val currentMatchId = targetMatchId ?: _selectedChatMatch.value?.id ?: return
    if (text.isBlank() && photoUri == null) return

    viewModelScope.launch {
      repository.sendMessage(currentMatchId, text.trim(), photoUri)
      _uiEvents.emit(UiEvent.VibrateFeedback("click"))
    }
  }

  fun unmatch(matchId: String) {
    viewModelScope.launch {
      repository.unmatch(matchId)
      if (_selectedChatMatch.value?.id == matchId) {
        _selectedChatMatch.value = null
      }
      _uiEvents.emit(UiEvent.ShowToast("Unmatched profile"))
      _uiEvents.emit(UiEvent.VibrateFeedback("click"))
    }
  }

  fun blockUser(matchId: String) {
    viewModelScope.launch {
      repository.blockProfile(matchId)
      if (_selectedChatMatch.value?.id == matchId) {
        _selectedChatMatch.value = null
      }
      _uiEvents.emit(UiEvent.ShowToast("Profile blocked"))
      _uiEvents.emit(UiEvent.VibrateFeedback("click"))
    }
  }

  fun clearChat(matchId: String) {
    viewModelScope.launch {
      repository.deleteMessagesForMatch(matchId)
      _uiEvents.emit(UiEvent.ShowToast("Chat history cleared"))
      _uiEvents.emit(UiEvent.VibrateFeedback("click"))
    }
  }

  fun createTestMatch() {
    viewModelScope.launch {
      val matched = repository.createSimulatedTestMatch()
      if (matched != null) {
        _uiEvents.emit(UiEvent.ShowToast("Matched with ${matched.name}! 💕"))
        _uiEvents.emit(UiEvent.VibrateFeedback("match"))
      } else {
        _uiEvents.emit(UiEvent.ShowToast("No more candidate profiles to match!"))
      }
    }
  }

  fun autoSaveProfile(profile: UserProfile) {
    viewModelScope.launch {
      repository.saveUserProfile(profile)
      _uiEvents.emit(UiEvent.ShowToast("Profile updated"))
      _uiEvents.emit(UiEvent.VibrateFeedback("save"))
    }
  }

  fun updateProfile(profile: UserProfile, showToast: Boolean = true) {
    viewModelScope.launch {
      repository.saveUserProfile(profile)
      if (showToast) {
        _uiEvents.emit(UiEvent.ShowToast("Profile updated"))
        _uiEvents.emit(UiEvent.VibrateFeedback("save"))
      }
    }
  }

  fun initUserByPhone(phone: String, countryCode: String) {
    viewModelScope.launch {
      val user = repository.getOrInitUserByPhone(phone, countryCode)
      if (user.isOnboardingCompleted) {
        _uiEvents.emit(UiEvent.ShowToast("Welcome back, ${user.name.ifBlank { "User" }}! ✨"))
      }
    }
  }

  fun disableAccount(disabled: Boolean) {
    viewModelScope.launch {
      repository.disableAccount(disabled)
      val msg = if (disabled) "Account disabled. Profile hidden from Discover." else "Account activated! Profile is now visible."
      _uiEvents.emit(UiEvent.ShowToast(msg))
    }
  }

  fun deleteAccount() {
    viewModelScope.launch {
      repository.deleteAccount()
      _uiEvents.emit(UiEvent.ShowToast("Account permanently deleted."))
    }
  }

  val phoneAuthManager: com.example.data.remote.PhoneAuthManager
    get() = repository.phoneAuthManager

  val firebaseStorageManager: com.example.data.remote.FirebaseStorageManager
    get() = repository.firebaseStorageManager

  suspend fun uploadProfilePhoto(context: android.content.Context, userId: String, uri: android.net.Uri): String {
    return repository.firebaseStorageManager.uploadProfileImage(context, userId, uri)
  }

  suspend fun uploadProfileBitmap(context: android.content.Context, userId: String, bitmap: android.graphics.Bitmap): String {
    return repository.firebaseStorageManager.uploadBitmap(context, userId, bitmap)
  }

  suspend fun checkExistingUser(phone: String, countryCode: String): UserProfile? {
    return repository.checkExistingUserByPhone(phone, countryCode)
  }

  fun showGreetingAndEnter(profile: UserProfile) {
    viewModelScope.launch {
      val activeProfile = profile.copy(isOnboardingCompleted = true)
      repository.saveUserProfile(activeProfile)
      _showGreetingSplash.value = false
      _uiEvents.emit(UiEvent.ShowToast("Welcome back, ${activeProfile.name}! ✨"))
      _uiEvents.emit(UiEvent.VibrateFeedback("match"))
    }
  }

  fun logout() {
    viewModelScope.launch {
      repository.logoutActiveSession()
      _uiEvents.emit(UiEvent.ShowToast("Logged out successfully"))
    }
  }

  fun completeOnboarding(profile: UserProfile) {
    viewModelScope.launch {
      val completed = profile.copy(isOnboardingCompleted = true)
      repository.saveUserProfile(completed)
      _showGreetingSplash.value = true
      _uiEvents.emit(UiEvent.ShowToast("Welcome to Katkat, ${completed.name}! Your profile is active ✨"))
      _uiEvents.emit(UiEvent.VibrateFeedback("match"))
    }
  }

  fun restartOnboarding() {
    viewModelScope.launch {
      repository.logoutActiveSession()
    }
  }

  fun addPhotoToProfile(photoUri: String) {
    if (photoUri.startsWith("http://") || photoUri.startsWith("https://")) {
      val current = userProfile.value
      val newPhotos = current.photos.filter { it.isNotBlank() }.toMutableList().apply {
        if (size < 6) add(photoUri) else set(5, photoUri)
      }
      updateProfile(current.copy(photos = newPhotos))
    }
  }

  /**
   * Resolves the canonical user ID for Storage and database records.
   * Prioritizes Firebase Auth UID, then the existing profile ID, before falling back to phone/timestamp.
   * This guarantees photos are always stored in the exact same user folder in Firebase Storage.
   */
  fun getEffectiveUserId(): String {
    val current = userProfile.value
    val authUid = phoneAuthManager.currentUserId
    return when {
      !authUid.isNullOrBlank() -> authUid
      current.id.isNotBlank() && current.id != "my_profile" -> current.id
      else -> {
        val cleanPhone = current.phoneNumber.filter { it.isDigit() }
        if (cleanPhone.isNotBlank()) "user_$cleanPhone" else "user_${System.currentTimeMillis()}"
      }
    }
  }

  fun uploadAndAddPhoto(context: android.content.Context, uri: android.net.Uri, onComplete: ((String) -> Unit)? = null) {
    viewModelScope.launch {
      _isUploadingPhoto.value = true
      try {
        val current = userProfile.value
        val userId = getEffectiveUserId()
        _uiEvents.emit(UiEvent.ShowToast("Uploading..."))
        val storedUrl = repository.firebaseStorageManager.uploadProfileImage(context, userId, uri)
        if (storedUrl.isNotBlank()) {
          val newPhotos = current.photos.filter { it.isNotBlank() }.toMutableList().apply {
            if (size < 6) add(storedUrl) else set(5, storedUrl)
          }
          val updated = current.copy(photos = newPhotos)
          repository.saveUserProfile(updated)
          _uiEvents.emit(UiEvent.ShowToast("Photo saved"))
          onComplete?.invoke(storedUrl)
        } else {
          val err = repository.firebaseStorageManager.lastErrorMessage ?: "Could not upload photo. Please check connection."
          _uiEvents.emit(UiEvent.ShowToast(err))
        }
      } finally {
        _isUploadingPhoto.value = false
      }
    }
  }

  fun uploadAndAddBitmap(context: android.content.Context, bitmap: android.graphics.Bitmap, onComplete: ((String) -> Unit)? = null) {
    viewModelScope.launch {
      _isUploadingPhoto.value = true
      try {
        val current = userProfile.value
        val userId = getEffectiveUserId()
        _uiEvents.emit(UiEvent.ShowToast("Uploading..."))
        val storedUrl = repository.firebaseStorageManager.uploadBitmap(context, userId, bitmap)
        if (storedUrl.isNotBlank()) {
          val newPhotos = current.photos.filter { it.isNotBlank() }.toMutableList().apply {
            if (size < 6) add(storedUrl) else set(5, storedUrl)
          }
          val updated = current.copy(photos = newPhotos)
          repository.saveUserProfile(updated)
          _uiEvents.emit(UiEvent.ShowToast("Photo saved"))
          onComplete?.invoke(storedUrl)
        } else {
          val err = repository.firebaseStorageManager.lastErrorMessage ?: "Could not upload photo. Please check connection."
          _uiEvents.emit(UiEvent.ShowToast(err))
        }
      } finally {
        _isUploadingPhoto.value = false
      }
    }
  }

  fun uploadAndReplacePhoto(context: android.content.Context, index: Int, uri: android.net.Uri, onComplete: ((String) -> Unit)? = null) {
    viewModelScope.launch {
      _isUploadingPhoto.value = true
      try {
        val current = userProfile.value
        val userId = getEffectiveUserId()
        _uiEvents.emit(UiEvent.ShowToast("Uploading..."))
        val storedUrl = repository.firebaseStorageManager.uploadProfileImage(context, userId, uri)
        if (storedUrl.isNotBlank()) {
          val newPhotos = current.photos.filter { it.isNotBlank() }.toMutableList()
          if (index in newPhotos.indices) {
            newPhotos[index] = storedUrl
          } else if (newPhotos.size < 6) {
            newPhotos.add(storedUrl)
          }
          val updated = current.copy(photos = newPhotos)
          repository.saveUserProfile(updated)
          _uiEvents.emit(UiEvent.ShowToast("Photo saved"))
          onComplete?.invoke(storedUrl)
        } else {
          val err = repository.firebaseStorageManager.lastErrorMessage ?: "Could not upload photo. Please check connection."
          _uiEvents.emit(UiEvent.ShowToast(err))
        }
      } finally {
        _isUploadingPhoto.value = false
      }
    }
  }

  fun uploadAndReplaceBitmap(context: android.content.Context, index: Int, bitmap: android.graphics.Bitmap, onComplete: ((String) -> Unit)? = null) {
    viewModelScope.launch {
      _isUploadingPhoto.value = true
      try {
        val current = userProfile.value
        val userId = getEffectiveUserId()
        _uiEvents.emit(UiEvent.ShowToast("Uploading..."))
        val storedUrl = repository.firebaseStorageManager.uploadBitmap(context, userId, bitmap)
        if (storedUrl.isNotBlank()) {
          val newPhotos = current.photos.filter { it.isNotBlank() }.toMutableList()
          if (index in newPhotos.indices) {
            newPhotos[index] = storedUrl
          } else if (newPhotos.size < 6) {
            newPhotos.add(storedUrl)
          }
          val updated = current.copy(photos = newPhotos)
          repository.saveUserProfile(updated)
          _uiEvents.emit(UiEvent.ShowToast("Photo saved"))
          onComplete?.invoke(storedUrl)
        } else {
          val err = repository.firebaseStorageManager.lastErrorMessage ?: "Could not upload photo. Please check connection."
          _uiEvents.emit(UiEvent.ShowToast(err))
        }
      } finally {
        _isUploadingPhoto.value = false
      }
    }
  }

  fun replacePhotoAtSlot(index: Int, photoUri: String) {
    if (photoUri.startsWith("http://") || photoUri.startsWith("https://")) {
      val current = userProfile.value
      val newPhotos = current.photos.filter { it.isNotBlank() }.toMutableList()
      if (index in newPhotos.indices) {
        newPhotos[index] = photoUri
      } else if (newPhotos.size < 6) {
        newPhotos.add(photoUri)
      }
      updateProfile(current.copy(photos = newPhotos))
    }
  }

  fun setPrimaryPhoto(index: Int) {
    val current = userProfile.value
    val nonBlankPhotos = current.photos.filter { it.isNotBlank() }
    if (index in 1 until nonBlankPhotos.size) {
      val newPhotos = nonBlankPhotos.toMutableList()
      val selectedPhoto = newPhotos.removeAt(index)
      newPhotos.add(0, selectedPhoto)
      updateProfile(current.copy(photos = newPhotos))
    }
  }

  fun removePhotoFromProfile(index: Int) {
    val current = userProfile.value
    val nonBlankPhotos = current.photos.filter { it.isNotBlank() }
    if (index in nonBlankPhotos.indices && nonBlankPhotos.size > 1) {
      val newPhotos = nonBlankPhotos.toMutableList().apply { removeAt(index) }
      updateProfile(current.copy(photos = newPhotos))
    }
  }

  fun resetDeck() {
    viewModelScope.launch {
      _isRefreshingDeck.value = true
      kotlinx.coroutines.delay(650)
      val currentUserId = getEffectiveUserId()
      repository.resetDeckForTesting(currentUserId)
      _isRefreshingDeck.value = false
      _uiEvents.emit(UiEvent.ShowToast("Discover deck refreshed! ✨"))
      _uiEvents.emit(UiEvent.VibrateFeedback("refresh"))
    }
  }

  fun refreshDeck() {
    resetDeck()
  }

  // --- Notification Actions ---
  fun markNotificationAsRead(notificationId: String) {
    viewModelScope.launch {
      repository.markNotificationAsRead(notificationId)
    }
  }

  fun markAllNotificationsAsRead() {
    viewModelScope.launch {
      val userId = getEffectiveUserId()
      repository.markAllNotificationsAsRead(userId)
      _uiEvents.emit(UiEvent.ShowToast("All notifications marked as read"))
    }
  }

  fun deleteNotification(notificationId: String) {
    viewModelScope.launch {
      repository.deleteNotification(notificationId)
    }
  }

  fun clearAllNotifications() {
    viewModelScope.launch {
      val userId = getEffectiveUserId()
      repository.clearAllNotifications(userId)
      _uiEvents.emit(UiEvent.ShowToast("All notifications cleared"))
    }
  }

  fun postSystemNotification(title: String, message: String) {
    viewModelScope.launch {
      val userId = getEffectiveUserId()
      repository.postSystemNotification(title, message, userId)
    }
  }

  fun triggerSimulatedNotification(type: KatkatNotificationType) {
    viewModelScope.launch {
      val userId = getEffectiveUserId()
      val notif = when (type) {
        KatkatNotificationType.NEW_MATCH -> KatkatNotification(
          userId = userId,
          type = KatkatNotificationType.NEW_MATCH,
          title = "It's a Match! 🎉",
          message = "Yaaa! You have a new match!",
          senderProfileName = "Sarah Chen",
          deepLinkTarget = "chat"
        )
        KatkatNotificationType.NEW_MESSAGE -> KatkatNotification(
          userId = userId,
          type = KatkatNotificationType.NEW_MESSAGE,
          title = "New Message 💬",
          message = "Sarah Chen sent you a message",
          senderProfileName = "Sarah Chen",
          deepLinkTarget = "chat"
        )
        KatkatNotificationType.MESSAGE_READ -> KatkatNotification(
          userId = userId,
          type = KatkatNotificationType.MESSAGE_READ,
          title = "Message Read 👀",
          message = "Sarah Chen read your message",
          senderProfileName = "Sarah Chen",
          deepLinkTarget = "chat"
        )
        KatkatNotificationType.PROFILE_ACTIVITY -> KatkatNotification(
          userId = userId,
          type = KatkatNotificationType.PROFILE_ACTIVITY,
          title = "New Like! ✨",
          message = "Someone liked you",
          senderProfileName = "Aarav Sharma",
          deepLinkTarget = "likes_you"
        )
        KatkatNotificationType.SYSTEM_NOTIFICATION -> KatkatNotification(
          userId = userId,
          type = KatkatNotificationType.SYSTEM_NOTIFICATION,
          title = "Welcome to Katkat Dating",
          message = "Explore eligible matches near your location and start connecting!"
        )
      }
      repository.postLocalNotification(notif)
      _uiEvents.emit(UiEvent.ShowToast("Notification generated: ${notif.title}"))
      _uiEvents.emit(UiEvent.VibrateFeedback("tap"))
    }
  }
}


