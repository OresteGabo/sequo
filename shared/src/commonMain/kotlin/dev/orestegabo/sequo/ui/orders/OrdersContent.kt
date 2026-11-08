package dev.orestegabo.sequo.ui.orders

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.catalog.CustomerReturnEligibility
import dev.orestegabo.sequo.core.catalog.CustomerReturnRequest
import dev.orestegabo.sequo.core.catalog.CheckoutSummary
import dev.orestegabo.sequo.core.catalog.CustomerOrderDetails
import dev.orestegabo.sequo.core.catalog.CustomerDeliveryTracking
import dev.orestegabo.sequo.core.catalog.OrderedProduct
import dev.orestegabo.sequo.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import dev.orestegabo.sequo.domain.*
import dev.orestegabo.sequo.feature.settings.appText
import dev.orestegabo.sequo.logic.*
import dev.orestegabo.sequo.model.*
import dev.orestegabo.sequo.theme.*
import dev.orestegabo.sequo.ui.account.*
import dev.orestegabo.sequo.ui.app.*
import dev.orestegabo.sequo.ui.basket.*
import dev.orestegabo.sequo.ui.catalog.*
import dev.orestegabo.sequo.ui.chrome.*
import dev.orestegabo.sequo.ui.components.*
import dev.orestegabo.sequo.ui.home.*
import dev.orestegabo.sequo.ui.markets.*
import dev.orestegabo.sequo.ui.orders.*
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.*

