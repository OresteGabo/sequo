package dev.orestegabo.sequo.core.notification

import dev.orestegabo.sequo.core.network.NetworkConfig
import dev.orestegabo.sequo.core.network.createSequoHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable

@Serializable
internal data class CustomerInboxMessage(
    val id: String, val recipientUserId: String, val appFamily: String,
    val eventType: String, val severity: String, val title: String, val body: String,
    val createdAt: String, val readAt: String? = null, val archivedAt: String? = null,
)
@Serializable private data class InboxAccount(val id: String, val active: Boolean = true, val status: String)
internal class CustomerInboxSessionUnavailable : Exception("Inbox authentication is unavailable")
internal enum class CustomerInboxAction { Read, Archive, Restore }

/** Every request belongs to one verified account; no saved demo rows or provider send path. */
internal class CustomerInboxClient(
    private val accountId: String,
    private val accessToken: suspend () -> String?,
    private val currentAccountId: () -> String?,
    private val client: HttpClient = createSequoHttpClient(),
    private val baseUrl: String = NetworkConfig.BaseUrl,
) {
    private var closed = false
    private fun url(path: String) = "${baseUrl.trimEnd('/')}/api/$path"
    private fun current() { if (closed || currentAccountId() != accountId) throw CancellationException("Notification account changed") }
    private suspend fun verifiedToken(): String {
        current()
        val token = accessToken()?.takeIf { it.isNotBlank() } ?: throw CustomerInboxSessionUnavailable()
        current()
        // The saved token provider can rotate independently of a composed screen. Check its actual owner.
        val actual = client.get(url("auth/me")) { bearerAuth(token) }.body<InboxAccount>()
        current()
        if (actual.id != accountId || !actual.active || !actual.status.equals("ACTIVE", true)) throw CustomerInboxSessionUnavailable()
        return token
    }
    suspend fun inbox(): List<CustomerInboxMessage> {
        val token = verifiedToken()
        val rows = client.get(url("notifications/inbox")) {
            bearerAuth(token); parameter("includeArchived", true); parameter("limit", 100)
        }.body<List<CustomerInboxMessage>>()
        current()
        require(rows.size <= 100 && rows.map { it.id }.distinct().size == rows.size)
        rows.forEach(::validate)
        return rows.filter { it.appFamily == "SEQUO_CUSTOMER" }
    }
    suspend fun change(id: String, action: CustomerInboxAction): CustomerInboxMessage {
        require(validId(id)) { "Invalid notification identifier" }
        val token = verifiedToken()
        val path = url("notifications/inbox/$id/" + if (action == CustomerInboxAction.Read) "read" else "archive")
        val response = when (action) {
            CustomerInboxAction.Read -> client.patch(path) { bearerAuth(token) }
            CustomerInboxAction.Archive -> client.post(path) { bearerAuth(token) }
            CustomerInboxAction.Restore -> client.delete(path) { bearerAuth(token) }
        }.body<CustomerInboxMessage>()
        current(); validate(response)
        require(response.id == id && response.appFamily == "SEQUO_CUSTOMER") { "Unexpected notification response" }
        return response
    }
    private fun validate(message: CustomerInboxMessage) {
        require(validId(message.id) && message.recipientUserId == accountId && message.title.length <= 1000 &&
            message.body.length <= 50000 && message.createdAt.isNotBlank()) { "Invalid inbox response" }
    }
    fun close() { closed = true; client.close() }
    private fun validId(id: String) = id.matches(Regex("[A-Za-z0-9][A-Za-z0-9._:-]{0,127}")) && id != "." && id != ".."
}
