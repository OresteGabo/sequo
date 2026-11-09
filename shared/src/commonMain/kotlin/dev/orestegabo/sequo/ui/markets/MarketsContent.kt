package dev.orestegabo.sequo.ui.markets

import dev.orestegabo.sequo.ui.components.SequoEmptyState
import dev.orestegabo.sequo.ui.components.EmptyStateVisual
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
import dev.orestegabo.sequo.feature.settings.appText
import dev.orestegabo.sequo.feature.settings.appCatalogText
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.*

@Composable
internal fun MarketsContent(
    catalog: CatalogSnapshot,
    selectedTypeKey: String,
    browseMode: CatalogBrowseMode,
    onCategorySelected: (String) -> Unit,
    onResetFilters: () -> Unit,
    onRefreshCatalog: () -> Unit,
    onAddProduct: (String) -> Unit,
    onProductSelected: (SequoProductListing) -> Unit,
) {
    if (catalog.products.isEmpty()) {
        SequoEmptyState(
            visual = EmptyStateVisual.Catalog,
            title = appText(Res.string.empty_catalog_title), detail = appText(Res.string.empty_catalog_detail),
            actionLabel = appText(Res.string.cart_refresh), onAction = onRefreshCatalog,
        )
        return
    }
    var selectedArea by remember { mutableStateOf("All Lome") }
    var selectedSubcategory by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("Nearby") }
    var areaFiltersExpanded by remember { mutableStateOf(false) }
    var subcategoryFiltersExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTypeKey, browseMode) {
        selectedSubcategory = "All"
        selectedArea = "All Lome"
        selectedSort = "Nearby"
        areaFiltersExpanded = false
        subcategoryFiltersExpanded = false
    }

    val selectedType = catalog.categoryFor(selectedTypeKey)
    val shopsByType = catalog.browseShops(selectedTypeKey, browseMode)
    val subcategories = listOf("All") + productSubcategoriesFor(shopsByType)
    val areaShops = if (selectedArea == "All Lome") {
        shopsByType
    } else {
        shopsByType.filter { shop ->
            shop.area?.contains(selectedArea) == true || shop.name.contains(selectedArea)
        }
    }
    val filteredShops = areaShops.filter { shop ->
        selectedSubcategory == "All" || shop.products.any { product ->
            productSubcategory(product) == selectedSubcategory
        }
    }
    val visibleShops = when (selectedSort) {
        "Fastest" -> filteredShops.sortedBy { it.etaSortRank() }
        "Rating" -> filteredShops.sortedByDescending { it.rating?.toDoubleOrNull() ?: Double.NEGATIVE_INFINITY }
        "Delivery" -> filteredShops.sortedBy { it.distanceKm?.let(::baseDelivery) ?: Int.MAX_VALUE }
        else -> filteredShops.sortedBy { it.distanceKm ?: Double.POSITIVE_INFINITY }
    }

    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selectedTypeKey.isBlank(), onClick = { onCategorySelected("") }, label = { Text(appText(Res.string.browse_all_categories)) })
        catalog.categories.forEach { category ->
            FilterChip(selected = selectedTypeKey == category.key, onClick = { onCategorySelected(category.key) },
                label = { Text(appCatalogText(category.title)) })
        }
    }
    MarketSortBar(
        selectedType = selectedType,
        categoryTitle = if (selectedTypeKey.isBlank()) appText(Res.string.browse_all_categories) else appCatalogText(selectedType.title),
        visibleCount = visibleShops.size,
        selectedSort = selectedSort,
        onSortSelected = { selectedSort = it },
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CatalogFilterMenu(
            title = appText(Res.string.browse_product_type),
            categories = subcategories,
            selectedCategory = selectedSubcategory,
            onCategorySelected = { selectedSubcategory = it },
            expanded = subcategoryFiltersExpanded,
            onExpandedChange = { subcategoryFiltersExpanded = it; areaFiltersExpanded = false },
            modifier = Modifier.weight(1f),
        )
        CatalogFilterMenu(
            title = appText(Res.string.browse_shop_area),
            categories = listOf(
                "All Lome", "Tokoin", "Assigame", "Hedzranawoe", "Akodessewa",
                "Agbalepedo", "Be-Kpota", "Baguida", "Adidogome", "Nyekonakpoe",
                "Agoe", "Ablogame", "Kodjoviakope", "Be",
            ),
            selectedCategory = selectedArea,
            onCategorySelected = { selectedArea = it },
            expanded = areaFiltersExpanded,
            onExpandedChange = { areaFiltersExpanded = it; subcategoryFiltersExpanded = false },
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.LocationOn,
        )
    }
    if (visibleShops.isEmpty()) {
        val title = when (browseMode) {
            CatalogBrowseMode.Offers -> Res.string.empty_offers_title
            CatalogBrowseMode.Negotiable -> Res.string.empty_negotiable_title
            CatalogBrowseMode.Nearby -> Res.string.empty_nearby_title
            CatalogBrowseMode.All -> Res.string.empty_filter_title
        }
        val detail = when (browseMode) {
            CatalogBrowseMode.Offers -> Res.string.empty_offers_detail
            CatalogBrowseMode.Negotiable -> Res.string.empty_negotiable_detail
            CatalogBrowseMode.Nearby -> Res.string.empty_nearby_detail
            CatalogBrowseMode.All -> Res.string.empty_filter_detail
        }
        SequoEmptyState(
            visual = EmptyStateVisual.Search,
            compact = true,
            title = appText(title), detail = appText(detail),
            actionLabel = appText(Res.string.empty_browse_all),
            onAction = {
                selectedArea = "All Lome"
                selectedSubcategory = "All"
                selectedSort = "Nearby"
                onResetFilters()
            },
        )
    }
    visibleShops.forEach { shop ->
        SequoShopCard(
            shop = shop,
            selectedSubcategory = selectedSubcategory.takeUnless { it == "All" },
            onAddProduct = onAddProduct,
            onProductSelected = { product -> onProductSelected(SequoProductListing(shop, product)) },
            onNegotiateClick = { product -> onProductSelected(SequoProductListing(shop, product)) },
        )
    }
}

@Composable
private fun MarketSortBar(
    selectedType: SequoShopType,
    categoryTitle: String,
    visibleCount: Int,
    selectedSort: String,
    onSortSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(categoryTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$visibleCount ${appText(if (visibleCount == 1) Res.string.browse_shop_one else Res.string.browse_shops_count)}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box {
            TextButton(onClick = { expanded = true }) {
                Icon(Icons.AutoMirrored.Filled.Sort, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(appCatalogText(selectedSort))
                Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(18.dp))
            }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                listOf("Nearby", "Fastest", "Rating", "Delivery").forEach { sort ->
                    DropdownMenuItem(text = { Text(appCatalogText(sort)) },
                        trailingIcon = { if (sort == selectedSort) Icon(Icons.Filled.Check, null) },
                        onClick = { onSortSelected(sort); expanded = false })
                }
            }
        }
    }
}

private fun SequoShop.etaSortRank(): Int =
    when {
        eta == null -> Int.MAX_VALUE
        eta.contains("min", ignoreCase = true) -> eta.filter { it.isDigit() }.toIntOrNull() ?: 999
        eta.contains("today", ignoreCase = true) -> 240
        eta.contains("tomorrow", ignoreCase = true) -> 1440
        else -> 999
    }
