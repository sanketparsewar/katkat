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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethodType
import com.example.data.model.SubscriptionTier
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.ui.theme.LikeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutBottomSheet(
  tier: SubscriptionTier,
  isAnnual: Boolean,
  isProcessing: Boolean,
  onConfirmPayment: (PaymentMethodType, upiId: String?, cardLast4: String?) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedMethod by remember { mutableStateOf(PaymentMethodType.UPI) }

  // UPI input states
  var upiId by remember { mutableStateOf("") }
  val upiApps = listOf("Google Pay", "PhonePe", "Paytm", "BHIM UPI")
  var selectedUpiApp by remember { mutableStateOf("Google Pay") }

  // Card input states
  var cardNumber by remember { mutableStateOf("") }
  var cardExpiry by remember { mutableStateOf("") }
  var cardCvv by remember { mutableStateOf("") }
  var cardName by remember { mutableStateOf("") }

  // Net banking state
  val banks = listOf("State Bank of India", "HDFC Bank", "ICICI Bank", "Axis Bank", "Kotak Mahindra")
  var selectedBank by remember { mutableStateOf("HDFC Bank") }

  // Wallet state
  val wallets = listOf("Paytm Wallet", "PhonePe Wallet", "Amazon Pay")
  var selectedWallet by remember { mutableStateOf("Paytm Wallet") }

  val amount = if (isAnnual) tier.priceYearlyAmount else tier.priceMonthlyAmount
  val planPeriod = if (isAnnual) "Yearly (12 Months)" else "Monthly (1 Month)"
  val formattedPrice = if (isAnnual) tier.priceYearly else tier.priceMonthly

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
              text = "Secure Checkout",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            )
            Text(
              text = "100% Encrypted • 256-bit SSL",
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

      // Order Summary Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = tier.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "$planPeriod • ${tier.dailySwipes} Swipes/day",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.5.sp
                )
              )
            }

            Text(
              text = formattedPrice,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (tier == SubscriptionTier.TIER_2) Color(0xFFC79100) else CoralPrimary
              )
            )
          }

          if (isAnnual && tier.yearlySavings.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = LikeGreen.copy(alpha = 0.12f)
            ) {
              Text(
                text = "🎉 ${tier.yearlySavings}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = LikeGreen,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.5.sp
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Payment Method Options Selector
      Text(
        text = "Select Payment Method",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      PaymentMethodType.values().forEach { method ->
        val isSelected = selectedMethod == method
        val methodIcon = when (method) {
          PaymentMethodType.UPI -> Icons.Default.QrCode2
          PaymentMethodType.CARD -> Icons.Default.CreditCard
          PaymentMethodType.NET_BANKING -> Icons.Default.AccountBalance
          PaymentMethodType.WALLET -> Icons.Default.AccountBalanceWallet
        }

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = !isProcessing) { selectedMethod = method },
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CoralPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
          ),
          border = BorderStroke(
            1.5.dp,
            if (isSelected) CoralPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isSelected) CoralPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = methodIcon,
                contentDescription = null,
                tint = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = method.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                fontSize = 13.sp
              )
              Text(
                text = method.subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 10.5.sp
                )
              )
            }

            RadioButton(
              selected = isSelected,
              onClick = { if (!isProcessing) selectedMethod = method },
              colors = RadioButtonDefaults.colors(
                selectedColor = CoralPrimary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Input fields based on selected method
      when (selectedMethod) {
        PaymentMethodType.UPI -> {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
              .padding(14.dp)
          ) {
            Text(
              text = "Choose Quick UPI App",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              upiApps.forEach { app ->
                val isAppSelected = selectedUpiApp == app
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isAppSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { selectedUpiApp = app }
                ) {
                  Text(
                    text = app,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (isAppSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                      fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Or Enter UPI ID (VPA)",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = upiId,
              onValueChange = { upiId = it },
              placeholder = { Text("e.g. yourname@okhdfcbank", fontSize = 12.sp) },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_upi_id"),
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CoralPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
              )
            )
          }
        }

        PaymentMethodType.CARD -> {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
              .padding(14.dp)
          ) {
            Text(
              text = "Card Details",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = cardNumber,
              onValueChange = { if (it.length <= 19) cardNumber = it },
              placeholder = { Text("Card Number (16 digits)", fontSize = 12.sp) },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_card_number"),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CoralPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
              )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = cardExpiry,
                onValueChange = { if (it.length <= 5) cardExpiry = it },
                placeholder = { Text("MM/YY", fontSize = 12.sp) },
                modifier = Modifier
                  .weight(1f)
                  .testTag("input_card_expiry"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CoralPrimary,
                  unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
              )

              OutlinedTextField(
                value = cardCvv,
                onValueChange = { if (it.length <= 4) cardCvv = it },
                placeholder = { Text("CVV", fontSize = 12.sp) },
                modifier = Modifier
                  .weight(1f)
                  .testTag("input_card_cvv"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CoralPrimary,
                  unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = cardName,
              onValueChange = { cardName = it },
              placeholder = { Text("Name on Card", fontSize = 12.sp) },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_card_name"),
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CoralPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
              )
            )
          }
        }

        PaymentMethodType.NET_BANKING -> {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
              .padding(14.dp)
          ) {
            Text(
              text = "Popular Indian Banks",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            banks.forEach { bank ->
              val isBankSelected = selectedBank == bank
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { selectedBank = bank }
                  .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = isBankSelected,
                  onClick = { selectedBank = bank },
                  colors = RadioButtonDefaults.colors(selectedColor = CoralPrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = bank, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp))
              }
            }
          }
        }

        PaymentMethodType.WALLET -> {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
              .padding(14.dp)
          ) {
            Text(
              text = "Choose Wallet",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            wallets.forEach { wallet ->
              val isWalletSelected = selectedWallet == wallet
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { selectedWallet = wallet }
                  .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = isWalletSelected,
                  onClick = { selectedWallet = wallet },
                  colors = RadioButtonDefaults.colors(selectedColor = CoralPrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = wallet, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp))
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Pay Button
      Button(
        onClick = {
          val resolvedUpi = if (upiId.isNotBlank()) upiId else "$selectedUpiApp@upi"
          val last4 = if (cardNumber.length >= 4) cardNumber.takeLast(4) else "4242"
          onConfirmPayment(selectedMethod, resolvedUpi, last4)
        },
        enabled = !isProcessing,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("btn_pay_now"),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (tier == SubscriptionTier.TIER_2) GoldVip else CoralPrimary
        )
      ) {
        if (isProcessing) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Authorizing ₹$amount...",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }
        } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Pay ₹$amount & Activate ${tier.title}",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.5.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Shield,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "PCI-DSS Level 1 Compliant • RBI Mandate Protected",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
          )
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
