package dev.orestegabo.sequo

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.domain.DeliveryPricingInput
import dev.orestegabo.sequo.domain.calculateDeliveryPricing
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.Res
import sequo.shared.generated.resources.moov_africa_logo
import sequo.shared.generated.resources.yas_togo_logo

private val SequoPrimary = Color(0xFF2F6970)
private val SequoSecondary = Color(0xFFB4874F)
private val SequoAccent = Color(0xFFD8C6A3)
private val SequoSurface = Color(0xFFFCF8F1)
private val SequoBackground = Color(0xFFF3EEE5)
private val SequoSurfaceVariant = Color(0xFFF0EAE0)
private val SequoOnSurface = Color(0xFF1A1712)
private val SequoOnSurfaceVariant = Color(0xFF5E5A52)
private val SequoOutline = Color(0xFFD4CABB)

private val sequoColorScheme = lightColorScheme(
    primary = SequoPrimary,
    onPrimary = Color.White,
    secondary = SequoSecondary,
    onSecondary = Color.White,
    tertiary = SequoAccent,
    onTertiary = SequoOnSurface,
    surface = SequoSurface,
    onSurface = SequoOnSurface,
    background = SequoBackground,
    onBackground = SequoOnSurface,
    primaryContainer = SequoPrimary.copy(alpha = 0.12f),
    onPrimaryContainer = SequoPrimary,
    secondaryContainer = SequoSecondary.copy(alpha = 0.14f),
    onSecondaryContainer = Color(0xFF3A2811),
    tertiaryContainer = SequoAccent.copy(alpha = 0.20f),
    onTertiaryContainer = Color(0xFF4E3F21),
    surfaceVariant = SequoSurfaceVariant,
    onSurfaceVariant = SequoOnSurfaceVariant,
    outline = SequoOutline,
)

private data class SequoUiPalette(
    val ambientBottom: Color = SequoSurfaceVariant,
    val ambientLineStrong: Color = SequoPrimary.copy(alpha = 0.13f),
    val ambientLineSoft: Color = SequoAccent.copy(alpha = 0.10f),
    val ambientCirclePrimary: Color = SequoPrimary.copy(alpha = 0.08f),
    val ambientCircleSecondary: Color = SequoAccent.copy(alpha = 0.07f),
    val ambientPanelTop: Color = SequoPrimary.copy(alpha = 0.035f),
    val ambientPanelBottom: Color = SequoAccent.copy(alpha = 0.028f),
    val floatingShell: Color = SequoSurface,
    val floatingShellBorder: Color = SequoSecondary.copy(alpha = 0.20f),
)

private val sequoUi = SequoUiPalette()

private enum class SequoSection(val label: String, val icon: ImageVector) {
    Markets("Markets", Icons.Filled.Storefront),
    Basket("Basket", Icons.Filled.ShoppingBasket),
    Home("Sequo", Icons.Filled.Home),
    Orders("Orders", Icons.AutoMirrored.Filled.ReceiptLong),
    Account("Account", Icons.Filled.Person),
}

private val sequoPrimaryDestinations = listOf(
    SequoSection.Markets,
    SequoSection.Basket,
    SequoSection.Home,
    SequoSection.Orders,
    SequoSection.Account,
)

private data class SequoProduct(
    val name: String,
    val detail: String,
    val priceCfa: Int,
    val label: String,
    val optionHint: String,
    val bargainNote: String? = null,
)

