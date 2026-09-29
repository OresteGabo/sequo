package dev.orestegabo.sequo.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicTextField
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
internal fun SequoSearchCard(onClose: () -> Unit) {
    var query by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.54f)),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(21.dp),
            )
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isBlank()) {
                            Text(
                                "Search products or shops",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
internal fun ProductPromoCarousel(
    promos: List<SequoPromo>,
    onAddProduct: () -> Unit,
    onSeeAll: () -> Unit,
    onNegotiateClick: (SequoProduct) -> Unit = {},
    onProductSelected: (SequoProductListing) -> Unit = {},
) {
    val campaignPromos = promos.take(5)
    val pagerState = rememberPagerState(pageCount = { campaignPromos.size.coerceAtLeast(1) })

    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val promo = campaignPromos.getOrNull(page) ?: return@HorizontalPager
            ProductPromoCard(
                promo = promo,
                onAddProduct = onAddProduct,
                onSeeAll = onSeeAll,
                onNegotiateClick = { onNegotiateClick(promo.product) },
                onProductClick = { onProductSelected(SequoProductListing(promo.shop, promo.product)) },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            campaignPromos.indices.forEach { index ->
                val selected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (selected) 20.dp else 7.dp, height = 7.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        ),
                )
            }
        }
    }
}

@Composable
private fun ProductPromoCard(
    promo: SequoPromo,
    onAddProduct: () -> Unit,
    onSeeAll: () -> Unit,
    onNegotiateClick: () -> Unit,
    onProductClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(126.dp),
        onClick = onProductClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(productImageResource(promo.product)),
                contentDescription = promo.product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.72f),
                                Color.Black.copy(alpha = 0.38f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            PhotoAuthenticityBadge(
                product = promo.product,
                compact = true,
                onDark = true,
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 210.dp)
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        promo.headline,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.82f),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        promo.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            promo.product.name,
                            modifier = Modifier.weight(1f, fill = false),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.78f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        PromoPrice(
                            product = promo.product,
                            onNegotiateClick = onNegotiateClick,
                        )
                        ProductDiscountBadge(product = promo.product, compact = true, onDark = true)
                    }
                    Text(
                        promo.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.70f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    SequoTinyButton("Add", onAddProduct)
                    Surface(
                        onClick = onSeeAll,
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.18f),
                    ) {
                        Text(
                            "View",
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PromoPrice(
    product: SequoProduct,
    onNegotiateClick: () -> Unit,
) {
    if (product.isNegotiable) {
        Surface(
            onClick = onNegotiateClick,
            shape = RoundedCornerShape(999.dp),
            color = Color.White.copy(alpha = 0.18f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalOffer,
                    contentDescription = "Make an offer",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    formatCompactCfa(product.priceCfa),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    } else {
        Text(
            formatCompactCfa(product.priceCfa),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.78f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun ShopTypeRail(
    types: List<SequoShopType>,
    selectedTypeKey: String,
    onTypeSelected: (String) -> Unit,
) {
    MarketplaceCategorySection(
        types = types,
        selectedTypeKey = selectedTypeKey,
        onTypeSelected = onTypeSelected,
        onSeeAll = {},
        action = null,
    )
}

@Composable
internal fun MarketplaceCategorySection(
    types: List<SequoShopType>,
    selectedTypeKey: String,
    onTypeSelected: (String) -> Unit,
    onSeeAll: () -> Unit = {},
    action: String? = "See all",
    pinnedCategoryKeys: List<String> = emptyList(),
    onTogglePinned: (String) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MarketplaceSectionHeader(
            title = "Categories",
            action = action?.let { if (expanded) "Less" else it },
            onAction = {
                expanded = !expanded
                if (expanded) {
                    onSeeAll()
                }
            },
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            types.forEach { type ->
                MarketplaceCategoryBubble(
                    type = type,
                    selected = type.key == selectedTypeKey,
                    pinned = type.key in pinnedCategoryKeys,
                    onClick = { onTypeSelected(type.key) },
                    onTogglePinned = { onTogglePinned(type.key) },
                )
            }
            Spacer(Modifier.width(2.dp))
        }
        if (expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                types.chunked(3).forEach { rowTypes ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowTypes.forEach { type ->
                            MarketplaceCategoryBubble(
                                type = type,
                                selected = type.key == selectedTypeKey,
                                pinned = type.key in pinnedCategoryKeys,
                                onClick = { onTypeSelected(type.key) },
                                onTogglePinned = { onTogglePinned(type.key) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(3 - rowTypes.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun MarketplaceCategoryBubble(
    type: SequoShopType,
    selected: Boolean,
    pinned: Boolean = false,
    onClick: () -> Unit,
    onTogglePinned: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        type.accent
    } else {
        type.accent.copy(alpha = 0.14f)
    }
    val iconColor = if (selected) Color.White else type.accent
    val hangerColor = if (selected) {
        type.accent.copy(alpha = 0.72f)
    } else {
        type.accent.copy(alpha = 0.56f)
    }
    val nailColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.widthIn(min = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 54.dp, height = 64.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true },
                ),
        ) {
            if (pinned) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val centerX = size.width / 2f
                    drawLine(
                        color = hangerColor,
                        start = Offset(centerX, 9.dp.toPx()),
                        end = Offset(centerX, 18.dp.toPx()),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                    drawCircle(
                        color = nailColor,
                        radius = 3.2.dp.toPx(),
                        center = Offset(centerX, 6.dp.toPx()),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.72f),
                        radius = 1.dp.toPx(),
                        center = Offset(centerX - 1.dp.toPx(), 5.dp.toPx()),
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(54.dp),
                shape = RoundedCornerShape(16.dp),
                color = containerColor,
                border = if (selected) null else BorderStroke(1.dp, type.accent.copy(alpha = 0.34f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = type.icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            if (pinned) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 14.dp)
                        .size(width = 16.dp, height = 4.dp),
                    shape = CircleShape,
                    color = hangerColor.copy(alpha = 0.18f),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 9.dp, height = 1.dp)
                                .clip(CircleShape)
                                .background(hangerColor.copy(alpha = 0.48f)),
                        )
                    }
                }
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(if (pinned) "Unpin category" else "Pin category") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onTogglePinned()
                    },
                )
            }
        }
        Text(
            type.title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal fun personalizedShopTypes(
    types: List<SequoShopType>,
    usage: Map<String, Int>,
    pinnedKeys: List<String>,
): List<SequoShopType> {
    val originalIndex = types.mapIndexed { index, type -> type.key to index }.toMap()
    return types.sortedWith(
        compareByDescending<SequoShopType> { type -> type.key in pinnedKeys }
            .thenBy { type -> pinnedKeys.indexOf(type.key).takeIf { it >= 0 } ?: Int.MAX_VALUE }
            .thenByDescending { type -> usage[type.key] ?: 0 }
            .thenBy { type -> originalIndex[type.key] ?: Int.MAX_VALUE },
    )
}

@Composable
internal fun MarketplaceSectionHeader(
    title: String,
    action: String?,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (action != null) {
            Surface(
                onClick = onAction,
                shape = RoundedCornerShape(999.dp),
                color = Color.Transparent,
            ) {
                Row(
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        action,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SequoInlineSectionLabel(
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
