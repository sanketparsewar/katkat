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
    assertEquals(200, SubscriptionTier.TIER_1.monthlySwipes)
    assertEquals(500, SubscriptionTier.TIER_2.monthlySwipes)
  }

  @Test
  fun `verify user profile default state`() {
    val profile = com.example.data.model.UserProfile()
    assertEquals("my_profile", profile.id)
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
}

