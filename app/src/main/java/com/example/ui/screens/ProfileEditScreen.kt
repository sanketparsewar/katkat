package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.SubscriptionState
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserProfile
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.PeachSecondary
import com.example.ui.theme.SuperlikeBlue
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.io.File
import java.io.FileOutputStream

enum class ProfileViewTab {
  EDIT,
  PREVIEW
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
  userProfile: UserProfile,
  subscriptionState: SubscriptionState,
  onSaveProfile: (UserProfile) -> Unit,
  onAddPhoto: (String) -> Unit,
  onRemovePhoto: (Int) -> Unit,
  onSetPrimaryPhoto: (Int) -> Unit = {},
  onReplacePhoto: (Int, String) -> Unit = { _, _ -> },
  onOpenPaywall: () -> Unit,
  onRestartOnboarding: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  var currentTab by remember { mutableStateOf(ProfileViewTab.EDIT) }

  // Form State
  var name by remember(userProfile) { mutableStateOf(userProfile.name) }
  var ageText by remember(userProfile) { mutableStateOf(userProfile.age.toString()) }
  var gender by remember(userProfile) { mutableStateOf(userProfile.gender) }
  var pronouns by remember(userProfile) { mutableStateOf(userProfile.pronouns) }
  var bio by remember(userProfile) { mutableStateOf(userProfile.bio) }
  var occupation by remember(userProfile) { mutableStateOf(userProfile.occupation) }
  var education by remember(userProfile) { mutableStateOf(userProfile.education) }
  var hometown by remember(userProfile) { mutableStateOf(userProfile.hometown) }
  var height by remember(userProfile) { mutableStateOf(userProfile.height) }
  var zodiac by remember(userProfile) { mutableStateOf(userProfile.zodiac) }
  var datingIntention by remember(userProfile) { mutableStateOf(userProfile.datingIntention) }
  var drinking by remember(userProfile) { mutableStateOf(userProfile.drinking) }
  var smoking by remember(userProfile) { mutableStateOf(userProfile.smoking) }
  var pets by remember(userProfile) { mutableStateOf(userProfile.pets) }
  var promptQuestion by remember(userProfile) { mutableStateOf(userProfile.promptQuestion) }
  var promptAnswer by remember(userProfile) { mutableStateOf(userProfile.promptAnswer) }
  var selectedPassions by remember(userProfile) { mutableStateOf(userProfile.passions) }

