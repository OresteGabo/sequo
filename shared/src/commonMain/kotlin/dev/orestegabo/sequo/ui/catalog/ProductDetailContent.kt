package dev.orestegabo.sequo.ui.catalog

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.bargaining.*
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
import kotlin.math.abs
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.*

@Composable
internal fun ProductDetailContent(
    listing: SequoProductListing,
    onAddCustomized: suspend (String, List<ProductOptionSelection>) -> Int?,
    allShops: List<SequoShop>,
    onAddProduct: (String) -> Unit,
    onProductSelected: (SequoProductListing) -> Unit,
    bargainingRepository: BargainingRepository?,
    onSignInRequested: () -> Unit,
    onAddNegotiated: suspend (String, List<ProductOptionSelection>, String) -> Int?,
) {
    val product = listing.product
    val shop = listing.shop
    val suggestions = remember(listing, allShops) { similarProductListings(listing, allShops) }
    ProductDetailHero(product)
    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProductDetailInfoCard(
            listing = listing,
            onAddCustomized = onAddCustomized,
            onAddProduct = onAddProduct,
            bargainingRepository = bargainingRepository,
            onSignInRequested = onSignInRequested,
            onAddNegotiated = onAddNegotiated,
        )
        ProductSellerCard(shop = shop)
        ProductSuggestionsSection(
            current = listing,
            allShops = allShops,
            suggestions = suggestions,
            onAddProduct = onAddProduct,
            onProductSelected = onProductSelected,
        )
    }
}

@Composable
private fun ProductDetailHero(product: SequoProduct) {
    var photoActionProduct by remember(product.id) { mutableStateOf<SequoProduct?>(null) }
    Box(Modifier.fillMaxWidth().height(280.dp).padding(horizontal = 20.dp)
        .clip(RoundedCornerShape(24.dp))
        .combinedClickable(onClick = { photoActionProduct = product }, onLongClick = { photoActionProduct = product })) {
        CatalogProductImage(product, Modifier.fillMaxSize(), ContentScale.Crop)
        PhotoAuthenticityBadge(product, onDark = true,
            modifier = Modifier.align(Alignment.TopStart).padding(14.dp))
        Surface(shape = CircleShape, color = Color.Black.copy(alpha = .45f),
            modifier = Modifier.align(Alignment.BottomEnd).padding(14.dp).size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ZoomIn, appText(Res.string.polish_view_photo), tint = Color.White, modifier = Modifier.size(21.dp))
            }
        }
    }
    photoActionProduct?.let { ProductPhotoActionDialog(it, onDismiss = { photoActionProduct = null }) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductDetailInfoCard(
    listing: SequoProductListing,
    onAddCustomized: suspend (String, List<ProductOptionSelection>) -> Int?,
    onAddProduct: (String) -> Unit,
    bargainingRepository: BargainingRepository?,
    onSignInRequested: () -> Unit,
    onAddNegotiated: suspend (String, List<ProductOptionSelection>, String) -> Int?,
) {
    val product = listing.product
    var acceptedThreadId by remember(product.id, bargainingRepository) { mutableStateOf<String?>(null) }
    SequoCard(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(formatProductPrice(product), style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                product.originalPriceCfa?.takeIf { product.hasDiscount }?.let {
                    Text(formatCfa(it), style = MaterialTheme.typography.bodyMedium,
                        textDecoration = TextDecoration.LineThrough, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ProductDiscountBadge(product)
            }
            if (product.detail.isNotBlank()) Text(product.detail, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (product.optionHint.isNotBlank()) Text(product.optionHint, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (product.isNegotiable) {
                ProductBargainingPanel(product, bargainingRepository, onSignInRequested) { acceptedThreadId = it }
            }
            if (product.customizations.isEmpty() && acceptedThreadId == null) {
                SequoPrimaryButton(appText(Res.string.home_add), { onAddProduct(listing.product.id) }, Modifier.fillMaxWidth())
            } else {
                ProductOptionsPanel(listing.product, showPricePreview = acceptedThreadId == null) { productId, selections ->
                    val threadId = acceptedThreadId
                    if (threadId == null) onAddCustomized(productId, selections)
                    else onAddNegotiated(productId, selections, threadId)
                }
            }
        }
    }
}
@Composable
private fun ProductConcernPanel(product: SequoProduct) {
    val isFood = product.isFoodOrGrocery()
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.56f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.36f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isFood) Icons.Filled.Restaurant else Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    if (isFood) "Food notes" else "Product notes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (isFood) {
                ProductNoteRow(Icons.AutoMirrored.Filled.MenuBook, "Recipe idea", product.recipeSuggestion())
                ProductNoteRow(Icons.Filled.AcUnit, "Storage", product.storageSuggestion())
                ProductNoteRow(Icons.Filled.Inventory2, "Packaging", "Sealed bag or tray; keep separate from household items.")
                ProductNoteRow(Icons.Filled.Schedule, "Use soon", "Best checked at pickup and used within the freshness window.")
            } else {
                ProductNoteRow(Icons.Filled.CheckCircle, "Check", "Confirm size, color, and condition before checkout.")
                ProductNoteRow(Icons.Filled.Inventory2, "Packaging", "Seller packaging shown before delivery when available.")
                ProductNoteRow(Icons.Filled.LocalOffer, "Price", if (product.isNegotiable) "Offers are available for this item." else "Listed as fixed price.")
            }
        }
    }
}

