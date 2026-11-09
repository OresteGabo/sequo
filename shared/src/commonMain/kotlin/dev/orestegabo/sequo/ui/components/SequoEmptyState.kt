package dev.orestegabo.sequo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

internal enum class EmptyStateVisual { Cart, Orders, Search, Notifications, Archived, Addresses, Catalog }

/** A shared visual language; the owning screen supplies truthful copy and real actions. */
@Composable
internal fun SequoEmptyState(
    visual: EmptyStateVisual,
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    compact: Boolean = false,
    signInRequired: Boolean = false,
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = if (compact) 4.dp else 22.dp, vertical = if (compact) 8.dp else 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            EmptyStateArtwork(visual, compact, signInRequired)
            Text(title, style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 320.dp))
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 320.dp))
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(2.dp))
                Button(onClick = onAction, modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth().heightIn(min = 50.dp),
                    shape = RoundedCornerShape(17.dp), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp)) {
                    Text(actionLabel, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                }
            }
            if (secondaryLabel != null && onSecondaryAction != null) {
                TextButton(onClick = onSecondaryAction, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(secondaryLabel, textAlign = TextAlign.Center)
                }
            }
        }
    }
    if (compact) Box(modifier.fillMaxWidth()) { content() }
    else Surface(
        modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .48f)),
    ) { content() }
}

