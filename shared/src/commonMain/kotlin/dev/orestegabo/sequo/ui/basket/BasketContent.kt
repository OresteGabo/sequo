package dev.orestegabo.sequo.ui.basket

import dev.orestegabo.sequo.ui.components.SequoEmptyState
import dev.orestegabo.sequo.ui.components.EmptyStateVisual
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import dev.orestegabo.sequo.ui.components.SequoCard
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
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
import dev.orestegabo.sequo.logic.displayLabel
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
    onBrowse: () -> Unit,
) {
    var submitting by remember { mutableStateOf(false) }
    var providers by remember { mutableStateOf<List<CheckoutProvider>?>(null) }
    var providersFailed by remember { mutableStateOf(false) }
    LaunchedEffect(busy, cart?.items?.isEmpty()) {
        if (busy || cart?.items?.isEmpty() != false) return@LaunchedEffect
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
    if (busy) Text(appText(Res.string.cart_loading))
    if (failed) Text(appText(Res.string.cart_load_failed), color = MaterialTheme.colorScheme.error)
    if (cart == null || ((busy || failed) && cart.items.isEmpty())) {
        TextButton(onClick = onRefresh, enabled = editingEnabled) { Text(appText(Res.string.cart_refresh)) }
        return
    }
    if (cart.items.isEmpty()) {
        SequoEmptyState(
            visual = EmptyStateVisual.Cart,
            title = appText(Res.string.empty_cart_title), detail = appText(Res.string.empty_cart_detail),
            actionLabel = appText(Res.string.empty_explore), onAction = onBrowse,
        )
        return
    }
    TextButton(onClick = onRefresh, enabled = editingEnabled) { Text(appText(Res.string.cart_refresh)) }

    for (line in cart.items) {
        SequoCard {
            Column(Modifier.padding(SequoDesign.CardPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top) {
                    Text(line.product.name, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    val price = line.lineTotalCfa
                    Text(if (line.available && price != null) formatCfa(price) else appText(Res.string.cart_item_unavailable),
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                        color = if (line.available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
                line.negotiatedUnitPriceCfa?.let { Text(appText(Res.string.bargain_locked_price) + " · " + formatCfa(it)) }
                line.negotiatedPriceExpiresAt?.let { Text(appText(Res.string.bargain_expires) + " " + it) }
                if (!line.available && line.bargainingThreadId != null) Text(appText(Res.string.bargain_review_cart), color = MaterialTheme.colorScheme.error)
                val optionNames = line.selections.flatMap { selection ->
                    val group = line.product.customizations.firstOrNull { it.id == selection.groupId }
                    group?.options.orEmpty().filter { it.id in selection.optionIds }.map { it.displayLabel(requireNotNull(group)) }
                }
                if (optionNames.isNotEmpty()) Text(optionNames.joinToString())
                if (line.product.customizations.any { it.type == "MEASURE" }) Text(appText(Res.string.selling_portion_hint),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .35f))) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onQuantity(line, line.quantity - 1) },
                                enabled = editingEnabled && line.available && line.quantity > 1) {
                                Icon(Icons.Filled.Remove, appText(Res.string.polish_decrease_quantity))
                            }
                            Text(line.quantity.toString(), modifier = Modifier.padding(horizontal = 6.dp), fontWeight = FontWeight.SemiBold)
                            IconButton(onClick = { onQuantity(line, line.quantity + 1) },
                                enabled = editingEnabled && line.available && line.quantity < 99) {
                                Icon(Icons.Filled.Add, appText(Res.string.polish_increase_quantity))
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { onQuantity(line, 0) }, enabled = editingEnabled,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text(appText(Res.string.cart_remove))
                    }
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
            SequoCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .4f))
            CartAmount(appText(Res.string.basket_payment_total), it.totalCfa, strong = true)
            }
            }
            val reviewedQuote = it
            var provider by remember { mutableStateOf("yas_togo") }
            for ((id, label) in listOf("yas_togo" to "Mixx by Yas", "moov_africa" to "Flooz")) {
                val configuration = providers?.firstOrNull { it.providerId == id }
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (provider == id) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = .35f))) {
                    Row(Modifier.fillMaxWidth().selectable(selected = provider == id, enabled = editingEnabled,
                        role = Role.RadioButton, onClick = { provider = id }).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RadioButton(selected = provider == id, onClick = null, enabled = editingEnabled)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(label, style = MaterialTheme.typography.titleSmall)
                            Text(appText(when {
                                providersFailed -> Res.string.checkout_provider_status_failed
                                providers == null -> Res.string.cart_loading
                                configuration?.paymentAvailable == true -> Res.string.checkout_provider_ready
                                configuration?.apiConfigured == true -> Res.string.checkout_provider_prepared
                                else -> Res.string.basket_payment_not_configured_detail
                            }), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp), shape = RoundedCornerShape(16.dp),
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
private fun CartAmount(label: String, amount: Int, strong: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(formatCfa(amount), style = if (strong) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium,
            color = if (strong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Medium)
    }
}

internal fun deliveryModeName(id: String) = when (id) {
    "CLASSIC" -> Res.string.delivery_mode_classic
    "DEFERRED" -> Res.string.delivery_mode_deferred
    else -> Res.string.delivery_mode_express
}
