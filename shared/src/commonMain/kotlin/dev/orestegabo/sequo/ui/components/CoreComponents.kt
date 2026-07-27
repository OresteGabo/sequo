package dev.orestegabo.sequo.ui.components

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
internal fun ReturnHubCard() {
    SequoSectionCard(title = "Return hub", action = "72 hours") {
        RuleRow("Point de Relai Tokoin", "Open 08:00-19:00, accepts sealed general goods returns.")
        RuleRow("Inspection status", "Refund triggers automatically when Sequo accepts the returned product.")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SequoSecondaryButton("Start return", {}, Modifier.weight(1f), emphasized = true)
            SequoSecondaryButton("Find point", {}, Modifier.weight(1f))
        }
    }
}

@Composable
internal fun AccountAddressRow(label: String, address: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        SequoMiniMark(label.take(1), SequoSecondary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun SequoPassCard() {
    SequoCard(shape = RoundedCornerShape(34.dp), color = Color.Transparent, border = null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF111419), Color(0xFF18242B), Color(0xFF24404A))))
                .border(1.dp, SequoAccent.copy(alpha = 0.28f), RoundedCornerShape(34.dp))
                .padding(20.dp),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(SequoAccent.copy(alpha = 0.18f), radius = size.width * 0.26f, center = Offset(size.width * 0.92f, size.height * 0.16f))
                drawRoundRect(Color(0x14FFF9F0), Offset(-34.dp.toPx(), size.height - 74.dp.toPx()), Size(180.dp.toPx(), 82.dp.toPx()), CornerRadius(44.dp.toPx(), 24.dp.toPx()))
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("SEQUO ACCESS", style = MaterialTheme.typography.labelSmall, color = Color(0xCCFFF8EE))
                Text("Afi K.", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFFFFBF5), fontWeight = FontWeight.SemiBold)
                Text("Lome subscriber / 15% delivery discount", style = MaterialTheme.typography.bodyMedium, color = Color(0xCCFFF8EE))
                Box(Modifier.height(44.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaPill("500 CFA credit", SequoAccent, inverse = true)
                    MetaPill("3 Lome addresses", SequoPrimary, inverse = true)
                }
            }
        }
    }
}

@Composable
internal fun SequoCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(26.dp),
    color: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = color,
        border = border,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        content = content,
    )
}

@Composable
internal fun SequoPrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.92f))))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun SequoSecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    val containerColor = if (emphasized) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f)
    val borderColor = if (emphasized) MaterialTheme.colorScheme.primary.copy(alpha = 0.48f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
    val textColor = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f)
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .border(1.4.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun SequoTinyButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun MetaPill(label: String, accentColor: Color, inverse: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (inverse) Color(0x2EFFF8EE) else accentColor.copy(alpha = 0.14f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (inverse) Color(0xFFFFFBF5) else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun RuleRow(title: String, detail: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
        }
    }
}

@Composable
internal fun ShopMark(text: String) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(SequoPrimary.copy(alpha = 0.88f), SequoSecondary.copy(alpha = 0.72f)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(text.take(2).uppercase(), style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun RatingMark(rating: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.76f),
    ) {
        Text(
            rating,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun SequoMiniMark(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun SequoMonogram(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.30f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun StepRow(text: String, active: Boolean, detail: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)))
        Column(Modifier.weight(1f)) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (active) 0.96f else 0.68f), fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
            Text(detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f))
        }
    }
}
