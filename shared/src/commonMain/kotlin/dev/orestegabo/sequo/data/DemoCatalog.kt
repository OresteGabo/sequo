package dev.orestegabo.sequo.data

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

internal val sequoShopTypes = listOf(
    SequoShopType("food", "Food", "Hot meals", Icons.Filled.LocalDining, SequoSecondary),
    SequoShopType("market", "Fresh market", "Produce", Icons.Filled.Storefront, SequoPrimary),
    SequoShopType("electronics", "Electronics", "Phones & tech", Icons.Filled.PhoneAndroid, Color(0xFF4D6F9E)),
    SequoShopType("tires", "Tires & auto", "Car care", Icons.Filled.Build, Color(0xFF805E48)),
    SequoShopType("pharmacy", "Pharmacy", "Care items", Icons.Filled.MedicalServices, Color(0xFF6C7E51)),
    SequoShopType("home", "Home goods", "Daily basics", Icons.Filled.ShoppingBasket, Color(0xFFA77A41)),
    SequoShopType("bargains", "Bargains", "Negotiate", Icons.Filled.LocalOffer, Color(0xFF9A5A68)),
)

internal val sequoShops = listOf(
    SequoShop(
        name = "Chez Ramatou Attieke",
        area = "Tokoin Gbadago",
        kind = "Food now",
        distanceKm = 1.8,
        eta = "22 min",
        photoStatus = "Camera photo today",
        openStatus = "Open until 22:30",
        rating = "4.8",
        consolidation = "Food packed separately",
        products = listOf(
            SequoProduct(
                name = "Attieke poisson braise",
                detail = "Choose fish size, piment, onion, alloco",
                priceCfa = 4200,
                label = "Food",
                optionHint = "Medium fish / piment doux / extra onion",
            ),
            SequoProduct(
                name = "Riz gras poulet",
                detail = "Sauce tomate, fried plantain, cold bissap",
                priceCfa = 3300,
                label = "Hot",
                optionHint = "No pepper / add bissap",
            ),
        ),
    ),
    SequoShop(
        name = "Grand Marche Assigame",
        area = "Assigame",
        kind = "Market cooperative",
        distanceKm = 3.1,
        eta = "45 min",
        photoStatus = "88% live photos",
        openStatus = "Open now",
        rating = "4.6",
        consolidation = "Grouped by Sequo",
        products = listOf(
            SequoProduct(
                name = "Pagne wax 6 yards",
                detail = "Seller camera photo, bargain available",
                priceCfa = 17500,
                label = "Bargain",
                optionHint = "Historical minimum: 15 000 CFA",
                bargainNote = "Try 15 500 CFA / 3 attempts",
            ),
            SequoProduct(
                name = "Tomato and onion basket",
                detail = "Fresh produce inspected at pickup",
                priceCfa = 5200,
                label = "Fresh",
                optionHint = "Family basket / today harvest",
            ),
        ),
    ),
    SequoShop(
        name = "Hedzranawoe Electronics",
        area = "Hedzranawoe",
        kind = "Electronics & phones",
        distanceKm = 5.6,
        eta = "Tomorrow",
        photoStatus = "Live photos verified",
        openStatus = "Ships today",
        rating = "4.7",
        consolidation = "Single package eligible",
        products = listOf(
            SequoProduct(
                name = "Oraimo charger 20W",
                detail = "USB-C, box photo required before pay",
                priceCfa = 9000,
                label = "Live",
                optionHint = "Color: white / sealed box",
            ),
            SequoProduct(
                name = "Tecno Spark 20 case",
                detail = "Minimum accepted price visible",
                priceCfa = 3500,
                label = "Bargain",
                optionHint = "Historical minimum: 2 800 CFA",
                bargainNote = "Offer 3 000 CFA / 3 attempts",
            ),
        ),
    ),
    SequoShop(
        name = "Akodessewa Maison Service",
        area = "Akodessewa",
        kind = "Household essentials",
        distanceKm = 7.2,
        eta = "Today 18:10",
        photoStatus = "Generic bottle uploads allowed",
        openStatus = "Open until 20:00",
        rating = "4.5",
        consolidation = "Route via Be-Kpota",
        products = listOf(
            SequoProduct(
                name = "Pack eau 1.5L x 6",
                detail = "Generic item, gallery upload accepted",
                priceCfa = 2400,
                label = "Water",
                optionHint = "Voltic / room temperature",
            ),
            SequoProduct(
                name = "Liquide vaisselle 1L",
                detail = "Seller camera photo before checkout",
                priceCfa = 1500,
                label = "Home",
                optionHint = "Lemon scent / sealed bottle",
            ),
        ),
    ),
    SequoShop(
        name = "Agbalepedo Pneus Express",
        area = "Agbalepedo",
        kind = "Tires & auto",
        distanceKm = 6.4,
        eta = "Today 17:40",
        photoStatus = "Tread photo required",
        openStatus = "Open until 19:30",
        rating = "4.4",
        consolidation = "Auto goods only",
        products = listOf(
            SequoProduct(
                name = "Pneu neuf 185/65 R15",
                detail = "Real-time sidewall and tread photos",
                priceCfa = 38500,
                label = "Auto",
                optionHint = "Brand photo / DOT date visible",
                bargainNote = "Historical minimum: 35 000 CFA",
            ),
            SequoProduct(
                name = "Kit meche tubeless",
                detail = "Small auto repair kit for emergency punctures",
                priceCfa = 4500,
                label = "Auto",
                optionHint = "5 strips / tools included",
            ),
        ),
    ),
    SequoShop(
        name = "Pharmacie du Golfe",
        area = "Be-Kpota",
        kind = "Pharmacy & care",
        distanceKm = 4.7,
        eta = "35 min",
        photoStatus = "Box photo before payment",
        openStatus = "Open until 23:00",
        rating = "4.7",
        consolidation = "Care items sealed",
        products = listOf(
            SequoProduct(
                name = "Thermometre digital",
                detail = "Real seller camera photo before checkout",
                priceCfa = 6500,
                label = "Care",
                optionHint = "Battery included",
            ),
            SequoProduct(
                name = "Gel hydroalcoolique 500ml",
                detail = "Generic care item with sealed bottle photo",
                priceCfa = 2200,
                label = "Care",
                optionHint = "Pump bottle / sealed",
            ),
        ),
    ),
    SequoShop(
        name = "Baguida Maison Express",
        area = "Baguida",
        kind = "Home goods",
        distanceKm = 9.1,
        eta = "Tomorrow",
        photoStatus = "Live photos required",
        openStatus = "Ships today",
        rating = "4.3",
        consolidation = "Single package eligible",
        products = listOf(
            SequoProduct(
                name = "Ampoule LED 12W",
                detail = "Box and wattage photo before payment",
                priceCfa = 1800,
                label = "Home",
                optionHint = "White light / E27",
            ),
            SequoProduct(
                name = "Multiprise 4 ports",
                detail = "Seller camera photo of socket type",
                priceCfa = 5500,
                label = "Home",
                optionHint = "Cable 1.5m / switch",
            ),
        ),
    ),
)

