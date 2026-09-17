package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.PeachBlush
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/**
 * Greeting / Loading Screen shown on app opening.
 * Features an enchanting animated text motion for the app name "Katkat",
 * a pulsing glowing heart emblem, and smooth redirection to the Home page
 * once the user session is loaded.
 */
@Composable
fun GreetingLoadingScreen(
  isSessionLoaded: Boolean,
  isLoggedIn: Boolean,
  onRedirect: () -> Unit,
  modifier: Modifier = Modifier
) {
  var animationReady by remember { mutableStateOf(false) }
  var isExiting by remember { mutableStateOf(false) }

  // Exit alpha for cinematic transition to home or onboarding
  val screenAlpha = remember { Animatable(1f) }

  // Minimum greeting display duration so the user sees the text motion gracefully
  LaunchedEffect(Unit) {
    animationReady = true
  }

  // Handle redirection once session is loaded and animation has completed its initial arc
  LaunchedEffect(isSessionLoaded, animationReady) {
    if (animationReady && isSessionLoaded) {
      // Pleasant minimum duration for the motion to complete
      delay(1800)
      isExiting = true
      screenAlpha.animateTo(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
      )
      onRedirect()
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .alpha(screenAlpha.value)
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF14051E), // Twilight night
            Color(0xFF2B0A26), // Deep velvet orchid
            Color(0xFF16041A)  // Rich nocturnal warmth
          )
        )
      )
      .testTag("greeting_loading_screen"),
    contentAlignment = Alignment.Center
  ) {
    // Sparkling floating ambient romantic particles
    AmbientBokehStars()

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .statusBarsPadding()
        .padding(horizontal = 32.dp)
    ) {
      // 1. Radiant Glowing Heart Emblem with Breath Pulse
      PulsingHeartEmblem()

      Spacer(modifier = Modifier.height(28.dp))

      // 2. Animated Text Motion for "Katkat"
      KatkatAnimatedMotionText()

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Romantic Tagline Reveal
      var showTagline by remember { mutableStateOf(false) }
      LaunchedEffect(Unit) {
        delay(400)
        showTagline = true
      }

      AnimatedVisibility(
        visible = showTagline,
        enter = fadeIn(tween(600)) + slideInVertically(
          animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
          ),
          initialOffsetY = { 30 }
        )
      ) {
        Text(
          text = "Where genuine sparks ignite 💕",
          style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
          ),
          color = Color.White.copy(alpha = 0.9f),
          textAlign = TextAlign.Center
        )
      }

      Spacer(modifier = Modifier.height(36.dp))

      // 4. Delicate Loading Status Pill
      GreetingStatusIndicator(isLoggedIn = isLoggedIn)
    }
  }
}

/**
 * Animated Text Motion for the app name "Katkat":
 * - Staggered entrance for each letter (K-a-t-k-a-t)
 * - Individual vertical translation, scale bounce, and glowing alpha
 * - Followed by an ambient continuous harmonic sine wave floating motion
 */
@Composable
private fun KatkatAnimatedMotionText() {
  val letters = remember { listOf("K", "a", "t", "k", "a", "t") }

  // Infinite wave motion running across the word
  val infiniteTransition = rememberInfiniteTransition(label = "katkat_wave")
  val wavePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 6.28318f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wave_phase"
  )

  Row(
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
  ) {
    letters.forEachIndexed { index, letter ->
      val letterAnimProgress = remember { Animatable(0f) }

      LaunchedEffect(Unit) {
        delay(120L + (index * 95L))
        letterAnimProgress.animateTo(
          targetValue = 1f,
          animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
          )
        )
      }

      // Harmonic sine wave offset applied after entrance
      val waveOffset = if (letterAnimProgress.value > 0.8f) {
        (sin(wavePhase + (index * 0.85f)) * 5f).dp
      } else {
        0.dp
      }

      val entranceY = ((1f - letterAnimProgress.value) * 35f).dp
      val letterScale = 0.3f + (letterAnimProgress.value * 0.7f)
      val letterAlpha = letterAnimProgress.value.coerceIn(0f, 1f)

      Box(
        modifier = Modifier
          .offset(y = waveOffset + entranceY)
          .scale(letterScale)
          .alpha(letterAlpha)
          .padding(horizontal = 1.5.dp),
        contentAlignment = Alignment.Center
      ) {
        // Subtle ambient neon drop glow
        Text(
          text = letter,
          fontSize = 44.sp,
          fontWeight = FontWeight.ExtraBold,
          fontFamily = FontFamily.Serif,
          color = CoralPrimary.copy(alpha = 0.4f),
          modifier = Modifier.offset(y = 2.dp)
        )

        // Main Letter Character
        Text(
          text = letter,
          fontSize = 44.sp,
          fontWeight = FontWeight.ExtraBold,
          fontFamily = FontFamily.Serif,
          color = Color.White
        )
      }
    }
  }
}

