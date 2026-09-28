package dev.orestegabo.sequo.ui.markets

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
internal fun MarketsContent(
    selectedTypeKey: String,
    onAddProduct: () -> Unit,
) {
    var selectedArea by remember { mutableStateOf("All Lome") }
    var selectedSubcategory by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("Nearby") }
    var areaFiltersExpanded by remember { mutableStateOf(false) }
    var subcategoryFiltersExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTypeKey) {
        selectedSubcategory = "All"
        selectedArea = "All Lome"
        selectedSort = "Nearby"
        areaFiltersExpanded = false
        subcategoryFiltersExpanded = false
    }

    val selectedType = shopTypeFor(selectedTypeKey)
    val shopsByType = shopsForType(selectedTypeKey)
    val subcategories = listOf("All") + productSubcategoriesFor(shopsByType)
    val areaShops = if (selectedArea == "All Lome") {
        shopsByType
    } else {
        shopsByType.filter { shop ->
            shop.area.contains(selectedArea) || shop.name.contains(selectedArea)
        }
    }
    val filteredShops = areaShops.filter { shop ->
        selectedSubcategory == "All" || shop.products.any { product ->
            productSubcategory(product) == selectedSubcategory
        }
    }
    val visibleShops = when (selectedSort) {
        "Fastest" -> filteredShops.sortedBy { it.etaSortRank() }
        "Rating" -> filteredShops.sortedByDescending { it.rating.toDoubleOrNull() ?: 0.0 }
        "Delivery" -> filteredShops.sortedBy { baseDelivery(it.distanceKm) }
        else -> filteredShops.sortedBy { it.distanceKm }
    }

    MarketSortBar(
        selectedType = selectedType,
        visibleCount = visibleShops.size,
        selectedSort = selectedSort,
        onSortSelected = { selectedSort = it },
    )
    CategoryRail(
        categories = subcategories,
        selectedCategory = selectedSubcategory,
        onCategorySelected = { selectedSubcategory = it },
        expanded = subcategoryFiltersExpanded,
        onExpandedChange = { subcategoryFiltersExpanded = it },
    )
    CategoryRail(
        categories = listOf(
            "All Lome",
            "Tokoin",
            "Assigame",
            "Hedzranawoe",
            "Akodessewa",
            "Agbalepedo",
            "Be-Kpota",
            "Baguida",
            "Adidogome",
            "Nyekonakpoe",
            "Agoe",
            "Ablogame",
            "Kodjoviakope",
            "Be",
        ),
        selectedCategory = selectedArea,
        onCategorySelected = { selectedArea = it },
        expanded = areaFiltersExpanded,
        onExpandedChange = { areaFiltersExpanded = it },
    )
    LomeRouteCard()
    visibleShops.forEach { shop ->
        SequoShopCard(
            shop = shop,
            selectedSubcategory = selectedSubcategory.takeUnless { it == "All" },
            onAddProduct = onAddProduct,
        )
    }
}

@Composable
private fun MarketSortBar(
    selectedType: SequoShopType,
    visibleCount: Int,
    selectedSort: String,
    onSortSelected: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = selectedType.accent.copy(alpha = 0.11f),
        border = BorderStroke(1.dp, selectedType.accent.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = null,
                    tint = selectedType.accent,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    "${selectedType.title} / $visibleCount shops",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Nearby", "Fastest", "Rating", "Delivery").forEach { sort ->
                    SequoFilterChip(
                        label = sort,
                        selected = sort == selectedSort,
                        onClick = { onSortSelected(sort) },
                        modifier = Modifier.widthIn(min = 88.dp),
                    )
                }
            }
        }
    }
}

private fun SequoShop.etaSortRank(): Int =
    when {
        eta.contains("min", ignoreCase = true) -> eta.filter { it.isDigit() }.toIntOrNull() ?: 999
        eta.contains("today", ignoreCase = true) -> 240
        eta.contains("tomorrow", ignoreCase = true) -> 1440
        else -> 999
    }
