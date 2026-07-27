package dev.orestegabo.sequo.ui.chrome

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
internal fun SequoAmbientBackground(modifier: Modifier = Modifier) {
    val uiPalette = sequoUi
    val baseTop = MaterialTheme.colorScheme.background
    val baseBottom = uiPalette.ambientBottom
    val lineColor = uiPalette.ambientLineStrong
    val softLineColor = uiPalette.ambientLineSoft
    val circlePrimary = uiPalette.ambientCirclePrimary
    val circleSecondary = uiPalette.ambientCircleSecondary
    val topPanelColor = uiPalette.ambientPanelTop
    val bottomPanelColor = uiPalette.ambientPanelBottom

    Canvas(modifier = modifier.background(Brush.verticalGradient(listOf(baseTop, baseTop, baseBottom)))) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = topPanelColor,
            topLeft = Offset(w * 0.04f, h * 0.07f),
            size = Size(w * 0.92f, h * 0.24f),
            cornerRadius = CornerRadius(42f, 42f),
        )
        drawRoundRect(
            color = bottomPanelColor,
            topLeft = Offset(w * 0.05f, h * 0.66f),
            size = Size(w * 0.90f, h * 0.20f),
            cornerRadius = CornerRadius(42f, 42f),
        )

        drawLine(color = lineColor, start = Offset(w * 0.08f, h * 0.10f), end = Offset(w * 0.72f, h * 0.10f), strokeWidth = 5f, cap = StrokeCap.Round)
        drawLine(color = lineColor, start = Offset(w * 0.12f, h * 0.135f), end = Offset(w * 0.88f, h * 0.135f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(color = softLineColor, start = Offset(w * 0.18f, h * 0.74f), end = Offset(w * 0.84f, h * 0.74f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color = lineColor, start = Offset(w * 0.14f, h * 0.79f), end = Offset(w * 0.62f, h * 0.79f), strokeWidth = 5f, cap = StrokeCap.Round)

        val topPath = Path().apply {
            moveTo(w * 0.62f, h * 0.06f)
            lineTo(w * 0.76f, h * 0.06f)
            lineTo(w * 0.82f, h * 0.11f)
            lineTo(w * 0.93f, h * 0.11f)
            lineTo(w * 0.93f, h * 0.18f)
            lineTo(w * 0.82f, h * 0.18f)
            lineTo(w * 0.75f, h * 0.24f)
            lineTo(w * 0.58f, h * 0.24f)
        }
        drawPath(path = topPath, color = lineColor, style = Stroke(width = 3f))

        val bottomPath = Path().apply {
            moveTo(w * 0.10f, h * 0.88f)
            lineTo(w * 0.26f, h * 0.88f)
            lineTo(w * 0.33f, h * 0.83f)
            lineTo(w * 0.48f, h * 0.83f)
            lineTo(w * 0.48f, h * 0.90f)
            lineTo(w * 0.34f, h * 0.90f)
            lineTo(w * 0.27f, h * 0.95f)
            lineTo(w * 0.12f, h * 0.95f)
        }
        drawPath(path = bottomPath, color = softLineColor, style = Stroke(width = 3f))

        drawCircle(color = circlePrimary, radius = w * 0.18f, center = Offset(w * 0.88f, h * 0.22f), style = Stroke(width = 5f))
        drawCircle(color = circleSecondary, radius = w * 0.22f, center = Offset(w * 0.12f, h * 0.82f), style = Stroke(width = 5f))

        repeat(7) { index ->
            val y = h * (0.185f + index * 0.018f)
            drawLine(
                color = if (index % 2 == 0) lineColor else softLineColor,
                start = Offset(w * 0.11f, y),
                end = Offset(w * (0.36f + index * 0.06f), y),
                strokeWidth = if (index % 2 == 0) 3f else 2f,
                cap = StrokeCap.Round,
            )
        }
    }
}
