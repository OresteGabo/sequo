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
import androidx.compose.ui.Modifier
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
internal fun LomeRouteCard() {
    SequoCard(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(appText(Res.string.home_today_route), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(appText(Res.string.home_fees_visible), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                RouteStop("Tokoin", true, Modifier.weight(1f))
                RouteStop("Assigame", true, Modifier.weight(1f))
                RouteStop("Be", false, Modifier.weight(1f))
                RouteStop("Akodessewa", false, Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun RouteStop(label: String, active: Boolean, modifier: Modifier = Modifier) {
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
internal fun SequoHeroCard(
    eyebrow: String,
    title: String,
    body: String,
    primaryLabel: String,
    secondaryLabel: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
) {
    SequoCard(shape = RoundedCornerShape(32.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            SequoSecondary.copy(alpha = 0.20f),
                            SequoAccent.copy(alpha = 0.16f),
                            SequoPrimary.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(SequoSecondary.copy(alpha = 0.12f), radius = size.minDimension * 0.42f, center = Offset(size.width * 0.92f, size.height * 0.06f))
                drawCircle(SequoPrimary.copy(alpha = 0.10f), radius = size.minDimension * 0.30f, center = Offset(size.width * 0.10f, size.height * 0.84f))
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
                        border = BorderStroke(1.dp, SequoSecondary.copy(alpha = 0.22f)),
                    ) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                            SequoMonogram("SQ", SequoSecondary, Modifier.size(42.dp))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaPill(appText(Res.string.home_nearby_fee), SequoPrimary)
                    MetaPill(appText(Res.string.home_product_view), SequoAccent)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SequoPrimaryButton(primaryLabel, onPrimary, Modifier.weight(1f))
                    SequoSecondaryButton(secondaryLabel, onSecondary, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun SequoMetricRow(
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
internal fun MetricCard(value: String, label: String, modifier: Modifier = Modifier) {
    SequoCard(modifier = modifier, shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f))
        }
    }
}

@Composable
internal fun HomeSignalRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HomeSignalPill(Icons.Filled.PhotoCamera, appText(Res.string.home_product_view), SequoPrimary, Modifier.weight(1f))
        HomeSignalPill(Icons.Filled.Payments, "Yas/Moov", SequoSecondary, Modifier.weight(1f))
        HomeSignalPill(Icons.Filled.CheckCircle, appText(Res.string.home_relay_72h), SequoAccent, Modifier.weight(1f))
    }
}

@Composable
internal fun HomeSignalPill(icon: ImageVector, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun FlashSaleSection(
    title: String,
    badge: String?,
    products: List<Pair<SequoShop, SequoProduct>>,
    onAddProduct: () -> Unit,
    onSeeAll: () -> Unit,
    onNegotiateClick: (SequoProduct) -> Unit = {},
    onProductSelected: (SequoShop, SequoProduct) -> Unit = { _, _ -> },
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f) // Prevents title text overflow bugs
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (badge != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Surface(
                onClick = onSeeAll,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = appText(Res.string.home_see_all),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(21.dp),
                    )
                }
            }
        }

        // Product Grid Rows
        products.chunked(2).forEach { rowProducts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                rowProducts.forEach { (shop, product) ->
                    FlashProductCard(
                        shop = shop,
                        product = product,
                        onAddProduct = onAddProduct,
                        onNegotiateClick = { onNegotiateClick(product) },
                        onProductClick = { onProductSelected(shop, product) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowProducts.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun FlashProductCard(
    shop: SequoShop,
    product: SequoProduct,
    onAddProduct: () -> Unit,
    onNegotiateClick: () -> Unit = {},
    onProductClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                onClick = onProductClick,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
            ) {
                Box(Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(productImageResource(product)),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.32f)))))
                    PhotoAuthenticityBadge(
                        product = product,
                        compact = true,
                        onDark = true,
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                    )

                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(34.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f)),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp),
                            )
                        }
                    }

                    if (product.isNegotiable || product.hasDiscount) {
                        Row(
                            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (product.isNegotiable) {
                                Surface(
                                    onClick = onNegotiateClick,
                                    modifier = Modifier.size(30.dp),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.90f),
                                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f)),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalOffer,
                                            contentDescription = appText(Res.string.home_make_offer),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                            ProductDiscountBadge(product = product, compact = false, onDark = true)
                        }
                    }
                }
            }

            Text(
                product.name,
                modifier = Modifier.clickable(onClick = onProductClick),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    shop.area,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    NegotiablePrice(
                        product = product,
                        onNegotiateClick = onNegotiateClick,
                        compact = true,
                    )
                    ProductDiscountBadge(product = product, compact = true)
                }
                SequoTinyButton(appText(Res.string.home_add), onAddProduct)
            }
        }
    }
}

internal fun productVisualIcon(product: SequoProduct): ImageVector =
    when {
        product.label.contains("Food", ignoreCase = true) || product.label.contains("Hot", ignoreCase = true) -> Icons.Filled.LocalDining
        product.label.contains("Fresh", ignoreCase = true) -> Icons.Filled.Storefront
        product.label.contains("Auto", ignoreCase = true) -> Icons.Filled.Build
        product.label.contains("Care", ignoreCase = true) -> Icons.Filled.MedicalServices
        product.label.contains("Bargain", ignoreCase = true) -> Icons.Filled.LocalOffer
        product.name.contains("charger", ignoreCase = true) || product.name.contains("case", ignoreCase = true) -> Icons.Filled.PhoneAndroid
        else -> Icons.Filled.ShoppingBasket
    }

@Composable
internal fun SequoIntroCard(eyebrow: String, title: String, subtitle: String) {
    SequoCard(shape = RoundedCornerShape(24.dp)) {
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
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f))
                }
            }
        }
    }
}
