package dev.orestegabo.sequo

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import dev.orestegabo.sequo.core.catalog.*
import dev.orestegabo.sequo.core.network.createSequoHttpClient
import dev.orestegabo.sequo.model.ProductOptionSelection
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.*

class CartQuantityClientTest {
    private fun line(id: String, quantity: Int, option: String) = CartLine(id, CartProduct("same-product", "Shirt"), quantity,
        listOf(ProductOptionSelection("size", setOf(option))), 1000 * quantity, true)

    @Test fun quantitiesResolveByLineIdFromLatestSnapshotWithoutChangingOtherVariants() {
        val first = line("line-small", 1, "S"); val second = line("line-large", 4, "L")
        val before = CartSnapshot(listOf(first, second), 5, 5000)
        assertEquals(2, before.quantityAfter(first.id, 1))
        assertEquals(3, before.quantityAfter(second.id, -1))
        assertEquals(1, before.quantityAfter(first.id, -1))
        val refreshed = before.copy(items = listOf(second, first.copy(quantity = 2)))
        assertEquals(3, refreshed.quantityAfter(first.id, 1)) // queued tap sees current quantity, even if response order changes
        assertNull(refreshed.quantityAfter("removed-line", 1))
        assertEquals(99, before.copy(items = listOf(first.copy(quantity = 99))).quantityAfter(first.id, 1))
        assertFailsWith<IllegalArgumentException> { before.quantityAfter(first.id, 10) }
        assertEquals(4, second.quantity)
    }

    @Test fun updateRequestTargetsExactLineAndSendsOnlyQuantity() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var path: String? = null; var body: JsonObject? = null; var authorization: String? = null
        server.createContext("/api/cart/items/") { exchange ->
            path = exchange.requestURI.path
            authorization = exchange.requestHeaders.getFirst("Authorization")
            body = Json.parseToJsonElement(exchange.requestBody.readBytes().decodeToString()).jsonObject
            val bytes = """{"items":[],"totalItems":0,"subtotalCfa":0}""".toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong()); exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val client = CartApiClient(createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        try {
            client.setQuantity("fixture-token", "line-large", 5)
            assertEquals("/api/cart/items/line-large/quantity", path)
            assertEquals("Bearer fixture-token", authorization)
            assertEquals(setOf("quantity"), body!!.keys)
            assertEquals(5, body!!["quantity"]!!.jsonPrimitive.int)
        } finally { client.close(); server.stop(0) }
    }
}
