package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.LikeGreen
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.PeachSecondary
import com.example.ui.theme.SuperlikeBlue

enum class OnboardingStep(val title: String, val subtitle: String) {
  IDENTITY("Your Basics", "Let's start with who you are"),
  GENDER_PRONOUNS("Identity & Pronouns", "How should we present you?"),
  PHOTOS("Your Photos", "Add at least 2 photos to show your spark"),
  CAREER_EDUCATION("Work & Education", "What keeps you inspired?"),
  INTENTIONS_PASSIONS("Dating Goals & Passions", "What are you looking for?"),
  LIFESTYLE_PROMPTS("Personality & Prompts", "Share what makes you unique"),
  REVIEW_LAUNCH("Review & Launch", "Here is your Katkat profile!")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingProfileSetupScreen(
  initialProfile: UserProfile,
  onComplete: (UserProfile) -> Unit,
  modifier: Modifier = Modifier
) {
  var currentStepIndex by remember { mutableIntStateOf(0) }
  val steps = OnboardingStep.entries

  // Draft profile state
  var name by remember { mutableStateOf(initialProfile.name) }
  var ageText by remember { mutableStateOf(initialProfile.age.toString()) }
  var hometown by remember { mutableStateOf(initialProfile.hometown) }
  var gender by remember { mutableStateOf(initialProfile.gender) }
  var pronouns by remember { mutableStateOf(initialProfile.pronouns) }
  var photos by remember { mutableStateOf(initialProfile.photos.toMutableList()) }
  var occupation by remember { mutableStateOf(initialProfile.occupation) }
  var education by remember { mutableStateOf(initialProfile.education) }
  var datingIntention by remember { mutableStateOf(initialProfile.datingIntention) }
  var selectedPassions by remember { mutableStateOf(initialProfile.passions.toSet()) }
  var height by remember { mutableStateOf(initialProfile.height) }
  var zodiac by remember { mutableStateOf(initialProfile.zodiac) }
  var drinking by remember { mutableStateOf(initialProfile.drinking) }
  var smoking by remember { mutableStateOf(initialProfile.smoking) }
  var pets by remember { mutableStateOf(initialProfile.pets) }
  var promptQuestion by remember { mutableStateOf(initialProfile.promptQuestion) }
  var promptAnswer by remember { mutableStateOf(initialProfile.promptAnswer) }
  var bio by remember { mutableStateOf(initialProfile.bio) }

  // Step validations
  val isCurrentStepValid by remember {
    derivedStateOf {
      when (steps[currentStepIndex]) {
        OnboardingStep.IDENTITY -> {
          val age = ageText.toIntOrNull() ?: 0
          name.trim().length >= 2 && age in 18..100 && hometown.isNotBlank()
        }
        OnboardingStep.GENDER_PRONOUNS -> gender.isNotBlank() && pronouns.isNotBlank()
        OnboardingStep.PHOTOS -> photos.size >= 2
        OnboardingStep.CAREER_EDUCATION -> occupation.isNotBlank()
        OnboardingStep.INTENTIONS_PASSIONS -> datingIntention.isNotBlank() && selectedPassions.size >= 3
        OnboardingStep.LIFESTYLE_PROMPTS -> promptAnswer.trim().length >= 5 && bio.trim().length >= 10
        OnboardingStep.REVIEW_LAUNCH -> true
      }
    }
  }

  val progress = (currentStepIndex + 1).toFloat() / steps.size

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .testTag("onboarding_profile_setup_screen"),
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (currentStepIndex > 0) {
            IconButton(
              onClick = { currentStepIndex-- },
              modifier = Modifier.testTag("onboarding_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
          } else {
            Spacer(modifier = Modifier.size(48.dp))
          }

          // App Brand & Step Indicator
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Katkat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = CoralPrimary,
                letterSpacing = 0.5.sp
              )
              Text(
                text = " ✨",
                style = MaterialTheme.typography.titleSmall
              )
            }
            Text(
              text = "Step ${currentStepIndex + 1} of ${steps.size}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Step count badge
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = CoralPrimary.copy(alpha = 0.12f)
          ) {
            Text(
              text = "${((progress * 100).toInt())}%",
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = CoralPrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Animated progress line
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = CoralPrimary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
          strokeCap = StrokeCap.Round
        )
      }
    },
    bottomBar = {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.surface
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
          if (!isCurrentStepValid && steps[currentStepIndex] != OnboardingStep.REVIEW_LAUNCH) {
            val hint = when (steps[currentStepIndex]) {
              OnboardingStep.IDENTITY -> "Please enter a valid name, age (18+), and hometown."
              OnboardingStep.GENDER_PRONOUNS -> "Please select your gender and pronouns."
              OnboardingStep.PHOTOS -> "Add at least 2 photos to continue (${photos.size}/2 added)."
              OnboardingStep.CAREER_EDUCATION -> "Please enter your occupation."
              OnboardingStep.INTENTIONS_PASSIONS -> "Select dating goal and at least 3 passions (${selectedPassions.size}/3 selected)."
              OnboardingStep.LIFESTYLE_PROMPTS -> "Write your prompt answer and a brief bio."
              OnboardingStep.REVIEW_LAUNCH -> ""
            }
            Text(
              text = hint,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
            )
          }

          Button(
            onClick = {
              if (currentStepIndex < steps.size - 1) {
                currentStepIndex++
              } else {
                val finalProfile = UserProfile(
                  id = "my_profile",
                  name = name.trim(),
                  age = ageText.toIntOrNull() ?: 24,
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
                  passions = selectedPassions.toList(),
                  photos = photos.toList(),
                  promptQuestion = promptQuestion,
                  promptAnswer = promptAnswer.trim(),
                  isOnboardingCompleted = true
                )
                onComplete(finalProfile)
              }
            },
            enabled = isCurrentStepValid,
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("onboarding_continue_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = CoralPrimary,
              disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = if (currentStepIndex == steps.size - 1) "Complete Profile & Start Matching ✨" else "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentStepValid) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                imageVector = if (currentStepIndex == steps.size - 1) Icons.Default.Favorite else Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (isCurrentStepValid) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }
  ) { innerPadding ->
    AnimatedContent(
      targetState = steps[currentStepIndex],
      transitionSpec = {
        if (targetState.ordinal > initialState.ordinal) {
          (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
            slideOutHorizontally { width -> -width } + fadeOut()
          )
        } else {
          (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
            slideOutHorizontally { width -> width } + fadeOut()
          )
        }
      },
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      label = "onboarding_step_transition"
    ) { step ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp, vertical = 20.dp)
      ) {
        // Step Header Title & Subtitle
        Text(
          text = step.title,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = step.subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        when (step) {
          OnboardingStep.IDENTITY -> {
            StepIdentity(
              name = name,
              onNameChange = { name = it },
              ageText = ageText,
              onAgeChange = { ageText = it },
              hometown = hometown,
              onHometownChange = { hometown = it }
            )
          }
          OnboardingStep.GENDER_PRONOUNS -> {
            StepGenderPronouns(
              gender = gender,
              onGenderSelect = { gender = it },
              pronouns = pronouns,
              onPronounsSelect = { pronouns = it }
            )
          }
          OnboardingStep.PHOTOS -> {
            StepPhotos(
              photos = photos,
              onAddPhoto = { newUri -> if (photos.size < 6) photos.add(newUri) },
              onRemovePhoto = { idx -> if (idx in photos.indices) photos.removeAt(idx) },
              onSetPrimary = { idx ->
                if (idx in 1 until photos.size) {
                  val item = photos.removeAt(idx)
                  photos.add(0, item)
                }
              }
            )
          }
          OnboardingStep.CAREER_EDUCATION -> {
            StepCareerEducation(
              occupation = occupation,
              onOccupationChange = { occupation = it },
              education = education,
              onEducationChange = { education = it }
            )
          }
          OnboardingStep.INTENTIONS_PASSIONS -> {
            StepIntentionsPassions(
              datingIntention = datingIntention,
              onIntentionSelect = { datingIntention = it },
              selectedPassions = selectedPassions,
              onTogglePassion = { p ->
                selectedPassions = if (selectedPassions.contains(p)) {
                  selectedPassions - p
                } else {
                  if (selectedPassions.size < 6) selectedPassions + p else selectedPassions
                }
              }
            )
          }
          OnboardingStep.LIFESTYLE_PROMPTS -> {
            StepLifestylePrompts(
              height = height,
              onHeightChange = { height = it },
              zodiac = zodiac,
              onZodiacChange = { zodiac = it },
              drinking = drinking,
              onDrinkingChange = { drinking = it },
              smoking = smoking,
              onSmokingChange = { smoking = it },
              pets = pets,
              onPetsChange = { pets = it },
              promptQuestion = promptQuestion,
              onPromptQuestionChange = { promptQuestion = it },
              promptAnswer = promptAnswer,
              onPromptAnswerChange = { promptAnswer = it },
              bio = bio,
              onBioChange = { bio = it }
            )
          }
          OnboardingStep.REVIEW_LAUNCH -> {
            StepReviewLaunch(
              profile = UserProfile(
                name = name,
                age = ageText.toIntOrNull() ?: 24,
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
                passions = selectedPassions.toList(),
                photos = photos.toList(),
                promptQuestion = promptQuestion,
                promptAnswer = promptAnswer
              )
            )
          }
        }
      }
    }
  }
}

// ==========================================
// Step 1: Identity & Basics
// ==========================================
@Composable
private fun StepIdentity(
  name: String,
  onNameChange: (String) -> Unit,
  ageText: String,
  onAgeChange: (String) -> Unit,
  hometown: String,
  onHometownChange: (String) -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
    OutlinedTextField(
      value = name,
      onValueChange = onNameChange,
      label = { Text("First Name / Display Name *") },
      placeholder = { Text("e.g. Alex") },
      leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CoralPrimary) },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("onboarding_name_input"),
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CoralPrimary,
        focusedLabelColor = CoralPrimary
      )
    )

    OutlinedTextField(
      value = ageText,
      onValueChange = { if (it.length <= 2 && (it.isEmpty() || it.all { ch -> ch.isDigit() })) onAgeChange(it) },
      label = { Text("Age (Must be 18+) *") },
      placeholder = { Text("e.g. 24") },
      leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = CoralPrimary) },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("onboarding_age_input"),
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CoralPrimary,
        focusedLabelColor = CoralPrimary
      )
    )

    OutlinedTextField(
      value = hometown,
      onValueChange = onHometownChange,
      label = { Text("Location / Hometown *") },
      placeholder = { Text("e.g. Brooklyn, NY") },
      leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = CoralPrimary) },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("onboarding_hometown_input"),
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CoralPrimary,
        focusedLabelColor = CoralPrimary
      )
    )
  }
}

