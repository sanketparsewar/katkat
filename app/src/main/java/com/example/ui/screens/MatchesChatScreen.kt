package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.DatingProfile
import com.example.data.model.MatchConversation
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.PeachSecondary
import com.example.ui.theme.SuperlikeBlue
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ConversationFilter {
  ALL, UNREAD, ONLINE
}

@Composable
fun MatchesChatScreen(
  matches: List<DatingProfile>,
  conversations: List<MatchConversation> = emptyList(),
  onSelectMatch: (DatingProfile) -> Unit,
  onNavigateToDiscover: () -> Unit,
  onCreateTestMatch: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf(ConversationFilter.ALL) }

  // Merge match list with conversation data fallback
  val effectiveConversations = remember(matches, conversations) {
    if (conversations.isNotEmpty()) {
      conversations
    } else {
      matches.map { match ->
        MatchConversation(
          matchProfile = match,
          matchTimeMillis = match.matchedTimestamp ?: System.currentTimeMillis(),
          lastMessage = "New match! Say hello 👋",
          lastMessageTimeMillis = match.matchedTimestamp ?: System.currentTimeMillis(),
          unreadCount = 0,
          isOnline = true
        )
      }
    }
  }

  // Filter conversations based on search and selected tab
  val filteredConversations = remember(effectiveConversations, searchQuery, selectedFilter) {
    effectiveConversations.filter { conv ->
      val matchesSearch = searchQuery.isBlank() ||
        conv.matchProfile.name.contains(searchQuery, ignoreCase = true) ||
        conv.lastMessage.contains(searchQuery, ignoreCase = true) ||
        conv.matchProfile.location.contains(searchQuery, ignoreCase = true)

      val matchesFilter = when (selectedFilter) {
        ConversationFilter.ALL -> true
        ConversationFilter.UNREAD -> conv.unreadCount > 0
        ConversationFilter.ONLINE -> conv.isOnline
      }
      matchesSearch && matchesFilter
    }
  }

  val totalUnread = remember(effectiveConversations) {
    effectiveConversations.sumOf { it.unreadCount }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("matches_chat_screen")
  ) {
    // ── Header ────────────────────────────────────────────────────────
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Matches & Chats",
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onBackground,
              fontSize = 28.sp
            )
          )
          if (totalUnread > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = CircleShape,
              color = CoralPrimary
            ) {
              Text(
                text = "$totalUnread",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }
        }
        Text(
          text = "Connect, spark conversations & set up real dates 💕",
          style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
      }
    }

    if (matches.isNotEmpty() || effectiveConversations.isNotEmpty()) {
      // ── Search Bar ──────────────────────────────────────────────────
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search matches or messages...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp)
          .testTag("matches_search_input"),
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
          focusedTextColor = MaterialTheme.colorScheme.onSurface,
          unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = true
      )

      // ── Filter Chips ────────────────────────────────────────────────
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = selectedFilter == ConversationFilter.ALL,
          onClick = { selectedFilter = ConversationFilter.ALL },
          label = { Text("All (${effectiveConversations.size})") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CoralPrimary,
            selectedLabelColor = Color.White
          ),
          shape = RoundedCornerShape(16.dp)
        )

        FilterChip(
          selected = selectedFilter == ConversationFilter.UNREAD,
          onClick = { selectedFilter = ConversationFilter.UNREAD },
          label = {
            Text(
              if (totalUnread > 0) "Unread ($totalUnread)" else "Unread"
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CoralPrimary,
            selectedLabelColor = Color.White
          ),
          shape = RoundedCornerShape(16.dp)
        )

        FilterChip(
          selected = selectedFilter == ConversationFilter.ONLINE,
          onClick = { selectedFilter = ConversationFilter.ONLINE },
          label = { Text("Online now") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CoralPrimary,
            selectedLabelColor = Color.White
          ),
          shape = RoundedCornerShape(16.dp)
        )
      }

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
      ) {
        // Horizontal New Matches Carousel Section (only if not searching)
        if (searchQuery.isBlank() && matches.isNotEmpty()) {
          item {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 6.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "NEW MATCHES (${matches.size})",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = CoralPrimary,
                    letterSpacing = 1.2.sp
                  )
                )
                Text(
                  text = "Mutual Likes",
                  style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
              }

              LazyRow(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
              ) {
                items(matches, key = { it.id }) { match ->
                  NewMatchAvatarItem(
                    profile = match,
                    onClick = { onSelectMatch(match) }
                  )
                }
              }
            }

            Divider(
              color = MaterialTheme.colorScheme.outlineVariant,
              thickness = 1.dp,
              modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
          }
        }

        // Conversations Header
        item {
          Text(
            text = "MESSAGES (${filteredConversations.size})",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
          )
        }

        // Conversations List
        if (filteredConversations.isNotEmpty()) {
          items(filteredConversations, key = { "conv_${it.matchProfile.id}" }) { conv ->
            ConversationRowItem(
              conversation = conv,
              onClick = { onSelectMatch(conv.matchProfile) }
            )
          }
        } else {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (searchQuery.isNotBlank()) "No conversations match '$searchQuery'" else "No messages in this filter",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
    } else {
      // Empty Matches State
      EmptyMatchesView(
        onNavigateToDiscover = onNavigateToDiscover,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      )
    }
  }
}