@Composable
private fun EmptyStateArtwork(visual: EmptyStateVisual, compact: Boolean, signInRequired: Boolean) {
    val colors = MaterialTheme.colorScheme
    val ink = colors.primary
    val paper = colors.surfaceContainerLowest
    val tint = colors.primaryContainer
    Box(
        modifier = Modifier.size(if (compact) 180.dp else 240.dp, if (compact) 132.dp else 176.dp).clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val artworkScale = size.width / 240f
            withTransform({ scale(artworkScale, artworkScale, pivot = Offset.Zero) }) {
                drawOval(ink.copy(alpha = .07f), Offset(24f, 28f), Size(194f, 135f))
                drawCircle(colors.tertiaryContainer.copy(alpha = .55f), 26f, Offset(190f, 42f))
                drawCircle(tint.copy(alpha = .72f), 14f, Offset(39f, 112f))
                drawCircle(ink.copy(alpha = .25f), 3f, Offset(202f, 106f))
                drawCircle(ink.copy(alpha = .24f), 2.5f, Offset(58f, 33f))
                drawLine(ink.copy(alpha = .19f), Offset(47f, 159f), Offset(193f, 159f), 2f, StrokeCap.Round)
                drawRoundRect(ink.copy(alpha = .06f), Offset(65f, 41f), Size(119f, 115f), CornerRadius(25f))
                drawRoundRect(paper, Offset(59f, 32f), Size(122f, 116f), CornerRadius(25f))
                drawRoundRect(ink.copy(alpha = .13f), Offset(59f, 32f), Size(122f, 116f), CornerRadius(25f), style = Stroke(1.5f))
                val stroke = Stroke(3.2f, cap = StrokeCap.Round)
                when (visual) {
                    EmptyStateVisual.Cart -> {
                        drawRoundRect(tint, Offset(91f, 64f), Size(21f, 32f), CornerRadius(5f))
                        drawCircle(colors.tertiaryContainer, 13f, Offset(138f, 83f))
                        val basket = Path().apply { moveTo(80f, 91f); lineTo(88f, 127f); quadraticTo(120f, 134f, 152f, 127f); lineTo(160f, 91f); close() }
                        drawPath(basket, paper); drawPath(basket, ink, style = stroke)
                        drawLine(ink, Offset(80f, 91f), Offset(160f, 91f), 3.2f, StrokeCap.Round)
                        drawLine(ink, Offset(99f, 91f), Offset(111f, 67f), 3.2f, StrokeCap.Round)
                        drawLine(ink, Offset(141f, 91f), Offset(129f, 67f), 3.2f, StrokeCap.Round)
                        listOf(102f, 120f, 138f).forEach { x -> drawLine(ink.copy(alpha = .55f), Offset(x, 103f), Offset(x, 118f), 2.5f, StrokeCap.Round) }
                    }
                    EmptyStateVisual.Orders -> {
                        val receipt = Path().apply { moveTo(91f, 53f); lineTo(149f, 53f); lineTo(149f, 133f); lineTo(139f, 127f); lineTo(129f, 133f); lineTo(119f, 127f); lineTo(109f, 133f); lineTo(99f, 127f); lineTo(91f, 133f); close() }
                        drawPath(receipt, tint.copy(alpha = .5f)); drawPath(receipt, ink, style = Stroke(2.5f))
                        listOf(73f, 91f, 109f).forEach { y ->
                            drawCircle(ink.copy(alpha = .45f), 3f, Offset(105f, y))
                            drawLine(ink.copy(alpha = .55f), Offset(116f, y), Offset(136f, y), 3f, StrokeCap.Round)
                        }
                    }
                    EmptyStateVisual.Search -> {
                        drawRoundRect(tint, Offset(82f, 59f), Size(72f, 63f), CornerRadius(12f))
                        drawCircle(paper, 28f, Offset(116f, 85f)); drawCircle(ink, 28f, Offset(116f, 85f), style = Stroke(4f))
                        drawLine(ink, Offset(137f, 107f), Offset(158f, 130f), 6f, StrokeCap.Round)
                        drawLine(ink.copy(alpha = .35f), Offset(104f, 81f), Offset(128f, 81f), 3f, StrokeCap.Round)
                        drawLine(ink.copy(alpha = .35f), Offset(104f, 91f), Offset(120f, 91f), 3f, StrokeCap.Round)
                    }
                    EmptyStateVisual.Notifications -> {
                        val bell = Path().apply { moveTo(88f, 113f); quadraticTo(97f, 103f, 97f, 82f); cubicTo(97f, 53f, 143f, 53f, 143f, 82f); quadraticTo(143f, 103f, 152f, 113f); close() }
                        drawPath(bell, tint); drawPath(bell, ink, style = stroke)
                        drawArc(ink, 0f, 180f, false, Offset(111f, 115f), Size(18f, 17f), style = stroke)
                        drawCircle(ink, 4f, Offset(120f, 57f))
                    }
                    EmptyStateVisual.Archived -> {
                        drawRoundRect(tint, Offset(87f, 78f), Size(66f, 48f), CornerRadius(9f))
                        drawRoundRect(ink, Offset(87f, 78f), Size(66f, 48f), CornerRadius(9f), style = stroke)
                        drawRoundRect(paper, Offset(81f, 64f), Size(78f, 20f), CornerRadius(6f))
                        drawRoundRect(ink, Offset(81f, 64f), Size(78f, 20f), CornerRadius(6f), style = stroke)
                        drawLine(ink.copy(alpha = .6f), Offset(111f, 98f), Offset(129f, 98f), 3f, StrokeCap.Round)
                    }
                    EmptyStateVisual.Addresses -> {
                        val map = Path().apply { moveTo(81f, 83f); lineTo(107f, 75f); lineTo(133f, 83f); lineTo(159f, 75f); lineTo(159f, 126f); lineTo(133f, 134f); lineTo(107f, 126f); lineTo(81f, 134f); close() }
                        drawPath(map, tint.copy(alpha = .55f)); drawPath(map, ink.copy(alpha = .5f), style = Stroke(2f))
                        val pin = Path().apply { moveTo(120f, 111f); cubicTo(80f, 75f, 104f, 52f, 120f, 54f); cubicTo(140f, 52f, 158f, 75f, 120f, 111f); close() }
                        drawPath(pin, paper); drawPath(pin, ink, style = stroke)
                        drawCircle(ink.copy(alpha = .7f), 7f, Offset(120f, 74f))
                    }
                    EmptyStateVisual.Catalog -> {
                        drawRoundRect(tint, Offset(86f, 80f), Size(68f, 48f), CornerRadius(7f))
                        drawRoundRect(ink, Offset(86f, 80f), Size(68f, 48f), CornerRadius(7f), style = stroke)
                        val roof = Path().apply { moveTo(80f, 80f); lineTo(91f, 59f); lineTo(149f, 59f); lineTo(160f, 80f); close() }
                        drawPath(roof, paper); drawPath(roof, ink, style = stroke)
                        listOf(101f, 120f, 139f).forEach { x -> drawLine(ink.copy(alpha = .45f), Offset(x, 60f), Offset(x, 80f), 2f) }
                        drawRoundRect(paper, Offset(112f, 99f), Size(18f, 29f), CornerRadius(4f))
                    }
                }
            }
        }
        val badge = when {
            signInRequired -> Icons.Filled.Lock
            visual == EmptyStateVisual.Cart -> Icons.Filled.Add
            visual == EmptyStateVisual.Search -> Icons.Filled.Tune
            visual == EmptyStateVisual.Notifications -> Icons.Filled.Done
            visual == EmptyStateVisual.Archived -> Icons.Filled.Inventory2
            else -> Icons.Filled.AutoAwesome
        }
        Box(Modifier.offset(x = if (compact) 43.dp else 58.dp, y = if (compact) 37.dp else 49.dp)
            .size(if (compact) 28.dp else 38.dp).background(ink, CircleShape), contentAlignment = Alignment.Center) {
            Icon(badge, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(if (compact) 16.dp else 21.dp))
        }
    }
}
