package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.KatkatNotification
import com.example.data.model.KatkatNotificationType
import com.example.ui.theme.CoralPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
  notifications: List<KatkatNotification>,
  unreadCount: Int,
  onDismiss: () -> Unit,
  onMarkRead: (String) -> Unit,
  onMarkAllRead: () -> Unit,
  onDelete: (String) -> Unit,
  onClearAll: () -> Unit,
  onNavigateToTarget: (String) -> Unit = {},
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    modifier = Modifier
      .fillMaxHeight(0.85f)
      .testTag("notifications_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      // Sheet Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(CoralPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Notifications,
              contentDescription = null,
              tint = CoralPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Notifications",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.3).sp
            )
          )
          if (unreadCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(CoralPrimary)
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = "$unreadCount new",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("btn_close_notifications")
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Action Row: Mark All Read & Clear All
      if (notifications.isNotEmpty()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${notifications.size} total",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (unreadCount > 0) {
              TextButton(
                onClick = onMarkAllRead,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.testTag("btn_mark_all_read")
              ) {
                Icon(
                  imageVector = Icons.Default.DoneAll,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = CoralPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mark all read", fontSize = 12.sp, color = CoralPrimary, fontWeight = FontWeight.SemiBold)
              }
            }
            TextButton(
              onClick = onClearAll,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.testTag("btn_clear_all_notifications")
            ) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Clear all", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(10.dp))

      // Notifications List or Empty State
      if (notifications.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(36.dp)
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "No notifications yet",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Likes, new matches, and messages will appear right here in real time.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
              ),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 24.dp)
        ) {
          items(notifications, key = { it.id }) { notif ->
            NotificationItemCard(
              notification = notif,
              onItemClick = {
                onMarkRead(notif.id)
                notif.deepLinkTarget?.let { onNavigateToTarget(it) }
              },
              onDelete = { onDelete(notif.id) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun NotificationItemCard(
  notification: KatkatNotification,
  onItemClick: () -> Unit,
  onDelete: () -> Unit
) {
  val (icon, iconColor, bgGradient) = getNotificationVisuals(notification.type)

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onItemClick() }
      .testTag("notification_item_${notification.id}"),
    shape = RoundedCornerShape(16.dp),
    color = if (!notification.isRead) {
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    } else {
      MaterialTheme.colorScheme.surface
    },
    border = androidx.compose.foundation.BorderStroke(
      width = 1.dp,
      color = if (!notification.isRead) CoralPrimary.copy(alpha = 0.35f)
      else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left Visual (Avatar or Type Icon)
      Box(
        modifier = Modifier.size(46.dp),
        contentAlignment = Alignment.Center
      ) {
        if (!notification.senderAvatarUrl.isNullOrBlank()) {
          AsyncImage(
            model = notification.senderAvatarUrl,
            contentDescription = notification.senderProfileName ?: "Sender",
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape),
            contentScale = ContentScale.Crop
          )
        } else {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(bgGradient),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }
        }

        // Small indicator badge
        if (!notification.senderAvatarUrl.isNullOrBlank()) {
          Box(
            modifier = Modifier
              .size(18.dp)
              .align(Alignment.BottomEnd)
              .clip(CircleShape)
              .background(iconColor)
              .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(10.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Notification Content
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = notification.title,
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Text(
            text = formatRelativeTime(notification.timestamp),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
          text = notification.message,
          style = MaterialTheme.typography.bodySmall.copy(
            color = if (!notification.isRead) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (!notification.isRead) FontWeight.Medium else FontWeight.Normal
          ),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Delete action button
      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(28.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Delete,
          contentDescription = "Delete notification",
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

private fun getNotificationVisuals(type: KatkatNotificationType): Triple<ImageVector, Color, Brush> {
  return when (type) {
    KatkatNotificationType.NEW_MATCH -> Triple(
      Icons.Default.Favorite,
      Color(0xFFFF2A6D),
      Brush.linearGradient(listOf(Color(0xFFFF2A6D), Color(0xFFFF62A5)))
    )
    KatkatNotificationType.NEW_MESSAGE -> Triple(
      Icons.Default.ChatBubble,
      Color(0xFF7C4DFF),
      Brush.linearGradient(listOf(Color(0xFF7C4DFF), Color(0xFFB388FF)))
    )
    KatkatNotificationType.MESSAGE_READ -> Triple(
      Icons.Default.CheckCircle,
      Color(0xFF00BFA5),
      Brush.linearGradient(listOf(Color(0xFF00BFA5), Color(0xFF64FFDA)))
    )
    KatkatNotificationType.PROFILE_ACTIVITY -> Triple(
      Icons.Default.AutoAwesome,
      Color(0xFFFF9100),
      Brush.linearGradient(listOf(Color(0xFFFF9100), Color(0xFFFFD54F)))
    )
    KatkatNotificationType.SYSTEM_NOTIFICATION -> Triple(
      Icons.Default.Info,
      Color(0xFF2979FF),
      Brush.linearGradient(listOf(Color(0xFF2979FF), Color(0xFF82B1FF)))
    )
  }
}

private fun formatRelativeTime(timestamp: Long): String {
  val diff = System.currentTimeMillis() - timestamp
  val seconds = diff / 1000
  val minutes = seconds / 60
  val hours = minutes / 60
  val days = hours / 24

  return when {
    seconds < 60 -> "Just now"
    minutes < 60 -> "${minutes}m ago"
    hours < 24 -> "${hours}h ago"
    days < 7 -> "${days}d ago"
    else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
  }
}
