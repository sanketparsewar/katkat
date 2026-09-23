package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ChatMessage
import com.example.data.model.DatingProfile
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.PeachSecondary
import com.example.ui.theme.SuperlikeBlue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ChatDetailScreen(
  match: DatingProfile,
  messages: List<ChatMessage>,
  onSendMessage: (String, String?) -> Unit,
  onRetryMessage: ((String) -> Unit)? = null,
  onBack: () -> Unit,
  onInspectProfile: () -> Unit,
  isMatchTyping: Boolean = false,
  onClearChat: (() -> Unit)? = null,
  onUnmatch: (() -> Unit)? = null,
  onBlockProfile: (() -> Unit)? = null,
  onReportProfile: ((String, String, Boolean) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var inputText by remember { mutableStateOf("") }
  var showMenu by remember { mutableStateOf(false) }
  var showCallDialog by remember { mutableStateOf(false) }
  var showUnmatchConfirm by remember { mutableStateOf(false) }
  var showClearChatConfirm by remember { mutableStateOf(false) }
  var showBlockConfirm by remember { mutableStateOf(false) }
  var showReportDialog by remember { mutableStateOf(false) }

  val listState = rememberLazyListState()
  val avatarUrl = match.photos.firstOrNull { it.isNotBlank() }

  // Scroll to bottom when new messages arrive or match is typing
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
      .background(MaterialTheme.colorScheme.background)
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
            tint = MaterialTheme.colorScheme.onSurface
          )
        }

        // Match Avatar + Online status
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
              .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
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
                color = MaterialTheme.colorScheme.onSurface
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Call / Date Invite button
        IconButton(
          onClick = { showCallDialog = true },
          modifier = Modifier.testTag("btn_chat_call")
        ) {
          Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = "Invite to Date",
            tint = CoralPrimary
          )
        }

        // Options dropdown menu
        Box {
          IconButton(onClick = { showMenu = true }) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "More Options",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
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
            DropdownMenuItem(
              text = { Text("Block ${match.name}", color = Color(0xFFD32F2F)) },
              leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFD32F2F)) },
              onClick = {
                showMenu = false
                showBlockConfirm = true
              }
            )
            DropdownMenuItem(
              text = { Text("Report ${match.name}", color = Color(0xFFE65100)) },
              leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFFE65100)) },
              onClick = {
                showMenu = false
                showReportDialog = true
              }
            )
          }
        }
      }
    }

    // ── Messages List / Empty Chat State ───────────────────────────────
    if (messages.isEmpty()) {
      // Empty Chat State with Match Celebration Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(20.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(90.dp)
              .clip(CircleShape)
              .border(2.dp, CoralPrimary, CircleShape)
          ) {
            if (!avatarUrl.isNullOrBlank()) {
              AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                  .data(avatarUrl)
                  .crossfade(true)
                  .build(),
                contentDescription = match.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            } else {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(CoralPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = match.name.take(1),
                  style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CoralPrimary
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "You & ${match.name} Matched! ✨",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            ),
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Send a first message or tap one of the icebreakers below to get things started.",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
          )
        }
      }
    } else {
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Matched badge header
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
              Text(
                text = "Matched & Connected ✨",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
              )
            }
          }
        }

        // Messages with Date Separators
        var lastDayKey = ""
        messages.forEachIndexed { index, message ->
          val currentDayKey = formatMessageDayHeader(message.timestamp)
          if (currentDayKey != lastDayKey) {
            lastDayKey = currentDayKey
            item(key = "day_header_$index") {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                  Text(
                    text = currentDayKey,
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }

          item(key = message.id) {
            ChatMessageBubble(
              message = message,
              onRetry = { onRetryMessage?.invoke(message.id) }
            )
          }
        }

        // Live typing indicator bubble
        if (isMatchTyping) {
          item(key = "typing_bubble") {
            TypingIndicatorBubble(matchName = match.name)
          }
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
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

    // ── Input Bar ──────────────────────────────────────────────────────
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
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              fontSize = 14.sp
            )
          },
          modifier = Modifier
            .weight(1f)
            .testTag("chat_input_text_field"),
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = CoralPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
            .background(if (canSend) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
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

  // ── Call / Coffee Date Dialog ────────────────────────────────────────
  if (showCallDialog) {
    AlertDialog(
      onDismissRequest = { showCallDialog = false },
      title = {
        Text("Connect with ${match.name}", fontWeight = FontWeight.Bold)
      },
      text = {
        Text("Spark a connection! Would you like to invite ${match.name} on a Coffee Date? ☕")
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
          Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    )
  }

  // ── Clear Chat Confirmation Dialog ──────────────────────────────────
  if (showClearChatConfirm) {
    AlertDialog(
      onDismissRequest = { showClearChatConfirm = false },
      title = { Text("Clear Chat History?") },
      text = { Text("All messages in this conversation with ${match.name} will be permanently removed locally.") },
      confirmButton = {
        Button(
          onClick = {
            showClearChatConfirm = false
            onClearChat?.invoke()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
          Text("Clear Chat")
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
      text = { Text("They will no longer appear in your matches or conversations. You can always discover new profiles.") },
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

  // ── Block Confirmation Dialog ───────────────────────────────────────
  if (showBlockConfirm) {
    AlertDialog(
      onDismissRequest = { showBlockConfirm = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Block,
            contentDescription = null,
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Block ${match.name}?", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "When you block ${match.name}:",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text("• Remove from Discover feed", style = MaterialTheme.typography.bodySmall)
          Text("• Prevent likes between you", style = MaterialTheme.typography.bodySmall)
          Text("• Prevent messaging and chat history", style = MaterialTheme.typography.bodySmall)
          Text("• Prevent future matching", style = MaterialTheme.typography.bodySmall)
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "This action takes effect immediately across the platform.",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showBlockConfirm = false
            onBlockProfile?.invoke()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
          modifier = Modifier.testTag("btn_confirm_block_profile_chat")
        ) {
          Text("Block Profile", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showBlockConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // ── Report User Dialog ──────────────────────────────────────────────
  if (showReportDialog) {
    val reportReasons = listOf(
      "Fake profile",
      "Spam",
      "Harassment",
      "Inappropriate content",
      "Scam",
      "Other"
    )
    var selectedReason by remember { mutableStateOf(reportReasons.first()) }
    var reportDetails by remember { mutableStateOf("") }
    var alsoBlock by remember { mutableStateOf(true) }

    AlertDialog(
      onDismissRequest = { showReportDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Flag,
            contentDescription = null,
            tint = Color(0xFFE65100),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Report ${match.name}", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            "Select a reason for reporting:",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))
          reportReasons.forEach { reason ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { selectedReason = reason }
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .testTag("report_reason_$reason")
            ) {
              RadioButton(
                selected = selectedReason == reason,
                onClick = { selectedReason = reason },
                colors = RadioButtonDefaults.colors(selectedColor = CoralPrimary)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selectedReason == reason) FontWeight.Bold else FontWeight.Normal)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = reportDetails,
            onValueChange = { reportDetails = it },
            placeholder = { Text("Provide details (optional)...", fontSize = 12.sp) },
            label = { Text("Details", fontSize = 12.sp) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("report_details_input"),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CoralPrimary
            )
          )

          Spacer(modifier = Modifier.height(10.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .clickable { alsoBlock = !alsoBlock }
              .padding(vertical = 4.dp)
              .testTag("report_also_block_checkbox")
          ) {
            Checkbox(
              checked = alsoBlock,
              onCheckedChange = { alsoBlock = it },
              colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD32F2F))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text("Also block ${match.name}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
              Text("Hide from Discover & prevent messaging", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showReportDialog = false
            onReportProfile?.invoke(selectedReason, reportDetails, alsoBlock)
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
          modifier = Modifier.testTag("btn_submit_report_chat")
        ) {
          Text("Submit Report", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showReportDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun ChatMessageBubble(
  message: ChatMessage,
  onRetry: () -> Unit = {}
) {
  val isMine = message.isFromMe

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
  ) {
    Column(
      horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
      modifier = Modifier.widthIn(max = 280.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(
          topStart = 18.dp,
          topEnd = 18.dp,
          bottomStart = if (isMine) 18.dp else 4.dp,
          bottomEnd = if (isMine) 4.dp else 18.dp
        ),
        color = if (isMine) {
          if (message.isFailed) Color(0xFFD32F2F) else CoralPrimary
        } else {
          MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (isMine) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        shadowElevation = 1.5.dp
      ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
          // Message text
          if (message.text.isNotBlank()) {
            Text(
              text = message.text,
              style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(3.dp))

          // Timestamp, Sending state, Read receipts
          Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = formatMessageTime(message.timestamp),
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (isMine) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
              )
            )

            if (isMine) {
              Spacer(modifier = Modifier.width(4.dp))
              when {
                message.isSending -> {
                  CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    strokeWidth = 1.5.dp
                  )
                }
                message.isFailed -> {
                  Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Failed",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                  )
                }
                message.isRead -> {
                  Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Read",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                  )
                }
                else -> {
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

      // If message failed to send, display retry affordance
      if (isMine && message.isFailed) {
        Spacer(modifier = Modifier.height(2.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clickable(onClick = onRetry)
            .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Retry",
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "Failed to send • Tap to retry",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color(0xFFD32F2F),
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          )
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
      color = MaterialTheme.colorScheme.surfaceVariant,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

fun formatMessageDayHeader(millis: Long): String {
  val msgCal = Calendar.getInstance().apply { timeInMillis = millis }
  val nowCal = Calendar.getInstance()

  return when {
    msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
      msgCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) -> "Today"
    msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
      msgCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) - 1 -> "Yesterday"
    else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
  }
}