/**
 * Pulsing Glowing Heart Emblem
 */
@Composable
private fun PulsingHeartEmblem() {
  val infiniteTransition = rememberInfiniteTransition(label = "heart_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  val auraAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.75f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "aura_alpha"
  )

  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier.size(90.dp)
  ) {
    // Outer Radiant Glow Ring
    Box(
      modifier = Modifier
        .size(86.dp)
        .scale(pulseScale * 1.06f)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(
              CoralPrimary.copy(alpha = auraAlpha * 0.6f),
              CoralDark.copy(alpha = auraAlpha * 0.2f),
              Color.Transparent
            )
          )
        )
    )

    // Core Heart Circle
    Box(
      modifier = Modifier
        .size(64.dp)
        .scale(pulseScale)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(CoralPrimary, CoralDark)
          )
        )
        .shadow(16.dp, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Katkat",
        tint = Color.White,
        modifier = Modifier.size(34.dp)
      )
    }
  }
}

/**
 * Delicate Loading Status Indicator with 3 animated bouncing dots
 */
@Composable
private fun GreetingStatusIndicator(isLoggedIn: Boolean) {
  val infiniteTransition = rememberInfiniteTransition(label = "loading_dots")
  val dotProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 3f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "dot_progress"
  )

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = Color.White.copy(alpha = 0.08f),
    modifier = Modifier.padding(top = 8.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      Text(
        text = if (isLoggedIn) "Connecting to your sparks" else "Starting Katkat",
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Medium,
          letterSpacing = 0.3.sp
        ),
        color = Color.White.copy(alpha = 0.85f)
      )

      Spacer(modifier = Modifier.size(8.dp))

      // 3 subtle bouncing loading dots
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 0..2) {
          val active = (dotProgress.toInt() % 3) == i
          Box(
            modifier = Modifier
              .size(5.dp)
              .clip(CircleShape)
              .background(
                if (active) CoralPrimary else Color.White.copy(alpha = 0.3f)
              )
          )
        }
      }
    }
  }
}

/**
 * Ambient background floating sparkles
 */
@Composable
private fun AmbientBokehStars() {
  val infiniteTransition = rememberInfiniteTransition(label = "bokeh_particles")
  val progress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 5000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "bokeh_progress"
  )

  val randomSeeds = remember {
    List(20) {
      Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 3.5f + 1.2f)
    }
  }

  Canvas(modifier = Modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height

    randomSeeds.forEachIndexed { i, (seedX, seedY, radius) ->
      val currentY = (seedY - progress * (0.28f + (i % 3) * 0.06f) + 1f) % 1f
      val currentX = (seedX + sin((progress * 6.28f) + i).toFloat() * 0.025f).coerceIn(0f, 1f)
      val alpha = (0.2f + 0.5f * sin(progress * 3.14f + i).toFloat().coerceIn(0f, 1f))

      drawCircle(
        color = if (i % 2 == 0) PeachBlush.copy(alpha = alpha) else Color(0xFFFFD1DC).copy(alpha = alpha * 0.7f),
        radius = radius * density,
        center = Offset(currentX * w, currentY * h)
      )
    }
  }
}