// ==========================================
// Step 2: Gender & Pronouns
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepGenderPronouns(
  gender: String,
  onGenderSelect: (String) -> Unit,
  pronouns: String,
  onPronounsSelect: (String) -> Unit
) {
  val genderOptions = listOf("Woman", "Man", "Non-binary", "Genderfluid", "Agender", "Prefer not to say")
  val pronounOptions = listOf("She/Her", "He/Him", "They/Them", "She/They", "He/They", "Any pronouns")

  Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
    Text(
      text = "Gender Identity *",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      genderOptions.forEach { opt ->
        val selected = gender == opt
        FilterChip(
          selected = selected,
          onClick = { onGenderSelect(opt) },
          label = { Text(opt, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
          leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
          } else null,
          shape = RoundedCornerShape(20.dp),
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CoralPrimary.copy(alpha = 0.15f),
            selectedLabelColor = CoralPrimary,
            selectedLeadingIconColor = CoralPrimary
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            selectedBorderColor = CoralPrimary,
            borderWidth = 1.5.dp,
            selectedBorderWidth = 1.5.dp
          )
        )
      }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

    Text(
      text = "Pronouns *",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      pronounOptions.forEach { opt ->
        val selected = pronouns == opt
        FilterChip(
          selected = selected,
          onClick = { onPronounsSelect(opt) },
          label = { Text(opt, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
          leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
          } else null,
          shape = RoundedCornerShape(20.dp),
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PeachSecondary.copy(alpha = 0.15f),
            selectedLabelColor = PeachSecondary,
            selectedLeadingIconColor = PeachSecondary
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            selectedBorderColor = PeachSecondary,
            borderWidth = 1.5.dp,
            selectedBorderWidth = 1.5.dp
          )
        )
      }
    }
  }
}

