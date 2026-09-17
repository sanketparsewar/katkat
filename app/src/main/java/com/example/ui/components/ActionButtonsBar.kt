package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BoostPurple
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.ui.theme.LikeGreen
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
  enabled: Boolean = true
) {
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

    // Pass Button
    ActionButtonItem(
      icon = Icons.Default.Close,
      tint = NopeRed,
      backgroundColor = Color.White,
      size = 58.dp,
      iconSize = 30.dp,
      contentDescription = "Pass",
      testTag = "btn_pass",
      enabled = enabled,
      onClick = onPass
    )

    // Super Like Button
    ActionButtonItem(
      icon = Icons.Default.Star,
      tint = SuperlikeBlue,
      backgroundColor = Color.White,
      size = 48.dp,
      iconSize = 24.dp,
      contentDescription = "Super Like",
      testTag = "btn_superlike",
      enabled = enabled,
      onClick = onSuperLike
    )

    // Like Button
    ActionButtonItem(
      icon = Icons.Default.Favorite,
      tint = CoralPrimary,
      backgroundColor = Color.White,
      size = 58.dp,
      iconSize = 30.dp,
      contentDescription = "Like",
      testTag = "btn_like",
      enabled = enabled,
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
  onClick: () -> Unit
) {
  val context = LocalContext.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.88f else 1f,
    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
    label = "action_btn_scale"
  )

  Surface(
    modifier = Modifier
      .size(size)
      .scale(scale)
      .testTag(testTag)
      .shadow(elevation = 6.dp, shape = CircleShape, spotColor = tint.copy(alpha = 0.35f))
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
    color = backgroundColor,
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
