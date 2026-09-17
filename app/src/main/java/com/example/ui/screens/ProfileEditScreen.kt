package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.SubscriptionState
import com.example.data.model.UserProfile
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.ui.theme.LikeGreen
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.PeachSecondary
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

enum class ProfileTabMode {
  EDIT,
  PREVIEW
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
  userProfile: UserProfile,
  subscriptionState: SubscriptionState,
  themeMode: com.example.ui.theme.AppThemeMode = com.example.ui.theme.AppThemeMode.LIGHT,
  onThemeModeChange: (com.example.ui.theme.AppThemeMode) -> Unit = {},
  onSaveProfile: (UserProfile) -> Unit,
  onAddPhoto: (String) -> Unit,
  onRemovePhoto: (Int) -> Unit,
  onSetPrimaryPhoto: (Int) -> Unit = {},
  onReplacePhoto: (Int, String) -> Unit = { _, _ -> },
  onOpenPaywall: () -> Unit = {},
  onRestartOnboarding: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var currentTab by remember { mutableStateOf(ProfileTabMode.EDIT) }

  // Form State
  var name by remember(userProfile.name) { mutableStateOf(userProfile.name) }
  var ageText by remember(userProfile.age) { mutableStateOf(if (userProfile.age > 0) userProfile.age.toString() else "") }
  var gender by remember(userProfile.gender) { mutableStateOf(userProfile.gender) }
  var pronouns by remember(userProfile.pronouns) { mutableStateOf(userProfile.pronouns) }
  var bio by remember(userProfile.bio) { mutableStateOf(userProfile.bio) }
  var occupation by remember(userProfile.occupation) { mutableStateOf(userProfile.occupation) }
  var education by remember(userProfile.education) { mutableStateOf(userProfile.education) }
  var hometown by remember(userProfile.hometown) { mutableStateOf(userProfile.hometown) }
  var height by remember(userProfile.height) { mutableStateOf(userProfile.height) }
  var zodiac by remember(userProfile.zodiac) { mutableStateOf(userProfile.zodiac) }
  var datingIntention by remember(userProfile.datingIntention) { mutableStateOf(userProfile.datingIntention) }
  var drinking by remember(userProfile.drinking) { mutableStateOf(userProfile.drinking) }
  var smoking by remember(userProfile.smoking) { mutableStateOf(userProfile.smoking) }
  var pets by remember(userProfile.pets) { mutableStateOf(userProfile.pets) }
  var promptQuestion by remember(userProfile.promptQuestion) { mutableStateOf(userProfile.promptQuestion) }
  var promptAnswer by remember(userProfile.promptAnswer) { mutableStateOf(userProfile.promptAnswer) }
  var selectedPassions by remember(userProfile.passions) { mutableStateOf(userProfile.passions) }

  // Photo management dialog state
  var activePhotoSlotForAdd by remember { mutableStateOf<Int?>(null) }
  var activePhotoIndexForManage by remember { mutableStateOf<Int?>(null) }