@Composable
private fun ProductNoteRow(icon: ImageVector, title: String, detail: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp).padding(top = 1.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProductSellerCard(shop: SequoShop) {
    SequoCard(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                SequoShoppingMark(Modifier.size(46.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(shop.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    listOfNotNull(shop.area, shop.kind).joinToString(" / ").takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
                RatingMark(shop.rating)
            }
            ShopFacts(shop)
            RuleRow(shop.openStatus, shop.consolidation)
        }
    }
}

private fun SequoProduct.isFoodOrGrocery(): Boolean {
    val text = "$name $detail $label $subcategory".lowercase()
    return listOf(
        "food",
        "grocery",
        "produce",
        "meat",
        "seafood",
        "milk",
        "lait",
        "boeuf",
        "tilapia",
        "saumon",
        "poivron",
        "tomate",
        "farine",
        "cajou",
        "coco",
        "soja",
    ).any { it in text }
}

private fun SequoProduct.recipeSuggestion(): String {
    val text = "$name $detail $label".lowercase()
    return when {
        "poivron" in text -> "Slice into sauces, omelets, fried rice, or grilled meat plates."
        "boeuf" in text || "beef" in text || "steak" in text -> "Works for skewers, quick stew, stir-fry, or pepper sauce."
        "tilapia" in text || "poisson" in text || "fish" in text -> "Good grilled, fried, or cooked in tomato-onion sauce."
        "saumon" in text || "salmon" in text -> "Pan-sear, oven-bake, or pair with rice and vegetables."
        "lait" in text || "milk" in text -> "Use for breakfast, smoothies, baking, or tea/coffee."
        "farine" in text || "flour" in text -> "Useful for bread, pancakes, fritters, and simple batters."
        "cajou" in text || "cashew" in text -> "Snack, blend into sauces, or add crunch to rice and salads."
        "coco" in text || "coconut" in text -> "Use fresh, grate into desserts, or add to rice and sauces."
        "soja" in text || "soy" in text -> "Soak before cooking; good for stews, milk, or protein-rich meals."
        else -> "Use in everyday meals; check freshness and quantity before checkout."
    }
}

private fun SequoProduct.storageSuggestion(): String {
    val text = "$name $detail $optionHint".lowercase()
    return when {
        "chilled" in text || "fresh milk" in text || "lait" in text -> "Keep cold and deliver quickly."
        "frozen" in text -> "Keep frozen until preparation."
        "meat" in text || "boeuf" in text || "fish" in text || "poisson" in text || "saumon" in text -> "Keep chilled; request sealed cold packaging."
        "dry" in text || "farine" in text || "soja" in text || "beans" in text -> "Store dry, sealed, and away from humidity."
        else -> "Keep cool, clean, and separate from cleaning products."
    }
}

@Composable
private fun ProductSuggestionsSection(
    current: SequoProductListing,
    allShops: List<SequoShop>,
    suggestions: List<SequoProductListing>,
    onAddProduct: (String) -> Unit,
    onProductSelected: (SequoProductListing) -> Unit,
) {
    var selectedTab by remember(current) { mutableStateOf(ProductSuggestionTab.SameShop) }
    val sameShopOptions = remember(current, allShops) { sameShopProductListings(current, allShops) }
    val otherShopOptions = remember(current, suggestions) {
        suggestions.filterNot { it.shop.name == current.shop.name }
    }
    val visibleSuggestions = when (selectedTab) {
        ProductSuggestionTab.SameShop -> sameShopOptions
        ProductSuggestionTab.OtherShops -> otherShopOptions
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(appText(Res.string.polish_more_options), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                ProductSuggestionTab.entries.forEach { tab ->
                    val count = when (tab) {
                        ProductSuggestionTab.SameShop -> sameShopOptions.size
                        ProductSuggestionTab.OtherShops -> otherShopOptions.size
                    }
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(appText(if (tab == ProductSuggestionTab.SameShop) Res.string.polish_same_shop else Res.string.polish_other_shops), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    "$count ${appText(Res.string.polish_items)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        },
                    )
                }
            }
        }
        if (visibleSuggestions.isEmpty()) {
            Text(
                when (selectedTab) {
                    ProductSuggestionTab.SameShop -> appText(Res.string.polish_no_same_shop)
                    ProductSuggestionTab.OtherShops -> appText(Res.string.polish_no_other_shops)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            visibleSuggestions.forEach { suggestion ->
                CompactProductCard(
                    shop = suggestion.shop,
                    product = suggestion.product,
                    comparisonPriceCfa = current.product.priceCfa,
                    relationLabel = null,
                    onAddProduct = onAddProduct,
                    onProductClick = { onProductSelected(suggestion) },
                    onNegotiateClick = { onProductSelected(suggestion) },
                )
            }
        }
    }
}

private enum class ProductSuggestionTab {
    SameShop,
    OtherShops;

    val label: String
        get() = when (this) {
            SameShop -> "Same shop"
            OtherShops -> "Other shops"
        }
}

@Composable
private fun DetailChip(icon: ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
    }
}

private fun similarProductListings(current: SequoProductListing, allShops: List<SequoShop>): List<SequoProductListing> {
    val currentRelationKey = productRelationKey(current.product)
    val currentSubcategory = normalizedProductSubcategory(current.product)
    return allShops
        .flatMap { shop -> shop.products.map { product -> SequoProductListing(shop, product) } }
        .filterNot { it.shop.name == current.shop.name && it.product.name == current.product.name }
        .filter { listing ->
            val product = listing.product
            productRelationKey(product) == currentRelationKey ||
                normalizedProductSubcategory(product) == currentSubcategory ||
                product.label == current.product.label ||
                listing.shop.kind != null && listing.shop.kind == current.shop.kind
        }
        .sortedWith(
            compareByDescending<SequoProductListing> { listing ->
                when {
                    productRelationKey(listing.product) == currentRelationKey -> 4
                    normalizedProductSubcategory(listing.product) == currentSubcategory -> 3
                    listing.product.label == current.product.label -> 2
                    listing.shop.kind != null && listing.shop.kind == current.shop.kind -> 1
                    else -> 0
                }
            }.thenBy { listing ->
                abs(listing.product.priceCfa - current.product.priceCfa)
            }.thenBy { listing ->
                listing.shop.distanceKm ?: Double.POSITIVE_INFINITY
            },
        )
        .take(6)
}

private fun sameShopProductListings(current: SequoProductListing, allShops: List<SequoShop>): List<SequoProductListing> {
    val currentRelationKey = productRelationKey(current.product)
    val currentSubcategory = normalizedProductSubcategory(current.product)
    val currentShop = allShops.firstOrNull { it.name == current.shop.name } ?: current.shop
    return currentShop.products
        .filterNot { it.name == current.product.name }
        .sortedWith(
            compareByDescending<SequoProduct> { product ->
                when {
                    productRelationKey(product) == currentRelationKey -> 4
                    normalizedProductSubcategory(product) == currentSubcategory -> 3
                    product.label == current.product.label -> 2
                    else -> 1
                }
            }.thenBy { product ->
                abs(product.priceCfa - current.product.priceCfa)
            },
        )
        .map { product -> SequoProductListing(current.shop, product) }
        .take(6)
}

private fun suggestionExplainer(tab: ProductSuggestionTab, current: SequoProductListing): String {
    val relation = productRelationLabel(current.product).lowercase()
    return when (tab) {
        ProductSuggestionTab.SameShop -> "More from ${current.shop.name}; closest $relation alternatives stay first."
        ProductSuggestionTab.OtherShops -> "Similar $relation options from other sellers, sorted by closest match and price."
    }
}

private fun productRelationLabel(product: SequoProduct): String =
    when (productRelationKey(product)) {
        "smartphones" -> "smartphone"
        "laptops" -> "laptop"
        "jollof-rice" -> "jollof plate"
        "attieke-fish" -> "attieke fish plate"
        "grilled-food" -> "grilled food"
        "rice-bag" -> "rice bag"
        "cooking-oil" -> "cooking oil"
        "power-bank" -> "power bank"
        "earbuds" -> "earbuds"
        "t-shirt" -> "T-shirt"
        "sanitizer" -> "sanitizer"
        "pastry" -> "pastry"
        else -> productSubcategory(product)
    }

private fun productRelationKey(product: SequoProduct): String {
    val text = "${product.name} ${product.detail} ${product.label} ${product.subcategory} ${product.optionHint}".lowercase()
    return when {
        "iphone" in text || "smartphone" in text || "poco" in text -> "smartphones"
        ("laptop" in text || "ordinateur" in text) && "sleeve" !in text -> "laptops"
        "jollof" in text || "riz gras" in text -> "jollof-rice"
        "attieke" in text && ("fish" in text || "tilapia" in text || "poisson" in text) -> "attieke-fish"
        "grill" in text || "brochette" in text || "shawarma" in text || "kebab" in text -> "grilled-food"
        "rice bag" in text || "local rice" in text -> "rice-bag"
        "vegetable oil" in text || "palm oil" in text || "huile" in text -> "cooking-oil"
        "power bank" in text -> "power-bank"
        "earbuds" in text || "headset" in text -> "earbuds"
        "tee" in text || "t-shirt" in text -> "t-shirt"
        "sanitizer" in text -> "sanitizer"
        "croissant" in text || "pastry" in text || "pain au lait" in text -> "pastry"
        else -> normalizedProductSubcategory(product)
    }
}

private fun normalizedProductSubcategory(product: SequoProduct): String =
    productSubcategory(product).lowercase().trim()
