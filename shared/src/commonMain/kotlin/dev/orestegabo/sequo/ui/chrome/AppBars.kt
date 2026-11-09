package dev.orestegabo.sequo.ui.chrome

import dev.orestegabo.sequo.design.sequoBrandSymbolPainter
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
import dev.orestegabo.sequo.design.SequoPageHeader
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
internal fun SequoTopAppBar(
    currentDestination: SequoSection,
    sectionTitle: String? = null,
    sectionSubtitle: String? = null,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    notificationUnreadCount: Int = 0,
    productListing: SequoProductListing? = null,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val productMode = productListing != null
    val backMode = productMode || showBackButton
    val searchAvailable = currentDestination != SequoSection.Notifications
    Row(modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(onClick = if (backMode) onBackClick else onMenuClick) {
            Icon(if (backMode) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.Menu,
                contentDescription = appText(if (backMode) Res.string.cd_back else Res.string.cd_open_menu),
                modifier = Modifier.size(23.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(productListing?.product?.name ?: sectionTitle ?: currentDestination.appBarTitle(),
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(productListing?.shop?.name ?: sectionSubtitle ?: currentDestination.appBarSubtitle(),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (!productMode && currentDestination != SequoSection.Notifications) {
            if (searchAvailable) IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, appText(Res.string.cd_search), modifier = Modifier.size(23.dp))
            }
            IconButton(onClick = onNotificationsClick) {
                BadgedBox(badge = { if (notificationUnreadCount > 0) Badge {
                    Text(if (notificationUnreadCount > 99) "99+" else "$notificationUnreadCount")
                } }) { Icon(Icons.Filled.NotificationsNone, appText(Res.string.cd_notifications), modifier = Modifier.size(23.dp)) }
            }
        }
    }
}

@Composable
private fun SequoSection.appBarTitle(): String =
    when (this) {
        SequoSection.Home -> appText(Res.string.nav_home_title)
        SequoSection.Markets -> appText(Res.string.nav_markets_title)
        SequoSection.Basket -> appText(Res.string.nav_basket_title)
        SequoSection.Orders -> appText(Res.string.nav_orders_title)
        SequoSection.Notifications -> appText(Res.string.nav_notifications_title)
        SequoSection.Settings -> appText(Res.string.drawer_settings)
    }

@Composable
private fun SequoSection.appBarSubtitle(): String =
    when (this) {
        SequoSection.Home -> appText(Res.string.nav_home_subtitle)
        SequoSection.Markets -> appText(Res.string.nav_markets_subtitle)
        SequoSection.Basket -> appText(Res.string.nav_basket_subtitle)
        SequoSection.Orders -> appText(Res.string.nav_orders_subtitle)
        SequoSection.Notifications -> appText(Res.string.nav_notifications_subtitle)
        SequoSection.Settings -> appText(Res.string.settings_subtitle)
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

@Composable
internal fun SequoShoppingMark(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.13f))
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
            .padding(5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = sequoBrandSymbolPainter(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
