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
internal fun SequoTopAppBar(
    currentDestination: SequoSection,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    productListing: SequoProductListing? = null,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val productMode = productListing != null
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (productMode) MaterialTheme.colorScheme.surface.copy(alpha = 0.94f) else MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
        tonalElevation = 3.dp,
        shadowElevation = if (productMode) 2.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = 20.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = if (!productMode) onMenuClick else onBackClick) {
                Icon(
                    imageVector = if (!productMode) Icons.Filled.Menu else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (!productMode) "Open menu" else "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = productListing?.product?.name ?: currentDestination.appBarTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = productListing?.let { "${it.shop.name} / ${it.shop.area}" } ?: currentDestination.appBarSubtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!productMode) {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp),
                    )
                }
                IconButton(onClick = onNotificationsClick) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ) {
                                Text("2")
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

private val SequoSection.appBarTitle: String
    get() = when (this) {
        SequoSection.Home -> "Tokoin Gbadago"
        SequoSection.Markets -> "Markets"
        SequoSection.Basket -> "Cart"
        SequoSection.Orders -> "Orders"
        SequoSection.Notifications -> "Notifications"
        SequoSection.Account -> "Account"
    }

private val SequoSection.appBarSubtitle: String
    get() = when (this) {
        SequoSection.Home -> "Deliver to Pharmacie des Etoiles area"
        SequoSection.Markets -> "Verified Lome sellers"
        SequoSection.Basket -> "Review and checkout"
        SequoSection.Orders -> "Track every handoff"
        SequoSection.Notifications -> "What needs your attention"
        SequoSection.Account -> "Addresses, payment, and support"
    }

@Composable
internal fun HomeAppBar() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SequoAppBar(
            title = "Tokoin Gbadago",
            subtitle = "Deliver to Pharmacie des Etoiles area",
            leadingIcon = Icons.Filled.Place,
            actions = listOf(
                AppBarAction(Icons.Filled.Payments, "Yas payment ready", emphasized = true),
                AppBarAction(Icons.Filled.Notifications, "Notifications", badge = "2"),
            ),
        )
        SequoStatusStrip(
            icon = Icons.Filled.PhotoCamera,
            title = "Camera checks nearby",
            detail = "8 refreshed",
            tag = "live",
        )
    }
}

@Composable
internal fun SequoAppBar(
    title: String,
    subtitle: String,
    leadingIcon: ImageVector,
    actions: List<AppBarAction>,
) {
    SequoCard(shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SequoIconMark(leadingIcon, MaterialTheme.colorScheme.primary, Modifier.size(38.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            actions.forEach { action ->
                AppBarIconButton(action)
            }
        }
    }
}

@Composable
internal fun AppBarIconButton(action: AppBarAction) {
    val accent = if (action.emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    Box {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (action.emphasized) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest)
                .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                .clickable { }
                .padding(11.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.contentDescription,
                tint = accent,
                modifier = Modifier.size(18.dp),
            )
        }
        action.badge?.let { badge ->
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.32f)),
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
internal fun SequoStatusStrip(
    icon: ImageVector,
    title: String,
    detail: String,
    tag: String,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SequoIconMark(icon, MaterialTheme.colorScheme.primary, Modifier.size(30.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            MetaPill(tag, SequoSecondary)
        }
    }
}

@Composable
internal fun SequoIconMark(icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.13f))
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
    }
}
