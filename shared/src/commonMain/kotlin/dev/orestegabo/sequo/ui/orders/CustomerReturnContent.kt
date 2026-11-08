package dev.orestegabo.sequo.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.catalog.CustomerReturnEligibility
import dev.orestegabo.sequo.core.catalog.CustomerReturnRequest
import dev.orestegabo.sequo.core.catalog.RelayDestination
import dev.orestegabo.sequo.feature.settings.appText
import dev.orestegabo.sequo.logic.formatCfa
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.random.Random
import sequo.shared.generated.resources.*

@Composable
internal fun CustomerReturnContent(
    orderId: String,
    refreshVersion: Int,
    loadEligibility: suspend (String) -> CustomerReturnEligibility,
    loadReturns: suspend (String) -> List<CustomerReturnRequest>,
    submit: suspend (String, String, String) -> CustomerReturnRequest,
    onRequested: () -> Unit,
) {
    var expanded by rememberSaveable(orderId) { mutableStateOf(false) }
    var reason by rememberSaveable(orderId) { mutableStateOf("") }
    var retryReason by rememberSaveable(orderId) { mutableStateOf<String?>(null) }
    val requestId = rememberSaveable(orderId) {
        "return-" + Random.nextBytes(16).joinToString("") { byte -> (byte.toInt() and 255).toString(16).padStart(2, '0') }
    }
    var eligibility by remember(orderId) { mutableStateOf<CustomerReturnEligibility?>(null) }
    var requests by remember(orderId) { mutableStateOf(emptyList<CustomerReturnRequest>()) }
    var busy by remember(orderId) { mutableStateOf(false) }
    var failed by remember(orderId) { mutableStateOf(false) }
    var submitFailed by remember(orderId) { mutableStateOf(false) }
    var refresh by remember(orderId) { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(orderId, expanded, refresh, refreshVersion) {
        if (expanded) {
            busy = true; failed = false; eligibility = null
            try {
                val actualRequests = loadReturns(orderId)
                val actualEligibility = loadEligibility(orderId)
                requests = actualRequests
                eligibility = actualEligibility
                retryReason = null
                submitFailed = false
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { failed = true }
            finally { busy = false }
        }
    }
    TextButton(onClick = { expanded = !expanded }, enabled = !busy) {
        Text(appText(Res.string.returns_options))
    }
    if (!expanded) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { refresh++ }, enabled = !busy) { Text(appText(Res.string.cart_refresh)) }
        if (busy) Text(appText(Res.string.returns_loading))
        if (failed) Text(appText(Res.string.returns_load_failed), color = MaterialTheme.colorScheme.error)
        if (submitFailed) Text(appText(Res.string.returns_request_failed), color = MaterialTheme.colorScheme.error)
        for (request in requests) {
            Text(appText(Res.string.orders_return_reference) + " · " + request.id)
            Text(appText(returnStatus(request.status)), style = MaterialTheme.typography.titleSmall)
            Text(request.reason)
            Text(request.requestedAt, style = MaterialTheme.typography.bodySmall)
            ReturnInstructions(request.dropoffMethod, request.instructions, request.relayPoints)
            if (!busy && !failed && request.status == "AwaitingRelayDropoff" && request.dropoffMethod == "RELAY") {
                request.returnPin?.takeIf { it.isNotBlank() }?.let {
                    Text(appText(Res.string.orders_return_pin) + " · " + it, style = MaterialTheme.typography.titleLarge)
                }
            }
            request.droppedAt?.let { Text(appText(Res.string.orders_return_status_dropped_relay) + " · " + it) }
            request.collectedBySequoAt?.let { Text(appText(Res.string.returns_status_collected) + " · " + it) }
            request.receivedBySequoAt?.let { Text(appText(Res.string.returns_status_received) + " · " + it) }
            request.inspectionDecision?.let { decision ->
                Text(appText(when (decision) {
                    "ACCEPTED" -> Res.string.returns_inspection_accepted
                    "REJECTED" -> Res.string.returns_inspection_rejected
                    else -> Res.string.returns_inspection_recorded
                }) + request.inspectedAt?.let { " · " + it }.orEmpty())
            }
            request.refundInstruction?.let { instruction ->
                Text(appText(Res.string.returns_refund_instruction) + " · " + instruction.id)
                Text(if (instruction.currency == "XOF") formatCfa(instruction.amountCfa) else "${instruction.amountCfa} ${instruction.currency}")
                Text(if (instruction.status == "REQUESTED") appText(Res.string.returns_refund_instruction_recorded) else instruction.status)
                Text(if (instruction.paymentStatus == "NOT_SUBMITTED") appText(Res.string.returns_refund_not_submitted) else instruction.paymentStatus)
                Text(instruction.requestedAt, style = MaterialTheme.typography.bodySmall)
                if (request.refundDeadlineState == "UNCONFIGURED") Text(appText(Res.string.returns_refund_timeline_pending))
                else request.refundDueAt?.let { Text(appText(Res.string.returns_refund_due) + " " + it) }
            }
        }
        val policy = eligibility
        if (!failed && policy != null && requests.isEmpty()) {
            if (policy.existingReturnId != null) {
                Text(appText(Res.string.orders_return_reference) + " · " + policy.existingReturnId)
                Text(appText(Res.string.returns_refresh_existing))
            } else if (policy.eligible) {
                policy.returnWindowEndsAt?.let { Text(appText(Res.string.returns_request_by) + " " + it) }
                ReturnInstructions(policy.dropoffMethod, policy.instructions, policy.relayPoints)
                OutlinedTextField(value = retryReason ?: reason,
                    onValueChange = { reason = it.take(1000) },
                    enabled = !busy && retryReason == null,
                    label = { Text(appText(Res.string.orders_return_reason)) },
                    modifier = Modifier.fillMaxWidth(), minLines = 2)
                Button(onClick = {
                    val submittedReason = retryReason ?: reason.trim().also { retryReason = it }
                    busy = true; submitFailed = false
                    scope.launch {
                        try {
                            val saved = submit(orderId, requestId, submittedReason)
                            requests = listOf(saved)
                            onRequested()
                        } catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) { submitFailed = true }
                        finally { busy = false }
                    }
                }, enabled = !busy && (retryReason ?: reason).isNotBlank()) {
                    Text(appText(if (retryReason == null) Res.string.orders_return_confirm else Res.string.returns_retry_request))
                }
                Text(appText(Res.string.returns_review_note), style = MaterialTheme.typography.bodySmall)
            } else {
                Text(appText(when (policy.code) {
                    "return_window_expired" -> Res.string.orders_return_expired
                    "order_not_delivered" -> Res.string.returns_not_delivered
                    else -> Res.string.returns_unavailable
                }))
                policy.message?.takeIf { it.isNotBlank() }?.let { Text(it) }
            }
        }
    }
}

