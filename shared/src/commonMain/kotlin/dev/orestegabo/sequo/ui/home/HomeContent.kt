package dev.orestegabo.sequo.ui.home

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
import dev.orestegabo.sequo.core.catalog.CatalogSnapshot
import dev.orestegabo.sequo.data.*
import dev.orestegabo.sequo.domain.*
import dev.orestegabo.sequo.feature.settings.appCatalogText
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
internal fun HomeContent(
    catalog: CatalogSnapshot,
    onDestinationSelected: (SequoSection) -> Unit,
    onAddProduct: (String) -> Unit,
    onProductSelected: (SequoProductListing) -> Unit,
    categoryUsage: Map<String, Int>,
    pinnedCategoryKeys: List<String>,
    onCategoryUsed: (String) -> Unit,
    onToggleCategoryPinned: (String) -> Unit,
) {
    var selectedTypeKey by remember(catalog.defaultCategoryKey) { mutableStateOf(catalog.defaultCategoryKey) }
    // Record new usage for future visits without moving a category under the current tap.
    val usageAtEntry = remember { categoryUsage.toMap() }
    val orderedTypes = personalizedShopTypes(catalog.categories, usageAtEntry, pinnedCategoryKeys)
    val selectedType = catalog.categoryFor(selectedTypeKey)
    val selectedShops = catalog.shopsForCategory(selectedTypeKey)
    val featuredProducts = catalog.featuredProductsFor(selectedTypeKey)

    ProductPromoCarousel(
        promos = catalog.promotions,
        onAddProduct = onAddProduct,
        onSeeAll = { onDestinationSelected(SequoSection.Markets) },
        onProductSelected = onProductSelected,
        onNegotiateClick = { product -> catalog.products.firstOrNull { it.product.id == product.id }?.shop?.let { onProductSelected(SequoProductListing(it, product)) } },
    )
    MarketplaceCategorySection(
        types = orderedTypes,
        selectedTypeKey = selectedTypeKey,
        pinnedCategoryKeys = pinnedCategoryKeys,
        onTypeSelected = {
            selectedTypeKey = it
            onCategoryUsed(it)
        },
        onTogglePinned = onToggleCategoryPinned,
    )
    if (featuredProducts.isEmpty()) {
        SequoEmptyState(
            visual = EmptyStateVisual.Catalog,
            title = appText(Res.string.empty_filter_title),
            detail = appText(Res.string.empty_filter_detail),
            actionLabel = appText(Res.string.empty_browse_all),
            onAction = { onDestinationSelected(SequoSection.Markets) },
            compact = true,
        )
    } else FlashSaleSection(
        title = appText(Res.string.home_popular_picks),
        badge = appCatalogText(selectedType.supportLabel),
        products = featuredProducts,
        onAddProduct = onAddProduct,
        onSeeAll = { onDestinationSelected(SequoSection.Markets) },
        onProductSelected = { shop, product -> onProductSelected(SequoProductListing(shop, product)) },
        onNegotiateClick = { product -> featuredProducts.firstOrNull { it.second.id == product.id }?.let { onProductSelected(SequoProductListing(it.first, product)) } },
    )
    if (selectedShops.isNotEmpty()) Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MarketplaceSectionHeader(
            title = appText(Res.string.home_nearby_shops),
            action = appCatalogText(selectedType.supportLabel),
            onAction = { onDestinationSelected(SequoSection.Markets) },
        )
        selectedShops.take(2).forEach { shop ->
            ShopSummaryRow(shop = shop)
        }
    }
}
