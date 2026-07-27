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
internal fun MarketsContent(onAddProduct: () -> Unit) {
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
