package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DatingProfile
import com.example.data.model.UserProfile
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.PeachSecondary
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.WarmCream
import com.example.util.HapticHelper

@Composable
fun MatchCelebrationDialog(
  matchedProfile: DatingProfile,
  userProfile: UserProfile,
  onSendMessage: (String) -> Unit,
  onOpenChat: () -> Unit,
  onKeepSwiping: () -> Unit
) {
  val context = LocalContext.current
  var quickMessage by remember { mutableStateOf("") }

  LaunchedEffect(matchedProfile.id) {
    HapticHelper.triggerHaptic(context, "match")
  }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "heart_pulse"
  )

  Dialog(
    onDismissRequest = onKeepSwiping,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFF261820).copy(alpha = 0.95f),
              Color(0xFF3B1F2B).copy(alpha = 0.98f)
            )
          )
        )
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("match_celebration_dialog"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Close Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          IconButton(
            onClick = onKeepSwiping,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.2f))
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Text(
          text = "It's a Katkat Match! 🎉",
          style = MaterialTheme.typography.headlineLarge.copy(
            color = CoralPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp
          ),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "You and ${matchedProfile.name} liked each other! Say hello and start the spark ✨",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
          ),
          modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Overlapping Avatars connected by Heart
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
          contentAlignment = Alignment.Center
        ) {
          // Left: User Avatar
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(userProfile.photos.firstOrNull() ?: "")
              .crossfade(true)
              .build(),
            contentDescription = "My Photo",
            modifier = Modifier
              .offset(x = (-45).dp)
              .size(105.dp)
              .clip(CircleShape)
              .border(3.5.dp, Color.White, CircleShape)
              .shadow(8.dp, CircleShape),
            contentScale = ContentScale.Crop
          )

          // Right: Match Avatar
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(matchedProfile.photos.firstOrNull() ?: "")
              .crossfade(true)
              .build(),
            contentDescription = "${matchedProfile.name} Photo",
            modifier = Modifier
              .offset(x = 45.dp)
              .size(105.dp)
              .clip(CircleShape)
              .border(3.5.dp, CoralPrimary, CircleShape)
              .shadow(8.dp, CircleShape),
            contentScale = ContentScale.Crop
          )

          // Center glowing heart
          Box(
            modifier = Modifier
              .scale(pulseScale)
              .size(44.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  colors = listOf(CoralPrimary, PeachSecondary)
                )
              )
              .border(2.dp, Color.White, CircleShape)
              .shadow(10.dp, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = "Match Heart",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Message Input Bar
        OutlinedTextField(
          value = quickMessage,
          onValueChange = { quickMessage = it },
          placeholder = { Text("Say something charming...", color = Color.Gray) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("match_quick_message_input"),
          shape = RoundedCornerShape(28.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = CoralPrimary,
            unfocusedBorderColor = Color.LightGray,
            focusedTextColor = TextPrimaryDark,
            unfocusedTextColor = TextPrimaryDark
          ),
          trailingIcon = {
            IconButton(
              onClick = {
                if (quickMessage.isNotBlank()) {
                  onSendMessage(quickMessage)
                  onOpenChat()
                } else {
                  onOpenChat()
                }
              },
              modifier = Modifier
                .padding(end = 4.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(CoralPrimary)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }
          },
          singleLine = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Primary Action: Open Full Chat
        Button(
          onClick = onOpenChat,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("btn_open_chat_from_dialog"),
          shape = RoundedCornerShape(26.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Text(
            text = "Chat with ${matchedProfile.name}",
            style = MaterialTheme.typography.titleMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary Action: Keep Swiping
        TextButton(
          onClick = onKeepSwiping,
          modifier = Modifier.testTag("btn_keep_swiping")
        ) {
          Text(
            text = "Keep Swiping",
            style = MaterialTheme.typography.labelLarge.copy(
              color = Color.White.copy(alpha = 0.8f),
              fontWeight = FontWeight.Medium
            )
          )
        }
      }
    }
  }
}