@Composable
fun NewMatchAvatarItem(
  profile: DatingProfile,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clickable(onClick = onClick)
      .testTag("match_avatar_${profile.id}")
      .width(72.dp)
  ) {
    val photoUrl = profile.photos.firstOrNull { it.isNotBlank() }

    Box(
      modifier = Modifier
        .size(70.dp)
        .clip(CircleShape)
        .background(
          Brush.sweepGradient(
            listOf(CoralPrimary, PeachSecondary, SuperlikeBlue, CoralPrimary)
          )
        )
        .padding(2.5.dp),
      contentAlignment = Alignment.Center
    ) {
      if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(photoUrl)
            .crossfade(true)
            .build(),
          contentDescription = profile.name,
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape),
          contentScale = ContentScale.Crop
        )
      } else {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(Color(0xFF2E1A36)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = profile.name.take(1).uppercase().ifBlank { "?" },
            style = MaterialTheme.typography.titleMedium,
            color = CoralPrimary,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Online green badge on avatar
      Box(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .size(15.dp)
          .clip(CircleShape)
          .background(Color(0xFF2EC4B6))
          .border(2.dp, Color.White, CircleShape)
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = profile.name.split(" ").first(),
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      ),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun ConversationRowItem(
  conversation: MatchConversation,
  onClick: () -> Unit
) {
  val profile = conversation.matchProfile
  val hasUnread = conversation.unreadCount > 0

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("conversation_row_${profile.id}"),
    color = if (hasUnread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar with Online dot
      val conversationAvatar = profile.photos.firstOrNull { it.isNotBlank() }

      Box(modifier = Modifier.size(56.dp)) {
        if (!conversationAvatar.isNullOrBlank()) {
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(conversationAvatar)
              .crossfade(true)
              .build(),
            contentDescription = profile.name,
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape),
            contentScale = ContentScale.Crop
          )
        } else {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(Color(0xFF2E1A36)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = profile.name.take(1).uppercase().ifBlank { "?" },
              style = MaterialTheme.typography.titleMedium,
              color = CoralPrimary,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Green active dot
        if (conversation.isOnline) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .size(14.dp)
              .clip(CircleShape)
              .background(Color(0xFF2EC4B6))
              .border(2.dp, Color.White, CircleShape)
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      // Name & Last message preview
      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = profile.name,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (hasUnread) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (profile.isVerified) {
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified",
                tint = SuperlikeBlue,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          Text(
            text = formatConversationTime(conversation.lastMessageTimeMillis),
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (hasUnread) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal
            )
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = conversation.lastMessage,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = if (hasUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
              fontSize = 13.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )

          if (hasUnread) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = CircleShape,
              color = CoralPrimary
            ) {
              Text(
                text = "${conversation.unreadCount}",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
              )
            }
          }
        }
      }
    }
  }
}

fun formatConversationTime(millis: Long): String {
  if (millis <= 0) return "Just now"
  val now = System.currentTimeMillis()
  val diff = now - millis
  return when {
    diff < 60_000L -> "Just now"
    diff < 3600_000L -> "${diff / 60_000L}m"
    diff < 86400_000L -> "${diff / 3600_000L}h"
    diff < 172800_000L -> "Yesterday"
    else -> {
      val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
      sdf.format(Date(millis))
    }
  }
}

@Composable
fun EmptyMatchesView(
  onNavigateToDiscover: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier.padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .size(96.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Favorite,
          contentDescription = "No matches yet",
          tint = CoralPrimary,
          modifier = Modifier.size(52.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "No matches yet",
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface,
          fontSize = 22.sp
        ),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "When you match with others on Discover, they'll appear here and conversations will begin. Start swiping to find new matches!",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 20.sp
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(28.dp))

      Button(
        onClick = onNavigateToDiscover,
        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
          .fillMaxWidth(0.75f)
          .height(48.dp)
          .testTag("btn_empty_start_swiping")
      ) {
        Icon(
          imageVector = Icons.Default.Favorite,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Start Swiping", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
    }
  }
}
