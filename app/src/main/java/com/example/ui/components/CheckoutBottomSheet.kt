package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriptionPlan
import com.example.data.model.SubscriptionTier
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.ui.theme.LikeGreen

/**
 * Dedicated Razorpay Checkout Bottom Sheet.
 * Displays dynamic plan pricing directly from the database, keeps AutoPay enabled by default,
 * and launches Razorpay Payment Gateway directly without any other third-party or mock options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutBottomSheet(
  plan: SubscriptionPlan,
  isAnnual: Boolean,
  isProcessing: Boolean,
  customerName: String? = null,
  onPayWithRazorpay: (amountInRupees: Int, isAutoPay: Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val tier = plan.tier
  val amount = if (isAnnual) plan.priceYearlyAmount else plan.priceMonthlyAmount
  val planPeriod = if (isAnnual) "Yearly (12 Months)" else "Monthly (1 Month)"
  val formattedPrice = if (isAnnual) plan.priceYearly else plan.priceMonthly

  // AutoPay enabled by default
  var isAutoPayEnabled by remember { mutableStateOf(true) }

  ModalBottomSheet(
    onDismissRequest = { if (!isProcessing) onDismiss() },
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.background,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 6.dp)
        .testTag("checkout_bottom_sheet"),
      horizontalAlignment = Alignment.CenterHorizontally
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
              .background(
                Brush.linearGradient(
                  if (tier == SubscriptionTier.TIER_2) listOf(GoldVip, Color(0xFFFF8F00))
                  else listOf(CoralPrimary, Color(0xFF7B1FA2))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Razorpay Checkout",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            )
            Text(
              text = "Encrypted & Verified Payment",
              style = MaterialTheme.typography.labelSmall.copy(
                color = LikeGreen,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
              )
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          enabled = !isProcessing,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Order Summary Card with Dynamic Pricing from Database
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = plan.title,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (plan.badge.isNotBlank()) {
                  Spacer(modifier = Modifier.width(8.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (tier == SubscriptionTier.TIER_2) GoldVip.copy(alpha = 0.2f) else CoralPrimary.copy(alpha = 0.12f)
                  ) {
                    Text(
                      text = plan.badge,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = if (tier == SubscriptionTier.TIER_2) Color(0xFFC79100) else CoralPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                      )
                    )
                  }
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "$planPeriod • ${plan.dailySwipes} Swipes/day",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 12.sp
                )
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = formattedPrice,
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = if (tier == SubscriptionTier.TIER_2) Color(0xFFC79100) else CoralPrimary
                )
              )
              if (isAnnual && plan.effectiveMonthlyPrice.isNotBlank()) {
                Text(
                  text = plan.effectiveMonthlyPrice,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                  )
                )
              }
            }
          }

          if (isAnnual && plan.yearlySavings.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = LikeGreen.copy(alpha = 0.12f)
            ) {
              Text(
                text = "🎉 ${plan.yearlySavings}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = LikeGreen,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Dynamic features with green tick if included, red tick if not included
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            plan.resolvedFeatures.forEach { feature ->
              Row(verticalAlignment = Alignment.CenterVertically) {
                val tickColor = if (feature.included) LikeGreen else Color(0xFFE53935)
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = if (feature.included) "Included" else "Not included",
                  tint = tickColor,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = feature.text,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = if (feature.included) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    fontSize = 12.sp
                  )
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // AutoPay Renewal Toggle Card (Enabled by default)
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(LikeGreen.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Autorenew,
                contentDescription = null,
                tint = LikeGreen,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "AutoPay Renewal",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Enabled by default • Hassle-free renewal",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = LikeGreen,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.sp
                )
              )
            }
          }

          Switch(
            checked = isAutoPayEnabled,
            onCheckedChange = { isAutoPayEnabled = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = LikeGreen
            ),
            modifier = Modifier.testTag("switch_autopay")
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Primary Action: Launch Razorpay Checkout
      Button(
        onClick = { onPayWithRazorpay(amount, isAutoPayEnabled) },
        enabled = !isProcessing,
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("btn_pay_with_razorpay"),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (tier == SubscriptionTier.TIER_2) GoldVip else Color(0xFF0C2340)
        )
      ) {
        if (isProcessing) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = if (tier == SubscriptionTier.TIER_2) Color.Black else Color.White,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Opening Razorpay...",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (tier == SubscriptionTier.TIER_2) Color.Black else Color.White
              )
            )
          }
        } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = if (tier == SubscriptionTier.TIER_2) Color.Black else Color.White,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Pay ₹$amount with Razorpay",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (tier == SubscriptionTier.TIER_2) Color.Black else Color.White,
                fontSize = 15.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }
  }
}
