package dev.orestegabo.sequo.core.bargaining

import dev.orestegabo.sequo.model.SequoProduct

internal data class BargainingUiState(
    val bargainingAvailable: Boolean,
    val attemptsRemaining: Int,
    val historicalMinimumAcceptedCfa: Int? = null,
    val acceptedLockedPriceCfa: Int? = null,
    val lockedPriceExpiresLabel: String? = null,
    val lastSubmittedOfferCfa: Int? = null,
    val localOnly: Boolean = true,
)

internal sealed interface BargainingSubmitResult {
    data class Accepted(val state: BargainingUiState) : BargainingSubmitResult
    data object Disabled : BargainingSubmitResult
    data object AttemptsExhausted : BargainingSubmitResult
    data object InvalidAmount : BargainingSubmitResult
}

internal interface BargainingRepository {
    fun stateFor(product: SequoProduct): BargainingUiState
    fun submitCustomerOffer(product: SequoProduct, amountCfa: Int, currentState: BargainingUiState): BargainingSubmitResult
}

internal class LocalBargainingRepository : BargainingRepository {
    override fun stateFor(product: SequoProduct): BargainingUiState =
        BargainingUiState(
            bargainingAvailable = product.isNegotiable,
            attemptsRemaining = if (product.isNegotiable) 3 else 0,
            historicalMinimumAcceptedCfa = null,
        )

    override fun submitCustomerOffer(
        product: SequoProduct,
        amountCfa: Int,
        currentState: BargainingUiState,
    ): BargainingSubmitResult {
        if (!product.isNegotiable || !currentState.bargainingAvailable) return BargainingSubmitResult.Disabled
        if (currentState.attemptsRemaining <= 0) return BargainingSubmitResult.AttemptsExhausted
        if (amountCfa <= 0 || amountCfa > product.priceCfa) return BargainingSubmitResult.InvalidAmount

        val attemptsLeft = (currentState.attemptsRemaining - 1).coerceAtLeast(0)
        return BargainingSubmitResult.Accepted(
            currentState.copy(
                attemptsRemaining = attemptsLeft,
                lastSubmittedOfferCfa = amountCfa,
                acceptedLockedPriceCfa = amountCfa,
                lockedPriceExpiresLabel = "24h",
            )
        )
    }
}
