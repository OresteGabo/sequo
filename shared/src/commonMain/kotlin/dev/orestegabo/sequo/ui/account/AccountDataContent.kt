package dev.orestegabo.sequo.ui.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.account.*
import dev.orestegabo.sequo.core.network.SequoApiException
import dev.orestegabo.sequo.core.platform.*
import dev.orestegabo.sequo.design.sequoBrandSymbolPainter
import dev.orestegabo.sequo.feature.legal.LegalInitialTab
import dev.orestegabo.sequo.feature.settings.appText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import sequo.shared.generated.resources.*

@Composable
internal fun AboutSequoContent(onOpenLegal: (LegalInitialTab) -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .35f)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Image(sequoBrandSymbolPainter(), contentDescription = null, modifier = Modifier.size(72.dp))
            Text("Sequo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(appText(Res.string.settings_about_tagline), style = MaterialTheme.typography.titleMedium)
            Text(appText(Res.string.settings_version) + " " + appVersionLabel(), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Text(appText(Res.string.settings_about_description), style = MaterialTheme.typography.bodyLarge)
    TextButton(onClick = { onOpenLegal(LegalInitialTab.Privacy) }) { Text(appText(Res.string.settings_privacy)) }
    TextButton(onClick = { onOpenLegal(LegalInitialTab.Terms) }) { Text(appText(Res.string.settings_terms)) }
}

@Composable
internal fun AccountDataContent(accessToken: suspend () -> String?, preferences: Map<String, String>, deletion: Boolean) {
    val client = remember { AccountDataClient() }
    DisposableEffect(client) { onDispose(client::close) }
    val saver = rememberAccountArchiveSaver()
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<StringResource?>(null) }
    var exportSaved by remember { mutableStateOf(false) }
    var statuses by remember { mutableStateOf<List<AccountDeletionStatus>>(emptyList()) }
    var loading by remember { mutableStateOf(deletion) }
    var statusFailed by remember { mutableStateOf(false) }
    var reload by remember { mutableStateOf(0) }
    var confirm by remember { mutableStateOf(false) }
    var confirmation by remember { mutableStateOf("") }
    LaunchedEffect(deletion, reload) {
        if (!deletion) return@LaunchedEffect
        loading = true
        statusFailed = false
        try { statuses = client.deletions(checkNotNull(accessToken())) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { statusFailed = true }
        finally { loading = false }
    }
    val busy = exporting || submitting
    val pending = statuses.any { it.status in setOf("REQUESTED", "UNDER_REVIEW", "APPROVED") }
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Filled.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Text(appText(if (deletion) Res.string.settings_export_first else Res.string.settings_export_title),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(appText(Res.string.settings_export_description), style = MaterialTheme.typography.bodyMedium)
            Text(appText(Res.string.settings_export_private), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp), enabled = !busy,
                onClick = {
                    exporting = true
                    message = null
                    scope.launch {
                        try {
                            val archive = client.archive(checkNotNull(accessToken()), preferences)
                            message = when (saver.save(archive)) {
                                ArchiveSaveResult.Saved -> { exportSaved = true; Res.string.settings_export_saved }
                                ArchiveSaveResult.Cancelled -> Res.string.settings_export_cancelled
                                ArchiveSaveResult.Busy -> Res.string.settings_export_busy
                                ArchiveSaveResult.Failed -> Res.string.settings_export_failed
                            }
                        } catch (cancelled: CancellationException) { throw cancelled }
                        catch (error: SequoApiException) {
                            message = when (error.statusCode) {
                                413 -> Res.string.settings_export_too_large
                                429 -> Res.string.settings_export_rate_limit
                                else -> Res.string.settings_export_failed
                            }
                        } catch (_: Exception) { message = Res.string.settings_export_failed }
                        finally { exporting = false }
                    }
                }) {
                if (exporting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Icon(if (exportSaved) Icons.Filled.CheckCircle else Icons.Filled.Download, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(appText(if (exporting) Res.string.settings_export_preparing else Res.string.settings_download_data))
            }
        }
    }
    message?.let { Text(appText(it), style = MaterialTheme.typography.bodyMedium) }
    if (deletion) {
        HorizontalDivider()
        Text(appText(Res.string.settings_delete_explanation), style = MaterialTheme.typography.bodyMedium)
        Text(appText(Res.string.settings_delete_retention), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        when {
            loading -> CircularProgressIndicator(Modifier.size(24.dp))
            statusFailed -> {
                Text(appText(Res.string.settings_delete_status_failed), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { reload++ }, enabled = !busy) { Text(appText(Res.string.cart_refresh)) }
            }
            else -> {
                statuses.firstOrNull()?.let { status ->
                    Text(appText(deletionStatusLabel(status.status)), style = MaterialTheme.typography.titleSmall)
                }
                if (!pending) OutlinedButton(onClick = { confirmation = ""; confirm = true }, enabled = !busy,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text(appText(Res.string.settings_request_deletion))
                }
                else TextButton(onClick = { reload++ }, enabled = !busy) { Text(appText(Res.string.cart_refresh)) }
            }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { if (!submitting) confirm = false },
        icon = { Icon(Icons.Filled.PersonRemove, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(appText(Res.string.settings_delete_account)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(appText(Res.string.settings_delete_confirm_detail))
                if (!exportSaved) Text(appText(Res.string.settings_delete_export_reminder), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(value = confirmation, onValueChange = { confirmation = it }, enabled = !submitting,
                    label = { Text(appText(Res.string.settings_delete_type)) }, singleLine = true)
            }
        },
        dismissButton = { TextButton(enabled = !submitting, onClick = { confirm = false }) { Text(appText(Res.string.settings_delete_cancel)) } },
        confirmButton = {
            TextButton(enabled = confirmation == "DELETE" && !submitting, onClick = {
                submitting = true
                message = null
                scope.launch {
                    try {
                        val result = client.requestDeletion(checkNotNull(accessToken()))
                        statuses = listOf(result)
                        confirm = false
                        message = Res.string.settings_delete_received
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { message = Res.string.settings_delete_failed; confirm = false }
                    finally { submitting = false }
                }
            }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text(appText(if (submitting) Res.string.checkout_submitting else Res.string.settings_request_deletion))
            }
        })
}

private fun deletionStatusLabel(status: String) = when (status) {
    "REQUESTED" -> Res.string.settings_delete_pending
    "UNDER_REVIEW" -> Res.string.settings_delete_review
    "APPROVED" -> Res.string.settings_delete_approved
    "COMPLETED" -> Res.string.settings_delete_complete
    "REJECTED" -> Res.string.settings_delete_rejected
    "CANCELLED" -> Res.string.settings_delete_cancelled
    else -> Res.string.settings_delete_status_failed
}
