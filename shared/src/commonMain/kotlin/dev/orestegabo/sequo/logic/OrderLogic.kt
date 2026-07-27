package dev.orestegabo.sequo.logic

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

internal fun baseDelivery(distanceKm: Double): Int =
    calculateDeliveryPricing(DeliveryPricingInput(distanceKm)).baseFeeCfa

internal fun SequoOrder.itemCount(): Int =
    items.sumOf { it.quantity }

internal fun orderTitle(order: SequoOrder): String =
    if (order.itemCount() == 1) {
        order.items.first().name
    } else if (order.sellers.size == 1) {
        "${order.itemCount()} items from ${order.sellers.first()}"
    } else {
        "Grouped Sequo package"
    }

internal fun sellerSummary(order: SequoOrder): String =
    if (order.sellers.size == 1) {
        order.sellers.first()
    } else {
        "${order.sellers.size} sellers: ${order.sellers.take(2).joinToString(" + ")}"
    }

internal fun orderMetaLine(order: SequoOrder): String =
    "${formatCfa(order.amountCfa)} / ${order.itemCount()} item${if (order.itemCount() == 1) "" else "s"} / ${order.paymentMethod}"

internal fun orderTimelineEvents(order: SequoOrder): List<OrderTimelineEvent> {
    val states = orderTimelineStates(order.state)
    return states.mapIndexed { index, state ->
        val isCurrent = state == order.state
        OrderTimelineEvent(
            state = state,
            time = orderTimelineTime(order, state, index, states.lastIndex, isCurrent),
            detail = orderTimelineDetail(order, state, isCurrent),
            isCurrent = isCurrent,
        )
    }.asReversed()
}

internal fun orderTimelineStates(state: SequoOrderState): List<SequoOrderState> {
    val acceptedFlow = listOf(
        SequoOrderState.Ordered,
        SequoOrderState.Paid,
        SequoOrderState.MerchantAccepted,
    )
    val preparedFlow = acceptedFlow + SequoOrderState.Preparing
    val deliveryFlow = preparedFlow + listOf(
        SequoOrderState.ReadyForPickup,
        SequoOrderState.PickedUp,
        SequoOrderState.InDelivery,
    )
    val deliveredFlow = deliveryFlow + SequoOrderState.Delivered
    val returnFlow = deliveredFlow + SequoOrderState.ReturnRequested

    return when (state) {
        SequoOrderState.Ordered -> listOf(SequoOrderState.Ordered)
        SequoOrderState.PaymentPending -> listOf(SequoOrderState.Ordered, SequoOrderState.PaymentPending)
        SequoOrderState.Paid -> listOf(SequoOrderState.Ordered, SequoOrderState.Paid)
        SequoOrderState.MerchantAccepted -> acceptedFlow
        SequoOrderState.MerchantDeclined -> listOf(SequoOrderState.Ordered, SequoOrderState.Paid, SequoOrderState.MerchantDeclined)
        SequoOrderState.Preparing -> preparedFlow
        SequoOrderState.ReadyForPickup -> preparedFlow + SequoOrderState.ReadyForPickup
        SequoOrderState.PickedUp -> preparedFlow + SequoOrderState.ReadyForPickup + SequoOrderState.PickedUp
        SequoOrderState.InDelivery -> deliveryFlow
        SequoOrderState.DeliveryAttempted -> deliveryFlow + SequoOrderState.DeliveryAttempted
        SequoOrderState.Delivered -> deliveredFlow
        SequoOrderState.ReturnRequested -> returnFlow
        SequoOrderState.ReturnInInspection -> returnFlow + SequoOrderState.ReturnInInspection
        SequoOrderState.RefundIssued -> returnFlow + SequoOrderState.ReturnInInspection + SequoOrderState.RefundIssued
        SequoOrderState.ReturnRejected -> returnFlow + SequoOrderState.ReturnInInspection + SequoOrderState.ReturnRejected
        SequoOrderState.CancelledByCustomer -> listOf(SequoOrderState.Ordered, SequoOrderState.CancelledByCustomer)
        SequoOrderState.CancelledByMerchant -> listOf(SequoOrderState.Ordered, SequoOrderState.Paid, SequoOrderState.CancelledByMerchant)
        SequoOrderState.CancelledBySequo -> preparedFlow + SequoOrderState.CancelledBySequo
    }
}

