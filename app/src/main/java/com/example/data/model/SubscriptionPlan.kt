package com.example.data.model

data class SubscriptionPlan(
  val id: String,
  val tier: SubscriptionTier,
  val title: String = tier.title,
  val subtitle: String = tier.subtitle,
  val tagline: String = tier.tagline,
  val badge: String = tier.badge,
  val dailySwipes: Int = tier.dailySwipes,
  val monthlySwipes: Int = tier.monthlySwipes,
  val superLikesPerDay: String = tier.superLikesPerDay,
  val rewindsPerDay: String = tier.rewindsPerDay,
  val priceMonthly: String = tier.priceMonthly,
  val priceMonthlyAmount: Int = tier.priceMonthlyAmount,
  val priceYearly: String = tier.priceYearly,
  val priceYearlyAmount: Int = tier.priceYearlyAmount,
  val yearlySavings: String = tier.yearlySavings,
  val effectiveMonthlyPrice: String = tier.effectiveMonthlyPrice,
  val savingsPercent: String = tier.savingsPercent,
  val seeWhoLikedYou: Boolean = tier.seeWhoLikedYou,
  val adFree: Boolean = tier.adFree,
  val perks: List<String> = tier.perks,
  val isBackendProvided: Boolean = false
) {
  companion object {
    fun fromTier(tier: SubscriptionTier): SubscriptionPlan = SubscriptionPlan(
      id = tier.name,
      tier = tier,
      title = tier.title,
      subtitle = tier.subtitle,
      tagline = tier.tagline,
      badge = tier.badge,
      dailySwipes = tier.dailySwipes,
      monthlySwipes = tier.monthlySwipes,
      superLikesPerDay = tier.superLikesPerDay,
      rewindsPerDay = tier.rewindsPerDay,
      priceMonthly = tier.priceMonthly,
      priceMonthlyAmount = tier.priceMonthlyAmount,
      priceYearly = tier.priceYearly,
      priceYearlyAmount = tier.priceYearlyAmount,
      yearlySavings = tier.yearlySavings,
      effectiveMonthlyPrice = tier.effectiveMonthlyPrice,
      savingsPercent = tier.savingsPercent,
      seeWhoLikedYou = tier.seeWhoLikedYou,
      adFree = tier.adFree,
      perks = tier.perks,
      isBackendProvided = false
    )

    fun defaultPlans(): List<SubscriptionPlan> = listOf(
      fromTier(SubscriptionTier.FREE),
      fromTier(SubscriptionTier.TIER_1),
      fromTier(SubscriptionTier.TIER_2)
    )
  }
}
