package dev.orestegabo.sequo

import dev.orestegabo.sequo.core.notification.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class CustomerInboxClientTest {
    private val base = "http://127.0.0.1:55840"
    private val account = "customer-a"
    private fun profile(id: String = account) = """{"id":"$id","active":true,"status":"ACTIVE"}"""
    private fun row(id: String = "notification-a", owner: String = account, family: String = "SEQUO_CUSTOMER", read: Boolean = false, archived: Boolean = false) = """{
      "id":"$id","recipientUserId":"$owner","appFamily":"$family","eventType":"ORDER_CREATED","severity":"INFO",
      "title":"Saved fixture order","body":"The server recorded the order status.","createdAt":"2026-11-08T10:00:00Z",
      "readAt":${if (read) "\"2026-11-08T10:01:00Z\"" else "null"},"archivedAt":${if (archived) "\"2026-11-08T10:02:00Z\"" else "null"},
      "payload":"not displayed","deliveries":[{"targetRef":"not deserialized"}]
    }"""
    private fun http(engine: MockEngine) = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
    @Test fun inboxUsesVerifiedOwnBearerAndOnlyCustomerFamilyRecords() = runBlocking<Unit> {
        val paths = mutableListOf<String>()
        val client = CustomerInboxClient(account, { "own-token" }, { account }, http(MockEngine { request ->
            paths += request.url.encodedPath
            assertEquals("Bearer own-token", request.headers[HttpHeaders.Authorization])
            if (request.url.encodedPath.endsWith("/me")) respond(profile(), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            else {
                assertEquals("true", request.url.parameters["includeArchived"]); assertEquals("100", request.url.parameters["limit"])
                respond("[${row()},${row("merchant-message", family = "SEQUO_MERCHANT")}]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }), base)
        try {
            val inbox = client.inbox()
            assertEquals(listOf("notification-a"), inbox.map { it.id }); assertEquals(1, inbox.count { it.readAt == null })
            assertEquals(listOf("/api/auth/me", "/api/notifications/inbox"), paths)
        } finally { client.close() }
    }
    @Test fun readArchiveAndRestoreUseExistingRoutesAndCanonicalReadback() = runBlocking<Unit> {
        var read = false; var archived = false; val writes = mutableListOf<Pair<HttpMethod, String>>()
        val client = CustomerInboxClient(account, { "token" }, { account }, http(MockEngine { request ->
            val body = if (request.url.encodedPath.endsWith("/me")) profile() else {
                if (request.method != HttpMethod.Get) {
                    writes += request.method to request.url.encodedPath
                    if (request.url.encodedPath.endsWith("/read")) read = true
                    else { archived = request.method == HttpMethod.Post; if (archived) read = true }
                }
                val message = row(read = read, archived = archived)
                if (request.method == HttpMethod.Get) "[$message]" else message
            }
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }), base)
        try {
            client.change("notification-a", CustomerInboxAction.Read)
            assertNotNull(client.inbox().single().readAt)
            client.change("notification-a", CustomerInboxAction.Archive)
            assertNotNull(client.inbox().single().archivedAt)
            client.change("notification-a", CustomerInboxAction.Restore)
            assertNull(client.inbox().single().archivedAt)
            assertEquals(listOf(HttpMethod.Patch, HttpMethod.Post, HttpMethod.Delete), writes.map { it.first })
            assertEquals(listOf("/api/notifications/inbox/notification-a/read", "/api/notifications/inbox/notification-a/archive", "/api/notifications/inbox/notification-a/archive"), writes.map { it.second })
        } finally { client.close() }
    }
    @Test fun changedTokenOwnerStopsBeforeReadingOrMutatingAnyInbox() = runBlocking<Unit> {
        var calls = 0
        val client = CustomerInboxClient(account, { "next-accounts-token" }, { account }, http(MockEngine { request ->
            calls++; assertEquals("/api/auth/me", request.url.encodedPath)
            respond(profile("customer-b"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }), base)
        try {
            assertFailsWith<CustomerInboxSessionUnavailable> { client.inbox() }
            assertFailsWith<CustomerInboxSessionUnavailable> { client.change("notification-a", CustomerInboxAction.Archive) }
            assertEquals(2, calls)
        } finally { client.close() }
    }
    @Test fun anotherRecipientsOrWrongActionRecordIsNeverAccepted() = runBlocking<Unit> {
        var foreignOwner = true
        val client = CustomerInboxClient(account, { "token" }, { account }, http(MockEngine { request ->
            val body = if (request.url.encodedPath.endsWith("/me")) profile() else if (request.method == HttpMethod.Get)
                "[${row(owner = if (foreignOwner) "customer-b" else account)}]" else row("other-record")
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }), base)
        try {
            assertFailsWith<IllegalArgumentException> { client.inbox() }; foreignOwner = false
            assertFailsWith<IllegalArgumentException> { client.change("notification-a", CustomerInboxAction.Read) }
        } finally { client.close() }
    }
    @Test fun missingExpiredAndDeniedSessionsNeverBecomeAnEmptySuccess() = runBlocking<Unit> {
        var calls = 0
        val missing = CustomerInboxClient(account, { null }, { account }, http(MockEngine { calls++; respond("[]") }), base)
        try { assertFailsWith<CustomerInboxSessionUnavailable> { missing.inbox() }; assertEquals(0, calls) } finally { missing.close() }
        for (status in listOf(HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden, HttpStatusCode.ServiceUnavailable)) {
            val failed = CustomerInboxClient(account, { "token" }, { account }, http(MockEngine { respond("{}", status, headersOf(HttpHeaders.ContentType, "application/json")) }), base)
            try { assertFailsWith<ResponseException> { failed.inbox() } } finally { failed.close() }
        }
    }
    @Test fun leavingAccountDuringTokenReadCancelsBeforeAnyNetworkRequest() = runBlocking<Unit> {
        val started = CompletableDeferred<Unit>(); val token = CompletableDeferred<String>(); var calls = 0
        val client = CustomerInboxClient(account, { started.complete(Unit); token.await() }, { account }, http(MockEngine { calls++; respond("[]") }), base)
        val pending = async { client.inbox() }; started.await(); client.close(); token.complete("old-token")
        assertFailsWith<CancellationException> { pending.await() }; assertEquals(0, calls)
    }
    @Test fun emptyInboxAndBadIdentifiersRemainDistinctFromFailure() = runBlocking<Unit> {
        var calls = 0
        val client = CustomerInboxClient(account, { "token" }, { account }, http(MockEngine { request ->
            calls++; respond(if (request.url.encodedPath.endsWith("/me")) profile() else "[]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }), base)
        try {
            assertTrue(client.inbox().isEmpty()); assertEquals(2, calls)
            for (id in listOf("..", ".", "../someone", "x?next=other")) assertFailsWith<IllegalArgumentException> { client.change(id, CustomerInboxAction.Read) }
            assertEquals(2, calls)
        } finally { client.close() }
    }
}
