package dev.orestegabo.sequo.ui.basket

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
internal fun CartItemsPanel(entries: List<BasketEntry>, extraBasketItems: Int) {
    SequoCard(shape = RoundedCornerShape(22.dp)) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Your items",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Ready for checkout",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    "${entries.size + extraBasketItems} items",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            entries.forEachIndexed { index, entry ->
                BasketLine(entry, entries)
                if (index != entries.lastIndex || extraBasketItems > 0) {
                    Spacer(Modifier.height(2.dp))
                }
            }
            if (extraBasketItems > 0) {
                BasketAddedLine(extraBasketItems)
            }
        }
    }
}

@Composable
internal fun BasketLine(entry: BasketEntry, entries: List<BasketEntry>) {
    val shop = entry.shop
    val product = entry.product
    val packageLabel = packageLabelForArea(shop.area, entries)
    CartItemTile(
        image = {
            ProductImage(
                product = product,
                modifier = Modifier.fillMaxSize(),
            )
        },
        content = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    product.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                PackageBadge(packageLabel)
            }
            Text(
                shop.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    formatCfa(product.priceCfa * entry.quantity),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    "x${entry.quantity}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        trailing = { QuantityStepper(entry.quantity) },
    )
}

private fun packageLabelForArea(area: String, entries: List<BasketEntry>): String {
    val areas = entries.map { it.shop.area }.distinct()
    val index = areas.indexOf(area).coerceAtLeast(0)
    return ('A'.code + index).toChar().toString()
}

@Composable
private fun PackageBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f),
    ) {
        Text(
            "Pkg $label",
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun CartItemTile(
    image: @Composable BoxScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    trailing: @Composable BoxScope.() -> Unit,
) {
    val tileHeight = 92.dp

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(tileHeight),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.36f)),
        ) {}
        Box(
            modifier = Modifier
                .height(tileHeight)
                .width(150.dp)
                .clip(RoundedCornerShape(18.dp)),
            content = image,
        )
        Box(
            modifier = Modifier
                .height(tileHeight)
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        0.0f to Color.Transparent,
                        0.22f to MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.10f),
                        0.42f to MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.86f),
                        0.60f to MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.98f),
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 96.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                content = content,
            )
            Box(content = trailing)
        }
    }
}

@Composable
internal fun BasketAddedLine(count: Int) {
    CartItemTile(
        image = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.ShoppingBasket,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        content = {
            Text("Added while browsing", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Temporary basket item", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatCfa(count * 3500), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
        },
        trailing = { QuantityStepper(count) },
    )
}

@Composable
internal fun DeliveryAddressCard(packageCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(78.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "Tokoin Gbadago, near Pharmacie des Etoiles",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "Delivery in 35-45 min  |  $packageCount packages from shopping locations",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
internal fun CartDeliveryPackagesCard(entries: List<BasketEntry>) {
    var expanded by remember { mutableStateOf(false) }
    val packages = entries.groupBy { it.shop.area }.values.toList()
    val totalDelivery = cartPackageDeliveryFees(entries).sum()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Delivery & packaging", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "${packages.size} packages with item and delivery details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Delivery", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatCfa(totalDelivery), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse delivery fees" else "Show delivery fees",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
            if (expanded) packages.forEachIndexed { index, entries ->
                val shop = entries.first().shop
                val itemCount = entries.sumOf { it.quantity }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f))
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(30.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                ('A'.code + index).toChar().toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Package ${('A'.code + index).toChar()}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "${shop.area}  |  $itemCount item${if (itemCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        formatCfa(baseDelivery(shop.distanceKm)),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(
                    modifier = Modifier.padding(start = 50.dp, top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    entries.forEach { entry ->
                        Text(
                            "${entry.quantity} x ${entry.product.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    val foodEntries = entries.filter { isFoodBasketEntry(it) }
                    if (foodEntries.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f))
                        Text(
                            "Packaging: sealed thermal bag",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        foodEntries.forEach { entry ->
                            Text(
                                "Preparation: ${entry.product.optionHint}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isFoodBasketEntry(entry: BasketEntry): Boolean =
    entry.shop.kind.contains("food", ignoreCase = true) ||
        entry.product.label.contains("food", ignoreCase = true) ||
        entry.product.label.contains("hot", ignoreCase = true)

private fun cartPackageDeliveryFees(entries: List<BasketEntry>): List<Int> =
    entries
        .groupBy { it.shop.area }
        .values
        .map { entries -> baseDelivery(entries.first().shop.distanceKm) }

@Composable
internal fun QuantityStepper(quantity: Int) {
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.52f), RoundedCornerShape(999.dp))
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(Icons.Filled.Remove)
        Text(
            quantity.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.widthIn(min = 14.dp),
            textAlign = TextAlign.Center,
        )
        StepperButton(Icons.Filled.Add)
    }
}

@Composable
internal fun StepperButton(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(22.dp),
        shape = CircleShape,
        color = Color.Transparent,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
internal fun SequoCheckoutCard(
    entries: List<BasketEntry>,
    extraBasketItems: Int,
    selectedPayment: String,
    onPaymentSelected: (String) -> Unit,
) {
    val subtotal = entries.sumOf { it.product.priceCfa * it.quantity } + (extraBasketItems * 3500)
    val packageDelivery = cartPackageDeliveryFees(entries).sum()
    val subscriptionDiscount = (packageDelivery * 15) / 100
    val deliveryAfterSubscription = packageDelivery - subscriptionDiscount
    val referralCredit = 500.coerceAtMost(deliveryAfterSubscription)
    val finalDelivery = deliveryAfterSubscription - referralCredit
    val total = subtotal + finalDelivery

    SequoSectionCard(title = "Pay securely", action = selectedPayment) {
        ValueRow("Items", formatCfa(subtotal))
        ValueRow("Delivery", formatCfa(packageDelivery))
        ValueRow("Subscriber", "-${formatCfa(subscriptionDiscount)}")
        ValueRow("Parrainage", "-${formatCfa(referralCredit)}")
        ValueRow("Total", formatCfa(total), strong = true)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PaymentChoice("Yas Togo", selectedPayment, onPaymentSelected, Modifier.weight(1f))
            PaymentChoice("Moov Africa", selectedPayment, onPaymentSelected, Modifier.weight(1f))
        }
        SequoPrimaryButton("Pay ${formatCfa(total)}", {}, Modifier.fillMaxWidth())
    }
}

@Composable
internal fun PaymentChoice(
    label: String,
    selectedPayment: String,
    onPaymentSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = label == selectedPayment
    val borderColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.48f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)

    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onPaymentSelected(label) }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
            PaymentLogo(label, Modifier.size(width = 46.dp, height = 34.dp))
            Text(
                label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
internal fun SupportedPaymentRow(label: String, detail: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PaymentLogo(label, Modifier.size(width = 74.dp, height = 44.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
            }
            MetaPill("Enabled", SequoPrimary)
        }
    }
}

@Composable
internal fun PaymentLogo(label: String, modifier: Modifier = Modifier) {
    val logo = if (label.contains("Yas")) {
        Res.drawable.yas_togo_logo
    } else {
        Res.drawable.moov_africa_logo
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(11.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Image(
            painter = painterResource(logo),
            contentDescription = label,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().padding(4.dp),
        )
    }
}
