package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriptionState
import com.example.data.model.SubscriptionTier
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.ui.theme.PeachSecondary
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun KatkatTopBar(
  subscriptionState: SubscriptionState,
  onOpenPaywall: () -> Unit,
  onOpenFilter: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .statusBarsPadding(),
    color = MaterialTheme.colorScheme.background,
    shadowElevation = 0.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Logo & App Name
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.testTag("app_logo_title")
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  colors = listOf(CoralPrimary, PeachSecondary)
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = "Katkat Logo",
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Katkat",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = CoralPrimary,
                  letterSpacing = (-0.5).sp
                )
              )
              if (subscriptionState.currentTier != SubscriptionTier.FREE) {
                Spacer(modifier = Modifier.width(6.dp))
                val (badgeText, badgeBg, badgeTextColor) = when (subscriptionState.currentTier) {
                  SubscriptionTier.TIER_2 -> Triple("👑 VIP", GoldVip, Color.Black)
                  SubscriptionTier.TIER_1 -> Triple("💜 Plus", Color(0xFF7B1FA2), Color.White)
                  SubscriptionTier.FREE -> Triple("", Color.Transparent, Color.Transparent)
                }
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg)
                    .clickable { onOpenPaywall() }
                    .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    .testTag("topbar_active_plan_badge")
                ) {
                  Text(
                    text = badgeText,
                    color = badgeTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                  )
                }
              }
            }
          }
        }

        // Action Buttons: Swipes Counter Pill & Upgrade Button
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Swipes remaining indicator pill
          Box(
            modifier = Modifier
              .testTag("swipe_counter_pill")
              .clip(RoundedCornerShape(20.dp))
              .background(
                if (subscriptionState.hasReachedLimit) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceVariant
              )
              .border(
                1.dp,
                if (subscriptionState.hasReachedLimit) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(20.dp)
              )
              .clickable { onOpenPaywall() }
              .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = if (subscriptionState.hasReachedLimit) MaterialTheme.colorScheme.error else CoralPrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${subscriptionState.remainingSwipes} left",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (subscriptionState.hasReachedLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
              )
            }
          }

          // Paywall / Tier Upgrade button
          IconButton(
            onClick = onOpenPaywall,
            modifier = Modifier
              .size(38.dp)
              .testTag("btn_upgrade_topbar")
              .clip(CircleShape)
              .background(
                Brush.horizontalGradient(
                  colors = listOf(CoralPrimary, PeachSecondary)
                )
              )
          ) {
            Icon(
              imageVector = Icons.Default.Stars,
              contentDescription = "Subscription Plans",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      // Swipe usage mini progress bar
      Spacer(modifier = Modifier.height(6.dp))
      LinearProgressIndicator(
        progress = { subscriptionState.usagePercentage },
        modifier = Modifier
          .fillMaxWidth()
          .height(3.dp)
          .clip(CircleShape),
        color = if (subscriptionState.hasReachedLimit) Color(0xFFFF4757) else CoralPrimary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )
    }
  }
}