internal fun orderTimelineTime(
    order: SequoOrder,
    state: SequoOrderState,
    index: Int,
    lastIndex: Int,
    isCurrent: Boolean,
): String =
    if (isCurrent) {
        currentTimelineTime(order, state)
    } else {
        val dayPrefix = pastTimelineDayPrefix(order)
        val time = timelineFallbackTime(state)
        if (dayPrefix == null || index < lastIndex - 1) {
            time
        } else {
            "$dayPrefix $time"
        }
    }

internal fun currentTimelineTime(order: SequoOrder, state: SequoOrderState): String {
    val explicitTime = order.dateLine.extractClockTime()
    return when {
        explicitTime != null && order.dateLine.contains("yesterday", ignoreCase = true) -> "Yesterday $explicitTime"
        explicitTime != null && order.dateLine.contains("friday", ignoreCase = true) -> "Friday $explicitTime"
        explicitTime != null -> explicitTime
        state == SequoOrderState.InDelivery -> "In progress"
        order.dateLine.contains("now", ignoreCase = true) -> "Now"
        order.dateLine.contains("min", ignoreCase = true) -> "Now"
        order.dateLine.contains("today", ignoreCase = true) -> "Today ${timelineFallbackTime(state)}"
        order.dateLine.contains("monday", ignoreCase = true) -> "Monday ${timelineFallbackTime(state)}"
        else -> timelineFallbackTime(state)
    }
}

internal fun timelineFallbackTime(state: SequoOrderState): String =
    when (state) {
        SequoOrderState.Ordered -> "11:52"
        SequoOrderState.PaymentPending -> "11:54"
        SequoOrderState.Paid -> "11:58"
        SequoOrderState.MerchantAccepted -> "12:09"
        SequoOrderState.MerchantDeclined -> "12:12"
        SequoOrderState.Preparing -> "12:18"
        SequoOrderState.ReadyForPickup -> "12:31"
        SequoOrderState.PickedUp -> "12:44"
        SequoOrderState.InDelivery -> "12:51"
        SequoOrderState.DeliveryAttempted -> "13:08"
        SequoOrderState.Delivered -> "18:40"
        SequoOrderState.ReturnRequested -> "10:24"
        SequoOrderState.ReturnInInspection -> "14:12"
        SequoOrderState.RefundIssued -> "16:05"
        SequoOrderState.ReturnRejected -> "16:05"
        SequoOrderState.CancelledByCustomer -> "12:02"
        SequoOrderState.CancelledByMerchant -> "12:16"
        SequoOrderState.CancelledBySequo -> "12:27"
    }

internal fun pastTimelineDayPrefix(order: SequoOrder): String? =
    when {
        order.dateLine.contains("yesterday", ignoreCase = true) -> "Yesterday"
        order.dateLine.contains("friday", ignoreCase = true) -> "Friday"
        order.dateLine.contains("monday", ignoreCase = true) -> "Monday"
        else -> null
    }

internal fun String.extractClockTime(): String? {
    val index = windowed(size = 5, step = 1).indexOfFirst { candidate ->
        candidate[0].isDigit() &&
            candidate[1].isDigit() &&
            candidate[2] == ':' &&
            candidate[3].isDigit() &&
            candidate[4].isDigit()
    }
    return if (index >= 0) substring(index, index + 5) else null
}

