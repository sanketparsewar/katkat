package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DatingProfile
import com.example.data.model.SubscriptionState
import com.example.ui.components.ActionButtonsBar
import com.example.ui.components.CardSwipeDirection
import com.example.ui.components.SwipeCard
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(
  profiles: List<DatingProfile>,
  isRefreshing: Boolean,
  subscriptionState: SubscriptionState,
  onRefresh: () -> Unit,
  onSwipeLeft: (String) -> Unit,
  onSwipeRight: (String) -> Unit,
  onSuperLike: (String) -> Unit,
  onRewind: () -> Unit,
  onBoost: () -> Unit,
  onInspectProfile: (DatingProfile) -> Unit,
  onResetDeck: () -> Unit,
  onOpenPaywall: () -> Unit,
  modifier: Modifier = Modifier
) {
  val refreshState = rememberPullToRefreshState()
  var programmaticSwipe by remember { mutableStateOf<CardSwipeDirection?>(null) }
  var topCardDragProgress by remember { mutableFloatStateOf(0f) }
  var topCardDragDirection by remember { mutableStateOf<CardSwipeDirection?>(null) }

  val topProfile = profiles.firstOrNull()
  val topProfileId = topProfile?.id

  // Automatically reset card drag and programmatic state when the top card changes
  LaunchedEffect(topProfileId) {
    programmaticSwipe = null
    topCardDragProgress = 0f
    topCardDragDirection = null
  }

  // Safety fallback: ensure programmatic swipe lock is never stuck
  LaunchedEffect(programmaticSwipe) {
    if (programmaticSwipe != null) {
      delay(400)
      if (programmaticSwipe != null) {
        val currentTop = topProfile
        val dir = programmaticSwipe
        programmaticSwipe = null
        topCardDragProgress = 0f
        topCardDragDirection = null
        if (currentTop != null && dir != null) {
          when (dir) {
            CardSwipeDirection.LEFT -> onSwipeLeft(currentTop.id)
            CardSwipeDirection.RIGHT -> onSwipeRight(currentTop.id)
            CardSwipeDirection.UP -> onSuperLike(currentTop.id)
          }
        }
      }
    }
  }

  PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    state = refreshState,
    indicator = {
      PullToRefreshDefaults.Indicator(
        state = refreshState,
        isRefreshing = isRefreshing,
        modifier = Modifier.align(Alignment.TopCenter),
        containerColor = CoralPrimary,
        color = Color.White
      )
    },
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("swipe_screen")
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (profiles.isNotEmpty()) {
        // Swipe Deck Box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 16.dp, vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          // Render up to 2 cards for optimal performance & stack depth
          val visibleCards = profiles.take(2).reversed()
          visibleCards.forEach { profile ->
            key(profile.id) {
              val isTop = profile.id == topProfileId
              // Dynamically scale and lift background card as top card moves
              val bgScale = if (isTop) 1f else 0.94f + (0.06f * topCardDragProgress)
              val bgYOffset = if (isTop) 0.dp else 12.dp * (1f - topCardDragProgress)
              val bgAlpha = if (isTop) 1f else 0.85f + (0.15f * topCardDragProgress)

              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .graphicsLayer {
                    scaleX = bgScale
                    scaleY = bgScale
                    translationY = bgYOffset.toPx()
                    alpha = bgAlpha
                  }
              ) {
                SwipeCard(
                  profile = profile,
                  onSwipedLeft = {
                    topCardDragProgress = 0f
                    topCardDragDirection = null
                    programmaticSwipe = null
                    onSwipeLeft(profile.id)
                  },
                  onSwipedRight = {
                    topCardDragProgress = 0f
                    topCardDragDirection = null
                    programmaticSwipe = null
                    onSwipeRight(profile.id)
                  },
                  onSuperLiked = {
                    topCardDragProgress = 0f
                    topCardDragDirection = null
                    programmaticSwipe = null
                    onSuperLike(profile.id)
                  },
                  onDragProgress = { fraction, direction ->
                    if (isTop) {
                      topCardDragProgress = fraction
                      topCardDragDirection = direction
                    }
                  },
                  onInspectProfile = { onInspectProfile(profile) },
                  isTopCard = isTop,
                  programmaticSwipe = if (isTop) programmaticSwipe else null
                )
              }
            }
          }
        }

        // Action Buttons Bar connected to spring physics animations and reactive drag highlighting
        ActionButtonsBar(
          onRewind = onRewind,
          onPass = {
            if (programmaticSwipe == null && profiles.isNotEmpty()) {
              programmaticSwipe = CardSwipeDirection.LEFT
            }
          },
          onSuperLike = {
            if (programmaticSwipe == null && profiles.isNotEmpty()) {
              programmaticSwipe = CardSwipeDirection.UP
            }
          },
          onLike = {
            if (programmaticSwipe == null && profiles.isNotEmpty()) {
              programmaticSwipe = CardSwipeDirection.RIGHT
            }
          },
          onBoost = onBoost,
          enabled = programmaticSwipe == null,
          activeDirection = topCardDragDirection,
          dragFraction = topCardDragProgress
        )
      } else {
        // Empty Deck State with animated radar pulse
        EmptyDeckView(
          onResetDeck = onResetDeck,
          onOpenPaywall = onOpenPaywall,
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        )
      }
    }
  }
}

@Composable
fun EmptyDeckView(
  onResetDeck: () -> Unit,
  onOpenPaywall: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "radar")
  val radarScale1 by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 2.4f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radar_1"
  )
  val radarAlpha1 by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radar_alpha_1"
  )

  Box(
    modifier = modifier
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Radar pulsing circles
      Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(80.dp)
            .scale(radarScale1)
            .clip(CircleShape)
            .background(CoralPrimary.copy(alpha = radarAlpha1))
        )
        Box(
          modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = CoralPrimary,
            modifier = Modifier.size(36.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "You've Swiped Everyone Nearby!",
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface
        ),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Check back soon for new profiles, or pull down to refresh the deck.",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          lineHeight = 20.sp
        ),
        modifier = Modifier.padding(horizontal = 24.dp)
      )

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = onResetDeck,
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
        modifier = Modifier.testTag("btn_refresh_deck")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Refresh Deck", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
