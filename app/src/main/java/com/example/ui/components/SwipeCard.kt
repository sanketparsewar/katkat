package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DatingProfile
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.ui.theme.LikeGreen
import com.example.ui.theme.NopeRed
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.SuperlikeBlue
import com.example.util.HapticHelper
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

enum class CardSwipeDirection {
  LEFT, RIGHT, UP
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwipeCard(
  profile: DatingProfile,
  onSwipedLeft: () -> Unit,
  onSwipedRight: () -> Unit,
  onSuperLiked: () -> Unit,
  onInspectProfile: () -> Unit,
  modifier: Modifier = Modifier,
  isTopCard: Boolean = true,
  programmaticSwipe: CardSwipeDirection? = null,
  onDragProgress: (fraction: Float, direction: CardSwipeDirection?) -> Unit = { _, _ -> }
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val offset = remember(profile.id) { Animatable(Offset.Zero, Offset.VectorConverter) }
  var currentPhotoIndex by remember(profile.id) { mutableIntStateOf(0) }
  var lastThresholdZone by remember(profile.id) { mutableStateOf<String?>(null) }
  var isDragging by remember(profile.id) { mutableStateOf(false) }
  val photos = profile.photos.filter { it.isNotBlank() }

  val density = LocalDensity.current
  val swipeThresholdPx = with(density) { 120.dp.toPx() }
  val superlikeThresholdPx = with(density) { 140.dp.toPx() }

  // Ensure offset and state are always centered whenever profile changes
  LaunchedEffect(profile.id) {
    offset.snapTo(Offset.Zero)
    isDragging = false
    lastThresholdZone = null
    currentPhotoIndex = 0
  }

  // Butter-smooth exit fling animation without oscillation offscreen
  val exitSpringSpec = spring<Offset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium
  )

  // Natural elastic snap-back spring with realistic physical settle
  val snapBackSpringSpec = spring<Offset>(
    dampingRatio = 0.8f,
    stiffness = Spring.StiffnessMedium
  )

  // Continuously report drag progress to the deck & action bar
  LaunchedEffect(offset.value, isTopCard) {
    if (isTopCard) {
      val absX = kotlin.math.abs(offset.value.x)
      val curY = offset.value.y
      val direction: CardSwipeDirection?
      val fraction: Float
      if (absX > 6f || curY < -6f) {
        if (absX >= kotlin.math.abs(curY) || curY >= 0f) {
          direction = if (offset.value.x > 0f) CardSwipeDirection.RIGHT else CardSwipeDirection.LEFT
          fraction = (absX / swipeThresholdPx).coerceIn(0f, 1f)
        } else {
          direction = CardSwipeDirection.UP
          fraction = (-curY / superlikeThresholdPx).coerceIn(0f, 1f)
        }
      } else {
        direction = null
        fraction = 0f
      }
      onDragProgress(fraction, direction)
    }
  }

  // Handle programmatic button triggers with haptic confirmation and smooth exit physics
  LaunchedEffect(programmaticSwipe, profile.id) {
    val dir = programmaticSwipe ?: return@LaunchedEffect
    try {
      when (dir) {
        CardSwipeDirection.RIGHT -> {
          HapticHelper.triggerHaptic(context, "swipe_like")
          offset.animateTo(
            Offset(2200f, 100f),
            animationSpec = exitSpringSpec
          )
          onSwipedRight()
        }
        CardSwipeDirection.LEFT -> {
          HapticHelper.triggerHaptic(context, "swipe_pass")
          offset.animateTo(
            Offset(-2200f, 100f),
            animationSpec = exitSpringSpec
          )
          onSwipedLeft()
        }
        CardSwipeDirection.UP -> {
          HapticHelper.triggerHaptic(context, "swipe_superlike")
          offset.animateTo(
            Offset(0f, -2400f),
            animationSpec = exitSpringSpec
          )
          onSuperLiked()
        }
      }
    } catch (_: Exception) {
      when (dir) {
        CardSwipeDirection.RIGHT -> onSwipedRight()
        CardSwipeDirection.LEFT -> onSwipedLeft()
        CardSwipeDirection.UP -> onSuperLiked()
      }
    }
  }

  // Tactile lift scaling when card is actively picked up / dragged
  val cardLiftScale by animateFloatAsState(
    targetValue = if (isDragging) 1.025f else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "cardLiftScale"
  )