internal fun orderTimelineDetail(order: SequoOrder, state: SequoOrderState, isCurrent: Boolean): String =
    if (isCurrent) {
        "${order.dateLine}. ${order.note}"
    } else {
        when (state) {
            SequoOrderState.Ordered -> "Order was created for ${sellerSummary(order)}."
            SequoOrderState.PaymentPending -> "Payment validation was requested through ${order.paymentMethod}."
            SequoOrderState.Paid -> "Payment was validated with ${order.paymentMethod} before merchant fulfillment."
            SequoOrderState.MerchantAccepted -> "${order.sellers.first()} confirmed product availability."
            SequoOrderState.MerchantDeclined -> "Merchant could not provide every requested product."
            SequoOrderState.Preparing -> "Merchant prepared the order and submitted required live camera checks."
            SequoOrderState.ReadyForPickup -> "Package was sealed and made ready for rider pickup."
            SequoOrderState.PickedUp -> "Rider validated pickup and collected the package."
            SequoOrderState.InDelivery -> "Package moved onto the customer delivery route."
            SequoOrderState.DeliveryAttempted -> "Rider attempted delivery and needed customer action."
            SequoOrderState.Delivered -> "Order was handed over at the delivery address."
            SequoOrderState.ReturnRequested -> "Customer return was dropped at a Point de Relai."
            SequoOrderState.ReturnInInspection -> "Sequo inspection started for the returned product."
            SequoOrderState.RefundIssued -> "Refund was released to the original mobile money method."
            SequoOrderState.ReturnRejected -> "Return was rejected after inspection."
            SequoOrderState.CancelledByCustomer -> "Customer cancelled before fulfillment completed."
            SequoOrderState.CancelledByMerchant -> "Merchant cancelled because fulfillment was not possible."
            SequoOrderState.CancelledBySequo -> "Sequo stopped the order before pickup could be completed."
        }
    }

internal val SequoOrderState.shouldShowPickupCode: Boolean
    get() = when (this) {
        SequoOrderState.MerchantAccepted,
        SequoOrderState.Preparing,
        SequoOrderState.ReadyForPickup -> true
        SequoOrderState.Ordered,
        SequoOrderState.PaymentPending,
        SequoOrderState.Paid,
        SequoOrderState.MerchantDeclined,
        SequoOrderState.PickedUp,
        SequoOrderState.InDelivery,
        SequoOrderState.DeliveryAttempted,
        SequoOrderState.Delivered,
        SequoOrderState.ReturnRequested,
        SequoOrderState.ReturnInInspection,
        SequoOrderState.RefundIssued,
        SequoOrderState.ReturnRejected,
        SequoOrderState.CancelledByCustomer,
        SequoOrderState.CancelledByMerchant,
        SequoOrderState.CancelledBySequo -> false
    }

internal fun orderNextStepTag(state: SequoOrderState): String =
    when (state) {
        SequoOrderState.Ordered -> "order"
        SequoOrderState.PaymentPending -> "payment"
        SequoOrderState.Paid -> "paid"
        SequoOrderState.MerchantAccepted -> "accepted"
        SequoOrderState.MerchantDeclined -> "declined"
        SequoOrderState.Preparing -> "photo"
        SequoOrderState.ReadyForPickup -> "ready"
        SequoOrderState.PickedUp -> "pickup"
        SequoOrderState.InDelivery -> "route"
        SequoOrderState.DeliveryAttempted -> "action"
        SequoOrderState.Delivered -> "72h"
        SequoOrderState.ReturnRequested -> "relay"
        SequoOrderState.ReturnInInspection -> "inspect"
        SequoOrderState.RefundIssued -> "refund"
        SequoOrderState.ReturnRejected -> "support"
        SequoOrderState.CancelledByCustomer -> "closed"
        SequoOrderState.CancelledByMerchant -> "closed"
        SequoOrderState.CancelledBySequo -> "closed"
    }

internal fun orderNextStepTitle(state: SequoOrderState): String =
    when (state) {
        SequoOrderState.Ordered -> "Complete payment"
        SequoOrderState.PaymentPending -> "Validate payment"
        SequoOrderState.Paid -> "Merchant checks availability"
        SequoOrderState.MerchantAccepted -> "Prepare the package"
        SequoOrderState.MerchantDeclined -> "Payment reversal"
        SequoOrderState.Preparing -> "Live photo check"
        SequoOrderState.ReadyForPickup -> "Assign a rider"
        SequoOrderState.PickedUp -> "Route consolidation"
        SequoOrderState.InDelivery -> "Follow the rider"
        SequoOrderState.DeliveryAttempted -> "Resolve delivery"
        SequoOrderState.Delivered -> "Return window"
        SequoOrderState.ReturnRequested -> "Drop-off received"
        SequoOrderState.ReturnInInspection -> "Inspection in progress"
        SequoOrderState.RefundIssued -> "Refund triggered"
        SequoOrderState.ReturnRejected -> "Return rejected"
        SequoOrderState.CancelledByCustomer -> "Order closed"
        SequoOrderState.CancelledByMerchant -> "Order closed"
        SequoOrderState.CancelledBySequo -> "Order closed"
    }

