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
import dev.orestegabo.sequo.data.*
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
internal fun OrdersContent() {
    var selectedOrder by remember { mutableStateOf<SequoOrder?>(null) }
    var selectedTab by remember { mutableStateOf(OrdersTab.Current) }
    val currentOrders = remember { emptyList<SequoOrder>() }
    val pastOrders = remember { emptyList<SequoOrder>() }
    val visibleOrders = if (selectedTab == OrdersTab.Current) currentOrders else pastOrders
    val returnableCount = currentOrders.count { it.isInsideReturnWindow }
    val orderForDetail = selectedOrder

    if (orderForDetail != null) {
        OrderDetailScreen(
            order = orderForDetail,
            onBack = { selectedOrder = null },
        )
    } else {
        OrderTabs(
            selectedTab = selectedTab,
            currentCount = currentOrders.size,
            pastCount = pastOrders.size,
            onTabSelected = { selectedTab = it },
        )
        //ReturnPolicyNote(returnableCount = returnableCount)
        OrderListHeader(
            title = if (selectedTab == OrdersTab.Current) "Current orders" else "Past orders",
            action = when (selectedTab) {
                OrdersTab.Current -> "$returnableCount returnable"
                OrdersTab.Past -> "${pastOrders.size} closed"
            },
        )
        if (visibleOrders.isEmpty()) {
            SequoErrorPanel(
                kind = SequoErrorKind.OrderSync,
                onRetry = {},
                technicalNote = "Your ${selectedTab.label.lowercase()} orders will appear here after checkout.",
            )
        } else {
            when (selectedTab) {
                OrdersTab.Current -> CurrentOrderBoard(
                    orders = visibleOrders,
                    onOrderSelected = { selectedOrder = it },
                )
                OrdersTab.Past -> PastOrderBoard(
                    orders = visibleOrders,
                    onOrderSelected = { selectedOrder = it },
                )
            }
        }
    }
}

@Composable
private fun CurrentOrderBoard(
    orders: List<SequoOrder>,
    onOrderSelected: (SequoOrder) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        orders.forEach { order ->
            ActiveOrderCard(order = order, onClick = { onOrderSelected(order) })
        }
    }
}

@Composable
private fun PastOrderBoard(
    orders: List<SequoOrder>,
    onOrderSelected: (SequoOrder) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        orders.chunked(2).forEach { rowOrders ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowOrders.forEach { order ->
                    PastOrderReceiptCard(
                        order = order,
                        onClick = { onOrderSelected(order) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowOrders.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

private enum class OrdersTab(val label: String) {
    Current("Current"),
    Past("Past"),
}

@Composable
private fun OrderTabs(
    selectedTab: OrdersTab,
    currentCount: Int,
    pastCount: Int,
    onTabSelected: (OrdersTab) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            OrdersTab.entries.forEach { tab ->
                val count = if (tab == OrdersTab.Current) currentCount else pastCount
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "$count orders",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                )
            }
        }
    }
}
/*
@Composable
private fun ReturnPolicyNote(returnableCount: Int) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(13.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SequoIconMark(Icons.AutoMirrored.Filled.AssignmentReturn, MaterialTheme.colorScheme.primary, Modifier.size(36.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(appText(Res.string.orders_returns_current_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    appText(Res.string.orders_returns_current_detail),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (returnableCount > 0) {
                Text(
                    "$returnableCount open",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}*/

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
        val icon = if (order.isInsideReturnWindow) Icons.AutoMirrored.Filled.AssignmentReturn else Icons.Filled.Schedule
        val label = if (order.isInsideReturnWindow) "Return open 72h" else order.dateLine
        Icon(icon, contentDescription = null, tint = if (order.isInsideReturnWindow) SequoSecondary else accent, modifier = Modifier.size(15.dp))
        Text(
            label,
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
