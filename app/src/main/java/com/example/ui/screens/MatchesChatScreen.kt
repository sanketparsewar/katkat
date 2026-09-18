package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DatingProfile
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.PeachSecondary
import com.example.ui.theme.SuperlikeBlue
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun MatchesChatScreen(
  matches: List<DatingProfile>,
  onSelectMatch: (DatingProfile) -> Unit,
  onNavigateToDiscover: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("matches_chat_screen")
  ) {
    // Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      Text(
        text = "Matches & Chats",
        style = MaterialTheme.typography.headlineLarge.copy(
          fontWeight = FontWeight.ExtraBold,
          color = TextPrimaryDark
        )
      )
      Text(
        text = "Real-time messaging unlocks upon mutual like 💕",
        style = MaterialTheme.typography.bodySmall.copy(
          color = TextSecondaryDark
        )
      )
    }

    if (matches.isNotEmpty()) {
      LazyColumn(
        modifier = Modifier.fillMaxSize()
      ) {
        // Horizontal New Matches Carousel Section
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp)
          ) {
            Text(
              text = "NEW MATCHES (${matches.size})",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CoralPrimary,
                letterSpacing = 1.sp
              ),
              modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            LazyRow(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
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
            color = Color(0xFFF3E7DF),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
          )
        }

        // Messages List Header
        item {
          Text(
            text = "CONVERSATIONS",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = TextSecondaryDark,
              letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
          )
        }

        // Conversation items
        items(matches, key = { "conv_${it.id}" }) { match ->
          ConversationRowItem(
            profile = match,
            onClick = { onSelectMatch(match) }
          )
        }
      }
    } else {
      // Empty matches state
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
  ) {
    val photoUrl = profile.photos.firstOrNull { it.isNotBlank() }
      ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80"

    Box(
      modifier = Modifier
        .size(68.dp)
        .clip(CircleShape)
        .background(
          Brush.linearGradient(listOf(CoralPrimary, PeachSecondary))
        )
        .padding(2.5.dp),
      contentAlignment = Alignment.Center
    ) {
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(photoUrl)
          .crossfade(true)
          .build(),
        contentDescription = profile.name,
        modifier = Modifier
          .size(63.dp)
          .clip(CircleShape),
        contentScale = ContentScale.Crop
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = profile.name.split(" ").first(),
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.SemiBold,
        color = TextPrimaryDark
      ),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun ConversationRowItem(
  profile: DatingProfile,
  onClick: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("conversation_row_${profile.id}"),
    color = Color.Transparent
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar with Online dot
      val conversationAvatar = profile.photos.firstOrNull { it.isNotBlank() }
        ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80"

      Box(modifier = Modifier.size(56.dp)) {
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
        // Green active dot
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(14.dp)
            .clip(CircleShape)
            .background(Color(0xFF2EC4B6))
            .border(2.dp, Color.White, CircleShape)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      // Name & Last message teaser
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
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
              )
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
            text = "Just now",
            style = MaterialTheme.typography.labelSmall.copy(color = CoralPrimary)
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Hey! Loved your taste in music and photo...",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = TextSecondaryDark,
            fontSize = 13.sp
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
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
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(90.dp)
          .clip(CircleShape)
          .background(PeachBlush),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.ChatBubbleOutline,
          contentDescription = null,
          tint = CoralPrimary,
          modifier = Modifier.size(44.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "No Matches Yet",
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.ExtraBold,
          color = TextPrimaryDark
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "When you and someone both swipe right, you'll match and unlock real-time instant chat here!",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = TextSecondaryDark,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          lineHeight = 20.sp
        )
      )
    }
  }
}
