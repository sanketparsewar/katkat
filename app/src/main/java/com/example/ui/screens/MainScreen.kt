package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
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
import com.example.viewmodel.KatkatViewModel
import com.example.viewmodel.UiEvent

@Composable
fun MainScreen(
  viewModel: KatkatViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  var currentTab by remember { mutableStateOf(KatkatTab.DISCOVER) }

  val activeProfiles by viewModel.activeProfiles.collectAsStateWithLifecycle()
  val mutualMatches by viewModel.mutualMatches.collectAsStateWithLifecycle()
  val likedMeProfiles by viewModel.likedMeProfiles.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val subscriptionState by viewModel.subscriptionState.collectAsStateWithLifecycle()
  val isRefreshingDeck by viewModel.isRefreshingDeck.collectAsStateWithLifecycle()

  val activeMatchCelebration by viewModel.activeMatchCelebration.collectAsStateWithLifecycle()
  val showPaywall by viewModel.showPaywall.collectAsStateWithLifecycle()
  val inspectedProfile by viewModel.inspectedProfile.collectAsStateWithLifecycle()
  val selectedChatMatch by viewModel.selectedChatMatch.collectAsStateWithLifecycle()
  val activeChatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()

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

  // If a chat is open, show ChatDetailScreen full screen
  val currentChat = selectedChatMatch
  if (currentChat != null) {
    ChatDetailScreen(
      match = currentChat,
      messages = activeChatMessages,
      onSendMessage = { text -> viewModel.sendMessage(text) },
      onBack = { viewModel.closeChat() },
      onInspectProfile = { viewModel.inspectProfile(currentChat) }
    )
  } else {
    Scaffold(
      modifier = modifier.fillMaxSize(),
      topBar = {
        KatkatTopBar(
          subscriptionState = subscriptionState,
          onOpenPaywall = { viewModel.openPaywall() }
        )
      },
      bottomBar = {
        KatkatBottomNav(
          currentTab = currentTab,
          onTabSelected = { currentTab = it },
          likesCount = likedMeProfiles.size,
          unreadMatchesCount = mutualMatches.size
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
              onOpenPaywall = { viewModel.openPaywall() }
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
              onInspectProfile = { profile -> viewModel.inspectProfile(profile) }
            )
          }

          KatkatTab.MATCHES -> {
            MatchesChatScreen(
              matches = mutualMatches,
              onSelectMatch = { profile -> viewModel.openChat(profile) },
              onNavigateToDiscover = { currentTab = KatkatTab.DISCOVER }
            )
          }

          KatkatTab.PROFILE -> {
            ProfileEditScreen(
              userProfile = userProfile,
              subscriptionState = subscriptionState,
              onSaveProfile = { updated -> viewModel.updateProfile(updated) },
              onAddPhoto = { uri -> viewModel.addPhotoToProfile(uri) },
              onRemovePhoto = { idx -> viewModel.removePhotoFromProfile(idx) },
              onSetPrimaryPhoto = { idx -> viewModel.setPrimaryPhoto(idx) },
              onReplacePhoto = { idx, uri -> viewModel.replacePhotoAtSlot(idx, uri) },
              onOpenPaywall = { viewModel.openPaywall() }
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
        viewModel.sendMessage(text)
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
    ProfileDetailBottomSheet(
      profile = profileToInspect,
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
      onDismiss = {
        viewModel.inspectProfile(null)
      }
    )
  }
}

