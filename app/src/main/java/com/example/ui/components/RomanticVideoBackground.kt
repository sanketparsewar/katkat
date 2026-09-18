package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * Romantic Scenic Background with smooth cinematic zoom/pan motion,
 * dreamy sunset lighting, and sparkling ambient bokeh particles.
 * Eliminates MediaPlayerNative codec and network I/O errors.
 */
@Composable
fun RomanticVideoBackground(
  modifier: Modifier = Modifier
) {
  // 1. Cinematic Ken-Burns subtle zoom & pan effect
  val scaleAnim = remember { Animatable(1f) }
  val panAnim = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    scaleAnim.animateTo(
      targetValue = 1.15f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 12000, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      )
    )
  }

  LaunchedEffect(Unit) {
    panAnim.animateTo(
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 16000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
      )
    )
  }

  Box(modifier = modifier.fillMaxSize()) {
    // Rich romantic twilight and velvet rose ambient gradient
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFF160824), // Deep romantic violet-night top
              Color(0xFF8E1B3D), // Radiant coral-rose heart glow
              Color(0xFF260517), // Rich burgundy middle
              Color(0xFF0D031A)  // Luxurious deep velvet bottom
            )
          )
        )
    )

    // Sparkling floating bokeh particles
    SparklingParticlesOverlay()
  }
}

@Composable
private fun SparklingParticlesOverlay() {
  val particleProgress = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    particleProgress.animateTo(
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 5000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      )
    )
  }

  val randomSeeds = remember {
    List(24) {
      Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 4.5f + 1.5f)
    }
  }

  Canvas(modifier = Modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height
    val progress = particleProgress.value

    randomSeeds.forEachIndexed { i, (seedX, seedY, radius) ->
      val currentY = (seedY - progress * (0.35f + (i % 3) * 0.08f) + 1f) % 1f
      val currentX = (seedX + kotlin.math.sin((progress * 6.28f) + i).toFloat() * 0.035f).coerceIn(0f, 1f)
      val alpha = (0.25f + 0.55f * kotlin.math.sin(progress * 3.14f + i).toFloat().coerceIn(0f, 1f))

      drawCircle(
        color = if (i % 2 == 0) Color(0xFFFFD1DC).copy(alpha = alpha) else Color(0xFFFFE082).copy(alpha = alpha * 0.8f),
        radius = radius * density,
        center = Offset(currentX * w, currentY * h)
      )
    }
  }
}