internal val sequoBasket = listOf(
    BasketEntry(sequoShops[0], sequoShops[0].products[0], 1),
    BasketEntry(sequoShops[1], sequoShops[1].products[0], 1),
    BasketEntry(sequoShops[3], sequoShops[3].products[0], 2),
)

internal val recentOrders = listOf(
    SequoOrder(
        id = "SQ-2419",
        sellers = listOf("Chez Ramatou Attieke"),
        items = listOf(OrderItem("Attieke poisson braise", 1), OrderItem("Bissap frais", 2)),
        state = SequoOrderState.InDelivery,
        dateLine = "Arriving in 9 min",
        note = "Rider validated pickup at Tokoin and is heading to Pharmacie des Etoiles.",
        amountCfa = 5600,
        paymentMethod = "Yas Togo",
        pickupCode = pickupCodeFor("SQ-2419"),
    ),
    SequoOrder(
        id = "SQ-2418",
        sellers = listOf("Pharmacie du Golfe", "Akodessewa Maison Service"),
        items = listOf(OrderItem("Thermometre digital", 1), OrderItem("Eau minerale 1.5L", 2)),
        state = SequoOrderState.PickedUp,
        dateLine = "Picked up 12:44",
        note = "Pickup code confirmed; package is being consolidated at Sequo for the Tokoin route.",
        amountCfa = 9100,
        paymentMethod = "Moov Africa",
        pickupCode = pickupCodeFor("SQ-2418"),
    ),
    SequoOrder(
        id = "SQ-2417",
        sellers = listOf("Grand Marche Assigame"),
        items = listOf(OrderItem("Tomato and onion basket", 1), OrderItem("Pagne wax 6 yards", 1)),
        state = SequoOrderState.ReadyForPickup,
        dateLine = "Ready since 12:31",
        note = "Merchant photos accepted; Sequo rider is being assigned for Assigame pickup.",
        amountCfa = 22700,
        paymentMethod = "Yas Togo",
        pickupCode = pickupCodeFor("SQ-2417"),
    ),
    SequoOrder(
        id = "SQ-2416",
        sellers = listOf("Hedzranawoe Electronics"),
        items = listOf(OrderItem("Tecno Spark 20 case", 1), OrderItem("Oraimo charger 20W", 1)),
        state = SequoOrderState.Preparing,
        dateLine = "Preparing now",
        note = "Merchant accepted the items and is taking required real-time product photos.",
        amountCfa = 10900,
        paymentMethod = "Moov Africa",
        pickupCode = pickupCodeFor("SQ-2416"),
    ),
    SequoOrder(
        id = "SQ-2415",
        sellers = listOf("Be-Kpota Superette"),
        items = listOf(OrderItem("Pack eau minerale", 1)),
        state = SequoOrderState.MerchantAccepted,
        dateLine = "Accepted 12:09",
        note = "Generic water product accepted; gallery restriction does not apply.",
        amountCfa = 3900,
        paymentMethod = "Yas Togo",
        pickupCode = pickupCodeFor("SQ-2415"),
    ),
    SequoOrder(
        id = "SQ-2414",
        sellers = listOf("Adidogome Bazar"),
        items = listOf(OrderItem("Lampe rechargeable", 1)),
        state = SequoOrderState.Paid,
        dateLine = "Paid 11:58",
        note = "Payment validated; waiting for merchant acceptance and availability check.",
        amountCfa = 8200,
        paymentMethod = "Moov Africa",
        pickupCode = pickupCodeFor("SQ-2414"),
    ),
    SequoOrder(
        id = "SQ-2408",
        sellers = listOf("Grand Marche Assigame", "Hedzranawoe Electronics"),
        items = listOf(OrderItem("Pagne wax 6 yards", 1), OrderItem("Oraimo charger 20W", 1)),
        state = SequoOrderState.Delivered,
        dateLine = "Delivered yesterday 18:40",
        note = "Return possible at Point de Relai Tokoin until tomorrow 18:40.",
        amountCfa = 27000,
        paymentMethod = "Yas Togo",
        pickupCode = pickupCodeFor("SQ-2408"),
    ),
    SequoOrder(
        id = "SQ-2397",
        sellers = listOf("Chez Ramatou Attieke"),
        items = listOf(OrderItem("Attieke poisson braise", 1)),
        state = SequoOrderState.Delivered,
        dateLine = "Delivered Friday 13:05",
        note = "Food order closed after delivery confirmation.",
        amountCfa = 4600,
        paymentMethod = "Yas Togo",
        pickupCode = pickupCodeFor("SQ-2397"),
    ),
    SequoOrder(
        id = "SQ-2388",
        sellers = listOf("Pharmacie du Golfe"),
        items = listOf(OrderItem("Thermometre digital", 1)),
        state = SequoOrderState.ReturnInInspection,
        dateLine = "Dropped at Point de Relai today",
        note = "Sequo inspection pending before refund is released.",
        amountCfa = 7100,
        paymentMethod = "Moov Africa",
        pickupCode = pickupCodeFor("SQ-2388"),
    ),
    SequoOrder(
        id = "SQ-2374",
        sellers = listOf("Hedzranawoe Electronics"),
        items = listOf(OrderItem("Tecno Spark 20 case", 1)),
        state = SequoOrderState.RefundIssued,
        dateLine = "Return accepted Monday",
        note = "Refund sent back to Moov Africa after inspection.",
        amountCfa = 3900,
        paymentMethod = "Moov Africa",
        pickupCode = pickupCodeFor("SQ-2374"),
    ),
    SequoOrder(
        id = "SQ-2360",
        sellers = listOf("Grand Marche Assigame"),
        items = listOf(OrderItem("Tomato and onion basket", 1), OrderItem("Pagne wax 6 yards", 1)),
        state = SequoOrderState.CancelledBySequo,
        dateLine = "Cancelled before pickup",
        note = "Seller did not confirm the required live camera photo.",
        amountCfa = 22700,
        paymentMethod = "Yas Togo",
        pickupCode = pickupCodeFor("SQ-2360"),
    ),
)

