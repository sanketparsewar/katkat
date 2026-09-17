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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriptionState
import com.example.data.model.SubscriptionTier
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.ui.theme.LikeGreen
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.PeachSecondary
import com.example.ui.theme.SuperlikeBlue
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarmCream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
  subscriptionState: SubscriptionState,
  onSelectTier: (SubscriptionTier, Boolean) -> Unit,
  onRestorePurchases: () -> Unit,
  onResetSwipeUsageForTesting: () -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedTier by remember { mutableStateOf(if (subscriptionState.currentTier == SubscriptionTier.FREE) SubscriptionTier.TIER_1 else subscriptionState.currentTier) }
  var isAnnual by remember { mutableStateOf(subscriptionState.isAnnualBilling) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.background,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .testTag("paywall_bottom_sheet"),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header with close button
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
              .background(CoralPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ElectricBolt,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Katkat Premium",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = TextPrimaryDark
            )
          )
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Unlock More Swipes & Find Love Faster",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = CoralPrimary
        ),
        textAlign = TextAlign.Center
      )

      Text(
        text = "Native App Store subscriptions powered by Emergent RevenueCat",
        style = MaterialTheme.typography.labelSmall.copy(
          color = TextSecondaryDark
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 2.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Billing Cycle Switch (Monthly vs Yearly)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp)),
        color = PeachBlush
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Annual Billing",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
              )
            )
            Text(
              text = "Save 40% with yearly subscription",
              style = MaterialTheme.typography.labelSmall.copy(
                color = CoralPrimary,
                fontWeight = FontWeight.SemiBold
              )
            )
          }
          Switch(
            checked = isAnnual,
            onCheckedChange = { isAnnual = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = CoralPrimary,
              uncheckedThumbColor = Color.White,
              uncheckedTrackColor = Color.LightGray
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3 Tiers Cards Comparison
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Free Tier Card
        TierCard(
          tier = SubscriptionTier.FREE,
          isSelected = selectedTier == SubscriptionTier.FREE,
          isCurrent = subscriptionState.currentTier == SubscriptionTier.FREE,
          isAnnual = isAnnual,
          onSelect = { selectedTier = SubscriptionTier.FREE }
        )

        // Paid Tier 1 (Katkat Plus)
        TierCard(
          tier = SubscriptionTier.TIER_1,
          isSelected = selectedTier == SubscriptionTier.TIER_1,
          isCurrent = subscriptionState.currentTier == SubscriptionTier.TIER_1,
          isAnnual = isAnnual,
          onSelect = { selectedTier = SubscriptionTier.TIER_1 }
        )

        // Paid Tier 2 (Katkat VIP)
        TierCard(
          tier = SubscriptionTier.TIER_2,
          isSelected = selectedTier == SubscriptionTier.TIER_2,
          isCurrent = subscriptionState.currentTier == SubscriptionTier.TIER_2,
          isAnnual = isAnnual,
          onSelect = { selectedTier = SubscriptionTier.TIER_2 }
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Selected Tier Perks Breakdown
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp)),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF0E5DF))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Included with ${selectedTier.title}:",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = TextPrimaryDark
            )
          )
          Spacer(modifier = Modifier.height(8.dp))
          selectedTier.perks.forEach { perk ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(20.dp)
                  .clip(CircleShape)
                  .background(LikeGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = LikeGreen,
                  modifier = Modifier.size(13.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = perk,
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = TextPrimaryDark,
                  fontSize = 13.sp
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Main Upgrade CTA Button
      Button(
        onClick = {
          onSelectTier(selectedTier, isAnnual)
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_confirm_subscription"),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (selectedTier == SubscriptionTier.TIER_2) GoldVip else CoralPrimary
        )
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (selectedTier == SubscriptionTier.FREE) Icons.Default.Check else Icons.Default.LockOpen,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (selectedTier == subscriptionState.currentTier) "Current Plan Active"
            else if (selectedTier == SubscriptionTier.FREE) "Switch to Free Tier"
            else "Continue with ${selectedTier.title}",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Footer action links: Restore Purchases & Test Reset
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = onRestorePurchases,
          modifier = Modifier.testTag("btn_restore_purchases")
        ) {
          Text(
            text = "Restore Purchases",
            style = MaterialTheme.typography.labelMedium.copy(color = TextSecondaryDark)
          )
        }

        TextButton(
          onClick = onResetSwipeUsageForTesting,
          modifier = Modifier.testTag("btn_reset_swipes_dev")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              tint = CoralPrimary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Reset Swipes (Dev)",
              style = MaterialTheme.typography.labelMedium.copy(color = CoralPrimary)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
fun TierCard(
  tier: SubscriptionTier,
  isSelected: Boolean,
  isCurrent: Boolean,
  isAnnual: Boolean,
  onSelect: () -> Unit
) {
  val borderColor = when {
    isSelected && tier == SubscriptionTier.TIER_2 -> GoldVip
    isSelected -> CoralPrimary
    else -> Color(0xFFEADBCE)
  }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onSelect)
      .testTag("tier_card_${tier.name}"),
    color = if (isSelected) PeachBlush else Color.White,
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = tier.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = TextPrimaryDark
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(
                when (tier) {
                  SubscriptionTier.FREE -> Color.LightGray
                  SubscriptionTier.TIER_1 -> CoralPrimary
                  SubscriptionTier.TIER_2 -> GoldVip
                }
              )
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "${tier.monthlySwipes} SWIPES/MO",
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (tier == SubscriptionTier.FREE) "Standard discovery"
          else if (isAnnual) tier.priceYearly
          else tier.priceMonthly,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = if (isSelected) CoralPrimary else TextSecondaryDark,
            fontWeight = FontWeight.SemiBold
          )
        )
      }

      // Radio indicator
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(
            if (isSelected) CoralPrimary else Color.Transparent
          )
          .border(2.dp, if (isSelected) CoralPrimary else Color.LightGray, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        if (isSelected) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(Color.White)
          )
        }
      }
    }
  }
}