internal fun orderNextStepDetail(state: SequoOrderState): String =
    when (state) {
        SequoOrderState.Ordered -> "Choose Yas Togo or Moov Africa and validate payment before the merchant receives the order."
        SequoOrderState.PaymentPending -> "Complete Yas Togo or Moov Africa validation before Sequo finishes the order."
        SequoOrderState.Paid -> "Sequo asks the merchant to confirm product availability before preparation begins."
        SequoOrderState.MerchantAccepted -> "The merchant starts preparation and uploads required real-time camera photos."
        SequoOrderState.MerchantDeclined -> "Sequo reverses or adjusts the payment because the merchant cannot fulfill the order."
        SequoOrderState.Preparing -> "Sequo waits for the required real-time seller camera photo before releasing the rider."
        SequoOrderState.ReadyForPickup -> "A rider is assigned and must enter the pickup code when arriving at the merchant."
        SequoOrderState.PickedUp -> "Sequo can consolidate eligible grouped-seller orders before sending the rider to you."
        SequoOrderState.InDelivery -> "Keep the app open for route updates while the rider moves toward your delivery address."
        SequoOrderState.DeliveryAttempted -> "Confirm address details or contact support so the rider can complete or reschedule delivery."
        SequoOrderState.Delivered -> "Eligible non-food products can be dropped at a Point de Relai within 72 hours."
        SequoOrderState.ReturnRequested -> "The Point de Relai has received your return and Sequo will start inspection."
        SequoOrderState.ReturnInInspection -> "Sequo inspects the returned product; refund starts automatically if accepted."
        SequoOrderState.RefundIssued -> "The refund has been released to the original mobile money payment method."
        SequoOrderState.ReturnRejected -> "Review the inspection reason and contact support if something looks wrong."
        SequoOrderState.CancelledByCustomer -> "No pickup code is active; any eligible reversal follows the original payment method."
        SequoOrderState.CancelledByMerchant -> "No pickup code is active; Sequo reverses or adjusts the payment after merchant cancellation."
        SequoOrderState.CancelledBySequo -> "No pickup code is active; any eligible reversal follows the original payment method."
    }

internal fun pickupCodeFor(orderId: String): String {
    val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    var value = orderId.fold(0) { accumulator, character ->
        ((accumulator * 31) + character.code).and(0x7fffffff)
    }
    return buildString {
        repeat(6) { index ->
            value = ((value * 1103515245) + 12345 + index).and(0x7fffffff)
            append(alphabet[value % alphabet.length])
        }
    }
}

internal fun orderStateColor(state: SequoOrderState): Color =
    when (state) {
        SequoOrderState.Ordered -> SequoSecondary
        SequoOrderState.PaymentPending -> SequoSecondary
        SequoOrderState.Paid -> SequoPrimary
        SequoOrderState.MerchantAccepted -> SequoPrimary
        SequoOrderState.MerchantDeclined -> Color(0xFF9A5A4E)
        SequoOrderState.Preparing -> SequoAccent
        SequoOrderState.ReadyForPickup -> SequoPrimary
        SequoOrderState.PickedUp -> SequoPrimary
        SequoOrderState.InDelivery -> SequoPrimary
        SequoOrderState.DeliveryAttempted -> SequoSecondary
        SequoOrderState.Delivered -> SequoPrimary
        SequoOrderState.ReturnRequested -> SequoSecondary
        SequoOrderState.ReturnInInspection -> SequoSecondary
        SequoOrderState.RefundIssued -> SequoAccent
        SequoOrderState.ReturnRejected -> Color(0xFF9A5A4E)
        SequoOrderState.CancelledByCustomer -> Color(0xFF9A5A4E)
        SequoOrderState.CancelledByMerchant -> Color(0xFF9A5A4E)
        SequoOrderState.CancelledBySequo -> Color(0xFF9A5A4E)
    }

internal fun formatDistance(km: Double): String {
    val tenths = (km * 10).roundToInt()
    return if (tenths % 10 == 0) "${tenths / 10} km" else "${tenths / 10}.${tenths % 10} km"
}

internal fun formatCfa(amount: Int): String =
    amount.toString().reversed().chunked(3).joinToString(" ").reversed() + " CFA"
