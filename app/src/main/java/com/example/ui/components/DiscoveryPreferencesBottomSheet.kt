package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriptionState
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserProfile
import com.example.ui.screens.AllDatingIntentionsList
import com.example.ui.screens.AllZodiacList
import com.example.ui.screens.DrinkingHabitsList
import com.example.ui.screens.SmokingHabitsList
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.ui.theme.PeachSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiscoveryPreferencesBottomSheet(
  userProfile: UserProfile,
  subscriptionState: SubscriptionState,
  onSavePreferences: (UserProfile) -> Unit,
  onOpenPaywall: () -> Unit,
  onDismiss: () -> Unit
) {
  val isVip = subscriptionState.currentTier == SubscriptionTier.TIER_2

  val defaultInterestedIn = when {
    userProfile.interestedInGender.isNotBlank() -> userProfile.interestedInGender
    userProfile.gender.equals("Man", ignoreCase = true) -> "Women"
    userProfile.gender.equals("Woman", ignoreCase = true) || userProfile.gender.equals("Women", ignoreCase = true) -> "Men"
    else -> "Everyone"
  }

  var distanceKm by remember(userProfile.maxDistanceKm) {
    mutableFloatStateOf(if (userProfile.maxDistanceKm > 0) userProfile.maxDistanceKm.toFloat() else 50f)
  }
  var minAge by remember(userProfile.minAgePreference) {
    mutableFloatStateOf(if (userProfile.minAgePreference in 18..75) userProfile.minAgePreference.toFloat() else 18f)
  }
  var maxAge by remember(userProfile.maxAgePreference) {
    mutableFloatStateOf(if (userProfile.maxAgePreference in 18..75) userProfile.maxAgePreference.toFloat() else 35f)
  }
  var interestedIn by remember(userProfile.interestedInGender, userProfile.gender) {
    mutableStateOf(defaultInterestedIn)
  }

  // VIP Advanced Filter States
  // Initially, all categories are selected as "Any" and saved in preferences only
  var filterIntention by remember(userProfile.preferDatingIntention) {
    mutableStateOf(if (isVip) userProfile.preferDatingIntention else "")
  }
  var filterDrinking by remember(userProfile.preferDrinking) {
    mutableStateOf(if (isVip) userProfile.preferDrinking else "")
  }
  var filterSmoking by remember(userProfile.preferSmoking) {
    mutableStateOf(if (isVip) userProfile.preferSmoking else "")
  }
  var filterZodiac by remember(userProfile.preferZodiac) {
    mutableStateOf(if (isVip) userProfile.preferZodiac else "")
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Header
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
              .background(CoralPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = null,
              tint = CoralPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Discovery & Preferences",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
              )
            )
            Text(
              text = "Filter who you see in Discover",
              style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp
              )
            )
          }
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 1. Distance Slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          "Maximum Distance",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          "${distanceKm.toInt()} km",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = CoralPrimary
        )
      }
      Slider(
        value = distanceKm,
        onValueChange = { distanceKm = it },
        valueRange = 5f..150f,
        colors = SliderDefaults.colors(
          thumbColor = CoralPrimary,
          activeTrackColor = CoralPrimary
        ),
        modifier = Modifier.testTag("slider_max_distance")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // 2. Age Range Slider (Extended up to 75)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          "Age Range",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          "${minAge.toInt()} – ${maxAge.toInt()} years",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = CoralPrimary
        )
      }
      RangeSlider(
        value = minAge..maxAge,
        onValueChange = { range ->
          minAge = range.start.coerceIn(18f, 75f)
          maxAge = range.endInclusive.coerceIn(18f, 75f)
        },
        valueRange = 18f..75f,
        steps = 56, // 18 to 75 (57 total values, 56 intermediate steps)
        colors = SliderDefaults.colors(
          thumbColor = CoralPrimary,
          activeTrackColor = CoralPrimary
        ),
        modifier = Modifier.testTag("slider_age_range")
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Interested In
      Text(
        "Interested In",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("Women", "Men", "Everyone").forEach { genderOption ->
          val selected = interestedIn.equals(genderOption, ignoreCase = true)
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (selected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = if (selected) BorderStroke(1.dp, CoralPrimary) else null,
            modifier = Modifier
              .weight(1f)
              .clickable { interestedIn = genderOption }
              .testTag("filter_gender_$genderOption")
          ) {
            Text(
              text = genderOption,
              fontSize = 13.sp,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
              color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(vertical = 10.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // ───────────────────────────────────────────────────────────────────────
      // VIP EXCLUSIVE PREFERENCE FILTERING SECTION
      // ───────────────────────────────────────────────────────────────────────
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .testTag("vip_advanced_filters_card"),
        color = if (isVip) Color(0xFFFFF9E6) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
          width = 1.dp,
          color = if (isVip) GoldVip else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
        ) {
          // VIP Banner Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = if (isVip) Color(0xFFB8860B) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "VIP Advanced Filters",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isVip) Color(0xFF7A5200) else MaterialTheme.colorScheme.onSurface
              )
            }

            if (isVip) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(GoldVip)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "UNLOCKED 👑",
                  color = Color.Black,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            } else {
              Surface(
                onClick = onOpenPaywall,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF7A5200),
                modifier = Modifier.testTag("btn_unlock_vip_filters")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = GoldVip,
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Unlock with VIP",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }

          Text(
            text = if (isVip) "Filter Discover cards by relationship goal, lifestyle, and horoscope."
            else "Advanced filtering is exclusive to KatKat VIP members. Upgrade to filter by lifestyle & dating goals.",
            fontSize = 11.5.sp,
            color = if (isVip) Color(0xFF5C3E00) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
          )

          // 1. Dating Intention Filter
          Text(
            text = "Dating Intention",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isVip) Color(0xFF7A5200) else MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(5.dp))
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val intentionOptions = listOf("Any") + AllDatingIntentionsList
            intentionOptions.forEach { opt ->
              val isSelected = if (opt == "Any") filterIntention.isBlank() || filterIntention.equals("Any", ignoreCase = true) else filterIntention.equals(opt, ignoreCase = true)
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                  !isVip -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                  isSelected -> GoldVip
                  else -> Color.White
                },
                border = BorderStroke(
                  0.8.dp,
                  if (isSelected && isVip) Color(0xFFB8860B) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.clickable(enabled = isVip) {
                  filterIntention = if (opt == "Any" || isSelected) "" else opt
                }
              ) {
                Text(
                  text = opt,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected && isVip) FontWeight.Bold else FontWeight.Normal,
                  color = when {
                    !isVip -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    isSelected -> Color.Black
                    else -> MaterialTheme.colorScheme.onSurface
                  },
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 2. Drinking Habit Filter
          Text(
            text = "Drinking Habits",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isVip) Color(0xFF7A5200) else MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(5.dp))
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val drinkingOptions = listOf("Any") + DrinkingHabitsList
            drinkingOptions.forEach { opt ->
              val isSelected = if (opt == "Any") filterDrinking.isBlank() || filterDrinking.equals("Any", ignoreCase = true) else filterDrinking.equals(opt, ignoreCase = true)
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                  !isVip -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                  isSelected -> GoldVip
                  else -> Color.White
                },
                border = BorderStroke(
                  0.8.dp,
                  if (isSelected && isVip) Color(0xFFB8860B) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.clickable(enabled = isVip) {
                  filterDrinking = if (opt == "Any" || isSelected) "" else opt
                }
              ) {
                Text(
                  text = opt,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected && isVip) FontWeight.Bold else FontWeight.Normal,
                  color = when {
                    !isVip -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    isSelected -> Color.Black
                    else -> MaterialTheme.colorScheme.onSurface
                  },
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 3. Smoking Habit Filter
          Text(
            text = "Smoking Habits",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isVip) Color(0xFF7A5200) else MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(5.dp))
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val smokingOptions = listOf("Any") + SmokingHabitsList
            smokingOptions.forEach { opt ->
              val isSelected = if (opt == "Any") filterSmoking.isBlank() || filterSmoking.equals("Any", ignoreCase = true) else filterSmoking.equals(opt, ignoreCase = true)
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                  !isVip -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                  isSelected -> GoldVip
                  else -> Color.White
                },
                border = BorderStroke(
                  0.8.dp,
                  if (isSelected && isVip) Color(0xFFB8860B) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.clickable(enabled = isVip) {
                  filterSmoking = if (opt == "Any" || isSelected) "" else opt
                }
              ) {
                Text(
                  text = opt,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected && isVip) FontWeight.Bold else FontWeight.Normal,
                  color = when {
                    !isVip -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    isSelected -> Color.Black
                    else -> MaterialTheme.colorScheme.onSurface
                  },
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 4. Zodiac / Star Sign Filter
          Text(
            text = "Zodiac Sign",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isVip) Color(0xFF7A5200) else MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(5.dp))
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val zodiacOptions = listOf("Any") + AllZodiacList
            zodiacOptions.forEach { opt ->
              val isSelected = if (opt == "Any") filterZodiac.isBlank() || filterZodiac.equals("Any", ignoreCase = true) else filterZodiac.equals(opt, ignoreCase = true)
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                  !isVip -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                  isSelected -> GoldVip
                  else -> Color.White
                },
                border = BorderStroke(
                  0.8.dp,
                  if (isSelected && isVip) Color(0xFFB8860B) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.clickable(enabled = isVip) {
                  filterZodiac = if (opt == "Any" || isSelected) "" else opt
                }
              ) {
                Text(
                  text = opt,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected && isVip) FontWeight.Bold else FontWeight.Normal,
                  color = when {
                    !isVip -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    isSelected -> Color.Black
                    else -> MaterialTheme.colorScheme.onSurface
                  },
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      // Save Preferences Button
      Button(
        onClick = {
          val updated = userProfile.copy(
            maxDistanceKm = distanceKm.toInt(),
            minAgePreference = minAge.toInt(),
            maxAgePreference = maxAge.toInt(),
            interestedInGender = interestedIn,
            preferDatingIntention = if (isVip) filterIntention else userProfile.preferDatingIntention,
            preferDrinking = if (isVip) filterDrinking else userProfile.preferDrinking,
            preferSmoking = if (isVip) filterSmoking else userProfile.preferSmoking,
            preferZodiac = if (isVip) filterZodiac else userProfile.preferZodiac
          )
          onSavePreferences(updated)
          onDismiss()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("btn_save_preferences"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
      ) {
        Text("Apply & Save Preferences", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