// ==========================================
// Step 3: Photos
// ==========================================
@Composable
private fun StepPhotos(
  photos: MutableList<String>,
  onAddPhoto: (String) -> Unit,
  onRemovePhoto: (Int) -> Unit,
  onSetPrimary: (Int) -> Unit
) {
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let { onAddPhoto(it.toString()) }
  }

  val curatedPresets = listOf(
    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=900&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=900&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=900&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=900&auto=format&fit=crop&q=80"
  )

  Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
    Text(
      text = "Profile Photos (${photos.size}/6)",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )

    // 6-slot photo grid (2 columns x 3 rows)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      for (row in 0 until 3) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          for (col in 0 until 2) {
            val slotIndex = row * 2 + col
            val hasPhoto = slotIndex < photos.size
            val photoUri = if (hasPhoto) photos[slotIndex] else null

            Box(
              modifier = Modifier
                .weight(1f)
                .aspectRatio(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .border(
                  BorderStroke(
                    width = if (slotIndex == 0 && hasPhoto) 2.5.dp else 1.dp,
                    color = if (slotIndex == 0 && hasPhoto) CoralPrimary else MaterialTheme.colorScheme.outlineVariant
                  ),
                  shape = RoundedCornerShape(16.dp)
                )
            ) {
              if (hasPhoto && photoUri != null) {
                AsyncImage(
                  model = photoUri,
                  contentDescription = "Photo ${slotIndex + 1}",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )

                // Primary badge
                if (slotIndex == 0) {
                  Surface(
                    color = CoralPrimary,
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                  ) {
                    Text(
                      text = "★ Main",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }
                } else {
                  // Button to set as main
                  Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape,
                    modifier = Modifier
                      .align(Alignment.BottomStart)
                      .padding(6.dp)
                      .clickable { onSetPrimary(slotIndex) }
                  ) {
                    Text(
                      text = "Make Main",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }

                // Delete button
                IconButton(
                  onClick = { onRemovePhoto(slotIndex) },
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                }
              } else {
                // Empty photo slot with click to upload
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                      photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                      )
                    },
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
                    text = if (slotIndex == 0) "Add Main Photo" else "Add Photo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
    }

    // Quick add presets
    Column(modifier = Modifier.padding(top = 8.dp)) {
      Text(
        text = "Or choose sample presets:",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(8.dp))
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(curatedPresets) { preset ->
          AsyncImage(
            model = preset,
            contentDescription = "Preset photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(56.dp)
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                if (photos.size < 6 && !photos.contains(preset)) {
                  onAddPhoto(preset)
                }
              }
              .border(1.dp, CoralPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
          )
        }
      }
    }
  }
}

