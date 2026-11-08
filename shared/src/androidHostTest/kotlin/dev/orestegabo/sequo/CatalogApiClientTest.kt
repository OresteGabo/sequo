package dev.orestegabo.sequo

import dev.orestegabo.sequo.core.catalog.CatalogApiClient
import dev.orestegabo.sequo.core.catalog.searchProducts
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.*

class CatalogApiClientTest {
    private val product = """{"id":"product-new","merchantId":"merchant-new","name":"Gourde publiée","detail":"Article standard","priceCfa":2200}"""
    private fun response(extra: String = "", merchantProducts: String = product, globalProducts: String = product) = """{
      "merchants":[{"id":"merchant-new","name":"Vraie boutique","status":"ACTIVE"$extra,"products":[$merchantProducts]}],
      "products":[$globalProducts]
    }"""
    private suspend fun <T> withCatalog(payload: String, test: suspend (CatalogApiClient) -> T): T {
        val http = HttpClient(MockEngine { request ->
            assertEquals("/api/catalog/home", request.url.encodedPath)
            respond(payload, HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
        }) { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }
        return try { test(CatalogApiClient(http)) } finally { http.close() }
    }

    @Test fun merchantWithoutOptionalStorefrontRemainsVisibleWithoutInventedPublicClaims() = runBlocking {
        withCatalog(response()) { client ->
            val actual = client.home()
            val shop = actual.shops.single()
            assertEquals("Vraie boutique", shop.name)
            assertNull(shop.area); assertNull(shop.kind); assertNull(shop.distanceKm); assertNull(shop.eta)
            assertNull(shop.rating); assertNull(shop.openStatus); assertNull(shop.photoStatus); assertNull(shop.consolidation)
            val found = actual.searchProducts("gourde vraie boutique").single()
            assertSame(shop, found.shop)
            assertEquals("product-new", found.product.id)
            assertEquals("merchant-new", found.product.merchantId)
            assertEquals(2200, found.product.priceCfa)
            assertEquals("product-new", actual.featuredProductsFor("").single().second.id)
        }
    }

    @Test fun knownMetadataIncludingRealZeroDistanceIsPreservedAndSortsBeforeUnknownDistance() = runBlocking {
        val knownProduct = product.replace("product-new", "product-known").replace("merchant-new", "merchant-known")
        val payload = """{"merchants":[
          {"id":"merchant-new","name":"Vraie boutique","status":"ACTIVE","products":[$product]},
          {"id":"merchant-known","name":"Boutique connue","status":"ACTIVE","area":"Tokoin","kind":"Commerce","distanceKm":0.0,"eta":"35–45 min","photoStatus":"Photos validées","openStatus":"Open","rating":"4.8","consolidation":"SEQUO","products":[$knownProduct]}
        ],"products":[$product,$knownProduct]}"""
        withCatalog(payload) { client ->
            val actual = client.home()
            val known = actual.shops.last()
            assertEquals("Tokoin", known.area); assertEquals("Commerce", known.kind)
            assertEquals(0.0, known.distanceKm); assertEquals("35–45 min", known.eta)
            assertEquals("4.8", known.rating); assertEquals("Photos validées", known.photoStatus)
            assertEquals("Open", known.openStatus); assertEquals("SEQUO", known.consolidation)
            assertEquals(listOf("product-known", "product-new"), actual.featuredProductsFor("").map { it.second.id })
        }
    }

    @Test fun blankOrInvalidMetadataDoesNotBecomeAnAvailableNearbyShopOrCreateAFallbackMerchant() = runBlocking {
        withCatalog(response(",\"area\":\"\",\"kind\":\" \",\"distanceKm\":-1.0,\"eta\":\"\",\"rating\":\"\",\"openStatus\":\"\",\"photoStatus\":\"\",\"consolidation\":\"\"")) { client ->
            val shop = client.home().shops.single()
            assertNull(shop.area); assertNull(shop.kind); assertNull(shop.distanceKm); assertNull(shop.eta)
            assertNull(shop.rating); assertNull(shop.openStatus); assertNull(shop.photoStatus); assertNull(shop.consolidation)
        }
        withCatalog("""{"merchants":[],"products":[$product]}""") { client ->
            val actual = client.home()
            assertTrue(actual.shops.isEmpty()); assertNull(actual.products.single().shop)
            assertTrue(actual.searchProducts("gourde").isEmpty())
        }
    }
}
