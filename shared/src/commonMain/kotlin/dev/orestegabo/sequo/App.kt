package dev.orestegabo.sequo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.domain.DeliveryPricingInput
import dev.orestegabo.sequo.domain.calculateDeliveryPricing
import kotlin.math.roundToInt

private val KazePrimary = Color(0xFF2F6970)
private val KazeSecondary = Color(0xFFB4874F)
private val KazeAccent = Color(0xFFD8C6A3)
private val KazeSurface = Color(0xFFFCF8F1)
private val KazeBackground = Color(0xFFF3EEE5)
private val KazeSurfaceVariant = Color(0xFFF0EAE0)
private val KazeOnSurface = Color(0xFF1A1712)
private val KazeOnSurfaceVariant = Color(0xFF5E5A52)
private val KazeOutline = Color(0xFFD4CABB)

private val kazeColorScheme = lightColorScheme(
    primary = KazePrimary,
    onPrimary = Color.White,
    secondary = KazeSecondary,
    onSecondary = Color.White,
    tertiary = KazeAccent,
    onTertiary = KazeOnSurface,
    surface = KazeSurface,
    onSurface = KazeOnSurface,
    background = KazeBackground,
    onBackground = KazeOnSurface,
    primaryContainer = KazePrimary.copy(alpha = 0.12f),
    onPrimaryContainer = KazePrimary,
    secondaryContainer = KazeSecondary.copy(alpha = 0.14f),
    onSecondaryContainer = Color(0xFF3A2811),
    tertiaryContainer = KazeAccent.copy(alpha = 0.20f),
    onTertiaryContainer = Color(0xFF4E3F21),
    surfaceVariant = KazeSurfaceVariant,
    onSurfaceVariant = KazeOnSurfaceVariant,
    outline = KazeOutline,
)

private data class KazeUiPalette(
    val ambientBottom: Color = KazeSurfaceVariant,
    val ambientLineStrong: Color = KazePrimary.copy(alpha = 0.13f),
    val ambientLineSoft: Color = KazeAccent.copy(alpha = 0.10f),
    val ambientCirclePrimary: Color = KazePrimary.copy(alpha = 0.08f),
    val ambientCircleSecondary: Color = KazeAccent.copy(alpha = 0.07f),
    val ambientPanelTop: Color = KazePrimary.copy(alpha = 0.035f),
    val ambientPanelBottom: Color = KazeAccent.copy(alpha = 0.028f),
    val floatingShell: Color = KazeSurface,
    val floatingShellBorder: Color = KazeSecondary.copy(alpha = 0.20f),
)

private val kazeUi = KazeUiPalette()

private enum class KazeSection(val label: String, val icon: ImageVector) {
    Markets("Markets", Icons.Filled.Storefront),
    Basket("Basket", Icons.Filled.ShoppingBasket),
    Home("Sequo", Icons.Filled.Home),
    Orders("Orders", Icons.AutoMirrored.Filled.ReceiptLong),
    Account("Account", Icons.Filled.Person),
}

private val kazePrimaryDestinations = listOf(
    KazeSection.Markets,
    KazeSection.Basket,
    KazeSection.Home,
    KazeSection.Orders,
    KazeSection.Account,
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
        kind = "General goods",
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
        name = "Akodessewa Maison Sante",
        area = "Akodessewa",
        kind = "Home essentials",
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
                name = "Thermometre digital",
                detail = "Real seller camera photo before checkout",
                priceCfa = 6500,
                label = "Live",
                optionHint = "Battery included",
            ),
        ),
    ),
)

private data class BasketEntry(
    val shop: SequoShop,
    val product: SequoProduct,
    val quantity: Int,
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

private val quickCategories = listOf("Food now", "Fresh market", "Phones", "Water", "Bargains")

private fun featuredProductsFor(category: String): List<Pair<SequoShop, SequoProduct>> =
    when (category) {
        "Food now" -> listOf(sequoShops[0] to sequoShops[0].products[0], sequoShops[0] to sequoShops[0].products[1])
        "Fresh market" -> listOf(sequoShops[1] to sequoShops[1].products[1], sequoShops[1] to sequoShops[1].products[0])
        "Phones" -> listOf(sequoShops[2] to sequoShops[2].products[0], sequoShops[2] to sequoShops[2].products[1])
        "Water" -> listOf(sequoShops[3] to sequoShops[3].products[0], sequoShops[3] to sequoShops[3].products[1])
        else -> listOf(sequoShops[1] to sequoShops[1].products[0], sequoShops[2] to sequoShops[2].products[1])
    }

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = kazeColorScheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SequoKazeShell()
        }
    }
}