  // Gallery Picker Launcher
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let {
      val targetSlot = activePhotoSlotForAdd
      if (targetSlot != null && targetSlot < userProfile.photos.size) {
        onReplacePhoto(targetSlot, it.toString())
      } else {
        onAddPhoto(it.toString())
      }
      activePhotoSlotForAdd = null
      Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
    }
  }

  // Camera Capture Launcher
  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    bitmap?.let {
      val savedUri = saveBitmapToInternalStorage(context, it)
      if (savedUri != null) {
        val targetSlot = activePhotoSlotForAdd
        if (targetSlot != null && targetSlot < userProfile.photos.size) {
          onReplacePhoto(targetSlot, savedUri.toString())
        } else {
          onAddPhoto(savedUri.toString())
        }
        activePhotoSlotForAdd = null
        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
      }
    }
  }

  // Camera Permission Launcher
  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (isGranted) {
      cameraLauncher.launch()
    } else {
      Toast.makeText(context, "Camera permission needed to take photos", Toast.LENGTH_SHORT).show()
    }
  }

  // Calculate live profile strength
  val profileStrength by remember(userProfile.photos, name, ageText, bio, occupation, education, selectedPassions, promptAnswer) {
    derivedStateOf {
      var score = 0
      if (userProfile.photos.isNotEmpty()) score += 25
      if (userProfile.photos.size >= 3) score += 15
      if (userProfile.photos.size >= 5) score += 10
      if (name.isNotBlank() && (ageText.toIntOrNull() ?: 0) > 0) score += 10
      if (bio.isNotBlank() && bio.length >= 15) score += 15
      if (selectedPassions.size >= 3) score += 10
      if (promptAnswer.isNotBlank()) score += 10
      if (occupation.isNotBlank() || education.isNotBlank()) score += 5
      score.coerceIn(0, 100)
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // AUTOSAVE LOGIC with Debounce (750ms)
  // ─────────────────────────────────────────────────────────────────────────
  var isInitialMount by remember { mutableStateOf(true) }
  LaunchedEffect(
    name,
    ageText,
    gender,
    pronouns,
    bio,
    occupation,
    education,
    hometown,
    height,
    zodiac,
    datingIntention,
    drinking,
    smoking,
    pets,
    promptQuestion,
    promptAnswer,
    selectedPassions
  ) {
    if (isInitialMount) {
      isInitialMount = false
      return@LaunchedEffect
    }

    delay(750) // Debounce autosave

    val parsedAge = ageText.toIntOrNull() ?: userProfile.age
    val updated = userProfile.copy(
      name = name.trim(),
      age = parsedAge,
      gender = gender,
      pronouns = pronouns,
      bio = bio.trim(),
      occupation = occupation.trim(),
      education = education.trim(),
      hometown = hometown.trim(),
      height = height,
      zodiac = zodiac,
      datingIntention = datingIntention,
      drinking = drinking,
      smoking = smoking,
      pets = pets,
      promptQuestion = promptQuestion,
      promptAnswer = promptAnswer.trim(),
      passions = selectedPassions
    )

    if (updated != userProfile) {
      onSaveProfile(updated)
      Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
    }
  }

  // Options & Constants
  val intentionsList = listOf(
    "Long-term relationship 💖",
    "Casual dating ✨",
    "Marriage partner 💍",
    "New friends ☕",
    "Still figuring it out 🤔"
  )

  val zodiacOptions = listOf(
    "Aries ♈", "Taurus ♉", "Gemini ♊", "Cancer ♋", "Leo ♌", "Virgo ♍",
    "Libra ♎", "Scorpio ♏", "Sagittarius ♐", "Capricorn ♑", "Aquarius ♒", "Pisces ♓"
  )

  val drinkingOptions = listOf("Frequently 🍸", "Socially 🍷", "Rarely 🍻", "Never 🚫")
  val smokingOptions = listOf("Socially 🚬", "Never 🚭", "Regularly 💨", "Trying to quit 🌱")
  val petsOptions = listOf("Have dog(s) 🐕", "Have cat(s) 🐾", "Love all pets 🐾", "No pets 🚫")

  val passionCategories = listOf(
    "🎨 Arts & Culture" to listOf("Photography", "Art Galleries", "Vinyl Records", "Film", "Writing", "Design"),
    "☕ Food & Drink" to listOf("Coffee", "Cooking", "Baking", "Cocktails", "Wine Tasting", "Street Food"),
    "🏃 Active & Outdoors" to listOf("Hiking", "Yoga", "Running", "Gym", "Swimming", "Cycling"),
    "🎮 Entertainment" to listOf("Gaming", "Anime", "Indie Pop", "Music Festivals", "Board Games"),
    "🌿 Lifestyle & Pets" to listOf("Cats", "Dogs", "Travel", "Reading", "Plants", "Astrology")
  )

  val promptSuggestions = listOf(
    "My simple pleasures in life...",
    "Two truths and a lie...",
    "I get along best with people who...",
    "The best way to ask me out is...",
    "Together, we could..."
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("profile_edit_screen")
  ) {
    // Segmented Tab Row (Edit Profile vs Preview)
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      TabRow(
        selectedTabIndex = currentTab.ordinal,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = CoralPrimary,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
            color = CoralPrimary,
            height = 3.dp
          )
        }
      ) {
        Tab(
          selected = currentTab == ProfileTabMode.EDIT,
          onClick = { currentTab = ProfileTabMode.EDIT },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Profile Management", fontWeight = FontWeight.Bold)
            }
          }
        )
        Tab(
          selected = currentTab == ProfileTabMode.PREVIEW,
          onClick = { currentTab = ProfileTabMode.PREVIEW },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Preview Card", fontWeight = FontWeight.Bold)
            }
          }
        )
      }
    }

    if (currentTab == ProfileTabMode.PREVIEW) {
      ProfilePreviewView(
        userProfile = userProfile.copy(
          name = name.ifBlank { "User" },
          age = ageText.toIntOrNull() ?: userProfile.age,
          bio = bio,
          occupation = occupation,
          education = education,
          hometown = hometown,
          passions = selectedPassions,
          promptQuestion = promptQuestion,
          promptAnswer = promptAnswer
        )
      )
    } else {
      // ─────────────────────────────────────────────────────────────────────
      // EDIT MODE
      // ─────────────────────────────────────────────────────────────────────
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {

        // ───────────────────────────────────────────────────────────────────
        // 1. PROFILE PICTURE WITH PROFILE STRENGTH BORDER & COMPLETION %
        // ───────────────────────────────────────────────────────────────────
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.size(136.dp)
            ) {
              // Circular Profile Strength Progress Border
              Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 6.dp.toPx()
                // Track background
                drawCircle(
                  color = Color(0xFFEEEEEE),
                  style = Stroke(width = strokeWidth)
                )
                // Strength Arc
                val sweepAngle = (profileStrength / 100f) * 360f
                drawArc(
                  brush = Brush.sweepGradient(
                    listOf(CoralPrimary, GoldVip, CoralDark, CoralPrimary)
                  ),
                  startAngle = -90f,
                  sweepAngle = sweepAngle,
                  useCenter = false,
                  style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
              }

              // Avatar Image inside the circular frame
              Box(
                modifier = Modifier
                  .size(114.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .clickable {
                    activePhotoSlotForAdd = 0
                    galleryLauncher.launch(
                      androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                      )
                    )
                  },
                contentAlignment = Alignment.Center
              ) {
                if (userProfile.photos.isNotEmpty()) {
                  AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                      .data(userProfile.photos.first())
                      .crossfade(true)
                      .build(),
                    contentDescription = "Primary Profile Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Add Photo",
                    tint = CoralPrimary,
                    modifier = Modifier.size(40.dp)
                  )
                }

                // Camera icon overlay
                Box(
                  modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(CoralPrimary),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Photo",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Profile Strength Badge & Completion Indicator
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (profileStrength >= 80) LikeGreen.copy(alpha = 0.15f) else PeachBlush)
                .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
              Icon(
                imageVector = if (profileStrength >= 80) Icons.Default.Check else Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = if (profileStrength >= 80) LikeGreen else CoralPrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Profile Strength: $profileStrength%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (profileStrength >= 80) LikeGreen else CoralDark
              )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = if (profileStrength < 80) "Add more photos & details for 3x more matches" else "Great profile! Ready to discover matches.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ─────────────────────────────────────────────────────────────
            // 2. BASIC DETAILS & ABOUT ME DISPLAYED JUST BELOW PROFILE PHOTO
            // ─────────────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Basic Details & About Me",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name & Age Row (Reduced Padding)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              CompactTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                modifier = Modifier.weight(1.8f),
                testTag = "input_name"
              )

              CompactTextField(
                value = ageText,
                onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) ageText = it },
                label = "Age",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
                testTag = "input_age"
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Gender & Pronouns Row (Reduced Padding)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              CompactTextField(
                value = gender,
                onValueChange = { gender = it },
                label = "Gender",
                placeholder = "e.g. Woman, Man, Non-binary",
                modifier = Modifier.weight(1f),
                testTag = "input_gender"
              )

              CompactTextField(
                value = pronouns,
                onValueChange = { pronouns = it },
                label = "Pronouns",
                placeholder = "e.g. She/Her, He/Him",
                modifier = Modifier.weight(1f),
                testTag = "input_pronouns"
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Hometown & City (Reduced Padding)
            CompactTextField(
              value = hometown,
              onValueChange = { hometown = it },
              label = "Hometown / Current City",
              placeholder = "e.g. Mumbai, Maharashtra",
              modifier = Modifier.fillMaxWidth(),
              testTag = "input_hometown"
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Occupation & Education (Reduced Padding)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              CompactTextField(
                value = occupation,
                onValueChange = { occupation = it },
                label = "Occupation / Job Title",
                placeholder = "e.g. Designer",
                modifier = Modifier.weight(1f),
                testTag = "input_occupation"
              )

              CompactTextField(
                value = education,
                onValueChange = { education = it },
                label = "Education / College",
                placeholder = "e.g. University",
                modifier = Modifier.weight(1f),
                testTag = "input_education"
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // About Me / Bio Field (Reduced Padding)
            CompactTextField(
              value = bio,
              onValueChange = { if (it.length <= 400) bio = it },
              label = "About Me (Bio)",
              placeholder = "Share what excites you, how you spend weekends, or your ideal date...",
              minLines = 3,
              maxLines = 5,
              modifier = Modifier.fillMaxWidth(),
              testTag = "input_bio"
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              Text(
                text = "${bio.length}/400",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // ───────────────────────────────────────────────────────────────────
        // 3. PHOTOS MANAGEMENT GRID (6 Slots)
        // ───────────────────────────────────────────────────────────────────
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Profile Photos",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Add up to 6 photos. First photo is your main cover.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Text(
                text = "${userProfile.photos.size}/6",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = CoralPrimary
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2 x 3 Photo Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              for (row in 0 until 2) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  for (col in 0 until 3) {
                    val slotIndex = row * 3 + col
                    val photoUri = userProfile.photos.getOrNull(slotIndex)

                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.85f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                          width = if (slotIndex == 0) 2.dp else 1.dp,
                          color = if (slotIndex == 0) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                          shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                          if (photoUri != null) {
                            activePhotoIndexForManage = slotIndex
                          } else {
                            activePhotoSlotForAdd = slotIndex
                            galleryLauncher.launch(
                              androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                              )
                            )
                          }
                        },
                      contentAlignment = Alignment.Center
                    ) {
                      if (photoUri != null) {
                        AsyncImage(
                          model = ImageRequest.Builder(LocalContext.current)
                            .data(photoUri)
                            .crossfade(true)
                            .build(),
                          contentDescription = "Photo ${slotIndex + 1}",
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.fillMaxSize()
                        )

                        // Main cover badge
                        if (slotIndex == 0) {
                          Box(
                            modifier = Modifier
                              .align(Alignment.TopStart)
                              .padding(4.dp)
                              .clip(RoundedCornerShape(6.dp))
                              .background(CoralPrimary)
                              .padding(horizontal = 6.dp, vertical = 2.dp)
                          ) {
                            Text(
                              text = "MAIN",
                              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                              color = Color.White
                            )
                          }
                        }

                        // Remove / manage icon button
                        Box(
                          modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f)),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit photo",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                          )
                        }
                      } else {
                        Column(
                          horizontalAlignment = Alignment.CenterHorizontally,
                          verticalArrangement = Arrangement.Center
                        ) {
                          Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Photo",
                            tint = CoralPrimary,
                            modifier = Modifier.size(24.dp)
                          )
                          Text(
                            text = if (slotIndex == 0) "Cover" else "Add",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoralPrimary
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // ───────────────────────────────────────────────────────────────────
        // 4. LIFESTYLE & DETAILS SECTION (Reduced Padding)
        // ───────────────────────────────────────────────────────────────────
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "Lifestyle & Preferences",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )

            // Dating Intention Dropdown
            DropdownField(
              label = "Dating Intention",
              selectedValue = datingIntention,
              options = intentionsList,
              onSelect = { datingIntention = it }
            )

            // Zodiac & Height Row (Reduced Padding)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(modifier = Modifier.weight(1f)) {
                DropdownField(
                  label = "Zodiac Sign",
                  selectedValue = zodiac,
                  options = zodiacOptions,
                  onSelect = { zodiac = it }
                )
              }

              CompactTextField(
                value = height,
                onValueChange = { height = it },
                label = "Height",
                placeholder = "e.g. 5'9\"",
                modifier = Modifier.weight(1f),
                testTag = "input_height"
              )
            }

            // Drinking, Smoking, Pets
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(modifier = Modifier.weight(1f)) {
                DropdownField(
                  label = "Drinking",
                  selectedValue = drinking,
                  options = drinkingOptions,
                  onSelect = { drinking = it }
                )
              }

              Box(modifier = Modifier.weight(1f)) {
                DropdownField(
                  label = "Smoking",
                  selectedValue = smoking,
                  options = smokingOptions,
                  onSelect = { smoking = it }
                )
              }
            }

            DropdownField(
              label = "Pets",
              selectedValue = pets,
              options = petsOptions,
              onSelect = { pets = it }
            )
          }
        }

        // ───────────────────────────────────────────────────────────────────
        // 5. PASSIONS & INTERESTS
        // ───────────────────────────────────────────────────────────────────
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Passions & Interests",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${selectedPassions.size} selected",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = CoralPrimary
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            passionCategories.forEach { (categoryName, tags) ->
              Text(
                text = categoryName,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
              )

              FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                tags.forEach { tag ->
                  val isSelected = selectedPassions.contains(tag)
                  FilterChip(
                    selected = isSelected,
                    onClick = {
                      val list = selectedPassions.toMutableList()
                      if (isSelected) list.remove(tag) else list.add(tag)
                      selectedPassions = list
                    },
                    label = {
                      Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall
                      )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = CoralPrimary,
                      selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
            }
          }
        }

        // ───────────────────────────────────────────────────────────────────
        // 6. PROMPT QUESTION & ANSWER (Reduced Padding)
        // ───────────────────────────────────────────────────────────────────
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "Personal Icebreaker Prompt",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )

            DropdownField(
              label = "Prompt Topic",
              selectedValue = promptQuestion,
              options = promptSuggestions,
              onSelect = { promptQuestion = it }
            )

            CompactTextField(
              value = promptAnswer,
              onValueChange = { if (it.length <= 250) promptAnswer = it },
              label = "Your Answer",
              placeholder = "Write a playful, genuine response...",
              minLines = 2,
              maxLines = 4,
              modifier = Modifier.fillMaxWidth(),
              testTag = "input_prompt_answer"
            )
          }
        }

        // Subtle Auto-save Indicator Note at Bottom
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = LikeGreen,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Autosaving all changes automatically ✨",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Photo Management Bottom Sheet (Replace / Set as Primary / Delete)
  // ─────────────────────────────────────────────────────────────────────────
  if (activePhotoIndexForManage != null) {
    val photoIdx = activePhotoIndexForManage!!
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
      onDismissRequest = { activePhotoIndexForManage = null },
      sheetState = sheetState
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Manage Photo ${photoIdx + 1}",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        // Make Primary
        if (photoIdx != 0) {
          OutlinedButton(
            onClick = {
              onSetPrimaryPhoto(photoIdx)
              activePhotoIndexForManage = null
              Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = CoralPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Set as Primary Cover Photo")
          }
        }

        // Replace Photo
        OutlinedButton(
          onClick = {
            activePhotoSlotForAdd = photoIdx
            activePhotoIndexForManage = null
            galleryLauncher.launch(
              androidx.activity.result.PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
              )
            )
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Replace from Gallery")
        }

        // Delete Photo
        if (userProfile.photos.size > 1) {
          OutlinedButton(
            onClick = {
              onRemovePhoto(photoIdx)
              activePhotoIndexForManage = null
              Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
              contentColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Remove Photo")
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Compact Text Field with Reduced Padding
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CompactTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  placeholder: String = "",
  minLines: Int = 1,
  maxLines: Int = 1,
  keyboardType: KeyboardType = KeyboardType.Text,
  testTag: String = ""
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label, fontSize = 12.sp) },
    placeholder = if (placeholder.isNotBlank()) { { Text(placeholder, fontSize = 12.sp) } } else null,
    singleLine = maxLines == 1,
    minLines = minLines,
    maxLines = maxLines,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    shape = RoundedCornerShape(10.dp),
    modifier = modifier.testTag(testTag),
    colors = OutlinedTextFieldDefaults.colors(
      focusedBorderColor = CoralPrimary,
      focusedLabelColor = CoralPrimary,
      unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
    )
  )
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Compact Dropdown Field
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
  label: String,
  selectedValue: String,
  options: List<String>,
  onSelect: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded },
    modifier = Modifier.fillMaxWidth()
  ) {
    OutlinedTextField(
      value = selectedValue,
      onValueChange = {},
      readOnly = true,
      label = { Text(label, fontSize = 12.sp) },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      shape = RoundedCornerShape(10.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CoralPrimary,
        focusedLabelColor = CoralPrimary,
        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
      ),
      modifier = Modifier
        .menuAnchor()
        .fillMaxWidth()
    )

    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      options.forEach { option ->
        DropdownMenuItem(
          text = { Text(option, fontSize = 13.sp) },
          onClick = {
            onSelect(option)
            expanded = false
          }
        )
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile Preview Composable
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfilePreviewView(userProfile: UserProfile) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(6.dp, RoundedCornerShape(24.dp)),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column {
        // Main Cover Photo
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
          if (userProfile.photos.isNotEmpty()) {
            AsyncImage(
              model = userProfile.photos.first(),
              contentDescription = userProfile.name,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }

          // Gradient overlay
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                  startY = 400f
                )
              )
          )

          Column(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(18.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "${userProfile.name}, ${userProfile.age}",
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
              )
              if (userProfile.isPhoneVerified) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Default.Verified,
                  contentDescription = "Verified",
                  tint = LikeGreen,
                  modifier = Modifier.size(22.dp)
                )
              }
            }

            if (userProfile.occupation.isNotBlank()) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Work,
                  contentDescription = null,
                  tint = Color.White.copy(alpha = 0.8f),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = userProfile.occupation,
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.White.copy(alpha = 0.9f)
                )
              }
            }

            if (userProfile.hometown.isNotBlank()) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = Color.White.copy(alpha = 0.8f),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = userProfile.hometown,
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.85f)
                )
              }
            }
          }
        }

        // Details content
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          if (userProfile.bio.isNotBlank()) {
            Text(
              text = "About Me",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = CoralPrimary
            )
            Text(
              text = userProfile.bio,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          if (userProfile.passions.isNotEmpty()) {
            Text(
              text = "Passions",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = CoralPrimary
            )
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              userProfile.passions.forEach { passion ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PeachBlush)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = passion,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = CoralDark
                  )
                }
              }
            }
          }

          if (userProfile.promptAnswer.isNotBlank()) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = PeachBlush.copy(alpha = 0.6f))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(
                  text = userProfile.promptQuestion,
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = CoralDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = userProfile.promptAnswer,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }
    }
  }
}

private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): Uri? {
  return try {
    val file = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
      bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    Uri.fromFile(file)
  } catch (e: Exception) {
    null
  }
}
