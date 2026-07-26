package dev.orestegabo.sequo.domain

import kotlin.math.ceil
import kotlin.math.max

data class DeliveryPricingInput(
    val distanceKm: Double,
    val subscriptionDiscountPercent: Int = 0,
    val referralCreditCfa: Int = 0,
)

data class DeliveryPricingBreakdown(
    val billableKm: Int,
    val baseFeeCfa: Int,
    val subscriptionDiscountCfa: Int,
    val referralCreditAppliedCfa: Int,
    val finalDeliveryFeeCfa: Int,
)

fun calculateDeliveryPricing(input: DeliveryPricingInput): DeliveryPricingBreakdown {
    require(input.distanceKm >= 0.0) { "distanceKm must be non-negative" }
    require(input.subscriptionDiscountPercent in 0..100) {
        "subscriptionDiscountPercent must be between 0 and 100"
    }
    require(input.referralCreditCfa >= 0) { "referralCreditCfa must be non-negative" }

    val billableKm = ceil(input.distanceKm).toInt()
    val baseFeeCfa = if (billableKm <= 5) {
        MINIMUM_DELIVERY_FEE_CFA
    } else {
        MINIMUM_DELIVERY_FEE_CFA + ((billableKm - 5) * EXTRA_KM_FEE_CFA)
    }
    val subscriptionDiscountCfa = (baseFeeCfa * input.subscriptionDiscountPercent) / 100
    val feeAfterSubscription = baseFeeCfa - subscriptionDiscountCfa
    val referralCreditAppliedCfa = input.referralCreditCfa.coerceAtMost(feeAfterSubscription)

    return DeliveryPricingBreakdown(
        billableKm = billableKm,
        baseFeeCfa = baseFeeCfa,
        subscriptionDiscountCfa = subscriptionDiscountCfa,
        referralCreditAppliedCfa = referralCreditAppliedCfa,
        finalDeliveryFeeCfa = max(feeAfterSubscription - referralCreditAppliedCfa, 0),
    )
}

private const val MINIMUM_DELIVERY_FEE_CFA = 400
private const val EXTRA_KM_FEE_CFA = 100
