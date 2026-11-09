package dev.orestegabo.sequo

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import dev.orestegabo.sequo.core.auth.AuthApiClient
import dev.orestegabo.sequo.core.network.createSequoHttpClient
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.*

class DemoAuthClientTest {
    @Test fun ordinaryBuildsCannotRequestDemoAccess() = runBlocking {
        for(environment in listOf("PRODUCTION","STAGING","LOCAL_FIXTURE")) {
            val client=AuthApiClient("http://127.0.0.1:1",createSequoHttpClient("http://127.0.0.1:1"),{"test-device"},environment)
            assertFailsWith<IllegalStateException> {client.loginWithDemo("never-send-this")}
        }
    }
    @Test fun demoLoginUsesPrivateCodeAndNormalDeviceBoundSession() = runBlocking {
        val server=HttpServer.create(InetSocketAddress("127.0.0.1",0),0)
        var payload:JsonObject?=null
        server.createContext("/api/auth/demo/session") { x ->
            payload=Json.parseToJsonElement(x.requestBody.readBytes().decodeToString()).jsonObject
            val response="""{"accessToken":"test-access","refreshToken":"test-refresh","expiresIn":3600}""".toByteArray()
            x.responseHeaders.set("Content-Type","application/json");x.sendResponseHeaders(200,response.size.toLong());x.responseBody.use{it.write(response)}
        }
        server.start()
        val http=createSequoHttpClient("http://127.0.0.1:${server.address.port}")
        try {
            val client=AuthApiClient("http://127.0.0.1:${server.address.port}",http,{"test-device"},"DEMO")
            val session=client.loginWithDemo(" private-demo-test ")
            assertEquals("test-access",session.accessToken)
            assertEquals("private-demo-test",payload!!["accessCode"]!!.jsonPrimitive.content)
            assertEquals("test-device",payload!!["deviceId"]!!.jsonPrimitive.content)
            assertEquals(setOf("accessCode","deviceId"),payload!!.keys)
        } finally {http.close();server.stop(0)}
    }
}
