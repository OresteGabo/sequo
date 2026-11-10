package dev.orestegabo.sequo

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import dev.orestegabo.sequo.core.account.AccountDataClient
import dev.orestegabo.sequo.core.network.createSequoHttpClient
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.*

class AccountDataClientTest {
    @Test fun downloadsAuthenticatedZipAndUsesTheRealDeletionConfirmation() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var exported: JsonObject? = null
        var confirmed: JsonObject? = null
        val archive = byteArrayOf(0x50, 0x4b, 3, 4, 10)
        server.createContext("/api/account/data-export") { exchange ->
            assertEquals("Bearer fixture-token", exchange.requestHeaders.getFirst("Authorization"))
            exported = Json.parseToJsonElement(exchange.requestBody.readBytes().decodeToString()).jsonObject
            exchange.responseHeaders.set("Content-Type", "application/zip")
            exchange.sendResponseHeaders(200, archive.size.toLong())
            exchange.responseBody.use { it.write(archive) }
        }
        server.createContext("/api/account/deletion-requests") { exchange ->
            assertEquals("Bearer fixture-token", exchange.requestHeaders.getFirst("Authorization"))
            val status = """{"id":"request-fixture","status":"REQUESTED"}"""
            val body = if (exchange.requestMethod == "POST") {
                confirmed = Json.parseToJsonElement(exchange.requestBody.readBytes().decodeToString()).jsonObject
                status
            } else "[$status]"
            val bytes = body.toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong()); exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val client = AccountDataClient(createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        try {
            assertContentEquals(archive, client.archive("fixture-token", mapOf("language" to "French")))
            assertEquals("French", exported!!["devicePreferences"]!!.jsonObject["language"]!!.jsonPrimitive.content)
            assertEquals(setOf("devicePreferences"), exported!!.keys)
            assertEquals("REQUESTED", client.deletions("fixture-token").single().status)
            assertEquals("request-fixture", client.requestDeletion("fixture-token").id)
            assertEquals("DELETE_MY_ACCOUNT", confirmed!!["confirmation"]!!.jsonPrimitive.content)
        } finally { client.close(); server.stop(0) }
    }
}
