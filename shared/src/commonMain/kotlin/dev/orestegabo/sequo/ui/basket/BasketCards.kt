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
internal fun BasketLine(entry: BasketEntry) {
    val shop = entry.shop
    val product = entry.product
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        ShopMark(product.name)
        Column(Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${shop.name} / Qty ${entry.quantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(formatCfa(product.priceCfa * entry.quantity), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun BasketAddedLine(count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        ShopMark("Added")
        Column(Modifier.weight(1f)) {
            Text("Added while browsing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Temporary basket items / Qty $count", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
        }
        Text(formatCfa(count * 3500), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun DeliveryAddressCard() {
    SequoCard(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Delivery address", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("edit", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            RuleRow("Tokoin Gbadago", "Near Pharmacie des Etoiles, call when outside.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill("5.6 km route", SequoPrimary)
                MetaPill("Subscriber -15%", SequoSecondary)
            }
        }
    }
}

@Composable
internal fun SequoCheckoutCard(
    extraBasketItems: Int,
    selectedPayment: String,
    onPaymentSelected: (String) -> Unit,
) {
    val subtotal = sequoBasket.sumOf { it.product.priceCfa * it.quantity } + (extraBasketItems * 3500)
    val delivery = calculateDeliveryPricing(
        DeliveryPricingInput(distanceKm = 5.6, subscriptionDiscountPercent = 15, referralCreditCfa = 500),
    )
    val total = subtotal + delivery.finalDeliveryFeeCfa

    SequoSectionCard(title = "Pay securely", action = selectedPayment) {
        ValueRow("Items", formatCfa(subtotal))
        ValueRow("Delivery", formatCfa(delivery.baseFeeCfa))
        ValueRow("Subscriber", "-${formatCfa(delivery.subscriptionDiscountCfa)}")
        ValueRow("Parrainage", "-${formatCfa(delivery.referralCreditAppliedCfa)}")
        ValueRow("Total", formatCfa(total), strong = true)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PaymentChoice("Yas Togo", selectedPayment, onPaymentSelected, Modifier.weight(1f))
            PaymentChoice("Moov Africa", selectedPayment, onPaymentSelected, Modifier.weight(1f))
        }
        SequoPrimaryButton("Pay ${formatCfa(total)}", {}, Modifier.fillMaxWidth())
        Text(
            "Payment must be confirmed before the seller receives completion status.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
        )
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
