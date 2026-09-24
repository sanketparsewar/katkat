package com.example.ui.screens

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmokingRooms
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.KatkatNotification
import com.example.data.model.KatkatNotificationType
import com.example.data.model.SubscriptionState
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserProfile
import com.example.ui.components.NotificationsBottomSheet
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.util.LocationHelper
import java.util.Calendar
import kotlinx.coroutines.launch

// Preset interest category item
data class InterestItem(
  val name: String,
  val iconEmoji: String,
  val backgroundColor: Color,
  val textColor: Color
)

val AllPresetInterests = listOf(
  InterestItem("Travel", "✈️", Color(0xFFFFEBEE), Color(0xFFD32F2F)),
  InterestItem("Foodie", "🍴", Color(0xFFFFF3E0), Color(0xFFE65100)),
  InterestItem("Music", "🎵", Color(0xFFEDE7F6), Color(0xFF5E35B1)),
  InterestItem("Fitness & Gym", "🏋️", Color(0xFFFCE4EC), Color(0xFFC2185B)),
  InterestItem("Reading & Books", "📖", Color(0xFFE3F2FD), Color(0xFF1976D2)),
  InterestItem("Photography", "📷", Color(0xFFFFEBEE), Color(0xFFE91E63)),
  InterestItem("Nature & Hiking", "🍃", Color(0xFFE8F5E9), Color(0xFF2E7D32)),
  InterestItem("Dogs & Pets", "🐾", Color(0xFFF3E5F5), Color(0xFF7B1FA2)),
  InterestItem("Coffee", "☕", Color(0xFFE0F7FA), Color(0xFF00838F)),
  InterestItem("Art & Design", "🎨", Color(0xFFFFF8E1), Color(0xFFF57F17)),
  InterestItem("Gaming", "🎮", Color(0xFFEDE7F6), Color(0xFF673AB7)),
  InterestItem("Yoga & Meditation", "🧘", Color(0xFFF1F8E9), Color(0xFF558B2F)),
  InterestItem("Cooking", "🍳", Color(0xFFFFF3E0), Color(0xFFD84315)),
  InterestItem("Movies & Cinema", "🎬", Color(0xFFECEFF1), Color(0xFF37474F)),
  InterestItem("Startups & Tech", "💻", Color(0xFFE1F5FE), Color(0xFF0277BD)),
  InterestItem("Dancing", "💃", Color(0xFFFCE4EC), Color(0xFFAD1457)),
  InterestItem("Swimming", "🏊", Color(0xFFE0F2F1), Color(0xFF00695C))
)

val AllDatingIntentionsList = listOf(
  "Long-term ❤️",
  "Casual dating ☕",
  "Marriage 💍",
  "New friends 🤝",
  "Short-term ✨",
  "Figuring it out 🧭"
)

val HeightOptionsList = listOf(
  "4'10\" (147 cm)", "4'11\" (150 cm)",
  "5'0\" (152 cm)", "5'1\" (155 cm)", "5'2\" (157 cm)", "5'3\" (160 cm)",
  "5'4\" (163 cm)", "5'5\" (165 cm)", "5'6\" (168 cm)", "5'7\" (170 cm)",
  "5'8\" (173 cm)", "5'9\" (175 cm)", "5'10\" (178 cm)", "5'11\" (180 cm)",
  "6'0\" (183 cm)", "6'1\" (185 cm)", "6'2\" (188 cm)", "6'3\" (190 cm)",
  "6'4\" (193 cm)", "6'5\" (196 cm)", "6'6\" (198 cm)"
)

val AllZodiacList = listOf(
  "Aries ♈", "Taurus ♉", "Gemini ♊", "Cancer ♋",
  "Leo ♌", "Virgo ♍", "Libra ♎", "Scorpio ♏",
  "Sagittarius ♐", "Capricorn ♑", "Aquarius ♒", "Pisces ♓"
)

val DrinkingHabitsList = listOf(
  "Non-drinker 🚫",
  "Socially on weekends 🍷",
  "Frequently 🍻",
  "Sober & Clean 🌱",
  "Prefer not to say"
)

val SmokingHabitsList = listOf(
  "Non-smoker 🚭",
  "Occasionally 💨",
  "Regular smoker 🚬",
  "Trying to quit 🌿",
  "Prefer not to say"
)

val PetsHabitsList = listOf(
  "Dog lover 🐶",
  "Cat lover 🐱",
  "Has multiple pets 🐾",
  "Bird / Aquarium 🦜",
  "No pets, but love them ❤️",
  "No pets 🚫"
)

val PromptQuestionList = listOf(
  "My simple pleasures in life...",
  "The key to my heart is...",
  "A life goal of mine...",
  "My most controversial opinion...",
  "Together, we could...",
  "Two truths and a lie...",
  "Best travel memory..."
)

val GenderOptionsList = listOf("Woman", "Man", "Non-binary", "Other")
val PronounsOptionsList = listOf("she/her", "he/him", "they/them", "she/they", "he/they", "other")

fun parseDobToCalendar(dobStr: String): Calendar {
  val cal = Calendar.getInstance()
  cal.add(Calendar.YEAR, -24)
  if (dobStr.isBlank()) return cal

  try {
    if (dobStr.contains("/")) {
      val parts = dobStr.split("/")
      if (parts.size == 3) {
        val d = parts[0].trim().toInt()
        val m = parts[1].trim().toInt() - 1
        val y = parts[2].trim().toInt()
        cal.set(y, m, d)
        return cal
      }
    } else if (dobStr.contains("-")) {
      val parts = dobStr.split("-")
      if (parts.size == 3) {
        if (parts[0].length == 4) { // YYYY-MM-DD
          val y = parts[0].trim().toInt()
          val m = parts[1].trim().toInt() - 1
          val d = parts[2].trim().toInt()
          cal.set(y, m, d)
          return cal
        } else { // DD-MM-YYYY
          val d = parts[0].trim().toInt()
          val m = parts[1].trim().toInt() - 1
          val y = parts[2].trim().toInt()
          cal.set(y, m, d)
          return cal
        }
      }
    }
  } catch (_: Exception) {}
  return cal
}

fun calculateAgeFromDobString(dobStr: String): Int {
  if (dobStr.isBlank()) return 0
  try {
    val cal = parseDobToCalendar(dobStr)
    val today = Calendar.getInstance()
    var age = today.get(Calendar.YEAR) - cal.get(Calendar.YEAR)
    if (today.get(Calendar.MONTH) < cal.get(Calendar.MONTH) ||
      (today.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && today.get(Calendar.DAY_OF_MONTH) < cal.get(Calendar.DAY_OF_MONTH))
    ) {
      age--
    }
    return age.coerceAtLeast(0)
  } catch (_: Exception) {
    return 0
  }
}

