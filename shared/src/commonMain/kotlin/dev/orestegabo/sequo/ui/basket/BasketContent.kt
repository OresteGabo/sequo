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
import androidx.compose.runtime.key
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
    pendingLineIds: Set<String>,
    failedLineIds: Set<String>,
    failed: Boolean,
    onRefresh: () -> Unit,
    onQuantity: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    loadAddresses: suspend () -> List<SavedDeliveryAddress>,
    saveAddress: suspend (String?, dev.orestegabo.sequo.core.catalog.DeliveryAddressInput) -> SavedDeliveryAddress,
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
    if (busy && cart?.items.isNullOrEmpty()) Text(appText(Res.string.cart_loading))
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
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onRefresh, enabled = editingEnabled) { Text(appText(Res.string.cart_refresh)) }
        Spacer(Modifier.weight(1f))
        if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
    }

    for (line in cart.items) {
        key(line.id) {
            val rowEnabled = editingEnabled && line.id !in pendingLineIds
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
                    if (line.id in failedLineIds) Text(appText(Res.string.cart_item_update_failed),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    line.negotiatedUnitPriceCfa?.let { Text(appText(Res.string.bargain_locked_price) + " · " + formatCfa(it)) }
                    line.negotiatedPriceExpiresAt?.let { Text(appText(Res.string.bargain_expires) + " " + it) }
                    if (!line.available && line.bargainingThreadId != null) Text(appText(Res.string.bargain_review_cart), color = MaterialTheme.colorScheme.error)
                    val optionNames = line.selections.flatMap { selection ->
                        val group = line.product.customizations.firstOrNull { it.id == selection.groupId }
                        group?.options.orEmpty().filter { it.id in selection.optionIds }.map { it.displayLabel(requireNotNull(group)) }
                    }
                    val itemDescription = listOf(line.product.name, optionNames.joinToString()).filter { it.isNotBlank() }.joinToString(" · ")
                    if (optionNames.isNotEmpty()) Text(optionNames.joinToString())
                    if (line.product.customizations.any { it.type == "MEASURE" }) Text(appText(Res.string.selling_portion_hint),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .35f))) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onQuantity(line.id, -1) },
                                    enabled = rowEnabled && line.available && line.quantity > 1) {
                                    Icon(Icons.Filled.Remove, appText(Res.string.polish_decrease_quantity) + " · " + itemDescription)
                                }
                                Box(Modifier.width(36.dp).height(48.dp), contentAlignment = Alignment.Center) {
                                if (line.id in pendingLineIds) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                else Text(line.quantity.toString(), fontWeight = FontWeight.SemiBold)
                            }
                                IconButton(onClick = { onQuantity(line.id, 1) },
                                    enabled = rowEnabled && line.available && line.quantity < 99) {
                                    Icon(Icons.Filled.Add, appText(Res.string.polish_increase_quantity) + " · " + itemDescription)
                                }
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { onRemove(line.id) }, enabled = rowEnabled,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Text(appText(Res.string.cart_remove))
                        }
                    }
                }
            }
        }
    }

    CartAmount(appText(Res.string.basket_payment_items), cart.subtotalCfa)
    if (cart.items.any { !it.available } || failed) {
        Text(appText(Res.string.cart_review_unavailable), color = MaterialTheme.colorScheme.error)
        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
            Text(appText(Res.string.cart_proceed))
        }
        return
    }

    var addresses by remember { mutableStateOf<List<SavedDeliveryAddress>?>(null) }
    var addressesFailed by remember { mutableStateOf(false) }
    var addressId by remember { mutableStateOf<String?>(null) }
    var addressRevision by remember { mutableStateOf(0) }
    var addressEditor by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf<SavedDeliveryAddress?>(null) }
    var relayPickup by remember { mutableStateOf(false) }
    var provider by remember { mutableStateOf("kkiapay") }
    var relayPointId by remember { mutableStateOf<String?>(null) }
    var relayPoints by remember { mutableStateOf<List<dev.orestegabo.sequo.core.catalog.RelayDestination>?>(null) }
    var relaysFailed by remember { mutableStateOf(false) }
    var deliveryMode by remember { mutableStateOf<String?>(null) }
    var modes by remember { mutableStateOf<List<dev.orestegabo.sequo.core.catalog.DeliveryModeOffer>>(emptyList()) }
    var quote by remember(addressId, relayPointId, relayPickup, deliveryMode) { mutableStateOf<CartQuote?>(null) }
    var quoteFailed by remember(addressId, relayPointId, relayPickup, deliveryMode) { mutableStateOf(false) }
    LaunchedEffect(busy) {
        if (busy) return@LaunchedEffect
        try {
            addresses = loadAddresses().filter { it.id.isNotBlank() }
            addressesFailed = false
            addressId = addressId?.takeIf { id -> addresses.orEmpty().any { it.id == id } }
                ?: addresses.orEmpty().firstOrNull()?.id
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            addresses = null
            addressesFailed = true
        }
    }
    val relayCartProducts = cart.items.map { it.product.id }.toSet()
    LaunchedEffect(busy, relayCartProducts) {
        if (busy) return@LaunchedEffect
        relaysFailed = false
        try {
            relayPoints = loadRelayPoints()
            // Keep a chosen but now unavailable relay until the customer explicitly changes destination.
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { relayPoints = null; relaysFailed = true }
    }
    var quoteRefresh by remember { mutableStateOf(0) }
    var quotedCart by remember(addressId, addressRevision, relayPointId, relayPickup, deliveryMode) { mutableStateOf<CartSnapshot?>(null) }
    var quoteLoading by remember { mutableStateOf(false) }
    val updating = busy || pendingLineIds.isNotEmpty()
    val quoteCurrent = quote != null && quotedCart == cart && !updating && !quoteLoading && !quoteFailed
    LaunchedEffect(cart, addressId, addressRevision, relayPointId, relayPickup, deliveryMode, relayPoints, relaysFailed, updating, quoteRefresh) {
        if (updating || (relayPickup && relayPointId == null)) return@LaunchedEffect
        val selected = addressId ?: return@LaunchedEffect
        if (relayPointId != null && (relaysFailed || relayPoints.orEmpty().none { it.id == relayPointId })) {
            quoteFailed = true
            return@LaunchedEffect
        }
        quoteLoading = true
        quoteFailed = false
        try {
            val refreshed = loadQuote(selected, relayPointId, deliveryMode)
            quote = refreshed
            quotedCart = cart
            modes = refreshed.availableDeliveryModes
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            quoteFailed = true
        } finally {
            quoteLoading = false
        }
    }
    Text(appText(Res.string.cart_relay_title), style = MaterialTheme.typography.titleMedium)
    PrimaryTabRow(selectedTabIndex = if (relayPickup) 1 else 0) {
        Tab(selected = !relayPickup, enabled = editingEnabled,
            text = { Text(appText(Res.string.cart_your_address)) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            onClick = { relayPickup = false; relayPointId = null; deliveryMode = null; checkoutError = null })
        Tab(selected = relayPickup, enabled = editingEnabled,
            text = { Text(appText(Res.string.basket_delivery_relay)) },
            icon = { Icon(Icons.Filled.Storefront, contentDescription = null) },
            onClick = { relayPickup = true; deliveryMode = null; checkoutError = null })
    }
    if (relayPickup) {
        Text(appText(Res.string.cart_choose_relay), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (relaysFailed) Text(appText(Res.string.cart_load_failed), color = MaterialTheme.colorScheme.error)
        else if (relayPoints == null) Text(appText(Res.string.cart_loading))
        else if (relayPoints.orEmpty().isEmpty()) Text(appText(Res.string.cart_relay_unavailable))
        for (relay in relayPoints.orEmpty()) {
            OutlinedCard(onClick = { relayPointId = relay.id; deliveryMode = null; checkoutError = null },
                enabled = editingEnabled, modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (relayPointId == relay.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RadioButton(selected = relayPointId == relay.id, onClick = null)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(relay.name, style = MaterialTheme.typography.titleSmall)
                        Text(listOfNotNull(relay.neighborhood, relay.city, relay.landmark).joinToString(", "),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    Text(appText(if (!relayPickup) Res.string.cart_your_address else Res.string.cart_relay_contact), style = MaterialTheme.typography.titleMedium)
    when {
        addressesFailed -> Text(appText(Res.string.cart_load_failed), color = MaterialTheme.colorScheme.error)
        addresses == null -> Text(appText(Res.string.cart_loading))
    }
    OutlinedButton(onClick = { editingAddress = null; addressEditor = true }, enabled = editingEnabled) {
        Icon(Icons.Filled.AddLocationAlt, contentDescription = null); Spacer(Modifier.width(8.dp))
        Text(appText(Res.string.cart_add_address))
    }
    if (addressEditor) DeliveryAddressEditor(editingAddress, onDismiss = { addressEditor = false }) { id, request ->
        val saved = saveAddress(id, request)
        addresses = addresses.orEmpty().filterNot { it.id == saved.id } + saved
        addressesFailed = false
        addressId = saved.id
        addressRevision++
        deliveryMode = null
        checkoutError = null
        addressEditor = false
    }
    for (address in addresses.orEmpty()) {
        OutlinedCard(onClick = { addressId = address.id; deliveryMode = null; checkoutError = null },
            enabled = editingEnabled, modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, if (addressId == address.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RadioButton(selected = address.id == addressId, onClick = null)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(address.label, style = MaterialTheme.typography.titleSmall)
                    Text(listOfNotNull(address.streetHint, address.area, address.city).filter { it.isNotBlank() }.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { editingAddress = address; addressEditor = true }, enabled = editingEnabled) {
                    Icon(Icons.Filled.Edit, appText(Res.string.cart_edit_address) + " · " + address.label)
                }
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
    quote?.let {
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
        if (dev.orestegabo.sequo.core.network.NetworkConfig.Environment == "DEMO") {
            Text("DEMO · No charge. After submitting, open Orders to simulate payment and delivery.", color=MaterialTheme.colorScheme.primary, style=MaterialTheme.typography.bodyMedium)
        }
        val kkiapay = providers?.firstOrNull { it.providerId == "kkiapay" }
        val paymentReady = kkiapay?.paymentAvailable == true && kkiapay.sandbox == reviewedQuote.testOrderAvailable
        LaunchedEffect(reviewedQuote.testOrderAvailable) {
            provider = if (reviewedQuote.testOrderAvailable) "yas_togo" else "kkiapay"
        }
        val choices = if (reviewedQuote.testOrderAvailable) listOf("yas_togo", "kkiapay") else listOf("kkiapay")
        for (id in choices) {
            val manualTest = id == "yas_togo"
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (provider == id) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = .35f))) {
                Row(Modifier.fillMaxWidth().selectable(selected = provider == id, enabled = editingEnabled,
                    role = Role.RadioButton, onClick = { provider = id }).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RadioButton(selected = provider == id, onClick = null, enabled = editingEnabled)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(appText(if (manualTest) Res.string.payment_test_manual else Res.string.payment_togo_methods),
                            style = MaterialTheme.typography.titleSmall)
                        Text(appText(when {
                            manualTest -> Res.string.payment_test_manual_detail
                            providersFailed -> Res.string.checkout_provider_status_failed
                            providers == null -> Res.string.cart_loading
                            paymentReady && reviewedQuote.testOrderAvailable -> Res.string.payment_sandbox_detail
                            paymentReady -> Res.string.payment_kkiapay_detail
                            else -> Res.string.payment_unavailable
                        }), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    val nextStep = when {
        addressesFailed -> Res.string.cart_load_failed
        addresses == null -> Res.string.cart_loading
        addresses.orEmpty().isEmpty() -> Res.string.cart_no_addresses
        addressId == null -> Res.string.cart_choose_address
        relayPickup && relaysFailed -> Res.string.cart_load_failed
        relayPickup && relayPointId == null -> Res.string.cart_choose_relay
        quoteFailed -> Res.string.cart_quote_failed
        !quoteCurrent -> Res.string.cart_updating_totals
        else -> null
    }
    nextStep?.let { Text(appText(it), style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant) }
    if (quoteFailed || addressesFailed || relaysFailed) TextButton(onClick = onRefresh, enabled = editingEnabled) {
        Text(appText(Res.string.cart_refresh))
    }
    Button(
        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp), shape = RoundedCornerShape(16.dp),
        enabled = editingEnabled && quoteCurrent && nextStep == null && (
            (quote?.testOrderAvailable == true && provider == "yas_togo") ||
            providers?.any { it.providerId == "kkiapay" && it.paymentAvailable && it.sandbox == quote?.testOrderAvailable } == true),
        onClick = {
            val reviewedQuote = quote ?: return@Button
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
        Text(appText(if (submitting) Res.string.checkout_submitting else if (quote?.testOrderAvailable == true) Res.string.cart_test_proceed else Res.string.cart_proceed))
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
