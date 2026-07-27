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
internal fun HomeContent(
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
