package dev.orestegabo.sequo.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeliveryPricingTest {

    @Test
    fun keepsMinimumFeeAtFiveKilometersOrLess() {
        val pricing = calculateDeliveryPricing(DeliveryPricingInput(distanceKm = 5.0))

        assertEquals(5, pricing.billableKm)
        assertEquals(400, pricing.baseFeeCfa)
        assertEquals(400, pricing.finalDeliveryFeeCfa)
    }

    @Test
    fun roundsUpAndChargesExtraKilometersAfterFiveKilometers() {
        val pricing = calculateDeliveryPricing(DeliveryPricingInput(distanceKm = 5.1))

        assertEquals(6, pricing.billableKm)
        assertEquals(500, pricing.baseFeeCfa)
    }

    @Test
    fun appliesSubscriptionDiscountBeforeReferralCredit() {
        val pricing = calculateDeliveryPricing(
            DeliveryPricingInput(
                distanceKm = 8.4,
                subscriptionDiscountPercent = 15,
                referralCreditCfa = 500,
            )
        )

        assertEquals(9, pricing.billableKm)
        assertEquals(800, pricing.baseFeeCfa)
        assertEquals(120, pricing.subscriptionDiscountCfa)
        assertEquals(500, pricing.referralCreditAppliedCfa)
        assertEquals(180, pricing.finalDeliveryFeeCfa)
    }

    @Test
    fun capsReferralCreditAtRemainingDeliveryFee() {
        val pricing = calculateDeliveryPricing(
            DeliveryPricingInput(distanceKm = 2.0, referralCreditCfa = 500)
        )

        assertEquals(400, pricing.referralCreditAppliedCfa)
        assertEquals(0, pricing.finalDeliveryFeeCfa)
    }

    @Test
    fun rejectsInvalidInputs() {
        assertFailsWith<IllegalArgumentException> {
            calculateDeliveryPricing(DeliveryPricingInput(distanceKm = -1.0))
        }
        assertFailsWith<IllegalArgumentException> {
            calculateDeliveryPricing(
                DeliveryPricingInput(distanceKm = 1.0, subscriptionDiscountPercent = 101)
            )
        }
        assertFailsWith<IllegalArgumentException> {
            calculateDeliveryPricing(
                DeliveryPricingInput(distanceKm = 1.0, referralCreditCfa = -1)
            )
        }
    }
}