  // Photo management dialogs
  var activePhotoSlotForAdd by remember { mutableStateOf<Int?>(null) }
  var activePhotoIndexForManage by remember { mutableStateOf<Int?>(null) }
  var customPassionInput by remember { mutableStateOf("") }
  var showCustomPassionDialog by remember { mutableStateOf(false) }

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
      Toast.makeText(context, "Photo updated from gallery! 📸", Toast.LENGTH_SHORT).show()
    }
  }

  // Camera Capture Launcher (returns preview bitmap)
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
        Toast.makeText(context, "Photo captured with camera! 📷", Toast.LENGTH_SHORT).show()
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
      Toast.makeText(context, "Camera permission needed to take profile photos", Toast.LENGTH_SHORT).show()
    }
  }

  // Completeness score
  val completionScore by remember(userProfile, bio, selectedPassions, promptAnswer) {
    derivedStateOf {
      var score = 0
      if (userProfile.photos.isNotEmpty()) score += 25
      if (userProfile.photos.size >= 3) score += 15
      if (userProfile.photos.size >= 5) score += 10
      if (bio.isNotBlank() && bio.length > 20) score += 15
      if (selectedPassions.size >= 3) score += 15
      if (promptAnswer.isNotBlank()) score += 10
      if (occupation.isNotBlank() || education.isNotBlank()) score += 10
      score.coerceIn(0, 100)
    }
  }

  val intentionsList = listOf(
    "Long-term relationship 💖",
    "Casual dating ✨",
    "Marriage partner 💍",
    "New friends ☕",
    "Still figuring it out 🤔"
  )

  val promptSuggestions = listOf(
    "My simple pleasures in life...",
    "Two truths and a lie...",
    "I get along best with people who...",
    "The best way to ask me out is...",
    "Together, we could...",
    "A random fact I love is...",
    "My most controversial opinion..."
  )

  val bioStarters = listOf(
    "☕ Coffee fanatic & weekend baker",
    "✈️ Next destination: Japan & Italy",
    "🎧 Always hunting for rare vinyl records",
    "🐕 Proud dog parent & park explorer",
    "🎨 Modern art lover, ramen devotee & runner",
    "🌿 Plant parent with 20+ thriving succulents"
  )

  val passionCategories = listOf(
    "🎨 Arts & Culture" to listOf("Photography", "Art Galleries", "Vinyl Records", "Film", "Writing", "Design", "Architecture"),
    "☕ Food & Drink" to listOf("Coffee", "Cooking", "Baking", "Cocktails", "Boba", "Wine Tasting", "Street Food"),
    "🏃 Active & Outdoors" to listOf("Hiking", "Yoga", "Running", "Climbing", "Swimming", "Cycling", "Surfing", "Gym"),
    "🎮 Entertainment" to listOf("Gaming", "Anime", "Indie Pop", "Music Festivals", "Board Games", "Sci-Fi", "Stand-up"),
    "🌿 Lifestyle & Pets" to listOf("Cats", "Dogs", "Travel", "Reading", "Plants", "Meditation", "Astrology", "Volunteering")
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("profile_edit_screen")
  ) {
    // Segmented Tab Row (Edit vs Preview)
    Surface(
      color = Color.White,
      shadowElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column {
        TabRow(
          selectedTabIndex = currentTab.ordinal,
          containerColor = Color.White,
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
            selected = currentTab == ProfileViewTab.EDIT,
            onClick = { currentTab = ProfileViewTab.EDIT },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Profile", fontWeight = FontWeight.Bold)
              }
            },
            selectedContentColor = CoralPrimary,
            unselectedContentColor = TextSecondaryDark,
            modifier = Modifier.testTag("tab_edit_profile")
          )

          Tab(
            selected = currentTab == ProfileViewTab.PREVIEW,
            onClick = { currentTab = ProfileViewTab.PREVIEW },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Live Preview", fontWeight = FontWeight.Bold)
              }
            },
            selectedContentColor = CoralPrimary,
            unselectedContentColor = TextSecondaryDark,
            modifier = Modifier.testTag("tab_preview_profile")
          )
        }
      }
    }

    if (currentTab == ProfileViewTab.EDIT) {
      // ── EDIT TAB CONTENT ──────────────────────────────────────────────
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 14.dp)
      ) {
        // Save Action Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Profile Management",
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimaryDark
              )
            )
            Text(
              text = "Customize your presence to attract your ideal matches",
              style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
            )
          }

          Button(
            onClick = {
              val updated = userProfile.copy(
                name = name.trim(),
                age = ageText.toIntOrNull() ?: userProfile.age,
                gender = gender.trim(),
                pronouns = pronouns.trim(),
                bio = bio.trim(),
                occupation = occupation.trim(),
                education = education.trim(),
                hometown = hometown.trim(),
                height = height.trim(),
                zodiac = zodiac.trim(),
                datingIntention = datingIntention,
                drinking = drinking,
                smoking = smoking,
                pets = pets,
                promptQuestion = promptQuestion.trim(),
                promptAnswer = promptAnswer.trim(),
                passions = selectedPassions
              )
              onSaveProfile(updated)
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            modifier = Modifier.testTag("btn_save_profile")
          ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Save", fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Completeness Progress Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, Color(0xFFF0E5DC))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CoralPrimary.copy(alpha = 0.12f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Profile Strength",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                  )
                  Text(
                    text = if (completionScore >= 90) "Awesome profile! Top tier visibility 🚀" else "Complete all sections for 3x more matches",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                  )
                }
              }

              Text(
                text = "$completionScore%",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = if (completionScore >= 80) CoralPrimary else Color(0xFFFF9E7D)
                )
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
              progress = { completionScore / 100f },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = CoralPrimary,
              trackColor = PeachBlush
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
              onClick = onRestartOnboarding,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CoralPrimary
              ),
              border = BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.5f))
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = CoralPrimary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Run Step-by-Step Profile Wizard", fontWeight = FontWeight.SemiBold)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subscription Status Card
        SubscriptionStatusCard(
          subscriptionState = subscriptionState,
          onOpenPaywall = onOpenPaywall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── 1. PHOTO MANAGEMENT (6 SLOTS) ──────────────────────────────────
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "PROFILE PHOTOS (${userProfile.photos.size}/6)",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              color = CoralPrimary,
              letterSpacing = 1.sp
            )
          )
          Text(
            text = "Tap to manage or add",
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6 photo slots in 2 rows of 3
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Row 1 (Slots 0, 1, 2)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            for (i in 0..2) {
              val photoUrl = userProfile.photos.getOrNull(i)
              PhotoSlotItem(
                slotNumber = i + 1,
                photoUrl = photoUrl,
                isPrimary = i == 0,
                onSlotClick = {
                  if (photoUrl != null) {
                    activePhotoIndexForManage = i
                  } else {
                    activePhotoSlotForAdd = i
                  }
                },
                modifier = Modifier.weight(1f)
              )
            }
          }

          // Row 2 (Slots 3, 4, 5)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            for (i in 3..5) {
              val photoUrl = userProfile.photos.getOrNull(i)
              PhotoSlotItem(
                slotNumber = i + 1,
                photoUrl = photoUrl,
                isPrimary = false,
                onSlotClick = {
                  if (photoUrl != null) {
                    activePhotoIndexForManage = i
                  } else {
                    activePhotoSlotForAdd = i
                  }
                },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 2. BIO MANAGEMENT ─────────────────────────────────────────────
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "ABOUT ME (BIO)",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              color = CoralPrimary,
              letterSpacing = 1.sp
            )
          )
          Text(
            text = "${bio.length} / 500",
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (bio.length > 450) Color(0xFFFF4757) else TextSecondaryDark,
              fontWeight = FontWeight.Bold
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        KatkatInputField(
          value = bio,
          onValueChange = { if (it.length <= 500) bio = it },
          label = "Tell potential matches about your vibe, passions & hobbies...",
          testTag = "input_profile_bio",
          singleLine = false,
          maxLines = 4
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Quick Bio Starters (Tap to insert):",
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextSecondaryDark,
            fontWeight = FontWeight.SemiBold
          )
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
          items(bioStarters) { starter ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = PeachBlush,
              border = BorderStroke(1.dp, Color(0xFFF3E2D8)),
              modifier = Modifier.clickable {
                bio = if (bio.isBlank()) starter else "$bio\n$starter"
              }
            ) {
              Text(
                text = starter,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = TextPrimaryDark,
                  fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 3. INTERESTS & PASSIONS MANAGEMENT ───────────────────────────
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "PASSIONS & INTERESTS (${selectedPassions.size}/6)",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              color = CoralPrimary,
              letterSpacing = 1.sp
            )
          )
          Text(
            text = "Pick up to 6 passions",
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active Selected Passions Preview
        if (selectedPassions.isNotEmpty()) {
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            selectedPassions.forEach { tag ->
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = CoralPrimary,
                modifier = Modifier.clickable {
                  selectedPassions = selectedPassions - tag
                }
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = tag,
                    style = MaterialTheme.typography.labelMedium.copy(
                      color = Color.White,
                      fontWeight = FontWeight.Bold
                    )
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove $tag",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
        }

        // Categorized Passions Explorer
        passionCategories.forEach { (categoryName, tags) ->
          Text(
            text = categoryName,
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = TextPrimaryDark
            )
          )
          Spacer(modifier = Modifier.height(6.dp))

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            tags.forEach { tag ->
              val isSelected = selectedPassions.contains(tag)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .background(if (isSelected) CoralPrimary else Color.White)
                  .border(
                    width = 1.dp,
                    color = if (isSelected) CoralPrimary else Color(0xFFEADBCE),
                    shape = RoundedCornerShape(16.dp)
                  )
                  .clickable {
                    selectedPassions = if (isSelected) {
                      selectedPassions - tag
                    } else {
                      if (selectedPassions.size < 6) selectedPassions + tag else selectedPassions
                    }
                  }
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(
                  text = tag,
                  style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isSelected) Color.White else TextPrimaryDark,
                    fontWeight = FontWeight.SemiBold
                  )
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
        }

        // Custom Passion Creator
        OutlinedButton(
          onClick = { showCustomPassionDialog = true },
          shape = RoundedCornerShape(20.dp),
          border = BorderStroke(1.dp, CoralPrimary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Custom Interest ✨", color = CoralPrimary, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 4. PROFILE PROMPTS & Q&A ──────────────────────────────────────
        Text(
          text = "PROFILE PROMPT",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = CoralPrimary,
            letterSpacing = 1.sp
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        PromptSelectorDropdown(
          selectedPrompt = promptQuestion,
          options = promptSuggestions,
          onSelectPrompt = { promptQuestion = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        KatkatInputField(
          value = promptAnswer,
          onValueChange = { promptAnswer = it },
          label = "Your Answer...",
          testTag = "input_prompt_answer",
          singleLine = false,
          maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── 5. BASIC INFORMATION ─────────────────────────────────────────
        Text(
          text = "BASIC DETAILS",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = CoralPrimary,
            letterSpacing = 1.sp
          )
        )
        Spacer(modifier = Modifier.height(10.dp))

        KatkatInputField(
          value = name,
          onValueChange = { name = it },
          label = "Full Name",
          testTag = "input_profile_name"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          KatkatInputField(
            value = ageText,
            onValueChange = { ageText = it },
            label = "Age",
            testTag = "input_profile_age",
            modifier = Modifier.weight(1f)
          )
          KatkatInputField(
            value = pronouns,
            onValueChange = { pronouns = it },
            label = "Pronouns",
            testTag = "input_profile_pronouns",
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          KatkatInputField(
            value = gender,
            onValueChange = { gender = it },
            label = "Gender Identity",
            testTag = "input_profile_gender",
            modifier = Modifier.weight(1f)
          )
          KatkatInputField(
            value = height,
            onValueChange = { height = it },
            label = "Height (e.g. 5'9\")",
            testTag = "input_profile_height",
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        KatkatInputField(
          value = occupation,
          onValueChange = { occupation = it },
          label = "Job Title / Occupation",
          testTag = "input_profile_occupation"
        )

        Spacer(modifier = Modifier.height(10.dp))

        KatkatInputField(
          value = education,
          onValueChange = { education = it },
          label = "College / University",
          testTag = "input_profile_education"
        )

        Spacer(modifier = Modifier.height(10.dp))

        KatkatInputField(
          value = hometown,
          onValueChange = { hometown = it },
          label = "Hometown / Living in",
          testTag = "input_profile_hometown"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── 6. DATING INTENTIONS ──────────────────────────────────────────
        Text(
          text = "DATING INTENTIONS",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = CoralPrimary,
            letterSpacing = 1.sp
          )
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          intentionsList.forEach { intention ->
            val isSelected = datingIntention == intention
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) CoralPrimary else PeachBlush)
                .clickable { datingIntention = intention }
                .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
              Text(
                text = intention,
                style = MaterialTheme.typography.labelMedium.copy(
                  color = if (isSelected) Color.White else TextPrimaryDark,
                  fontWeight = FontWeight.Bold
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 7. LIFESTYLE BADGES ──────────────────────────────────────────
        Text(
          text = "LIFESTYLE ATTRIBUTES",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = CoralPrimary,
            letterSpacing = 1.sp
          )
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          KatkatInputField(
            value = drinking,
            onValueChange = { drinking = it },
            label = "Drinking (e.g. Socially)",
            testTag = "input_profile_drinking",
            modifier = Modifier.weight(1f)
          )
          KatkatInputField(
            value = smoking,
            onValueChange = { smoking = it },
            label = "Smoking (e.g. Never)",
            testTag = "input_profile_smoking",
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          KatkatInputField(
            value = pets,
            onValueChange = { pets = it },
            label = "Pets (e.g. 2 rescue cats)",
            testTag = "input_profile_pets",
            modifier = Modifier.weight(1f)
          )
          KatkatInputField(
            value = zodiac,
            onValueChange = { zodiac = it },
            label = "Zodiac Sign",
            testTag = "input_profile_zodiac",
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Bottom Save Button
        Button(
          onClick = {
            val updated = userProfile.copy(
              name = name.trim(),
              age = ageText.toIntOrNull() ?: userProfile.age,
              gender = gender.trim(),
              pronouns = pronouns.trim(),
              bio = bio.trim(),
              occupation = occupation.trim(),
              education = education.trim(),
              hometown = hometown.trim(),
              height = height.trim(),
              zodiac = zodiac.trim(),
              datingIntention = datingIntention,
              drinking = drinking,
              smoking = smoking,
              pets = pets,
              promptQuestion = promptQuestion.trim(),
              promptAnswer = promptAnswer.trim(),
              passions = selectedPassions
            )
            onSaveProfile(updated)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("btn_save_profile_bottom"),
          shape = RoundedCornerShape(27.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Icon(imageVector = Icons.Default.Check, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Save Profile Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
      }
    } else {
      // ── PREVIEW TAB CONTENT ───────────────────────────────────────────
      LiveProfilePreviewView(
        userProfile = userProfile.copy(
          name = name,
          age = ageText.toIntOrNull() ?: userProfile.age,
          gender = gender,
          pronouns = pronouns,
          bio = bio,
          occupation = occupation,
          education = education,
          hometown = hometown,
          height = height,
          zodiac = zodiac,
          datingIntention = datingIntention,
          drinking = drinking,
          smoking = smoking,
          pets = pets,
          promptQuestion = promptQuestion,
          promptAnswer = promptAnswer,
          passions = selectedPassions
        ),
        onSwitchToEdit = { currentTab = ProfileViewTab.EDIT }
      )
    }
  }

  // ── Bottom Sheet 1: Add/Upload Photo (Camera vs Gallery) ──────────────
  if (activePhotoSlotForAdd != null) {
    ModalBottomSheet(
      onDismissRequest = { activePhotoSlotForAdd = null },
      containerColor = MaterialTheme.colorScheme.background,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Upload Profile Photo",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark
          )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Select an option to add a photo to Slot ${(activePhotoSlotForAdd ?: 0) + 1}",
          style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Take Photo with Camera
        Button(
          onClick = {
            val hasCameraPermission = ContextCompat.checkSelfPermission(
              context,
              Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasCameraPermission) {
              cameraLauncher.launch()
            } else {
              cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("btn_take_photo_camera"),
          shape = RoundedCornerShape(26.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
          Spacer(modifier = Modifier.width(10.dp))
          Text("Take Photo with Camera 📷", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Choose from Gallery
        OutlinedButton(
          onClick = {
            galleryLauncher.launch(
              androidx.activity.result.PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
              )
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("btn_pick_gallery_photo"),
          shape = RoundedCornerShape(26.dp),
          border = BorderStroke(1.5.dp, CoralPrimary)
        ) {
          Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = CoralPrimary)
          Spacer(modifier = Modifier.width(10.dp))
          Text("Choose from Gallery 🖼️", color = CoralPrimary, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // ── Bottom Sheet 2: Manage Existing Photo (Primary, Replace, Delete) ───
  val manageIndex = activePhotoIndexForManage
  if (manageIndex != null && manageIndex in userProfile.photos.indices) {
    ModalBottomSheet(
      onDismissRequest = { activePhotoIndexForManage = null },
      containerColor = MaterialTheme.colorScheme.background,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Manage Photo ${manageIndex + 1}",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Preview thumbnail
        Box(
          modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(16.dp))
        ) {
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(userProfile.photos[manageIndex])
              .crossfade(true)
              .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Set as Main Photo (if not already main)
        if (manageIndex != 0) {
          Button(
            onClick = {
              onSetPrimaryPhoto(manageIndex)
              activePhotoIndexForManage = null
              Toast.makeText(context, "Set as main profile photo! ⭐", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
          ) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Set as Main Photo ⭐", fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(10.dp))
        }

        // Replace Photo
        OutlinedButton(
          onClick = {
            val replaceSlot = manageIndex
            activePhotoIndexForManage = null
            activePhotoSlotForAdd = replaceSlot
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(24.dp),
          border = BorderStroke(1.dp, CoralPrimary)
        ) {
          Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = CoralPrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Replace Photo 🔄", color = CoralPrimary, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Delete Photo
        if (userProfile.photos.size > 1) {
          Button(
            onClick = {
              onRemovePhoto(manageIndex)
              activePhotoIndexForManage = null
              Toast.makeText(context, "Photo removed", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4757))
          ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete Photo 🗑️", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }

  // ── Custom Passion Input Dialog ───────────────────────────────────────
  if (showCustomPassionDialog) {
    androidx.compose.material3.AlertDialog(
      onDismissRequest = {
        showCustomPassionDialog = false
        customPassionInput = ""
      },
      title = {
        Text("Add Custom Interest", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
      },
      text = {
        Column {
          Text(
            "Enter any unique hobby, interest, or niche topic:",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
          )
          Spacer(modifier = Modifier.height(12.dp))
          KatkatInputField(
            value = customPassionInput,
            onValueChange = { customPassionInput = it },
            label = "e.g. Pottery, Matcha, Synthwave",
            testTag = "input_custom_passion"
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val clean = customPassionInput.trim()
            if (clean.isNotBlank()) {
              if (selectedPassions.size < 6 && !selectedPassions.contains(clean)) {
                selectedPassions = selectedPassions + clean
              }
            }
            showCustomPassionDialog = false
            customPassionInput = ""
          },
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Text("Add", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = {
            showCustomPassionDialog = false
            customPassionInput = ""
          }
        ) {
          Text("Cancel")
        }
      },
      containerColor = Color.White,
      shape = RoundedCornerShape(20.dp)
    )
  }
}

// ── Photo Slot Composable ────────────────────────────────────────────────
@Composable
fun PhotoSlotItem(
  slotNumber: Int,
  photoUrl: String?,
  isPrimary: Boolean,
  onSlotClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .aspectRatio(0.75f)
      .clip(RoundedCornerShape(16.dp))
      .background(if (photoUrl != null) Color.Transparent else PeachBlush)
      .border(
        width = if (isPrimary) 2.dp else 1.dp,
        color = if (isPrimary) CoralPrimary else Color(0xFFEADBCE),
        shape = RoundedCornerShape(16.dp)
      )
      .clickable { onSlotClick() }
  ) {
    if (photoUrl != null) {
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(photoUrl)
          .crossfade(true)
          .build(),
        contentDescription = "Photo slot $slotNumber",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )

      // Main Photo Badge
      if (isPrimary) {
        Box(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CoralPrimary)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(text = "★ MAIN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        }
      }

      // Slot Number Tag
      Box(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(4.dp)
          .size(20.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
      ) {
        Text(text = "$slotNumber", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
      }
    } else {
      Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.AddAPhoto,
          contentDescription = "Add Photo",
          tint = CoralPrimary,
          modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (isPrimary) "Main Photo" else "Slot $slotNumber",
          style = MaterialTheme.typography.labelSmall.copy(
            color = CoralPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
          )
        )
      }
    }
  }
}

// ── Prompt Dropdown Menu ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptSelectorDropdown(
  selectedPrompt: String,
  options: List<String>,
  onSelectPrompt: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = it },
    modifier = Modifier.fillMaxWidth()
  ) {
    OutlinedTextField(
      value = selectedPrompt,
      onValueChange = {},
      readOnly = true,
      label = { Text("Choose a Prompt Question") },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      modifier = Modifier
        .menuAnchor()
        .fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = CoralPrimary,
        unfocusedBorderColor = Color(0xFFEADBCE),
        focusedTextColor = TextPrimaryDark,
        unfocusedTextColor = TextPrimaryDark
      )
    )

    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.background(Color.White)
    ) {
      options.forEach { option ->
        DropdownMenuItem(
          text = { Text(option, fontWeight = if (option == selectedPrompt) FontWeight.Bold else FontWeight.Normal) },
          onClick = {
            onSelectPrompt(option)
            expanded = false
          }
        )
      }
    }
  }
}

// ── Live Profile Preview Component ───────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveProfilePreviewView(
  userProfile: UserProfile,
  onSwitchToEdit: () -> Unit
) {
  var currentPhotoIndex by remember { mutableIntStateOf(0) }
  val photos = userProfile.photos.ifEmpty {
    listOf("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&q=80")
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    // Top Preview banner
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = CoralPrimary.copy(alpha = 0.1f),
      border = BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.2f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("This is how other singles see you", style = MaterialTheme.typography.labelMedium.copy(color = CoralPrimary, fontWeight = FontWeight.Bold))
        }

        Text(
          text = "Edit ✏️",
          style = MaterialTheme.typography.labelSmall.copy(color = CoralPrimary, fontWeight = FontWeight.ExtraBold),
          modifier = Modifier.clickable { onSwitchToEdit() }
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Interactive Photo Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .height(440.dp)
        .shadow(6.dp, RoundedCornerShape(24.dp)),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(photos.getOrElse(currentPhotoIndex.coerceIn(0, photos.size - 1)) { photos[0] })
            .crossfade(true)
            .build(),
          contentDescription = "Profile photo preview",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )

        // Gradient overlay
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.35f),
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.85f)
                ),
                startY = 0f,
                endY = Float.POSITIVE_INFINITY
              )
            )
        )

        // Photo indicator pills
        if (photos.size > 1) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp)
              .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            photos.forEachIndexed { index, _ ->
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(3.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(if (index == currentPhotoIndex) Color.White else Color.White.copy(alpha = 0.4f))
              )
            }
          }
        }

        // Tap navigators (Left/Right)
        Row(modifier = Modifier.fillMaxSize()) {
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxSize()
              .clickable {
                if (currentPhotoIndex > 0) currentPhotoIndex-- else currentPhotoIndex = photos.size - 1
              }
          )
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxSize()
              .clickable {
                if (currentPhotoIndex < photos.size - 1) currentPhotoIndex++ else currentPhotoIndex = 0
              }
          )
        }

        // Floating Name & Badge details on photo
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(16.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "${userProfile.name}, ${userProfile.age}",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = "Verified",
              tint = SuperlikeBlue,
              modifier = Modifier.size(22.dp)
            )
          }

          if (userProfile.pronouns.isNotBlank()) {
            Text(
              text = userProfile.pronouns,
              style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f))
            )
          }

          if (userProfile.occupation.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Work, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = userProfile.occupation, style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
            }
          }

          if (userProfile.hometown.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = userProfile.hometown, style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Bio Card
    if (userProfile.bio.isNotBlank()) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0E5DC))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "About Me", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark))
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = userProfile.bio,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryDark, lineHeight = 20.sp)
          )
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    // Passions Chips Card
    if (userProfile.passions.isNotEmpty()) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0E5DC))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "Passions & Interests", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark))
          Spacer(modifier = Modifier.height(10.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            userProfile.passions.forEach { passion ->
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = PeachBlush,
                border = BorderStroke(1.dp, Color(0xFFF3E2D8))
              ) {
                Text(
                  text = passion,
                  style = MaterialTheme.typography.labelMedium.copy(
                    color = CoralPrimary,
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    // Prompt Card
    if (userProfile.promptAnswer.isNotBlank()) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PeachBlush),
        border = BorderStroke(1.dp, Color(0xFFF3E2D8))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = userProfile.promptQuestion,
            style = MaterialTheme.typography.labelMedium.copy(color = CoralPrimary, fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = userProfile.promptAnswer,
            style = MaterialTheme.typography.bodyLarge.copy(
              fontWeight = FontWeight.Bold,
              color = TextPrimaryDark
            )
          )
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    // Lifestyle & Details Badges Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, Color(0xFFF0E5DC))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Lifestyle & Intentions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark))
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (userProfile.datingIntention.isNotBlank()) {
            PreviewDetailPill(icon = Icons.Default.Favorite, text = userProfile.datingIntention)
          }
          if (userProfile.height.isNotBlank()) {
            PreviewDetailPill(icon = Icons.Default.Star, text = userProfile.height)
          }
          if (userProfile.zodiac.isNotBlank()) {
            PreviewDetailPill(icon = Icons.Default.AutoAwesome, text = userProfile.zodiac)
          }
          if (userProfile.pets.isNotBlank()) {
            PreviewDetailPill(icon = Icons.Default.Favorite, text = userProfile.pets)
          }
          if (userProfile.drinking.isNotBlank()) {
            PreviewDetailPill(icon = Icons.Default.Star, text = "Drinks: ${userProfile.drinking}")
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(40.dp))
  }
}

@Composable
fun PreviewDetailPill(
  icon: ImageVector,
  text: String
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color(0xFFFBF7F4),
    border = BorderStroke(1.dp, Color(0xFFECE1D8))
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(14.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(text = text, style = MaterialTheme.typography.labelSmall.copy(color = TextPrimaryDark, fontWeight = FontWeight.Medium))
    }
  }
}

@Composable
fun SubscriptionStatusCard(
  subscriptionState: SubscriptionState,
  onOpenPaywall: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp)),
    color = PeachBlush,
    border = BorderStroke(1.dp, Color(0xFFF3E2D8))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
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
              .background(
                if (subscriptionState.currentTier == SubscriptionTier.TIER_2) GoldVip else CoralPrimary
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ElectricBolt,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = subscriptionState.currentTier.title,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
              )
            )
            Text(
              text = "Monthly Allowance: ${subscriptionState.currentTier.monthlySwipes} swipes",
              style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
            )
          }
        }

        Button(
          onClick = onOpenPaywall,
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (subscriptionState.currentTier == SubscriptionTier.TIER_2) GoldVip else CoralPrimary
          ),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.height(34.dp)
        ) {
          Text("Manage Tier", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "${subscriptionState.swipesUsedThisMonth} of ${subscriptionState.currentTier.monthlySwipes} swipes used this month (${subscriptionState.remainingSwipes} remaining)",
        style = MaterialTheme.typography.labelSmall.copy(
          color = if (subscriptionState.hasReachedLimit) Color(0xFFFF4757) else TextSecondaryDark,
          fontWeight = FontWeight.Medium
        )
      )
    }
  }
}

@Composable
fun KatkatInputField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  testTag: String,
  modifier: Modifier = Modifier,
  singleLine: Boolean = true,
  maxLines: Int = 1
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label) },
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = Color.White,
      unfocusedContainerColor = Color.White,
      focusedBorderColor = CoralPrimary,
      unfocusedBorderColor = Color(0xFFEADBCE),
      focusedTextColor = TextPrimaryDark,
      unfocusedTextColor = TextPrimaryDark
    ),
    singleLine = singleLine,
    maxLines = maxLines
  )
}

// Helper to save Bitmap from camera to internal app storage
private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): Uri? {
  return try {
    val file = File(context.cacheDir, "profile_photo_${System.currentTimeMillis()}.jpg")
    val out = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    out.flush()
    out.close()
    Uri.fromFile(file)
  } catch (e: Exception) {
    e.printStackTrace()
    null
  }
}
