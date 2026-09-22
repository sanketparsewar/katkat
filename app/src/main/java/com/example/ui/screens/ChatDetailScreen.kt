package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ChatMessage
import com.example.data.model.DatingProfile
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.PeachSecondary
import com.example.ui.theme.SuperlikeBlue
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatDetailScreen(
  match: DatingProfile,
  messages: List<ChatMessage>,
  onSendMessage: (String, String?) -> Unit,
  onBack: () -> Unit,
  onInspectProfile: () -> Unit,
  isMatchTyping: Boolean = false,
  onClearChat: (() -> Unit)? = null,
  onUnmatch: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var inputText by remember { mutableStateOf("") }
  var showMenu by remember { mutableStateOf(false) }
  var showCallDialog by remember { mutableStateOf(false) }
  var showUnmatchConfirm by remember { mutableStateOf(false) }
  var showClearChatConfirm by remember { mutableStateOf(false) }

  val listState = rememberLazyListState()

  // Scroll to bottom when messages change or typing changes
  LaunchedEffect(messages.size, isMatchTyping) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  val icebreakers = remember(match.name) {
    listOf(
      "Coffee or boba on our first date? ☕",
      "Two truths and a lie! You first 😉",
      "What's your dream travel spot? 🌴",
      "Tell me your favorite song right now 🎵",
      "Drinks this Friday evening? 🍸"
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFAF7F5))
      .statusBarsPadding()
      .navigationBarsPadding()
      .imePadding()
      .testTag("chat_detail_screen")
  ) {
    // ── Top Bar ────────────────────────────────────────────────────────
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 3.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Back Button
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("btn_chat_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimaryDark
          )
        }

        // Match Avatar + Presence
        val avatarUrl = match.photos.firstOrNull { it.isNotBlank() }
        Box(
          modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onInspectProfile)
        ) {
          if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
              model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
              contentDescription = match.name,
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape),
              contentScale = ContentScale.Crop
            )
          } else {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E1A36)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = match.name.take(1).uppercase(),
                color = CoralPrimary,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Active indicator
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .size(12.dp)
              .clip(CircleShape)
              .background(Color(0xFF2EC4B6))
              .border(1.5.dp, Color.White, CircleShape)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Match Name & Presence / Typing state
        Column(
          modifier = Modifier
            .weight(1f)
            .clickable(onClick = onInspectProfile)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = match.name,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (match.isVerified) {
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified",
                tint = SuperlikeBlue,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          if (isMatchTyping) {
            Text(
              text = "typing...",
              style = MaterialTheme.typography.labelSmall.copy(
                color = CoralPrimary,
                fontWeight = FontWeight.Bold
              )
            )
          } else {
            Text(
              text = "Active now • ${match.location}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondaryDark,
                fontSize = 11.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Call button
        IconButton(
          onClick = { showCallDialog = true },
          modifier = Modifier.testTag("btn_chat_call")
        ) {
          Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = "Call",
            tint = CoralPrimary
          )
        }

        // Info / Profile
        IconButton(
          onClick = onInspectProfile,
          modifier = Modifier.testTag("btn_chat_info")
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Profile Details",
            tint = TextSecondaryDark
          )
        }

        // More options dropdown
        Box {
          IconButton(onClick = { showMenu = true }) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "More Options",
              tint = TextSecondaryDark
            )
          }

          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("View Full Profile") },
              leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
              onClick = {
                showMenu = false
                onInspectProfile()
              }
            )
            DropdownMenuItem(
              text = { Text("Clear Chat History") },
              leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
              onClick = {
                showMenu = false
                showClearChatConfirm = true
              }
            )
            DropdownMenuItem(
              text = { Text("Unmatch ${match.name}", color = Color(0xFFD32F2F)) },
              leadingIcon = { Icon(Icons.Default.PersonRemove, contentDescription = null, tint = Color(0xFFD32F2F)) },
              onClick = {
                showMenu = false
                showUnmatchConfirm = true
              }
            )
          }
        }
      }
    }

    // ── Messages List ──────────────────────────────────────────────────
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Date badge header
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFECE4DF)
          ) {
            Text(
              text = "Matched & Connected ✨",
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondaryDark,
                fontWeight = FontWeight.SemiBold
              ),
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
          }
        }
      }

      items(messages, key = { it.id }) { message ->
        ChatMessageBubble(
          message = message
        )
      }

      // Live typing indicator bubble
      if (isMatchTyping) {
        item {
          TypingIndicatorBubble(matchName = match.name)
        }
      }
    }

    // ── Quick Icebreakers & Prompts ────────────────────────────────────
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(icebreakers) { prompt ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PeachBlush)
            .clickable {
              onSendMessage(prompt, null)
            }
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Text(
            text = prompt,
            style = MaterialTheme.typography.labelSmall.copy(
              color = CoralPrimary,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }
    }

    // ── Input Bar (Text Only) ──────────────────────────────────────────
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 8.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = {
            Text(
              "Type a message...",
              color = Color.Gray,
              fontSize = 14.sp
            )
          },
          modifier = Modifier
            .weight(1f)
            .testTag("chat_input_text_field"),
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedBorderColor = CoralPrimary,
            unfocusedBorderColor = Color(0xFFE8DDD6),
            focusedTextColor = TextPrimaryDark,
            unfocusedTextColor = TextPrimaryDark
          ),
          maxLines = 4
        )

        Spacer(modifier = Modifier.width(8.dp))

        val canSend = inputText.isNotBlank()
        IconButton(
          onClick = {
            if (canSend) {
              onSendMessage(inputText, null)
              inputText = ""
            }
          },
          enabled = canSend,
          modifier = Modifier
            .size(46.dp)
            .testTag("btn_send_chat_message")
            .clip(CircleShape)
            .background(if (canSend) CoralPrimary else Color.LightGray)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send Message",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }

  // ── Call Dialog ──────────────────────────────────────────────────────
  if (showCallDialog) {
    AlertDialog(
      onDismissRequest = { showCallDialog = false },
      title = {
        Text("Connect with ${match.name}", fontWeight = FontWeight.Bold)
      },
      text = {
        Text("Spark a connection! Would you like to send a voice note or invite ${match.name} on a Coffee Date? ☕")
      },
      confirmButton = {
        Button(
          onClick = {
            showCallDialog = false
            onSendMessage("Hey ${match.name}! Would you be free to grab coffee or boba sometime this week? ☕✨", null)
          },
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Text("Invite to Coffee Date ☕")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCallDialog = false }) {
          Text("Cancel", color = TextSecondaryDark)
        }
      }
    )
  }

  // ── Clear Chat Confirmation Dialog ──────────────────────────────────
  if (showClearChatConfirm) {
    AlertDialog(
      onDismissRequest = { showClearChatConfirm = false },
      title = { Text("Clear Chat History?") },
      text = { Text("All messages with ${match.name} will be permanently removed from this conversation.") },
      confirmButton = {
        Button(
          onClick = {
            showClearChatConfirm = false
            onClearChat?.invoke()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
          Text("Clear")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearChatConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // ── Unmatch Confirmation Dialog ─────────────────────────────────────
  if (showUnmatchConfirm) {
    AlertDialog(
      onDismissRequest = { showUnmatchConfirm = false },
      title = { Text("Unmatch ${match.name}?") },
      text = { Text("They will no longer appear in your matches or conversations. You can always discover other people.") },
      confirmButton = {
        Button(
          onClick = {
            showUnmatchConfirm = false
            onUnmatch?.invoke()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
          Text("Unmatch")
        }
      },
      dismissButton = {
        TextButton(onClick = { showUnmatchConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun ChatMessageBubble(
  message: ChatMessage
) {
  val isMine = message.isFromMe

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
  ) {
    Surface(
      shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isMine) 18.dp else 4.dp,
        bottomEnd = if (isMine) 4.dp else 18.dp
      ),
      color = if (isMine) CoralPrimary else Color.White,
      shadowElevation = 1.5.dp,
      modifier = Modifier.widthIn(max = 280.dp)
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        // Text content
        if (message.text.isNotBlank()) {
          Text(
            text = message.text,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = if (isMine) Color.White else TextPrimaryDark,
              lineHeight = 20.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Time and read receipt
        Row(
          modifier = Modifier.align(Alignment.End),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = formatMessageTime(message.timestamp),
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (isMine) Color.White.copy(alpha = 0.75f) else TextSecondaryDark,
              fontSize = 10.sp
            )
          )
          if (isMine) {
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Delivered",
              tint = Color.White.copy(alpha = 0.85f),
              modifier = Modifier.size(12.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun TypingIndicatorBubble(matchName: String) {
  val infiniteTransition = rememberInfiniteTransition(label = "typing")
  val dot1Alpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot1"
  )
  val dot2Alpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, delayMillis = 200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot2"
  )
  val dot3Alpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, delayMillis = 400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot3"
  )

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Start
  ) {
    Surface(
      shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = 4.dp,
        bottomEnd = 18.dp
      ),
      color = Color.White,
      shadowElevation = 1.dp,
      modifier = Modifier.padding(vertical = 4.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${matchName.split(" ").first()} is typing",
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextSecondaryDark,
            fontSize = 11.sp
          )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(CoralPrimary.copy(alpha = dot1Alpha))
        )
        Spacer(modifier = Modifier.width(3.dp))
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(CoralPrimary.copy(alpha = dot2Alpha))
        )
        Spacer(modifier = Modifier.width(3.dp))
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(CoralPrimary.copy(alpha = dot3Alpha))
        )
      }
    }
  }
}

fun formatMessageTime(millis: Long): String {
  val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
  return sdf.format(Date(millis))
}
