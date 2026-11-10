package dev.orestegabo.sequo.core.account

import dev.orestegabo.sequo.core.network.createSequoHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.serialization.Serializable

internal class AccountDataClient(private val client: HttpClient = createSequoHttpClient()) {
    suspend fun archive(token: String, preferences: Map<String, String>): ByteArray =
        client.preparePost("/api/account/data-export") {
            bearerAuth(token)
            accept(ContentType.Application.Zip)
            setBody(ExportRequest(preferences))
            timeout { requestTimeoutMillis = 120_000; socketTimeoutMillis = 120_000 }
        }.execute { response ->
            check(response.contentType()?.withoutParameters() == ContentType.Application.Zip)
            val channel = response.bodyAsChannel()
            val chunks = mutableListOf<ByteArray>()
            var total = 0
            val buffer = ByteArray(8192)
            while (!channel.isClosedForRead) {
                val count = channel.readAvailable(buffer)
                if (count < 0) break
                if (count == 0) continue
                total += count
                check(total <= 50 * 1024 * 1024) { "Export exceeds the device download limit." }
                chunks += buffer.copyOf(count)
            }
            ByteArray(total).also { result ->
                var offset = 0
                chunks.forEach { it.copyInto(result, offset); offset += it.size }
                check(result.size >= 4 && result[0] == 0x50.toByte() && result[1] == 0x4b.toByte())
            }
        }

    suspend fun deletions(token: String): List<AccountDeletionStatus> = client.get("/api/account/deletion-requests") { bearerAuth(token) }.body()
    suspend fun requestDeletion(token: String): AccountDeletionStatus = client.post("/api/account/deletion-requests") {
        bearerAuth(token)
        setBody(DeletionRequest("DELETE_MY_ACCOUNT"))
    }.body()
    fun close() = client.close()
}

@Serializable private data class ExportRequest(val devicePreferences: Map<String, String>)
@Serializable private data class DeletionRequest(val confirmation: String)
@Serializable internal data class AccountDeletionStatus(val id: String, val status: String, val requestedAt: String? = null)
