package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PaymentMethodType(
  val displayName: String,
  val iconName: String,
  val subtitle: String
) {
  UPI(
    displayName = "UPI (Google Pay / PhonePe / Paytm)",
    iconName = "upi",
    subtitle = "Instant payment via any UPI App"
  ),
  CARD(
    displayName = "Credit / Debit Card",
    iconName = "card",
    subtitle = "Visa, MasterCard, RuPay, Amex"
  ),
  NET_BANKING(
    displayName = "Net Banking",
    iconName = "bank",
    subtitle = "All major Indian banks supported"
  ),
  WALLET(
    displayName = "Wallets",
    iconName = "wallet",
    subtitle = "Paytm, PhonePe, Amazon Pay"
  )
}

enum class PaymentStatus {
  INITIATED,
  PROCESSING,
  SUCCESS,
  FAILED,
  CANCELLED
}

data class PaymentTransaction(
  val transactionId: String,
  val tier: SubscriptionTier,
  val isAnnual: Boolean,
  val amountInRupees: Int,
  val method: PaymentMethodType,
  val status: PaymentStatus,
  val timestamp: Long = System.currentTimeMillis(),
  val upiId: String? = null,
  val cardLast4: String? = null,
  val failureReason: String? = null
) {
  val formattedDate: String
    get() {
      val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
      return sdf.format(Date(timestamp))
    }

  val displayAmount: String
    get() = "₹$amountInRupees"
}