internal fun shopTypeFor(key: String): SequoShopType =
    sequoShopTypes.firstOrNull { it.key == key } ?: sequoShopTypes.first()

internal fun shopsForType(typeKey: String): List<SequoShop> =
    when (typeKey) {
        "food" -> sequoShops.filter { it.kind.contains("Food", ignoreCase = true) }
        "market" -> sequoShops.filter { it.kind.contains("Market", ignoreCase = true) }
        "electronics" -> sequoShops.filter { it.kind.contains("electronics", ignoreCase = true) || it.kind.contains("Phone", ignoreCase = true) }
        "tires" -> sequoShops.filter { it.kind.contains("Tires", ignoreCase = true) || it.kind.contains("auto", ignoreCase = true) }
        "pharmacy" -> sequoShops.filter { it.kind.contains("Pharmacy", ignoreCase = true) || it.kind.contains("care", ignoreCase = true) }
        "home" -> sequoShops.filter { it.kind.contains("Home", ignoreCase = true) || it.kind.contains("household", ignoreCase = true) }
        "bargains" -> sequoShops.filter { shop -> shop.products.any { it.bargainNote != null } }
        else -> sequoShops
    }

internal fun featuredProductsFor(typeKey: String): List<Pair<SequoShop, SequoProduct>> {
    val pairs = if (typeKey == "bargains") {
        sequoShops.flatMap { shop ->
            shop.products.filter { it.bargainNote != null }.map { product -> shop to product }
        }
    } else {
        shopsForType(typeKey).flatMap { shop ->
            shop.products.map { product -> shop to product }
        }
    }
    return pairs.take(3)
}
