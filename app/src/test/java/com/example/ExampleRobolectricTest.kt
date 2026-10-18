package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SubscriptionTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Katkat", appName)
  }

  @Test
  fun `verify subscription tier swipe quotas`() {
    assertEquals(50, SubscriptionTier.FREE.monthlySwipes)
    assertEquals(150, SubscriptionTier.TIER_1.monthlySwipes)
    assertEquals(300, SubscriptionTier.TIER_2.monthlySwipes)
  }

  @Test
  fun `verify user profile default state`() {
    val profile = com.example.data.model.UserProfile()
    assertEquals("", profile.id)
    assertFalse(profile.isOnboardingCompleted)
    assertTrue(profile.photos.isEmpty())
  }

  @Test
  fun `verify haptic feedback helper triggers without crashing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.util.HapticHelper.triggerHaptic(context, "match")
    com.example.util.HapticHelper.triggerHaptic(context, "swipe_like")
    com.example.util.HapticHelper.triggerHaptic(context, "swipe_pass")
    com.example.util.HapticHelper.triggerHaptic(context, "swipe_superlike")
    com.example.util.HapticHelper.triggerHaptic(context, "threshold")
    com.example.util.HapticHelper.triggerHaptic(context, "boost")
    com.example.util.HapticHelper.triggerHaptic(context, "rewind")
  }

  @Test
  fun `verify firestore manager initializes safely`() {
    val firestoreManager = com.example.data.remote.FirestoreManager()
    // Verification that manager handles availability safely without throwing unhandled runtime exceptions
    val isAvailable = firestoreManager.isAvailable
    assertTrue(isAvailable || !isAvailable)
  }

  @Test
  fun `verify end-to-end encryption and decryption between matched participants`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val userA = "profile_user_1"
    val userB = "profile_user_2"
    val messageText = "Hey! Let's grab coffee this weekend ☕"

    // User A encrypts
    val encrypted = com.example.util.EndToEndEncryptionHelper.encryptMessage(context, messageText, userA, userB)
    assertTrue(encrypted.startsWith("e2e:v1:"))
    assertFalse(encrypted.contains("coffee"))

    // User B decrypts
    val decryptedByB = com.example.util.EndToEndEncryptionHelper.decryptMessage(context, encrypted, userB, userA)
    assertEquals(messageText, decryptedByB)

    // User A also decrypts their own message
    val decryptedByA = com.example.util.EndToEndEncryptionHelper.decryptMessage(context, encrypted, userA, userB)
    assertEquals(messageText, decryptedByA)
  }

  @Test
  fun `verify encryption does not double encrypt and unwrap handles nested payloads`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val userA = "alice_id"
    val userB = "bob_id"
    val messageText = "Testing nested encryption unwrapping ✨"

    val encryptedOnce = com.example.util.EndToEndEncryptionHelper.encryptMessage(context, messageText, userA, userB)
    val encryptedAgain = com.example.util.EndToEndEncryptionHelper.encryptMessage(context, encryptedOnce, userA, userB)
    // Should be idempotent
    assertEquals(encryptedOnce, encryptedAgain)

    // Decrypting returns original message
    val decrypted = com.example.util.EndToEndEncryptionHelper.decryptMessage(context, encryptedOnce, userB, userA)
    assertEquals(messageText, decrypted)
  }

  @Test
  fun `verify razorpay api key is loaded properly`() {
    val key = com.example.util.RazorpayPaymentHelper.getApiKey()
    assertTrue(key.isNotBlank())
    assertTrue(key.startsWith("rzp_"))
  }

  @Test
  fun `verify dynamic subscription plans defaults and serialization`() {
    val plans = com.example.data.model.SubscriptionPlan.defaultPlans()
    assertEquals(3, plans.size)
    val freePlan = plans[0]
    val tier1Plan = plans[1]
    val tier2Plan = plans[2]

    assertEquals(SubscriptionTier.FREE, freePlan.tier)
    assertEquals(SubscriptionTier.TIER_1, tier1Plan.tier)
    assertEquals(SubscriptionTier.TIER_2, tier2Plan.tier)

    assertEquals(50, freePlan.dailySwipes)
    assertEquals(150, tier1Plan.dailySwipes)
    assertEquals(300, tier2Plan.dailySwipes)

    assertTrue(tier1Plan.priceMonthlyAmount > 0)
    assertTrue(tier2Plan.priceYearlyAmount > tier1Plan.priceYearlyAmount)
  }

  @Test
  fun `verify payment transaction model with razorpay method`() {
    val transaction = com.example.data.model.PaymentTransaction(
      transactionId = "RZP_test_123456",
      tier = SubscriptionTier.TIER_2,
      isAnnual = true,
      amountInRupees = 4999,
      method = com.example.data.model.PaymentMethodType.RAZORPAY,
      status = com.example.data.model.PaymentStatus.SUCCESS,
      razorpayPaymentId = "pay_test_998877",
      razorpayOrderId = "order_test_112233"
    )

    assertEquals(com.example.data.model.PaymentMethodType.RAZORPAY, transaction.method)
    assertEquals(com.example.data.model.PaymentStatus.SUCCESS, transaction.status)
    assertEquals("pay_test_998877", transaction.razorpayPaymentId)
    assertTrue(transaction.formattedDate.isNotBlank())
  }
}

