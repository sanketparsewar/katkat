package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KatkatDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.DatingProfile
import com.example.data.model.SubscriptionState
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserProfile
import com.example.data.repository.KatkatRepository
import com.example.data.repository.SwipeAction
import com.example.data.repository.SwipeResult
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

  // Session loaded state from SQLite database
  private val _isSessionLoaded = MutableStateFlow(false)
  val isSessionLoaded: StateFlow<Boolean> = _isSessionLoaded.asStateFlow()

  // App opening Greeting Splash visibility
  private val _showGreetingSplash = MutableStateFlow(true)
  val showGreetingSplash: StateFlow<Boolean> = _showGreetingSplash.asStateFlow()

  init {
    viewModelScope.launch {
      repository.userProfile.collect {
        _isSessionLoaded.value = true
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

  fun sendMessage(text: String, photoUri: String? = null) {
    val currentMatch = _selectedChatMatch.value ?: return
    if (text.isBlank() && photoUri == null) return

    viewModelScope.launch {
      repository.sendMessage(currentMatch.id, text.trim(), photoUri)
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

  fun uploadAndAddPhoto(context: android.content.Context, uri: android.net.Uri, onComplete: ((String) -> Unit)? = null) {
    viewModelScope.launch {
      val current = userProfile.value
      val cleanPhone = current.phoneNumber.filter { it.isDigit() }
      val userId = if (current.id.startsWith("user_") && current.id.length > 5) {
        current.id
      } else {
        "user_${cleanPhone.ifBlank { System.currentTimeMillis().toString() }}"
      }
      _uiEvents.emit(UiEvent.ShowToast("Uploading photo to Firebase Storage..."))
      val storedUrl = repository.firebaseStorageManager.uploadProfileImage(context, userId, uri)
      if (storedUrl.isNotBlank() && (storedUrl.startsWith("http://") || storedUrl.startsWith("https://"))) {
        val newPhotos = current.photos.filter { it.isNotBlank() }.toMutableList().apply {
          if (size < 6) add(storedUrl) else set(5, storedUrl)
        }
        val updated = current.copy(photos = newPhotos)
        repository.saveUserProfile(updated)
        _uiEvents.emit(UiEvent.ShowToast("✓ Photo uploaded to cloud storage ✨"))
        onComplete?.invoke(storedUrl)
      } else {
        _uiEvents.emit(UiEvent.ShowToast("Could not upload to cloud storage. Please check connection."))
      }
    }
  }

  fun uploadAndAddBitmap(context: android.content.Context, bitmap: android.graphics.Bitmap, onComplete: ((String) -> Unit)? = null) {
    viewModelScope.launch {
      val current = userProfile.value
      val cleanPhone = current.phoneNumber.filter { it.isDigit() }
      val userId = if (current.id.startsWith("user_") && current.id.length > 5) {
        current.id
      } else {
        "user_${cleanPhone.ifBlank { System.currentTimeMillis().toString() }}"
      }
      _uiEvents.emit(UiEvent.ShowToast("Uploading photo to Firebase Storage..."))
      val storedUrl = repository.firebaseStorageManager.uploadBitmap(context, userId, bitmap)
      if (storedUrl.isNotBlank() && (storedUrl.startsWith("http://") || storedUrl.startsWith("https://"))) {
        val newPhotos = current.photos.filter { it.isNotBlank() }.toMutableList().apply {
          if (size < 6) add(storedUrl) else set(5, storedUrl)
        }
        val updated = current.copy(photos = newPhotos)
        repository.saveUserProfile(updated)
        _uiEvents.emit(UiEvent.ShowToast("✓ Photo uploaded to cloud storage ✨"))
        onComplete?.invoke(storedUrl)
      } else {
        _uiEvents.emit(UiEvent.ShowToast("Could not upload to cloud storage. Please check connection."))
      }
    }
  }

  fun uploadAndReplacePhoto(context: android.content.Context, index: Int, uri: android.net.Uri, onComplete: ((String) -> Unit)? = null) {
    viewModelScope.launch {
      val current = userProfile.value
      val cleanPhone = current.phoneNumber.filter { it.isDigit() }
      val userId = if (current.id.startsWith("user_") && current.id.length > 5) {
        current.id
      } else {
        "user_${cleanPhone.ifBlank { System.currentTimeMillis().toString() }}"
      }
      _uiEvents.emit(UiEvent.ShowToast("Updating photo in Firebase Storage..."))
      val storedUrl = repository.firebaseStorageManager.uploadProfileImage(context, userId, uri)
      if (storedUrl.isNotBlank() && (storedUrl.startsWith("http://") || storedUrl.startsWith("https://"))) {
        val newPhotos = current.photos.filter { it.isNotBlank() }.toMutableList()
        if (index in newPhotos.indices) {
          newPhotos[index] = storedUrl
        } else if (newPhotos.size < 6) {
          newPhotos.add(storedUrl)
        }
        val updated = current.copy(photos = newPhotos)
        repository.saveUserProfile(updated)
        _uiEvents.emit(UiEvent.ShowToast("✓ Photo updated in cloud storage ✨"))
        onComplete?.invoke(storedUrl)
      } else {
        _uiEvents.emit(UiEvent.ShowToast("Could not upload to cloud storage. Please check connection."))
      }
    }
  }

  fun uploadAndReplaceBitmap(context: android.content.Context, index: Int, bitmap: android.graphics.Bitmap, onComplete: ((String) -> Unit)? = null) {
    viewModelScope.launch {
      val current = userProfile.value
      val cleanPhone = current.phoneNumber.filter { it.isDigit() }
      val userId = if (current.id.startsWith("user_") && current.id.length > 5) {
        current.id
      } else {
        "user_${cleanPhone.ifBlank { System.currentTimeMillis().toString() }}"
      }
      _uiEvents.emit(UiEvent.ShowToast("Updating photo in Firebase Storage..."))
      val storedUrl = repository.firebaseStorageManager.uploadBitmap(context, userId, bitmap)
      if (storedUrl.isNotBlank() && (storedUrl.startsWith("http://") || storedUrl.startsWith("https://"))) {
        val newPhotos = current.photos.filter { it.isNotBlank() }.toMutableList()
        if (index in newPhotos.indices) {
          newPhotos[index] = storedUrl
        } else if (newPhotos.size < 6) {
          newPhotos.add(storedUrl)
        }
        val updated = current.copy(photos = newPhotos)
        repository.saveUserProfile(updated)
        _uiEvents.emit(UiEvent.ShowToast("✓ Photo updated in cloud storage ✨"))
        onComplete?.invoke(storedUrl)
      } else {
        _uiEvents.emit(UiEvent.ShowToast("Could not upload to cloud storage. Please check connection."))
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
      repository.resetDeckForTesting()
      _isRefreshingDeck.value = false
      _uiEvents.emit(UiEvent.ShowToast("Discover deck refreshed! ✨"))
      _uiEvents.emit(UiEvent.VibrateFeedback("refresh"))
    }
  }

  fun refreshDeck() {
    resetDeck()
  }
}


