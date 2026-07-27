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
internal fun SequoBottomBar(
    modifier: Modifier = Modifier,
    currentDestination: SequoSection,
    onDestinationSelected: (SequoSection) -> Unit,
    pendingBasketCount: Int = 0,
    destinations: List<SequoSection> = sequoPrimaryDestinations,
) {
    SequoNavigationContainer(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEach { destination ->
                SequoBottomNavItem(
                    destination = destination,
                    selected = currentDestination == destination,
                    onClick = { onDestinationSelected(destination) },
                    badgeCount = if (destination == SequoSection.Basket) pendingBasketCount else 0,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
internal fun SequoNavigationContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val uiPalette = sequoUi
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        color = Color.Transparent,
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            uiPalette.floatingShell.copy(alpha = 0.96f),
                            colors.surface.copy(alpha = 0.92f),
                        ),
                    ),
                )
                .border(
                    BorderStroke(
                        1.dp,
                        uiPalette.floatingShellBorder.copy(alpha = 0.9f),
                    ),
                    RoundedCornerShape(30.dp),
                ),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                drawLine(
                    color = colors.primary.copy(alpha = 0.10f),
                    start = Offset(w * 0.10f, h * 0.20f),
                    end = Offset(w * 0.88f, h * 0.20f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = colors.secondary.copy(alpha = 0.08f),
                    radius = w * 0.12f,
                    center = Offset(w * 0.90f, h * 0.12f),
                    style = Stroke(width = 3f),
                )
            }
            content()
        }
    }
}

@Composable
internal fun SequoNavigationItemFrame(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Color) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = if (selected) colors.surface.copy(alpha = 0.94f) else Color.Transparent
    val contentColor = if (selected) colors.onSurface else colors.onSurfaceVariant.copy(alpha = 0.92f)

    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = if (selected) 1.dp else 0.dp,
        shadowElevation = if (selected) 2.dp else 0.dp,
        border = null,
        modifier = modifier.then(
            if (selected) {
                Modifier.drawWithContent {
                    drawContent()
                    val strokeWidth = 1.5.dp.toPx()
                    val inset = strokeWidth / 2f
                    val radius = 22.dp.toPx() - inset
                    val topStopY = size.height * 0.30f
                    val leftX = inset
                    val rightX = size.width - inset
                    val bottomY = size.height - inset
                    val sideStrokePath = Path().apply {
                        moveTo(leftX, topStopY)
                        lineTo(leftX, bottomY - radius)
                        quadraticTo(leftX, bottomY, leftX + radius, bottomY)
                        moveTo(rightX - radius, bottomY)
                        quadraticTo(rightX, bottomY, rightX, bottomY - radius)
                        lineTo(rightX, topStopY)
                    }
                    val sideStrokeBrush = Brush.verticalGradient(
                        colors = listOf(
                            colors.outline.copy(alpha = 0f),
                            colors.primary.copy(alpha = 0.18f),
                            colors.primary.copy(alpha = 0.34f),
                        ),
                        startY = topStopY,
                        endY = bottomY,
                    )
                    drawPath(
                        path = sideStrokePath,
                        brush = sideStrokeBrush,
                        style = Stroke(width = strokeWidth),
                    )
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colors.primary.copy(alpha = 0.24f),
                                colors.primary.copy(alpha = 0.40f),
                                colors.primary.copy(alpha = 0.24f),
                            ),
                            startX = leftX + radius * 0.72f,
                            endX = rightX - radius * 0.72f,
                        ),
                        start = Offset(leftX + radius * 0.72f, bottomY),
                        end = Offset(rightX - radius * 0.72f, bottomY),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
            } else {
                Modifier
            },
        ),
    ) {
        content(contentColor)
    }
}

@Composable
internal fun SequoBottomNavItem(
    destination: SequoSection,
    selected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int,
    modifier: Modifier = Modifier,
) {
    SequoNavigationItemFrame(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    ) { contentColor ->
        val selectedIconContainerColor =
            if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            } else {
                Color.Transparent
            }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(selectedIconContainerColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                        modifier = Modifier.size(23.dp),
                        tint = contentColor,
                    )
                }
                NavigationBadge(
                    count = badgeCount,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp),
                )
            }
            Text(
                text = destination.label,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp),
            )
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(width = if (selected) 18.dp else 10.dp, height = 3.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
                    ),
            )
        }
    }
}

@Composable
internal fun NavigationBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    if (count <= 0) return
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.36f)),
    ) {
        Text(
            text = count.coerceAtMost(9).toString(),
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
