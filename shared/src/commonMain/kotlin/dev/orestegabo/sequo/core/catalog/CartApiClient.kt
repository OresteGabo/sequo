package dev.orestegabo.sequo.core.catalog

import dev.orestegabo.sequo.core.network.createSequoHttpClient
import dev.orestegabo.sequo.model.ProductOptionSelection
import dev.orestegabo.sequo.model.ProductOptionGroup
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

internal class CartApiClient(private val client: HttpClient = createSequoHttpClient()) {
    suspend fun cart(token: String): CartSnapshot = client.get("/api/cart") { bearerAuth(token) }.body()

    suspend fun put(token: String, productId: String, quantity: Int, selections: List<ProductOptionSelection>, bargainingThreadId: String? = null): CartSnapshot =
        client.put("/api/cart/items") {
            bearerAuth(token)
            setBody(CartItemRequest(productId, quantity, selections, bargainingThreadId))
        }.body()

    suspend fun remove(token: String, lineId: String): CartSnapshot =
        client.delete("/api/cart/items/$lineId") { bearerAuth(token) }.body()

    suspend fun quote(token: String, addressId: String, relayPointId: String? = null, deliveryMode: String? = null): CartQuote = client.get("/api/cart/quote") {
        bearerAuth(token)
        parameter("addressId", addressId)
        relayPointId?.let { parameter("relayPointId", it) }
        deliveryMode?.let { parameter("deliveryMode", it) }
    }.body()

    suspend fun relayPoints(token: String): List<RelayDestination> = client.get("/api/cart/relay-points") { bearerAuth(token) }.body()

    suspend fun benefits(token: String): CustomerBenefits =
        client.get("/api/subscriptions/benefits") { bearerAuth(token) }.body<CustomerBenefits>().also {
            require(it.status in setOf("ACTIVE", "NO_APPROVED_SUBSCRIPTION", "PENDING_VERIFICATION", "EXPIRED") &&
                it.currency == "XOF" && it.availableReferralCreditCfa >= 0) { "Delivery benefits are unavailable." }
        }

    suspend fun paymentProviders(): List<CheckoutProvider> =
        client.get("/api/payments/providers").body()

    suspend fun checkout(token: String, quote: CartQuote, paymentProvider: String): CheckoutSummary =
        client.post("/api/orders/checkouts") {
            bearerAuth(token)
            setBody(CartCheckoutRequest(quote.addressId, quote.revision, paymentProvider, quote.relayPointId, quote.deliveryOption?.id))
        }.body()

    suspend fun checkouts(token: String): List<CheckoutSummary> =
        client.get("/api/orders/checkouts") { bearerAuth(token) }.body()

    suspend fun orders(token: String): List<CustomerOrderDetails> =
        client.get("/api/orders") { bearerAuth(token) }.body()

    suspend fun relayPickups(token: String, orderId: String): List<CustomerRelayPickup> =
        client.get("/api/orders/$orderId/relay-pickups") { bearerAuth(token) }.body()

    suspend fun renewRelayPickup(token: String, orderId: String, parcelId: String) {
        client.post("/api/orders/$orderId/relay-pickups/$parcelId/credential") { bearerAuth(token) }
    }

    suspend fun returnEligibility(token: String, orderId: String): CustomerReturnEligibility =
        client.get("/api/returns/orders/$orderId/eligibility") { bearerAuth(token) }.body()

    suspend fun returns(token: String, orderId: String): List<CustomerReturnRequest> =
        client.get("/api/returns/orders/$orderId") { bearerAuth(token) }.body()

    suspend fun requestReturn(token: String, orderId: String, returnId: String, reason: String): CustomerReturnRequest =
        client.post("/api/returns") {
            bearerAuth(token)
            setBody(CustomerReturnRequestBody(orderId, returnId, reason))
        }.body()

    suspend fun demoJourney(token: String, checkoutId: String): DemoJourneyState {
        check(dev.orestegabo.sequo.core.network.NetworkConfig.Environment == "DEMO")
        return client.get("/api/demo/checkouts/$checkoutId") { bearerAuth(token) }.body()
    }

    suspend fun advanceDemo(token: String, checkoutId: String, action: String, riderId: String?): DemoJourneyState {
        check(dev.orestegabo.sequo.core.network.NetworkConfig.Environment == "DEMO")
        return client.post("/api/demo/checkouts/$checkoutId/advance") {
            bearerAuth(token); setBody(DemoJourneyStep(action, riderId))
        }.body()
    }

    fun close() = client.close()
}

@Serializable
private data class CartItemRequest(val productId: String, val quantity: Int, val selections: List<ProductOptionSelection>, val bargainingThreadId: String? = null)

@Serializable
internal data class CartSnapshot(val items: List<CartLine>, val totalItems: Int, val subtotalCfa: Int) {
    fun findLine(productId: String, selections: List<ProductOptionSelection>, bargainingThreadId: String? = null): CartLine? {
        val choices = selections.associate { it.groupId to it.optionIds }
        return items.firstOrNull { line ->
            line.product.id == productId && line.bargainingThreadId == bargainingThreadId && line.selections.associate { it.groupId to it.optionIds } == choices
        }
    }
}

