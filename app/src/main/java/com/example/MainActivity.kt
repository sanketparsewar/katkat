package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.model.SubscriptionTier
import com.example.ui.screens.MainScreen
import com.example.ui.theme.KatkatTheme
import com.example.util.RazorpayPaymentHelper
import com.example.viewmodel.KatkatViewModel
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

  private val viewModel: KatkatViewModel by viewModels()

  // Track pending payment order info for callback correlation
  private var pendingTier: SubscriptionTier? = null
  private var pendingIsAnnual: Boolean = false
  private var pendingAmount: Int? = null
  private var pendingIsAutoPay: Boolean = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    try {
      RazorpayPaymentHelper.preload(this)
    } catch (t: Throwable) {
      Log.w("MainActivity", "Razorpay preload notice: ${t.message}")
    }

    setContent {
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
      KatkatTheme(themeMode = themeMode) {
        MainScreen(
          viewModel = viewModel,
          onStartRazorpayPayment = { tier, isAnnual, amount, isAutoPay, email, phone, name ->
            launchRazorpayPayment(tier, isAnnual, amount, isAutoPay, email, phone, name)
          }
        )
      }
    }
  }

  fun launchRazorpayPayment(
    tier: SubscriptionTier,
    isAnnual: Boolean,
    amountInRupees: Int? = null,
    isAutoPay: Boolean = true,
    email: String?,
    phone: String?,
    name: String?
  ) {
    pendingTier = tier
    pendingIsAnnual = isAnnual
    pendingAmount = amountInRupees
    pendingIsAutoPay = isAutoPay
    try {
      RazorpayPaymentHelper.startPayment(
        activity = this,
        tier = tier,
        isAnnual = isAnnual,
        amountInRupeesOverride = amountInRupees,
        isAutoPay = isAutoPay,
        userEmail = email,
        userPhone = phone,
        userName = name
      )
    } catch (e: Exception) {
      Log.e("MainActivity", "Failed to start Razorpay payment: ${e.message}", e)
      viewModel.onRazorpayPaymentError(tier, isAnnual, -1, e.message ?: "Failed to open gateway")
    }
  }

  override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
    Log.i("MainActivity", "Razorpay payment success! ID: $razorpayPaymentId, Order ID: ${paymentData?.orderId}")
    val tier = pendingTier ?: SubscriptionTier.TIER_1
    val isAnnual = pendingIsAnnual
    val amount = pendingAmount
    val paymentId = razorpayPaymentId ?: paymentData?.paymentId ?: "pay_${System.currentTimeMillis()}"
    viewModel.onRazorpayPaymentSuccess(
      tier = tier,
      isAnnual = isAnnual,
      razorpayPaymentId = paymentId,
      razorpayOrderId = paymentData?.orderId,
      amountInRupees = amount
    )
    pendingTier = null
    pendingAmount = null
  }

  override fun onPaymentError(errorCode: Int, errorDescription: String?, paymentData: PaymentData?) {
    Log.w("MainActivity", "Razorpay payment error ($errorCode): $errorDescription")
    val tier = pendingTier ?: SubscriptionTier.TIER_1
    val isAnnual = pendingIsAnnual
    viewModel.onRazorpayPaymentError(
      tier = tier,
      isAnnual = isAnnual,
      code = errorCode,
      description = errorDescription ?: "Payment cancelled or declined"
    )
    pendingTier = null
  }

  override fun onStart() {
    super.onStart()
    try {
      if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
          checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
        com.example.service.KatkatNotificationSyncService.startService(this)
      }
    } catch (t: Throwable) {
      android.util.Log.w("MainActivity", "Notice launching sync service: ${t.message}")
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.checkAndResetDailySwipes()
  }
}



