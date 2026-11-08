package dev.orestegabo.sequo.ui.basket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.design.SequoDesign
import dev.orestegabo.sequo.design.SequoPanel
import dev.orestegabo.sequo.core.auth.SavedDeliveryAddress
import dev.orestegabo.sequo.core.catalog.CartLine
import dev.orestegabo.sequo.core.catalog.CartQuote
import dev.orestegabo.sequo.core.catalog.CartSnapshot
import dev.orestegabo.sequo.core.catalog.CheckoutSummary
import dev.orestegabo.sequo.core.catalog.CheckoutProvider
import dev.orestegabo.sequo.core.network.SequoApiException
import dev.orestegabo.sequo.feature.settings.appText
import dev.orestegabo.sequo.logic.formatCfa
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import sequo.shared.generated.resources.*

@Composable
internal fun BasketContent(
    cart: CartSnapshot?,
    busy: Boolean,
    failed: Boolean,
    onRefresh: () -> Unit,
    onQuantity: (CartLine, Int) -> Unit,
    loadAddresses: suspend () -> List<SavedDeliveryAddress>,
    loadQuote: suspend (String, String?, String?) -> CartQuote,
    loadRelayPoints: suspend () -> List<dev.orestegabo.sequo.core.catalog.RelayDestination>,
    loadPaymentProviders: suspend () -> List<CheckoutProvider>,
    submitCheckout: suspend (CartQuote, String) -> CheckoutSummary,
    onOpenOrders: () -> Unit,
) {
    var submitting by remember { mutableStateOf(false) }
    var providers by remember { mutableStateOf<List<CheckoutProvider>?>(null) }
    var providersFailed by remember { mutableStateOf(false) }
    LaunchedEffect(busy) {
        if (busy) return@LaunchedEffect
        providersFailed = false
        providers = null
        try {
            providers = loadPaymentProviders()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            providers = null
            providersFailed = true
        }
    }
    var checkoutError by remember { mutableStateOf<CheckoutError?>(null) }
    val scope = rememberCoroutineScope()
    val editingEnabled = !busy && !submitting
    Text(appText(Res.string.nav_basket_title), style = MaterialTheme.typography.headlineSmall)
    if (busy) Text(appText(Res.string.cart_loading))
    if (failed) Text(appText(Res.string.cart_load_failed), color = MaterialTheme.colorScheme.error)
    TextButton(onClick = onRefresh, enabled = editingEnabled) { Text(appText(Res.string.cart_refresh)) }
    if (cart == null) return
    if (cart.items.isEmpty()) {
        Text(appText(Res.string.cart_empty))
        return
    }

    for (line in cart.items) {
        SequoPanel {
            Column(Modifier.padding(SequoDesign.CardPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(line.product.name, style = MaterialTheme.typography.titleMedium)
                val price = line.lineTotalCfa
                Text(if (line.available && price != null) formatCfa(price) else appText(Res.string.cart_item_unavailable))
                line.negotiatedUnitPriceCfa?.let { Text(appText(Res.string.bargain_locked_price) + " · " + formatCfa(it)) }
                line.negotiatedPriceExpiresAt?.let { Text(appText(Res.string.bargain_expires) + " " + it) }
                if (!line.available && line.bargainingThreadId != null) Text(appText(Res.string.bargain_review_cart), color = MaterialTheme.colorScheme.error)
                val optionNames = line.selections.flatMap { selection ->
                    val group = line.product.customizations.firstOrNull { it.id == selection.groupId }
                    group?.options.orEmpty().filter { it.id in selection.optionIds }.map { it.name }
                }
                if (optionNames.isNotEmpty()) Text(optionNames.joinToString())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onQuantity(line, line.quantity - 1) }, enabled = editingEnabled && line.available && line.quantity > 1) {
                        Text("−")
                    }
                    Text(line.quantity.toString())
                    TextButton(onClick = { onQuantity(line, line.quantity + 1) }, enabled = editingEnabled && line.available && line.quantity < 99) {
                        Text("+")
                    }
                    TextButton(onClick = { onQuantity(line, 0) }, enabled = editingEnabled) { Text(appText(Res.string.cart_remove)) }
                }
            }
            }
    }

    CartAmount(appText(Res.string.basket_payment_items), cart.subtotalCfa)
    if (cart.items.any { !it.available } || failed) return

    var addresses by remember { mutableStateOf<List<SavedDeliveryAddress>?>(null) }
    var addressesFailed by remember { mutableStateOf(false) }
    var addressId by remember { mutableStateOf<String?>(null) }
    var relayPointId by remember { mutableStateOf<String?>(null) }
    var relayPoints by remember { mutableStateOf<List<dev.orestegabo.sequo.core.catalog.RelayDestination>?>(null) }
    var relaysFailed by remember { mutableStateOf(false) }
    var deliveryMode by remember { mutableStateOf<String?>(null) }
    var modes by remember { mutableStateOf<List<dev.orestegabo.sequo.core.catalog.DeliveryModeOffer>>(emptyList()) }
    var quote by remember(cart, addressId, relayPointId, deliveryMode) { mutableStateOf<CartQuote?>(null) }
    var quoteFailed by remember(cart, addressId, relayPointId, deliveryMode) { mutableStateOf(false) }
    LaunchedEffect(cart) {
        try {
            addresses = loadAddresses().filter { it.id.isNotBlank() }
            addressesFailed = false
            addressId = addressId?.takeIf { id -> addresses.orEmpty().any { it.id == id } }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            addresses = null
            addressesFailed = true
        }
    }
    LaunchedEffect(cart, busy) {
        if (busy) return@LaunchedEffect
        relaysFailed = false
        try {
            relayPoints = loadRelayPoints()
            // Keep a chosen but now unavailable relay until the customer explicitly changes destination.
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { relayPoints = null; relaysFailed = true }
    }
    var quoteRefresh by remember { mutableStateOf(0) }
    LaunchedEffect(cart, addressId, relayPointId, deliveryMode, relayPoints, relaysFailed, busy, quoteRefresh) {
        quote = null
        quoteFailed = false
        if (busy) return@LaunchedEffect
        val selected = addressId ?: return@LaunchedEffect
        if (relayPointId != null && (relaysFailed || relayPoints.orEmpty().none { it.id == relayPointId })) {
            quoteFailed = true
            return@LaunchedEffect
        }
        try {
            quote = loadQuote(selected, relayPointId, deliveryMode)
            modes = quote?.availableDeliveryModes.orEmpty()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            quoteFailed = true
        }
    }
    Text(appText(Res.string.cart_relay_title), style = MaterialTheme.typography.titleMedium)
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = relayPointId == null, enabled = editingEnabled,
            onClick = { relayPointId = null; checkoutError = null })
        Text(appText(Res.string.basket_delivery_home))
    }
    if (relaysFailed) Text(appText(Res.string.cart_load_failed), color = MaterialTheme.colorScheme.error)
    else if (relayPoints == null) Text(appText(Res.string.cart_loading))
    else if (relayPoints.orEmpty().isEmpty()) Text(appText(Res.string.cart_relay_unavailable))
    for (relay in relayPoints.orEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = relayPointId == relay.id, enabled = editingEnabled,
                onClick = { relayPointId = relay.id; checkoutError = null })
            Column { Text(relay.name); Text(listOfNotNull(relay.neighborhood, relay.city, relay.landmark).joinToString(", ")) }
        }
    }
    Text(appText(if (relayPointId == null) Res.string.basket_delivery_home else Res.string.cart_relay_contact), style = MaterialTheme.typography.titleMedium)
    when {
        addressesFailed -> Text(appText(Res.string.cart_load_failed), color = MaterialTheme.colorScheme.error)
        addresses == null -> Text(appText(Res.string.cart_loading))
        addresses.orEmpty().isEmpty() -> Text(appText(Res.string.cart_no_addresses))
    }
    for (address in addresses.orEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = address.id == addressId,
                onClick = {
                    addressId = address.id
                    checkoutError = null
                },
                enabled = editingEnabled,
            )
            Column {
                Text(address.label)
                Text(listOfNotNull(address.area, address.city).joinToString(", "))
            }
        }
    }
    if (modes.isNotEmpty()) {
        Text(appText(Res.string.delivery_mode_title), style = MaterialTheme.typography.titleMedium)
        for (mode in modes) Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = (deliveryMode ?: quote?.deliveryOption?.id) == mode.id, enabled = editingEnabled,
                onClick = { deliveryMode = mode.id; checkoutError = null })
            Column {
                Text(appText(deliveryModeName(mode.id)))
                Text("${mode.etaMinMinutes}–${mode.etaMaxMinutes} min · ${formatCfa(mode.deliveryFeeCfa)}")
            }
        }
    } else if (quote != null) Text(appText(Res.string.delivery_eta_unavailable))
    if (quoteFailed && deliveryMode != null) TextButton(onClick = { deliveryMode = null; quoteRefresh++ }, enabled = editingEnabled) {
        Text(appText(Res.string.cart_refresh))
    }
    when {
        quoteFailed -> Text(appText(Res.string.cart_quote_failed), color = MaterialTheme.colorScheme.error)
        addressId != null && quote == null -> Text(appText(Res.string.cart_loading))
        !busy -> quote?.let {
            CartAmount(appText(Res.string.basket_payment_items), it.productTotalCfa)
            CartAmount(appText(Res.string.cart_options), it.optionTotalCfa)
            CartAmount(appText(Res.string.cart_service_fee), it.serviceFeeCfa)
            val hasDeliveryBenefits = it.subscriptionDiscountCfa > 0 || it.loyaltyDiscountCfa > 0 || it.referralCreditAppliedCfa > 0
            if (hasDeliveryBenefits) {
                CartAmount(appText(Res.string.benefits_delivery_base), it.deliveryBaseFeeCfa ?: it.deliveryFeeCfa)
                if (it.subscriptionDiscountCfa > 0) CartAmount(appText(Res.string.benefits_subscription_discount), -it.subscriptionDiscountCfa)
                if (it.loyaltyDiscountCfa > 0) CartAmount(appText(Res.string.benefits_loyalty_discount), -it.loyaltyDiscountCfa)
                if (it.referralCreditAppliedCfa > 0) CartAmount(appText(Res.string.benefits_referral_credit), -it.referralCreditAppliedCfa)
            }
            CartAmount(appText(if (hasDeliveryBenefits) Res.string.benefits_delivery_final else Res.string.basket_delivery), it.deliveryFeeCfa)
            CartAmount(appText(Res.string.basket_payment_total), it.totalCfa)
            val reviewedQuote = it
            var provider by remember { mutableStateOf("yas_togo") }
            for ((id, label) in listOf("yas_togo" to "Mixx by Yas", "moov_africa" to "Flooz")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = provider == id, onClick = { provider = id }, enabled = editingEnabled)
                    Text(label)
                }
                val configuration = providers?.firstOrNull { it.providerId == id }
                Text(appText(when {
                    providersFailed -> Res.string.checkout_provider_status_failed
                    providers == null -> Res.string.cart_loading
                    configuration?.paymentAvailable == true -> Res.string.checkout_provider_ready
                    configuration?.apiConfigured == true -> Res.string.checkout_provider_prepared
                    else -> Res.string.basket_payment_not_configured_detail
                }), style = MaterialTheme.typography.bodySmall)
            }
            Button(
                enabled = editingEnabled,
                onClick = {
                    submitting = true
                    checkoutError = null
                    scope.launch {
                        try {
                            submitCheckout(reviewedQuote, provider)
                            onOpenOrders()
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (error: SequoApiException) {
                            when (error.statusCode) {
                                409 -> {
                                    checkoutError = CheckoutError.PriceChanged
                                    quote = null
                                    quoteRefresh++
                                    onRefresh()
                                }
                                422 -> checkoutError = CheckoutError.Rejected
                                else -> checkoutError = CheckoutError.SubmitFailed
                            }
                        } catch (_: Exception) {
                            checkoutError = CheckoutError.SubmitFailed
                        } finally {
                            submitting = false
                        }
                    }
                },
            ) {
                Text(appText(if (submitting) Res.string.checkout_submitting else Res.string.checkout_submit))
            }
        }
    }
    checkoutError?.let { error ->
        Text(appText(when (error) {
            CheckoutError.PriceChanged -> Res.string.checkout_price_changed
            CheckoutError.Rejected -> Res.string.checkout_rejected
            CheckoutError.SubmitFailed -> Res.string.checkout_submit_failed
        }), color = MaterialTheme.colorScheme.error)
        if (error != CheckoutError.PriceChanged) {
            TextButton(onClick = onOpenOrders) { Text(appText(Res.string.checkout_view_orders)) }
        }
    }
}

private enum class CheckoutError { PriceChanged, Rejected, SubmitFailed }

@Composable
private fun CartAmount(label: String, amount: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(formatCfa(amount))
    }
}

internal fun deliveryModeName(id: String) = when (id) {
    "CLASSIC" -> Res.string.delivery_mode_classic
    "DEFERRED" -> Res.string.delivery_mode_deferred
    else -> Res.string.delivery_mode_express
}
