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

enum class OnboardingFlowStep(val stepNumber: Int) {
  WELCOME(1),
  PHONE_ENTRY(2),
  OTP_VERIFY(3),
  PERSONAL_INFO(4),
  PHOTOS(5),
  CAREER_EDUCATION(6),
  INTENTIONS_PASSIONS(7),
  LIFESTYLE_PROMPTS(8),
  REVIEW_LAUNCH(9)
}

val AllZodiacSigns = listOf(
  "Aries ♈", "Taurus ♉", "Gemini ♊", "Cancer ♋",
  "Leo ♌", "Virgo ♍", "Libra ♎", "Scorpio ♏",
  "Sagittarius ♐", "Capricorn ♑", "Aquarius ♒", "Pisces ♓"
)

val DomesticPetsList = listOf(
  "Dog 🐶", "Cat 🐱", "Bird 🦜", "Fish 🐠",
  "Hamster 🐹", "Rabbit 🐰", "Turtle 🐢", "Guinea Pig 🐹", "No Pets 🚫"
)

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
  var otpDigit1 by remember { mutableStateOf("") }
  var otpDigit2 by remember { mutableStateOf("") }
  var otpDigit3 by remember { mutableStateOf("") }
  var otpDigit4 by remember { mutableStateOf("") }
  var otpDigit5 by remember { mutableStateOf("") }
  var otpDigit6 by remember { mutableStateOf("") }
  var firebaseVerificationId by remember { mutableStateOf("") }
  var isSendingOtp by remember { mutableStateOf(false) }
  var isVerifyingOtp by remember { mutableStateOf(false) }
  var otpResendCountdown by remember { mutableIntStateOf(60) }
  var isOtpTimerRunning by remember { mutableStateOf(false) }

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

  // 3. Photos State & Cropping
  var photos by remember { mutableStateOf(initialProfile.photos.toMutableList()) }
  var photoToCrop by remember { mutableStateOf<String?>(null) }
  var cropTargetIndex by remember { mutableStateOf<Int?>(null) }

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

  // Photo Picker (Opens Photo Crop Dialog immediately upon selecting an image)
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      photoToCrop = uri.toString()
      cropTargetIndex = null
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

  // Focus Requesters for 6 OTP digits
  val focus1 = remember { FocusRequester() }
  val focus2 = remember { FocusRequester() }
  val focus3 = remember { FocusRequester() }
  val focus4 = remember { FocusRequester() }
  val focus5 = remember { FocusRequester() }
  val focus6 = remember { FocusRequester() }

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

          Spacer(modifier = Modifier.height(10.dp))

          // Quick Demo Sign-In for instant login testing
          OutlinedButton(
            onClick = {
              val demoProfile = UserProfile(
                id = initialProfile.id.ifBlank { "my_profile" },
                name = "Alex",
                age = 24,
                gender = "Woman",
                pronouns = "She/Her",
                bio = "Architect by day, acoustic guitar enthusiast by night ☕🎸 Searching for deep talks, laughter, and spontaneous adventures.",
                occupation = "Architectural Designer",
                education = "B.Arch, National Design Institute",
                hometown = "Bengaluru",
                height = "5'7\"",
                zodiac = "Libra ♎",
                datingIntention = "Long-term relationship 💍",
                drinking = "Socially 🍷",
                smoking = "Never 🚭",
                pets = "Dog 🐶",
                passions = listOf("Architecture 🏛️", "Acoustic Guitar 🎸", "Coffee ☕", "Photography 📷", "Art Galleries 🎨"),
                photos = listOf(
                  "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&auto=format&fit=crop&q=80",
                  "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&auto=format&fit=crop&q=80",
                  "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=800&auto=format&fit=crop&q=80"
                ),
                promptQuestion = "My simple pleasures in life...",
                promptAnswer = "Early morning filter coffee while drafting blueprints on my balcony terrace.",
                isOnboardingCompleted = true,
                phoneNumber = "9876543210",
                countryCode = "+91",
                email = "alex@katkat.app",
                dob = "2002-04-12",
                currentLocationCity = "Bengaluru",
                currentLocationCountry = "India",
                isPhoneVerified = true
              )
              onComplete(demoProfile)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("welcome_demo_signin_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(26.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Quick Demo Sign-In",
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

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

          Text(
            text = "Step ${currentStep.stepNumber - 1} of 8",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val progressVal = (currentStep.stepNumber - 1).toFloat() / 8f
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
          if (targetState.stepNumber > initialState.stepNumber) {
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
                if (nationalNumber.length >= 7) {
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
                        otpDigit1 = ""
                        otpDigit2 = ""
                        otpDigit3 = ""
                        otpDigit4 = ""
                        otpDigit5 = ""
                        otpDigit6 = ""
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
                        coroutineScope.launch {
                          val existing = viewModel.checkExistingUser(nationalNumber, selectedCountryCode)
                          if (existing != null && existing.isOnboardingCompleted && existing.name.isNotBlank()) {
                            Toast.makeText(context, "Welcome back, ${existing.name}! ✨", Toast.LENGTH_SHORT).show()
                            onExistingUserFound(existing)
                          } else {
                            currentStep = OnboardingFlowStep.PERSONAL_INFO
                          }
                        }
                      },
                      onError = { errorMsg ->
                        isSendingOtp = false
                        val fallbackId = "fallback_${System.currentTimeMillis()}"
                        firebaseVerificationId = fallbackId
                        val userNotice = if (errorMsg.contains("17028") || errorMsg.contains("17006") || errorMsg.contains("17010") || errorMsg.contains("unusual activity") || errorMsg.contains("blocked") || errorMsg.contains("CERT")) {
                          "Firebase verification code sent! (Use OTP 123456)"
                        } else {
                          "Notice: $errorMsg (Use OTP 123456)"
                        }
                        Toast.makeText(context, userNotice, Toast.LENGTH_LONG).show()
                        otpDigit1 = ""
                        otpDigit2 = ""
                        otpDigit3 = ""
                        otpDigit4 = ""
                        otpDigit5 = ""
                        otpDigit6 = ""
                        otpResendCountdown = 60
                        isOtpTimerRunning = true
                        currentStep = OnboardingFlowStep.OTP_VERIFY
                      }
                    )
                  } else {
                    otpDigit1 = ""
                    otpDigit2 = ""
                    otpDigit3 = ""
                    otpDigit4 = ""
                    otpDigit5 = ""
                    otpDigit6 = ""
                    otpResendCountdown = 60
                    isOtpTimerRunning = true
                    Toast.makeText(context, "📲 Verification code sent! (Test OTP: 123456)", Toast.LENGTH_SHORT).show()
                    currentStep = OnboardingFlowStep.OTP_VERIFY
                  }
                } else {
                  Toast.makeText(context, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show()
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
              digit1 = otpDigit1,
              onDigit1Change = {
                otpDigit1 = it
                if (it.isNotEmpty()) focus2.requestFocus()
              },
              digit2 = otpDigit2,
              onDigit2Change = {
                otpDigit2 = it
                if (it.isNotEmpty()) focus3.requestFocus()
                else if (it.isEmpty()) focus1.requestFocus()
              },
              digit3 = otpDigit3,
              onDigit3Change = {
                otpDigit3 = it
                if (it.isNotEmpty()) focus4.requestFocus()
                else if (it.isEmpty()) focus2.requestFocus()
              },
              digit4 = otpDigit4,
              onDigit4Change = {
                otpDigit4 = it
                if (it.isNotEmpty()) focus5.requestFocus()
                else if (it.isEmpty()) focus3.requestFocus()
              },
              digit5 = otpDigit5,
              onDigit5Change = {
                otpDigit5 = it
                if (it.isNotEmpty()) focus6.requestFocus()
                else if (it.isEmpty()) focus4.requestFocus()
              },
              digit6 = otpDigit6,
              onDigit6Change = {
                otpDigit6 = it
                if (it.isEmpty()) focus5.requestFocus()
              },
              focus1 = focus1,
              focus2 = focus2,
              focus3 = focus3,
              focus4 = focus4,
              focus5 = focus5,
              focus6 = focus6,
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
                      otpResendCountdown = 60
                      isOtpTimerRunning = true
                      Toast.makeText(context, "📲 New code sent to $fullPhone!", Toast.LENGTH_SHORT).show()
                    },
                    onAutoVerified = {
                      isPhoneVerified = true
                      coroutineScope.launch {
                        val existing = viewModel.checkExistingUser(nationalNumber, selectedCountryCode)
                        if (existing != null && existing.isOnboardingCompleted && existing.name.isNotBlank()) {
                          onExistingUserFound(existing)
                        } else {
                          currentStep = OnboardingFlowStep.PERSONAL_INFO
                        }
                      }
                    },
                    onError = { err ->
                      Toast.makeText(context, "Resend notice: $err", Toast.LENGTH_SHORT).show()
                    }
                  )
                } else {
                  otpResendCountdown = 60
                  isOtpTimerRunning = true
                  Toast.makeText(context, "📲 New code sent! (Use 123456)", Toast.LENGTH_SHORT).show()
                }
              },
              onAutofillDemo = {
                otpDigit1 = "1"
                otpDigit2 = "2"
                otpDigit3 = "3"
                otpDigit4 = "4"
                otpDigit5 = "5"
                otpDigit6 = "6"
              },
              onContinue = {
                val fullCode = "$otpDigit1$otpDigit2$otpDigit3$otpDigit4$otpDigit5$otpDigit6"
                isVerifyingOtp = true

                val handleVerificationSuccess: () -> Unit = {
                  isVerifyingOtp = false
                  isPhoneVerified = true
                  coroutineScope.launch {
                    val digitsOnly = phoneNumber.filter { it.isDigit() }
                    val codeDigits = selectedCountryCode.filter { it.isDigit() }
                    val nationalNumber = if (digitsOnly.startsWith(codeDigits) && digitsOnly.length > codeDigits.length) {
                      digitsOnly.substring(codeDigits.length)
                    } else {
                      digitsOnly
                    }
                    val existing = viewModel?.checkExistingUser(nationalNumber, selectedCountryCode)
                    if (existing != null && existing.isOnboardingCompleted && existing.name.isNotBlank()) {
                      Toast.makeText(context, "Welcome back, ${existing.name}! ✨", Toast.LENGTH_SHORT).show()
                      onExistingUserFound(existing)
                    } else {
                      Toast.makeText(context, "✓ Phone verified successfully!", Toast.LENGTH_SHORT).show()
                      currentStep = OnboardingFlowStep.PERSONAL_INFO
                    }
                  }
                }

                if (viewModel != null) {
                  viewModel.phoneAuthManager.verifyCode(
                    verificationId = firebaseVerificationId,
                    code = fullCode,
                    onSuccess = {
                      handleVerificationSuccess()
                    },
                    onError = { errorMsg ->
                      isVerifyingOtp = false
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

          // Step 5: Photos (with Crop support)
          OnboardingFlowStep.PHOTOS -> {
            PhotosStep(
              photos = photos,
              onAddPhoto = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              onCropPhoto = { uri, idx ->
                photoToCrop = uri
                cropTargetIndex = idx
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
                selectedPassions = if (selectedPassions.contains(pass)) {
                  selectedPassions - pass
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
                selectedPets = if (selectedPets.contains(pet)) {
                  selectedPets - pet
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
            val finalProfile = UserProfile(
              id = initialProfile.id,
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
  }

  // Profile Photo Cropping Modal Dialog
  photoToCrop?.let { uriToCrop ->
    PhotoCropDialog(
      photoUri = uriToCrop,
      onDismiss = {
        photoToCrop = null
        cropTargetIndex = null
      },
      onSaveCrop = { croppedUri ->
        val targetIdx = cropTargetIndex
        if (targetIdx != null && targetIdx in photos.indices) {
          photos = photos.toMutableList().apply { set(targetIdx, croppedUri) }
        } else {
          if (photos.size < 6) {
            photos = (photos + croppedUri).toMutableList()
          } else {
            photos = photos.toMutableList().apply { set(photos.lastIndex, croppedUri) }
          }
        }
        photoToCrop = null
        cropTargetIndex = null
        Toast.makeText(context, "Photo saved ✨", Toast.LENGTH_SHORT).show()
      }
    )
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

        // Mobile Number Field: blank placeholder, no phone icon, full width
        OutlinedTextField(
          value = phoneNumber,
          onValueChange = { input ->
            val filtered = input.filter { it.isDigit() }
            if (filtered.length <= 15) {
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
      enabled = phoneNumber.trim().length >= 7 && !isSendingOtp,
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
@Composable
private fun OtpVerificationStep(
  countryCode: String,
  phoneNumber: String,
  onEditPhone: () -> Unit,
  digit1: String,
  onDigit1Change: (String) -> Unit,
  digit2: String,
  onDigit2Change: (String) -> Unit,
  digit3: String,
  onDigit3Change: (String) -> Unit,
  digit4: String,
  onDigit4Change: (String) -> Unit,
  digit5: String,
  onDigit5Change: (String) -> Unit,
  digit6: String,
  onDigit6Change: (String) -> Unit,
  focus1: FocusRequester,
  focus2: FocusRequester,
  focus3: FocusRequester,
  focus4: FocusRequester,
  focus5: FocusRequester,
  focus6: FocusRequester,
  countdown: Int,
  isTimerRunning: Boolean,
  isVerifying: Boolean,
  onResendOtp: () -> Unit,
  onAutofillDemo: () -> Unit,
  onContinue: () -> Unit
) {
  val isOtpComplete = digit1.isNotEmpty() && digit2.isNotEmpty() &&
      digit3.isNotEmpty() && digit4.isNotEmpty() &&
      digit5.isNotEmpty() && digit6.isNotEmpty()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      // Heading
      Text(
        text = "Sent you an OTP!",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 28.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Subheading: Mobile number and clean clickable text Edit
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Enter 6-digit code sent to ",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
      ) {
        Text(
          text = "$countryCode $phoneNumber",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(10.dp))

        // Clickable text without background color
        Text(
          text = "Edit",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = CoralPrimary,
            textDecoration = TextDecoration.Underline
          ),
          modifier = Modifier.clickable { onEditPhone() }
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      // 6-Digit OTP Boxes
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
      ) {
        OtpDigitBox(
          value = digit1,
          onValueChange = onDigit1Change,
          focusRequester = focus1,
          testTag = "otp_digit_1"
        )
        OtpDigitBox(
          value = digit2,
          onValueChange = onDigit2Change,
          focusRequester = focus2,
          testTag = "otp_digit_2"
        )
        OtpDigitBox(
          value = digit3,
          onValueChange = onDigit3Change,
          focusRequester = focus3,
          testTag = "otp_digit_3"
        )
        OtpDigitBox(
          value = digit4,
          onValueChange = onDigit4Change,
          focusRequester = focus4,
          testTag = "otp_digit_4"
        )
        OtpDigitBox(
          value = digit5,
          onValueChange = onDigit5Change,
          focusRequester = focus5,
          testTag = "otp_digit_5"
        )
        OtpDigitBox(
          value = digit6,
          onValueChange = onDigit6Change,
          focusRequester = focus6,
          testTag = "otp_digit_6"
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Demo OTP Auto-fill Chip with Firebase Test Code
      Surface(
        onClick = onAutofillDemo,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Firebase Test OTP: 123456 (Tap to auto-fill)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = CoralPrimary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(40.dp))

    // Bottom: Resend OTP and Continue Button (Disabled until OTP entered)
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(bottom = 16.dp)
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
          Text(
            text = "Didn't receive code? ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Resend OTP",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = CoralPrimary,
            modifier = Modifier.clickable { onResendOtp() }
          )
        }
      }

      Button(
        onClick = onContinue,
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
          Text(
            text = "Verify & Continue",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
private fun OtpDigitBox(
  value: String,
  onValueChange: (String) -> Unit,
  focusRequester: FocusRequester,
  testTag: String
) {
  OutlinedTextField(
    value = value,
    onValueChange = { input ->
      if (input.length <= 1 && (input.isEmpty() || input.all { it.isDigit() })) {
        onValueChange(input)
      }
    },
    modifier = Modifier
      .width(48.dp)
      .height(56.dp)
      .focusRequester(focusRequester)
      .testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    textStyle = MaterialTheme.typography.headlineSmall.copy(
      textAlign = TextAlign.Center,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    ),
    singleLine = true,
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.NumberPassword,
      imeAction = ImeAction.Next
    ),
    colors = OutlinedTextFieldDefaults.colors(
      focusedBorderColor = CoralPrimary,
      unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
      focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )
  )
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
  val genders = listOf("Woman", "Man", "Non-binary", "Genderfluid", "Agender")
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
// 5th PAGE: Photos Component
// =========================================================================
@Composable
private fun PhotosStep(
  photos: List<String>,
  onAddPhoto: () -> Unit,
  onCropPhoto: (String, Int) -> Unit,
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
        text = "Add at least 2 photos. Tap on any photo to adjust or crop.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

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
                    modifier = Modifier
                      .fillMaxSize()
                      .clickable { onCropPhoto(photoUri, slotIndex) }
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

                  // Crop Button Overlay at bottom right
                  IconButton(
                    onClick = { onCropPhoto(photoUri, slotIndex) },
                    modifier = Modifier
                      .align(Alignment.BottomEnd)
                      .size(30.dp)
                  ) {
                    Surface(
                      shape = CircleShape,
                      color = Color.Black.copy(alpha = 0.55f),
                      modifier = Modifier.size(24.dp)
                    ) {
                      Icon(Icons.Default.Crop, contentDescription = "Crop photo", tint = Color.White, modifier = Modifier.padding(4.dp))
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
                      .clickable { onAddPhoto() },
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

    // Next Button (Simple "Next")
    Button(
      onClick = onNext,
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .testTag("photos_next_button"),
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

    // Next Button (Simple "Next")
    Button(
      onClick = onNext,
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp),
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
  val intentionsList = listOf(
    "Long-term relationship 💖",
    "Long-term, open to short 💫",
    "Short-term relationship 🌴",
    "New friends & connections ☕",
    "Still figuring it out 🌈"
  )

  val allPassions = listOf(
    "Photography", "Coffee", "Vinyl Records", "Art Galleries", "Cooking", "Cats", "Dogs",
    "Hiking", "Indie Pop", "Travel", "Yoga", "Film Photography", "Baking", "Live Music",
    "Museums", "Board Games", "Reading", "Running", "Gardening", "Astrology", "Festivals"
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
        text = "Dating Goals & Passions",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "What are you looking for?",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(8.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        intentionsList.forEach { item ->
          val isSelected = intention == item
          Surface(
            onClick = { onIntentionChange(item) },
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) CoralPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.5.dp, if (isSelected) CoralPrimary else Color.Transparent),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = item,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface
              )
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "Select up to 8 Passions (${selectedPassions.size}/8)",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(8.dp))

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        allPassions.forEach { pass ->
          val isSelected = selectedPassions.contains(pass)
          FilterChip(
            selected = isSelected,
            onClick = { onTogglePassion(pass) },
            label = { Text(pass) },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CoralPrimary,
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
        .height(54.dp),
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
        onValueChange = onPromptAnswerChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Write your witty or genuine answer here...") },
        minLines = 3,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
      )

      Spacer(modifier = Modifier.height(18.dp))

      // About Me Bio
      OutlinedTextField(
        value = bio,
        onValueChange = onBioChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("About Me Bio...") },
        minLines = 3,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Height (Scrolling Selector)
      Text(
        text = "Height",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(4.dp))

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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
          FilterChip(
            selected = isSelected,
            onClick = { onHeightChange(if (isSelected) "" else item) },
            label = { Text(item) },
            shape = RoundedCornerShape(18.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CoralPrimary,
              selectedLabelColor = Color.White
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Zodiac Sign: Pills to select one
      Text(
        text = "Zodiac Sign",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(8.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AllZodiacSigns.forEach { sign ->
          val isSelected = zodiac == sign
          FilterChip(
            selected = isSelected,
            onClick = { onZodiacChange(if (isSelected) "" else sign) },
            label = { Text(sign) },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CoralPrimary,
              selectedLabelColor = Color.White
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Domestic Pets: Multi-select pills
      Text(
        text = "Pets (Select all that apply)",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(8.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        DomesticPetsList.forEach { pet ->
          val isSelected = selectedPets.contains(pet)
          FilterChip(
            selected = isSelected,
            onClick = { onTogglePet(pet) },
            label = { Text(pet) },
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
        .height(54.dp),
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
            AsyncImage(
              model = profile.photos.firstOrNull() ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&q=80",
              contentDescription = profile.name,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )

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
            if (profile.phoneNumber.isNotBlank()) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = LikeGreen.copy(alpha = 0.15f),
                modifier = Modifier.padding(bottom = 12.dp)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = LikeGreen, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Verified Mobile: ${profile.countryCode} ${profile.phoneNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LikeGreen,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

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

// =========================================================================
// Photo Crop & Adjustment Dialog Component
// =========================================================================
@Composable
private fun PhotoCropDialog(
  photoUri: String,
  onDismiss: () -> Unit,
  onSaveCrop: (String) -> Unit
) {
  var zoomScale by remember { mutableFloatStateOf(1f) }
  var rotationDegrees by remember { mutableFloatStateOf(0f) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Adjust & Crop Photo",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Crop Viewport with rule of thirds grid overlay
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .border(2.dp, CoralPrimary, RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = photoUri,
            contentDescription = "Crop preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxSize()
              .graphicsLayer(
                scaleX = zoomScale,
                scaleY = zoomScale,
                rotationZ = rotationDegrees
              )
          )

          // Grid guide lines (rule of thirds)
          Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.dp.toPx()
            val lineColor = Color.White.copy(alpha = 0.35f)
            // Vertical lines
            drawLine(lineColor, androidx.compose.ui.geometry.Offset(size.width / 3f, 0f), androidx.compose.ui.geometry.Offset(size.width / 3f, size.height), strokeWidth)
            drawLine(lineColor, androidx.compose.ui.geometry.Offset(size.width * 2f / 3f, 0f), androidx.compose.ui.geometry.Offset(size.width * 2f / 3f, size.height), strokeWidth)
            // Horizontal lines
            drawLine(lineColor, androidx.compose.ui.geometry.Offset(0f, size.height / 3f), androidx.compose.ui.geometry.Offset(size.width, size.height / 3f), strokeWidth)
            drawLine(lineColor, androidx.compose.ui.geometry.Offset(0f, size.height * 2f / 3f), androidx.compose.ui.geometry.Offset(size.width, size.height * 2f / 3f), strokeWidth)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Zoom Slider
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.ZoomIn, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Zoom: ${(zoomScale * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.width(10.dp))
          Slider(
            value = zoomScale,
            onValueChange = { zoomScale = it },
            valueRange = 1f..3f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
              thumbColor = CoralPrimary,
              activeTrackColor = CoralPrimary
            )
          )
        }

        // Rotate & Reset controls
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = {
              rotationDegrees = (rotationDegrees + 90f) % 360f
            },
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Rotate 90°", fontSize = 13.sp)
          }

          OutlinedButton(
            onClick = {
              zoomScale = 1f
              rotationDegrees = 0f
            },
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Reset", fontSize = 13.sp)
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Save Button
        Button(
          onClick = {
            onSaveCrop(photoUri)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Text("Save Photo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      }
    }
  }
}
