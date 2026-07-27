package dev.orestegabo.sequo.model

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

internal enum class SequoSection(val label: String, val icon: ImageVector) {
    Markets("Markets", Icons.Filled.Storefront),
    Basket("Basket", Icons.Filled.ShoppingBasket),
    Home("Sequo", Icons.Filled.Home),
    Orders("Orders", Icons.AutoMirrored.Filled.ReceiptLong),
    Account("Account", Icons.Filled.Person),
}

internal val sequoPrimaryDestinations = listOf(
    SequoSection.Markets,
    SequoSection.Basket,
    SequoSection.Home,
    SequoSection.Orders,
    SequoSection.Account,
)

internal data class SequoProduct(
    val name: String,
    val detail: String,
    val priceCfa: Int,
    val label: String,
    val optionHint: String,
    val bargainNote: String? = null,
)

internal data class SequoShop(
    val name: String,
    val area: String,
    val kind: String,
    val distanceKm: Double,
    val eta: String,
    val photoStatus: String,
    val openStatus: String,
    val rating: String,
    val consolidation: String,
    val products: List<SequoProduct>,
)

internal data class SequoShopType(
    val key: String,
    val title: String,
    val supportLabel: String,
    val icon: ImageVector,
    val accent: Color,
)

internal data class BasketEntry(
    val shop: SequoShop,
    val product: SequoProduct,
    val quantity: Int,
)

internal enum class SequoOrderState(val label: String) {
    Ordered("Ordered"),
    PaymentPending("Payment pending"),
    Paid("Paid"),
    MerchantAccepted("Merchant accepted"),
    MerchantDeclined("Merchant declined"),
    Preparing("Preparing"),
    ReadyForPickup("Ready for pickup"),
    PickedUp("Picked up"),
    InDelivery("In delivery"),
    DeliveryAttempted("Delivery attempted"),
    Delivered("Delivered"),
    ReturnRequested("Return requested"),
    ReturnInInspection("Return inspection"),
    RefundIssued("Refund issued"),
    ReturnRejected("Return rejected"),
    CancelledByCustomer("Cancelled by customer"),
    CancelledByMerchant("Cancelled by merchant"),
    CancelledBySequo("Cancelled by Sequo"),
}

internal data class OrderItem(
    val name: String,
    val quantity: Int,
)

internal data class OrderTimelineEvent(
    val state: SequoOrderState,
    val time: String,
    val detail: String,
    val isCurrent: Boolean,
)

internal enum class OrderDetailTab(val label: String) {
    Status("Status"),
    Details("Details"),
}

internal data class SequoOrder(
    val id: String,
    val sellers: List<String>,
    val items: List<OrderItem>,
    val state: SequoOrderState,
    val dateLine: String,
    val note: String,
    val amountCfa: Int,
    val paymentMethod: String,
    val pickupCode: String,
)

internal data class AppBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val emphasized: Boolean = false,
    val badge: String? = null,
)
