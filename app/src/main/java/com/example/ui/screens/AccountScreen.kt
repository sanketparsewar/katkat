package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SubscriptionState
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserProfile
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.ui.theme.LikeGreen
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.PeachSecondary
import com.example.ui.theme.SuperlikeBlue

enum class AccountSubPage {
  MAIN,
  THEME,
  SETTINGS
}

@Composable
fun AccountScreen(
  userProfile: UserProfile,
  subscriptionState: SubscriptionState,
  themeMode: AppThemeMode,
  onThemeModeChange: (AppThemeMode) -> Unit,
  onOpenPaywall: () -> Unit,
  onDisableAccount: (Boolean) -> Unit,
  onDeleteAccount: () -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier
) {
  var currentPage by remember { mutableStateOf(AccountSubPage.MAIN) }

  AnimatedContent(
    targetState = currentPage,
    transitionSpec = {
      if (targetState != AccountSubPage.MAIN) {
        slideInHorizontally { width -> width } + fadeIn() togetherWith
            slideOutHorizontally { width -> -width } + fadeOut()
      } else {
        slideInHorizontally { width -> -width } + fadeIn() togetherWith
            slideOutHorizontally { width -> width } + fadeOut()
      }
    },
    label = "account_page_transition",
    modifier = modifier.fillMaxSize()
  ) { page ->
    when (page) {
      AccountSubPage.MAIN -> {
        AccountMainPage(
          userProfile = userProfile,
          subscriptionState = subscriptionState,
          themeMode = themeMode,
          onNavigateToTheme = { currentPage = AccountSubPage.THEME },
          onNavigateToSettings = { currentPage = AccountSubPage.SETTINGS },
          onOpenPaywall = onOpenPaywall,
          onLogout = onLogout
        )
      }
      AccountSubPage.THEME -> {
        ThemeSelectionPage(
          currentTheme = themeMode,
          onSelectTheme = { selected ->
            onThemeModeChange(selected)
          },
          onBack = { currentPage = AccountSubPage.MAIN }
        )
      }
      AccountSubPage.SETTINGS -> {
        SettingsPage(
          userProfile = userProfile,
          onDisableAccount = onDisableAccount,
          onDeleteAccount = onDeleteAccount,
          onBack = { currentPage = AccountSubPage.MAIN }
        )
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. Account Main Page
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountMainPage(
  userProfile: UserProfile,
  subscriptionState: SubscriptionState,
  themeMode: AppThemeMode,
  onNavigateToTheme: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onOpenPaywall: () -> Unit,
  onLogout: () -> Unit
) {
  val context = LocalContext.current
  var showLogoutDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Account",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    containerColor = MaterialTheme.colorScheme.background
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

      // User Profile Header Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Avatar
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
          ) {
            if (userProfile.photos.isNotEmpty()) {
              AsyncImage(
                model = userProfile.photos.first(),
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            } else {
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = CoralPrimary,
                modifier = Modifier.size(32.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = userProfile.name.ifBlank { "Katkat Member" },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              if (userProfile.isPhoneVerified) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Filled.CheckCircle,
                  contentDescription = "Verified Phone",
                  tint = LikeGreen,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = if (userProfile.phoneNumber.isNotBlank()) "${userProfile.countryCode} ${userProfile.phoneNumber}" else "Active Member",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (userProfile.isAccountDisabled) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "● Account Paused",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFE65100)
              )
            }
          }
        }
      }

      // ─────────────────────────────────────────────────────────────────────
      // Current Subscription Card
      // ─────────────────────────────────────────────────────────────────────
      val tier = subscriptionState.currentTier
      val isFree = tier == SubscriptionTier.FREE
      val subGradient = if (isFree) {
        listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)
      } else {
        listOf(Color(0xFF2C191D), Color(0xFF1E1014))
      }

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.horizontalGradient(
                colors = if (!isFree) listOf(CoralDark, CoralPrimary) else listOf(PeachBlush, PeachSecondary.copy(alpha = 0.5f))
              )
            )
            .padding(18.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (!isFree) Icons.Filled.Star else Icons.Filled.ElectricBolt,
                  contentDescription = null,
                  tint = if (!isFree) GoldVip else CoralPrimary,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Current Subscription",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                  color = if (!isFree) Color.White else MaterialTheme.colorScheme.onSurface
                )
              }

              // Active Badge
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (!isFree) GoldVip else CoralPrimary)
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(
                  text = tier.title.uppercase(),
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                  color = Color.White
                )
              }
            }

            Text(
              text = if (isFree) "Free Plan • 50 swipes/month" else "${tier.title} • Unlimited Matches & SuperLikes",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = if (!isFree) Color.White else MaterialTheme.colorScheme.onSurface
            )

            Text(
              text = "Swipes used: ${subscriptionState.swipesUsedThisMonth} / ${if (isFree) "50" else "Unlimited"}",
              style = MaterialTheme.typography.bodySmall,
              color = if (!isFree) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
              onClick = onOpenPaywall,
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (!isFree) Color.White else CoralPrimary,
                contentColor = if (!isFree) CoralDark else Color.White
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = if (isFree) "✨ Upgrade to Katkat VIP" else "Manage Subscription Plan",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // ─────────────────────────────────────────────────────────────────────
      // Options List
      // ─────────────────────────────────────────────────────────────────────
      Text(
        text = "Preferences & Controls",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
      )

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column {
          // 1. Theme Option
          AccountOptionRow(
            icon = Icons.Filled.Palette,
            iconTint = CoralPrimary,
            title = "Theme",
            subtitle = when (themeMode) {
              AppThemeMode.LIGHT -> "Light Theme"
              AppThemeMode.DARK -> "Dark Theme"
              AppThemeMode.SYSTEM -> "System Default"
            },
            onClick = onNavigateToTheme,
            testTag = "option_theme"
          )

          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          )

          // 2. Settings Option
          AccountOptionRow(
            icon = Icons.Filled.Settings,
            iconTint = SuperlikeBlue,
            title = "Setting",
            subtitle = "Disable or delete account",
            onClick = onNavigateToSettings,
            testTag = "option_settings"
          )

          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          )

          // 3. Safety & Privacy
          AccountOptionRow(
            icon = Icons.Filled.Shield,
            iconTint = LikeGreen,
            title = "Safety & Community",
            subtitle = "Dating guidelines and verification",
            onClick = {
              Toast.makeText(context, "Katkat Safe Dating: Always meet in public places and never share financial credentials.", Toast.LENGTH_LONG).show()
            },
            testTag = "option_safety"
          )
        }
      }

      // Logout Button
      OutlinedButton(
        onClick = { showLogoutDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = MaterialTheme.colorScheme.error
        )
      ) {
        Icon(imageVector = Icons.Filled.Logout, contentDescription = "Log Out")
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Log Out", fontWeight = FontWeight.SemiBold)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  if (showLogoutDialog) {
    AlertDialog(
      onDismissRequest = { showLogoutDialog = false },
      icon = { Icon(Icons.Filled.Logout, contentDescription = null, tint = CoralPrimary) },
      title = { Text("Log Out from Katkat?") },
      text = { Text("You will be returned to the mobile number sign-in screen.") },
      confirmButton = {
        Button(
          onClick = {
            showLogoutDialog = false
            onLogout()
          },
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Text("Log Out")
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogoutDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Theme Selection Page (Separate Page)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelectionPage(
  currentTheme: AppThemeMode,
  onSelectTheme: (AppThemeMode) -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Theme", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    containerColor = MaterialTheme.colorScheme.background
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "Select Appearance",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Choose your preferred visual theme for Katkat. Your choice will be applied immediately across all tabs and screens.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(4.dp))

      // 1. System Default
      ThemeOptionCard(
        title = "System Default",
        description = "Automatically adapt to your device's system dark and light mode settings.",
        icon = Icons.Filled.SettingsBrightness,
        isSelected = currentTheme == AppThemeMode.SYSTEM,
        onClick = {
          onSelectTheme(AppThemeMode.SYSTEM)
          Toast.makeText(context, "Theme set to System Default", Toast.LENGTH_SHORT).show()
        }
      )

      // 2. Light Theme
      ThemeOptionCard(
        title = "Light Theme",
        description = "Warm cream background, playful peach blush, and radiant coral accents.",
        icon = Icons.Filled.LightMode,
        isSelected = currentTheme == AppThemeMode.LIGHT,
        onClick = {
          onSelectTheme(AppThemeMode.LIGHT)
          Toast.makeText(context, "Light Theme applied", Toast.LENGTH_SHORT).show()
        }
      )

      // 3. Dark Theme
      ThemeOptionCard(
        title = "Dark Theme",
        description = "Midnight velvet, deep contrast dark surface, and battery-friendly night viewing.",
        icon = Icons.Filled.DarkMode,
        isSelected = currentTheme == AppThemeMode.DARK,
        onClick = {
          onSelectTheme(AppThemeMode.DARK)
          Toast.makeText(context, "Dark Theme applied", Toast.LENGTH_SHORT).show()
        }
      )
    }
  }
}

@Composable
private fun ThemeOptionCard(
  title: String,
  description: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) PeachBlush.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface
    ),
    border = if (isSelected) {
      androidx.compose.foundation.BorderStroke(2.dp, CoralPrimary)
    } else {
      androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isSelected) Color.White else CoralPrimary,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      RadioButton(
        selected = isSelected,
        onClick = onClick,
        colors = RadioButtonDefaults.colors(selectedColor = CoralPrimary)
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Settings Page (Separate Page with Disable & Delete Account)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPage(
  userProfile: UserProfile,
  onDisableAccount: (Boolean) -> Unit,
  onDeleteAccount: () -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current

  var showDisableDialog by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Setting", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    containerColor = MaterialTheme.colorScheme.background
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "Account Visibility & Controls",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      // Disable Account Option Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (userProfile.isAccountDisabled) Color(0xFFFFEBEE) else PeachBlush),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (userProfile.isAccountDisabled) Icons.Filled.PlayCircle else Icons.Filled.PauseCircle,
                contentDescription = null,
                tint = if (userProfile.isAccountDisabled) CoralPrimary else CoralDark,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (userProfile.isAccountDisabled) "Account Disabled (Paused)" else "Disable Account",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (userProfile.isAccountDisabled) "Profile is currently hidden from discovery" else "Temporarily hide profile from discovery",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Switch(
              checked = userProfile.isAccountDisabled,
              onCheckedChange = { isChecked ->
                if (isChecked) {
                  showDisableDialog = true
                } else {
                  onDisableAccount(false)
                }
              },
              colors = SwitchDefaults.colors(checkedThumbColor = CoralPrimary, checkedTrackColor = PeachBlush)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "When disabled, other members cannot find you in Discovery or send new likes. Your existing matches and messages remain intact.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Delete Account Option Card (Destructive)
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Delete Account",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error
              )
              Text(
                text = "Permanently remove your account and data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Deleting your account will permanently wipe your profile photos, messages, swipes, and account from the database. This action cannot be undone.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Filled.DeleteForever, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete My Account", fontWeight = FontWeight.Bold)
          }
        }
      }

      // App Version & Safety Badge
      Spacer(modifier = Modifier.height(8.dp))
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Katkat Dating • v2.4.0",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Cloud Database Connected • Firebase & Room",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
      }
    }
  }

  // 1. Disable Account Confirmation Dialog
  if (showDisableDialog) {
    AlertDialog(
      onDismissRequest = { showDisableDialog = false },
      icon = { Icon(Icons.Filled.PauseCircle, contentDescription = null, tint = CoralPrimary) },
      title = { Text("Disable Account?") },
      text = {
        Text("Your profile will be hidden from new people in Discover. You can re-enable your account anytime by visiting Settings.")
      },
      confirmButton = {
        Button(
          onClick = {
            showDisableDialog = false
            onDisableAccount(true)
          },
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Text("Disable Profile")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDisableDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // 2. Delete Account Confirmation Dialog
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      icon = { Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
      title = { Text("Permanently Delete Account?", color = MaterialTheme.colorScheme.error) },
      text = {
        Text("Are you sure you want to delete your Katkat account? All your matches, chat conversations, and profile details will be permanently removed from the database.")
      },
      confirmButton = {
        Button(
          onClick = {
            showDeleteDialog = false
            onDeleteAccount()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Yes, Delete Everything")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) {
          Text("Keep Account")
        }
      }
    )
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Option Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AccountOptionRow(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  testTag: String = ""
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(iconTint.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(22.dp)
      )
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
      contentDescription = "Navigate",
      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
      modifier = Modifier.size(16.dp)
    )
  }
}