// ==========================================
// Step 4: Career & Education
// ==========================================
@Composable
private fun StepCareerEducation(
  occupation: String,
  onOccupationChange: (String) -> Unit,
  education: String,
  onEducationChange: (String) -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
    OutlinedTextField(
      value = occupation,
      onValueChange = onOccupationChange,
      label = { Text("Occupation / Job Title *") },
      placeholder = { Text("e.g. UX Designer & Visual Artist") },
      leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = CoralPrimary) },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("onboarding_occupation_input"),
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CoralPrimary,
        focusedLabelColor = CoralPrimary
      )
    )

    OutlinedTextField(
      value = education,
      onValueChange = onEducationChange,
      label = { Text("College / University (Optional)") },
      placeholder = { Text("e.g. NYU Tisch School of the Arts") },
      leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = CoralPrimary) },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("onboarding_education_input"),
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CoralPrimary,
        focusedLabelColor = CoralPrimary
      )
    )
  }
}

// ==========================================
// Step 5: Intentions & Passions
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepIntentionsPassions(
  datingIntention: String,
  onIntentionSelect: (String) -> Unit,
  selectedPassions: Set<String>,
  onTogglePassion: (String) -> Unit
) {
  val intentionOptions = listOf(
    "Long-term relationship 💖" to "Looking for something genuine & lasting",
    "Casual but open to sparks ✨" to "Going with the flow & open to chemistry",
    "New friends & travel 🌴" to "Exploring new places & expanding circle",
    "Still figuring it out 💭" to "Taking things one great date at a time"
  )

  val passionOptions = listOf(
    "Photography", "Coffee ☕", "Vinyl Records 🎵", "Art Galleries 🎨",
    "Cooking 🍳", "Hiking 🌲", "Indie Music 🎸", "Cats 🐱",
    "Dogs 🐶", "Travel ✈️", "Books 📚", "Gaming 🎮",
    "Fitness 🏃", "Bouldering 🧗", "Film 🎬", "Boba Tea 🧋",
    "Architecture 🏛️", "Yoga 🧘"
  )

  Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
    Text(
      text = "Dating Intention *",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      intentionOptions.forEach { (option, desc) ->
        val selected = datingIntention == option
        Card(
          onClick = { onIntentionSelect(option) },
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (selected) CoralPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
          ),
          border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) CoralPrimary else MaterialTheme.colorScheme.outlineVariant
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = option,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) CoralPrimary else MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            if (selected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = CoralPrimary,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }
      }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Passions & Interests *",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "${selectedPassions.size}/6 selected",
        style = MaterialTheme.typography.labelMedium,
        color = CoralPrimary,
        fontWeight = FontWeight.Bold
      )
    }

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      passionOptions.forEach { p ->
        val isSelected = selectedPassions.contains(p)
        FilterChip(
          selected = isSelected,
          onClick = { onTogglePassion(p) },
          label = { Text(p, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
          shape = RoundedCornerShape(16.dp),
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CoralPrimary.copy(alpha = 0.16f),
            selectedLabelColor = CoralPrimary
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            selectedBorderColor = CoralPrimary,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.5.dp
          )
        )
      }
    }
  }
}

