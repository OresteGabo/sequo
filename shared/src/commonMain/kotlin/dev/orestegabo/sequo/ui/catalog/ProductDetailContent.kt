package dev.orestegabo.sequo.ui.catalog

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
internal fun ProductDetailContent(
    listing: SequoProductListing,
    onAddProduct: () -> Unit,
    onProductSelected: (SequoProductListing) -> Unit,
    onNegotiateClick: () -> Unit,
) {
    val product = listing.product
    val shop = listing.shop
    val suggestions = remember(listing) { similarProductListings(listing) }

    ProductDetailHero(product = product)
    Column(
        modifier = Modifier
            .offset(y = (-28).dp)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ProductDetailInfoCard(
            listing = listing,
            onAddProduct = onAddProduct,
            onNegotiateClick = onNegotiateClick,
        )
        ProductSellerCard(shop = shop)
        ProductSuggestionsSection(
            suggestions = suggestions,
            onAddProduct = onAddProduct,
            onProductSelected = onProductSelected,
        )
    }
}

@Composable
private fun ProductDetailHero(product: SequoProduct) {
    Box(modifier = Modifier.fillMaxWidth().height(348.dp)) {
        Image(
            painter = painterResource(productImageResource(product)),
            contentDescription = product.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.54f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.50f),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                ),
        )
        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, end = 20.dp, bottom = 46.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (product.isNegotiable) {
                DetailChip(Icons.Filled.LocalOffer, "Offer")
            }
            if (product.hasDiscount) {
                DetailChip(Icons.Filled.Percent, "Discount")
            }
        }
    }
}

@Composable
private fun ProductDetailInfoCard(
    listing: SequoProductListing,
    onAddProduct: () -> Unit,
    onNegotiateClick: () -> Unit,
) {
    val product = listing.product
    SequoCard(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(product.detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(product.optionHint, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f))
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(formatCfa(product.priceCfa), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                    product.originalPriceCfa?.takeIf { product.hasDiscount }?.let { original ->
                        Text(
                            "Was ${formatCfa(original)} / save ${formatCfa(original - product.priceCfa)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                ProductDiscountBadge(product = product)
            }
            ProductConcernPanel(product = product)
            if (product.isNegotiable) {
                Surface(
                    onClick = onNegotiateClick,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Make an offer", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(product.bargainNote ?: "Send a price proposal to the seller.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            SequoPrimaryButton("Add to cart", onAddProduct, Modifier.fillMaxWidth())
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
                SequoIconMark(Icons.Filled.Storefront, MaterialTheme.colorScheme.primary, Modifier.size(46.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(shop.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${shop.area} / ${shop.kind}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                RatingMark(shop.rating)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MetaPill(formatDistance(shop.distanceKm), SequoPrimary)
                MetaPill(formatCfa(baseDelivery(shop.distanceKm)), SequoSecondary)
                MetaPill(shop.eta, SequoAccent)
            }
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
    suggestions: List<SequoProductListing>,
    onAddProduct: () -> Unit,
    onProductSelected: (SequoProductListing) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MarketplaceSectionHeader(
            title = "Similar options",
            action = "${suggestions.size} found",
            onAction = {},
        )
        if (suggestions.isEmpty()) {
            Text(
                "No close alternatives yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            suggestions.forEach { suggestion ->
                CompactProductCard(
                    shop = suggestion.shop,
                    product = suggestion.product,
                    onAddProduct = onAddProduct,
                    onProductClick = { onProductSelected(suggestion) },
                )
            }
        }
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

private fun similarProductListings(current: SequoProductListing): List<SequoProductListing> {
    val currentSubcategory = productSubcategory(current.product)
    return sequoShops
        .flatMap { shop -> shop.products.map { product -> SequoProductListing(shop, product) } }
        .filterNot { it.shop.name == current.shop.name && it.product.name == current.product.name }
        .sortedByDescending { listing ->
            val product = listing.product
            when {
                productSubcategory(product) == currentSubcategory -> 3
                product.label == current.product.label -> 2
                listing.shop.kind == current.shop.kind -> 1
                else -> 0
            }
        }
        .filter { listing ->
            productSubcategory(listing.product) == currentSubcategory ||
                listing.product.label == current.product.label ||
                listing.shop.kind == current.shop.kind
        }
        .take(6)
}
