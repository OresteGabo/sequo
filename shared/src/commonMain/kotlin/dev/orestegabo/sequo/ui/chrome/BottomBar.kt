package dev.orestegabo.sequo.ui.chrome

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.designsystem.component.SequoShapes
import dev.orestegabo.sequo.feature.settings.appText
import dev.orestegabo.sequo.model.SequoSection
import dev.orestegabo.sequo.model.sequoPrimaryDestinations
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.*

@Composable
internal fun SequoBottomBar(
    modifier: Modifier = Modifier,
    currentDestination: SequoSection,
    onDestinationSelected: (SequoSection) -> Unit,
    pendingBasketCount: Int = 0,
    notificationUnreadCount: Int = 0,
    destinations: List<SequoSection> = sequoPrimaryDestinations,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = Color.Transparent,
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SequoShapes.NavContainer)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.surface.copy(alpha = 0.98f),
                            colorScheme.surfaceContainer.copy(alpha = 0.94f),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = colorScheme.outlineVariant.copy(alpha = 0.45f),
                    shape = SequoShapes.NavContainer,
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                destinations.forEach { destination ->
                    SequoBottomNavItem(
                        modifier = Modifier.weight(1f),
                        destination = destination,
                        selected = currentDestination == destination,
                        onClick = { onDestinationSelected(destination) },
                        badgeCount = when (destination) {
                            SequoSection.Basket -> pendingBasketCount
                            SequoSection.Notifications -> notificationUnreadCount
                            else -> 0
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SequoBottomNavItem(
    destination: SequoSection,
    selected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val label = destination.bottomLabel()
    val containerColor by animateColorAsState(
        targetValue = if (selected) colorScheme.primaryContainer else Color.Transparent,
        label = "navItemContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
        label = "navItemContent",
    )

    Surface(
        onClick = onClick,
        color = containerColor,
        shape = SequoShapes.NavItem,
        tonalElevation = if (selected) 3.dp else 0.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(SequoShapes.IconCapsule)
                    .background(if (selected) colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                BadgedBox(
                    badge = {
                        if (badgeCount > 0) {
                            Badge(
                                containerColor = colorScheme.secondaryContainer,
                                contentColor = colorScheme.onSecondaryContainer,
                            ) {
                                Text(badgeCount.coerceAtMost(9).toString())
                            }
                        }
                    },
                ) {
                    if (destination == SequoSection.Home) {
                        Image(
                            painter = painterResource(Res.drawable.sequo_shopping_icon),
                            contentDescription = label,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(36.dp),
                        )
                    } else {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(width = 16.dp, height = 3.dp)
                    .clip(SequoShapes.IconCapsule)
                    .background(if (selected) colorScheme.primary else Color.Transparent),
            )
        }
    }
}

@Composable
private fun SequoSection.bottomLabel(): String =
    when (this) {
        SequoSection.Home -> appText(Res.string.bottom_home)
        SequoSection.Markets -> appText(Res.string.nav_markets_title)
        SequoSection.Basket -> appText(Res.string.nav_basket_title)
        SequoSection.Orders -> appText(Res.string.nav_orders_title)
        SequoSection.Notifications -> appText(Res.string.nav_notifications_title)
        SequoSection.Account -> appText(Res.string.nav_account_title)
    }
