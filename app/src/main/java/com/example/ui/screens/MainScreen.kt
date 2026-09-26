package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DatingProfile
import com.example.ui.components.KatkatBottomNav
import com.example.ui.components.KatkatTab
import com.example.ui.components.KatkatTopBar
import com.example.ui.components.MatchCelebrationDialog
import com.example.ui.components.PaywallBottomSheet
import com.example.ui.components.ProfileDetailBottomSheet
import com.example.util.HapticHelper
import com.example.util.PushNotificationHelper
import com.example.viewmodel.KatkatViewModel
import com.example.viewmodel.UiEvent

@Composable
fun MainScreen(
  viewModel: KatkatViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Push Notifications Permission Launcher for Android 13+ (Tiramisu)
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    // Notification permission granted or declined
  }

  LaunchedEffect(Unit) {
    PushNotificationHelper.createNotificationChannel(context)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (!PushNotificationHelper.hasNotificationPermission(context)) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  val selectedChatMatch by viewModel.selectedChatMatch.collectAsStateWithLifecycle()
  val activeChatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()

  // Keep KatkatApplication informed of which match chat is currently active on screen
  LaunchedEffect(selectedChatMatch) {
    com.example.KatkatApplication.activeChatPartnerId = selectedChatMatch?.id
  }

  var currentTab by remember { mutableStateOf(KatkatTab.DISCOVER) }

  val activeProfiles by viewModel.activeProfiles.collectAsStateWithLifecycle()
  val mutualMatches by viewModel.mutualMatches.collectAsStateWithLifecycle()
  val likedMeProfiles by viewModel.likedMeProfiles.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val subscriptionState by viewModel.subscriptionState.collectAsStateWithLifecycle()
  val isRefreshingDeck by viewModel.isRefreshingDeck.collectAsStateWithLifecycle()
  val isRefreshingLikes by viewModel.isRefreshingLikes.collectAsStateWithLifecycle()
  val isRefreshingChats by viewModel.isRefreshingChats.collectAsStateWithLifecycle()
  val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
  val isLoadingMoreProfiles by viewModel.isLoadingMoreProfiles.collectAsStateWithLifecycle()
  val isSessionLoaded by viewModel.isSessionLoaded.collectAsStateWithLifecycle()
  val isUploadingPhoto by viewModel.isUploadingPhoto.collectAsStateWithLifecycle()
  val showGreetingSplash by viewModel.showGreetingSplash.collectAsStateWithLifecycle()

  val activeMatchCelebration by viewModel.activeMatchCelebration.collectAsStateWithLifecycle()
  val showPaywall by viewModel.showPaywall.collectAsStateWithLifecycle()
  val inspectedProfile by viewModel.inspectedProfile.collectAsStateWithLifecycle()
  val conversations by viewModel.conversations.collectAsStateWithLifecycle()
  val typingMatchIds by viewModel.typingMatchIds.collectAsStateWithLifecycle()
  val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
  val notifications by viewModel.notifications.collectAsStateWithLifecycle()
  val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()

  // Real-time network and error states
  val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
  val deckErrorMessage by viewModel.deckErrorMessage.collectAsStateWithLifecycle()
  val likesErrorMessage by viewModel.likesErrorMessage.collectAsStateWithLifecycle()
  val matchesErrorMessage by viewModel.matchesErrorMessage.collectAsStateWithLifecycle()
  val profileErrorMessage by viewModel.profileErrorMessage.collectAsStateWithLifecycle()

  // Calculate actual unread conversation count for bottom navigation badge
  val unreadConversationsCount = remember(conversations) {
    conversations.count { it.unreadCount > 0 }
  }

  var showDiscoveryPreferences by remember { mutableStateOf(false) }

  // Intercept system back gestures to prevent app closing:
  // 1. If chat is open, back takes user back to chats list
  BackHandler(enabled = selectedChatMatch != null) {
    viewModel.closeChat()
  }

  // 2. If profile is being inspected in bottom sheet, dismiss it
  BackHandler(enabled = inspectedProfile != null) {
    viewModel.inspectProfile(null)
  }

  // 3. If paywall sheet is open, dismiss it
  BackHandler(enabled = showPaywall) {
    viewModel.dismissPaywall()
  }

  // 4. If discovery preferences sheet is open, dismiss it
  BackHandler(enabled = showDiscoveryPreferences) {
    showDiscoveryPreferences = false
  }

  // 5. If match celebration dialog is open, dismiss it
  BackHandler(enabled = activeMatchCelebration != null) {
    viewModel.dismissMatchCelebration()
  }

  // 6. If user is on a secondary tab (Likes, Profile, Account), back returns to Chats or Discover
  BackHandler(enabled = selectedChatMatch == null && inspectedProfile == null && !showPaywall && !showDiscoveryPreferences && activeMatchCelebration == null && currentTab != KatkatTab.DISCOVER) {
    if (currentTab == KatkatTab.PROFILE || currentTab == KatkatTab.ACCOUNT) {
      currentTab = KatkatTab.MATCHES
    } else {
      currentTab = KatkatTab.DISCOVER
    }
  }

  // Handle ViewModel Toast & Vibration events
  LaunchedEffect(Unit) {
    viewModel.uiEvents.collect { event ->
      when (event) {
        is UiEvent.ShowToast -> {
          Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
        }
        is UiEvent.VibrateFeedback -> {
          HapticHelper.triggerHaptic(context, event.type)
        }
      }
    }
  }

  // 1. App Opening Greeting Splash with text motion of Katkat as loading page
  if (showGreetingSplash) {
    GreetingLoadingScreen(
      isSessionLoaded = isSessionLoaded,
      isLoggedIn = userProfile.isOnboardingCompleted,
      onRedirect = {
        viewModel.dismissGreeting()
      }
    )
    return
  }

  // 2. First-Time User Profile Onboarding Step-by-Step Flow (only shown when not logged in)
  if (!userProfile.isOnboardingCompleted) {
    OnboardingProfileSetupScreen(
      initialProfile = userProfile,
      viewModel = viewModel,
      onExistingUserFound = { existingProfile ->
        viewModel.showGreetingAndEnter(existingProfile)
      },
      onComplete = { completedProfile ->
        viewModel.completeOnboarding(completedProfile)
        currentTab = KatkatTab.DISCOVER
      }
    )
    return
  }

  // If a chat is open, show ChatDetailScreen full screen
  val currentChat = selectedChatMatch
  if (currentChat != null) {
    ChatDetailScreen(
      match = currentChat,
      messages = activeChatMessages,
      onSendMessage = { text, photoUri -> viewModel.sendMessage(text = text, photoUri = photoUri) },
      onRetryMessage = { msgId -> viewModel.retrySendMessage(msgId) },
      onBack = { viewModel.closeChat() },
      onInspectProfile = { viewModel.inspectProfile(currentChat) },
      isMatchTyping = typingMatchIds.contains(currentChat.id),
      onClearChat = { viewModel.clearChat(currentChat.id) },
      onUnmatch = { viewModel.unmatch(currentChat.id) },
      onBlockProfile = { viewModel.blockUser(currentChat.id) },
      onReportProfile = { reason, details, blockAlso ->
        viewModel.reportUser(currentChat.id, reason, details, blockAlso)
      }
    )
  } else {
    Scaffold(
      modifier = modifier.fillMaxSize(),
      topBar = {
        if (currentTab != KatkatTab.PROFILE && currentTab != KatkatTab.ACCOUNT) {
          KatkatTopBar(
            subscriptionState = subscriptionState,
            onOpenPaywall = { viewModel.openPaywall() },
            onOpenFilter = { showDiscoveryPreferences = true }
          )
        }
      },
      bottomBar = {
        KatkatBottomNav(
          currentTab = currentTab,
          onTabSelected = { currentTab = it },
          likesCount = likedMeProfiles.size,
          unreadMatchesCount = unreadConversationsCount
        )
      },
      containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (currentTab) {
          KatkatTab.DISCOVER -> {
            SwipeScreen(
              profiles = activeProfiles,
              isRefreshing = isRefreshingDeck,
              subscriptionState = subscriptionState,
              onRefresh = { viewModel.refreshDeck() },
              onSwipeLeft = { id -> viewModel.swipeLeft(id) },
              onSwipeRight = { id -> viewModel.swipeRight(id) },
              onSuperLike = { id -> viewModel.superLike(id) },
              onRewind = { viewModel.rewind() },
              onBoost = { viewModel.boostProfile() },
              onInspectProfile = { profile -> viewModel.inspectProfile(profile) },
              onResetDeck = { viewModel.resetDeck() },
              onOpenPaywall = { viewModel.openPaywall() },
              currentPage = currentPage,
              isLoadingMore = isLoadingMoreProfiles,
              onLoadMore = { viewModel.loadNextPage() },
              isAccountPaused = userProfile.isAccountDisabled,
              onUnpauseAccount = { viewModel.disableAccount(false) },
              isOnline = isOnline,
              errorMessage = deckErrorMessage,
              onRetry = { viewModel.retryLoadProfiles() }
            )
          }

          KatkatTab.LIKES_YOU -> {
            LikesYouScreen(
              likedProfiles = likedMeProfiles,
              subscriptionState = subscriptionState,
              onOpenPaywall = { viewModel.openPaywall() },
              onInstantMatch = { profile ->
                viewModel.swipeRight(profile.id)
              },
              onPassProfile = { profile ->
                viewModel.passFromLikes(profile.id)
              },
              onInspectProfile = { profile -> viewModel.inspectProfile(profile) },
              onNavigateToDiscover = { currentTab = KatkatTab.DISCOVER },
              isRefreshing = isRefreshingLikes,
              onRefresh = { viewModel.refreshLikes() },
              isOnline = isOnline,
              errorMessage = likesErrorMessage,
              onRetry = { viewModel.retryLoadLikes() }
            )
          }

          KatkatTab.MATCHES -> {
            MatchesChatScreen(
              matches = mutualMatches,
              conversations = conversations,
              onSelectMatch = { profile -> viewModel.openChat(profile) },
              onNavigateToDiscover = { currentTab = KatkatTab.DISCOVER },
              isRefreshing = isRefreshingChats,
              onRefresh = { viewModel.refreshChats() },
              isOnline = isOnline,
              errorMessage = matchesErrorMessage,
              onRetry = { viewModel.retryLoadMatches() }
            )
          }

          KatkatTab.PROFILE -> {
            ProfileEditScreen(
              userProfile = userProfile,
              subscriptionState = subscriptionState,
              themeMode = themeMode,
              likesCount = likedMeProfiles.size,
              matchesCount = mutualMatches.size,
              chatsCount = mutualMatches.size,
              isUploadingPhoto = isUploadingPhoto,
              notifications = notifications,
              unreadNotificationCount = unreadNotificationCount,
              onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
              onSaveProfile = { updated -> viewModel.autoSaveProfile(updated) },
              onAddPhoto = { uriString ->
                val parsed = try { android.net.Uri.parse(uriString) } catch (_: Exception) { null }
                if (parsed != null && (uriString.startsWith("content://") || uriString.startsWith("file://"))) {
                  viewModel.uploadAndAddPhoto(context, parsed)
                } else if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                  viewModel.addPhotoToProfile(uriString)
                }
              },
              onAddBitmap = { bitmap ->
                viewModel.uploadAndAddBitmap(context, bitmap)
              },
              onRemovePhoto = { idx -> viewModel.removePhotoFromProfile(idx) },
              onSetPrimaryPhoto = { idx -> viewModel.setPrimaryPhoto(idx) },
              onReplacePhoto = { idx, uriString ->
                val parsed = try { android.net.Uri.parse(uriString) } catch (_: Exception) { null }
                if (parsed != null && (uriString.startsWith("content://") || uriString.startsWith("file://"))) {
                  viewModel.uploadAndReplacePhoto(context, idx, parsed)
                } else {
                  viewModel.replacePhotoAtSlot(idx, uriString)
                }
              },
              onOpenPaywall = { viewModel.openPaywall() },
              onRestartOnboarding = { viewModel.restartOnboarding() },
              onDisableAccount = { disabled -> viewModel.disableAccount(disabled) },
              onDeleteAccount = { viewModel.deleteAccount() },
              onLogout = { viewModel.logout() },
              onMarkNotificationRead = { id -> viewModel.markNotificationAsRead(id) },
              onMarkAllNotificationsRead = { viewModel.markAllNotificationsAsRead() },
              onDeleteNotification = { id -> viewModel.deleteNotification(id) },
              onClearAllNotifications = { viewModel.clearAllNotifications() },
              onNavigateToChat = { currentTab = KatkatTab.MATCHES }
            )
          }

          KatkatTab.ACCOUNT -> {
            AccountScreen(
              userProfile = userProfile,
              subscriptionState = subscriptionState,
              themeMode = themeMode,
              onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
              onOpenPaywall = { viewModel.openPaywall() },
              onDisableAccount = { disabled -> viewModel.disableAccount(disabled) },
              onDeleteAccount = { viewModel.deleteAccount() },
              onLogout = { viewModel.logout() },
              isOnline = isOnline,
              errorMessage = profileErrorMessage,
              onRetry = { viewModel.retryLoadProfile() }
            )
          }
        }
      }
    }
  }

  // ── Overlays & Modals ──────────────────────────────────────────────

  // 1. Mutual Match Celebration Dialog
  val matchCelebration = activeMatchCelebration
  if (matchCelebration != null) {
    MatchCelebrationDialog(
      matchedProfile = matchCelebration,
      userProfile = userProfile,
      onSendMessage = { text ->
        viewModel.sendMessage(text = text, targetMatchId = matchCelebration.id)
      },
      onOpenChat = {
        viewModel.openChat(matchCelebration)
      },
      onKeepSwiping = {
        viewModel.dismissMatchCelebration()
      }
    )
  }

  // 2. RevenueCat Native Paywall Bottom Sheet
  if (showPaywall) {
    PaywallBottomSheet(
      subscriptionState = subscriptionState,
      onSelectTier = { tier, isAnnual ->
        viewModel.selectTier(tier, isAnnual)
      },
      onRestorePurchases = {
        viewModel.restorePurchases()
      },
      onResetSwipeUsageForTesting = {
        viewModel.resetSwipeUsage()
      },
      onDismiss = {
        viewModel.dismissPaywall()
      }
    )
  }

  // 3. Inspect Full Profile Bottom Sheet
  val profileToInspect = inspectedProfile
  if (profileToInspect != null) {
    val isAlreadyMatched = profileToInspect.isMutualMatch || mutualMatches.any { it.id == profileToInspect.id } || selectedChatMatch?.id == profileToInspect.id
    ProfileDetailBottomSheet(
      profile = profileToInspect,
      isMatched = isAlreadyMatched,
      onLike = {
        viewModel.swipeRight(profileToInspect.id)
        viewModel.inspectProfile(null)
      },
      onPass = {
        viewModel.swipeLeft(profileToInspect.id)
        viewModel.inspectProfile(null)
      },
      onSuperLike = {
        viewModel.superLike(profileToInspect.id)
        viewModel.inspectProfile(null)
      },
      onBlock = {
        viewModel.blockUser(profileToInspect.id)
        viewModel.inspectProfile(null)
      },
      onReport = { reason, details, alsoBlock ->
        viewModel.reportUser(profileToInspect.id, reason, details, alsoBlock)
        viewModel.inspectProfile(null)
      },
      onDismiss = {
        viewModel.inspectProfile(null)
      }
    )
  }

  // 4. Discovery & Preferences Bottom Sheet
  if (showDiscoveryPreferences) {
    com.example.ui.components.DiscoveryPreferencesBottomSheet(
      userProfile = userProfile,
      subscriptionState = subscriptionState,
      onSavePreferences = { updated ->
        viewModel.updateProfile(updated)
      },
      onOpenPaywall = {
        showDiscoveryPreferences = false
        viewModel.openPaywall()
      },
      onDismiss = {
        showDiscoveryPreferences = false
      }
    )
  }
}