// ==========================================
// Step 6: Lifestyle & Prompts
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepLifestylePrompts(
  height: String,
  onHeightChange: (String) -> Unit,
  zodiac: String,
  onZodiacChange: (String) -> Unit,
  drinking: String,
  onDrinkingChange: (String) -> Unit,
  smoking: String,
  onSmokingChange: (String) -> Unit,
  pets: String,
  onPetsChange: (String) -> Unit,
  promptQuestion: String,
  onPromptQuestionChange: (String) -> Unit,
  promptAnswer: String,
  onPromptAnswerChange: (String) -> Unit,
  bio: String,
  onBioChange: (String) -> Unit
) {
  var isPromptExpanded by remember { mutableStateOf(false) }
  val promptQuestions = listOf(
    "My simple pleasures in life...",
    "Together, we could...",
    "I geek out on...",
    "The key to my heart is...",
    "Two truths and a lie...",
    "Don't hate me if I...",
    "Best travel story involves..."
  )

  Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
    // Prompt Selection & Answer
    Text(
      text = "Profile Prompt *",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )

    ExposedDropdownMenuBox(
      expanded = isPromptExpanded,
      onExpandedChange = { isPromptExpanded = !isPromptExpanded }
    ) {
      OutlinedTextField(
        value = promptQuestion,
        onValueChange = {},
        readOnly = true,
        label = { Text("Choose a Prompt") },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPromptExpanded) },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .menuAnchor(),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CoralPrimary,
          focusedLabelColor = CoralPrimary
        )
      )
      ExposedDropdownMenu(
        expanded = isPromptExpanded,
        onDismissRequest = { isPromptExpanded = false }
      ) {
        promptQuestions.forEach { question ->
          DropdownMenuItem(
            text = { Text(question) },
            onClick = {
              onPromptQuestionChange(question)
              isPromptExpanded = false
            }
          )
        }
      }
    }

    OutlinedTextField(
      value = promptAnswer,
      onValueChange = onPromptAnswerChange,
      label = { Text("Your Prompt Answer *") },
      placeholder = { Text("Write something charming and authentic...") },
      modifier = Modifier
        .fillMaxWidth()
        .height(110.dp)
        .testTag("onboarding_prompt_answer_input"),
      maxLines = 4,
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CoralPrimary,
        focusedLabelColor = CoralPrimary
      )
    )

    // Bio
    OutlinedTextField(
      value = bio,
      onValueChange = onBioChange,
      label = { Text("About Me Bio *") },
      placeholder = { Text("Describe yourself in a few sentences...") },
      modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
        .testTag("onboarding_bio_input"),
      maxLines = 5,
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CoralPrimary,
        focusedLabelColor = CoralPrimary
      )
    )

    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

    Text(
      text = "Lifestyle & Vitals",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      OutlinedTextField(
        value = height,
        onValueChange = onHeightChange,
        label = { Text("Height") },
        placeholder = { Text("5'9\"") },
        singleLine = true,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp)
      )
      OutlinedTextField(
        value = zodiac,
        onValueChange = onZodiacChange,
        label = { Text("Zodiac") },
        placeholder = { Text("Sagittarius ♐") },
        singleLine = true,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp)
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      OutlinedTextField(
        value = pets,
        onValueChange = onPetsChange,
        label = { Text("Pets") },
        placeholder = { Text("Cat lover 🐾") },
        singleLine = true,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp)
      )
      OutlinedTextField(
        value = drinking,
        onValueChange = onDrinkingChange,
        label = { Text("Drinking") },
        placeholder = { Text("Socially 🍷") },
        singleLine = true,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp)
      )
    }
  }
}

