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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stars
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
  userProfile: UserProfile,
  subscriptionState: SubscriptionState,
  onSaveProfile: (UserProfile) -> Unit,
  onAddPhoto: (String) -> Unit,
  onRemovePhoto: (Int) -> Unit,
  onOpenPaywall: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  var name by remember(userProfile) { mutableStateOf(userProfile.name) }
  var ageText by remember(userProfile) { mutableStateOf(userProfile.age.toString()) }
  var gender by remember(userProfile) { mutableStateOf(userProfile.gender) }
  var pronouns by remember(userProfile) { mutableStateOf(userProfile.pronouns) }
  var bio by remember(userProfile) { mutableStateOf(userProfile.bio) }
  var occupation by remember(userProfile) { mutableStateOf(userProfile.occupation) }
  var education by remember(userProfile) { mutableStateOf(userProfile.education) }
  var hometown by remember(userProfile) { mutableStateOf(userProfile.hometown) }
  var datingIntention by remember(userProfile) { mutableStateOf(userProfile.datingIntention) }
  var promptQuestion by remember(userProfile) { mutableStateOf(userProfile.promptQuestion) }
  var promptAnswer by remember(userProfile) { mutableStateOf(userProfile.promptAnswer) }
  var selectedPassions by remember(userProfile) { mutableStateOf(userProfile.passions) }

  var showMediaPickerSheet by remember { mutableStateOf(false) }

  // Gallery Picker Launcher
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let {
      onAddPhoto(it.toString())
      Toast.makeText(context, "Photo added from gallery! 📸", Toast.LENGTH_SHORT).show()
    }
  }

  // Camera Capture Launcher (returns preview bitmap)
  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    bitmap?.let {
      val savedUri = saveBitmapToInternalStorage(context, it)
      if (savedUri != null) {
        onAddPhoto(savedUri.toString())
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

  val allPassions = listOf(
    "Photography", "Coffee", "Vinyl Records", "Cats", "Dogs",
    "Baking", "Art Galleries", "Cooking", "Hiking", "Indie Pop",
    "Gaming", "Anime", "Travel", "Yoga", "Film", "Reading",
    "Cocktails", "Music Festivals", "Board Games", "Astrology"
  )

  val intentionsList = listOf(
    "Long-term relationship 💖",
    "Casual dating ✨",
    "Marriage partner 💍",
    "New friends ☕",
    "Still figuring it out 🤔"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("profile_edit_screen")
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Edit Profile",
          style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimaryDark
          )
        )
        Text(
          text = "Build your profile to attract your best matches",
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
            datingIntention = datingIntention,
            promptQuestion = promptQuestion.trim(),
            promptAnswer = promptAnswer.trim(),
            passions = selectedPassions
          )
          onSaveProfile(updated)
        },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
        modifier = Modifier.testTag("btn_save_profile")
      ) {
        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Save", fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Subscription Status Banner
    SubscriptionStatusCard(
      subscriptionState = subscriptionState,
      onOpenPaywall = onOpenPaywall
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Media Uploads: 6-Slot Photo Grid
    Text(
      text = "PROFILE PHOTOS (${userProfile.photos.size}/6)",
      style = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.Bold,
        color = CoralPrimary,
        letterSpacing = 1.sp
      )
    )
    Spacer(modifier = Modifier.height(8.dp))

    // 6 photo slots grid
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Row 1 (Slots 0, 1, 2)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PhotoSlotItem(
          photoUrl = userProfile.photos.getOrNull(0),
          isPrimary = true,
          onAddClick = { showMediaPickerSheet = true },
          onRemoveClick = { onRemovePhoto(0) },
          modifier = Modifier.weight(1f)
        )
        PhotoSlotItem(
          photoUrl = userProfile.photos.getOrNull(1),
          isPrimary = false,
          onAddClick = { showMediaPickerSheet = true },
          onRemoveClick = { onRemovePhoto(1) },
          modifier = Modifier.weight(1f)
        )
        PhotoSlotItem(
          photoUrl = userProfile.photos.getOrNull(2),
          isPrimary = false,
          onAddClick = { showMediaPickerSheet = true },
          onRemoveClick = { onRemovePhoto(2) },
          modifier = Modifier.weight(1f)
        )
      }

      // Row 2 (Slots 3, 4, 5)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PhotoSlotItem(
          photoUrl = userProfile.photos.getOrNull(3),
          isPrimary = false,
          onAddClick = { showMediaPickerSheet = true },
          onRemoveClick = { onRemovePhoto(3) },
          modifier = Modifier.weight(1f)
        )
        PhotoSlotItem(
          photoUrl = userProfile.photos.getOrNull(4),
          isPrimary = false,
          onAddClick = { showMediaPickerSheet = true },
          onRemoveClick = { onRemovePhoto(4) },
          modifier = Modifier.weight(1f)
        )
        PhotoSlotItem(
          photoUrl = userProfile.photos.getOrNull(5),
          isPrimary = false,
          onAddClick = { showMediaPickerSheet = true },
          onRemoveClick = { onRemovePhoto(5) },
          modifier = Modifier.weight(1f)
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Form fields
    Text(
      text = "BASIC INFORMATION",
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

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

    KatkatInputField(
      value = bio,
      onValueChange = { bio = it },
      label = "About Me (Bio)",
      testTag = "input_profile_bio",
      singleLine = false,
      maxLines = 4
    )

    Spacer(modifier = Modifier.height(10.dp))

    KatkatInputField(
      value = occupation,
      onValueChange = { occupation = it },
      label = "Occupation / Job Title",
      testTag = "input_profile_occupation"
    )

    Spacer(modifier = Modifier.height(10.dp))

    KatkatInputField(
      value = education,
      onValueChange = { education = it },
      label = "Education / University",
      testTag = "input_profile_education"
    )

    Spacer(modifier = Modifier.height(10.dp))

    KatkatInputField(
      value = hometown,
      onValueChange = { hometown = it },
      label = "Hometown / Location",
      testTag = "input_profile_hometown"
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Dating Intentions
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

    Spacer(modifier = Modifier.height(20.dp))

    // Passions / Interests Selector
    Text(
      text = "PASSIONS & INTERESTS (Pick up to 6)",
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
      allPassions.forEach { tag ->
        val isSelected = selectedPassions.contains(tag)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) CoralPrimary else Color.White)
            .border(1.dp, if (isSelected) CoralPrimary else Color(0xFFEADBCE), RoundedCornerShape(16.dp))
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

    Spacer(modifier = Modifier.height(20.dp))

    // Prompt Q&A
    Text(
      text = "PROFILE PROMPT",
      style = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.Bold,
        color = CoralPrimary,
        letterSpacing = 1.sp
      )
    )
    Spacer(modifier = Modifier.height(8.dp))

    KatkatInputField(
      value = promptQuestion,
      onValueChange = { promptQuestion = it },
      label = "Prompt Question",
      testTag = "input_prompt_question"
    )

    Spacer(modifier = Modifier.height(8.dp))

    KatkatInputField(
      value = promptAnswer,
      onValueChange = { promptAnswer = it },
      label = "Your Answer",
      testTag = "input_prompt_answer",
      singleLine = false,
      maxLines = 3
    )

    Spacer(modifier = Modifier.height(36.dp))
  }

  // Media Picker Bottom Sheet (Camera or Gallery)
  if (showMediaPickerSheet) {
    ModalBottomSheet(
      onDismissRequest = { showMediaPickerSheet = false },
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
          text = "Select an option to add a new photo to your Katkat profile",
          style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Take photo with camera button
        Button(
          onClick = {
            showMediaPickerSheet = false
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

        // Choose from gallery button (Zero-permission photo picker)
        OutlinedButton(
          onClick = {
            showMediaPickerSheet = false
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

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}

@Composable
fun PhotoSlotItem(
  photoUrl: String?,
  isPrimary: Boolean,
  onAddClick: () -> Unit,
  onRemoveClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .height(130.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(if (photoUrl != null) Color.Transparent else PeachBlush)
      .border(
        width = 1.5.dp,
        color = if (isPrimary) CoralPrimary else Color(0xFFEADBCE),
        shape = RoundedCornerShape(16.dp)
      )
      .clickable { if (photoUrl == null) onAddClick() }
  ) {
    if (photoUrl != null) {
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(photoUrl)
          .crossfade(true)
          .build(),
        contentDescription = "Profile photo",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )

      // Delete photo button
      IconButton(
        onClick = onRemoveClick,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(4.dp)
          .size(26.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f))
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Remove photo",
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
      }

      if (isPrimary) {
        Box(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CoralPrimary)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(text = "MAIN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        }
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
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (isPrimary) "Main" else "Add",
          style = MaterialTheme.typography.labelSmall.copy(color = CoralPrimary, fontWeight = FontWeight.Bold)
        )
      }
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
