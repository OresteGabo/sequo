package dev.orestegabo.sequo

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import dev.orestegabo.sequo.core.catalog.*
import dev.orestegabo.sequo.core.platform.deliveryPin
import dev.orestegabo.sequo.core.network.createSequoHttpClient
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.*

class DeliveryAddressClientTest {
    @Test fun explicitMapPinsAreParsedWithoutGuessingFromAddressesOrViewport() {
        val point = assertNotNull(deliveryPin("https://maps.apple.com/?ll=6.178,1.237"))
        assertEquals(6.178, point.latitude); assertEquals(1.237, point.longitude)
        assertEquals(point, deliveryPin("https://www.google.com/maps/search/?api=1&query=6.178%2C1.237"))
        assertEquals(point, deliveryPin("https://www.openstreetmap.org/?mlat=6.178&mlon=1.237"))
        for (link in listOf("https://evil.invalid/?ll=6.178,1.237", "https://maps.apple.com/?ll=100,1", "https://maps.apple.com/?ll=NaN,1", "https://maps.apple.com/?q=Tokoin", "https://maps.app.goo.gl/example", "https://www.google.com/maps/@6,1,12z")) assertNull(deliveryPin(link), link)
    }
    @Test fun createAndEditAddressesSendCoordinatesAndRequiredKindAndRestoreDetails() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val requests = mutableListOf<Pair<String, JsonObject>>()
        server.createContext("/api/customer/addresses") { x ->
            assertEquals("Bearer fixture-token", x.requestHeaders.getFirst("Authorization"))
            requests += x.requestMethod + " " + x.requestURI.path to Json.parseToJsonElement(x.requestBody.readBytes().decodeToString()).jsonObject
            val bytes = """{"id":"address-fixture","label":"[TEST] Home","recipientName":"Test customer","phoneNumber":"+22800000000","city":"Lome","latitude":6.178,"longitude":1.237}""".toByteArray()
            x.responseHeaders.set("Content-Type", "application/json"); x.sendResponseHeaders(200, bytes.size.toLong()); x.responseBody.use { it.write(bytes) }
        }
        server.start()
        val client = CartApiClient(createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        val input = DeliveryAddressInput("[TEST] Home", "Test customer", "+22800000000", "Lome", "Tokoin", "Test only", 6.178, 1.237, "HOME")
        try {
            val created = client.saveAddress("fixture-token", null, input)
            val updated = client.saveAddress("fixture-token", created.id, input.copy(label = "[TEST] Edited"))
            assertEquals("POST /api/customer/addresses", requests[0].first)
            assertEquals("PUT /api/customer/addresses/address-fixture", requests[1].first)
            assertEquals("HOME", requests[0].second["kind"]!!.jsonPrimitive.content)
            assertEquals(6.178, requests[0].second["latitude"]!!.jsonPrimitive.double)
            assertEquals("[TEST] Edited", requests[1].second["label"]!!.jsonPrimitive.content)
            assertEquals("+22800000000", updated.phoneNumber)
            assertEquals(1.237, updated.longitude)
        } finally { client.close(); server.stop(0) }
    }
}
