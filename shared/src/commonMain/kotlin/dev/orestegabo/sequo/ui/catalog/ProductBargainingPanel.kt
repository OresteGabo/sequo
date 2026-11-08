package dev.orestegabo.sequo.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.bargaining.*
import dev.orestegabo.sequo.core.network.SequoApiException
import dev.orestegabo.sequo.feature.settings.appText
import dev.orestegabo.sequo.logic.formatCfa
import dev.orestegabo.sequo.model.SequoProduct
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.random.Random
import sequo.shared.generated.resources.*

@Composable
internal fun ProductBargainingPanel(
    product: SequoProduct,
    repository: BargainingRepository?,
    onSignInRequested: () -> Unit,
    onAcceptedThreadChanged: (String?) -> Unit,
) {
    var thread by remember(product.id, repository) { mutableStateOf<BargainingThread?>(null) }
    var loaded by remember(product.id, repository) { mutableStateOf(false) }
    var busy by remember(product.id, repository) { mutableStateOf(false) }
    var failed by remember(product.id, repository) { mutableStateOf(false) }
    var signInRequired by remember(product.id, repository) { mutableStateOf(false) }
    var offerText by rememberSaveable(product.id) { mutableStateOf("") }
    var retrySignature by rememberSaveable(product.id) { mutableStateOf("") }
    var retryKey by rememberSaveable(product.id) { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun actionKey(signature: String): String {
        if (signature != retrySignature || retryKey.isEmpty()) {
            retrySignature = signature
            retryKey = "bargain-" + Random.nextBytes(16).joinToString("") { byte -> (byte.toInt() and 255).toString(16).padStart(2, '0') }
        }
        return retryKey
    }
    suspend fun request(action: suspend () -> BargainingThread?) {
        busy = true; failed = false; signInRequired = false
        try {
            thread = action()
            loaded = true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            failed = true
            signInRequired = (error as? SequoApiException)?.statusCode in setOf(401, 403)
        } finally { busy = false }
    }
    suspend fun refresh() = request { repository?.threads(product.id)?.firstOrNull() }

    LaunchedEffect(product.id, repository) { if (repository != null) refresh() }
    LaunchedEffect(thread?.id, thread?.status, thread?.acceptedPriceCfa, repository) {
        onAcceptedThreadChanged(thread?.takeIf { it.accepted }?.id)
    }
    val current = thread
    val merchantCounter = current?.pendingMerchantOffer
    val amount = offerText.toIntOrNull()
    val canOffer = current == null || (current.nextActor == "CUSTOMER" && current.status in setOf("OPEN", "REJECTED") && current.attemptsRemaining > 0)
    val amountValid = amount != null && amount > 0 && amount < (current?.catalogPriceCfa ?: product.priceCfa)

    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f))) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(appText(Res.string.home_make_offer), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(appText(Res.string.bargain_detail_default))
            if (repository == null) {
                TextButton(onClick = onSignInRequested) { Text(appText(Res.string.bargain_sign_in)) }
            } else {
                if (busy) CircularProgressIndicator(Modifier.size(20.dp))
                TextButton(onClick = { scope.launch { refresh() } }, enabled = !busy) { Text(appText(Res.string.bargain_refresh)) }
                if (failed) {
                    Text(appText(if (signInRequired) Res.string.bargain_sign_in else Res.string.bargain_request_failed), color = MaterialTheme.colorScheme.error)
                    if (signInRequired) TextButton(onClick = onSignInRequested) { Text(appText(Res.string.bargain_sign_in)) }
                }
                current?.let { saved ->
                    Text(appText(when (saved.status) {
                        "ACCEPTED" -> Res.string.bargain_status_accepted
                        "REJECTED" -> Res.string.bargain_status_rejected
                        "EXPIRED" -> Res.string.bargain_status_expired
                        "LOCKED_ATTEMPTS_EXHAUSTED" -> Res.string.bargain_attempts_exhausted
                        "CANCELLED" -> Res.string.bargain_status_closed
                        else -> if (saved.nextActor == "MERCHANT") Res.string.bargain_waiting_merchant else Res.string.bargain_status_open
                    }))
                    Text(appText(Res.string.bargain_attempts_remaining) + " " + saved.attemptsRemaining)
                    if (!saved.bargainingEnabled && !saved.accepted) Text(appText(Res.string.bargain_disabled))
                    if (saved.accepted) {
                        Text(appText(Res.string.bargain_locked_price) + " · " + formatCfa(checkNotNull(saved.acceptedPriceCfa)))
                        saved.expiresAt?.let { Text(appText(Res.string.bargain_expires) + " " + it) }
                    }
                    saved.offers.forEach { offer ->
                        val actor = appText(if (offer.actorType == "CUSTOMER") Res.string.bargain_actor_you else Res.string.bargain_actor_seller)
                        val kind = appText(when (offer.offerType) {
                            "ACCEPT" -> Res.string.bargain_status_accepted
                            "REJECT" -> Res.string.bargain_status_rejected
                            "COUNTER_OFFER" -> Res.string.bargain_counter_offer
                            "MESSAGE" -> Res.string.bargain_message
                            else -> Res.string.bargain_offer_label
                        })
                        Text(actor + " · " + kind + offer.amountCfa?.let { " · " + formatCfa(it) }.orEmpty(), style = MaterialTheme.typography.bodySmall)
                        offer.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
                if (loaded && canOffer && current?.bargainingEnabled != false) {
                    OutlinedTextField(value = offerText, onValueChange = { offerText = it.filter(Char::isDigit).take(9) },
                        modifier = Modifier.fillMaxWidth(), enabled = !busy, singleLine = true,
                        label = { Text(appText(Res.string.bargain_offer_label)) }, suffix = { Text("CFA") },
                        supportingText = { Text(appText(Res.string.bargain_invalid_amount)) })
                    Button(onClick = {
                        val submittedAmount = amount ?: return@Button
                        val threadId = current?.id
                        val key = actionKey("${threadId.orEmpty()}:OFFER:$submittedAmount")
                        scope.launch {
                            request { repository.offer(product.id, threadId, submittedAmount, key) }
                            if (!failed) { offerText = ""; retrySignature = ""; retryKey = "" }
                        }
                    }, enabled = !busy && amountValid, modifier = Modifier.fillMaxWidth()) { Text(appText(Res.string.bargain_submit_offer)) }
                }
                if (merchantCounter != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (accept in listOf(true, false)) {
                            OutlinedButton(onClick = {
                                val key = actionKey("${current.id}:${merchantCounter.id}:$accept")
                                scope.launch {
                                    request { repository.respond(current.id, merchantCounter.id, accept, key) }
                                    if (!failed) { retrySignature = ""; retryKey = "" }
                                }
                            }, enabled = !busy && current.bargainingEnabled) { Text(appText(if (accept) Res.string.bargain_accept_counter else Res.string.bargain_reject_counter)) }
                        }
                    }
                }
            }
        }
    }
}
