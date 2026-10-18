package com.example.data.model

data class PlanFeature(
  val text: String,
  val included: Boolean = true
) {
  companion object {
    /**
     * Parses an arbitrary feature object or raw string into clean text and included boolean.
     * Guaranteed to never output curly braces or map keys.
     */
    fun parse(raw: Any?): PlanFeature {
      if (raw == null) return PlanFeature(text = "", included = true)

      if (raw is Map<*, *>) {
        val textVal = raw["text"] ?: raw["name"] ?: raw["title"] ?: raw["label"] ?: ""
        val cleanText = textVal.toString().trim()
        val incVal = raw["included"] ?: raw["isIncluded"] ?: raw["enabled"] ?: raw["available"]
        val isIncluded = when (incVal) {
          is Boolean -> incVal
          is String -> incVal.equals("true", ignoreCase = true)
          else -> !cleanText.contains("Locked", ignoreCase = true)
        }
        return PlanFeature(text = cleanText, included = isIncluded)
      }

      val str = raw.toString().trim()
      // If string looks like a serialized map "{text=..., included=...}"
      if (str.startsWith("{") && str.endsWith("}")) {
        val inner = str.removeSurrounding("{", "}").trim()
        var extractedText = ""
        var isIncluded = true

        val parts = inner.split(",")
        for (part in parts) {
          val kv = part.split("=")
          if (kv.size == 2) {
            val key = kv[0].trim()
            val value = kv[1].trim()
            if (key.equals("text", ignoreCase = true) || key.equals("name", ignoreCase = true)) {
              extractedText = value
            } else if (key.equals("included", ignoreCase = true) || key.equals("isIncluded", ignoreCase = true)) {
              isIncluded = value.equals("true", ignoreCase = true)
            }
          }
        }

        if (extractedText.isNotBlank()) {
          return PlanFeature(text = extractedText, included = isIncluded)
        }
      }

      // Plain string
      val clean = str.replace("{", "").replace("}", "").trim()
      val isIncluded = !clean.contains("Locked", ignoreCase = true) && !clean.startsWith("No ", ignoreCase = true)
      return PlanFeature(text = clean, included = isIncluded)
    }
  }
}

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
  val featuresList: List<PlanFeature> = emptyList(),
  val isBackendProvided: Boolean = false
) {
  val resolvedFeatures: List<PlanFeature>
    get() {
      if (featuresList.isNotEmpty()) return featuresList
      return perks.map { PlanFeature.parse(it) }
    }

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
      featuresList = tier.perks.map { PlanFeature.parse(it) },
      isBackendProvided = false
    )

    fun defaultPlans(): List<SubscriptionPlan> = listOf(
      fromTier(SubscriptionTier.FREE),
      fromTier(SubscriptionTier.TIER_1),
      fromTier(SubscriptionTier.TIER_2)
    )
  }
}
