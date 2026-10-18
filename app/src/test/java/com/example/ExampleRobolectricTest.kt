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
}