@Composable
internal fun OrdersContent(
    loadCheckouts: suspend () -> List<CheckoutSummary>,
    loadOrders: suspend () -> List<CustomerOrderDetails>,
    sessionKey: String?,
    loadRelayPickups: suspend (String) -> List<dev.orestegabo.sequo.core.catalog.CustomerRelayPickup>,
    renewRelayPickup: suspend (String, String) -> Unit,
    loadReturnEligibility: suspend (String) -> CustomerReturnEligibility,
    loadReturns: suspend (String) -> List<CustomerReturnRequest>,
    submitReturn: suspend (String, String, String) -> CustomerReturnRequest,
) {
    var refresh by remember { mutableStateOf(0) }
    var checkouts by remember(sessionKey) { mutableStateOf(emptyList<CheckoutSummary>()) }
    var orders by remember(sessionKey) { mutableStateOf(emptyList<CustomerOrderDetails>()) }
    var loading by remember(sessionKey) { mutableStateOf(true) }
    var failed by remember(sessionKey) { mutableStateOf(false) }
    LaunchedEffect(refresh, sessionKey) {
        loading = true
        failed = false
        checkouts = emptyList()
        orders = emptyList()
        try {
            val savedCheckouts = loadCheckouts()
            val savedOrders = loadOrders()
            checkouts = savedCheckouts
            orders = savedOrders
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            checkouts = emptyList()
            orders = emptyList()
            failed = true
        } finally {
            loading = false
        }
    }
    TextButton(onClick = { refresh++ }, enabled = !loading) { Text(appText(Res.string.cart_refresh)) }
    if (loading) Text(appText(Res.string.orders_loading))
    if (failed) Text(appText(Res.string.orders_load_failed), color = MaterialTheme.colorScheme.error)

    val completedCheckoutIds = orders.map { it.order.checkoutId }.toSet()
    val pendingRows = checkouts.filter { it.checkoutId !in completedCheckoutIds }.map {
        OrderHistoryRow(it.checkoutId, it.status, it.totalCfa, it.destinationLabel, it.createdAt, it.lines,
            displayReference = it.publicReference?.takeIf(String::isNotBlank) ?: it.checkoutId)
    }
    val orderRows = orders.map { details ->
        val order = details.order
        val destination = details.deliveries.mapNotNull { it.relayDestination?.name }.distinct().takeIf { it.isNotEmpty() }?.joinToString(" / ")
            ?: checkouts.firstOrNull { it.checkoutId == order.checkoutId }?.destinationLabel
        OrderHistoryRow(order.id, order.orderStatus, order.totalCfa, destination, order.createdAt, details.lines, details.deliveries, persistedOrder = true,
            displayReference = order.publicReference?.takeIf(String::isNotBlank) ?: order.id)
    }
    val rows = (pendingRows + orderRows).sortedByDescending { it.createdAt }
    if (!loading && !failed && rows.isEmpty()) Text(appText(Res.string.orders_empty))
    for (row in rows) {
        key(sessionKey, row.id) {
        var expanded by remember { mutableStateOf(false) }
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(appText(orderHistoryStatus(row.status)), style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    Text(formatCfa(row.totalCfa), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
                Text(row.displayReference, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                row.lines.take(if (expanded) row.lines.size else 2).forEach {
                    Text("${it.quantity} × ${it.productName}", style = MaterialTheme.typography.bodyMedium)
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(appText(if (expanded) Res.string.orders_hide_detail else Res.string.orders_detail_title))
                    Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
                }
                if (expanded) {
                HorizontalDivider()
                row.destination?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                row.deliveries.forEach { delivery ->
                    Text(appText(customerDeliveryStatus(delivery.status)), style = MaterialTheme.typography.titleSmall)
                    Text(delivery.publicReference?.takeIf(String::isNotBlank) ?: delivery.deliveryCode, style = MaterialTheme.typography.bodySmall)
                    delivery.deliveryOption?.let { mode -> Text("${appText(deliveryModeName(mode.id))} · ${mode.etaMinMinutes}–${mode.etaMaxMinutes} min · ${formatCfa(mode.deliveryFeeCfa)}") }
                    delivery.relayDestination?.let { relay -> Text(listOfNotNull(relay.name, relay.neighborhood, relay.city).joinToString(" · ")) }
                    delivery.relayDepositedAt?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
                if (row.deliveries.any { it.relayDestination != null }) {
                    key(sessionKey, row.id, refresh) { RelayPickupContent(row.id, loadRelayPickups, renewRelayPickup) }
                }
                if (row.persistedOrder) {
                    key(sessionKey, row.id) {
                        CustomerReturnContent(row.id, refresh, loadReturnEligibility, loadReturns, submitReturn,
                            onRequested = { refresh++ })
                    }
                }
                }
            }
        }
        }
    }
}

@Composable
private fun RelayPickupContent(orderId: String,
    load: suspend (String) -> List<dev.orestegabo.sequo.core.catalog.CustomerRelayPickup>,
    renew: suspend (String, String) -> Unit) {
    var pickups by remember { mutableStateOf<List<dev.orestegabo.sequo.core.catalog.CustomerRelayPickup>?>(null) }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    TextButton(enabled = !busy, onClick = {
        busy = true; failed = false; pickups = null
        scope.launch {
            try { pickups = load(orderId) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { failed = true }
            finally { busy = false }
        }
    }) { Text(appText(Res.string.relay_pickup_show)) }
    if (busy) Text(appText(Res.string.orders_loading))
    if (failed) Text(appText(Res.string.orders_load_failed), color = MaterialTheme.colorScheme.error)
    for (pickup in pickups.orEmpty()) {
        val expiry = pickup.expiresAt?.let { runCatching { kotlin.time.Instant.parse(it) }.getOrNull() }
        var expired by remember(pickup) { mutableStateOf(expiry == null || expiry <= kotlin.time.Clock.System.now()) }
        LaunchedEffect(pickup) {
            if (expiry != null) {
                kotlinx.coroutines.delay((expiry.toEpochMilliseconds() - kotlin.time.Clock.System.now().toEpochMilliseconds()).coerceAtLeast(0))
                expired = true
            }
        }
        Text(pickup.parcelReference?.takeIf(String::isNotBlank) ?: pickup.publicReference?.takeIf(String::isNotBlank) ?: pickup.parcelId, style = MaterialTheme.typography.bodySmall)
        Text(appText(Res.string.relay_pickup_identity))
        if (pickup.credentialState == "READY" && !expired) {
            Text(pickup.numericCode.orEmpty(), style = MaterialTheme.typography.headlineMedium)
            val modules = pickup.qrModules
            if (modules.isNotEmpty()) Canvas(Modifier.size(240.dp)) {
                drawRect(Color.White)
                val cell = size.width / modules.size
                modules.forEachIndexed { y, row -> row.forEachIndexed { x, bit ->
                    if (bit == '1') drawRect(Color.Black, androidx.compose.ui.geometry.Offset(x * cell, y * cell), androidx.compose.ui.geometry.Size(cell, cell))
                } }
            }
            Text(pickup.expiresAt.orEmpty(), style = MaterialTheme.typography.bodySmall)
        } else if (pickup.credentialState != "CLOSED" && pickup.credentialState != "USED") {
            Text(appText(Res.string.relay_pickup_renew_note))
            TextButton(enabled = !busy, onClick = {
                busy = true; failed = false; pickups = null
                scope.launch {
                    try { renew(orderId, pickup.parcelId); pickups = load(orderId) }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { failed = true }
                    finally { busy = false }
                }
            }) { Text(appText(Res.string.relay_pickup_renew)) }
        } else Text(appText(Res.string.relay_pickup_closed))
        pickup.depositedAt?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        pickup.pickedUpAt?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}

private data class OrderHistoryRow(
    val id: String,
    val status: String,
    val totalCfa: Int,
    val destination: String?,
    val createdAt: String,
    val lines: List<OrderedProduct>,
    val deliveries: List<CustomerDeliveryTracking> = emptyList(),
    val persistedOrder: Boolean = false,
    val displayReference: String = id,
)

private fun customerDeliveryStatus(status: String) = when (status) {
    "CREATED", "OFFERED_TO_COURIER" -> Res.string.orders_delivery_offered
    "ACCEPTED_BY_COURIER" -> Res.string.orders_delivery_accepted
    "PICKED_UP_FROM_SELLER" -> Res.string.orders_on_way
    "DELIVERED_TO_CUSTOMER", "RELEASED_BY_RELAY" -> Res.string.orders_delivered
    "DEPOSITED_AT_RELAY" -> Res.string.basket_delivery_relay
    "PROBLEM_REPORTED" -> Res.string.orders_delivery_problem
    "CANCELLED" -> Res.string.basket_payment_cancelled
    else -> Res.string.orders_status_unknown
}

private fun orderHistoryStatus(status: String) = when (status) {
    "AWAITING_WEBHOOK" -> Res.string.orders_awaiting_payment
    "FAILED" -> Res.string.basket_payment_failed
    "CANCELLED" -> Res.string.basket_payment_cancelled
    "COMPLETED", "ACCEPTED_FOR_FULFILLMENT" -> Res.string.orders_in_preparation
    "DELIVERED" -> Res.string.orders_delivered
    "RETURN_REQUESTED" -> Res.string.orders_return_requested
    "REFUNDED" -> Res.string.orders_refunded
    else -> Res.string.orders_status_unknown
}

@Composable
private fun OrderListHeader(title: String, action: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ActiveOrderCard(order: SequoOrder, onClick: () -> Unit) {
    val accent = orderStateColor(order.state)
    val previewProduct = remember(order.id) { order.previewProduct() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f)),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OrderPreviewImage(
                    product = previewProduct,
                    fallbackIcon = order.orderTileIcon,
                    accent = accent,
                    modifier = Modifier.size(width = 76.dp, height = 86.dp),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            order.cardEyebrow(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            order.cardTitle(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    OrderStatusLabel(order.state)
                    OrderContextLine(order = order, accent = accent)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        formatCompactCfa(order.amountCfa),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "View order details",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            OrderProgressLine(order = order)
        }
    }
}

@Composable
private fun PastOrderReceiptCard(
    order: SequoOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = orderStateColor(order.state)
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                SequoIconMark(order.orderTileIcon, accent, Modifier.size(36.dp))
                Text(
                    order.id.removePrefix("SQ-"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                orderTitle(order),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            OrderStatusLabel(order.state)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    formatCompactCfa(order.amountCfa),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    order.dateLine.shortOrderTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun OrderProgressLine(order: SequoOrder) {
    val accent = orderStateColor(order.state)
    val progress = order.progressFraction
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.44f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(999.dp))
                    .background(accent),
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                order.progressLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                order.shortProgressPosition,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun OrderContextLine(order: SequoOrder, accent: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Schedule, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
        Text(
            order.dateLine,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun OrderPreviewImage(
    product: SequoProduct?,
    fallbackIcon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.14f)),
    ) {
        Box(Modifier.fillMaxSize()) {
            if (product != null) {
                Image(
                    painter = painterResource(productImageResource(product)),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.18f),
                            ),
                        ),
                )
                if (product.isCameraVerified) {
                    PhotoAuthenticityBadge(
                        product = product,
                        compact = true,
                        onDark = true,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(5.dp),
                    )
                }
            } else {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.align(Alignment.Center).size(30.dp),
                )
            }
        }
    }
}

@Composable
private fun OrderStatusLabel(state: SequoOrderState) {
    val accent = orderStateColor(state)
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Text(
            state.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun SequoOrder.previewProduct(): SequoProduct? {
    val mainItemName = items.firstOrNull()?.name ?: return null
    return SequoProduct(
        name = mainItemName,
        detail = sellers.firstOrNull().orEmpty(),
        priceCfa = amountCfa,
        label = "Order",
        optionHint = note,
    )
}

private fun SequoOrder.cardTitle(): String {
    val mainItemName = items.firstOrNull()?.name ?: "Order"
    val remainingCount = itemCount() - (items.firstOrNull()?.quantity ?: 0)
    return if (remainingCount > 0) {
        "$mainItemName +$remainingCount more"
    } else {
        mainItemName
    }
}

private fun SequoOrder.cardEyebrow(): String =
    if (sellers.size == 1) {
        "$id / ${sellers.first()}"
    } else {
        "$id / ${sellers.size} sellers"
    }

private val SequoOrder.belongsInCurrentOrders: Boolean
    get() = state !in pastOrderStates || isInsideReturnWindow || state in activeReturnStates

private val SequoOrder.progressSteps: List<Boolean>
    get() {
        val currentIndex = when (state) {
            SequoOrderState.Ordered -> 0
            SequoOrderState.PaymentPending -> 0
            SequoOrderState.Paid -> 1
            SequoOrderState.MerchantAccepted -> 2
            SequoOrderState.MerchantDeclined -> 1
            SequoOrderState.Preparing -> 2
            SequoOrderState.ReadyForPickup -> 3
            SequoOrderState.PickedUp -> 3
            SequoOrderState.InDelivery -> 4
            SequoOrderState.DeliveryAttempted -> 4
            SequoOrderState.Delivered -> 5
            SequoOrderState.ReturnRequested -> 3
            SequoOrderState.ReturnInInspection -> 4
            SequoOrderState.RefundIssued -> 5
            SequoOrderState.ReturnRejected -> 5
            SequoOrderState.CancelledByCustomer -> 1
            SequoOrderState.CancelledByMerchant -> 1
            SequoOrderState.CancelledBySequo -> 2
        }
        return List(5) { index -> index < currentIndex }
    }

private val SequoOrder.progressFraction: Float
    get() = when (state) {
        SequoOrderState.Ordered,
        SequoOrderState.PaymentPending -> 0.12f
        SequoOrderState.Paid -> 0.25f
        SequoOrderState.MerchantAccepted,
        SequoOrderState.Preparing -> 0.46f
        SequoOrderState.ReadyForPickup,
        SequoOrderState.PickedUp -> 0.66f
        SequoOrderState.InDelivery,
        SequoOrderState.DeliveryAttempted -> 0.84f
        SequoOrderState.Delivered,
        SequoOrderState.RefundIssued,
        SequoOrderState.ReturnRejected -> 1f
        SequoOrderState.ReturnRequested,
        SequoOrderState.ReturnInInspection -> 0.78f
        SequoOrderState.MerchantDeclined,
        SequoOrderState.CancelledByCustomer,
        SequoOrderState.CancelledByMerchant,
        SequoOrderState.CancelledBySequo -> 1f
    }

private val SequoOrder.progressLabel: String
    get() = when (state) {
        SequoOrderState.Ordered -> "Order received"
        SequoOrderState.PaymentPending -> "Waiting for payment"
        SequoOrderState.Paid -> "Paid, waiting for seller"
        SequoOrderState.MerchantAccepted -> "Seller accepted"
        SequoOrderState.MerchantDeclined -> "Seller declined"
        SequoOrderState.Preparing -> "Being prepared"
        SequoOrderState.ReadyForPickup -> "Ready for pickup"
        SequoOrderState.PickedUp -> "Picked up by Sequo"
        SequoOrderState.InDelivery -> "On the way"
        SequoOrderState.DeliveryAttempted -> "Delivery attempt made"
        SequoOrderState.Delivered -> "Delivered"
        SequoOrderState.ReturnRequested -> "Return requested"
        SequoOrderState.ReturnInInspection -> "Return under inspection"
        SequoOrderState.RefundIssued -> "Refund issued"
        SequoOrderState.ReturnRejected -> "Return rejected"
        SequoOrderState.CancelledByCustomer,
        SequoOrderState.CancelledByMerchant,
        SequoOrderState.CancelledBySequo -> "Order closed"
    }

private val SequoOrder.shortProgressPosition: String
    get() = when (state) {
        SequoOrderState.Ordered,
        SequoOrderState.PaymentPending -> "1/5"
        SequoOrderState.Paid -> "2/5"
        SequoOrderState.MerchantAccepted,
        SequoOrderState.Preparing -> "3/5"
        SequoOrderState.ReadyForPickup,
        SequoOrderState.PickedUp -> "4/5"
        SequoOrderState.InDelivery,
        SequoOrderState.DeliveryAttempted -> "5/5"
        SequoOrderState.Delivered -> "Done"
        SequoOrderState.ReturnRequested,
        SequoOrderState.ReturnInInspection -> "Return"
        SequoOrderState.RefundIssued -> "Refund"
        SequoOrderState.ReturnRejected -> "Closed"
        SequoOrderState.MerchantDeclined,
        SequoOrderState.CancelledByCustomer,
        SequoOrderState.CancelledByMerchant,
        SequoOrderState.CancelledBySequo -> "Closed"
    }

private val SequoOrder.isInsideReturnWindow: Boolean
    get() = state == SequoOrderState.Delivered &&
        (note.contains("return open", ignoreCase = true) ||
            dateLine.contains("today", ignoreCase = true) ||
            dateLine.contains("yesterday", ignoreCase = true))

private val activeReturnStates = setOf(
    SequoOrderState.ReturnRequested,
    SequoOrderState.ReturnInInspection,
)

private val pastOrderStates = setOf(
    SequoOrderState.Delivered,
    SequoOrderState.RefundIssued,
    SequoOrderState.ReturnRejected,
    SequoOrderState.CancelledByCustomer,
    SequoOrderState.CancelledByMerchant,
    SequoOrderState.CancelledBySequo,
)

private val SequoOrder.orderTileIcon: ImageVector
    get() = when {
        state in activeReturnStates || isInsideReturnWindow -> Icons.AutoMirrored.Filled.AssignmentReturn
        state == SequoOrderState.InDelivery -> Icons.Filled.LocalShipping
        state.shouldShowPickupCode -> Icons.Filled.Lock
        state == SequoOrderState.Paid || state == SequoOrderState.PaymentPending -> Icons.Filled.Payments
        state in pastOrderStates -> Icons.AutoMirrored.Filled.ReceiptLong
        else -> Icons.Filled.Inventory2
    }

private fun String.shortOrderTime(): String =
    when {
        contains("min", ignoreCase = true) -> substringBefore(" ").takeIf { it.isNotBlank() }?.let { "${it}m" } ?: this
        contains("today", ignoreCase = true) -> "Today"
        contains("yesterday", ignoreCase = true) -> "Yday"
        contains("monday", ignoreCase = true) -> "Mon"
        contains("friday", ignoreCase = true) -> "Fri"
        contains("now", ignoreCase = true) -> "Now"
        else -> substringBefore(" ").take(8)
    }

@Composable
internal fun OrderDetailScreen(order: SequoOrder, onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(OrderDetailTab.Status) }
    val timelineEvents = orderTimelineEvents(order)

    OrderDetailAppBar(order = order, onBack = onBack)
    SequoIntroCard(
        eyebrow = "Order detail",
        title = "Order ${order.id}",
        subtitle = "${order.state.label} / ${order.dateLine}",
    )
    OrderDetailTabs(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
    )
    when (selectedTab) {
        OrderDetailTab.Status -> {
            SequoSectionCard(title = "Timeline") {
                OrderTimeline(events = timelineEvents, timelineKey = order.id)
            }
            if (order.state.shouldShowPickupCode) {
                SequoSectionCard(title = "Pickup validation", action = order.pickupCode) {
                    PickupCodePanel(order)
                }
            }
            SequoSectionCard(title = "Next step", action = orderNextStepTag(order.state)) {
                RuleRow(orderNextStepTitle(order.state), orderNextStepDetail(order.state))
            }
            ReturnRequestPanel(order)
        }
        OrderDetailTab.Details -> {
            SequoSectionCard(title = "Receipt", action = order.state.label) {
                ValueRow("Amount", formatCfa(order.amountCfa), strong = true)
                ValueRow("Items", "${order.itemCount()} item${if (order.itemCount() == 1) "" else "s"}")
                ValueRow("Payment", order.paymentMethod)
                OrderStatusPill(order.state)
            }
            SequoSectionCard(title = "Sellers and items", action = "${order.itemCount()} items") {
                RuleRow("Seller${if (order.sellers.size == 1) "" else "s"}", order.sellers.joinToString(" + "))
                order.items.forEach { item ->
                    OrderItemDetailRow(item)
                }
            }
        }
    }
}

@Composable
private fun ReturnRequestPanel(order: SequoOrder) {
    if (order.state == SequoOrderState.Delivered || order.state in activeReturnStates) {
        Text(appText(Res.string.returns_open_orders))
    }
}

private enum class CustomerReturnStatus {
    Requested,
    DropoffPending,
    DroppedAtRelay,
    CollectedBySequo,
    UnderInspection,
    RefundProcessing,
    RefundCompleted,
    Rejected,
    Expired,
}

@Composable
private fun ReturnStatusTracker(order: SequoOrder, requestCreated: Boolean) {
    val statuses = remember(order.id, order.state, requestCreated, order.isInsideReturnWindow) {
        returnStatusesFor(order, requestCreated)
    }
    if (statuses.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            appText(Res.string.orders_return_status_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            appText(Res.string.orders_return_status_local_demo),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        statuses.forEachIndexed { index, status ->
            val isCurrent = index == statuses.lastIndex
            ReturnStatusRow(status = status, isCurrent = isCurrent)
        }
    }
}

@Composable
private fun ReturnStatusRow(status: CustomerReturnStatus, isCurrent: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(status.title(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(status.detail(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun returnStatusesFor(order: SequoOrder, requestCreated: Boolean): List<CustomerReturnStatus> =
    when (order.state) {
        SequoOrderState.ReturnRequested -> listOf(
            CustomerReturnStatus.Requested,
            CustomerReturnStatus.DropoffPending,
        )
        SequoOrderState.ReturnInInspection -> listOf(
            CustomerReturnStatus.Requested,
            CustomerReturnStatus.DropoffPending,
            CustomerReturnStatus.DroppedAtRelay,
            CustomerReturnStatus.CollectedBySequo,
            CustomerReturnStatus.UnderInspection,
        )
        SequoOrderState.RefundIssued -> listOf(
            CustomerReturnStatus.Requested,
            CustomerReturnStatus.DropoffPending,
            CustomerReturnStatus.DroppedAtRelay,
            CustomerReturnStatus.CollectedBySequo,
            CustomerReturnStatus.UnderInspection,
            CustomerReturnStatus.RefundProcessing,
            CustomerReturnStatus.RefundCompleted,
        )
        SequoOrderState.ReturnRejected -> listOf(
            CustomerReturnStatus.Requested,
            CustomerReturnStatus.DropoffPending,
            CustomerReturnStatus.DroppedAtRelay,
            CustomerReturnStatus.CollectedBySequo,
            CustomerReturnStatus.UnderInspection,
            CustomerReturnStatus.Rejected,
        )
        SequoOrderState.Delivered -> when {
            requestCreated -> listOf(CustomerReturnStatus.Requested, CustomerReturnStatus.DropoffPending)
            !order.isInsideReturnWindow -> listOf(CustomerReturnStatus.Expired)
            else -> emptyList()
        }
        else -> emptyList()
    }

@Composable
private fun CustomerReturnStatus.title(): String =
    when (this) {
        CustomerReturnStatus.Requested -> appText(Res.string.orders_return_status_requested)
        CustomerReturnStatus.DropoffPending -> appText(Res.string.orders_return_status_dropoff_pending)
        CustomerReturnStatus.DroppedAtRelay -> appText(Res.string.orders_return_status_dropped_relay)
        CustomerReturnStatus.CollectedBySequo -> appText(Res.string.orders_return_status_collected_sequo)
        CustomerReturnStatus.UnderInspection -> appText(Res.string.orders_return_status_under_inspection)
        CustomerReturnStatus.RefundProcessing -> appText(Res.string.orders_return_status_refund_processing)
        CustomerReturnStatus.RefundCompleted -> appText(Res.string.orders_return_status_refund_completed)
        CustomerReturnStatus.Rejected -> appText(Res.string.orders_return_status_rejected)
        CustomerReturnStatus.Expired -> appText(Res.string.orders_return_status_expired)
    }

@Composable
private fun CustomerReturnStatus.detail(): String =
    when (this) {
        CustomerReturnStatus.Requested -> appText(Res.string.orders_return_status_requested_detail)
        CustomerReturnStatus.DropoffPending -> appText(Res.string.orders_return_status_dropoff_pending_detail)
        CustomerReturnStatus.DroppedAtRelay -> appText(Res.string.orders_return_status_dropped_relay_detail)
        CustomerReturnStatus.CollectedBySequo -> appText(Res.string.orders_return_status_collected_sequo_detail)
        CustomerReturnStatus.UnderInspection -> appText(Res.string.orders_return_status_under_inspection_detail)
        CustomerReturnStatus.RefundProcessing -> appText(Res.string.orders_return_status_refund_processing_detail)
        CustomerReturnStatus.RefundCompleted -> appText(Res.string.orders_return_status_refund_completed_detail)
        CustomerReturnStatus.Rejected -> appText(Res.string.orders_return_status_rejected_detail)
        CustomerReturnStatus.Expired -> appText(Res.string.orders_return_status_expired_detail)
    }

private enum class ReturnReason {
    WrongProduct,
    Damaged,
    MissingItem,
    VariantIssue,
    NotDescribed,
}

@Composable
private fun ReturnReason.label(): String =
    when (this) {
        ReturnReason.WrongProduct -> appText(Res.string.orders_return_wrong_product)
        ReturnReason.Damaged -> appText(Res.string.orders_return_damaged)
        ReturnReason.MissingItem -> appText(Res.string.orders_return_missing_item)
        ReturnReason.VariantIssue -> appText(Res.string.orders_return_variant_issue)
        ReturnReason.NotDescribed -> appText(Res.string.orders_return_not_described)
    }