  Card(
    modifier = modifier
      .fillMaxSize()
      .testTag("swipe_card_${profile.id}")
      .graphicsLayer {
        translationX = offset.value.x
        translationY = offset.value.y
        // Anchored pivot slightly below center for realistic handheld card physics
        transformOrigin = TransformOrigin(0.5f, 0.85f)
        rotationZ = if (isTopCard) (offset.value.x / 18f).coerceIn(-24f, 24f) else 0f
        scaleX = if (isTopCard) cardLiftScale else 1f
        scaleY = if (isTopCard) cardLiftScale else 1f
        cameraDistance = 14f * density.density
        shadowElevation = if (isDragging) 16.dp.toPx() else if (isTopCard) 8.dp.toPx() else 3.dp.toPx()
        shape = RoundedCornerShape(26.dp)
        clip = true
      }
      .then(
        if (isTopCard) {
          Modifier.pointerInput(profile.id) {
            coroutineScope {
              val velocityTracker = VelocityTracker()
              val touchSlop = viewConfiguration.touchSlop

              awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                var dragStarted = false
                var totalDrag = Offset.Zero
                var lastHapticZone: String? = null

                velocityTracker.resetTracking()
                velocityTracker.addPosition(down.uptimeMillis, down.position)

                while (true) {
                  val event = awaitPointerEvent()
                  val change = event.changes.firstOrNull { it.id == down.id } ?: break

                  if (!change.pressed) {
                    // Pointer released (Up)
                    if (dragStarted) {
                      isDragging = false
                      val velocity = velocityTracker.calculateVelocity()
                      val vx = velocity.x
                      val vy = velocity.y
                      val curX = offset.value.x
                      val curY = offset.value.y
                      val absX = kotlin.math.abs(curX)
                      val absY = kotlin.math.abs(curY)

                      // Determine gesture intent: horizontal (Like/Pass) vs vertical upward (Super Like)
                      val isHorizontal = absX >= absY || curY >= 0f

                      when {
                        // Like: Swiped past threshold OR flicked right with sufficient momentum
                        isHorizontal && (curX > swipeThresholdPx || (vx > 500f && curX > 30f)) -> {
                          HapticHelper.triggerHaptic(context, "swipe_like")
                          launch {
                            try {
                              offset.animateTo(
                                Offset(2200f, curY + vy * 0.08f),
                                animationSpec = exitSpringSpec
                              )
                            } finally {
                              onSwipedRight()
                            }
                          }
                        }
                        // Pass: Swiped past negative threshold OR flicked left with sufficient momentum
                        isHorizontal && (curX < -swipeThresholdPx || (vx < -500f && curX < -30f)) -> {
                          HapticHelper.triggerHaptic(context, "swipe_pass")
                          launch {
                            try {
                              offset.animateTo(
                                Offset(-2200f, curY + vy * 0.08f),
                                animationSpec = exitSpringSpec
                              )
                            } finally {
                              onSwipedLeft()
                            }
                          }
                        }
                        // Super Like: Swiped upward past threshold OR flicked up with sufficient momentum
                        !isHorizontal && (curY < -superlikeThresholdPx || (vy < -500f && curY < -30f)) -> {
                          HapticHelper.triggerHaptic(context, "swipe_superlike")
                          launch {
                            try {
                              offset.animateTo(
                                Offset(curX * 0.3f, -2400f),
                                animationSpec = exitSpringSpec
                              )
                            } finally {
                              onSuperLiked()
                            }
                          }
                        }
                        // Snap back gracefully to center
                        else -> {
                          launch {
                            offset.animateTo(Offset.Zero, animationSpec = snapBackSpringSpec)
                            onDragProgress(0f, null)
                          }
                        }
                      }
                    } else {
                      // Tap without drag: Photo navigation or profile inspection
                      val tapY = change.position.y
                      val tapX = change.position.x
                      // If tap was in bottom 22% info area: inspect profile
                      if (tapY > size.height * 0.78f) {
                        onInspectProfile()
                      } else if (photos.size > 1) {
                        // In upper photo area: left = prev, right = next
                        if (tapX < size.width * 0.35f) {
                          if (currentPhotoIndex > 0) {
                            currentPhotoIndex--
                            HapticHelper.triggerHaptic(context, "threshold")
                          }
                        } else {
                          if (currentPhotoIndex < photos.size - 1) {
                            currentPhotoIndex++
                            HapticHelper.triggerHaptic(context, "threshold")
                          }
                        }
                      }
                    }
                    break
                  }

                  // Pointer moved
                  val dragDelta = change.positionChange()
                  totalDrag += dragDelta
                  velocityTracker.addPosition(change.uptimeMillis, change.position)

                  if (!dragStarted) {
                    if (totalDrag.getDistance() > touchSlop) {
                      dragStarted = true
                      isDragging = true
                      change.consume()
                      HapticHelper.triggerHaptic(context, "threshold")
                    }
                  } else {
                    change.consume()
                    val newOffset = offset.value + dragDelta
                    coroutineScope.launch {
                      offset.snapTo(newOffset)
                    }

                    val absX = kotlin.math.abs(newOffset.x)
                    val curY = newOffset.y
                    val dir: CardSwipeDirection?
                    val fraction: Float
                    if (absX >= kotlin.math.abs(curY) || curY >= 0f) {
                      dir = if (newOffset.x > 0f) CardSwipeDirection.RIGHT else CardSwipeDirection.LEFT
                      fraction = (absX / swipeThresholdPx).coerceIn(0f, 1f)
                    } else {
                      dir = CardSwipeDirection.UP
                      fraction = (-curY / superlikeThresholdPx).coerceIn(0f, 1f)
                    }
                    onDragProgress(fraction, dir)

                    val currentZone = when {
                      newOffset.x > swipeThresholdPx -> "like"
                      newOffset.x < -swipeThresholdPx -> "pass"
                      newOffset.y < -superlikeThresholdPx -> "superlike"
                      else -> null
                    }
                    if (currentZone != lastHapticZone) {
                      if (currentZone != null) HapticHelper.triggerHaptic(context, "threshold")
                      lastHapticZone = currentZone
                    }
                  }
                }
              }
            }
          }
        } else Modifier
      ),
    shape = RoundedCornerShape(26.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Main Photo or Clean Monogram Placeholder
      if (photos.isNotEmpty()) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(photos.getOrElse(currentPhotoIndex) { photos.first() })
            .crossfade(true)
            .build(),
          contentDescription = "${profile.name} photo",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      } else {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                listOf(Color(0xFF2E1A36), Color(0xFF16091D))
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
              modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(CoralPrimary.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = profile.name.take(1).uppercase().ifBlank { "?" },
                style = MaterialTheme.typography.displayMedium,
                color = CoralPrimary,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Top gradient and Photo Indicator bars
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
              startY = 0f,
              endY = 220f
            )
          )
          .padding(horizontal = 14.dp, vertical = 12.dp)
      ) {
        // Multi-photo indicator pills
        if (photos.size > 1) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            photos.indices.forEach { index ->
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(3.5.dp)
                  .clip(CircleShape)
                  .background(
                    if (index == currentPhotoIndex) Color.White else Color.White.copy(alpha = 0.4f)
                  )
              )
            }
          }
        }
      }

      // Top-Right VIP Badge for VIP Subscribed Profiles
      if (profile.isVip) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = if (photos.size > 1) 22.dp else 12.dp, end = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              Brush.horizontalGradient(
                listOf(GoldVip, Color(0xFFFFB300), Color(0xFFFF8F00))
              )
            )
            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.5.dp)
            .testTag("vip_badge_top_right_${profile.id}"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Text(
              text = "👑",
              fontSize = 11.sp
            )
            Text(
              text = "VIP",
              color = Color.Black,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 10.5.sp,
              letterSpacing = 0.5.sp
            )
          }
        }
      }

      // Bottom Gradient Scrim & Info Section
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.4f),
                Color.Black.copy(alpha = 0.85f),
                Color.Black.copy(alpha = 0.95f)
              )
            )
          )
          .padding(start = 18.dp, end = 18.dp, bottom = 18.dp, top = 40.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Name, Age, Verified badge & Info button
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = profile.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "${profile.age}",
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontWeight = FontWeight.Light,
                  color = Color.White.copy(alpha = 0.9f)
                )
              )
              if (profile.isVerified) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Default.Verified,
                  contentDescription = "Verified Profile",
                  tint = SuperlikeBlue,
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            // Info (ℹ) button to inspect full profile details
            IconButton(
              onClick = onInspectProfile,
              modifier = Modifier
                .testTag("btn_inspect_${profile.id}")
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f))
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "View Details",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          // Occupation & Location
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = profile.occupation,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = Color.White.copy(alpha = 0.9f),
              fontWeight = FontWeight.Medium
            )
          )

          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = CoralPrimary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = profile.location,
              style = MaterialTheme.typography.labelMedium.copy(
                color = Color.White.copy(alpha = 0.8f)
              )
            )
          }

          // Bio teaser
          if (profile.bio.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = profile.bio,
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 16.sp
              ),
              maxLines = 2
            )
          }

          // Top Passions chips teaser
          if (profile.passions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
              maxItemsInEachRow = 3
            ) {
              profile.passions.take(3).forEach { passion ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = passion,
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = Color.White,
                      fontWeight = FontWeight.SemiBold
                    )
                  )
                }
              }
            }
          }
        }
      }

      // ── Tactile Ambient Aura Overlays ──
      Box(
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer {
            val a = (offset.value.x / swipeThresholdPx).coerceIn(0f, 1f)
            alpha = a * 0.28f
          }
          .background(
            Brush.horizontalGradient(
              colors = listOf(Color.Transparent, LikeGreen)
            )
          )
      )

      Box(
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer {
            val a = (-offset.value.x / swipeThresholdPx).coerceIn(0f, 1f)
            alpha = a * 0.28f
          }
          .background(
            Brush.horizontalGradient(
              colors = listOf(NopeRed, Color.Transparent)
            )
          )
      )

      Box(
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer {
            val a = (-offset.value.y / superlikeThresholdPx).coerceIn(0f, 1f)
            alpha = a * 0.32f
          }
          .background(
            Brush.verticalGradient(
              colors = listOf(Color.Transparent, SuperlikeBlue)
            )
          )
      )

      // ── Swipe Stamp Overlays ──

      // LIKE Stamp (Top-Left)
      Surface(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(start = 24.dp, top = 40.dp)
          .graphicsLayer {
            val a = (offset.value.x / swipeThresholdPx).coerceIn(0f, 1f)
            alpha = if (a > 0.04f) a else 0f
            rotationZ = -14f
            val scale = (0.75f + a * 0.35f).coerceIn(0.75f, 1.15f)
            scaleX = scale
            scaleY = scale
          },
        shape = RoundedCornerShape(14.dp),
        color = Color.Black.copy(alpha = 0.5f),
        border = BorderStroke(3.5.dp, LikeGreen),
        shadowElevation = 8.dp
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "LIKE",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Black,
              color = LikeGreen,
              letterSpacing = 2.5.sp
            )
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "💚", fontSize = 19.sp)
        }
      }

      // NOPE Stamp (Top-Right)
      Surface(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(end = 24.dp, top = 40.dp)
          .graphicsLayer {
            val a = (-offset.value.x / swipeThresholdPx).coerceIn(0f, 1f)
            alpha = if (a > 0.04f) a else 0f
            rotationZ = 14f
            val scale = (0.75f + a * 0.35f).coerceIn(0.75f, 1.15f)
            scaleX = scale
            scaleY = scale
          },
        shape = RoundedCornerShape(14.dp),
        color = Color.Black.copy(alpha = 0.5f),
        border = BorderStroke(3.5.dp, NopeRed),
        shadowElevation = 8.dp
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "❌", fontSize = 18.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "NOPE",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Black,
              color = NopeRed,
              letterSpacing = 2.5.sp
            )
          )
        }
      }

      // SUPER LIKE Stamp (Center)
      Surface(
        modifier = Modifier
          .align(Alignment.Center)
          .graphicsLayer {
            val a = (-offset.value.y / superlikeThresholdPx).coerceIn(0f, 1f)
            alpha = if (a > 0.06f) a else 0f
            val scale = (0.75f + a * 0.35f).coerceIn(0.75f, 1.15f)
            scaleX = scale
            scaleY = scale
          },
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.6f),
        border = BorderStroke(3.5.dp, SuperlikeBlue),
        shadowElevation = 12.dp
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "⭐", fontSize = 22.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "SUPER LIKE",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Black,
              color = SuperlikeBlue,
              letterSpacing = 2.sp
            )
          )
        }
      }
    }
  }
}
