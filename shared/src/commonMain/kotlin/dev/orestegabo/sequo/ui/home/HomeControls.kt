package dev.orestegabo.sequo.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
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
internal fun SequoSearchCard(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    val placeholder = appText(Res.string.home_search_placeholder)
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
                onValueChange = { onQueryChange(it.take(160)) },
                modifier = Modifier.weight(1f).semantics { contentDescription = placeholder },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isBlank()) {
                            Text(
                                appText(Res.string.home_search_placeholder),
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
                    contentDescription = appText(Res.string.home_close_search),
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
    onAddProduct: (String) -> Unit,
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
    onAddProduct: (String) -> Unit,
    onSeeAll: () -> Unit,
    onNegotiateClick: () -> Unit,
    onProductClick: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxWidth().height(184.dp), onClick = onProductClick,
        shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Box {
            CatalogProductImage(promo.product, Modifier.fillMaxSize(), ContentScale.Crop)
            Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(
                Color.Black.copy(alpha = .76f), Color.Black.copy(alpha = .48f), Color.Black.copy(alpha = .12f)))))
            Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.widthIn(max = 260.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(appCatalogText(promo.headline), style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = .8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(promo.title, style = MaterialTheme.typography.titleLarge, color = Color.White,
                        fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    PromoPrice(promo.product, onNegotiateClick)
                    ProductDiscountBadge(promo.product, compact = true, onDark = true)
                    Spacer(Modifier.weight(1f))
                    SequoTinyButton(appText(Res.string.home_add),
                        if (promo.product.customizations.isEmpty()) ({ onAddProduct(promo.product.id) }) else onProductClick)
                }
            }
            PhotoAuthenticityBadge(promo.product, compact = true, onDark = true,
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
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
                    contentDescription = appText(Res.string.home_make_offer),
                    tint = Color.White,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    formatProductPrice(product, compact = true),
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
            formatProductPrice(product, compact = true),
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
    )
}

@Composable
internal fun MarketplaceCategorySection(
    types: List<SequoShopType>,
    selectedTypeKey: String,
    onTypeSelected: (String) -> Unit,
    pinnedCategoryKeys: List<String> = emptyList(),
    onTogglePinned: (String) -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MarketplaceSectionHeader(title = appText(Res.string.home_categories), action = null, onAction = {})
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            types.forEach { type ->
                key(type.key) {
                    val pinned = type.key in pinnedCategoryKeys
                    MarketplaceCategoryBubble(
                        type = type,
                        selected = type.key == selectedTypeKey,
                        pinned = pinned,
                        canTogglePinned = pinned || pinnedCategoryKeys.size < 3,
                        onClick = { onTypeSelected(type.key) },
                        onTogglePinned = { onTogglePinned(type.key) },
                    )
                }
            }
            Spacer(Modifier.width(2.dp))
        }

    }
}

@Composable
internal fun MarketplaceCategoryBubble(
    type: SequoShopType,
    selected: Boolean,
    pinned: Boolean = false,
    canTogglePinned: Boolean = true,
    onClick: () -> Unit,
    onTogglePinned: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember(type.key) { mutableStateOf(false) }
    Column(modifier.width(76.dp)
        .semantics(mergeDescendants = true) { this.selected = selected; role = Role.Tab }
        .combinedClickable(onClick = onClick, onLongClick = { menuExpanded = true }),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box {
            Surface(modifier = Modifier.size(56.dp), shape = RoundedCornerShape(18.dp),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(type.icon, null, Modifier.size(24.dp),
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (pinned) Icon(Icons.Filled.PushPin, null,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(12.dp),
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(text = { Text(appText(when {
                    pinned -> Res.string.home_unpin_category
                    canTogglePinned -> Res.string.home_pin_category
                    else -> Res.string.home_pinned_max
                })) }, leadingIcon = { PushPinActionIcon(showAdd = !pinned, enabled = canTogglePinned) },
                    enabled = canTogglePinned, onClick = { menuExpanded = false; onTogglePinned() })
            }
        }
        Text(appCatalogText(type.title), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun PushPinActionIcon(showAdd: Boolean, enabled: Boolean) {
    val contentColor = if (enabled) {
        LocalContentColor.current
    } else {
        LocalContentColor.current.copy(alpha = 0.38f)
    }
    Box(modifier = Modifier.size(24.dp)) {
        Icon(
            imageVector = Icons.Outlined.PushPin,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier
                .align(Alignment.Center)
                .size(21.dp),
        )
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(12.dp),
            shape = CircleShape,
            color = if (showAdd) {
                MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.38f)
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
        ) {
            Icon(
                imageVector = if (showAdd) Icons.Outlined.Add else Icons.Outlined.Remove,
                contentDescription = null,
                tint = if (showAdd) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                },
                modifier = Modifier.padding(1.5.dp),
            )
        }
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
            modifier = Modifier.weight(1f),
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
