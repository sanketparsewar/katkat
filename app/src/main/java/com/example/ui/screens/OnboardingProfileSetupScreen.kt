package com.example.ui.screens

import android.app.Activity
import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.viewmodel.KatkatViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.ZoomIn
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.ui.components.RomanticVideoBackground
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.LikeGreen
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.PeachSecondary
import com.example.util.LocationHelper
import java.util.Calendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class CountryCodeItem(
  val name: String,
  val code: String,
  val flag: String
)

val SupportedCountryCodes = listOf(
  CountryCodeItem("India", "+91", "🇮🇳"),
  CountryCodeItem("United States", "+1", "🇺🇸"),
  CountryCodeItem("United Kingdom", "+44", "🇬🇧"),
  CountryCodeItem("Australia", "+61", "🇦🇺"),
  CountryCodeItem("Canada", "+1", "🇨🇦"),
  CountryCodeItem("United Arab Emirates", "+971", "🇦🇪"),
  CountryCodeItem("Singapore", "+65", "🇸🇬"),
  CountryCodeItem("Germany", "+49", "🇩🇪"),
  CountryCodeItem("France", "+33", "🇫🇷"),
  CountryCodeItem("Japan", "+81", "🇯🇵")
)

enum class OnboardingFlowStep {
  WELCOME,
  PHONE_ENTRY,
  OTP_VERIFY,
  PERSONAL_INFO,
  PHOTOS,
  CAREER_EDUCATION,
  INTENTIONS_PASSIONS,
  LIFESTYLE_PROMPTS,
  REVIEW_LAUNCH
}

val AllZodiacSigns = listOf(
  "♈ Aries", "♉ Taurus", "♊ Gemini",
  "♋ Cancer", "♌ Leo",
  "♍ Virgo", "♎ Libra", "♏ Scorpio",
  "♐ Sagittarius", "♑ Capricorn",
  "♒ Aquarius", "♓ Pisces"
)

val DomesticPetsList = listOf(
  "🐶 Dog lover", "🐱 Cat lover",
  "🐾 Multiple pets", "🦜 Bird/Aquarium", "🐰 Pet friendly",
  "🚫 No pets"
)

