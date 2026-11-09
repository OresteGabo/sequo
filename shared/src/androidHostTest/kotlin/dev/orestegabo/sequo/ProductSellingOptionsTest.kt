package dev.orestegabo.sequo

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import dev.orestegabo.sequo.core.catalog.*
import dev.orestegabo.sequo.core.network.createSequoHttpClient
import dev.orestegabo.sequo.logic.*
import dev.orestegabo.sequo.model.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.*

class ProductSellingOptionsTest {
    private val size = ProductOptionGroup("size", "Shoe size (EU)", 1, 1,
        listOf(ProductOption("40", "40", 0, true), ProductOption("41", "41", 0, false)), type = "SIZE")
    @Test fun requiredSizesRejectMissingUnknownAndUnavailableChoices() {
        assertFalse(productSelectionsComplete(listOf(size), emptyMap()))
        assertFalse(productSelectionsComplete(listOf(size), mapOf("size" to setOf("41"))))
        assertFalse(productSelectionsComplete(listOf(size), mapOf("size" to setOf("40", "41"))))
        assertFalse(productSelectionsComplete(listOf(size), mapOf("size" to setOf("invented"))))
        assertTrue(productSelectionsComplete(listOf(size), mapOf("size" to setOf("40"))))
    }
    @Test fun legacyFoodAndTypedMeasurementMetadataDecodeWithoutLosingAmount() {
        val json = Json { ignoreUnknownKeys = true }
        val legacy = json.decodeFromString<ProductOptionGroup>("""{"id":"spice","name":"Spice","minChoices":0,"maxChoices":1,"options":[]}""")
        assertEquals("PREPARATION", legacy.type)
        val measured = json.decodeFromString<ProductOptionGroup>("""{"id":"weight","name":"Weight","type":"MEASURE","unit":"KG","minChoices":1,"maxChoices":1,"options":[{"id":"half","name":"Small bag","priceDeltaCfa":0,"active":true,"measureAmount":"0.5"}]}""")
        assertEquals("Small bag (0.5 kg)", measured.options.single().displayLabel(measured))
        assertEquals("0.5 kg", measured.options.single().copy(name = "0.5 kg").displayLabel(measured))
        val product = SequoProduct("beans", name = "Beans", detail = "", priceCfa = 1000, label = "", optionHint = "", customizations = listOf(measured))
        assertEquals(" / kg", product.priceUnitSuffix())
    }
    @Test fun customerUsesTheBackendPriceAndSendsOnlyOptionIds() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var submitted: JsonObject? = null
        server.createContext("/api/catalog/products/beans/customizations/quote") { exchange ->
            submitted = Json.parseToJsonElement(exchange.requestBody.readBytes().decodeToString()).jsonObject
            val bytes = """{"productId":"beans","basePriceCfa":500,"optionPriceCfa":0,"unitPriceCfa":500,"measurementAmount":"0.5","measurementUnit":"KG"}""".toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val client = ProductOptionsClient(createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        try {
            val actual = client.quote("beans", listOf(ProductOptionSelection("weight", setOf("half"))))
            assertEquals(500, actual.unitPriceCfa)
            assertEquals("0.5", actual.measurementAmount)
            assertEquals(setOf("selections"), submitted!!.keys)
            assertEquals("half", submitted!!["selections"]!!.jsonArray.single().jsonObject["optionIds"]!!.jsonArray.single().jsonPrimitive.content)
        } finally { client.close(); server.stop(0) }
    }
}
