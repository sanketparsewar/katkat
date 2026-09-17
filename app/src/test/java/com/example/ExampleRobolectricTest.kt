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
}

