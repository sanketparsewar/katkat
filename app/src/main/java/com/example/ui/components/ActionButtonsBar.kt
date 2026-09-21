package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BoostPurple
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.ui.theme.NopeRed
import com.example.ui.theme.SuperlikeBlue
import com.example.util.HapticHelper

@Composable
fun ActionButtonsBar(
  onRewind: () -> Unit,
  onPass: () -> Unit,
  onSuperLike: () -> Unit,
  onLike: () -> Unit,
  onBoost: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  activeDirection: CardSwipeDirection? = null,
  dragFraction: Float = 0f
) {
  val isPassActive = activeDirection == CardSwipeDirection.LEFT && dragFraction > 0.05f
  val isLikeActive = activeDirection == CardSwipeDirection.RIGHT && dragFraction > 0.05f
  val isSuperLikeActive = activeDirection == CardSwipeDirection.UP && dragFraction > 0.05f

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Rewind Button
    ActionButtonItem(
      icon = Icons.Default.Replay,
      tint = GoldVip,
      backgroundColor = Color.White,
      size = 46.dp,
      iconSize = 22.dp,
      contentDescription = "Rewind",
      testTag = "btn_rewind",
      enabled = enabled,
      onClick = onRewind
    )

    // Pass Button - dynamically reacts when dragging left
    val passDynamicScale = if (isPassActive) 1f + (0.18f * dragFraction) else 1f
    ActionButtonItem(
      icon = Icons.Default.Close,
      tint = NopeRed,
      backgroundColor = if (isPassActive) NopeRed.copy(alpha = 0.15f) else Color.White,
      size = 58.dp,
      iconSize = 30.dp,
      contentDescription = "Pass",
      testTag = "btn_pass",
      enabled = enabled,
      externalScale = passDynamicScale,
      isHighlighted = isPassActive,
      onClick = onPass
    )

    // Super Like Button - dynamically reacts when dragging up
    val superLikeDynamicScale = if (isSuperLikeActive) 1f + (0.22f * dragFraction) else 1f
    ActionButtonItem(
      icon = Icons.Default.Star,
      tint = SuperlikeBlue,
      backgroundColor = if (isSuperLikeActive) SuperlikeBlue.copy(alpha = 0.15f) else Color.White,
      size = 48.dp,
      iconSize = 24.dp,
      contentDescription = "Super Like",
      testTag = "btn_superlike",
      enabled = enabled,
      externalScale = superLikeDynamicScale,
      isHighlighted = isSuperLikeActive,
      onClick = onSuperLike
    )

    // Like Button - dynamically reacts when dragging right
    val likeDynamicScale = if (isLikeActive) 1f + (0.18f * dragFraction) else 1f
    ActionButtonItem(
      icon = Icons.Default.Favorite,
      tint = CoralPrimary,
      backgroundColor = if (isLikeActive) CoralPrimary.copy(alpha = 0.15f) else Color.White,
      size = 58.dp,
      iconSize = 30.dp,
      contentDescription = "Like",
      testTag = "btn_like",
      enabled = enabled,
      externalScale = likeDynamicScale,
      isHighlighted = isLikeActive,
      onClick = onLike
    )

    // Boost Button
    ActionButtonItem(
      icon = Icons.Default.ElectricBolt,
      tint = BoostPurple,
      backgroundColor = Color.White,
      size = 46.dp,
      iconSize = 22.dp,
      contentDescription = "Boost",
      testTag = "btn_boost",
      enabled = enabled,
      onClick = onBoost
    )
  }
}

@Composable
fun ActionButtonItem(
  icon: ImageVector,
  tint: Color,
  backgroundColor: Color,
  size: Dp,
  iconSize: Dp,
  contentDescription: String,
  testTag: String,
  enabled: Boolean,
  externalScale: Float = 1f,
  isHighlighted: Boolean = false,
  onClick: () -> Unit
) {
  val context = LocalContext.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val pressedScale by animateFloatAsState(
    targetValue = if (isPressed) 0.88f else 1f,
    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
    label = "action_btn_scale"
  )
  val animatedBgColor by animateColorAsState(
    targetValue = backgroundColor,
    animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
    label = "action_btn_bg"
  )
  val finalScale = pressedScale * externalScale

  Surface(
    modifier = Modifier
      .size(size)
      .graphicsLayer {
        scaleX = finalScale
        scaleY = finalScale
      }
      .testTag(testTag)
      .shadow(
        elevation = if (isHighlighted) 12.dp else 6.dp,
        shape = CircleShape,
        spotColor = if (isHighlighted) tint.copy(alpha = 0.65f) else tint.copy(alpha = 0.35f)
      )
      .clip(CircleShape)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = {
          HapticHelper.triggerHaptic(context, "threshold")
          onClick()
        }
      ),
    color = animatedBgColor,
    shape = CircleShape
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.size(size)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = if (enabled) tint else Color.LightGray,
        modifier = Modifier.size(iconSize)
      )
    }
  }
}
