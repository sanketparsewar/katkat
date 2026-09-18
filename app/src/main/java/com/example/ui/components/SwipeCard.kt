package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DatingProfile
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.LikeGreen
import com.example.ui.theme.NopeRed
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.SuperlikeBlue
import com.example.util.HapticHelper
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
  programmaticSwipe: CardSwipeDirection? = null
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
  var currentPhotoIndex by remember { mutableIntStateOf(0) }
  var lastThresholdZone by remember { mutableStateOf<String?>(null) }
  var isDragging by remember { mutableStateOf(false) }
  val photos = profile.photos.filter { it.isNotBlank() }.ifEmpty {
    listOf("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80")
  }

  val density = LocalDensity.current
  val swipeThresholdPx = with(density) { 130.dp.toPx() }
  val superlikeThresholdPx = with(density) { 150.dp.toPx() }

  // Spring Specs for custom physics animations
  val likeSpringSpec = spring<Offset>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow
  )
  val passSpringSpec = spring<Offset>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow
  )
  val superlikeSpringSpec = spring<Offset>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
  )
  val snapBackSpringSpec = spring<Offset>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
  )

  // Handle programmatic button triggers with the same custom spring animation
  LaunchedEffect(programmaticSwipe) {
    when (programmaticSwipe) {
      CardSwipeDirection.RIGHT -> {
        offset.animateTo(
          Offset(1500f, 120f),
          animationSpec = likeSpringSpec
        )
        onSwipedRight()
      }
      CardSwipeDirection.LEFT -> {
        offset.animateTo(
          Offset(-1500f, 120f),
          animationSpec = passSpringSpec
        )
        onSwipedLeft()
      }
      CardSwipeDirection.UP -> {
        offset.animateTo(
          Offset(0f, -1700f),
          animationSpec = superlikeSpringSpec
        )
        onSuperLiked()
      }
      null -> Unit
    }
  }

  // Tactile lift scaling when card is actively picked up / dragged
  val cardLiftScale by animateFloatAsState(
    targetValue = if (isDragging) 1.028f else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "cardLiftScale"
  )

  // Dynamic rotation based on X drag (max ~20 degrees for playful tilt)
  val rotationDegrees = (offset.value.x / 18f).coerceIn(-20f, 20f)

  // Alpha and animated scale values for overlay stamps
  val likeAlpha = (offset.value.x / swipeThresholdPx).coerceIn(0f, 1f)
  val nopeAlpha = (-offset.value.x / swipeThresholdPx).coerceIn(0f, 1f)
  val superlikeAlpha = (-offset.value.y / superlikeThresholdPx).coerceIn(0f, 1f)
  val stampScale = (0.88f + (likeAlpha.coerceAtLeast(nopeAlpha).coerceAtLeast(superlikeAlpha)) * 0.22f).coerceIn(0.88f, 1.15f)

  Card(
    modifier = modifier
      .fillMaxSize()
      .testTag("swipe_card_${profile.id}")
      .offset { IntOffset(offset.value.x.roundToInt(), offset.value.y.roundToInt()) }
      .rotate(if (isTopCard) rotationDegrees else 0f)
      .scale(if (isTopCard) cardLiftScale else 1f)
      .shadow(
        elevation = if (isDragging) 22.dp else if (isTopCard) 12.dp else 4.dp,
        shape = RoundedCornerShape(26.dp)
      )
      .clip(RoundedCornerShape(26.dp))
      .then(
        if (isTopCard) {
          Modifier.pointerInput(Unit) {
            detectDragGestures(
              onDragStart = {
                isDragging = true
              },
              onDragEnd = {
                isDragging = false
                lastThresholdZone = null
                coroutineScope.launch {
                  when {
                    offset.value.x > swipeThresholdPx -> {
                      offset.animateTo(
                        Offset(1500f, offset.value.y * 0.5f),
                        animationSpec = likeSpringSpec
                      )
                      onSwipedRight()
                    }
                    offset.value.x < -swipeThresholdPx -> {
                      offset.animateTo(
                        Offset(-1500f, offset.value.y * 0.5f),
                        animationSpec = passSpringSpec
                      )
                      onSwipedLeft()
                    }
                    offset.value.y < -superlikeThresholdPx -> {
                      offset.animateTo(
                        Offset(offset.value.x * 0.3f, -1700f),
                        animationSpec = superlikeSpringSpec
                      )
                      onSuperLiked()
                    }
                    else -> {
                      offset.animateTo(
                        Offset.Zero,
                        animationSpec = snapBackSpringSpec
                      )
                    }
                  }
                }
              },
              onDragCancel = {
                isDragging = false
                lastThresholdZone = null
                coroutineScope.launch {
                  offset.animateTo(
                    Offset.Zero,
                    animationSpec = snapBackSpringSpec
                  )
                }
              },
              onDrag = { change, dragAmount ->
                change.consume()
                val targetOffset = offset.value + dragAmount
                val currentZone = when {
                  targetOffset.x > swipeThresholdPx -> "like"
                  targetOffset.x < -swipeThresholdPx -> "pass"
                  targetOffset.y < -superlikeThresholdPx -> "superlike"
                  else -> null
                }
                // Subtle tactile tick when crossing into a commit zone
                if (currentZone != null && currentZone != lastThresholdZone) {
                  HapticHelper.triggerHaptic(context, "threshold")
                }
                lastThresholdZone = currentZone

                coroutineScope.launch {
                  offset.snapTo(targetOffset)
                }
              }
            )
          }
        } else Modifier
      ),
    shape = RoundedCornerShape(26.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Main Photo
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(photos.getOrElse(currentPhotoIndex) { photos.first() })
          .crossfade(true)
          .build(),
        contentDescription = "${profile.name} photo",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )

      // Tap navigation zones for photos (Left 35% -> Prev, Right 65% -> Next)
      Box(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(photos.size) {
            detectTapGestures { tapOffset ->
              if (tapOffset.x < size.width * 0.35f) {
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
      )

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

      // ── Swipe Stamp Overlays ──────────────────────────────────────────

      // LIKE Stamp (Top-Left, Green/Coral border)
      if (likeAlpha > 0.05f) {
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 24.dp, top = 40.dp)
            .rotate(-15f)
            .scale(stampScale)
            .alpha(likeAlpha)
            .border(3.dp, LikeGreen, RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          Text(
            text = "LIKE",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = LikeGreen,
              letterSpacing = 2.sp
            )
          )
        }
      }

      // NOPE Stamp (Top-Right, Red border)
      if (nopeAlpha > 0.05f) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(end = 24.dp, top = 40.dp)
            .rotate(15f)
            .scale(stampScale)
            .alpha(nopeAlpha)
            .border(3.dp, NopeRed, RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          Text(
            text = "NOPE",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = NopeRed,
              letterSpacing = 2.sp
            )
          )
        }
      }

      // SUPER LIKE Stamp (Bottom-Center, Blue border)
      if (superlikeAlpha > 0.1f) {
        Box(
          modifier = Modifier
            .align(Alignment.Center)
            .scale(stampScale)
            .alpha(superlikeAlpha)
            .border(3.dp, SuperlikeBlue, RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
          Text(
            text = "SUPER LIKE ⭐",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = SuperlikeBlue,
              letterSpacing = 2.sp
            )
          )
        }
      }
    }
  }
}
