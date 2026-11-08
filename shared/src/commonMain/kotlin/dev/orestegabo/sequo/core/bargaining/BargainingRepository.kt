package dev.orestegabo.sequo.core.bargaining

import dev.orestegabo.sequo.core.network.createSequoHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

internal interface BargainingRepository {
    suspend fun threads(productId: String): List<BargainingThread>
    suspend fun offer(productId: String, threadId: String?, amountCfa: Int, idempotencyKey: String): BargainingThread
    suspend fun respond(threadId: String, offerId: String, accept: Boolean, idempotencyKey: String): BargainingThread
}

internal class NetworkBargainingRepository(
    private val tokenProvider: suspend () -> String?,
    private val client: HttpClient = createSequoHttpClient(),
) : BargainingRepository {
    private suspend fun token(): String = checkNotNull(tokenProvider())

    override suspend fun threads(productId: String): List<BargainingThread> =
        client.get("/api/bargaining/threads") { bearerAuth(token()) }.body<List<BargainingThread>>()
            .filter { it.productId == productId }

    override suspend fun offer(productId: String, threadId: String?, amountCfa: Int, idempotencyKey: String): BargainingThread =
        if (threadId == null) client.post("/api/bargaining/threads") {
            bearerAuth(token())
            setBody(CreateThreadRequest(productId, amountCfa, idempotencyKey))
        }.body() else client.post("/api/bargaining/threads/$threadId/offers") {
            bearerAuth(token())
            setBody(OfferRequest("OFFER", amountCfa = amountCfa, idempotencyKey = idempotencyKey))
        }.body()

    override suspend fun respond(threadId: String, offerId: String, accept: Boolean, idempotencyKey: String): BargainingThread =
        client.post("/api/bargaining/threads/$threadId/offers") {
            bearerAuth(token())
            setBody(OfferRequest(if (accept) "ACCEPT" else "REJECT", expectedOfferId = offerId, idempotencyKey = idempotencyKey))
        }.body()

    fun close() = client.close()
}

@Serializable
private data class CreateThreadRequest(val productId: String, val amountCfa: Int, val idempotencyKey: String)
@Serializable
private data class OfferRequest(val offerType: String, val amountCfa: Int? = null, val expectedOfferId: String? = null, val idempotencyKey: String)

@Serializable
internal data class BargainingThread(
    val id: String,
    val productId: String,
    val status: String,
    val acceptedPriceCfa: Int? = null,
    val catalogPriceCfa: Int? = null,
    val expiresAt: String? = null,
    val bargainingEnabled: Boolean = true,
    val customerAttemptsUsed: Int = 0,
    val maxCustomerAttempts: Int = 3,
    val nextActor: String? = null,
    val offers: List<BargainingOffer> = emptyList(),
) {
    val attemptsRemaining: Int get() = (maxCustomerAttempts - customerAttemptsUsed).coerceAtLeast(0)
    val accepted: Boolean get() = status == "ACCEPTED" && acceptedPriceCfa != null
    val pendingMerchantOffer: BargainingOffer? get() = offers.lastOrNull { it.offerType != "MESSAGE" }
        ?.takeIf { status == "OPEN" && nextActor == "CUSTOMER" && it.actorType == "MERCHANT" && it.offerType == "COUNTER_OFFER" }
}

@Serializable
internal data class BargainingOffer(val id: String, val actorType: String, val offerType: String, val amountCfa: Int? = null, val message: String? = null)
