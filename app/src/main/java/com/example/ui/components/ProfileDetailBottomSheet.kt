package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DatingProfile
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.NopeRed
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.SuperlikeBlue
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileDetailBottomSheet(
  profile: DatingProfile,
  onLike: () -> Unit,
  onPass: () -> Unit,
  onSuperLike: () -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var currentPhotoIndex by remember { mutableIntStateOf(0) }
  val photos = profile.photos.filter { it.isNotBlank() }.ifEmpty {
    listOf("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80")
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.background,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(bottom = 24.dp)
        .testTag("profile_detail_bottom_sheet")
    ) {
      // Photo Carousel
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(380.dp)
      ) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(photos.getOrElse(currentPhotoIndex) { photos.first() })
            .crossfade(true)
            .build(),
          contentDescription = "${profile.name} photo",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )

        // Dismiss button
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .padding(16.dp)
            .align(Alignment.TopEnd)
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }

        // Photo switcher dots
        if (photos.size > 1) {
          Row(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 12.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.Black.copy(alpha = 0.4f))
              .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            photos.indices.forEach { index ->
              Box(
                modifier = Modifier
                  .size(if (index == currentPhotoIndex) 8.dp else 6.dp)
                  .clip(CircleShape)
                  .background(
                    if (index == currentPhotoIndex) CoralPrimary else Color.White.copy(alpha = 0.6f)
                  )
              )
            }
          }
        }
      }

      // Details Content
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 16.dp)
      ) {
        // Name, Age & Verified
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = profile.name,
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = TextPrimaryDark
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${profile.age}",
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.Light,
              color = TextSecondaryDark
            )
          )
          if (profile.isVerified) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = "Verified",
              tint = SuperlikeBlue,
              modifier = Modifier.size(22.dp)
            )
          }
        }

        // Occupation & Education
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Work, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "${profile.occupation} ${if (profile.company.isNotBlank()) "at ${profile.company}" else ""}", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryDark))
        }

        if (profile.education.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.School, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = profile.education, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark))
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = profile.location, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bio section
        if (profile.bio.isNotBlank()) {
          Text(
            text = "About Me",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = profile.bio,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryDark, lineHeight = 22.sp)
          )
          Spacer(modifier = Modifier.height(16.dp))
        }

        // Prompt Card (e.g. Two truths and a lie / Quickest way to my heart)
        if (profile.promptQuestion != null && profile.promptAnswer != null) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PeachBlush)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = profile.promptQuestion,
                style = MaterialTheme.typography.labelLarge.copy(color = CoralPrimary, fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = profile.promptAnswer,
                style = MaterialTheme.typography.titleMedium.copy(
                  color = TextPrimaryDark,
                  fontWeight = FontWeight.Medium,
                  lineHeight = 22.sp
                )
              )
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }

        // Passions / Interests tags
        if (profile.passions.isNotEmpty()) {
          Text(
            text = "Passions & Interests",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark)
          )
          Spacer(modifier = Modifier.height(8.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            profile.passions.forEach { tag ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .background(Color.White)
                  .border(1.dp, Color(0xFFF0E5DF), RoundedCornerShape(16.dp))
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(
                  text = tag,
                  style = MaterialTheme.typography.labelMedium.copy(
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.SemiBold
                  )
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }

        // Basics badges
        Text(
          text = "Lifestyle & Basics",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          LifestyleBadge(text = profile.zodiac)
          LifestyleBadge(text = profile.height)
          LifestyleBadge(text = profile.datingIntention)
          LifestyleBadge(text = profile.pets)
          LifestyleBadge(text = profile.drinking)
        }

        // Spotify Anthem
        if (profile.anthemSong != null && profile.anthemArtist != null) {
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "My Anthem 🎵",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1DB954).copy(alpha = 0.1f),
            border = BorderStroke(1.dp, Color(0xFF1DB954).copy(alpha = 0.3f))
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF1DB954)),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Anthem", tint = Color.White)
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = profile.anthemSong,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                )
                Text(
                  text = profile.anthemArtist,
                  style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Bottom Action Row (Pass, Superlike, Like)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = {
              onPass()
              onDismiss()
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NopeRed)
          ) {
            Text("Pass ✖", fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = {
              onSuperLike()
              onDismiss()
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SuperlikeBlue)
          ) {
            Text("Super Like ⭐", color = Color.White, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = {
              onLike()
              onDismiss()
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
          ) {
            Text("Like ❤️", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun LifestyleBadge(text: String) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(14.dp))
      .background(PeachBlush)
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium.copy(
        color = TextPrimaryDark,
        fontWeight = FontWeight.Medium
      )
    )
  }
}