private data class SequoShop(
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

private data class SequoShopType(
    val key: String,
    val title: String,
    val supportLabel: String,
    val icon: ImageVector,
    val accent: Color,
)

private val sequoShopTypes = listOf(
    SequoShopType("food", "Food", "Hot meals", Icons.Filled.LocalDining, SequoSecondary),
    SequoShopType("market", "Fresh market", "Produce", Icons.Filled.Storefront, SequoPrimary),
    SequoShopType("phones", "Phones", "Electronics", Icons.Filled.PhoneAndroid, Color(0xFF4D6F9E)),
    SequoShopType("water", "Water", "Home packs", Icons.Filled.WaterDrop, Color(0xFF3E8B92)),
    SequoShopType("tires", "Tires & auto", "Car care", Icons.Filled.Build, Color(0xFF805E48)),
    SequoShopType("pharmacy", "Pharmacy", "Care items", Icons.Filled.MedicalServices, Color(0xFF6C7E51)),
    SequoShopType("home", "Home goods", "Daily basics", Icons.Filled.ShoppingBasket, Color(0xFFA77A41)),
    SequoShopType("bargains", "Bargains", "Negotiate", Icons.Filled.LocalOffer, Color(0xFF9A5A68)),
)

private val sequoShops = listOf(
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
        name = "Hedzranawoe Phones",
        area = "Hedzranawoe",
        kind = "Phones & electronics",
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
        name = "Akodessewa Eau Service",
        area = "Akodessewa",
        kind = "Water & household",
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

private data class BasketEntry(
    val shop: SequoShop,
    val product: SequoProduct,
    val quantity: Int,
)

private enum class SequoOrderState(val label: String) {
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

private data class OrderItem(
    val name: String,
    val quantity: Int,
)

private data class OrderTimelineEvent(
    val state: SequoOrderState,
    val time: String,
    val detail: String,
    val isCurrent: Boolean,
)

private enum class OrderDetailTab(val label: String) {
    Status("Status"),
    Details("Details"),
}

private data class SequoOrder(
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

private data class AppBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val emphasized: Boolean = false,
    val badge: String? = null,
)

private val sequoBasket = listOf(
    BasketEntry(sequoShops[0], sequoShops[0].products[0], 1),
    BasketEntry(sequoShops[1], sequoShops[1].products[0], 1),
    BasketEntry(sequoShops[3], sequoShops[3].products[0], 2),
)

private val recentOrders = listOf(
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
        sellers = listOf("Akodessewa Maison Sante"),
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
        sellers = listOf("Hedzranawoe Phones"),
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
        sellers = listOf("Grand Marche Assigame", "Hedzranawoe Phones"),
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
        sellers = listOf("Akodessewa Maison Sante"),
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
        sellers = listOf("Hedzranawoe Phones"),
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

private fun shopTypeFor(key: String): SequoShopType =
    sequoShopTypes.firstOrNull { it.key == key } ?: sequoShopTypes.first()

private fun shopsForType(typeKey: String): List<SequoShop> =
    when (typeKey) {
        "food" -> sequoShops.filter { it.kind.contains("Food", ignoreCase = true) }
        "market" -> sequoShops.filter { it.kind.contains("Market", ignoreCase = true) }
        "phones" -> sequoShops.filter { it.kind.contains("Phone", ignoreCase = true) || it.kind.contains("electronics", ignoreCase = true) }
        "water" -> sequoShops.filter { it.kind.contains("Water", ignoreCase = true) }
        "tires" -> sequoShops.filter { it.kind.contains("Tires", ignoreCase = true) || it.kind.contains("auto", ignoreCase = true) }
        "pharmacy" -> sequoShops.filter { it.kind.contains("Pharmacy", ignoreCase = true) || it.kind.contains("care", ignoreCase = true) }
        "home" -> sequoShops.filter { it.kind.contains("Home", ignoreCase = true) || it.kind.contains("household", ignoreCase = true) }
        "bargains" -> sequoShops.filter { shop -> shop.products.any { it.bargainNote != null } }
        else -> sequoShops
    }

private fun featuredProductsFor(typeKey: String): List<Pair<SequoShop, SequoProduct>> {
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

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = sequoColorScheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SequoShell()
        }
    }
}

@Composable
private fun SequoShell() {
    var currentDestination by remember { mutableStateOf(SequoSection.Home) }
    var extraBasketItems by remember { mutableStateOf(0) }
    val basketCount = sequoBasket.sumOf { it.quantity } + extraBasketItems

    Box(modifier = Modifier.fillMaxSize()) {
        SequoAmbientBackground(modifier = Modifier.fillMaxSize())
        SequoContentStage(
            currentDestination = currentDestination,
            onDestinationSelected = { currentDestination = it },
            extraBasketItems = extraBasketItems,
            onAddProduct = { extraBasketItems += 1 },
            modifier = Modifier.fillMaxSize(),
        )
        SequoBottomBar(
            currentDestination = currentDestination,
            onDestinationSelected = { currentDestination = it },
            pendingBasketCount = basketCount,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SequoContentStage(
    currentDestination: SequoSection,
    onDestinationSelected: (SequoSection) -> Unit,
    extraBasketItems: Int,
    onAddProduct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SequoScreenColumn(modifier = modifier) {
        when (currentDestination) {
            SequoSection.Home -> HomeContent(onDestinationSelected, onAddProduct)
            SequoSection.Markets -> MarketsContent(onAddProduct)
            SequoSection.Basket -> BasketContent(extraBasketItems)
            SequoSection.Orders -> OrdersContent()
            SequoSection.Account -> AccountContent()
        }
    }
}

@Composable
private fun SequoScreenColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 26.dp, end = 20.dp, bottom = 126.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
private fun HomeContent(
    onDestinationSelected: (SequoSection) -> Unit,
    onAddProduct: () -> Unit,
) {
    var selectedTypeKey by remember { mutableStateOf(sequoShopTypes.first().key) }
    val selectedType = shopTypeFor(selectedTypeKey)
    val selectedShops = shopsForType(selectedTypeKey)
    val nearestShop = selectedShops.minByOrNull { it.distanceKm } ?: sequoShops.first()

    HomeAppBar()
    SequoHeroCard(
        eyebrow = "Lome now",
        title = "Food. Market. Goods.",
        body = "Pick a shop, see the fee, pay mobile money.",
        primaryLabel = "Shop Lome",
        secondaryLabel = "Track order",
        onPrimary = { onDestinationSelected(SequoSection.Markets) },
        onSecondary = { onDestinationSelected(SequoSection.Orders) },
    )
    SequoSearchCard()
    ShopTypeRail(
        types = sequoShopTypes,
        selectedTypeKey = selectedTypeKey,
        onTypeSelected = { selectedTypeKey = it },
    )
    SequoMetricRow(
        leftValue = formatDistance(nearestShop.distanceKm),
        leftLabel = nearestShop.area,
        rightValue = formatCfa(baseDelivery(nearestShop.distanceKm)),
        rightLabel = "delivery fee",
    )
    HomeSignalRow()
    SequoSectionCard(title = "${selectedType.title} shops", action = "nearby") {
        selectedShops.take(3).forEach { shop ->
            ShopSummaryRow(shop = shop)
        }
    }
    SequoSectionCard(title = "Popular now", action = selectedType.supportLabel) {
        featuredProductsFor(selectedTypeKey).forEach { (shop, product) ->
            CompactProductCard(shop, product, onAddProduct)
        }
    }
}

@Composable
private fun MarketsContent(onAddProduct: () -> Unit) {
    var selectedTypeKey by remember { mutableStateOf(sequoShopTypes.first().key) }
    var selectedArea by remember { mutableStateOf("All Lome") }
    val selectedType = shopTypeFor(selectedTypeKey)
    val shopsByType = shopsForType(selectedTypeKey)
    val visibleShops = if (selectedArea == "All Lome") {
        shopsByType
    } else {
        shopsByType.filter { shop ->
            shop.area.contains(selectedArea) || shop.name.contains(selectedArea)
        }
    }

    SequoAppBar(
        title = "Markets",
        subtitle = "Verified Lomé sellers",
        leadingIcon = Icons.Filled.Storefront,
        actions = listOf(
            AppBarAction(Icons.Filled.Search, "Search markets"),
            AppBarAction(Icons.Filled.Tune, "Filter shops", emphasized = true),
        ),
    )
    SequoStatusStrip(
        icon = Icons.Filled.PhotoCamera,
        title = "Live photo gate",
        detail = "Seller gallery uploads stay blocked unless the product is generic.",
        tag = "active",
    )
    SequoIntroCard(
        eyebrow = "Markets",
        title = "${selectedType.title} shops in Lome.",
        subtitle = "Distance, fee, photos, and pickup grouping are visible before checkout.",
    )
    ShopTypeRail(
        types = sequoShopTypes,
        selectedTypeKey = selectedTypeKey,
        onTypeSelected = { selectedTypeKey = it },
    )
    CategoryRail(
        categories = listOf("All Lome", "Tokoin", "Assigame", "Hedzranawoe", "Akodessewa", "Agbalepedo", "Be-Kpota", "Baguida"),
        selectedCategory = selectedArea,
        onCategorySelected = { selectedArea = it },
    )
    LomeRouteCard()
    visibleShops.forEach { shop ->
        SequoShopCard(shop = shop, onAddProduct = onAddProduct)
    }
}

@Composable
private fun BasketContent(extraBasketItems: Int) {
    var selectedPayment by remember { mutableStateOf("Yas Togo") }

    SequoAppBar(
        title = "Checkout",
        subtitle = "Basket locked before payment",
        leadingIcon = Icons.Filled.ShoppingBasket,
        actions = listOf(
            AppBarAction(Icons.Filled.Lock, "Secure checkout", emphasized = true),
            AppBarAction(Icons.Filled.Payments, "Payment methods"),
        ),
    )
    SequoStatusStrip(
        icon = Icons.Filled.Lock,
        title = "Payment validation required",
        detail = "Order completion unlocks only after Yas Togo or Moov Africa confirms.",
        tag = "secure",
    )
    DeliveryAddressCard()
    SequoSectionCard(title = "Basket", action = "${sequoBasket.sumOf { it.quantity } + extraBasketItems} items") {
        sequoBasket.forEach { entry ->
            BasketLine(entry)
        }
        if (extraBasketItems > 0) {
            BasketAddedLine(extraBasketItems)
        }
    }
    SequoSectionCard(title = "Food options", action = "custom") {
        RuleRow("Attieke poisson braise", sequoShops[0].products[0].optionHint)
        RuleRow("No mixing with goods", "Hot food travels in a separate sealed bag inside the Sequo package.")
    }
    SequoSectionCard(title = "Consolidation", action = "Lome route") {
        RuleRow("Assigame + Akodessewa", "Eligible for one Sequo package after inspection at pickup.")
        RuleRow("Food exception", "Tokoin hot meal keeps its own thermal seal and pickup timing.")
    }
    SequoCheckoutCard(extraBasketItems, selectedPayment, onPaymentSelected = { selectedPayment = it })
}

@Composable
private fun OrdersContent() {
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
private fun OrderDetailScreen(order: SequoOrder, onBack: () -> Unit) {
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

@Composable
private fun AccountContent() {
    SequoAppBar(
        title = "Afi K.",
        subtitle = "Subscriber profile",
        leadingIcon = Icons.Filled.Person,
        actions = listOf(
            AppBarAction(Icons.Filled.Settings, "Account settings", emphasized = true),
            AppBarAction(Icons.Filled.Notifications, "Notification settings"),
        ),
    )
    SequoStatusStrip(
        icon = Icons.Filled.CheckCircle,
        title = "Subscription active",
        detail = "15% delivery-fee discount plus 500 CFA parrainage credit.",
        tag = "15%",
    )
    SequoPassCard()
    SequoSectionCard(title = "Saved places", action = "Lome") {
        AccountAddressRow("Home", "Tokoin Gbadago, near Pharmacie des Etoiles")
        AccountAddressRow("Family", "Adidogome, carrefour Limousine")
        AccountAddressRow("Office", "Be-Kpota, route du marche")
    }
    SequoSectionCard(title = "Supported payments", action = "no cash") {
        SupportedPaymentRow("Yas Togo", "Primary mobile money validation before order completion")
        SupportedPaymentRow("Moov Africa", "Backup mobile money validation for checkout")
    }
    SequoSectionCard(title = "Account tools", action = "secure") {
        SettingRow("Payments", "Yas Togo and Moov Africa validation")
        SettingRow("Returns", "Point de Relai drop-off within 72 hours")
        SettingRow("Parrainage", "Delivery credit, never cash payout")
        SettingRow("Subscription", "15% delivery fee discount active")
    }
}

@Composable
private fun HomeAppBar() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SequoAppBar(
            title = "Tokoin Gbadago",
            subtitle = "Deliver to Pharmacie des Etoiles area",
            leadingIcon = Icons.Filled.Place,
            actions = listOf(
                AppBarAction(Icons.Filled.Payments, "Yas payment ready", emphasized = true),
                AppBarAction(Icons.Filled.Notifications, "Notifications", badge = "2"),
            ),
        )
        SequoStatusStrip(
            icon = Icons.Filled.PhotoCamera,
            title = "Camera checks nearby",
            detail = "8 live seller photos refreshed around Tokoin and Assigame.",
            tag = "live",
        )
    }
}

@Composable
private fun SequoAppBar(
    title: String,
    subtitle: String,
    leadingIcon: ImageVector,
    actions: List<AppBarAction>,
) {
    SequoCard(shape = RoundedCornerShape(28.dp)) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SequoIconMark(leadingIcon, MaterialTheme.colorScheme.primary, Modifier.size(46.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            actions.forEach { action ->
                AppBarIconButton(action)
            }
        }
    }
}

@Composable
private fun AppBarIconButton(action: AppBarAction) {
    val accent = if (action.emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    Box {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (action.emphasized) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
                .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                .clickable { }
                .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.contentDescription,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
        action.badge?.let { badge ->
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.32f)),
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SequoStatusStrip(
    icon: ImageVector,
    title: String,
    detail: String,
    tag: String,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SequoIconMark(icon, MaterialTheme.colorScheme.primary, Modifier.size(34.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            MetaPill(tag, SequoSecondary)
        }
    }
}

@Composable
private fun SequoIconMark(icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(17.dp))
            .background(color.copy(alpha = 0.13f))
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(17.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun SequoSearchCard() {
    SequoCard(shape = RoundedCornerShape(24.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SequoIconMark(Icons.Filled.Search, SequoSecondary, Modifier.size(34.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Search Lome", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Attieke, water packs, phone chargers, wax pagnes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ShopTypeRail(
    types: List<SequoShopType>,
    selectedTypeKey: String,
    onTypeSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SequoInlineSectionLabel("Shop type")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            types.forEach { type ->
                ShopTypeCard(
                    type = type,
                    selected = type.key == selectedTypeKey,
                    onClick = { onTypeSelected(type.key) },
                )
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun ShopTypeCard(
    type: SequoShopType,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val alpha = if (selected) 1f else 0.72f
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(112.dp)
            .height(126.dp),
        shape = RoundedCornerShape(24.dp),
        color = type.accent.copy(alpha = if (selected) 0.20f else 0.12f),
        border = BorderStroke(1.dp, type.accent.copy(alpha = if (selected) 0.38f else 0.18f)),
    ) {
        Box {
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .width(38.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(type.accent),
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = type.accent.copy(alpha = if (selected) 0.25f else 0.16f),
                ) {
                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = type.icon,
                            contentDescription = null,
                            tint = type.accent.copy(alpha = alpha),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Text(
                    type.title,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (selected) 0.88f else 0.70f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    type.supportLabel,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (selected) 0.62f else 0.48f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SequoInlineSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
        )
    }
}

@Composable
private fun CategoryRail(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { category ->
                    SequoFilterChip(
                        label = category,
                        selected = category == selectedCategory,
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(3 - rowItems.size) {
                    Box(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SequoFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .border(
                1.dp,
                if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
                RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun OrderDetailTabs(
    selectedTab: OrderDetailTab,
    onTabSelected: (OrderDetailTab) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(54.dp).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrderDetailTab.values().forEach { tab ->
                val selected = tab == selectedTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(tab) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(top = 8.dp, bottom = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            tab.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 34.dp, height = 3.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LomeRouteCard() {
    SequoCard(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Today route", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("fees visible", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                RouteStop("Tokoin", true, Modifier.weight(1f))
                RouteStop("Assigame", true, Modifier.weight(1f))
                RouteStop("Be", false, Modifier.weight(1f))
                RouteStop("Akodessewa", false, Modifier.weight(1f))
            }
            Text(
                "Sequo groups eligible market goods on the same Lome pickup route, then keeps hot food in a separate seal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
            )
        }
    }
}

@Composable
private fun RouteStop(label: String, active: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(
            modifier = Modifier
                .height(6.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (active) 0.82f else 0.52f), maxLines = 1)
    }
}

@Composable
private fun SequoHeroCard(
    eyebrow: String,
    title: String,
    body: String,
    primaryLabel: String,
    secondaryLabel: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
) {
    SequoCard(shape = RoundedCornerShape(32.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            SequoSecondary.copy(alpha = 0.20f),
                            SequoAccent.copy(alpha = 0.16f),
                            SequoPrimary.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(SequoSecondary.copy(alpha = 0.12f), radius = size.minDimension * 0.42f, center = Offset(size.width * 0.92f, size.height * 0.06f))
                drawCircle(SequoPrimary.copy(alpha = 0.10f), radius = size.minDimension * 0.30f, center = Offset(size.width * 0.10f, size.height * 0.84f))
            }
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                        ) {
                            Text(
                                eyebrow,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(
                            title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                    }
                    Surface(
                        modifier = Modifier.padding(start = 12.dp),
                        shape = RoundedCornerShape(26.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
                        border = BorderStroke(1.dp, SequoSecondary.copy(alpha = 0.22f)),
                    ) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                            SequoMonogram("SQ", SequoSecondary, Modifier.size(42.dp))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaPill("400 CFA nearby", SequoPrimary)
                    MetaPill("Live photos", SequoAccent)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SequoPrimaryButton(primaryLabel, onPrimary, Modifier.weight(1f))
                    SequoSecondaryButton(secondaryLabel, onSecondary, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SequoMetricRow(
    leftValue: String,
    leftLabel: String,
    rightValue: String,
    rightLabel: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricCard(leftValue, leftLabel, Modifier.weight(1f))
        MetricCard(rightValue, rightLabel, Modifier.weight(1f))
    }
}

@Composable
private fun MetricCard(value: String, label: String, modifier: Modifier = Modifier) {
    SequoCard(modifier = modifier, shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f))
        }
    }
}

@Composable
private fun HomeSignalRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HomeSignalPill(Icons.Filled.PhotoCamera, "Live photo", SequoPrimary, Modifier.weight(1f))
        HomeSignalPill(Icons.Filled.Payments, "Yas/Moov", SequoSecondary, Modifier.weight(1f))
        HomeSignalPill(Icons.Filled.CheckCircle, "72h relai", SequoAccent, Modifier.weight(1f))
    }
}

@Composable
private fun HomeSignalPill(icon: ImageVector, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SequoIntroCard(eyebrow: String, title: String, subtitle: String) {
    SequoCard(shape = RoundedCornerShape(24.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SequoMonogram(
                text = "SQ",
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 12.dp)
                    .size(72.dp),
            )
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(eyebrow, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f))
            }
        }
    }
}

@Composable
private fun SequoSectionCard(
    title: String,
    action: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    SequoCard {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                if (!action.isNullOrBlank()) {
                    Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            content()
        }
    }
}

@Composable
private fun SequoShopCard(shop: SequoShop, onAddProduct: () -> Unit) {
    SequoCard {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                SequoIconMark(Icons.Filled.Storefront, MaterialTheme.colorScheme.primary, Modifier.size(52.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(shop.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${shop.area} / ${shop.kind}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(shop.openStatus, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                RatingMark(shop.rating)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill(formatDistance(shop.distanceKm), SequoPrimary)
                MetaPill(formatCfa(baseDelivery(shop.distanceKm)), SequoSecondary)
                MetaPill(shop.eta, SequoAccent)
            }
            RuleRow(shop.photoStatus, shop.consolidation)
            shop.products.forEach { product ->
                ProductLine(product = product, onAddProduct = onAddProduct)
            }
        }
    }
}

@Composable
private fun ShopSummaryRow(shop: SequoShop) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                SequoIconMark(Icons.Filled.Storefront, MaterialTheme.colorScheme.primary, Modifier.size(46.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(shop.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${shop.area} / ${shop.kind}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                RatingMark(shop.rating)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill(formatDistance(shop.distanceKm), SequoPrimary)
                MetaPill(formatCfa(baseDelivery(shop.distanceKm)), SequoSecondary)
                MetaPill(shop.eta, SequoAccent)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ShopStatusDot()
                Text(shop.openStatus, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(shop.photoStatus, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ShopStatusDot() {
    Box(
        modifier = Modifier
            .size(9.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
    )
}

@Composable
private fun ProductLine(product: SequoProduct, onAddProduct: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(product.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(formatCfa(product.priceCfa), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                MetaPill(product.label, if (product.bargainNote == null) SequoAccent else SequoSecondary)
                Text(product.optionHint, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                SequoTinyButton("Add", onAddProduct)
            }
            product.bargainNote?.let { note ->
                RuleRow("Negotiation", note)
            }
        }
    }
}

@Composable
private fun CompactProductCard(shop: SequoShop, product: SequoProduct, onAddProduct: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShopMark(product.name)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${shop.area} / ${formatCfa(product.priceCfa)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            SequoTinyButton("Add", onAddProduct)
        }
    }
}

@Composable
private fun BasketLine(entry: BasketEntry) {
    val shop = entry.shop
    val product = entry.product
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        ShopMark(product.name)
        Column(Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${shop.name} / Qty ${entry.quantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(formatCfa(product.priceCfa * entry.quantity), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BasketAddedLine(count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        ShopMark("Added")
        Column(Modifier.weight(1f)) {
            Text("Added while browsing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Temporary basket items / Qty $count", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
        }
        Text(formatCfa(count * 3500), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DeliveryAddressCard() {
    SequoCard(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Delivery address", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("edit", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            RuleRow("Tokoin Gbadago", "Near Pharmacie des Etoiles, call when outside.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill("5.6 km route", SequoPrimary)
                MetaPill("Subscriber -15%", SequoSecondary)
            }
        }
    }
}

@Composable
private fun SequoCheckoutCard(
    extraBasketItems: Int,
    selectedPayment: String,
    onPaymentSelected: (String) -> Unit,
) {
    val subtotal = sequoBasket.sumOf { it.product.priceCfa * it.quantity } + (extraBasketItems * 3500)
    val delivery = calculateDeliveryPricing(
        DeliveryPricingInput(distanceKm = 5.6, subscriptionDiscountPercent = 15, referralCreditCfa = 500),
    )
    val total = subtotal + delivery.finalDeliveryFeeCfa

    SequoSectionCard(title = "Pay securely", action = selectedPayment) {
        ValueRow("Items", formatCfa(subtotal))
        ValueRow("Delivery", formatCfa(delivery.baseFeeCfa))
        ValueRow("Subscriber", "-${formatCfa(delivery.subscriptionDiscountCfa)}")
        ValueRow("Parrainage", "-${formatCfa(delivery.referralCreditAppliedCfa)}")
        ValueRow("Total", formatCfa(total), strong = true)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PaymentChoice("Yas Togo", selectedPayment, onPaymentSelected, Modifier.weight(1f))
            PaymentChoice("Moov Africa", selectedPayment, onPaymentSelected, Modifier.weight(1f))
        }
        SequoPrimaryButton("Pay ${formatCfa(total)}", {}, Modifier.fillMaxWidth())
        Text(
            "Payment must be confirmed before the seller receives completion status.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
        )
    }
}

@Composable
private fun PaymentChoice(
    label: String,
    selectedPayment: String,
    onPaymentSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = label == selectedPayment
    val borderColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.48f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)

    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onPaymentSelected(label) }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
            PaymentLogo(label, Modifier.size(width = 46.dp, height = 34.dp))
            Text(
                label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun SupportedPaymentRow(label: String, detail: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PaymentLogo(label, Modifier.size(width = 74.dp, height = 44.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
            }
            MetaPill("Enabled", SequoPrimary)
        }
    }
}

@Composable
private fun PaymentLogo(label: String, modifier: Modifier = Modifier) {
    val logo = if (label.contains("Yas")) {
        Res.drawable.yas_togo_logo
    } else {
        Res.drawable.moov_africa_logo
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(11.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Image(
            painter = painterResource(logo),
            contentDescription = label,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().padding(4.dp),
        )
    }
}

@Composable
private fun ReturnHubCard() {
    SequoSectionCard(title = "Return hub", action = "72 hours") {
        RuleRow("Point de Relai Tokoin", "Open 08:00-19:00, accepts sealed general goods returns.")
        RuleRow("Inspection status", "Refund triggers automatically when Sequo accepts the returned product.")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SequoSecondaryButton("Start return", {}, Modifier.weight(1f), emphasized = true)
            SequoSecondaryButton("Find point", {}, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AccountAddressRow(label: String, address: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        SequoMiniMark(label.take(1), SequoSecondary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SequoPassCard() {
    SequoCard(shape = RoundedCornerShape(34.dp), color = Color.Transparent, border = null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF111419), Color(0xFF18242B), Color(0xFF24404A))))
                .border(1.dp, SequoAccent.copy(alpha = 0.28f), RoundedCornerShape(34.dp))
                .padding(20.dp),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(SequoAccent.copy(alpha = 0.18f), radius = size.width * 0.26f, center = Offset(size.width * 0.92f, size.height * 0.16f))
                drawRoundRect(Color(0x14FFF9F0), Offset(-34.dp.toPx(), size.height - 74.dp.toPx()), Size(180.dp.toPx(), 82.dp.toPx()), CornerRadius(44.dp.toPx(), 24.dp.toPx()))
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("SEQUO ACCESS", style = MaterialTheme.typography.labelSmall, color = Color(0xCCFFF8EE))
                Text("Afi K.", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFFFFBF5), fontWeight = FontWeight.SemiBold)
                Text("Lome subscriber / 15% delivery discount", style = MaterialTheme.typography.bodyMedium, color = Color(0xCCFFF8EE))
                Box(Modifier.height(44.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaPill("500 CFA credit", SequoAccent, inverse = true)
                    MetaPill("3 Lome addresses", SequoPrimary, inverse = true)
                }
            }
        }
    }
}

@Composable
private fun SequoCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(26.dp),
    color: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = color,
        border = border,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        content = content,
    )
}

@Composable
private fun SequoPrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.92f))))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SequoSecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    val containerColor = if (emphasized) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f)
    val borderColor = if (emphasized) MaterialTheme.colorScheme.primary.copy(alpha = 0.48f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
    val textColor = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f)
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .border(1.4.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SequoTinyButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MetaPill(label: String, accentColor: Color, inverse: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (inverse) Color(0x2EFFF8EE) else accentColor.copy(alpha = 0.14f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (inverse) Color(0xFFFFFBF5) else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RuleRow(title: String, detail: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
        }
    }
}

@Composable
private fun ShopMark(text: String) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(SequoPrimary.copy(alpha = 0.88f), SequoSecondary.copy(alpha = 0.72f)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(text.take(2).uppercase(), style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun RatingMark(rating: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.76f),
    ) {
        Text(
            rating,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SequoMiniMark(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun SequoMonogram(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.30f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun StepRow(text: String, active: Boolean, detail: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)))
        Column(Modifier.weight(1f)) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (active) 0.96f else 0.68f), fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
            Text(detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f))
        }
    }
}

@Composable
private fun OrderMemory(
    order: SequoOrder,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SequoIconMark(Icons.AutoMirrored.Filled.ReceiptLong, orderStateColor(order.state), Modifier.size(48.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(orderTitle(order), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${sellerSummary(order)} / ${order.dateLine}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${order.id} / ${orderMetaLine(order)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                OrderStatusPill(order.state)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View order details",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun OrderDetailAppBar(order: SequoOrder, onBack: () -> Unit) {
    SequoCard(shape = RoundedCornerShape(28.dp)) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), RoundedCornerShape(17.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to orders",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(21.dp),
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Order detail", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${order.id} / ${order.state.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OrderStatusPill(order.state)
        }
    }
}

@Composable
private fun PickupCodePanel(order: SequoOrder) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SequoIconMark(Icons.Filled.Lock, MaterialTheme.colorScheme.primary, Modifier.size(46.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(order.pickupCode, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                Text("Rider enters this 6-character code at the seller before pickup is validated.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
            }
        }
    }
}

@Composable
private fun OrderItemDetailRow(item: OrderItem) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            MetaPill("x${item.quantity}", SequoSecondary)
        }
    }
}

@Composable
private fun OrderTimeline(events: List<OrderTimelineEvent>, timelineKey: String) {
    var expanded by remember(timelineKey) { mutableStateOf(false) }
    val collapsedCount = 3
    val visibleEvents = if (expanded || events.size <= collapsedCount) {
        events
    } else {
        events.take(collapsedCount)
    }

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        visibleEvents.forEachIndexed { index, event ->
            OrderTimelineRow(
                event = event,
                isLast = index == visibleEvents.lastIndex,
            )
        }
        if (events.size > collapsedCount) {
            TimelineExpandButton(
                expanded = expanded,
                hiddenCount = events.size - collapsedCount,
                onClick = { expanded = !expanded },
            )
        }
    }
}

@Composable
private fun TimelineExpandButton(
    expanded: Boolean,
    hiddenCount: Int,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(42.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (expanded) "Show fewer stages" else "Show $hiddenCount older stages",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun OrderTimelineRow(event: OrderTimelineEvent, isLast: Boolean) {
    val accent = orderStateColor(event.state)
    val pulseTransition = rememberInfiniteTransition(label = "orderTimelinePulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Restart,
        ),
        label = "currentStepPulse",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (event.isCurrent) {
                    Box(
                        modifier = Modifier
                            .size((18f + pulse * 10f).dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.18f * (1f - pulse))),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (event.isCurrent) accent else accent.copy(alpha = 0.34f))
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .size(width = 2.dp, height = 42.dp)
                        .background(accent.copy(alpha = 0.18f)),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(event.state.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (event.isCurrent && event.state == SequoOrderState.InDelivery) {
                    TimelineLivePill(accent)
                } else {
                    MetaPill(event.time, accent)
                }
            }
            Text(event.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (event.isCurrent) 0.78f else 0.58f))
        }
    }
}

@Composable
private fun TimelineLivePill(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "timelineLivePill")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1050),
            repeatMode = RepeatMode.Restart,
        ),
        label = "timelineLivePhase",
    )

    Surface(
        modifier = Modifier.size(width = 46.dp, height = 30.dp),
        shape = RoundedCornerShape(999.dp),
        color = accentColor.copy(alpha = 0.14f),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                val localPhase = (phase + index * 0.22f) % 1f
                val wave = if (localPhase < 0.5f) localPhase * 2f else (1f - localPhase) * 2f
                val alpha = (0.36f + wave * 0.46f).coerceIn(0.36f, 0.82f)
                Box(
                    modifier = Modifier
                        .offset(y = (-2f * wave).dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = alpha)),
                )
            }
        }
    }
}

@Composable
private fun OrderStatusPill(state: SequoOrderState) {
    val accent = orderStateColor(state)
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f)),
    ) {
        Text(
            state.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SettingRow(title: String, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
    }
}

@Composable
private fun ValueRow(label: String, value: String, strong: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (strong) 0.96f else 0.68f))
        Text(value, style = if (strong) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge, color = if (strong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}

private fun baseDelivery(distanceKm: Double): Int =
    calculateDeliveryPricing(DeliveryPricingInput(distanceKm)).baseFeeCfa

private fun SequoOrder.itemCount(): Int =
    items.sumOf { it.quantity }

private fun orderTitle(order: SequoOrder): String =
    if (order.itemCount() == 1) {
        order.items.first().name
    } else if (order.sellers.size == 1) {
        "${order.itemCount()} items from ${order.sellers.first()}"
    } else {
        "Grouped Sequo package"
    }

private fun sellerSummary(order: SequoOrder): String =
    if (order.sellers.size == 1) {
        order.sellers.first()
    } else {
        "${order.sellers.size} sellers: ${order.sellers.take(2).joinToString(" + ")}"
    }

private fun orderMetaLine(order: SequoOrder): String =
    "${formatCfa(order.amountCfa)} / ${order.itemCount()} item${if (order.itemCount() == 1) "" else "s"} / ${order.paymentMethod}"

private fun orderTimelineEvents(order: SequoOrder): List<OrderTimelineEvent> {
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

private fun orderTimelineStates(state: SequoOrderState): List<SequoOrderState> {
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

private fun orderTimelineTime(
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

private fun currentTimelineTime(order: SequoOrder, state: SequoOrderState): String {
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

private fun timelineFallbackTime(state: SequoOrderState): String =
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

private fun pastTimelineDayPrefix(order: SequoOrder): String? =
    when {
        order.dateLine.contains("yesterday", ignoreCase = true) -> "Yesterday"
        order.dateLine.contains("friday", ignoreCase = true) -> "Friday"
        order.dateLine.contains("monday", ignoreCase = true) -> "Monday"
        else -> null
    }

private fun String.extractClockTime(): String? {
    val index = windowed(size = 5, step = 1).indexOfFirst { candidate ->
        candidate[0].isDigit() &&
            candidate[1].isDigit() &&
            candidate[2] == ':' &&
            candidate[3].isDigit() &&
            candidate[4].isDigit()
    }
    return if (index >= 0) substring(index, index + 5) else null
}

private fun orderTimelineDetail(order: SequoOrder, state: SequoOrderState, isCurrent: Boolean): String =
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

private val SequoOrderState.shouldShowPickupCode: Boolean
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

private fun orderNextStepTag(state: SequoOrderState): String =
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

private fun orderNextStepTitle(state: SequoOrderState): String =
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

private fun orderNextStepDetail(state: SequoOrderState): String =
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

private fun pickupCodeFor(orderId: String): String {
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

private fun orderStateColor(state: SequoOrderState): Color =
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

private fun formatDistance(km: Double): String {
    val tenths = (km * 10).roundToInt()
    return if (tenths % 10 == 0) "${tenths / 10} km" else "${tenths / 10}.${tenths % 10} km"
}

private fun formatCfa(amount: Int): String =
    amount.toString().reversed().chunked(3).joinToString(" ").reversed() + " CFA"

@Composable
private fun SequoAmbientBackground(modifier: Modifier = Modifier) {
    val uiPalette = sequoUi
    val baseTop = MaterialTheme.colorScheme.background
    val baseBottom = uiPalette.ambientBottom
    val lineColor = uiPalette.ambientLineStrong
    val softLineColor = uiPalette.ambientLineSoft
    val circlePrimary = uiPalette.ambientCirclePrimary
    val circleSecondary = uiPalette.ambientCircleSecondary
    val topPanelColor = uiPalette.ambientPanelTop
    val bottomPanelColor = uiPalette.ambientPanelBottom

    Canvas(modifier = modifier.background(Brush.verticalGradient(listOf(baseTop, baseTop, baseBottom)))) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = topPanelColor,
            topLeft = Offset(w * 0.04f, h * 0.07f),
            size = Size(w * 0.92f, h * 0.24f),
            cornerRadius = CornerRadius(42f, 42f),
        )
        drawRoundRect(
            color = bottomPanelColor,
            topLeft = Offset(w * 0.05f, h * 0.66f),
            size = Size(w * 0.90f, h * 0.20f),
            cornerRadius = CornerRadius(42f, 42f),
        )

        drawLine(color = lineColor, start = Offset(w * 0.08f, h * 0.10f), end = Offset(w * 0.72f, h * 0.10f), strokeWidth = 5f, cap = StrokeCap.Round)
        drawLine(color = lineColor, start = Offset(w * 0.12f, h * 0.135f), end = Offset(w * 0.88f, h * 0.135f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(color = softLineColor, start = Offset(w * 0.18f, h * 0.74f), end = Offset(w * 0.84f, h * 0.74f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color = lineColor, start = Offset(w * 0.14f, h * 0.79f), end = Offset(w * 0.62f, h * 0.79f), strokeWidth = 5f, cap = StrokeCap.Round)

        val topPath = Path().apply {
            moveTo(w * 0.62f, h * 0.06f)
            lineTo(w * 0.76f, h * 0.06f)
            lineTo(w * 0.82f, h * 0.11f)
            lineTo(w * 0.93f, h * 0.11f)
            lineTo(w * 0.93f, h * 0.18f)
            lineTo(w * 0.82f, h * 0.18f)
            lineTo(w * 0.75f, h * 0.24f)
            lineTo(w * 0.58f, h * 0.24f)
        }
        drawPath(path = topPath, color = lineColor, style = Stroke(width = 3f))

        val bottomPath = Path().apply {
            moveTo(w * 0.10f, h * 0.88f)
            lineTo(w * 0.26f, h * 0.88f)
            lineTo(w * 0.33f, h * 0.83f)
            lineTo(w * 0.48f, h * 0.83f)
            lineTo(w * 0.48f, h * 0.90f)
            lineTo(w * 0.34f, h * 0.90f)
            lineTo(w * 0.27f, h * 0.95f)
            lineTo(w * 0.12f, h * 0.95f)
        }
        drawPath(path = bottomPath, color = softLineColor, style = Stroke(width = 3f))

        drawCircle(color = circlePrimary, radius = w * 0.18f, center = Offset(w * 0.88f, h * 0.22f), style = Stroke(width = 5f))
        drawCircle(color = circleSecondary, radius = w * 0.22f, center = Offset(w * 0.12f, h * 0.82f), style = Stroke(width = 5f))

        repeat(7) { index ->
            val y = h * (0.185f + index * 0.018f)
            drawLine(
                color = if (index % 2 == 0) lineColor else softLineColor,
                start = Offset(w * 0.11f, y),
                end = Offset(w * (0.36f + index * 0.06f), y),
                strokeWidth = if (index % 2 == 0) 3f else 2f,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun SequoBottomBar(
    modifier: Modifier = Modifier,
    currentDestination: SequoSection,
    onDestinationSelected: (SequoSection) -> Unit,
    pendingBasketCount: Int = 0,
    destinations: List<SequoSection> = sequoPrimaryDestinations,
) {
    SequoNavigationContainer(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEach { destination ->
                SequoBottomNavItem(
                    destination = destination,
                    selected = currentDestination == destination,
                    onClick = { onDestinationSelected(destination) },
                    badgeCount = if (destination == SequoSection.Basket) pendingBasketCount else 0,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SequoNavigationContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val uiPalette = sequoUi
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        color = Color.Transparent,
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            uiPalette.floatingShell.copy(alpha = 0.96f),
                            colors.surface.copy(alpha = 0.92f),
                        ),
                    ),
                )
                .border(
                    BorderStroke(
                        1.dp,
                        uiPalette.floatingShellBorder.copy(alpha = 0.9f),
                    ),
                    RoundedCornerShape(30.dp),
                ),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                drawLine(
                    color = colors.primary.copy(alpha = 0.10f),
                    start = Offset(w * 0.10f, h * 0.20f),
                    end = Offset(w * 0.88f, h * 0.20f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = colors.secondary.copy(alpha = 0.08f),
                    radius = w * 0.12f,
                    center = Offset(w * 0.90f, h * 0.12f),
                    style = Stroke(width = 3f),
                )
            }
            content()
        }
    }
}

@Composable
private fun SequoNavigationItemFrame(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Color) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = if (selected) colors.surface.copy(alpha = 0.94f) else Color.Transparent
    val contentColor = if (selected) colors.onSurface else colors.onSurfaceVariant.copy(alpha = 0.92f)

    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = if (selected) 1.dp else 0.dp,
        shadowElevation = if (selected) 2.dp else 0.dp,
        border = null,
        modifier = modifier.then(
            if (selected) {
                Modifier.drawWithContent {
                    drawContent()
                    val strokeWidth = 1.5.dp.toPx()
                    val inset = strokeWidth / 2f
                    val radius = 22.dp.toPx() - inset
                    val topStopY = size.height * 0.30f
                    val leftX = inset
                    val rightX = size.width - inset
                    val bottomY = size.height - inset
                    val sideStrokePath = Path().apply {
                        moveTo(leftX, topStopY)
                        lineTo(leftX, bottomY - radius)
                        quadraticTo(leftX, bottomY, leftX + radius, bottomY)
                        moveTo(rightX - radius, bottomY)
                        quadraticTo(rightX, bottomY, rightX, bottomY - radius)
                        lineTo(rightX, topStopY)
                    }
                    val sideStrokeBrush = Brush.verticalGradient(
                        colors = listOf(
                            colors.outline.copy(alpha = 0f),
                            colors.primary.copy(alpha = 0.18f),
                            colors.primary.copy(alpha = 0.34f),
                        ),
                        startY = topStopY,
                        endY = bottomY,
                    )
                    drawPath(
                        path = sideStrokePath,
                        brush = sideStrokeBrush,
                        style = Stroke(width = strokeWidth),
                    )
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colors.primary.copy(alpha = 0.24f),
                                colors.primary.copy(alpha = 0.40f),
                                colors.primary.copy(alpha = 0.24f),
                            ),
                            startX = leftX + radius * 0.72f,
                            endX = rightX - radius * 0.72f,
                        ),
                        start = Offset(leftX + radius * 0.72f, bottomY),
                        end = Offset(rightX - radius * 0.72f, bottomY),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
            } else {
                Modifier
            },
        ),
    ) {
        content(contentColor)
    }
}

@Composable
private fun SequoBottomNavItem(
    destination: SequoSection,
    selected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int,
    modifier: Modifier = Modifier,
) {
    SequoNavigationItemFrame(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    ) { contentColor ->
        val selectedIconContainerColor =
            if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            } else {
                Color.Transparent
            }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(selectedIconContainerColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                        modifier = Modifier.size(23.dp),
                        tint = contentColor,
                    )
                }
                NavigationBadge(
                    count = badgeCount,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp),
                )
            }
            Text(
                text = destination.label,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp),
            )
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(width = if (selected) 18.dp else 10.dp, height = 3.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
                    ),
            )
        }
    }
}

@Composable
private fun NavigationBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    if (count <= 0) return
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.36f)),
    ) {
        Text(
            text = count.coerceAtMost(9).toString(),
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
