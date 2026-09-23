package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DatingProfile
import com.example.data.model.SubscriptionState
import com.example.data.model.SubscriptionTier
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.ui.theme.NopeRed
import com.example.ui.theme.SuperlikeBlue

@Composable
fun LikesYouScreen(
  likedProfiles: List<DatingProfile>,
  subscriptionState: SubscriptionState,
  onOpenPaywall: () -> Unit,
  onInstantMatch: (DatingProfile) -> Unit,
  onPassProfile: (DatingProfile) -> Unit,
  onInspectProfile: (DatingProfile) -> Unit,
  onNavigateToDiscover: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isLocked = subscriptionState.currentTier == SubscriptionTier.FREE

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("likes_you_screen")
  ) {
    // Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Likes You",
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onBackground
            )
          )
          Text(
            text = if (likedProfiles.isEmpty()) "No pending likes right now" else "${likedProfiles.size} people liked your profile",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
          )
        }

        if (isLocked && likedProfiles.isNotEmpty()) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .background(GoldVip)
              .clickable(onClick = onOpenPaywall)
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Stars, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("SEE ALL", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
          }
        }
      }
    }

    if (isLocked && likedProfiles.isNotEmpty()) {
      // Locked Banner
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(CoralPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Upgrade to Katkat Plus or VIP",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            )
            Text(
              text = "Unblur photos and match instantly with anyone who liked you!",
              style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            )
          }
        }
      }
    }

    if (likedProfiles.isEmpty()) {
      // Empty state: "No new likes"
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .verticalScroll(rememberScrollState())
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(100.dp)
              .clip(CircleShape)
              .background(CoralPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = null,
              tint = CoralPrimary,
              modifier = Modifier.size(48.dp)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = "No New Likes",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "When someone swipes right on your profile, they will appear here. Keep swiping or boost your profile to get noticed faster!",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
          )

          Spacer(modifier = Modifier.height(24.dp))

          Button(
            onClick = onNavigateToDiscover,
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
            modifier = Modifier.testTag("btn_discover_from_likes")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Explore, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Discover Profiles", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    } else {
      // Grid of Liked Profiles
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(likedProfiles, key = { "liked_${it.id}" }) { profile ->
          LikedProfileGridCard(
            profile = profile,
            isLocked = isLocked,
            onCardClick = {
              if (isLocked) onOpenPaywall() else onInspectProfile(profile)
            },
            onInstantMatch = {
              if (isLocked) onOpenPaywall() else onInstantMatch(profile)
            },
            onPass = {
              if (isLocked) onOpenPaywall() else onPassProfile(profile)
            }
          )
        }
      }
    }
  }
}

@Composable
fun LikedProfileGridCard(
  profile: DatingProfile,
  isLocked: Boolean,
  onCardClick: () -> Unit,
  onInstantMatch: () -> Unit,
  onPass: () -> Unit
) {
  val isSuperLike = profile.isSuperLikedByMe || profile.anthemSong?.contains("Super", ignoreCase = true) == true || profile.id == "profile_maya_1"

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .height(250.dp)
      .clip(RoundedCornerShape(20.dp))
      .then(
        if (isSuperLike) Modifier.border(2.dp, SuperlikeBlue, RoundedCornerShape(20.dp))
        else Modifier
      )
      .clickable(onClick = onCardClick)
      .testTag("liked_card_${profile.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    val cardPhoto = profile.photos.firstOrNull { it.isNotBlank() }

    Box(modifier = Modifier.fillMaxSize()) {
      // Photo (Blurred if locked) or Clean Monogram
      if (!cardPhoto.isNullOrBlank()) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(cardPhoto)
            .crossfade(true)
            .build(),
          contentDescription = profile.name,
          modifier = Modifier
            .fillMaxSize()
            .then(if (isLocked) Modifier.blur(18.dp) else Modifier),
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
          Text(
            text = profile.name.take(1).uppercase().ifBlank { "?" },
            style = MaterialTheme.typography.headlineMedium,
            color = CoralPrimary,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Gradient overlay
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = if (isLocked) 0.5f else 0.85f)
              ),
              startY = 140f
            )
          )
      )

      // Super Like Top Badge
      if (isSuperLike) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SuperlikeBlue)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = "Super Liked",
              tint = Color.White,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "SUPER LIKE",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }
        }
      }

      if (isLocked) {
        // Center Lock badge
        Box(
          modifier = Modifier
            .align(Alignment.Center)
            .size(46.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      // Bottom Info & Actions
      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(10.dp)
      ) {
        Text(
          text = if (isLocked) "${profile.name.take(3)}..." else "${profile.name}, ${profile.age}",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        )

        if (!isLocked) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Pass button
            OutlinedButton(
              onClick = onPass,
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
              modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .testTag("btn_pass_like_${profile.id}")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Pass",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text("Pass", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // Match button
            Button(
              onClick = onInstantMatch,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .testTag("btn_match_like_${profile.id}")
            ) {
              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Match",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text("Match", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

