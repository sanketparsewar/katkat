package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.PeachSecondary
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun LikesYouScreen(
  likedProfiles: List<DatingProfile>,
  subscriptionState: SubscriptionState,
  onOpenPaywall: () -> Unit,
  onInstantMatch: (DatingProfile) -> Unit,
  onInspectProfile: (DatingProfile) -> Unit,
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
              color = TextPrimaryDark
            )
          )
          Text(
            text = "${likedProfiles.size} people have swiped right on you",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
          )
        }

        if (isLocked) {
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

    if (isLocked) {
      // Locked Banner
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 4.dp),
        color = PeachBlush,
        shape = RoundedCornerShape(16.dp)
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
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            )
            Text(
              text = "Unblur photos and match instantly with anyone who liked you!",
              style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark, fontSize = 12.sp)
            )
          }
        }
      }
    }

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
          }
        )
      }
    }
  }
}

@Composable
fun LikedProfileGridCard(
  profile: DatingProfile,
  isLocked: Boolean,
  onCardClick: () -> Unit,
  onInstantMatch: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .height(230.dp)
      .clip(RoundedCornerShape(20.dp))
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
                Color.Black.copy(alpha = if (isLocked) 0.5f else 0.8f)
              ),
              startY = 200f
            )
          )
      )

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

      // Bottom Info & Match action
      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(12.dp)
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
          Button(
            onClick = onInstantMatch,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Match", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