@Serializable
internal data class CartLine(
    val id: String,
    val product: CartProduct,
    val quantity: Int,
    val selections: List<ProductOptionSelection>,
    val lineTotalCfa: Int?,
    val available: Boolean,
    val bargainingThreadId: String? = null,
    val negotiatedUnitPriceCfa: Int? = null,
    val negotiatedPriceExpiresAt: String? = null,
    val unavailableReason: String? = null,
)

@Serializable
internal data class CartProduct(
    val id: String,
    val name: String,
    val customizations: List<ProductOptionGroup> = emptyList(),
)

@Serializable
internal data class CartQuote(
    val addressId: String,
    val revision: String,
    val productTotalCfa: Int,
    val optionTotalCfa: Int,
    val serviceFeeCfa: Int,
    val deliveryFeeCfa: Int,
    val totalCfa: Int,
    val relayPointId: String? = null,
    val fulfillmentMode: String = "HOME",
    val deliveryOption: DeliveryModeOffer? = null,
    val availableDeliveryModes: List<DeliveryModeOffer> = emptyList(),
    val deliveryBaseFeeCfa: Int? = null,
    val subscriptionDiscountCfa: Int = 0,
    val loyaltyDiscountCfa: Int = 0,
    val referralCreditAppliedCfa: Int = 0,
)


@Serializable
private data class CartCheckoutRequest(val addressId: String, val quoteRevision: String, val paymentProvider: String, val relayPointId: String? = null, val deliveryMode: String? = null)

@Serializable
internal data class CheckoutSummary(
    val checkoutId: String,
    val orderId: String? = null,
    val status: String,
    val totalCfa: Int,
    val destinationLabel: String? = null,
    val createdAt: String,
    val lines: List<OrderedProduct>,
    val publicReference: String? = null,
    val orderReference: String? = null,
)

@Serializable
internal data class CustomerOrderDetails(
    val order: CustomerOrder,
    val lines: List<OrderedProduct>,
    val deliveries: List<CustomerDeliveryTracking> = emptyList(),
)

@Serializable
internal data class CustomerDeliveryTracking(val deliveryCode: String, val status: String, val updatedAt: String,
    val relayDestination: RelayDestination? = null, val relayDepositedAt: String? = null, val deliveryOption: DeliveryModeOffer? = null,
    val publicReference: String? = null, val orderReference: String? = null)

@Serializable
internal data class RelayDestination(val id: String, val name: String, val city: String? = null,
    val neighborhood: String? = null, val landmark: String? = null)

@Serializable
internal data class CustomerOrder(
    val id: String,
    val checkoutId: String,
    val orderStatus: String,
    val totalCfa: Int,
    val createdAt: String,
    val publicReference: String? = null,
    val checkoutReference: String? = null,
)

@Serializable
internal data class OrderedProduct(val productName: String, val quantity: Int, val optionLabel: String? = null)


@Serializable
internal data class CheckoutProvider(
    val providerId: String,
    val apiConfigured: Boolean = false,
    val checkoutConnected: Boolean = false,
) {
    val paymentAvailable: Boolean get() = apiConfigured && checkoutConnected
}

@Serializable
internal data class CustomerRelayPickup(val parcelId: String, val relayPointId: String, val status: String,
    val depositedAt: String? = null, val pickedUpAt: String? = null, val expiresAt: String? = null,
    val credentialState: String, val numericCode: String? = null, val qrModules: List<String> = emptyList(),
    val publicReference: String? = null, val parcelReference: String? = null)

@Serializable
internal data class DeliveryModeOffer(val id: String, val missionMode: String, val etaMinMinutes: Int,
    val etaMaxMinutes: Int, val zoneId: String, val deliveryFeeCfa: Int)

@Serializable
private data class CustomerReturnRequestBody(val orderId: String, val returnId: String, val reason: String)

@Serializable
internal data class CustomerReturnRequest(val id: String, val orderId: String, val status: String,
    val reason: String, val requestedAt: String, val relayPointId: String? = null,
    val droppedAt: String? = null, val receivedBySequoAt: String? = null,
    val dropoffMethod: String? = null, val instructions: String? = null,
    val relayPoints: List<RelayDestination> = emptyList(), val returnPin: String? = null,
    val collectedBySequoAt: String? = null, val inspectionDecision: String? = null,
    val inspectedAt: String? = null, val refundInstruction: CustomerRefundInstruction? = null,
    val refundDeadlineState: String? = null, val refundDueAt: String? = null,
    val publicReference: String? = null, val orderReference: String? = null)

@Serializable
internal data class CustomerRefundInstruction(val id: String, val amountCfa: Int,
    val currency: String, val requestedAt: String, val status: String, val paymentStatus: String,
    val publicReference: String? = null)

@Serializable
internal data class CustomerReturnEligibility(val orderId: String, val eligible: Boolean,
    val code: String? = null, val message: String? = null, val deliveredAt: String? = null,
    val returnWindowEndsAt: String? = null, val existingReturnId: String? = null,
    val dropoffMethod: String, val instructions: String, val relayPoints: List<RelayDestination> = emptyList(),
    val orderReference: String? = null, val existingReturnReference: String? = null)

@Serializable
internal data class CustomerBenefits(val subscriptionActive: Boolean,
    val subscriptionStatus: String? = null, val planName: String? = null, val monthlyPriceCfa: Int? = null,
    val deliveryDiscountPercent: Int = 0, val loyaltyEnabled: Boolean = false,
    val availableReferralCreditCfa: Int = 0, val currency: String = "XOF", val status: String)
