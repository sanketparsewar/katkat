package com.example.data.model

enum class SubscriptionTier(
  val title: String,
  val subtitle: String,
  val tagline: String,
  val badge: String,
  val dailySwipes: Int,
  val monthlySwipes: Int,
  val superLikesPerDay: String,
  val rewindsPerDay: String,
  val priceMonthly: String,
  val priceMonthlyAmount: Int,
  val priceYearly: String,
  val priceYearlyAmount: Int,
  val yearlySavings: String,
  val effectiveMonthlyPrice: String,
  val savingsPercent: String,
  val seeWhoLikedYou: Boolean,
  val adFree: Boolean,
  val perks: List<String>
) {
  FREE(
    title = "KatKat Free",
    subtitle = "Explore",
    tagline = "Explore and connect with nearby singles",
    badge = "Free",
    dailySwipes = 50,
    monthlySwipes = 50,
    superLikesPerDay = "1/day",
    rewindsPerDay = "None",
    priceMonthly = "₹0",
    priceMonthlyAmount = 0,
    priceYearly = "₹0",
    priceYearlyAmount = 0,
    yearlySavings = "",
    effectiveMonthlyPrice = "₹0",
    savingsPercent = "",
    seeWhoLikedYou = false,
    adFree = false,
    perks = listOf(
      "50 swipes/day",
      "1 Super Like/day",
      "Basic preferences",
      "Match & Chat",
      "Receive Likes",
      "Likes You → Locked",
      "Standard Ads"
    )
  ),
  TIER_1(
    title = "KatKat Plus",
    subtitle = "Connect",
    tagline = "More matches, less waiting.",
    badge = "Popular",
    dailySwipes = 150,
    monthlySwipes = 150,
    superLikesPerDay = "5/day",
    rewindsPerDay = "5/day",
    priceMonthly = "₹149/mo",
    priceMonthlyAmount = 149,
    priceYearly = "₹999/yr",
    priceYearlyAmount = 999,
    yearlySavings = "Save ₹789 compared with 12 monthly payments",
    effectiveMonthlyPrice = "₹83/month",
    savingsPercent = "SAVE 44%",
    seeWhoLikedYou = true,
    adFree = true,
    perks = listOf(
      "150 swipes/day",
      "5 Super Likes/day",
      "See Who Liked You",
      "5 Rewinds/day",
      "Match & Chat",
      "Ad-free experience"
    )
  ),
  TIER_2(
    title = "KatKat VIP",
    subtitle = "Stand Out",
    tagline = "Get more visibility and control.",
    badge = "Best Value",
    dailySwipes = 300,
    monthlySwipes = 300,
    superLikesPerDay = "10/day",
    rewindsPerDay = "Unlimited",
    priceMonthly = "₹249/mo",
    priceMonthlyAmount = 249,
    priceYearly = "₹1,499/yr",
    priceYearlyAmount = 1499,
    yearlySavings = "Save ₹1,489 compared with 12 monthly payments",
    effectiveMonthlyPrice = "₹125/month",
    savingsPercent = "SAVE 50%",
    seeWhoLikedYou = true,
    adFree = true,
    perks = listOf(
      "300 swipes/day",
      "10 Super Likes/day",
      "See Who Liked You",
      "Unlimited Rewinds",
      "Advanced preferences",
      "Match & Chat",
      "Priority profile placement",
      "VIP badge",
      "Ad-free experience"
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
    get() = (currentTier.dailySwipes - swipesUsedThisMonth).coerceAtLeast(0)

  val hasReachedLimit: Boolean
    get() = swipesUsedThisMonth >= currentTier.dailySwipes

  val usagePercentage: Float
    get() = (swipesUsedThisMonth.toFloat() / currentTier.dailySwipes.toFloat()).coerceIn(0f, 1f)
}