@Composable
private fun ReturnInstructions(method: String?, instructions: String?, points: List<RelayDestination>) {
    val heading = when (method) {
        "RELAY" -> Res.string.orders_return_relay_dropoff
        "CUSTOMER_MANAGED_PENDING" -> Res.string.returns_customer_managed_pending
        "CUSTOMER_MANAGED" -> Res.string.returns_customer_managed
        "SEQUO_INSTRUCTIONS_PENDING" -> Res.string.returns_sequo_instructions_pending
        else -> null
    }
    heading?.let { Text(appText(it)) }
    instructions?.takeIf { it.isNotBlank() }?.let { text ->
        val translated = if (method in setOf("RELAY", "CUSTOMER_MANAGED_PENDING", "CUSTOMER_MANAGED", "SEQUO_INSTRUCTIONS_PENDING", "ALREADY_DEPOSITED")) returnInstructionTranslations[text] else null
        Text(translated?.let { appText(it) } ?: text, style = MaterialTheme.typography.bodySmall)
    }
    points.forEach { Text(listOfNotNull(it.name, it.landmark, it.neighborhood, it.city).joinToString(" · ")) }
}

private fun returnStatus(status: String) = when (status) {
    "Requested" -> Res.string.orders_return_status_requested
    "AwaitingRelayDropoff" -> Res.string.orders_return_status_dropoff_pending
    "DroppedAtRelay" -> Res.string.orders_return_status_dropped_relay
    "CollectedBySequo" -> Res.string.returns_status_collected
    "ReceivedBySequo" -> Res.string.returns_status_received
    "RefundPending" -> Res.string.returns_status_refund_pending
    "Rejected" -> Res.string.orders_return_status_rejected
    else -> Res.string.orders_status_unknown
}

private val returnInstructionTranslations = mapOf(
    "Bring the packaged item, return ID and return PIN to an available eligible relay below. Have the operator confirm receipt; reimbursement requires SEQUO receipt and inspection." to Res.string.returns_instruction_relay,
    "Keep the packaged item and your return ID. Contact SEQUO for an approved drop-off location; do not leave the item unattended." to Res.string.returns_instruction_pending,
    "Your Click and collect return request can be recorded. SEQUO must confirm the customer-managed return instructions before you move the item." to Res.string.returns_instruction_pickup_pending,
    "Arrange the customer-managed return directly with the seller. Keep your return ID; no relay drop-off is authorized by this instruction." to Res.string.returns_instruction_seller,
    "Your return is recorded. Contact SEQUO to confirm the existing return credential before going to a relay." to Res.string.returns_instruction_credential_pending,
    "The relay recorded your drop-off. Wait for SEQUO collection and inspection." to Res.string.returns_instruction_dropped,
    "SEQUO received the item. Its condition and refund eligibility are being reviewed." to Res.string.returns_instruction_received,
    "Refund processing is pending. This status does not confirm a completed payment." to Res.string.returns_instruction_refund_pending,
    "This return was rejected. Contact SEQUO for further instructions." to Res.string.returns_instruction_rejected,
    "Contact SEQUO for the current return instructions." to Res.string.returns_instruction_current,
)
