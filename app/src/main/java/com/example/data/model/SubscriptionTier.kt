package com.example.data.model

enum class SubscriptionTier(
  val title: String,
  val badge: String,
  val monthlySwipes: Int,
  val priceMonthly: String,
  val priceYearly: String,
  val savingsPercent: String,
  val perks: List<String>
) {
  FREE(
    title = "Katkat Free",
    badge = "Basic",
    monthlySwipes = 50,
    priceMonthly = "₹0",
    priceYearly = "₹0",
    savingsPercent = "",
    perks = listOf(
      "50 Swipes per month",
      "Standard Match Deck",
      "Real-time Mutual Chat",
      "Profile Photos & Bios"
    )
  ),
  TIER_1(
    title = "Katkat Plus",
    badge = "Popular",
    monthlySwipes = 200,
    priceMonthly = "₹199/month",
    priceYearly = "₹119/mo (₹1,428/yr)",
    savingsPercent = "SAVE 40%",
    perks = listOf(
      "200 Swipes per month",
      "Unlimited Rewinds (Undo accidental passes)",
      "5 Free Super Likes per week",
      "See Who Liked You before swiping",
      "No Banner Advertisements"
    )
  ),
  TIER_2(
    title = "Katkat VIP",
    badge = "Ultimate",
    monthlySwipes = 500,
    priceMonthly = "₹399/month",
    priceYearly = "₹239/mo (₹2,868/yr)",
    savingsPercent = "SAVE 40%",
    perks = listOf(
      "500 Swipes per month",
      "Priority Likes (be seen 3x faster)",
      "Unlimited Rewinds & Super Likes",
      "Direct Messaging before matching",
      "1 Free Profile Boost per month",
      "Passport Mode (swipe anywhere in the world)"
    )
  )
}

data class SubscriptionState(
  val currentTier: SubscriptionTier = SubscriptionTier.FREE,
  val swipesUsedThisMonth: Int = 0,
  val currentMonthKey: String = "2026-09",
  val isAnnualBilling: Boolean = false,
  val subscriptionExpiryDate: String = "Renews Oct 16, 2026",
  val isRevenueCatConnected: Boolean = true
) {
  val remainingSwipes: Int
    get() = (currentTier.monthlySwipes - swipesUsedThisMonth).coerceAtLeast(0)

  val hasReachedLimit: Boolean
    get() = swipesUsedThisMonth >= currentTier.monthlySwipes

  val usagePercentage: Float
    get() = (swipesUsedThisMonth.toFloat() / currentTier.monthlySwipes.toFloat()).coerceIn(0f, 1f)
}
