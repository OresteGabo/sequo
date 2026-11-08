package dev.orestegabo.sequo.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.network.SequoApiException
import dev.orestegabo.sequo.core.notification.*
import dev.orestegabo.sequo.feature.settings.LocalAppLanguage
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun LiveCustomerNotificationsContent(
    accountId: String, accessToken: suspend () -> String?,
    onUnreadCountChanged: (Int) -> Unit, onSignInRequested: () -> Unit, onOpenOrders: () -> Unit,
) {
    val latestAccount by rememberUpdatedState(accountId)
    val latestToken by rememberUpdatedState(accessToken)
    val client = remember(accountId) { CustomerInboxClient(accountId, { latestToken() }, { latestAccount }) }
    var rows by remember(accountId) { mutableStateOf<List<CustomerInboxMessage>>(emptyList()) }
    var loading by remember(accountId) { mutableStateOf(false) }
    var failed by remember(accountId) { mutableStateOf(false) }
    var expired by remember(accountId) { mutableStateOf(false) }
    var archived by remember(accountId) { mutableStateOf(false) }
    var active by remember(accountId) { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val text = customerInboxText(LocalAppLanguage.current)
    DisposableEffect(client) { onDispose { active = false; client.close() } }
    suspend fun read() {
        val actual = client.inbox()
        if (active && latestAccount == accountId) {
            rows = actual
            onUnreadCountChanged(actual.count { it.readAt == null && it.archivedAt == null })
        }
    }
    fun run(action: CustomerInboxAction? = null, id: String? = null) {
        if (loading || !active) return
        loading = true; failed = false; expired = false
        scope.launch {
            try {
                if (action != null && id != null) client.change(id, action)
                read()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                if (active) {
                    rows = emptyList(); failed = true
                    expired = failure is CustomerInboxSessionUnavailable ||
                        ((failure as? SequoApiException)?.statusCode ?: (failure as? ResponseException)?.response?.status?.value) in setOf(401, 403)
                    onUnreadCountChanged(0)
                }
            } finally { if (active) loading = false }
        }
    }
    LaunchedEffect(client) { run() }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = .64f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text(text.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(text.saved, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!loading && !failed) Text("${rows.count { it.readAt == null && it.archivedAt == null }} ${text.unread}", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(enabled = !loading, onClick = { run() }) { Text(text.refresh) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !archived, onClick = { archived = false }, label = { Text(text.active) })
            FilterChip(selected = archived, onClick = { archived = true }, label = { Text(text.archived) })
        }
        if (loading) Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(20.dp)); Text(text.loading) }
        if (failed) {
            Text(if (expired) text.sessionError else text.error, color = MaterialTheme.colorScheme.error)
            if (expired) OutlinedButton(onClick = onSignInRequested) { Text(text.signIn) }
        } else if (!loading && rows.none { (it.archivedAt != null) == archived }) {
            Text(text.empty, style = MaterialTheme.typography.titleMedium)
            Text(text.emptyDetail, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        rows.filter { (it.archivedAt != null) == archived }.forEach { message ->
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(message.title, style = MaterialTheme.typography.titleSmall, fontWeight = if (message.readAt == null) FontWeight.Bold else FontWeight.Medium)
                    Text(message.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(message.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!archived && message.readAt == null) TextButton(enabled = !loading, onClick = { run(CustomerInboxAction.Read, message.id) }) { Text(text.markRead) }
                        TextButton(enabled = !loading, onClick = { run(if (archived) CustomerInboxAction.Restore else CustomerInboxAction.Archive, message.id) }) { Text(if (archived) text.restore else text.archive) }
                    }
                }
            }
        }
        TextButton(onClick = onOpenOrders) { Text(text.orders) }
    }
}
