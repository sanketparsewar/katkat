package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SubscriptionState
import com.example.data.model.UserProfile
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CoralPrimary
import java.io.File
import java.io.FileOutputStream

// Custom interest model with icon and pastel color palette matching the UI design
data class InterestItem(
  val name: String,
  val iconEmoji: String,
  val backgroundColor: Color,
  val textColor: Color
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
  userProfile: UserProfile,
  subscriptionState: SubscriptionState,
  themeMode: AppThemeMode = AppThemeMode.LIGHT,
  likesCount: Int = 128,
  matchesCount: Int = 36,
  chatsCount: Int = 12,
  onThemeModeChange: (AppThemeMode) -> Unit = {},
  onSaveProfile: (UserProfile) -> Unit,
  onAddPhoto: (String) -> Unit,
  onRemovePhoto: (Int) -> Unit,
  onSetPrimaryPhoto: (Int) -> Unit = {},
  onReplacePhoto: (Int, String) -> Unit = { _, _ -> },
  onOpenPaywall: () -> Unit = {},
  onRestartOnboarding: () -> Unit = {},
  onDisableAccount: (Boolean) -> Unit = {},
  onDeleteAccount: () -> Unit = {},
  onLogout: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Sheet dialog states
  var showEditBioSheet by remember { mutableStateOf(false) }
  var showAboutMeSheet by remember { mutableStateOf(false) }
  var showInterestsSheet by remember { mutableStateOf(false) }
  var showPhotoManagementSheet by remember { mutableStateOf(false) }
  var showPreferencesSheet by remember { mutableStateOf(false) }
  var showSettingsSheet by remember { mutableStateOf(false) }
  var showNotificationsDialog by remember { mutableStateOf(false) }
  var showPhotoChoiceDialog by remember { mutableStateOf(false) }

  // Media pickers
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let {
      onAddPhoto(it.toString())
      Toast.makeText(context, "Photo added ✨", Toast.LENGTH_SHORT).show()
    }
  }

  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    bitmap?.let {
      val savedUri = saveBitmapToInternalStorage(context, it)
      if (savedUri != null) {
        onAddPhoto(savedUri.toString())
        Toast.makeText(context, "Photo captured ✨", Toast.LENGTH_SHORT).show()
      }
    }
  }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (isGranted) {
      cameraLauncher.launch()
    } else {
      Toast.makeText(context, "Camera permission needed", Toast.LENGTH_SHORT).show()
    }
  }

  // Predefined interests with pastel styling
  val allPresetInterests = listOf(
    InterestItem("Travel", "✈️", Color(0xFFFFEBEE), Color(0xFFD32F2F)),
    InterestItem("Food", "🍴", Color(0xFFFFF3E0), Color(0xFFE65100)),
    InterestItem("Music", "🎵", Color(0xFFEDE7F6), Color(0xFF5E35B1)),
    InterestItem("Fitness", "🏋️", Color(0xFFFCE4EC), Color(0xFFC2185B)),
    InterestItem("Books", "📖", Color(0xFFE3F2FD), Color(0xFF1976D2)),
    InterestItem("Photography", "📷", Color(0xFFFFEBEE), Color(0xFFE91E63)),
    InterestItem("Nature", "🍃", Color(0xFFE8F5E9), Color(0xFF2E7D32)),
    InterestItem("Dogs", "🐾", Color(0xFFF3E5F5), Color(0xFF7B1FA2)),
    InterestItem("Coffee", "☕", Color(0xFFE0F7FA), Color(0xFF00838F)),
    InterestItem("Art & Design", "🎨", Color(0xFFFFF8E1), Color(0xFFF57F17)),
    InterestItem("Gaming", "🎮", Color(0xFFEDE7F6), Color(0xFF673AB7)),
    InterestItem("Yoga", "🧘", Color(0xFFF1F8E9), Color(0xFF558B2F))
  )

  // Primary avatar image URL or fallback
  val avatarUrl = userProfile.photos.firstOrNull { it.isNotBlank() }
    ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop&q=80"

  val displayName = userProfile.name.ifBlank { "Aanya Sharma" }
  val displayAge = if (userProfile.age > 0) userProfile.age else 24
  val displayLocation = userProfile.hometown.ifBlank {
    userProfile.currentLocationCity.ifBlank { "Bangalore, India" }
  }
  val displayBio = userProfile.bio.ifBlank {
    "Good conversations, spontaneous plans and kind people make life better ✨ Here for meaningful connections (and maybe a little adventure)."
  }

  // Effective user interests
  val displayedInterests = remember(userProfile.passions) {
    if (userProfile.passions.isNotEmpty()) {
      userProfile.passions.map { passionName ->
        allPresetInterests.find { it.name.equals(passionName, ignoreCase = true) }
          ?: InterestItem(
            name = passionName,
            iconEmoji = "✨",
            backgroundColor = Color(0xFFFFEBEE),
            textColor = Color(0xFFE53950)
          )
      }
    } else {
      allPresetInterests.take(9)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFBF8F8))
      .verticalScroll(rememberScrollState())
      .testTag("profile_edit_screen")
  ) {
    // ── 1. Top Bar Header (Brand Name & Action Icons) ───────────────────
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Katkat",
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          fontStyle = FontStyle.Italic,
          color = Color(0xFFE53950),
          letterSpacing = (-0.5).sp
        )
        Text(
          text = "Better People. Brighter Connections.",
          fontSize = 11.5.sp,
          color = Color(0xFF555555),
          fontWeight = FontWeight.Normal,
          modifier = Modifier.padding(top = 1.dp)
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = { showSettingsSheet = true },
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = "Settings",
            tint = Color(0xFF222222),
            modifier = Modifier.size(24.dp)
          )
        }

        Box(
          modifier = Modifier.size(40.dp),
          contentAlignment = Alignment.Center
        ) {
          IconButton(
            onClick = { showNotificationsDialog = true },
            modifier = Modifier.fillMaxSize()
          ) {
            Icon(
              imageVector = Icons.Outlined.Notifications,
              contentDescription = "Notifications",
              tint = Color(0xFF222222),
              modifier = Modifier.size(26.dp)
            )
          }
          // Notification Pink Dot Badge
          Box(
            modifier = Modifier
              .size(8.dp)
              .align(Alignment.TopEnd)
              .offset(x = (-8).dp, y = 8.dp)
              .clip(CircleShape)
              .background(Color(0xFFE53950))
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // ── 2. Top Profile Info (Avatar + Name/Location/Bio) ─────────────────
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalAlignment = Alignment.Top
    ) {
      // Large Circular Avatar with Camera overlay
      Box(
        modifier = Modifier
          .size(116.dp)
          .align(Alignment.Top)
      ) {
        Box(
          modifier = Modifier
            .size(116.dp)
            .clip(CircleShape)
            .border(2.dp, Color.White, CircleShape)
            .shadow(2.dp, CircleShape)
        ) {
          AsyncImage(
            model = coil.request.ImageRequest.Builder(LocalContext.current)
              .data(avatarUrl)
              .crossfade(true)
              .build(),
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }

        // Camera Action Button
        Box(
          modifier = Modifier
            .size(34.dp)
            .align(Alignment.BottomEnd)
            .clip(CircleShape)
            .background(Color(0xFFE53950))
            .border(2.dp, Color.White, CircleShape)
            .clickable { showPhotoChoiceDialog = true },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = "Change avatar",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Profile Identity and Bio
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(start = 16.dp)
      ) {
        // Name & Age Row
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "$displayName  $displayAge",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1E)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Edit Profile",
            tint = Color(0xFF757575),
            modifier = Modifier
              .size(16.dp)
              .clickable { showEditBioSheet = true }
          )
        }

        // Location Row
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = Color(0xFF757575),
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = displayLocation,
            fontSize = 12.5.sp,
            color = Color(0xFF555555),
            fontWeight = FontWeight.Medium
          )
        }

        // Bio Text
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clickable { showEditBioSheet = true }
        ) {
          Column {
            Text(
              text = displayBio,
              fontSize = 12.sp,
              lineHeight = 16.5.sp,
              color = Color(0xFF333333)
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit Bio",
                tint = Color(0xFF888888),
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // ── 3. Horizontal Stats Row (Likes, Matches, Chats, Profile Views) ──
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Likes
      StatCounterItem(
        icon = Icons.Outlined.FavoriteBorder,
        iconTint = Color(0xFFE53950),
        label = "Likes",
        value = "${likesCount.coerceAtLeast(128)}"
      )

      StatDivider()

      // 2. Matches
      StatCounterItem(
        icon = Icons.Outlined.Group,
        iconTint = Color(0xFFE53950),
        label = "Matches",
        value = "${matchesCount.coerceAtLeast(36)}"
      )

      StatDivider()

      // 3. Chats
      StatCounterItem(
        icon = Icons.Outlined.ChatBubbleOutline,
        iconTint = Color(0xFFE53950),
        label = "Chats",
        value = "${chatsCount.coerceAtLeast(12)}"
      )

      StatDivider()

      // 4. Profile Views
      StatCounterItem(
        icon = Icons.Outlined.Visibility,
        iconTint = Color(0xFF222222),
        label = "Profile Views",
        value = "314"
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // ── 4. Upgrade to Heartly Plus Banner ────────────────────────────────
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 4.dp)
        .clickable { onOpenPaywall() },
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFFE53950)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.WorkspacePremium,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 12.dp)
        ) {
          Text(
            text = "Upgrade to Katkat Plus",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE53950)
          )
          Text(
            text = "See who liked you, get more matches and more control.",
            fontSize = 11.5.sp,
            color = Color(0xFF555555),
            modifier = Modifier.padding(top = 1.dp)
          )
        }

        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
          contentDescription = null,
          tint = Color(0xFF333333),
          modifier = Modifier.size(14.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // ── 5. My Photos Section ───────────────────────────────────────────
    val validUserPhotos = userProfile.photos.filter { it.isNotBlank() }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
      border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "My Photos",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1E)
          )
          Text(
            text = "Edit",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE53950),
            modifier = Modifier
              .clickable { showPhotoManagementSheet = true }
              .padding(4.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Photos LazyRow
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
        ) {
          // Display user photos
          itemsIndexed(validUserPhotos) { index, photoUrl ->
            Box(
              modifier = Modifier
                .size(width = 84.dp, height = 96.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { showPhotoManagementSheet = true }
            ) {
              AsyncImage(
                model = coil.request.ImageRequest.Builder(LocalContext.current)
                  .data(photoUrl)
                  .crossfade(true)
                  .build(),
                contentDescription = "Photo ${index + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            }
          }

          // If photos are fewer than fallback thumbnails or empty, show sample thumbnails
          if (validUserPhotos.isEmpty()) {
            val samplePhotos = listOf(
              "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
              "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80",
              "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&auto=format&fit=crop&q=80",
              "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=400&auto=format&fit=crop&q=80"
            )
            itemsIndexed(samplePhotos) { _, url ->
              Box(
                modifier = Modifier
                  .size(width = 84.dp, height = 96.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { showPhotoManagementSheet = true }
              ) {
                AsyncImage(
                  model = coil.request.ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                  contentDescription = "Sample Photo",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              }
            }
          }

          // + Add Photo item
          item {
            Box(
              modifier = Modifier
                .size(width = 84.dp, height = 96.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFAFAFA))
                .border(BorderStroke(1.dp, Color(0xFFE0E0E0)), RoundedCornerShape(12.dp))
                .clickable { showPhotoChoiceDialog = true },
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Filled.Add,
                  contentDescription = "Add Photo",
                  tint = Color(0xFFE53950),
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                  text = "Add Photo",
                  fontSize = 11.sp,
                  color = Color(0xFF555555),
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // ── 6. About Me Section ────────────────────────────────────────────
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
      border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "About Me",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1E)
          )
          Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Edit About Me",
            tint = Color(0xFF757575),
            modifier = Modifier
              .size(18.dp)
              .clickable { showAboutMeSheet = true }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // About Me Attributes list matching screenshot
        AboutMeItem(
          icon = Icons.Outlined.Cake,
          text = "$displayAge years old"
        )
        AboutMeItem(
          icon = Icons.Outlined.LocationOn,
          text = displayLocation
        )
        AboutMeItem(
          icon = Icons.Outlined.WorkOutline,
          text = userProfile.occupation.ifBlank { "Product Designer" }
        )
        AboutMeItem(
          icon = Icons.Outlined.School,
          text = userProfile.education.ifBlank { "National Institute of Design" }
        )
        AboutMeItem(
          icon = Icons.Outlined.FavoriteBorder,
          text = userProfile.datingIntention.ifBlank { "Looking for a meaningful relationship" }
        )
        AboutMeItem(
          icon = Icons.Outlined.Group,
          text = userProfile.promptAnswer.ifBlank { "Open to new people and great conversations" }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // ── 7. My Interests Section ────────────────────────────────────────
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
      border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "My Interests",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1E)
          )
          Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Edit Interests",
            tint = Color(0xFF757575),
            modifier = Modifier
              .size(18.dp)
              .clickable { showInterestsSheet = true }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // FlowRow of colorful pastel interest chips matching screenshot
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          displayedInterests.forEach { item ->
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = item.backgroundColor,
              modifier = Modifier.clickable { showInterestsSheet = true }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = item.iconEmoji,
                  fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = item.name,
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Medium,
                  color = item.textColor
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // ── 8. Dating Preferences Section ──────────────────────────────────
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp)
        .clickable { showPreferencesSheet = true },
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
      border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Outlined.Tune,
          contentDescription = null,
          tint = Color(0xFF1E1E1E),
          modifier = Modifier.size(24.dp)
        )

        Column(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 14.dp)
        ) {
          Text(
            text = "Dating Preferences",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1E)
          )
          Text(
            text = "Set your preferences and dealbreakers",
            fontSize = 12.sp,
            color = Color(0xFF757575),
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
          contentDescription = null,
          tint = Color(0xFF888888),
          modifier = Modifier.size(14.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(28.dp))
  }

  // ─────────────────────────────────────────────────────────────────────────
  // BOTTOM SHEETS & MODALS FOR EDITING
  // ─────────────────────────────────────────────────────────────────────────

  // 1. Photo Choice (Camera vs Gallery)
  if (showPhotoChoiceDialog) {
    AlertDialog(
      onDismissRequest = { showPhotoChoiceDialog = false },
      title = { Text("Update Photo", fontWeight = FontWeight.Bold) },
      text = { Text("Choose a photo from your gallery or take a new one with your camera.") },
      confirmButton = {
        Button(
          onClick = {
            showPhotoChoiceDialog = false
            galleryLauncher.launch(
              androidx.activity.result.PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
              )
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53950))
        ) {
          Text("Choose from Gallery")
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = {
            showPhotoChoiceDialog = false
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
          }
        ) {
          Text("Take Photo")
        }
      }
    )
  }

  // 2. Edit Profile Bio & Name Sheet
  if (showEditBioSheet) {
    ModalBottomSheet(
      onDismissRequest = { showEditBioSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      var editName by remember { mutableStateOf(userProfile.name) }
      var editAge by remember { mutableStateOf(if (userProfile.age > 0) userProfile.age.toString() else "24") }
      var editLocation by remember { mutableStateOf(userProfile.hometown.ifBlank { "Bangalore, India" }) }
      var editBio by remember { mutableStateOf(userProfile.bio) }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("Edit Basic Info", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = editName,
          onValueChange = { editName = it },
          label = { Text("Full Name") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE53950))
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = editAge,
          onValueChange = { editAge = it },
          label = { Text("Age") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE53950))
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = editLocation,
          onValueChange = { editLocation = it },
          label = { Text("Location / City") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE53950))
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = editBio,
          onValueChange = { editBio = it },
          label = { Text("Bio") },
          minLines = 3,
          maxLines = 6,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE53950))
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            val updated = userProfile.copy(
              name = editName.trim(),
              age = editAge.toIntOrNull() ?: userProfile.age,
              hometown = editLocation.trim(),
              bio = editBio.trim()
            )
            onSaveProfile(updated)
            showEditBioSheet = false
            Toast.makeText(context, "Profile updated ✨", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53950))
        ) {
          Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // 3. Edit About Me Details Sheet
  if (showAboutMeSheet) {
    ModalBottomSheet(
      onDismissRequest = { showAboutMeSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      var editOccupation by remember { mutableStateOf(userProfile.occupation) }
      var editEducation by remember { mutableStateOf(userProfile.education) }
      var editIntention by remember { mutableStateOf(userProfile.datingIntention) }
      var editPromptAnswer by remember { mutableStateOf(userProfile.promptAnswer) }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("Edit About Me", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = editOccupation,
          onValueChange = { editOccupation = it },
          label = { Text("Occupation / Job Title") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE53950))
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = editEducation,
          onValueChange = { editEducation = it },
          label = { Text("College / University / Education") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE53950))
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = editIntention,
          onValueChange = { editIntention = it },
          label = { Text("Dating Intention (e.g. Long-term relationship)") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE53950))
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = editPromptAnswer,
          onValueChange = { editPromptAnswer = it },
          label = { Text("Highlight / Open to") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE53950))
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            val updated = userProfile.copy(
              occupation = editOccupation.trim(),
              education = editEducation.trim(),
              datingIntention = editIntention.trim(),
              promptAnswer = editPromptAnswer.trim()
            )
            onSaveProfile(updated)
            showAboutMeSheet = false
            Toast.makeText(context, "About Me saved ✨", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53950))
        ) {
          Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // 4. Edit Interests Sheet
  if (showInterestsSheet) {
    ModalBottomSheet(
      onDismissRequest = { showInterestsSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      var currentSelections by remember {
        mutableStateOf(
          if (userProfile.passions.isNotEmpty()) userProfile.passions
          else allPresetInterests.take(9).map { it.name }
        )
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("Select Your Interests", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
          "Pick what excites you to connect with like-minded people.",
          fontSize = 12.sp,
          color = Color(0xFF555555),
          modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          allPresetInterests.forEach { item ->
            val isSelected = currentSelections.any { it.equals(item.name, ignoreCase = true) }
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (isSelected) Color(0xFFE53950) else item.backgroundColor,
              modifier = Modifier.clickable {
                currentSelections = if (isSelected) {
                  currentSelections.filterNot { it.equals(item.name, ignoreCase = true) }
                } else {
                  currentSelections + item.name
                }
              }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = item.iconEmoji, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = item.name,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium,
                  color = if (isSelected) Color.White else item.textColor
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = {
            val updated = userProfile.copy(passions = currentSelections)
            onSaveProfile(updated)
            showInterestsSheet = false
            Toast.makeText(context, "Interests saved ✨", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53950))
        ) {
          Text("Save Interests", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // 5. Manage Photos Sheet
  if (showPhotoManagementSheet) {
    ModalBottomSheet(
      onDismissRequest = { showPhotoManagementSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("Manage Photos", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
          "Your first photo is your main profile picture.",
          fontSize = 12.sp,
          color = Color(0xFF555555),
          modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))

        userProfile.photos.forEachIndexed { index, photoUrl ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
            border = BorderStroke(1.dp, Color(0xFFEEEEEE))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(60.dp)
                  .clip(RoundedCornerShape(8.dp))
              ) {
                AsyncImage(
                  model = photoUrl,
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              }

              Column(
                modifier = Modifier
                  .weight(1f)
                  .padding(start = 12.dp)
              ) {
                Text(
                  text = if (index == 0) "Primary Photo (Avatar)" else "Photo #${index + 1}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = if (index == 0) Color(0xFFE53950) else Color(0xFF222222)
                )
                if (index != 0) {
                  Text(
                    text = "Make Primary",
                    fontSize = 12.sp,
                    color = Color(0xFFE53950),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                      .clickable {
                        onSetPrimaryPhoto(index)
                        Toast.makeText(context, "Primary photo updated", Toast.LENGTH_SHORT).show()
                      }
                      .padding(vertical = 2.dp)
                  )
                }
              }

              IconButton(
                onClick = {
                  onRemovePhoto(index)
                  Toast.makeText(context, "Photo removed", Toast.LENGTH_SHORT).show()
                }
              ) {
                Icon(
                  imageVector = Icons.Filled.Delete,
                  contentDescription = "Delete",
                  tint = Color(0xFFD32F2F)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = {
            showPhotoChoiceDialog = true
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53950))
        ) {
          Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add New Photo", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // 6. Dating Preferences Sheet
  if (showPreferencesSheet) {
    ModalBottomSheet(
      onDismissRequest = { showPreferencesSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      var distanceKm by remember { mutableFloatStateOf(25f) }
      var minAge by remember { mutableFloatStateOf(20f) }
      var maxAge by remember { mutableFloatStateOf(32f) }
      var interestedIn by remember { mutableStateOf("Everyone") }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("Dating Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
          "Maximum Distance: ${distanceKm.toInt()} km",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium
        )
        Slider(
          value = distanceKm,
          onValueChange = { distanceKm = it },
          valueRange = 5f..150f,
          colors = SliderDefaults.colors(
            thumbColor = Color(0xFFE53950),
            activeTrackColor = Color(0xFFE53950)
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          "Age Range: ${minAge.toInt()} - ${maxAge.toInt()} years",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium
        )
        Slider(
          value = maxAge,
          onValueChange = { maxAge = it },
          valueRange = 21f..60f,
          colors = SliderDefaults.colors(
            thumbColor = Color(0xFFE53950),
            activeTrackColor = Color(0xFFE53950)
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Interested In", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("Women", "Men", "Everyone").forEach { genderOption ->
            val selected = interestedIn == genderOption
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (selected) Color(0xFFE53950) else Color(0xFFF0F0F0),
              modifier = Modifier
                .weight(1f)
                .clickable { interestedIn = genderOption }
            ) {
              Text(
                text = genderOption,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) Color.White else Color(0xFF333333),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 10.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = {
            showPreferencesSheet = false
            Toast.makeText(context, "Preferences updated ✨", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53950))
        ) {
          Text("Save Preferences", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // 7. Settings Modal Sheet
  if (showSettingsSheet) {
    ModalBottomSheet(
      onDismissRequest = { showSettingsSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("Settings & Account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Dark Theme Switcher
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (themeMode == AppThemeMode.DARK) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
              contentDescription = null,
              tint = Color(0xFFE53950),
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("Dark Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text("Toggle dark appearance", fontSize = 11.5.sp, color = Color(0xFF555555))
            }
          }
          Switch(
            checked = themeMode == AppThemeMode.DARK,
            onCheckedChange = { isDark ->
              onThemeModeChange(if (isDark) AppThemeMode.DARK else AppThemeMode.LIGHT)
            },
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFE53950))
          )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

        // Disable / Pause Account
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              onDisableAccount(!userProfile.isAccountDisabled)
            }
            .padding(vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.PauseCircle,
            contentDescription = null,
            tint = Color(0xFFE53950),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text("Pause Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
              "Temporarily hide your profile from Discover",
              fontSize = 11.5.sp,
              color = Color(0xFF555555)
            )
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

        // Reset / Re-run Onboarding
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              showSettingsSheet = false
              onRestartOnboarding()
            }
            .padding(vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.AccountCircle,
            contentDescription = null,
            tint = Color(0xFF222222),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text("Update Complete Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Re-enter full 6-step registration details", fontSize = 11.5.sp, color = Color(0xFF555555))
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

        // Logout
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              showSettingsSheet = false
              onLogout()
            }
            .padding(vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.Logout,
            contentDescription = null,
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text("Log Out", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // 8. Notifications Dialog
  if (showNotificationsDialog) {
    AlertDialog(
      onDismissRequest = { showNotificationsDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Color(0xFFE53950))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Notifications", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          NotificationRowItem("✨ You matched with Sarah!", "2 mins ago")
          NotificationRowItem("❤️ Someone liked your photo", "1 hour ago")
          NotificationRowItem("🔥 Your profile got 38 new views today!", "Today")
        }
      },
      confirmButton = {
        TextButton(onClick = { showNotificationsDialog = false }) {
          Text("Close", color = Color(0xFFE53950), fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}

@Composable
private fun StatCounterItem(
  icon: ImageVector,
  iconTint: Color,
  label: String,
  value: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(horizontal = 2.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Column {
      Text(
        text = label,
        fontSize = 11.sp,
        color = Color(0xFF757575),
        fontWeight = FontWeight.Normal
      )
      Text(
        text = value,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E1E1E),
        lineHeight = 16.sp
      )
    }
  }
}

@Composable
private fun StatDivider() {
  Box(
    modifier = Modifier
      .width(1.dp)
      .height(26.dp)
      .background(Color(0xFFE8E8E8))
  )
}

@Composable
private fun AboutMeItem(
  icon: ImageVector,
  text: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 5.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Color(0xFF555555),
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = text,
      fontSize = 13.5.sp,
      color = Color(0xFF2E2E2E),
      fontWeight = FontWeight.Normal
    )
  }
}

@Composable
private fun NotificationRowItem(
  title: String,
  time: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF222222))
    Text(text = time, fontSize = 11.sp, color = Color(0xFF888888))
  }
}

private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): Uri? {
  return try {
    val filename = "profile_img_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, filename)
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
    outputStream.flush()
    outputStream.close()
    Uri.fromFile(file)
  } catch (e: Exception) {
    null
  }
}
