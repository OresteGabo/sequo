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
    val activeOrder = recentOrders.first()
    val orderForDetail = selectedOrder

    if (orderForDetail != null) {
        OrderDetailScreen(
            order = orderForDetail,
            onBack = { selectedOrder = null },
        )
    } else {
        SequoAppBar(
            title = "Orders",
            subtitle = "Live route and returns",
            leadingIcon = Icons.AutoMirrored.Filled.ReceiptLong,
            actions = listOf(
                AppBarAction(Icons.Filled.SupportAgent, "Contact support"),
                AppBarAction(Icons.Filled.Map, "Open route", emphasized = true),
            )
        )
        SequoStatusStrip(
            icon = Icons.Filled.Map,
            title = if (activeOrder.state.shouldShowPickupCode) "Pickup validation required" else "Delivery route active",
            detail = if (activeOrder.state.shouldShowPickupCode) {
                "Rider must enter ${activeOrder.pickupCode} at ${activeOrder.sellers.first()} before the package leaves."
            } else {
                "${activeOrder.id} is ${activeOrder.state.label.lowercase()} with ${sellerSummary(activeOrder)}."
            },
            tag = "9 min",
        )
        SequoIntroCard(
            eyebrow = "Orders",
            title = "SQ-2419 is moving through Tokoin.",
            subtitle = if (activeOrder.state.shouldShowPickupCode) {
                "${activeOrder.sellers.first()} is sealed, payment is validated, and pickup code ${activeOrder.pickupCode} confirms the rider collected the right package."
            } else {
                "${activeOrder.sellers.first()} has already cleared pickup validation, so only route and delivery updates stay visible."
            },
        )
        SequoSectionCard(title = "Live progress", action = "9 min") {
            StepRow("Paid with Yas Togo", active = false, detail = "12:18")
            StepRow("Seller camera photo approved", active = false, detail = "12:21")
            StepRow("Thermal seal applied", active = false, detail = "12:27")
            StepRow("Rider approaching Tokoin", active = true, detail = "now")
        }
        SequoSectionCard(title = "Recent orders", action = "${recentOrders.size} receipts") {
            recentOrders.forEach { order ->
                OrderMemory(
                    order = order,
                    onClick = { selectedOrder = order },
                )
            }
        }
        ReturnHubCard()
    }
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