@Composable
private fun SequoKazeShell() {
    var currentDestination by remember { mutableStateOf(KazeSection.Home) }
    var extraBasketItems by remember { mutableStateOf(0) }
    val basketCount = sequoBasket.sumOf { it.quantity } + extraBasketItems

    Box(modifier = Modifier.fillMaxSize()) {
        KazeAmbientBackground(modifier = Modifier.fillMaxSize())
        SequoContentStage(
            currentDestination = currentDestination,
            onDestinationSelected = { currentDestination = it },
            extraBasketItems = extraBasketItems,
            onAddProduct = { extraBasketItems += 1 },
            modifier = Modifier.fillMaxSize(),
        )
        KazeBottomBar(
            currentDestination = currentDestination,
            onDestinationSelected = { currentDestination = it },
            pendingBasketCount = basketCount,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SequoContentStage(
    currentDestination: KazeSection,
    onDestinationSelected: (KazeSection) -> Unit,
    extraBasketItems: Int,
    onAddProduct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KazeScreenColumn(modifier = modifier) {
        when (currentDestination) {
            KazeSection.Home -> HomeContent(onDestinationSelected, onAddProduct)
            KazeSection.Markets -> MarketsContent(onAddProduct)
            KazeSection.Basket -> BasketContent(extraBasketItems)
            KazeSection.Orders -> OrdersContent()
            KazeSection.Account -> AccountContent()
        }
    }
}

@Composable
private fun KazeScreenColumn(
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
    onDestinationSelected: (KazeSection) -> Unit,
    onAddProduct: () -> Unit,
) {
    var selectedCategory by remember { mutableStateOf(quickCategories.first()) }

    HomeAppBar()
    KazeHeroCard(
        eyebrow = "Lome today",
        title = "Tokoin lunch, Assigame shopping, Hedzranawoe tech.",
        body = "One customer app for hot meals, fresh market errands, and general goods, with delivery fees visible before checkout.",
        primaryLabel = "Shop Lome",
        secondaryLabel = "Track order",
        onPrimary = { onDestinationSelected(KazeSection.Markets) },
        onSecondary = { onDestinationSelected(KazeSection.Orders) },
    )
    KazeSearchCard()
    CategoryRail(
        categories = quickCategories,
        selectedCategory = selectedCategory,
        onCategorySelected = { selectedCategory = it },
    )
    KazeMetricRow(
        leftValue = "1.8 km",
        leftLabel = "Chez Ramatou",
        rightValue = "400 CFA",
        rightLabel = "Tokoin delivery",
    )
    KazeSectionCard(title = "Open around you", action = "Tokoin radius") {
        sequoShops.take(3).forEach { shop ->
            ShopSummaryRow(shop = shop)
        }
    }
    KazeSectionCard(title = "Ready to add", action = selectedCategory) {
        featuredProductsFor(selectedCategory).forEach { (shop, product) ->
            CompactProductCard(shop, product, onAddProduct)
        }
    }
    KazeSectionCard(title = "Sequo promises", action = "before payment") {
        RuleRow("Live seller camera photos", "Gallery uploads blocked except generic water and sealed basics.")
        RuleRow("Yas Togo or Moov Africa", "Payment is validated before the order is completed.")
        RuleRow("72h Point de Relai returns", "Refund starts after Sequo inspection accepts the product.")
    }
}

@Composable
private fun MarketsContent(onAddProduct: () -> Unit) {
    var selectedArea by remember { mutableStateOf("All Lome") }
    val visibleShops = if (selectedArea == "All Lome") {
        sequoShops
    } else {
        sequoShops.filter { shop ->
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
    KazeIntroCard(
        eyebrow = "Markets",
        title = "Browse real sellers across Lome.",
        subtitle = "Every shop card shows distance, delivery fee, ETA, live photo rules, bargaining status, and whether Sequo can group the pickup.",
    )
    CategoryRail(
        categories = listOf("All Lome", "Tokoin", "Assigame", "Hedzranawoe", "Akodessewa"),
        selectedCategory = selectedArea,
        onCategorySelected = { selectedArea = it },
    )
    LomeRouteCard()
    visibleShops.forEach { shop ->
        KazeShopCard(shop = shop, onAddProduct = onAddProduct)
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
    KazeSectionCard(title = "Basket", action = "${sequoBasket.sumOf { it.quantity } + extraBasketItems} items") {
        sequoBasket.forEach { entry ->
            BasketLine(entry)
        }
        if (extraBasketItems > 0) {
            BasketAddedLine(extraBasketItems)
        }
    }
    KazeSectionCard(title = "Food options", action = "custom") {
        RuleRow("Attieke poisson braise", sequoShops[0].products[0].optionHint)
        RuleRow("No mixing with goods", "Hot food travels in a separate sealed bag inside the Sequo package.")
    }
    KazeSectionCard(title = "Consolidation", action = "Lome route") {
        RuleRow("Assigame + Akodessewa", "Eligible for one Sequo package after inspection at pickup.")
        RuleRow("Food exception", "Tokoin hot meal keeps its own thermal seal and pickup timing.")
    }
    KazeCheckoutCard(extraBasketItems, selectedPayment, onPaymentSelected = { selectedPayment = it })
}

@Composable
private fun OrdersContent() {
    SequoAppBar(
        title = "Orders",
        subtitle = "Live route and returns",
        leadingIcon = Icons.AutoMirrored.Filled.ReceiptLong,
        actions = listOf(
            AppBarAction(Icons.Filled.SupportAgent, "Contact support"),
            AppBarAction(Icons.Filled.Map, "Open route", emphasized = true),
        ),
    )
    SequoStatusStrip(
        icon = Icons.Filled.Map,
        title = "Rider route active",
        detail = "Tokoin pickup is moving toward Pharmacie des Etoiles.",
        tag = "9 min",
    )
    KazeIntroCard(
        eyebrow = "Orders",
        title = "SQ-2419 is moving through Tokoin.",
        subtitle = "Chez Ramatou is sealed, payment is validated, and the rider is heading toward Pharmacie des Etoiles. Delivery PIN: 4821.",
    )
    KazeSectionCard(title = "Live progress", action = "9 min") {
        StepRow("Paid with Yas Togo", active = false, detail = "12:18")
        StepRow("Seller camera photo approved", active = false, detail = "12:21")
        StepRow("Thermal seal applied", active = false, detail = "12:27")
        StepRow("Rider approaching Tokoin", active = true, detail = "now")
    }
    KazeSectionCard(title = "Recent orders", action = "receipts") {
        OrderMemory("Pagne wax 6 yards", "Delivered yesterday", "Return by 18:40")
        OrderMemory("Oraimo charger 20W", "Delivered Saturday", "Return by Tuesday")
        OrderMemory("Attieke poisson braise", "Delivered Friday", "Food order closed")
    }
    ReturnHubCard()
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
    KazePassCard()
    KazeSectionCard(title = "Saved places", action = "Lome") {
        AccountAddressRow("Home", "Tokoin Gbadago, near Pharmacie des Etoiles")
        AccountAddressRow("Family", "Adidogome, carrefour Limousine")
        AccountAddressRow("Office", "Be-Kpota, route du marche")
    }
    KazeSectionCard(title = "Account tools", action = "secure") {
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
    KazeCard(shape = RoundedCornerShape(28.dp)) {
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
            MetaPill(tag, KazeSecondary)
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
private fun KazeSearchCard() {
    KazeCard(shape = RoundedCornerShape(24.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SequoIconMark(Icons.Filled.Search, KazeSecondary, Modifier.size(34.dp))
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
private fun CategoryRail(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { category ->
                    KazeFilterChip(
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
private fun KazeFilterChip(
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
private fun LomeRouteCard() {
    KazeCard(shape = RoundedCornerShape(24.dp)) {
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
private fun KazeHeroCard(
    eyebrow: String,
    title: String,
    body: String,
    primaryLabel: String,
    secondaryLabel: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
) {
    KazeCard(shape = RoundedCornerShape(32.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            KazeSecondary.copy(alpha = 0.20f),
                            KazeAccent.copy(alpha = 0.16f),
                            KazePrimary.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(KazeSecondary.copy(alpha = 0.12f), radius = size.minDimension * 0.42f, center = Offset(size.width * 0.92f, size.height * 0.06f))
                drawCircle(KazePrimary.copy(alpha = 0.10f), radius = size.minDimension * 0.30f, center = Offset(size.width * 0.10f, size.height * 0.84f))
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
                        border = BorderStroke(1.dp, KazeSecondary.copy(alpha = 0.22f)),
                    ) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                            SequoMonogram("SQ", KazeSecondary, Modifier.size(42.dp))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaPill("400 CFA nearby", KazePrimary)
                    MetaPill("Live photos", KazeAccent)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KazePrimaryButton(primaryLabel, onPrimary, Modifier.weight(1f))
                    KazeSecondaryButton(secondaryLabel, onSecondary, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KazeMetricRow(
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
    KazeCard(modifier = modifier, shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f))
        }
    }
}

@Composable
private fun KazeIntroCard(eyebrow: String, title: String, subtitle: String) {
    KazeCard(shape = RoundedCornerShape(24.dp)) {
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
private fun KazeSectionCard(title: String, action: String, content: @Composable ColumnScope.() -> Unit) {
    KazeCard {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            content()
        }
    }
}

@Composable
private fun KazeShopCard(shop: SequoShop, onAddProduct: () -> Unit) {
    KazeCard {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                ShopMark(shop.name)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(shop.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${shop.area} / ${shop.kind}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(shop.openStatus, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                RatingMark(shop.rating)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill(formatDistance(shop.distanceKm), KazePrimary)
                MetaPill(formatCfa(baseDelivery(shop.distanceKm)), KazeSecondary)
                MetaPill(shop.eta, KazeAccent)
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
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        ShopMark(shop.name)
        Column(Modifier.weight(1f)) {
            Text(shop.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${shop.area} / ${formatDistance(shop.distanceKm)} / ${formatCfa(baseDelivery(shop.distanceKm))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        MetaPill(shop.eta, KazeAccent)
    }
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
                MetaPill(product.label, if (product.bargainNote == null) KazeAccent else KazeSecondary)
                Text(product.optionHint, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                KazeTinyButton("Add", onAddProduct)
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
            KazeTinyButton("Add", onAddProduct)
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
    KazeCard(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Delivery address", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("edit", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            RuleRow("Tokoin Gbadago", "Near Pharmacie des Etoiles, call when outside.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill("5.6 km route", KazePrimary)
                MetaPill("Subscriber -15%", KazeSecondary)
            }
        }
    }
}

@Composable
private fun KazeCheckoutCard(
    extraBasketItems: Int,
    selectedPayment: String,
    onPaymentSelected: (String) -> Unit,
) {
    val subtotal = sequoBasket.sumOf { it.product.priceCfa * it.quantity } + (extraBasketItems * 3500)
    val delivery = calculateDeliveryPricing(
        DeliveryPricingInput(distanceKm = 5.6, subscriptionDiscountPercent = 15, referralCreditCfa = 500),
    )
    val total = subtotal + delivery.finalDeliveryFeeCfa

    KazeSectionCard(title = "Pay securely", action = selectedPayment) {
        ValueRow("Items", formatCfa(subtotal))
        ValueRow("Delivery", formatCfa(delivery.baseFeeCfa))
        ValueRow("Subscriber", "-${formatCfa(delivery.subscriptionDiscountCfa)}")
        ValueRow("Parrainage", "-${formatCfa(delivery.referralCreditAppliedCfa)}")
        ValueRow("Total", formatCfa(total), strong = true)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PaymentChoice("Yas Togo", selectedPayment, onPaymentSelected, Modifier.weight(1f))
            PaymentChoice("Moov Africa", selectedPayment, onPaymentSelected, Modifier.weight(1f))
        }
        KazePrimaryButton("Pay ${formatCfa(total)}", {}, Modifier.fillMaxWidth())
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
    KazeSecondaryButton(
        label = label,
        onClick = { onPaymentSelected(label) },
        modifier = modifier,
        emphasized = label == selectedPayment,
    )
}

@Composable
private fun ReturnHubCard() {
    KazeSectionCard(title = "Return hub", action = "72 hours") {
        RuleRow("Point de Relai Tokoin", "Open 08:00-19:00, accepts sealed general goods returns.")
        RuleRow("Inspection status", "Refund triggers automatically when Sequo accepts the returned product.")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KazeSecondaryButton("Start return", {}, Modifier.weight(1f), emphasized = true)
            KazeSecondaryButton("Find point", {}, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AccountAddressRow(label: String, address: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        SequoMiniMark(label.take(1), KazeSecondary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun KazePassCard() {
    KazeCard(shape = RoundedCornerShape(34.dp), color = Color.Transparent, border = null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF111419), Color(0xFF18242B), Color(0xFF24404A))))
                .border(1.dp, KazeAccent.copy(alpha = 0.28f), RoundedCornerShape(34.dp))
                .padding(20.dp),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(KazeAccent.copy(alpha = 0.18f), radius = size.width * 0.26f, center = Offset(size.width * 0.92f, size.height * 0.16f))
                drawRoundRect(Color(0x14FFF9F0), Offset(-34.dp.toPx(), size.height - 74.dp.toPx()), Size(180.dp.toPx(), 82.dp.toPx()), CornerRadius(44.dp.toPx(), 24.dp.toPx()))
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("SEQUO ACCESS", style = MaterialTheme.typography.labelSmall, color = Color(0xCCFFF8EE))
                Text("Afi K.", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFFFFBF5), fontWeight = FontWeight.SemiBold)
                Text("Lome subscriber / 15% delivery discount", style = MaterialTheme.typography.bodyMedium, color = Color(0xCCFFF8EE))
                Box(Modifier.height(44.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaPill("500 CFA credit", KazeAccent, inverse = true)
                    MetaPill("3 Lome addresses", KazePrimary, inverse = true)
                }
            }
        }
    }
}

@Composable
private fun KazeCard(
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
private fun KazePrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
private fun KazeSecondaryButton(
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
private fun KazeTinyButton(label: String, onClick: () -> Unit) {
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
            .background(Brush.linearGradient(listOf(KazePrimary.copy(alpha = 0.88f), KazeSecondary.copy(alpha = 0.72f)))),
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
private fun OrderMemory(name: String, status: String, note: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        ShopMark(name)
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(note, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

private fun formatDistance(km: Double): String {
    val tenths = (km * 10).roundToInt()
    return if (tenths % 10 == 0) "${tenths / 10} km" else "${tenths / 10}.${tenths % 10} km"
}

private fun formatCfa(amount: Int): String =
    amount.toString().reversed().chunked(3).joinToString(" ").reversed() + " CFA"

@Composable
private fun KazeAmbientBackground(modifier: Modifier = Modifier) {
    val uiPalette = kazeUi
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
private fun KazeBottomBar(
    modifier: Modifier = Modifier,
    currentDestination: KazeSection,
    onDestinationSelected: (KazeSection) -> Unit,
    pendingBasketCount: Int = 0,
    destinations: List<KazeSection> = kazePrimaryDestinations,
) {
    KazeNavigationContainer(
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
                KazeBottomNavItem(
                    destination = destination,
                    selected = currentDestination == destination,
                    onClick = { onDestinationSelected(destination) },
                    badgeCount = if (destination == KazeSection.Basket) pendingBasketCount else 0,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun KazeNavigationContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val uiPalette = kazeUi
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
private fun KazeNavigationItemFrame(
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
private fun KazeBottomNavItem(
    destination: KazeSection,
    selected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int,
    modifier: Modifier = Modifier,
) {
    KazeNavigationItemFrame(
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