// ==========================================
// Step 7: Review & Launch Card
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepReviewLaunch(
  profile: UserProfile
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(18.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth()
  ) {
    Surface(
      color = CoralPrimary.copy(alpha = 0.12f),
      shape = RoundedCornerShape(20.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = CoralPrimary,
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Your profile is ready!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CoralPrimary
          )
          Text(
            text = "Here's how your card will appear to potential matches in Discover.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Live Profile Preview Card
    Card(
      shape = RoundedCornerShape(24.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(480.dp)
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        val mainPhoto = profile.photos.firstOrNull() ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80"
        AsyncImage(
          model = mainPhoto,
          contentDescription = profile.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Transparent,
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.85f)
                )
              )
            )
        )

        // Card Info content
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(20.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "${profile.name}, ${profile.age}",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = CoralPrimary
            ) {
              Text(
                text = "✓ Verified",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Text(
            text = "${profile.occupation} • ${profile.hometown}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Passion pills
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            profile.passions.take(4).forEach { p ->
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.25f)
              ) {
                Text(
                  text = p,
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Prompt snippet
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.Black.copy(alpha = 0.45f)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = profile.promptQuestion,
                style = MaterialTheme.typography.labelSmall,
                color = CoralPrimary,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "\"${profile.promptAnswer}\"",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
              )
            }
          }
        }
      }
    }
  }
}
