package com.example.ui.components

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
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import com.example.ui.theme.LikeGreen

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
  var selectedTier by remember {
    mutableStateOf(
      if (subscriptionState.currentTier == SubscriptionTier.FREE) SubscriptionTier.TIER_1 else subscriptionState.currentTier
    )
  }
  var isAnnual by remember { mutableStateOf(subscriptionState.isAnnualBilling) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.background,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 2.dp)
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
              .size(28.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(listOf(CoralPrimary, Color(0xFF9C27B0)))
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.WorkspacePremium,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "KatKat Plans & Pricing",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
              )
            )
            Text(
              text = "Choose your path to more matches",
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Billing Cycle Selector Tabs (Monthly vs Yearly)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Monthly Option
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(13.dp))
              .background(if (!isAnnual) CoralPrimary else Color.Transparent)
              .clickable { isAnnual = false }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Monthly",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (!isAnnual) FontWeight.Bold else FontWeight.Medium,
                color = if (!isAnnual) Color.White else MaterialTheme.colorScheme.onSurface,
                fontSize = 12.5.sp
              )
            )
          }

          // Yearly Option (with Savings Badge)
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(13.dp))
              .background(if (isAnnual) CoralPrimary else Color.Transparent)
              .clickable { isAnnual = true }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Yearly Plan",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = if (isAnnual) FontWeight.Bold else FontWeight.Medium,
                  color = if (isAnnual) Color.White else MaterialTheme.colorScheme.onSurface,
                  fontSize = 12.5.sp
                )
              )
              Spacer(modifier = Modifier.width(5.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isAnnual) GoldVip else Color(0xFFE91E63))
                  .padding(horizontal = 5.dp, vertical = 1.5.dp)
              ) {
                Text(
                  text = "SAVE 50%",
                  color = if (isAnnual) Color.Black else Color.White,
                  fontSize = 8.5.sp,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3 Tiers Cards Comparison (Compact size and reduced padding)
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
      ) {
        // Free Tier Card
        CompactTierCard(
          tier = SubscriptionTier.FREE,
          isSelected = selectedTier == SubscriptionTier.FREE,
          isCurrent = subscriptionState.currentTier == SubscriptionTier.FREE,
          isAnnual = isAnnual,
          onSelect = { selectedTier = SubscriptionTier.FREE }
        )

        // Paid Tier 1 (KatKat Plus)
        CompactTierCard(
          tier = SubscriptionTier.TIER_1,
          isSelected = selectedTier == SubscriptionTier.TIER_1,
          isCurrent = subscriptionState.currentTier == SubscriptionTier.TIER_1,
          isAnnual = isAnnual,
          onSelect = { selectedTier = SubscriptionTier.TIER_1 }
        )

        // Paid Tier 2 (KatKat VIP)
        CompactTierCard(
          tier = SubscriptionTier.TIER_2,
          isSelected = selectedTier == SubscriptionTier.TIER_2,
          isCurrent = subscriptionState.currentTier == SubscriptionTier.TIER_2,
          isAnnual = isAnnual,
          onSelect = { selectedTier = SubscriptionTier.TIER_2 }
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Selected Tier Breakdown Box (Compact design)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(11.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "${selectedTier.title} — ${selectedTier.subtitle}",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  fontSize = 13.sp
                )
              )
              Text(
                text = "\"${selectedTier.tagline}\"",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = CoralPrimary,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.sp
                )
              )
            }

            if (selectedTier != SubscriptionTier.FREE && isAnnual) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFFE8F5E9))
                  .padding(horizontal = 6.dp, vertical = 2.5.dp)
              ) {
                Text(
                  text = selectedTier.effectiveMonthlyPrice,
                  color = Color(0xFF2E7D32),
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                )
              }
            }
          }

          if (isAnnual && selectedTier.yearlySavings.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFFFF8E1))
                .padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Savings,
                contentDescription = null,
                tint = Color(0xFFF57F17),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = selectedTier.yearlySavings,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFFF57F17),
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.5.sp
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Display perks in a compact format
          selectedTier.perks.forEach { perk ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(15.dp)
                  .clip(CircleShape)
                  .background(
                    if (perk.contains("Locked", ignoreCase = true) || perk.contains("Ads", ignoreCase = true)) {
                      MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                    } else {
                      LikeGreen.copy(alpha = 0.15f)
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = if (perk.contains("Locked", ignoreCase = true) || perk.contains("Ads", ignoreCase = true)) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  } else {
                    LikeGreen
                  },
                  modifier = Modifier.size(10.dp)
                )
              }
              Spacer(modifier = Modifier.width(7.dp))
              Text(
                text = perk,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = MaterialTheme.colorScheme.onSurface,
                  fontSize = 11.5.sp
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Main Action CTA Button (Streamlined height)
      val ctaText = when {
        selectedTier == subscriptionState.currentTier && isAnnual == subscriptionState.isAnnualBilling -> "Current Plan Active"
        selectedTier == SubscriptionTier.FREE -> "Switch to Free Tier (₹0)"
        isAnnual -> "Get ${selectedTier.title} • ${selectedTier.priceYearly}"
        else -> "Get ${selectedTier.title} • ${selectedTier.priceMonthly}"
      }

      Button(
        onClick = {
          onSelectTier(selectedTier, isAnnual)
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp)
          .testTag("btn_confirm_subscription"),
        shape = RoundedCornerShape(23.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = when (selectedTier) {
            SubscriptionTier.TIER_2 -> GoldVip
            SubscriptionTier.TIER_1 -> Color(0xFF7B1FA2)
            SubscriptionTier.FREE -> CoralPrimary
          }
        )
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (selectedTier == SubscriptionTier.FREE) Icons.Default.Check else Icons.Default.LockOpen,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = ctaText,
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 13.5.sp
            )
          )
        }
      }

      // Footer action links: Restore Purchases & Test Reset
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = onRestorePurchases,
          modifier = Modifier.testTag("btn_restore_purchases")
        ) {
          Text(
            text = "Restore Purchases",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
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
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "Reset Swipes (Dev)",
              style = MaterialTheme.typography.labelSmall.copy(
                color = CoralPrimary,
                fontSize = 11.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
fun CompactTierCard(
  tier: SubscriptionTier,
  isSelected: Boolean,
  isCurrent: Boolean,
  isAnnual: Boolean,
  onSelect: () -> Unit
) {
  val borderColor = when {
    isSelected && tier == SubscriptionTier.TIER_2 -> GoldVip
    isSelected && tier == SubscriptionTier.TIER_1 -> Color(0xFF9C27B0)
    isSelected -> CoralPrimary
    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
  }

  val containerBg = when {
    isSelected && tier == SubscriptionTier.TIER_2 -> Color(0xFFFFF9E6)
    isSelected && tier == SubscriptionTier.TIER_1 -> Color(0xFFF3E5F5)
    isSelected -> Color(0xFFFFF0F3)
    else -> MaterialTheme.colorScheme.surface
  }

  val tierEmoji = when (tier) {
    SubscriptionTier.FREE -> "🆓"
    SubscriptionTier.TIER_1 -> "💜"
    SubscriptionTier.TIER_2 -> "👑"
  }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(13.dp))
      .clickable(onClick = onSelect)
      .testTag("tier_card_${tier.name}"),
    color = containerBg,
    shape = RoundedCornerShape(13.dp),
    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 11.dp, vertical = 9.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = tierEmoji, fontSize = 13.sp)
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "${tier.title} — ${tier.subtitle}",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface,
              fontSize = 13.sp
            )
          )
          Spacer(modifier = Modifier.width(6.dp))

          // Swipes badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(5.dp))
              .background(
                when (tier) {
                  SubscriptionTier.FREE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                  SubscriptionTier.TIER_1 -> Color(0xFF7B1FA2)
                  SubscriptionTier.TIER_2 -> GoldVip
                }
              )
              .padding(horizontal = 5.dp, vertical = 1.dp)
          ) {
            Text(
              text = "${tier.dailySwipes}/DAY",
              color = Color.White,
              fontSize = 8.5.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Price display
          if (tier == SubscriptionTier.FREE) {
            Text(
              text = "₹0 • Free forever",
              style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
              )
            )
          } else if (isAnnual) {
            Text(
              text = tier.priceYearly,
              style = MaterialTheme.typography.bodySmall.copy(
                color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.5.sp
              )
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "(${tier.effectiveMonthlyPrice})",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            )
          } else {
            Text(
              text = tier.priceMonthly,
              style = MaterialTheme.typography.bodySmall.copy(
                color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.5.sp
              )
            )
          }

          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "• ${tier.tagline}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.5.sp
            ),
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Selection indicator
      Box(
        modifier = Modifier
          .size(18.dp)
          .clip(CircleShape)
          .background(
            if (isSelected) borderColor else Color.Transparent
          )
          .border(1.5.dp, if (isSelected) borderColor else MaterialTheme.colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        if (isSelected) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(Color.White)
          )
        }
      }
    }
  }
}
