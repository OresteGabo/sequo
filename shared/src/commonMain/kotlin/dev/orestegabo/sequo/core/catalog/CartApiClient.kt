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

    suspend fun put(token: String, productId: String, quantity: Int, selections: List<ProductOptionSelection>): CartSnapshot =
        client.put("/api/cart/items") {
            bearerAuth(token)
            setBody(CartItemRequest(productId, quantity, selections))
        }.body()

    suspend fun remove(token: String, lineId: String): CartSnapshot =
        client.delete("/api/cart/items/$lineId") { bearerAuth(token) }.body()

    suspend fun quote(token: String, addressId: String, relayPointId: String? = null): CartQuote = client.get("/api/cart/quote") {
        bearerAuth(token)
        parameter("addressId", addressId)
        relayPointId?.let { parameter("relayPointId", it) }
    }.body()

    suspend fun relayPoints(token: String): List<RelayDestination> = client.get("/api/cart/relay-points") { bearerAuth(token) }.body()

    suspend fun paymentProviders(): List<CheckoutProvider> =
        client.get("/api/payments/providers").body()

    suspend fun checkout(token: String, quote: CartQuote, paymentProvider: String): CheckoutSummary =
        client.post("/api/orders/checkouts") {
            bearerAuth(token)
            setBody(CartCheckoutRequest(quote.addressId, quote.revision, paymentProvider, quote.relayPointId))
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

    fun close() = client.close()
}

@Serializable
private data class CartItemRequest(val productId: String, val quantity: Int, val selections: List<ProductOptionSelection>)

@Serializable
internal data class CartSnapshot(val items: List<CartLine>, val totalItems: Int, val subtotalCfa: Int) {
    fun findLine(productId: String, selections: List<ProductOptionSelection>): CartLine? {
        val choices = selections.associate { it.groupId to it.optionIds }
        return items.firstOrNull { line ->
            line.product.id == productId && line.selections.associate { it.groupId to it.optionIds } == choices
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
)


@Serializable
private data class CartCheckoutRequest(val addressId: String, val quoteRevision: String, val paymentProvider: String, val relayPointId: String? = null)

@Serializable
internal data class CheckoutSummary(
    val checkoutId: String,
    val orderId: String? = null,
    val status: String,
    val totalCfa: Int,
    val destinationLabel: String? = null,
    val createdAt: String,
    val lines: List<OrderedProduct>,
)

@Serializable
internal data class CustomerOrderDetails(
    val order: CustomerOrder,
    val lines: List<OrderedProduct>,
    val deliveries: List<CustomerDeliveryTracking> = emptyList(),
)

@Serializable
internal data class CustomerDeliveryTracking(val deliveryCode: String, val status: String, val updatedAt: String,
    val relayDestination: RelayDestination? = null, val relayDepositedAt: String? = null)

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
)

@Serializable
internal data class OrderedProduct(val productName: String, val quantity: Int)


@Serializable
internal data class CheckoutProvider(
    val providerId: String,
    val apiConfigured: Boolean = false,
    val checkoutConnected: Boolean = false,
)

@Serializable
internal data class CustomerRelayPickup(val parcelId: String, val relayPointId: String, val status: String,
    val depositedAt: String? = null, val pickedUpAt: String? = null, val expiresAt: String? = null,
    val credentialState: String, val numericCode: String? = null, val qrModules: List<String> = emptyList())