fun <T> chunkByPattern(items: List<T>, pattern: List<Int>): List<List<T>> {
  val result = mutableListOf<List<T>>()
  var currentIndex = 0
  var patternIndex = 0
  while (currentIndex < items.size) {
    val count = pattern[patternIndex % pattern.size]
    val nextIndex = (currentIndex + count).coerceAtMost(items.size)
    result.add(items.subList(currentIndex, nextIndex))
    currentIndex = nextIndex
    patternIndex++
  }
  return result
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingProfileSetupScreen(
  initialProfile: UserProfile,
  onComplete: (UserProfile) -> Unit,
  onExistingUserFound: (UserProfile) -> Unit = {},
  viewModel: KatkatViewModel? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var currentStep by remember { mutableStateOf(OnboardingFlowStep.WELCOME) }

  // 1. Phone & Auth State (Blank by default)
  var selectedCountryCode by remember { mutableStateOf(initialProfile.countryCode.ifBlank { "+91" }) }
  var phoneNumber by remember { mutableStateOf(initialProfile.phoneNumber) }
  var email by remember { mutableStateOf(initialProfile.email) }
  var isPhoneVerified by remember { mutableStateOf(initialProfile.isPhoneVerified) }

  // OTP State (6-Digit Firebase Authentication)
  var otpCode by remember { mutableStateOf("") }
  var isOtpError by remember { mutableStateOf(false) }
  var otpErrorMessage by remember { mutableStateOf<String?>(null) }
  var firebaseVerificationId by remember { mutableStateOf("") }
  var isSendingOtp by remember { mutableStateOf(false) }
  var isVerifyingOtp by remember { mutableStateOf(false) }
  var otpResendCountdown by remember { mutableIntStateOf(60) }
  var isOtpTimerRunning by remember { mutableStateOf(false) }

  // State for previously deleted profile restoration or deletion
  var pendingDeletedProfile by remember { mutableStateOf<UserProfile?>(null) }
  var showRestoreDeletedDialog by remember { mutableStateOf(false) }
  var isProcessingDeletedAction by remember { mutableStateOf(false) }

  val checkUserStatusAndProceed: (String, String) -> Unit = { natNumber, cCode ->
    coroutineScope.launch {
      // 1. Check if user already exists with an active completed profile
      val existing = viewModel?.checkExistingUser(natNumber, cCode)
      if (existing != null && existing.isOnboardingCompleted && existing.name.isNotBlank()) {
        isVerifyingOtp = false
        onExistingUserFound(existing)
        return@launch
      }

      // 2. Check if a previously deleted profile exists in deleted_accounts collection
      val deleted = viewModel?.checkDeletedAccount(natNumber, cCode)
      if (deleted != null && deleted.name.isNotBlank()) {
        isVerifyingOtp = false
        pendingDeletedProfile = deleted
        showRestoreDeletedDialog = true
        return@launch
      }

      // 3. Otherwise brand new profile setup
      isVerifyingOtp = false
      Toast.makeText(context, "✓ Phone verified! Please complete your profile ✨", Toast.LENGTH_SHORT).show()
      currentStep = OnboardingFlowStep.PERSONAL_INFO
    }
  }

  // 2. Personal Information State (Blank by default)
  var name by remember { mutableStateOf("") }
  var birthDay by remember { mutableStateOf("") }
  var birthMonth by remember { mutableStateOf("") }
  var birthYear by remember { mutableStateOf("") }
  var hometown by remember { mutableStateOf("") }
  var currentLocationCity by remember { mutableStateOf("") }
  var currentLocationCountry by remember { mutableStateOf("") }
  var currentLat by remember { mutableStateOf(0.0) }
  var currentLon by remember { mutableStateOf(0.0) }
  var isFetchingGps by remember { mutableStateOf(false) }
  var gpsDetectedBadge by remember { mutableStateOf<String?>(null) }
  var gender by remember { mutableStateOf("") }
  var pronouns by remember { mutableStateOf("") }

  // 3. Photos State (Direct Firebase Storage Upload)
  var photos by remember { mutableStateOf(initialProfile.photos.toMutableList()) }
  var isUploadingPhoto by remember { mutableStateOf(false) }

  // 4. Work & Education
  var occupation by remember { mutableStateOf("") }
  var education by remember { mutableStateOf("") }

  // 5. Intentions & Passions
  var datingIntention by remember { mutableStateOf("") }
  var selectedPassions by remember { mutableStateOf(setOf<String>()) }

  // 6. Lifestyle & Prompts
  var height by remember { mutableStateOf("") }
  var zodiac by remember { mutableStateOf("") }
  var selectedPets by remember { mutableStateOf(setOf<String>()) }
  var drinking by remember { mutableStateOf("") }
  var smoking by remember { mutableStateOf("") }
  var promptQuestion by remember { mutableStateOf("My simple pleasures in life...") }
  var promptAnswer by remember { mutableStateOf("") }
  var bio by remember { mutableStateOf("") }

  // Calculate live age from DOB with validation checks
  val calculatedAge by remember {
    derivedStateOf {
      try {
        if (birthYear.length == 4 && birthMonth.isNotBlank() && birthDay.isNotBlank()) {
          val y = birthYear.toInt()
          val m = birthMonth.toInt()
          val d = birthDay.toInt()
          val today = Calendar.getInstance()
          var ageVal = today.get(Calendar.YEAR) - y
          if (today.get(Calendar.MONTH) + 1 < m || (today.get(Calendar.MONTH) + 1 == m && today.get(Calendar.DAY_OF_MONTH) < d)) {
            ageVal--
          }
          ageVal
        } else {
          0
        }
      } catch (e: Exception) {
        0
      }
    }
  }

  // Function to perform multi-tier location fetch
  val performLocationFetch: () -> Unit = {
    coroutineScope.launch {
      isFetchingGps = true
      try {
        val result = LocationHelper.getCurrentLocation(context)
        isFetchingGps = false
        if (result.isSuccess && (result.city.isNotBlank() || result.country.isNotBlank())) {
          currentLocationCity = result.city
          currentLocationCountry = result.country
          currentLat = result.latitude
          currentLon = result.longitude
          val displayLoc = if (result.city.isNotBlank() && result.country.isNotBlank()) {
            "${result.city}, ${result.country}"
          } else {
            result.city.ifBlank { result.country }
          }
          gpsDetectedBadge = "Location: $displayLoc"
          Toast.makeText(context, "📍 Location detected: $displayLoc", Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(context, "Location detection timed out. Please try again.", Toast.LENGTH_SHORT).show()
        }
      } catch (e: Exception) {
        isFetchingGps = false
        Toast.makeText(context, "Location error: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  // Location Permission Launcher
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { _ ->
    performLocationFetch()
  }

  val triggerLocationDetection: () -> Unit = {
    if (LocationHelper.hasLocationPermission(context)) {
      performLocationFetch()
    } else {
      locationPermissionLauncher.launch(
        arrayOf(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        )
      )
    }
  }

  var showPhotoSourceDialog by remember { mutableStateOf(false) }
  var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

  // Unified photo processor (handles both Firebase Cloud Storage and resilient local fallback)
  val processAndAddPhotoUri: (Uri) -> Unit = { uri ->
    coroutineScope.launch {
      isUploadingPhoto = true
      try {
        val userId = viewModel?.getEffectiveUserId() ?: run {
          val currentAuthUid = viewModel?.phoneAuthManager?.currentUserId
          val cleanPhone = phoneNumber.filter { it.isDigit() }
          if (!currentAuthUid.isNullOrBlank()) {
            currentAuthUid
          } else if (initialProfile.id.isNotBlank() && initialProfile.id != "my_profile") {
            initialProfile.id
          } else {
            "user_${cleanPhone.ifBlank { System.currentTimeMillis().toString() }}"
          }
        }
        val uploadedUrl = if (viewModel != null) {
          viewModel.uploadProfilePhoto(context, userId, uri)
        } else {
          ""
        }
        if (uploadedUrl.isNotBlank()) {
          if (photos.size < 6) {
            photos = (photos + uploadedUrl).toMutableList()
          } else {
            photos = photos.toMutableList().apply { set(5, uploadedUrl) }
          }
          Toast.makeText(context, "Photo uploaded", Toast.LENGTH_SHORT).show()
        } else {
          val err = viewModel?.firebaseStorageManager?.lastErrorMessage ?: "Could not process photo. Please try another image."
          Toast.makeText(context, err, Toast.LENGTH_LONG).show()
        }
      } catch (e: Exception) {
        Toast.makeText(context, "Upload error: ${e.message}", Toast.LENGTH_SHORT).show()
      } finally {
        isUploadingPhoto = false
      }
    }
  }

  // Camera Launcher (TakePicture contract with FileProvider Uri)
  val takeCameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
  ) { success: Boolean ->
    if (success && tempCameraUri != null) {
      processAndAddPhotoUri(tempCameraUri!!)
    }
  }

  // Camera Permission Launcher
  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (isGranted) {
      try {
        val photoFile = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          photoFile
        )
        tempCameraUri = uri
        takeCameraLauncher.launch(uri)
      } catch (e: Exception) {
        Toast.makeText(context, "Could not open camera: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    } else {
      Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
    }
  }

  val launchCamera: () -> Unit = {
    val hasCamPermission = androidx.core.content.ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.CAMERA
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    if (hasCamPermission) {
      try {
        val photoFile = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          photoFile
        )
        tempCameraUri = uri
        takeCameraLauncher.launch(uri)
      } catch (e: Exception) {
        Toast.makeText(context, "Could not open camera: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    } else {
      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  // Gallery Picker (Photo picker)
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      processAndAddPhotoUri(uri)
    }
  }

  // File Picker (GetContent for storage/documents/files)
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      processAndAddPhotoUri(uri)
    }
  }

  // OTP Countdown Timer
  LaunchedEffect(isOtpTimerRunning, otpResendCountdown) {
    if (isOtpTimerRunning && otpResendCountdown > 0) {
      delay(1000)
      otpResendCountdown--
    } else if (otpResendCountdown == 0) {
      isOtpTimerRunning = false
    }
  }



  // Step 1: WELCOME VIDEO SCREEN
  if (currentStep == OnboardingFlowStep.WELCOME) {
    Box(modifier = modifier.fillMaxSize().statusBarsPadding()) {
      RomanticVideoBackground()

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Top Center Header
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(top = 36.dp)
        ) {
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  colors = listOf(CoralPrimary, CoralDark)
                )
              )
              .shadow(16.dp, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = "Katkat",
              tint = Color.White,
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Katkat",
            style = MaterialTheme.typography.headlineLarge.copy(
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 2.sp,
              fontSize = 38.sp
            ),
            color = Color.White
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Where genuine sparks ignite 💕",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
          )
        }

        // Bottom Side Centered Button
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          Button(
            onClick = {
              currentStep = OnboardingFlowStep.PHONE_ENTRY
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(58.dp)
              .shadow(12.dp, RoundedCornerShape(29.dp))
              .testTag("welcome_get_started_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = CoralPrimary,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(29.dp)
          ) {
            Text(
              text = "Get Started",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = "By continuing, you agree to Katkat Safety & Community Terms",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
          )
        }
      }
    }
    return
  }

  // Multi-step Scaffold with top padding for all pages
  Scaffold(
    topBar = {
      val isAuthStep = currentStep == OnboardingFlowStep.PHONE_ENTRY || currentStep == OnboardingFlowStep.OTP_VERIFY
      val currentStepNumber = when (currentStep) {
        OnboardingFlowStep.PERSONAL_INFO -> 1
        OnboardingFlowStep.PHOTOS -> 2
        OnboardingFlowStep.CAREER_EDUCATION -> 3
        OnboardingFlowStep.INTENTIONS_PASSIONS -> 4
        OnboardingFlowStep.LIFESTYLE_PROMPTS -> 5
        OnboardingFlowStep.REVIEW_LAUNCH -> 6
        else -> 0
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .background(MaterialTheme.colorScheme.surface)
          .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          IconButton(
            onClick = {
              val allSteps = OnboardingFlowStep.entries
              val idx = allSteps.indexOf(currentStep)
              if (idx > 0) {
                currentStep = allSteps[idx - 1]
              }
            }
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = null,
              tint = CoralPrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Katkat",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = CoralPrimary
            )
          }

          if (isAuthStep) {
            Text(
              text = "Phone Verification",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.SemiBold
            )
          } else if (currentStepNumber > 0) {
            Text(
              text = "Step $currentStepNumber of 6",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.SemiBold
            )
          } else {
            Spacer(modifier = Modifier.width(48.dp))
          }
        }

        if (!isAuthStep && currentStepNumber > 0) {
          Spacer(modifier = Modifier.height(8.dp))

          val progressVal = currentStepNumber.toFloat() / 6f
          LinearProgressIndicator(
            progress = { progressVal },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = CoralPrimary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
          )
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
          if (targetState.ordinal > initialState.ordinal) {
            slideInHorizontally { width -> width } + fadeIn() togetherWith
                slideOutHorizontally { width -> -width } + fadeOut()
          } else {
            slideInHorizontally { width -> -width } + fadeIn() togetherWith
                slideOutHorizontally { width -> width } + fadeOut()
          }
        },
        label = "onboarding_step_anim"
      ) { targetStep ->
        when (targetStep) {
          OnboardingFlowStep.WELCOME -> { /* handled above */ }

          // Step 2: Phone Authentication Screen (Email option removed)
          OnboardingFlowStep.PHONE_ENTRY -> {
            PhoneEntryStep(
              countryCode = selectedCountryCode,
              onCountryCodeChange = { selectedCountryCode = it },
              phoneNumber = phoneNumber,
              onPhoneNumberChange = { phoneNumber = it },
              isSendingOtp = isSendingOtp,
              onSendOtp = {
                val digitsOnly = phoneNumber.filter { it.isDigit() }
                val codeDigits = selectedCountryCode.filter { it.isDigit() }
                val nationalNumber = if (digitsOnly.startsWith(codeDigits) && digitsOnly.length > codeDigits.length) {
                  digitsOnly.substring(codeDigits.length)
                } else {
                  digitsOnly
                }
                if (nationalNumber.length == 10) {
                  val fullPhone = "+$codeDigits$nationalNumber"
                  val act = context as? Activity
                  if (act != null && viewModel != null) {
                    isSendingOtp = true
                    viewModel.phoneAuthManager.sendVerificationCode(
                      activity = act,
                      fullPhoneNumber = fullPhone,
                      onCodeSent = { vId ->
                        isSendingOtp = false
                        firebaseVerificationId = vId
                        otpCode = ""
                        isOtpError = false
                        otpErrorMessage = null
                        otpResendCountdown = 60
                        isOtpTimerRunning = true
                        if (nationalNumber == "8830392209" || fullPhone.contains("8830392209")) {
                          Toast.makeText(context, "📲 Firebase Code sent! (Firebase Test OTP: 123456)", Toast.LENGTH_LONG).show()
                        } else {
                          Toast.makeText(context, "📲 Verification code sent to $fullPhone!", Toast.LENGTH_SHORT).show()
                        }
                        currentStep = OnboardingFlowStep.OTP_VERIFY
                      },
                      onAutoVerified = {
                        isSendingOtp = false
                        isPhoneVerified = true
                        phoneNumber = nationalNumber
                        checkUserStatusAndProceed(nationalNumber, selectedCountryCode)
                      },
                      onError = { errorMsg ->
                        isSendingOtp = false
                        val fallbackId = "fallback_${System.currentTimeMillis()}"
                        firebaseVerificationId = fallbackId
                        Toast.makeText(context, "Notice: $errorMsg (Use OTP 123456)", Toast.LENGTH_LONG).show()
                        otpCode = ""
                        isOtpError = false
                        otpErrorMessage = null
                        otpResendCountdown = 60
                        isOtpTimerRunning = true
                        currentStep = OnboardingFlowStep.OTP_VERIFY
                      }
                    )
                  } else {
                    otpCode = ""
                    isOtpError = false
                    otpErrorMessage = null
                    otpResendCountdown = 60
                    isOtpTimerRunning = true
                    Toast.makeText(context, "📲 Verification code sent! (Test OTP: 123456)", Toast.LENGTH_SHORT).show()
                    currentStep = OnboardingFlowStep.OTP_VERIFY
                  }
                } else {
                  Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                }
              }
            )
          }

          // Step 3: OTP Verification Screen (6-Digit Firebase Authentication)
          OnboardingFlowStep.OTP_VERIFY -> {
            OtpVerificationStep(
              countryCode = selectedCountryCode,
              phoneNumber = phoneNumber,
              onEditPhone = { currentStep = OnboardingFlowStep.PHONE_ENTRY },
              otpCode = otpCode,
              onOtpCodeChange = { code ->
                otpCode = code
                isOtpError = false
                otpErrorMessage = null
              },
              isError = isOtpError,
              errorMessage = otpErrorMessage,
              countdown = otpResendCountdown,
              isTimerRunning = isOtpTimerRunning,
              isVerifying = isVerifyingOtp,
              onResendOtp = {
                val digitsOnly = phoneNumber.filter { it.isDigit() }
                val codeDigits = selectedCountryCode.filter { it.isDigit() }
                val nationalNumber = if (digitsOnly.startsWith(codeDigits) && digitsOnly.length > codeDigits.length) {
                  digitsOnly.substring(codeDigits.length)
                } else {
                  digitsOnly
                }
                val fullPhone = "+$codeDigits$nationalNumber"
                val act = context as? Activity
                if (act != null && viewModel != null) {
                  viewModel.phoneAuthManager.sendVerificationCode(
                    activity = act,
                    fullPhoneNumber = fullPhone,
                    onCodeSent = { vId ->
                      firebaseVerificationId = vId
                      otpCode = ""
                      isOtpError = false
                      otpErrorMessage = null
                      otpResendCountdown = 60
                      isOtpTimerRunning = true
                      Toast.makeText(context, "📲 New code sent to $fullPhone!", Toast.LENGTH_SHORT).show()
                    },
                    onAutoVerified = {
                      isPhoneVerified = true
                      phoneNumber = nationalNumber
                      checkUserStatusAndProceed(nationalNumber, selectedCountryCode)
                    },
                    onError = { err ->
                      Toast.makeText(context, "Resend notice: $err", Toast.LENGTH_SHORT).show()
                    }
                  )
                } else {
                  otpCode = ""
                  isOtpError = false
                  otpErrorMessage = null
                  otpResendCountdown = 60
                  isOtpTimerRunning = true
                  Toast.makeText(context, "📲 New code sent! (Use 123456)", Toast.LENGTH_SHORT).show()
                }
              },
              onAutofillDemo = {
                otpCode = "123456"
                isOtpError = false
                otpErrorMessage = null
              },
              onContinue = { inputCode ->
                val fullCode = inputCode.ifBlank { otpCode }
                if (fullCode.length < 6) return@OtpVerificationStep

                isVerifyingOtp = true
                isOtpError = false
                otpErrorMessage = null

                val handleVerificationSuccess: () -> Unit = {
                  isPhoneVerified = true
                  val digitsOnly = phoneNumber.filter { it.isDigit() }
                  val codeDigits = selectedCountryCode.filter { it.isDigit() }
                  val nationalNumber = if (digitsOnly.startsWith(codeDigits) && digitsOnly.length > codeDigits.length) {
                    digitsOnly.substring(codeDigits.length)
                  } else {
                    digitsOnly
                  }
                  phoneNumber = nationalNumber
                  checkUserStatusAndProceed(nationalNumber, selectedCountryCode)
                }

                if (viewModel != null) {
                  val digitsOnly = phoneNumber.filter { it.isDigit() }
                  val codeDigits = selectedCountryCode.filter { it.isDigit() }
                  val nationalNumber = if (digitsOnly.startsWith(codeDigits) && digitsOnly.length > codeDigits.length) {
                    digitsOnly.substring(codeDigits.length)
                  } else {
                    digitsOnly
                  }
                  viewModel.phoneAuthManager.lastRequestedPhoneNumber = "+$codeDigits$nationalNumber"
                  viewModel.phoneAuthManager.verifyCode(
                    verificationId = firebaseVerificationId,
                    code = fullCode,
                    onSuccess = {
                      handleVerificationSuccess()
                    },
                    onError = { errorMsg ->
                      isVerifyingOtp = false
                      isOtpError = true
                      otpErrorMessage = errorMsg
                      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                  )
                } else {
                  handleVerificationSuccess()
                }
              }
            )
          }

          // Step 4: Personal Information & GPS Location
          OnboardingFlowStep.PERSONAL_INFO -> {
            PersonalInfoStep(
              name = name,
              onNameChange = { name = it },
              birthDay = birthDay,
              onBirthDayChange = { birthDay = it },
              birthMonth = birthMonth,
              onBirthMonthChange = { birthMonth = it },
              birthYear = birthYear,
              onBirthYearChange = { birthYear = it },
              calculatedAge = calculatedAge,
              hometown = hometown,
              onHometownChange = { hometown = it },
              currentLocationCity = currentLocationCity,
              onCityChange = { currentLocationCity = it },
              currentLocationCountry = currentLocationCountry,
              isFetchingGps = isFetchingGps,
              gpsBadge = gpsDetectedBadge,
              onTriggerGps = triggerLocationDetection,
              gender = gender,
              onGenderChange = { gender = it },
              pronouns = pronouns,
              onPronounsChange = { pronouns = it },
              onNext = {
                if (name.isBlank()) {
                  Toast.makeText(context, "Please enter your first name", Toast.LENGTH_SHORT).show()
                } else if (birthDay.isBlank() || birthMonth.isBlank() || birthYear.length < 4) {
                  Toast.makeText(context, "Please enter your complete date of birth (DD / MM / YYYY)", Toast.LENGTH_SHORT).show()
                } else if (calculatedAge < 18) {
                  Toast.makeText(context, "You must be at least 18 years old to join Katkat", Toast.LENGTH_SHORT).show()
                } else if (calculatedAge > 80) {
                  Toast.makeText(context, "Age must be 80 or younger", Toast.LENGTH_SHORT).show()
                } else {
                  currentStep = OnboardingFlowStep.PHOTOS
                }
              }
            )
          }

          // Step 2: Photos (Stored in Firebase Storage with Camera & File options)
          OnboardingFlowStep.PHOTOS -> {
            PhotosStep(
              photos = photos,
              isUploading = isUploadingPhoto,
              hasStoragePermissionError = viewModel?.firebaseStorageManager?.hasStoragePermissionError == true,
              onAddPhoto = {
                showPhotoSourceDialog = true
              },
              onRemovePhoto = { idx ->
                if (photos.size > 1) {
                  photos = photos.toMutableList().apply { removeAt(idx) }
                }
              },
              onSetPrimary = { idx ->
                if (idx in 1 until photos.size) {
                  val list = photos.toMutableList()
                  val item = list.removeAt(idx)
                  list.add(0, item)
                  photos = list
                }
              },
              onNext = {
                if (photos.size < 2) {
                  Toast.makeText(context, "Please add at least 2 photos to continue", Toast.LENGTH_SHORT).show()
                } else {
                  currentStep = OnboardingFlowStep.CAREER_EDUCATION
                }
              }
            )
          }

          // Step 6: Career & Education
          OnboardingFlowStep.CAREER_EDUCATION -> {
            CareerEducationStep(
              occupation = occupation,
              onOccupationChange = { occupation = it },
              education = education,
              onEducationChange = { education = it },
              onNext = { currentStep = OnboardingFlowStep.INTENTIONS_PASSIONS }
            )
          }

          // Step 7: Dating Goals & Passions
          OnboardingFlowStep.INTENTIONS_PASSIONS -> {
            IntentionsPassionsStep(
              intention = datingIntention,
              onIntentionChange = { datingIntention = it },
              selectedPassions = selectedPassions,
              onTogglePassion = { pass ->
                val cleanPass = pass.filter { it.isLetter() }.lowercase()
                val existing = selectedPassions.find { sel ->
                  sel == pass || (cleanPass.isNotEmpty() && sel.filter { it.isLetter() }.equals(cleanPass, ignoreCase = true))
                }
                selectedPassions = if (existing != null) {
                  selectedPassions - existing
                } else {
                  if (selectedPassions.size < 8) selectedPassions + pass else selectedPassions
                }
              },
              onNext = { currentStep = OnboardingFlowStep.LIFESTYLE_PROMPTS }
            )
          }

          // Step 8: Lifestyle & Prompts
          OnboardingFlowStep.LIFESTYLE_PROMPTS -> {
            val petsString = selectedPets.joinToString(", ")
            LifestylePromptsStep(
              promptQuestion = promptQuestion,
              onPromptQuestionChange = { promptQuestion = it },
              promptAnswer = promptAnswer,
              onPromptAnswerChange = { promptAnswer = it },
              bio = bio,
              onBioChange = { bio = it },
              height = height,
              onHeightChange = { height = it },
              zodiac = zodiac,
              onZodiacChange = { zodiac = it },
              selectedPets = selectedPets,
              onTogglePet = { pet ->
                val cleanPet = pet.filter { it.isLetter() }.lowercase()
                val existing = selectedPets.find { sel ->
                  sel == pet || (cleanPet.isNotEmpty() && sel.filter { it.isLetter() }.equals(cleanPet, ignoreCase = true))
                }
                selectedPets = if (existing != null) {
                  selectedPets - existing
                } else {
                  selectedPets + pet
                }
              },
              drinking = drinking,
              onDrinkingChange = { drinking = it },
              smoking = smoking,
              onSmokingChange = { smoking = it },
              onNext = { currentStep = OnboardingFlowStep.REVIEW_LAUNCH }
            )
          }

          // Step 9: Review & Launch Profile
          OnboardingFlowStep.REVIEW_LAUNCH -> {
            val petsString = selectedPets.joinToString(", ")
            val cleanPhoneDigits = phoneNumber.filter { it.isDigit() }
            val userId = viewModel?.getEffectiveUserId() ?: run {
              val currentAuthUid = viewModel?.phoneAuthManager?.currentUserId
              if (!currentAuthUid.isNullOrBlank()) {
                currentAuthUid
              } else if (initialProfile.id.isNotBlank() && initialProfile.id != "my_profile") {
                initialProfile.id
              } else {
                "user_${cleanPhoneDigits.ifBlank { System.currentTimeMillis().toString() }}"
              }
            }
            val finalProfile = UserProfile(
              id = userId,
              name = name.trim().ifBlank { "Alex" },
              age = if (calculatedAge >= 18) calculatedAge else 24,
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
              pets = petsString,
              passions = selectedPassions.toList(),
              photos = photos,
              promptQuestion = promptQuestion,
              promptAnswer = promptAnswer.trim(),
              isOnboardingCompleted = true,
              phoneNumber = phoneNumber.trim(),
              countryCode = selectedCountryCode,
              email = email.trim(),
              dob = if (birthYear.isNotBlank()) "$birthYear-$birthMonth-$birthDay" else "",
              currentLocationCity = currentLocationCity.trim(),
              currentLocationCountry = currentLocationCountry.trim(),
              latitude = currentLat,
              longitude = currentLon,
              isPhoneVerified = true
            )

            ReviewAndLaunchStep(
              profile = finalProfile,
              onComplete = {
                onComplete(finalProfile)
              }
            )
          }
        }
      }
    }

    if (showPhotoSourceDialog) {
      AlertDialog(
        onDismissRequest = { showPhotoSourceDialog = false },
        title = {
          Text(
            text = "Select Photo",
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
              text = "Choose a source to add your profile photo:",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Option 1: Camera
            Surface(
              onClick = {
                showPhotoSourceDialog = false
                launchCamera()
              },
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.3f)),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("photo_source_camera")
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = CircleShape,
                  color = CoralPrimary.copy(alpha = 0.15f),
                  modifier = Modifier.size(42.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      Icons.Default.PhotoCamera,
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
                    "Take a photo with your camera",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            // Option 2: Photo Gallery
            Surface(
              onClick = {
                showPhotoSourceDialog = false
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.3f)),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("photo_source_gallery")
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = CircleShape,
                  color = CoralPrimary.copy(alpha = 0.15f),
                  modifier = Modifier.size(42.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      Icons.Default.PhotoLibrary,
                      contentDescription = "Gallery",
                      tint = CoralPrimary,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                  Text(
                    "Gallery",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    "Choose an image from your photo album",
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
          TextButton(onClick = { showPhotoSourceDialog = false }) {
            Text("Cancel", color = CoralPrimary)
          }
        }
      )
    }

    // Previously Deleted Account Pop-up Dialog
    if (showRestoreDeletedDialog && pendingDeletedProfile != null) {
      val deletedProfile = pendingDeletedProfile!!
      AlertDialog(
        onDismissRequest = {
          // Keep dialog open until user selects an action
        },
        icon = {
          Icon(
            imageVector = Icons.Default.Restore,
            contentDescription = null,
            tint = CoralPrimary,
            modifier = Modifier.size(34.dp)
          )
        },
        title = {
          Text(
            text = "Previous Profile Found",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
        },
        text = {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Text(
              text = "A previously deleted profile was found for this mobile number ($selectedCountryCode $phoneNumber).",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )

            // Preview card of the found deleted profile
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                if (deletedProfile.photos.isNotEmpty()) {
                  AsyncImage(
                    model = deletedProfile.photos.first(),
                    contentDescription = deletedProfile.name,
                    modifier = Modifier
                      .size(54.dp)
                      .clip(CircleShape),
                    contentScale = ContentScale.Crop
                  )
                } else {
                  Surface(
                    shape = CircleShape,
                    color = CoralPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.size(54.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = deletedProfile.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = CoralPrimary,
                        fontSize = 22.sp
                      )
                    }
                  }
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${deletedProfile.name}${if (deletedProfile.age > 0) ", ${deletedProfile.age}" else ""}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  if (deletedProfile.occupation.isNotBlank()) {
                    Text(
                      text = deletedProfile.occupation,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                  }
                  if (deletedProfile.bio.isNotBlank()) {
                    Text(
                      text = deletedProfile.bio,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                      maxLines = 2,
                      overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }

            Text(
              text = "Would you like to get your previously deleted profile or create a new one? Selecting 'Create New' will permanently delete your previous profile data.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              if (isProcessingDeletedAction) return@Button
              isProcessingDeletedAction = true
              coroutineScope.launch {
                viewModel?.restoreDeletedAccount(deletedProfile)
                isProcessingDeletedAction = false
                showRestoreDeletedDialog = false
                Toast.makeText(context, "✓ Welcome back, ${deletedProfile.name}! Profile restored ✨", Toast.LENGTH_SHORT).show()
                onExistingUserFound(deletedProfile)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
            modifier = Modifier.testTag("btn_restore_deleted_profile")
          ) {
            if (isProcessingDeletedAction) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = androidx.compose.ui.graphics.Color.White,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(6.dp))
            }
            Text("Get Previously Deleted Profile", fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          OutlinedButton(
            onClick = {
              if (isProcessingDeletedAction) return@OutlinedButton
              isProcessingDeletedAction = true
              coroutineScope.launch {
                viewModel?.permanentlyDeleteDeletedAccount(
                  userId = deletedProfile.id,
                  phone = phoneNumber,
                  countryCode = selectedCountryCode
                )
                isProcessingDeletedAction = false
                showRestoreDeletedDialog = false
                pendingDeletedProfile = null
                Toast.makeText(context, "Previous profile permanently removed. Let's create your new profile! ✨", Toast.LENGTH_SHORT).show()
                currentStep = OnboardingFlowStep.PERSONAL_INFO
              }
            },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.testTag("btn_create_new_profile")
          ) {
            Text("Create New")
          }
        }
      )
    }
  }
}

// =========================================================================
// 2nd PAGE: Phone Number Verification Component (Clean, reduced padding, no icon, wide input)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhoneEntryStep(
  countryCode: String,
  onCountryCodeChange: (String) -> Unit,
  phoneNumber: String,
  onPhoneNumberChange: (String) -> Unit,
  isSendingOtp: Boolean = false,
  onSendOtp: () -> Unit
) {
  var isDropdownExpanded by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 18.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      // Subheading
      Text(
        text = "Please share your number to get started",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Subheading Message
      Text(
        text = "This helps us to verify and make katkat safe place to foster genuine connections.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 22.sp
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Phone Input Row (Wide input, reduced padding, no phone icon, empty placeholder)
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Country Code Selector (Compact with minimal internal padding)
        ExposedDropdownMenuBox(
          expanded = isDropdownExpanded,
          onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
          modifier = Modifier.width(96.dp)
        ) {
          val selectedCountry = SupportedCountryCodes.find { it.code == countryCode } ?: SupportedCountryCodes.first()
          Surface(
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
          ) {
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = "${selectedCountry.flag} ${selectedCountry.code}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(0.dp) // dummy to satisfy layout
              )
            }
          }

          ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false }
          ) {
            SupportedCountryCodes.forEach { item ->
              DropdownMenuItem(
                text = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.flag, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${item.name} (${item.code})", fontSize = 14.sp)
                  }
                },
                onClick = {
                  onCountryCodeChange(item.code)
                  isDropdownExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Mobile Number Field: blank placeholder, no phone icon, full width (restricted to 10 digits)
        OutlinedTextField(
          value = phoneNumber,
          onValueChange = { input ->
            val filtered = input.filter { it.isDigit() }
            if (filtered.length <= 10) {
              onPhoneNumberChange(filtered)
            }
          },
          modifier = Modifier
            .weight(1f)
            .height(54.dp)
            .testTag("phone_number_input"),
          placeholder = null,
          trailingIcon = {
            if (phoneNumber.isNotEmpty()) {
              IconButton(onClick = { onPhoneNumberChange("") }) {
                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CoralPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
          )
        )
      }

      if (phoneNumber.isEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          onClick = { onPhoneNumberChange("8830392209") },
          shape = RoundedCornerShape(12.dp),
          color = CoralPrimary.copy(alpha = 0.08f),
          border = BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.25f)),
          modifier = Modifier.padding(top = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Tap to test with 8830392209 (OTP: 123456)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = CoralPrimary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = CoralPrimary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "We never share your contact details. Phone verification keeps Katkat authentic & bot-free.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(36.dp))

    // Send OTP Button
    Button(
      onClick = onSendOtp,
      enabled = phoneNumber.trim().length == 10 && !isSendingOtp,
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .testTag("send_otp_button"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = CoralPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      if (isSendingOtp) {
        CircularProgressIndicator(
          color = Color.White,
          modifier = Modifier.size(20.dp),
          strokeWidth = 2.dp
        )
      } else {
        Text(
          text = "Send OTP",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
      }
    }
  }
}

// =========================================================================
// 3rd PAGE: OTP Verification Component (6 Digits for Firebase Auth)
// =========================================================================
// =========================================================================
// 3rd PAGE: OTP Verification Component (6 Digits for Firebase Auth)
// =========================================================================
@Composable
private fun OtpVerificationStep(
  countryCode: String,
  phoneNumber: String,
  onEditPhone: () -> Unit,
  otpCode: String,
  onOtpCodeChange: (String) -> Unit,
  isError: Boolean,
  errorMessage: String?,
  countdown: Int,
  isTimerRunning: Boolean,
  isVerifying: Boolean,
  onResendOtp: () -> Unit,
  onAutofillDemo: () -> Unit,
  onContinue: (String) -> Unit
) {
  val isOtpComplete = otpCode.length == 6

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      // Heading
      Text(
        text = "Verify Phone Number",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 28.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Clean Phone Badge Card with Edit action & Firebase Verification Badge
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.PhoneAndroid,
              contentDescription = null,
              tint = CoralPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "$countryCode $phoneNumber",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
              )
              Text(
                text = "Firebase SMS Verification",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Surface(
            onClick = onEditPhone,
            shape = RoundedCornerShape(10.dp),
            color = CoralPrimary.copy(alpha = 0.12f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit phone number",
                tint = CoralPrimary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Edit",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = CoralPrimary
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "Enter 6-digit code sent to your phone:",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Premium 6-Digit OTP Input Field (Supports paste, autofill, split styling & cursor animation)
      KatkatOtpInputField(
        otpCode = otpCode,
        onOtpCodeChange = onOtpCodeChange,
        isError = isError,
        onOtpComplete = { completedCode ->
          onContinue(completedCode)
        }
      )

      // Error message feedback
      if (isError && !errorMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Demo OTP Auto-fill Chip with Firebase Test Code
      Surface(
        onClick = {
          onAutofillDemo()
          onContinue("123456")
        },
        shape = RoundedCornerShape(14.dp),
        color = CoralPrimary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = CoralPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Firebase Test OTP: 123456 (Tap to auto-fill)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = CoralPrimary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Bottom Section: Resend Timer & Verify Button
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(bottom = 18.dp)
      ) {
        if (isTimerRunning) {
          Text(
            text = "Resend OTP in ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${countdown}s",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = CoralPrimary
          )
        } else {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onResendOtp() }
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              tint = CoralPrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Resend OTP via SMS",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = CoralPrimary
            )
          }
        }
      }

      Button(
        onClick = { onContinue(otpCode) },
        enabled = isOtpComplete && !isVerifying,
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("otp_continue_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = CoralPrimary,
          disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
      ) {
        if (isVerifying) {
          CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(22.dp),
            strokeWidth = 2.5.dp
          )
        } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Verify & Continue",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun KatkatOtpInputField(
  otpCode: String,
  onOtpCodeChange: (String) -> Unit,
  isError: Boolean = false,
  length: Int = 6,
  onOtpComplete: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  var isFocused by remember { mutableStateOf(false) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  // Blinking cursor animation for active empty box
  val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
  val cursorAlpha by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(500),
      repeatMode = RepeatMode.Reverse
    ),
    label = "cursor_alpha"
  )

  Box(
    modifier = modifier.fillMaxWidth(),
    contentAlignment = Alignment.Center
  ) {
    BasicTextField(
      value = otpCode,
      onValueChange = { input ->
        val digitsOnly = input.filter { it.isDigit() }.take(length)
        onOtpCodeChange(digitsOnly)
        if (digitsOnly.length == length && digitsOnly != otpCode) {
          onOtpComplete(digitsOnly)
        }
      },
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.NumberPassword,
        imeAction = ImeAction.Done
      ),
      modifier = Modifier
        .focusRequester(focusRequester)
        .onFocusChanged { isFocused = it.isFocused }
        .testTag("otp_hidden_input"),
      decorationBox = {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null
            ) {
              focusRequester.requestFocus()
              keyboardController?.show()
            },
          horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (index in 0 until length) {
            if (index == 3) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .background(
                    color = if (isError) MaterialTheme.colorScheme.error else CoralPrimary.copy(alpha = 0.6f),
                    shape = CircleShape
                  )
              )
            }

            val char = otpCode.getOrNull(index)?.toString() ?: ""
            val isBoxFocused = isFocused && (index == otpCode.length || (index == length - 1 && otpCode.length == length))
            val isFilled = char.isNotEmpty()

            val boxBorderColor = when {
              isError -> MaterialTheme.colorScheme.error
              isBoxFocused -> CoralPrimary
              isFilled -> CoralPrimary.copy(alpha = 0.7f)
              else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            }

            val boxBgColor = when {
              isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)
              isBoxFocused -> CoralPrimary.copy(alpha = 0.08f)
              isFilled -> MaterialTheme.colorScheme.surface
              else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }

            val strokeWidth = if (isBoxFocused || isError) 2.dp else 1.2.dp

            Surface(
              shape = RoundedCornerShape(16.dp),
              color = boxBgColor,
              border = BorderStroke(strokeWidth, boxBorderColor),
              modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null
                ) {
                  focusRequester.requestFocus()
                  keyboardController?.show()
                }
                .testTag("otp_digit_box_$index")
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
              ) {
                if (char.isNotEmpty()) {
                  Text(
                    text = char,
                    style = MaterialTheme.typography.headlineMedium.copy(
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 24.sp,
                      textAlign = TextAlign.Center
                    ),
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                  )
                } else if (isBoxFocused && index == otpCode.length) {
                  Box(
                    modifier = Modifier
                      .width(2.5.dp)
                      .height(24.dp)
                      .alpha(cursorAlpha)
                      .background(CoralPrimary, shape = RoundedCornerShape(1.dp))
                  )
                }
              }
            }
          }
        }
      }
    )
  }

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }
}

// =========================================================================
// 4th PAGE: Personal Information (Only Fields & Placeholders, Tap to Detect GPS, Blank Default)
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonalInfoStep(
  name: String,
  onNameChange: (String) -> Unit,
  birthDay: String,
  onBirthDayChange: (String) -> Unit,
  birthMonth: String,
  onBirthMonthChange: (String) -> Unit,
  birthYear: String,
  onBirthYearChange: (String) -> Unit,
  calculatedAge: Int,
  hometown: String,
  onHometownChange: (String) -> Unit,
  currentLocationCity: String,
  onCityChange: (String) -> Unit,
  currentLocationCountry: String,
  isFetchingGps: Boolean,
  gpsBadge: String?,
  onTriggerGps: () -> Unit,
  gender: String,
  onGenderChange: (String) -> Unit,
  pronouns: String,
  onPronounsChange: (String) -> Unit,
  onNext: () -> Unit
) {
  val genders = listOf("Woman", "Man", "Non-binary", "Other")
  val pronounsList = listOf("She/Her", "He/Him", "They/Them", "She/They", "He/They", "Any pronouns")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text(
        text = "Personal Information",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )

      Text(
        text = "Tell us about yourself to complete your profile.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(24.dp))

      // 1. First Name (Field only with placeholder)
      OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("first_name_input"),
        placeholder = { Text("First Name") },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Date of Birth (3 fields with restrictions: DD <= 31, MM <= 12, YYYY -> live age validation)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = birthDay,
          onValueChange = { input ->
            val clean = input.filter { it.isDigit() }
            if (clean.length <= 2) {
              val num = clean.toIntOrNull()
              if (clean.isEmpty() || (num != null && num in 1..31)) {
                onBirthDayChange(clean)
              }
            }
          },
          modifier = Modifier.weight(1f),
          placeholder = { Text("DD") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CoralPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
          )
        )
        OutlinedTextField(
          value = birthMonth,
          onValueChange = { input ->
            val clean = input.filter { it.isDigit() }
            if (clean.length <= 2) {
              val num = clean.toIntOrNull()
              if (clean.isEmpty() || (num != null && num in 1..12)) {
                onBirthMonthChange(clean)
              }
            }
          },
          modifier = Modifier.weight(1f),
          placeholder = { Text("MM") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CoralPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
          )
        )
        OutlinedTextField(
          value = birthYear,
          onValueChange = { input ->
            val clean = input.filter { it.isDigit() }
            if (clean.length <= 4) {
              onBirthYearChange(clean)
            }
          },
          modifier = Modifier.weight(1.3f),
          placeholder = { Text("YYYY") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CoralPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
          )
        )
      }

      if (birthYear.length == 4 && birthDay.isNotBlank() && birthMonth.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        when {
          calculatedAge in 18..80 -> {
            Text(
              text = "✓ Age: $calculatedAge years old",
              style = MaterialTheme.typography.labelMedium,
              color = LikeGreen,
              fontWeight = FontWeight.SemiBold
            )
          }
          calculatedAge in 1..17 -> {
            Text(
              text = "⚠️ You must be at least 18 years old ($calculatedAge)",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.SemiBold
            )
          }
          calculatedAge > 80 -> {
            Text(
              text = "⚠️ Age must be 80 or younger ($calculatedAge)",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.SemiBold
            )
          }
          else -> {
            Text(
              text = "⚠️ Please enter a valid date of birth",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3. Hometown (Field only with placeholder)
      OutlinedTextField(
        value = hometown,
        onValueChange = onHometownChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Hometown") },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 4. Current Location (No GPS logo, placeholder "Current Location", tap anywhere to auto-detect)
      val locationDisplay = if (currentLocationCity.isNotBlank()) {
        if (currentLocationCountry.isNotBlank()) "$currentLocationCity, $currentLocationCountry" else currentLocationCity
      } else ""

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .clickable(enabled = !isFetchingGps) { onTriggerGps() }
      ) {
        OutlinedTextField(
          value = locationDisplay,
          onValueChange = {},
          readOnly = true,
          enabled = false,
          modifier = Modifier.fillMaxWidth(),
          placeholder = { Text("Current Location") },
          trailingIcon = {
            if (isFetchingGps) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = CoralPrimary)
            } else if (locationDisplay.isNotBlank()) {
              Icon(Icons.Default.CheckCircle, contentDescription = "Location detected", tint = LikeGreen, modifier = Modifier.size(18.dp))
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            disabledBorderColor = if (locationDisplay.isNotBlank()) CoralPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            disabledTrailingIconColor = CoralPrimary
          )
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // 5. Gender Identity (Chips with no pre-selection)
      Text(
        text = "Gender Identity",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(8.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        genders.forEach { item ->
          FilterChip(
            selected = gender.equals(item, ignoreCase = true),
            onClick = { onGenderChange(item) },
            label = { Text(item) },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CoralPrimary,
              selectedLabelColor = Color.White
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 6. Pronouns (Chips with no pre-selection)
      Text(
        text = "Pronouns",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(8.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        pronounsList.forEach { item ->
          FilterChip(
            selected = pronouns.equals(item, ignoreCase = true),
            onClick = { onPronounsChange(item) },
            label = { Text(item) },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = PeachSecondary,
              selectedLabelColor = Color.White
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Next Button (Simple "Next")
    Button(
      onClick = onNext,
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .testTag("personal_info_next_button"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
    ) {
      Text("Next", fontSize = 17.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.width(8.dp))
      Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
    }
  }
}

// =========================================================================
// 5th PAGE: Photos Component (Direct Firebase Storage Integration)
// =========================================================================
@Composable
private fun PhotosStep(
  photos: List<String>,
  isUploading: Boolean = false,
  hasStoragePermissionError: Boolean = false,
  onAddPhoto: () -> Unit,
  onRemovePhoto: (Int) -> Unit,
  onSetPrimary: (Int) -> Unit,
  onNext: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text(
        text = "Your Profile Photos",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )

      Text(
        text = "Add at least 2 photos from camera or files. Your photos will be stored safely.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (hasStoragePermissionError) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              Icons.Default.Info,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(20.dp).padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Firebase Storage Rules Notice",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = "Firebase Storage rules currently deny write ('allow read, write: if false;'). Your photos are saved locally so you can continue onboarding!\n\nTo enable cloud storage sync, update rules in Firebase Console -> Storage -> Rules to:\nallow read, write: if request.auth != null;",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
              )
            }
          }
        }
      }

      if (isUploading) {
        Spacer(modifier = Modifier.height(14.dp))
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CoralPrimary.copy(alpha = 0.12f),
          border = BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp,
              color = CoralPrimary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Uploading image...",
              style = MaterialTheme.typography.bodyMedium,
              color = CoralPrimary,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 6-Photo Grid
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (row in 0..1) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            for (col in 0..2) {
              val slotIndex = row * 3 + col
              val photoUri = photos.getOrNull(slotIndex)

              Box(
                modifier = Modifier
                  .weight(1f)
                  .aspectRatio(0.75f)
                  .clip(RoundedCornerShape(16.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                  .border(
                    BorderStroke(
                      1.5.dp,
                      if (slotIndex == 0) CoralPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    ),
                    RoundedCornerShape(16.dp)
                  )
              ) {
                if (photoUri != null) {
                  AsyncImage(
                    model = photoUri,
                    contentDescription = "Photo $slotIndex",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )

                  if (slotIndex == 0) {
                    Surface(
                      color = CoralPrimary,
                      shape = RoundedCornerShape(bottomEnd = 12.dp),
                      modifier = Modifier.align(Alignment.TopStart)
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("MAIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                      }
                    }
                  } else {
                    IconButton(
                      onClick = { onSetPrimary(slotIndex) },
                      modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(30.dp)
                    ) {
                      Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                      ) {
                        Icon(Icons.Default.Star, contentDescription = "Set primary", tint = Color.White, modifier = Modifier.padding(4.dp))
                      }
                    }
                  }

                  IconButton(
                    onClick = { onRemovePhoto(slotIndex) },
                    modifier = Modifier
                      .align(Alignment.TopEnd)
                      .size(30.dp)
                  ) {
                    Surface(
                      shape = CircleShape,
                      color = Color.Black.copy(alpha = 0.5f),
                      modifier = Modifier.size(24.dp)
                    ) {
                      Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = Color.White, modifier = Modifier.padding(4.dp))
                    }
                  }
                } else {
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .clickable(enabled = !isUploading) { onAddPhoto() },
                    contentAlignment = Alignment.Center
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Add photo",
                        tint = CoralPrimary,
                        modifier = Modifier.size(24.dp)
                      )
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                        text = "Add",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoralPrimary,
                        fontWeight = FontWeight.Bold
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

    Spacer(modifier = Modifier.height(32.dp))

    // Next Button
    Button(
      onClick = onNext,
      enabled = photos.size >= 2 && !isUploading,
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .testTag("photos_next_button"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = CoralPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Text(
        text = if (photos.size < 2) "Add at least 2 photos (${photos.size}/2)" else "Next",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.width(8.dp))
      Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
    }
  }
}

// =========================================================================
// 6th PAGE: Work & Education Component
// =========================================================================
@Composable
private fun CareerEducationStep(
  occupation: String,
  onOccupationChange: (String) -> Unit,
  education: String,
  onEducationChange: (String) -> Unit,
  onNext: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text(
        text = "Work & Education",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )

      Text(
        text = "Share what keeps you driven and inspired.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(28.dp))

      OutlinedTextField(
        value = occupation,
        onValueChange = onOccupationChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Occupation / Job Title") },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
      )

      Spacer(modifier = Modifier.height(18.dp))

      OutlinedTextField(
        value = education,
        onValueChange = onEducationChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("University / College") },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
      )
    }

    Spacer(modifier = Modifier.height(40.dp))

    val isStep3Valid = occupation.trim().isNotBlank() && education.trim().isNotBlank()

    // Next Button (Enabled only when work and education are filled)
    Button(
      onClick = onNext,
      enabled = isStep3Valid,
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = CoralPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Text("Next", fontSize = 17.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.width(8.dp))
      Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
    }
  }
}

// =========================================================================
// 7th PAGE: Intentions & Passions Component
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntentionsPassionsStep(
  intention: String,
  onIntentionChange: (String) -> Unit,
  selectedPassions: Set<String>,
  onTogglePassion: (String) -> Unit,
  onNext: () -> Unit
) {
  val datingIntentionsList = listOf(
    "💘 Long term",
    "🥂 Casual dating",
    "💍 Marriage",
    "👯 New friends",
    "✨ Short term",
    "🧭 Figuring it out"
  )

  val allInterests = listOf(
    "✈️ Travel", "🍕 Foodie", "🎵 Music",
    "💪 Fitness & Gym", "📚 Reading",
    "☕ Coffee lover", "🎨 Art & Design", "🎮 Gaming",
    "🌲 Nature & Hiking", "📸 Photography",
    "🍳 Cooking", "💃 Dancing", "🏊 Swimming",
    "🧘 Yoga & Zen", "🎬 Movies",
    "🐶 Dogs & Pets", "🍷 Wine Tasting", "🚴 Cycling",
    "💡 Tech & Startups", "🎤 Karaoke",
    "⛺ Camping", "🎭 Theater", "🛹 Skateboarding"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text(
        text = "Dating Goals & Interests",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Dating Intention",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(8.dp))

      val intentionRows = remember {
        chunkByPattern(datingIntentionsList, listOf(2, 3, 1))
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        intentionRows.forEach { rowItems ->
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            rowItems.forEach { item ->
              val cleanItem = item.filter { it.isLetter() }.lowercase()
              val isSelected = intention.isNotBlank() && (
                intention.equals(item, ignoreCase = true) ||
                intention.filter { it.isLetter() }.equals(cleanItem, ignoreCase = true)
              )
              Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = if (isSelected) BorderStroke(1.2.dp, CoralPrimary) else BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                modifier = Modifier.clickable {
                  onIntentionChange(if (isSelected) "" else item)
                }
              ) {
                Text(
                  text = item,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "Interests (${selectedPassions.size}/8)",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(8.dp))

      val interestRows = remember {
        chunkByPattern(allInterests, listOf(3, 2, 3, 2, 3, 2, 3, 2, 3))
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        interestRows.forEach { rowItems ->
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            rowItems.forEach { interest ->
              val cleanInterest = interest.filter { it.isLetter() }.lowercase()
              val isSelected = selectedPassions.any { sel ->
                sel == interest || (cleanInterest.isNotEmpty() && sel.filter { it.isLetter() }.equals(cleanInterest, ignoreCase = true))
              }
              Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = if (isSelected) BorderStroke(1.2.dp, CoralPrimary) else BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                modifier = Modifier.clickable {
                  onTogglePassion(interest)
                }
              ) {
                Text(
                  text = interest,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    val isStep4Valid = intention.isNotBlank() && selectedPassions.isNotEmpty()

    // Next Button (Enabled only when required fields are filled)
    Button(
      onClick = onNext,
      enabled = isStep4Valid,
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = CoralPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Text("Next", fontSize = 17.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.width(8.dp))
      Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
    }
  }
}

// =========================================================================
// 8th PAGE: Lifestyle, Prompts & Bio (Zodiac Pills & Domestic Pets Pills)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun LifestylePromptsStep(
  promptQuestion: String,
  onPromptQuestionChange: (String) -> Unit,
  promptAnswer: String,
  onPromptAnswerChange: (String) -> Unit,
  bio: String,
  onBioChange: (String) -> Unit,
  height: String,
  onHeightChange: (String) -> Unit,
  zodiac: String,
  onZodiacChange: (String) -> Unit,
  selectedPets: Set<String>,
  onTogglePet: (String) -> Unit,
  drinking: String,
  onDrinkingChange: (String) -> Unit,
  smoking: String,
  onSmokingChange: (String) -> Unit,
  onNext: () -> Unit
) {
  val promptOptions = listOf(
    "My simple pleasures in life...",
    "The way to win me over is...",
    "Together, we could...",
    "I'm overly competitive about...",
    "A life goal of mine is..."
  )

  var isPromptExpanded by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text(
        text = "Personality & Prompts",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Dating Prompt",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(8.dp))

      // Dating Prompt Dropdown
      ExposedDropdownMenuBox(
        expanded = isPromptExpanded,
        onExpandedChange = { isPromptExpanded = !isPromptExpanded },
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedTextField(
          value = promptQuestion,
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPromptExpanded) },
          modifier = Modifier
            .menuAnchor()
            .fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CoralPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
          )
        )

        ExposedDropdownMenu(
          expanded = isPromptExpanded,
          onDismissRequest = { isPromptExpanded = false }
        ) {
          promptOptions.forEach { option ->
            DropdownMenuItem(
              text = { Text(option) },
              onClick = {
                onPromptQuestionChange(option)
                isPromptExpanded = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      OutlinedTextField(
        value = promptAnswer,
        onValueChange = {
          if (it.length <= 200) {
            onPromptAnswerChange(it)
          } else {
            onPromptAnswerChange(it.take(200))
          }
        },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Write your witty or genuine answer here...") },
        minLines = 3,
        supportingText = {
          Text(
            text = "${promptAnswer.length}/200",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
            color = if (promptAnswer.length >= 200) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
      )

      Spacer(modifier = Modifier.height(14.dp))

      // About Me Bio
      Text(
        text = "About Me Bio",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = bio,
        onValueChange = {
          if (it.length <= 500) {
            onBioChange(it)
          } else {
            onBioChange(it.take(500))
          }
        },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("About Me Bio...") },
        minLines = 3,
        supportingText = {
          Text(
            text = "${bio.length}/500",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
            color = if (bio.length >= 500) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Height (Scrolling Selector)
      Text(
        text = "Height",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(6.dp))

      if (height.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = CoralPrimary.copy(alpha = 0.12f),
          border = BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.4f)),
          modifier = Modifier.padding(bottom = 8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Straighten, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Selected: $height",
              style = MaterialTheme.typography.labelMedium,
              color = CoralPrimary,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        val standardHeights = listOf(
          "4'8\" (142 cm)", "4'9\" (145 cm)", "4'10\" (147 cm)", "4'11\" (150 cm)",
          "5'0\" (152 cm)", "5'1\" (155 cm)", "5'2\" (157 cm)", "5'3\" (160 cm)",
          "5'4\" (163 cm)", "5'5\" (165 cm)", "5'6\" (168 cm)", "5'7\" (170 cm)",
          "5'8\" (173 cm)", "5'9\" (175 cm)", "5'10\" (178 cm)", "5'11\" (180 cm)",
          "6'0\" (183 cm)", "6'1\" (185 cm)", "6'2\" (188 cm)", "6'3\" (191 cm)",
          "6'4\" (193 cm)", "6'5\" (196 cm)", "6'6\" (198 cm)", "6'7\" (201 cm)",
          "6'8\" (203 cm)"
        )
        items(standardHeights) { item ->
          val isSelected = height == item
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = if (isSelected) BorderStroke(1.2.dp, CoralPrimary) else BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
            modifier = Modifier.clickable { onHeightChange(if (isSelected) "" else item) }
          ) {
            Text(
              text = item,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              ),
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Zodiac Sign: Pills with emojis and row count randomness
      Text(
        text = "Zodiac Sign",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(6.dp))

      val zodiacRows = remember {
        chunkByPattern(AllZodiacSigns, listOf(3, 2, 3, 2, 2))
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        zodiacRows.forEach { rowItems ->
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            rowItems.forEach { sign ->
              val cleanSign = sign.filter { it.isLetter() }.lowercase()
              val isSelected = zodiac.isNotBlank() && (
                zodiac == sign || zodiac.filter { it.isLetter() }.equals(cleanSign, ignoreCase = true)
              )
              Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = if (isSelected) BorderStroke(1.2.dp, CoralPrimary) else BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                modifier = Modifier.clickable {
                  onZodiacChange(if (isSelected) "" else sign)
                }
              ) {
                Text(
                  text = sign,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Domestic Pets: Multi-select pills with emojis and row count randomness
      Text(
        text = "Pets (Select all that apply)",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(6.dp))

      val petsRows = remember {
        chunkByPattern(DomesticPetsList, listOf(2, 3, 1))
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        petsRows.forEach { rowItems ->
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            rowItems.forEach { pet ->
              val cleanPet = pet.filter { it.isLetter() }.lowercase()
              val isSelected = selectedPets.any { sel ->
                sel == pet || (cleanPet.isNotEmpty() && sel.filter { it.isLetter() }.equals(cleanPet, ignoreCase = true))
              }
              Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) PeachSecondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = if (isSelected) BorderStroke(1.2.dp, PeachSecondary) else BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                modifier = Modifier.clickable {
                  onTogglePet(pet)
                }
              ) {
                Text(
                  text = pet,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Drinking Habit
      Text(
        text = "Drinking Habit",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(6.dp))

      val drinkingOptions = listOf(
        "🍷 Socially", "🚫 Non-drinker",
        "🍻 Frequently", "🌱 Sober & clean", "🤫 Prefer not to say"
      )
      val drinkingRows = remember {
        chunkByPattern(drinkingOptions, listOf(2, 3))
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        drinkingRows.forEach { rowItems ->
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            rowItems.forEach { drink ->
              val cleanDrink = drink.filter { it.isLetter() }.lowercase()
              val isSelected = drinking.isNotBlank() && (
                drinking.equals(drink, ignoreCase = true) ||
                drinking.filter { it.isLetter() }.equals(cleanDrink, ignoreCase = true)
              )
              Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = if (isSelected) BorderStroke(1.2.dp, CoralPrimary) else BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                modifier = Modifier.clickable {
                  onDrinkingChange(if (isSelected) "" else drink)
                }
              ) {
                Text(
                  text = drink,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Smoking Habit
      Text(
        text = "Smoking Habit",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(6.dp))

      val smokingOptions = listOf(
        "🚭 Non-smoker", "💨 Occasionally", "🚬 Regular",
        "🌿 Trying to quit", "🤫 Prefer not to say"
      )
      val smokingRows = remember {
        chunkByPattern(smokingOptions, listOf(3, 2))
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        smokingRows.forEach { rowItems ->
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            rowItems.forEach { smoke ->
              val cleanSmoke = smoke.filter { it.isLetter() }.lowercase()
              val isSelected = smoking.isNotBlank() && (
                smoking.equals(smoke, ignoreCase = true) ||
                smoking.filter { it.isLetter() }.equals(cleanSmoke, ignoreCase = true)
              )
              Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = if (isSelected) BorderStroke(1.2.dp, CoralPrimary) else BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                modifier = Modifier.clickable {
                  onSmokingChange(if (isSelected) "" else smoke)
                }
              ) {
                Text(
                  text = smoke,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    val isStep5Valid = promptAnswer.trim().isNotBlank() && height.isNotBlank() && zodiac.isNotBlank() && selectedPets.isNotEmpty()

    // Next Button (Enabled only when required fields are filled)
    Button(
      onClick = onNext,
      enabled = isStep5Valid,
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = CoralPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Text("Next", fontSize = 17.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.width(8.dp))
      Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
    }
  }
}

// =========================================================================
// 9th PAGE: Review & Launch Profile Card Component
// =========================================================================
@Composable
private fun ReviewAndLaunchStep(
  profile: UserProfile,
  onComplete: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text(
        text = "Your Profile is Ready! ✨",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 26.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )

      Text(
        text = "Here is how other members in your area will see your profile.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Live Profile Card Preview
      Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
          .fillMaxWidth()
          .shadow(12.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1f)
          ) {
            val previewPhoto = profile.photos.firstOrNull { it.isNotBlank() }
            if (previewPhoto != null) {
              AsyncImage(
                model = previewPhoto,
                contentDescription = profile.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
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
                  style = MaterialTheme.typography.displaySmall,
                  color = CoralPrimary,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            // Gradient bottom overlay on photo
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                    startY = 200f
                  )
                )
            )

            Column(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "${profile.name}, ${profile.age}",
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Verified",
                  tint = LikeGreen,
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              val loc = if (profile.currentLocationCity.isNotBlank()) profile.currentLocationCity else profile.hometown
              if (loc.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (profile.currentLocationCountry.isNotBlank()) "$loc, ${profile.currentLocationCountry}" else loc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                  )
                }
              }
            }
          }

          Column(modifier = Modifier.padding(18.dp)) {
            if (profile.occupation.isNotBlank()) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Work, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = profile.occupation,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
            }

            if (profile.bio.isNotBlank()) {
              Text(
                text = profile.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(12.dp))
            }

            if (profile.promptAnswer.isNotBlank()) {
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(profile.promptQuestion, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CoralPrimary)
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(profile.promptAnswer, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    Button(
      onClick = onComplete,
      modifier = Modifier
        .fillMaxWidth()
        .height(58.dp)
        .testTag("launch_matching_button"),
      shape = RoundedCornerShape(20.dp),
      colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
    ) {
      Text(
        text = "Complete Profile & Start Matching ✨",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}
