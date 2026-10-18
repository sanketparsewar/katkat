package com.example.util

import android.app.Activity
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.SubscriptionTier
import com.razorpay.Checkout
import org.json.JSONObject

object RazorpayPaymentHelper {
  private const val TAG = "RazorpayPaymentHelper"

  /**
   * Retrieves the configured Razorpay Key ID from BuildConfig (injected via .env or AI Studio Secrets).
   * Falls back to user provided test key if placeholder.
   */
  fun getApiKey(): String {
    val configuredKey = try {
      BuildConfig.RAZORPAY_KEY_ID
    } catch (_: Exception) {
      ""
    }
    return if (configuredKey.isNotBlank() && !configuredKey.contains("placeholder", ignoreCase = true)) {
      configuredKey
    } else {
      "rzp_test_SYWvy2aPs56JHZ"
    }
  }

  /**
   * Preloads Razorpay Checkout SDK resources for snappy opening.
   */
  fun preload(activity: Activity) {
    try {
      Checkout.preload(activity.applicationContext)
    } catch (t: Throwable) {
      Log.w(TAG, "Notice preloading Razorpay: ${t.message}")
    }
  }

  /**
   * Launches Razorpay Standard Checkout overlay on the given Activity.
   *
   * @param activity Hosting Activity that implements PaymentResultListener or PaymentResultWithDataListener
   * @param tier Selected subscription tier
   * @param isAnnual Whether user chose annual billing
   * @param userEmail Optional user email
   * @param userPhone Optional user contact number
   * @param userName Optional user name
   * @return Generated order reference or transaction tracking ID
   */
  fun startPayment(
    activity: Activity,
    tier: SubscriptionTier,
    isAnnual: Boolean,
    userEmail: String? = null,
    userPhone: String? = null,
    userName: String? = null,
    customOrderId: String? = null
  ): String {
    val checkout = Checkout()
    val apiKey = getApiKey()
    checkout.setKeyID(apiKey)

    val amountInRupees = if (isAnnual) tier.priceYearlyAmount else tier.priceMonthlyAmount
    val amountInPaise = amountInRupees * 100
    val clientTxnId = customOrderId ?: "txn_${System.currentTimeMillis()}_${(1000..9999).random()}"

    try {
      val options = JSONObject()
      options.put("name", "Katkat Dating")
      options.put("description", "${tier.title} (${if (isAnnual) "12 Months" else "1 Month"})")
      options.put("currency", "INR")
      options.put("amount", amountInPaise)
      options.put("send_sms_hash", true)

      // Theme styling aligned with Katkat brand (Coral / Burgundy)
      val theme = JSONObject()
      theme.put("color", if (tier == SubscriptionTier.TIER_2) "#FFB300" else "#E53935")
      options.put("theme", theme)

      // Customer prefill
      val prefill = JSONObject()
      if (!userEmail.isNullOrBlank()) {
        prefill.put("email", userEmail)
      } else {
        prefill.put("email", "support@katkatdating.com")
      }
      if (!userPhone.isNullOrBlank()) {
        prefill.put("contact", userPhone.replace("+", "").trim())
      }
      if (!userName.isNullOrBlank()) {
        prefill.put("name", userName)
      }
      options.put("prefill", prefill)

      // Metadata notes
      val notes = JSONObject()
      notes.put("plan_id", tier.name)
      notes.put("plan_title", tier.title)
      notes.put("is_annual", isAnnual.toString())
      notes.put("client_txn_id", clientTxnId)
      options.put("notes", notes)

      val retryObj = JSONObject()
      retryObj.put("enabled", true)
      retryObj.put("max_count", 2)
      options.put("retry", retryObj)

      Log.i(TAG, "Opening Razorpay checkout for ₹$amountInRupees (${tier.title}), Key ID: $apiKey")
      checkout.open(activity, options)
      return clientTxnId
    } catch (e: Exception) {
      Log.e(TAG, "Error initiating Razorpay checkout: ${e.message}", e)
      throw e
    }
  }
}
