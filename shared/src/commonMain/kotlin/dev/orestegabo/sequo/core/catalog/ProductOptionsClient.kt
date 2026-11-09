package dev.orestegabo.sequo.core.catalog

import dev.orestegabo.sequo.core.network.createSequoHttpClient
import dev.orestegabo.sequo.model.ProductOptionSelection
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

internal class ProductOptionsClient(private val client: HttpClient = createSequoHttpClient()) {
    suspend fun quote(productId: String, selections: List<ProductOptionSelection>): ProductOptionsQuote =
        client.post("/api/catalog/products/$productId/customizations/quote") { setBody(ProductOptionsRequest(selections)) }.body()
    fun close() = client.close()
}

@Serializable
private data class ProductOptionsRequest(val selections: List<ProductOptionSelection>)
@Serializable
internal data class ProductOptionsQuote(val productId: String, val basePriceCfa: Int, val optionPriceCfa: Int, val unitPriceCfa: Int,
    val measurementAmount: String? = null, val measurementUnit: String? = null)