fun formatToDdMmYyyy(dobStr: String): String {
  if (dobStr.isBlank()) return ""
  try {
    if (dobStr.contains("/")) {
      val parts = dobStr.split("/")
      if (parts.size == 3 && parts[2].length == 4) {
        val d = parts[0].trim().toInt()
        val m = parts[1].trim().toInt()
        val y = parts[2].trim().toInt()
        return "%02d/%02d/%04d".format(d, m, y)
      }
    } else if (dobStr.contains("-")) {
      val parts = dobStr.split("-")
      if (parts.size == 3) {
        if (parts[0].length == 4) { // YYYY-MM-DD
          val y = parts[0].trim().toInt()
          val m = parts[1].trim().toInt()
          val d = parts[2].trim().toInt()
          return "%02d/%02d/%04d".format(d, m, y)
        } else {
          val d = parts[0].trim().toInt()
          val m = parts[1].trim().toInt()
          val y = parts[2].trim().toInt()
          return "%02d/%02d/%04d".format(d, m, y)
        }
      }
    }
  } catch (_: Exception) {}
  return dobStr
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
  userProfile: UserProfile,
  subscriptionState: SubscriptionState,
  themeMode: AppThemeMode = AppThemeMode.LIGHT,
  likesCount: Int = 0,
  matchesCount: Int = 0,
  chatsCount: Int = 0,
  isUploadingPhoto: Boolean = false,
  notifications: List<KatkatNotification> = emptyList(),
  unreadNotificationCount: Int = 0,
  onThemeModeChange: (AppThemeMode) -> Unit = {},
  onSaveProfile: (UserProfile) -> Unit,
  onAddPhoto: (String) -> Unit,
  onAddBitmap: (Bitmap) -> Unit = {},
  onRemovePhoto: (Int) -> Unit,
  onSetPrimaryPhoto: (Int) -> Unit = {},
  onReplacePhoto: (Int, String) -> Unit = { _, _ -> },
  onOpenPaywall: () -> Unit = {},
  onRestartOnboarding: () -> Unit = {},
  onDisableAccount: (Boolean) -> Unit = {},
  onDeleteAccount: () -> Unit = {},
  onLogout: () -> Unit = {},
  onMarkNotificationRead: (String) -> Unit = {},
  onMarkAllNotificationsRead: () -> Unit = {},
  onDeleteNotification: (String) -> Unit = {},
  onClearAllNotifications: () -> Unit = {},
  onNavigateToChat: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Calculate profile completeness and missing fields
  val (completionPercent, remainingFields) = remember(userProfile) {
    userProfile.calculateProfileCompletion()
  }

  // Sheet dialog states for all categories
  var showBasicInfoSheet by remember { mutableStateOf(false) }
  var showWorkEducationSheet by remember { mutableStateOf(false) }
  var showIntentionsLifestyleSheet by remember { mutableStateOf(false) }
  var showInterestsSheet by remember { mutableStateOf(false) }
  var showPromptsSheet by remember { mutableStateOf(false) }
  var showContactInfoSheet by remember { mutableStateOf(false) }
  var showPhotoManagementSheet by remember { mutableStateOf(false) }
  var showPreferencesSheet by remember { mutableStateOf(false) }
  var showSettingsSheet by remember { mutableStateOf(false) }
  var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }
  var deleteAccountInputText by remember { mutableStateOf("") }
  var showNotificationsDialog by remember { mutableStateOf(false) }
  var showPhotoChoiceDialog by remember { mutableStateOf(false) }

  // Media pickers
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let { onAddPhoto(it.toString()) }
  }

  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    bitmap?.let { onAddBitmap(it) }
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

  // Primary avatar image URL from database
  val avatarUrl = userProfile.photos.firstOrNull { it.isNotBlank() }

  val displayName = userProfile.name.ifBlank { "User" }
  val displayAge = if (userProfile.age > 0) userProfile.age else null
  val displayLocation = when {
    userProfile.hometown.isNotBlank() && userProfile.currentLocationCity.isNotBlank() ->
      "${userProfile.currentLocationCity}, ${userProfile.hometown}"
    userProfile.currentLocationCity.isNotBlank() -> userProfile.currentLocationCity
    userProfile.hometown.isNotBlank() -> userProfile.hometown
    else -> ""
  }
  val displayBio = userProfile.bio

  // Real user passions from database
  val displayedInterests = remember(userProfile.passions) {
    userProfile.passions.map { passionName ->
      AllPresetInterests.find { it.name.equals(passionName, ignoreCase = true) }
        ?: InterestItem(
          name = passionName,
          iconEmoji = "✨",
          backgroundColor = Color(0xFFFFEBEE),
          textColor = Color(0xFFE53950)
        )
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .testTag("profile_edit_screen")
  ) {
    // ── 1. Top Bar Header (Brand Name & Action Icons) ───────────────────
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Katkat",
          fontSize = 26.sp,
          fontWeight = FontWeight.Bold,
          fontStyle = FontStyle.Italic,
          color = CoralPrimary,
          letterSpacing = (-0.5).sp
        )
        Text(
          text = "Better People. Brighter Connections.",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Normal,
          modifier = Modifier.padding(top = 1.dp)
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = { showSettingsSheet = true },
          modifier = Modifier.size(38.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
          )
        }

        Box(
          modifier = Modifier.size(38.dp),
          contentAlignment = Alignment.Center
        ) {
          IconButton(
            onClick = { showNotificationsDialog = true },
            modifier = Modifier.fillMaxSize().testTag("btn_profile_notifications")
          ) {
            Icon(
              imageVector = Icons.Outlined.Notifications,
              contentDescription = "Notifications",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(24.dp)
            )
          }
          // Notification Dot Badge
          if (unreadNotificationCount > 0) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-2).dp, y = 2.dp)
                .clip(CircleShape)
                .background(CoralPrimary)
                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(2.dp))

    // Paused Profile Top Alert Banner (if account is paused/disabled)
    if (userProfile.isAccountDisabled) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 4.dp)
          .testTag("paused_profile_alert_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        border = BorderStroke(1.dp, Color(0xFFFFB74D))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = Color(0xFFFF9800).copy(alpha = 0.2f),
            modifier = Modifier.size(38.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.PauseCircle,
                contentDescription = "Profile Paused",
                tint = Color(0xFFE65100),
                modifier = Modifier.size(22.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Profile Paused & Hidden",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color(0xFFE65100)
            )
            Text(
              text = "Hidden from Discover. Existing matches and chats remain active.",
              fontSize = 11.5.sp,
              color = Color(0xFF5D4037),
              lineHeight = 15.sp,
              modifier = Modifier.padding(top = 1.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = { onDisableAccount(false) },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("unpause_profile_banner_button")
          ) {
            Text("Unpause", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }

    // ── 2. Top Profile Info (Avatar + Identity + Single Edit Button) ─────
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 4.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Circular Avatar with profile completion progress border
          Box(
            modifier = Modifier
              .size(92.dp)
              .clickable { showPhotoManagementSheet = true },
            contentAlignment = Alignment.Center
          ) {
            // Circular Completion Progress Ring
            Canvas(modifier = Modifier.fillMaxSize()) {
              val strokeWidthPx = 4.dp.toPx()
              val radius = (size.minDimension - strokeWidthPx) / 2f
              val centerOffset = Offset(size.width / 2f, size.height / 2f)
              val topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)
              val arcSize = Size(radius * 2f, radius * 2f)

              // Background ring track
              drawArc(
                color = CoralPrimary.copy(alpha = 0.15f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx)
              )

              // Active progress arc
              val sweep = (completionPercent / 100f) * 360f
              if (sweep > 0f) {
                drawArc(
                  color = if (completionPercent == 100) Color(0xFF4CAF50) else CoralPrimary,
                  startAngle = -90f,
                  sweepAngle = sweep,
                  useCenter = false,
                  topLeft = topLeft,
                  size = arcSize,
                  style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round
                  )
                )
              }
            }

            // Inner Avatar Image
            Box(
              modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(
                  Brush.verticalGradient(
                    listOf(Color(0xFF2E1A36), Color(0xFF16091D))
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                  model = coil.request.ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                  contentDescription = "User Avatar",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              } else {
                Text(
                  text = displayName.take(1).uppercase().ifBlank { "?" },
                  style = MaterialTheme.typography.headlineLarge,
                  color = CoralPrimary,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            // Percentage Badge at bottom center of avatar
            Surface(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 6.dp),
              shape = RoundedCornerShape(10.dp),
              color = if (completionPercent == 100) Color(0xFF4CAF50) else CoralPrimary,
              shadowElevation = 2.dp,
              border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface)
            ) {
              Text(
                text = "$completionPercent%",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(16.dp))

          // Name, Gender, Current Location, Hometown
          Column(modifier = Modifier.weight(1f)) {
            // 1. Name & Age (+ Verified badge)
            Row(verticalAlignment = Alignment.CenterVertically) {
              val ageStr = if (displayAge != null && displayAge > 0) ", $displayAge" else ""
              Text(
                text = "$displayName$ageStr",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              if (userProfile.isPhoneVerified) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Outlined.Verified,
                  contentDescription = "Verified",
                  tint = Color(0xFF4CAF50),
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            // 2. Gender (below name)
            val genderDisplay = remember(userProfile.gender, userProfile.pronouns) {
              listOfNotNull(
                userProfile.gender.ifBlank { null },
                userProfile.pronouns.ifBlank { null }
              ).joinToString(" • ").ifBlank { "Gender not set" }
            }
            Text(
              text = genderDisplay,
              fontSize = 12.5.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(top = 2.dp)
            )

            // 3. Current Location (below gender)
            val currentLocationDisplay = userProfile.currentLocationCity.ifBlank { "Location not set" }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 3.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = CoralPrimary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = currentLocationDisplay,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
              )
            }

            // 4. Hometown with home emoji (below current location)
            val hometownDisplay = userProfile.hometown.ifBlank { "Hometown not set" }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 2.dp)
            ) {
              Text(
                text = "🏠",
                fontSize = 12.sp
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = hometownDisplay,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // Bio section
        if (displayBio.isNotBlank()) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = displayBio,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Single Edit Profile / Basic Info Button
        Button(
          onClick = { showBasicInfoSheet = true },
          modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = CoralPrimary.copy(alpha = 0.12f),
            contentColor = CoralPrimary
          ),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Edit Basic Info",
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // ── 2B. Interactive Profile Completion Percentage Card & Missing Fields ───
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 4.dp)
        .testTag("profile_completion_percentage_card"),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (completionPercent == 100) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      border = BorderStroke(
        1.dp,
        if (completionPercent == 100) Color(0xFF81C784).copy(alpha = 0.6f) else CoralPrimary.copy(alpha = 0.25f)
      )
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
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (completionPercent == 100) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
              contentDescription = null,
              tint = if (completionPercent == 100) Color(0xFF2E7D32) else CoralPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (completionPercent == 100) "Profile Completed" else "Profile Completion",
              fontWeight = FontWeight.Bold,
              fontSize = 14.5.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (completionPercent == 100) Color(0xFF4CAF50) else CoralPrimary,
            contentColor = Color.White
          ) {
            Text(
              text = "$completionPercent%",
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Linear Progress Bar
        val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
          targetValue = completionPercent / 100f,
          animationSpec = androidx.compose.animation.core.tween(durationMillis = 600),
          label = "profile_completion_progress"
        )

        LinearProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(4.dp)),
          color = if (completionPercent == 100) Color(0xFF4CAF50) else CoralPrimary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )

        if (completionPercent == 100) {
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "⭐ Star Profile! Your profile is 100% complete and boosted for maximum visibility in Discover.",
              fontSize = 12.sp,
              color = Color(0xFF2E7D32),
              lineHeight = 16.sp
            )
          }
        } else if (remainingFields.isNotEmpty()) {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Tap to complete missing details & boost matches:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            remainingFields.forEach { field ->
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = CoralPrimary.copy(alpha = 0.1f),
                border = BorderStroke(0.5.dp, CoralPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.clickable {
                  when (field) {
                    "Bio", "Name", "Age / Birthday", "Gender", "Location" -> showBasicInfoSheet = true
                    "Add photos", "Add 2+ photos" -> showPhotoManagementSheet = true
                    "Work or Education" -> showWorkEducationSheet = true
                    "Lifestyle info" -> showIntentionsLifestyleSheet = true
                    "Interests" -> showInterestsSheet = true
                    "Profile Prompt" -> showPromptsSheet = true
                    else -> showBasicInfoSheet = true
                  }
                }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = field,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CoralPrimary
                  )
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // ── 3. Real Stats Row from Database ─────────────────────────────────
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Likes
      StatCounterItem(
        icon = Icons.Outlined.FavoriteBorder,
        iconTint = CoralPrimary,
        label = "Likes",
        value = "$likesCount"
      )

      StatDivider()

      // 2. Matches
      StatCounterItem(
        icon = Icons.Outlined.Group,
        iconTint = CoralPrimary,
        label = "Matches",
        value = "$matchesCount"
      )

      StatDivider()

      // 3. Chats
      StatCounterItem(
        icon = Icons.Outlined.ChatBubbleOutline,
        iconTint = CoralPrimary,
        label = "Chats",
        value = "$chatsCount"
      )

      StatDivider()

      // 4. Swipes Used
      StatCounterItem(
        icon = Icons.Outlined.Visibility,
        iconTint = MaterialTheme.colorScheme.onSurface,
        label = "Swipes Used",
        value = "${subscriptionState.swipesUsedThisMonth}"
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // ── 4. Dynamic Active Plan / Upgrade Banner ──────────────────────────
    val currentPlanTier = subscriptionState.currentTier
    val isVipPlan = currentPlanTier == SubscriptionTier.TIER_2
    val isPlusPlan = currentPlanTier == SubscriptionTier.TIER_1

    val bannerContainerColor = when {
      isVipPlan -> Color(0xFFFFF9E6)
      isPlusPlan -> Color(0xFFFFF0F3)
      else -> Color(0xFFFFEBEE)
    }

    val bannerBorder = when {
      isVipPlan -> BorderStroke(1.dp, GoldVip.copy(alpha = 0.5f))
      isPlusPlan -> BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.35f))
      else -> null
    }

    val bannerIconColor = when {
      isVipPlan -> GoldVip
      isPlusPlan -> Color(0xFF7B1FA2)
      else -> CoralPrimary
    }

    val bannerTitle = when {
      isVipPlan -> "KatKat VIP"
      isPlusPlan -> "KatKat Plus"
      else -> "KatKat Free"
    }

    val bannerSubtitle = when {
      isVipPlan -> "${subscriptionState.remainingSwipes} swipes left (${subscriptionState.swipesUsedThisMonth}/300) • VIP Badge & Unlimited Rewinds"
      isPlusPlan -> "${subscriptionState.remainingSwipes} swipes left (${subscriptionState.swipesUsedThisMonth}/150) • Tap to upgrade to VIP"
      else -> "${subscriptionState.swipesUsedThisMonth}/50 swipes used • Unlock See Who Liked You"
    }

    val bannerBadgeText = when {
      isVipPlan -> "👑 VIP"
      isPlusPlan -> "💜 PLUS"
      else -> "✨ UPGRADE"
    }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 2.dp)
        .clickable { onOpenPaywall() }
        .testTag("profile_active_plan_card"),
      shape = RoundedCornerShape(12.dp),
      border = bannerBorder,
      colors = CardDefaults.cardColors(containerColor = bannerContainerColor),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(bannerIconColor),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isPlusPlan) Icons.Filled.ElectricBolt else Icons.Filled.WorkspacePremium,
            contentDescription = null,
            tint = if (isVipPlan) Color.Black else Color.White,
            modifier = Modifier.size(16.dp)
          )
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 9.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = bannerTitle,
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Bold,
              color = if (isVipPlan) Color(0xFF7A5200) else if (isPlusPlan) Color(0xFF512DA8) else CoralPrimary
            )
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(if (isVipPlan) GoldVip else if (isPlusPlan) Color(0xFF7B1FA2) else CoralPrimary)
                .padding(horizontal = 4.5.dp, vertical = 1.dp)
            ) {
              Text(
                text = bannerBadgeText,
                color = if (isVipPlan) Color.Black else Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.2.sp
              )
            }
          }
          Text(
            text = bannerSubtitle,
            fontSize = 10.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 1.dp),
            maxLines = 1
          )
        }

        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(11.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // ── 5. My Photos Section ───────────────────────────────────────────
    val validUserPhotos = userProfile.photos.filter { it.isNotBlank() }

    CategoryCard(
      title = "My Photos",
      actionText = "Manage",
      onAction = { showPhotoManagementSheet = true }
    ) {
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
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

        // + Add Photo item
        item {
          Box(
            modifier = Modifier
              .size(width = 84.dp, height = 96.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .border(
                BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.4f)),
                RoundedCornerShape(12.dp)
              )
              .clickable(enabled = !isUploadingPhoto) { showPhotoChoiceDialog = true },
            contentAlignment = Alignment.Center
          ) {
            if (isUploadingPhoto) {
              CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = CoralPrimary,
                strokeWidth = 2.5.dp
              )
            } else {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Filled.Add,
                  contentDescription = "Add Photo",
                  tint = CoralPrimary,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                  text = "Add Photo",
                  fontSize = 11.sp,
                  color = CoralPrimary,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // ── 6. Work & Education Section ────────────────────────────────────
    CategoryCard(
      title = "Work & Education",
      actionIcon = Icons.Outlined.Edit,
      onAction = { showWorkEducationSheet = true }
    ) {
      val hasOccupation = userProfile.occupation.isNotBlank()
      val hasEducation = userProfile.education.isNotBlank()

      if (!hasOccupation && !hasEducation) {
        Text(
          text = "No work or education info added yet. Tap to add.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      } else {
        if (hasOccupation) {
          ProfileAttributeItem(
            icon = Icons.Outlined.WorkOutline,
            label = "Occupation",
            value = userProfile.occupation
          )
        }
        if (hasEducation) {
          ProfileAttributeItem(
            icon = Icons.Outlined.School,
            label = "Education",
            value = userProfile.education
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // ── 7. Relationship Goals & Details ────────────────────────────────
    CategoryCard(
      title = "Dating Goals & Details",
      actionIcon = Icons.Outlined.Edit,
      onAction = { showIntentionsLifestyleSheet = true }
    ) {
      val hasIntention = userProfile.datingIntention.isNotBlank()
      val hasHeight = userProfile.height.isNotBlank()
      val hasZodiac = userProfile.zodiac.isNotBlank()
      val hasDrinking = userProfile.drinking.isNotBlank()
      val hasSmoking = userProfile.smoking.isNotBlank()
      val hasPets = userProfile.pets.isNotBlank()

      val anyDetail = hasIntention || hasHeight || hasZodiac || hasDrinking || hasSmoking || hasPets

      if (!anyDetail) {
        Text(
          text = "No lifestyle or dating goals added yet. Tap to customize.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      } else {
        if (hasIntention) {
          ProfileAttributeItem(
            icon = Icons.Outlined.FavoriteBorder,
            label = "Relationship Goal",
            value = userProfile.datingIntention
          )
        }
        if (hasHeight) {
          ProfileAttributeItem(
            icon = Icons.Filled.Straighten,
            label = "Height",
            value = userProfile.height
          )
        }
        if (hasZodiac) {
          ProfileAttributeItem(
            icon = Icons.Outlined.Stars,
            label = "Zodiac Sign",
            value = userProfile.zodiac
          )
        }
        if (hasDrinking) {
          ProfileAttributeItem(
            icon = Icons.Outlined.LocalBar,
            label = "Drinking",
            value = userProfile.drinking
          )
        }
        if (hasSmoking) {
          ProfileAttributeItem(
            icon = Icons.Outlined.SmokingRooms,
            label = "Smoking",
            value = userProfile.smoking
          )
        }
        if (hasPets) {
          ProfileAttributeItem(
            icon = Icons.Filled.Pets,
            label = "Pets",
            value = userProfile.pets
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // ── 8. My Interests & Passions Section ──────────────────────────────
    CategoryCard(
      title = "My Interests",
      actionIcon = Icons.Outlined.Edit,
      onAction = { showInterestsSheet = true }
    ) {
      if (displayedInterests.isEmpty()) {
        Text(
          text = "No interests selected yet. Tap edit to pick what you love.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      } else {
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          displayedInterests.forEach { item ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = CoralPrimary.copy(alpha = 0.12f),
              border = BorderStroke(0.5.dp, CoralPrimary.copy(alpha = 0.35f)),
              modifier = Modifier.clickable { showInterestsSheet = true }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = item.iconEmoji, fontSize = 11.5.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = item.name,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Medium,
                  color = CoralPrimary
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // ── 9. Profile Prompt & Conversation Starter ───────────────────────
    CategoryCard(
      title = "Profile Prompt",
      actionIcon = Icons.Outlined.Edit,
      onAction = { showPromptsSheet = true }
    ) {
      val question = userProfile.promptQuestion.ifBlank { "My simple pleasures in life..." }
      val answer = userProfile.promptAnswer

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showPromptsSheet = true }
      ) {
        Text(
          text = question,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = CoralPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (answer.isNotBlank()) answer else "Tap to write an answer to start fun conversations...",
          fontSize = 13.sp,
          lineHeight = 18.sp,
          color = if (answer.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // ── 10. Contact & Account Info ─────────────────────────────────────
    CategoryCard(
      title = "Contact & Account",
      actionIcon = Icons.Outlined.Edit,
      onAction = { showContactInfoSheet = true }
    ) {
      if (userProfile.phoneNumber.isNotBlank()) {
        ProfileAttributeItem(
          icon = Icons.Outlined.Phone,
          label = "Mobile Number",
          value = "${userProfile.countryCode} ${userProfile.phoneNumber}",
          trailingContent = if (userProfile.isPhoneVerified) {
            {
              Surface(
                shape = CircleShape,
                color = Color(0xFFE8F5E9),
                modifier = Modifier.size(24.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Verified Mobile Number",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          } else null
        )
      }
      ProfileAttributeItem(
        icon = Icons.Outlined.Email,
        label = "Email Address",
        value = if (userProfile.email.isNotBlank()) userProfile.email else "N/A"
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // ── 11. Dating Discovery Preferences ───────────────────────────────
    val summaryInterestedIn = when {
      userProfile.interestedInGender.isNotBlank() -> userProfile.interestedInGender
      userProfile.gender.equals("Man", ignoreCase = true) -> "Women"
      userProfile.gender.equals("Woman", ignoreCase = true) || userProfile.gender.equals("Women", ignoreCase = true) -> "Men"
      else -> "Everyone"
    }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp)
        .clickable { showPreferencesSheet = true },
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
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
          tint = CoralPrimary,
          modifier = Modifier.size(24.dp)
        )

        Column(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 14.dp)
        ) {
          Text(
            text = "Discovery Preferences",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Up to ${userProfile.maxDistanceKm} km • Ages ${userProfile.minAgePreference}–${userProfile.maxAgePreference} • Interested in $summaryInterestedIn",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(14.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }

  // ─────────────────────────────────────────────────────────────────────────
  // BOTTOM SHEETS & MODALS FOR EDITING ALL CATEGORIES
  // ─────────────────────────────────────────────────────────────────────────

  // 1. Photo Choice (Camera, File / Storage, Gallery)
  if (showPhotoChoiceDialog) {
    AlertDialog(
      onDismissRequest = { showPhotoChoiceDialog = false },
      title = {
        Text(
          "Update Photo",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            "Choose a source to update your profile photo:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(4.dp))

          // Option 1: Camera
          Surface(
            onClick = {
              showPhotoChoiceDialog = false
              cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.3f)),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_photo_camera")
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = CircleShape,
                color = CoralPrimary.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = CoralPrimary,
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  "Camera",
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  "Take a new photo with camera",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          // Option 2: Photo Gallery
          Surface(
            onClick = {
              showPhotoChoiceDialog = false
              galleryLauncher.launch(
                androidx.activity.result.PickVisualMediaRequest(
                  ActivityResultContracts.PickVisualMedia.ImageOnly
                )
              )
            },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.3f)),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_photo_gallery")
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = CircleShape,
                color = CoralPrimary.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    Icons.Outlined.PhotoLibrary,
                    contentDescription = "Gallery",
                    tint = CoralPrimary,
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  "Photo Gallery",
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  "Choose an image from photo albums",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showPhotoChoiceDialog = false }) {
          Text("Cancel", color = CoralPrimary)
        }
      }
    )
  }

  // 2. Edit Basic Info Sheet (Name, Age, DOB, Gender, Pronouns, Location, Bio)
  if (showBasicInfoSheet) {
    ModalBottomSheet(
      onDismissRequest = { showBasicInfoSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      val coroutineScope = rememberCoroutineScope()
      var editName by remember { mutableStateOf(userProfile.name) }
      var editDob by remember { mutableStateOf(formatToDdMmYyyy(userProfile.dob)) }
      var editGender by remember { mutableStateOf(userProfile.gender) }
      var editPronouns by remember { mutableStateOf(userProfile.pronouns) }
      var editCity by remember { mutableStateOf(userProfile.currentLocationCity) }
      var editHometown by remember { mutableStateOf(userProfile.hometown) }
      var editBio by remember { mutableStateOf(userProfile.bio) }

      var showGenderDialog by remember { mutableStateOf(false) }
      var showPronounsDialog by remember { mutableStateOf(false) }
      var isDetectingLocation by remember { mutableStateOf(false) }

      // Auto-calculated age from DOB string
      val calculatedAge by remember(editDob) {
        derivedStateOf { calculateAgeFromDobString(editDob) }
      }

      // DatePickerDialog setup
      val calendar = remember(editDob) { parseDobToCalendar(editDob) }
      val datePickerDialog = remember(context, calendar) {
        DatePickerDialog(
          context,
          { _, year, month, dayOfMonth ->
            editDob = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
          },
          calendar.get(Calendar.YEAR),
          calendar.get(Calendar.MONTH),
          calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
          val maxCal = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }
          datePicker.maxDate = maxCal.timeInMillis
        }
      }

      // Location permission launcher
      val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
      ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        coroutineScope.launch {
          isDetectingLocation = true
          try {
            val res = LocationHelper.getCurrentLocation(context)
            if (res.city.isNotBlank()) {
              val locStr = if (res.country.isNotBlank()) "${res.city}, ${res.country}" else res.city
              editCity = locStr
              Toast.makeText(context, "📍 GPS Location set: $locStr", Toast.LENGTH_SHORT).show()
            } else {
              Toast.makeText(context, "Location detection timed out. Please try again.", Toast.LENGTH_SHORT).show()
            }
          } catch (e: Exception) {
            Toast.makeText(context, "Could not detect location: ${e.message}", Toast.LENGTH_SHORT).show()
          } finally {
            isDetectingLocation = false
          }
        }
      }

      val triggerLocationDetection = {
        if (LocationHelper.hasLocationPermission(context)) {
          coroutineScope.launch {
            isDetectingLocation = true
            try {
              val res = LocationHelper.getCurrentLocation(context)
              if (res.city.isNotBlank()) {
                val locStr = if (res.country.isNotBlank()) "${res.city}, ${res.country}" else res.city
                editCity = locStr
                Toast.makeText(context, "📍 GPS Location set: $locStr", Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, "Location detection timed out. Please try again.", Toast.LENGTH_SHORT).show()
              }
            } catch (e: Exception) {
              Toast.makeText(context, "Could not detect location: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
              isDetectingLocation = false
            }
          }
        } else {
          locationPermissionLauncher.launch(
            arrayOf(
              android.Manifest.permission.ACCESS_FINE_LOCATION,
              android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
          )
        }
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("Edit Basic Info", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        // Full Name Field
        OutlinedTextField(
          value = editName,
          onValueChange = { editName = it },
          label = { Text("Full Name") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoralPrimary)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // DOB & Age Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // DOB Selection Field (dd/mm/yyyy)
          Box(
            modifier = Modifier
              .weight(1.4f)
              .clickable { datePickerDialog.show() }
          ) {
            OutlinedTextField(
              value = editDob,
              onValueChange = {},
              readOnly = true,
              enabled = false,
              label = { Text("Date of Birth") },
              placeholder = { Text("DD/MM/YYYY") },
              trailingIcon = {
                Icon(
                  imageVector = Icons.Filled.CalendarToday,
                  contentDescription = "Select Date",
                  tint = CoralPrimary,
                  modifier = Modifier.size(18.dp)
                )
              },
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                disabledTrailingIconColor = CoralPrimary
              )
            )
          }

          // Age Field (Auto-fetched from DOB, Disabled)
          val displayedAgeValue = when {
            calculatedAge > 0 -> "$calculatedAge"
            userProfile.age > 0 -> "${userProfile.age}"
            else -> ""
          }

          OutlinedTextField(
            value = displayedAgeValue,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Age") },
            placeholder = { Text("Auto") },
            modifier = Modifier.weight(0.8f),
            colors = OutlinedTextFieldDefaults.colors(
              disabledTextColor = MaterialTheme.colorScheme.onSurface,
              disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
              disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
              disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Gender & Pronouns Row (Clickable selection dialogs)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Gender Field
          Box(
            modifier = Modifier
              .weight(1f)
              .clickable { showGenderDialog = true }
          ) {
            OutlinedTextField(
              value = editGender,
              onValueChange = {},
              readOnly = true,
              enabled = false,
              label = { Text("Gender") },
              placeholder = { Text("Select") },
              trailingIcon = {
                Icon(
                  imageVector = Icons.Filled.ArrowDropDown,
                  contentDescription = "Select Gender",
                  tint = CoralPrimary
                )
              },
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                disabledTrailingIconColor = CoralPrimary
              )
            )
          }

          // Pronouns Field
          Box(
            modifier = Modifier
              .weight(1f)
              .clickable { showPronounsDialog = true }
          ) {
            OutlinedTextField(
              value = editPronouns,
              onValueChange = {},
              readOnly = true,
              enabled = false,
              label = { Text("Pronouns") },
              placeholder = { Text("Select") },
              trailingIcon = {
                Icon(
                  imageVector = Icons.Filled.ArrowDropDown,
                  contentDescription = "Select Pronouns",
                  tint = CoralPrimary
                )
              },
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                disabledTrailingIconColor = CoralPrimary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Current City / Location Field (Clickable GPS auto-detect, disabled for typing)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              if (!isDetectingLocation) {
                triggerLocationDetection()
              }
            }
        ) {
          OutlinedTextField(
            value = if (isDetectingLocation) "Detecting GPS location..." else editCity,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Current City / Location") },
            placeholder = { Text("Tap to auto-detect GPS location") },
            trailingIcon = {
              if (isDetectingLocation) {
                CircularProgressIndicator(
                  modifier = Modifier.size(18.dp),
                  color = CoralPrimary,
                  strokeWidth = 2.dp
                )
              } else {
                Icon(
                  imageVector = Icons.Filled.MyLocation,
                  contentDescription = "Detect Location",
                  tint = CoralPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              disabledTextColor = MaterialTheme.colorScheme.onSurface,
              disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
              disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
              disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              disabledTrailingIconColor = CoralPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hometown Field
        OutlinedTextField(
          value = editHometown,
          onValueChange = { editHometown = it },
          label = { Text("Hometown") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoralPrimary)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Bio Field
        OutlinedTextField(
          value = editBio,
          onValueChange = { editBio = it },
          label = { Text("About Me / Bio") },
          minLines = 3,
          maxLines = 5,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoralPrimary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            val finalAge = if (calculatedAge > 0) calculatedAge else userProfile.age
            val updated = userProfile.copy(
              name = editName.trim(),
              age = finalAge,
              dob = editDob.trim(),
              gender = editGender.trim(),
              pronouns = editPronouns.trim(),
              currentLocationCity = editCity.trim(),
              hometown = editHometown.trim(),
              bio = editBio.trim()
            )
            onSaveProfile(updated)
            showBasicInfoSheet = false
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Gender Selection Dialog
      if (showGenderDialog) {
        AlertDialog(
          onDismissRequest = { showGenderDialog = false },
          title = {
            Text("Select Gender", fontWeight = FontWeight.Bold, fontSize = 17.sp)
          },
          text = {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              GenderOptionsList.forEach { option ->
                val isSelected = editGender.equals(option, ignoreCase = true)
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) CoralPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                  border = if (isSelected) BorderStroke(1.5.dp, CoralPrimary) else null,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      editGender = option
                      showGenderDialog = false
                    }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = option,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface,
                      fontSize = 14.sp
                    )
                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }
              }
            }
          },
          confirmButton = {},
          dismissButton = {
            TextButton(onClick = { showGenderDialog = false }) {
              Text("Cancel", color = CoralPrimary)
            }
          }
        )
      }

      // Pronouns Selection Dialog
      if (showPronounsDialog) {
        AlertDialog(
          onDismissRequest = { showPronounsDialog = false },
          title = {
            Text("Select Pronouns", fontWeight = FontWeight.Bold, fontSize = 17.sp)
          },
          text = {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              PronounsOptionsList.forEach { option ->
                val isSelected = editPronouns.equals(option, ignoreCase = true)
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) CoralPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                  border = if (isSelected) BorderStroke(1.5.dp, CoralPrimary) else null,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      editPronouns = option
                      showPronounsDialog = false
                    }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = option,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface,
                      fontSize = 14.sp
                    )
                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }
              }
            }
          },
          confirmButton = {},
          dismissButton = {
            TextButton(onClick = { showPronounsDialog = false }) {
              Text("Cancel", color = CoralPrimary)
            }
          }
        )
      }
    }
  }

  // 3. Edit Work & Education Sheet
  if (showWorkEducationSheet) {
    ModalBottomSheet(
      onDismissRequest = { showWorkEducationSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      var editOccupation by remember { mutableStateOf(userProfile.occupation) }
      var editEducation by remember { mutableStateOf(userProfile.education) }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("Edit Work & Education", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = editOccupation,
          onValueChange = { editOccupation = it },
          label = { Text("Occupation / Job Title") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoralPrimary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = editEducation,
          onValueChange = { editEducation = it },
          label = { Text("College / University / Degree") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoralPrimary)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            val updated = userProfile.copy(
              occupation = editOccupation.trim(),
              education = editEducation.trim()
            )
            onSaveProfile(updated)
            showWorkEducationSheet = false
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // 4. Edit Dating Goals & Lifestyle Sheet
  if (showIntentionsLifestyleSheet) {
    ModalBottomSheet(
      onDismissRequest = { showIntentionsLifestyleSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      var editIntention by remember { mutableStateOf(userProfile.datingIntention) }
      var editHeight by remember { mutableStateOf(userProfile.height) }
      var editZodiac by remember { mutableStateOf(userProfile.zodiac) }
      var editDrinking by remember { mutableStateOf(userProfile.drinking) }
      var editSmoking by remember { mutableStateOf(userProfile.smoking) }
      var editPets by remember { mutableStateOf(userProfile.pets) }
      var showHeightDialog by remember { mutableStateOf(false) }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("Edit Dating Goals & Lifestyle", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        // Relationship Goals Picker (Multiple pills per row, random/varied, highlighted)
        Text("Dating Intention", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
        Spacer(modifier = Modifier.height(5.dp))
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
          AllDatingIntentionsList.forEach { intention ->
            val isSelected = editIntention.equals(intention, ignoreCase = true) ||
              (editIntention.isNotBlank() && (intention.startsWith(editIntention.take(6), ignoreCase = true) || editIntention.startsWith(intention.take(6), ignoreCase = true)))
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = if (isSelected) BorderStroke(1.dp, CoralPrimary) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
              modifier = Modifier.clickable { editIntention = if (isSelected) "" else intention }
            ) {
              Text(
                text = intention,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Height Field (Clickable selection dialog)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showHeightDialog = true }
        ) {
          OutlinedTextField(
            value = editHeight,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Height") },
            placeholder = { Text("Select height") },
            trailingIcon = {
              Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Select Height",
                tint = CoralPrimary
              )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              disabledTextColor = MaterialTheme.colorScheme.onSurface,
              disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
              disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
              disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              disabledTrailingIconColor = CoralPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Zodiac Sign Picker (Reduced gap between pills)
        Text("Zodiac Sign", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
        Spacer(modifier = Modifier.height(5.dp))
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          AllZodiacList.forEach { zodiac ->
            val isSelected = editZodiac.equals(zodiac, ignoreCase = true) ||
              (editZodiac.isNotBlank() && (zodiac.startsWith(editZodiac.take(4), ignoreCase = true) || editZodiac.startsWith(zodiac.take(4), ignoreCase = true)))
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = if (isSelected) BorderStroke(1.dp, CoralPrimary) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
              modifier = Modifier.clickable { editZodiac = if (isSelected) "" else zodiac }
            ) {
              Text(
                text = zodiac,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Drinking Habits (Reduced gap between pills, highlighted)
        Text("Drinking Habit", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
        Spacer(modifier = Modifier.height(5.dp))
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          DrinkingHabitsList.forEach { drink ->
            val isSelected = editDrinking.equals(drink, ignoreCase = true) ||
              (editDrinking.isNotBlank() && (drink.startsWith(editDrinking.take(6), ignoreCase = true) || editDrinking.startsWith(drink.take(6), ignoreCase = true)))
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = if (isSelected) BorderStroke(1.dp, CoralPrimary) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
              modifier = Modifier.clickable { editDrinking = if (isSelected) "" else drink }
            ) {
              Text(
                text = drink,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Smoking Habits (Reduced gap between pills, highlighted)
        Text("Smoking Habit", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
        Spacer(modifier = Modifier.height(5.dp))
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          SmokingHabitsList.forEach { smoke ->
            val isSelected = editSmoking.equals(smoke, ignoreCase = true) ||
              (editSmoking.isNotBlank() && (smoke.startsWith(editSmoking.take(6), ignoreCase = true) || editSmoking.startsWith(smoke.take(6), ignoreCase = true)))
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = if (isSelected) BorderStroke(1.dp, CoralPrimary) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
              modifier = Modifier.clickable { editSmoking = if (isSelected) "" else smoke }
            ) {
              Text(
                text = smoke,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pets Section (Reduced gap between pills, highlighted)
        Text("Pets", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
        Spacer(modifier = Modifier.height(5.dp))
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          PetsHabitsList.forEach { pet ->
            val isSelected = editPets.equals(pet, ignoreCase = true) ||
              (editPets.isNotBlank() && (pet.startsWith(editPets.take(4), ignoreCase = true) || editPets.startsWith(pet.take(4), ignoreCase = true)))
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = if (isSelected) BorderStroke(1.dp, CoralPrimary) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
              modifier = Modifier.clickable { editPets = if (isSelected) "" else pet }
            ) {
              Text(
                text = pet,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            val updated = userProfile.copy(
              datingIntention = editIntention.trim(),
              height = editHeight.trim(),
              zodiac = editZodiac.trim(),
              drinking = editDrinking.trim(),
              smoking = editSmoking.trim(),
              pets = editPets.trim()
            )
            onSaveProfile(updated)
            showIntentionsLifestyleSheet = false
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Height Selection Dialog
      if (showHeightDialog) {
        AlertDialog(
          onDismissRequest = { showHeightDialog = false },
          title = {
            Text("Select Height", fontWeight = FontWeight.Bold, fontSize = 17.sp)
          },
          text = {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              HeightOptionsList.forEach { option ->
                val isSelected = editHeight.equals(option, ignoreCase = true) ||
                  (editHeight.isNotBlank() && (option.startsWith(editHeight.take(4), ignoreCase = true) || editHeight.startsWith(option.take(4), ignoreCase = true)))
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) CoralPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                  border = if (isSelected) BorderStroke(1.5.dp, CoralPrimary) else null,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      editHeight = option
                      showHeightDialog = false
                    }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = option,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface,
                      fontSize = 14.sp
                    )
                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }
              }
            }
          },
          confirmButton = {},
          dismissButton = {
            TextButton(onClick = { showHeightDialog = false }) {
              Text("Cancel", color = CoralPrimary)
            }
          }
        )
      }
    }
  }

  // 5. Edit Interests & Passions Sheet
  if (showInterestsSheet) {
    ModalBottomSheet(
      onDismissRequest = { showInterestsSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      var currentSelections by remember {
        mutableStateOf(userProfile.passions)
      }
      var customInterestInput by remember { mutableStateOf("") }

      val allAvailableInterests = remember(currentSelections) {
        val presetNames = AllPresetInterests.map { it.name }
        val customOnes = currentSelections.filterNot { sel -> presetNames.any { it.equals(sel, ignoreCase = true) } }
        AllPresetInterests.map { it.name to it.iconEmoji } + customOnes.map { it to "✨" }
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("Select Your Interests", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
          "Pick your favorite topics to connect with like-minded people.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          allAvailableInterests.forEach { (name, emoji) ->
            val isSelected = currentSelections.any { it.equals(name, ignoreCase = true) }
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = if (isSelected) BorderStroke(1.dp, CoralPrimary) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
              modifier = Modifier.clickable {
                currentSelections = if (isSelected) {
                  currentSelections.filterNot { it.equals(name, ignoreCase = true) }
                } else {
                  currentSelections + name
                }
              }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = emoji, fontSize = 11.5.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = name,
                  fontSize = 11.5.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add custom interest
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = customInterestInput,
            onValueChange = { customInterestInput = it },
            placeholder = { Text("Add custom interest...") },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoralPrimary)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              val trimmed = customInterestInput.trim()
              if (trimmed.isNotBlank() && !currentSelections.any { it.equals(trimmed, ignoreCase = true) }) {
                currentSelections = currentSelections + trimmed
                customInterestInput = ""
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Add")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            val updated = userProfile.copy(passions = currentSelections)
            onSaveProfile(updated)
            showInterestsSheet = false
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Text("Save Interests (${currentSelections.size} selected)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(18.dp))
      }
    }
  }

  // 6. Edit Prompts Sheet
  if (showPromptsSheet) {
    ModalBottomSheet(
      onDismissRequest = { showPromptsSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      var editQuestion by remember { mutableStateOf(userProfile.promptQuestion.ifBlank { PromptQuestionList.first() }) }
      var editAnswer by remember { mutableStateOf(userProfile.promptAnswer) }
      var showQuestionPickerDialog by remember { mutableStateOf(false) }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("Edit Profile Prompt", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
          "Choose a prompt question and share your unique personality.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Prompt Question Selector Field (Clickable to open option list)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showQuestionPickerDialog = true }
        ) {
          OutlinedTextField(
            value = editQuestion,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Prompt Question") },
            placeholder = { Text("Select a question") },
            trailingIcon = {
              Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Select Question",
                tint = CoralPrimary
              )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              disabledTextColor = MaterialTheme.colorScheme.onSurface,
              disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
              disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
              disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              disabledTrailingIconColor = CoralPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = editAnswer,
          onValueChange = { editAnswer = it },
          label = { Text("Your Answer") },
          placeholder = { Text("Write something genuine, witty, or intriguing...") },
          minLines = 3,
          maxLines = 5,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoralPrimary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            val updated = userProfile.copy(
              promptQuestion = editQuestion.trim(),
              promptAnswer = editAnswer.trim()
            )
            onSaveProfile(updated)
            showPromptsSheet = false
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Text("Save Prompt", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Question Selection Dialog
      if (showQuestionPickerDialog) {
        AlertDialog(
          onDismissRequest = { showQuestionPickerDialog = false },
          title = {
            Text("Select Prompt Question", fontWeight = FontWeight.Bold, fontSize = 17.sp)
          },
          text = {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              PromptQuestionList.forEach { q ->
                val isSelected = editQuestion == q
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) CoralPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                  border = if (isSelected) BorderStroke(1.5.dp, CoralPrimary) else null,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      editQuestion = q
                      showQuestionPickerDialog = false
                    }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = q,
                      fontSize = 13.5.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                      Spacer(modifier = Modifier.width(8.dp))
                      Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }
              }
            }
          },
          confirmButton = {},
          dismissButton = {
            TextButton(onClick = { showQuestionPickerDialog = false }) {
              Text("Cancel", color = CoralPrimary)
            }
          }
        )
      }
    }
  }

  // 7. Edit Contact Info Sheet (Phone, Email)
  if (showContactInfoSheet) {
    ModalBottomSheet(
      onDismissRequest = { showContactInfoSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      var editEmail by remember { mutableStateOf(userProfile.email) }
      var emailError by remember { mutableStateOf<String?>(null) }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("Contact & Account Info", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        // Phone Info (read-only / verified)
        OutlinedTextField(
          value = "${userProfile.countryCode} ${userProfile.phoneNumber}",
          onValueChange = {},
          readOnly = true,
          enabled = false,
          label = { Text("Registered Mobile Number") },
          trailingIcon = {
            if (userProfile.isPhoneVerified) {
              Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = "Verified Mobile",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(22.dp)
              )
            }
          },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Email Field (Editable with format validation)
        OutlinedTextField(
          value = editEmail,
          onValueChange = {
            editEmail = it
            emailError = null
          },
          label = { Text("Email Address") },
          placeholder = { Text("example@domain.com") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          isError = emailError != null,
          supportingText = {
            if (emailError != null) {
              Text(
                text = emailError ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
              )
            }
          },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CoralPrimary,
            errorBorderColor = MaterialTheme.colorScheme.error
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            val trimmedEmail = editEmail.trim()
            if (trimmedEmail.isNotBlank()) {
              val emailPattern = android.util.Patterns.EMAIL_ADDRESS
              if (!emailPattern.matcher(trimmedEmail).matches()) {
                emailError = "Please enter a valid email format (e.g. name@domain.com)"
                Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@Button
              }
            }
            val updated = userProfile.copy(email = trimmedEmail)
            onSaveProfile(updated)
            showContactInfoSheet = false
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Text("Save Contact Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(18.dp))
      }
    }
  }

  // 8. Manage Photos Sheet
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
          "Your first photo is your main profile avatar.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))

        val currentPhotos = userProfile.photos.filter { it.isNotBlank() }

        if (currentPhotos.isEmpty()) {
          Text(
            text = "No photos added yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          currentPhotos.forEachIndexed { index, photoUrl ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
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
                    color = if (index == 0) CoralPrimary else MaterialTheme.colorScheme.onSurface
                  )
                  if (index != 0) {
                    Text(
                      text = "Make Primary",
                      fontSize = 12.sp,
                      color = CoralPrimary,
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
                    tint = MaterialTheme.colorScheme.error
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = { showPhotoChoiceDialog = true },
          enabled = !isUploadingPhoto,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = CoralPrimary,
            disabledContainerColor = CoralPrimary.copy(alpha = 0.6f)
          )
        ) {
          if (isUploadingPhoto) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = Color.White,
              strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Uploading Photo...", fontWeight = FontWeight.Bold, color = Color.White)
          } else {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add New Photo", fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // 9. Dating Preferences Sheet
  if (showPreferencesSheet) {
    com.example.ui.components.DiscoveryPreferencesBottomSheet(
      userProfile = userProfile,
      subscriptionState = subscriptionState,
      onSavePreferences = { updated ->
        onSaveProfile(updated)
        Toast.makeText(context, "Discovery preferences saved ✨", Toast.LENGTH_SHORT).show()
      },
      onOpenPaywall = onOpenPaywall,
      onDismiss = { showPreferencesSheet = false }
    )
  }

  // 10. Settings Modal Sheet (WITHOUT "Update Complete Profile")
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
              tint = CoralPrimary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("Dark Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text("Toggle dark appearance", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          Switch(
            checked = themeMode == AppThemeMode.DARK,
            onCheckedChange = { isDark ->
              onThemeModeChange(if (isDark) AppThemeMode.DARK else AppThemeMode.LIGHT)
            },
            colors = SwitchDefaults.colors(checkedThumbColor = CoralPrimary)
          )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

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
            imageVector = Icons.Filled.PauseCircle,
            contentDescription = null,
            tint = CoralPrimary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (userProfile.isAccountDisabled) "Unpause Profile" else "Pause Profile",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              text = if (userProfile.isAccountDisabled) "Make your profile discoverable again" else "Temporarily hide your profile from Discover",
              fontSize = 11.5.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

        // Delete Account
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              deleteAccountInputText = ""
              showDeleteAccountConfirmDialog = true
            }
            .padding(vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.DeleteForever,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Delete Account",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.error
            )
            Text(
              text = "Permanently remove your profile and all associated data",
              fontSize = 11.5.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

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
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text("Log Out", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Delete Account Confirmation Dialog with Text Field Verification
  if (showDeleteAccountConfirmDialog) {
    val isDeleteConfirmed = deleteAccountInputText.trim().equals("delete", ignoreCase = true)
    AlertDialog(
      onDismissRequest = {
        showDeleteAccountConfirmDialog = false
        deleteAccountInputText = ""
      },
      icon = {
        Icon(
          imageVector = Icons.Filled.WarningAmber,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error,
          modifier = Modifier.size(32.dp)
        )
      },
      title = {
        Text(
          "Delete Account?",
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.error
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "Deleting your account is permanent. All your matches, chats, likes, and profile data will be permanently wiped from the database and you will no longer appear in Discover or Matches.",
            fontSize = 13.5.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "To confirm deletion, please type \"delete\" below:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = deleteAccountInputText,
            onValueChange = { deleteAccountInputText = it },
            placeholder = { Text("type 'delete'") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.error,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showDeleteAccountConfirmDialog = false
            showSettingsSheet = false
            deleteAccountInputText = ""
            onDeleteAccount()
          },
          enabled = isDeleteConfirmed,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.35f),
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.5f)
          )
        ) {
          Text("Delete Profile", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showDeleteAccountConfirmDialog = false
            deleteAccountInputText = ""
          }
        ) {
          Text("Cancel")
        }
      }
    )
  }

  // 11. Dynamic Notifications Bottom Sheet
  if (showNotificationsDialog) {
    NotificationsBottomSheet(
      notifications = notifications,
      unreadCount = unreadNotificationCount,
      onDismiss = { showNotificationsDialog = false },
      onMarkRead = onMarkNotificationRead,
      onMarkAllRead = onMarkAllNotificationsRead,
      onDelete = onDeleteNotification,
      onClearAll = onClearAllNotifications,
      onNavigateToTarget = { target ->
        showNotificationsDialog = false
        if (target.startsWith("chat")) {
          onNavigateToChat()
        }
      }
    )
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Component Helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategoryCard(
  title: String,
  actionText: String? = null,
  actionIcon: ImageVector? = null,
  onAction: () -> Unit,
  content: @Composable () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 5.dp),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
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
          text = title,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        if (actionText != null) {
          Text(
            text = actionText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = CoralPrimary,
            modifier = Modifier
              .clickable(onClick = onAction)
              .padding(4.dp)
          )
        } else if (actionIcon != null) {
          Icon(
            imageVector = actionIcon,
            contentDescription = "Edit $title",
            tint = CoralPrimary,
            modifier = Modifier
              .size(18.dp)
              .clickable(onClick = onAction)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      content()
    }
  }
}

@Composable
private fun ProfileAttributeItem(
  icon: ImageVector,
  label: String,
  value: String,
  trailingContent: (@Composable () -> Unit)? = null
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
      tint = CoralPrimary,
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = label,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = value,
        fontSize = 13.5.sp,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium
      )
    }
    if (trailingContent != null) {
      trailingContent()
    }
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
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Normal
      )
      Text(
        text = value,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
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
      .background(MaterialTheme.colorScheme.surfaceVariant)
  )
}
