package dev.orestegabo.sequo.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AssignmentReturn
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.data.recentOrders
import dev.orestegabo.sequo.logic.formatCfa
import dev.orestegabo.sequo.logic.orderTitle
import dev.orestegabo.sequo.theme.SequoPrimary
import dev.orestegabo.sequo.theme.SequoSecondary
import dev.orestegabo.sequo.ui.chrome.SequoIconMark

private enum class NotificationFilter(val label: String) {
    All("All"),
    Orders("Orders"),
    Bargains("Offers"),
    Delivery("Delivery"),
    Payments("Payments"),
    Returns("Returns"),
    Promos("Promos"),
    Security("Security"),
}

private data class SequoNotificationItem(
    val filter: NotificationFilter,
    val title: String,
    val detail: String,
    val time: String,
    val action: String? = null,
    val expandedTitle: String? = null,
    val expandedDetail: String? = null,
    val primaryAction: String? = null,
    val secondaryAction: String? = null,
    val icon: ImageVector,
    val accent: Color,
    val unread: Boolean = false,
    val urgent: Boolean = false,
)

@Composable
internal fun NotificationsContent(
    onOpenOrders: () -> Unit,
    onOpenCart: () -> Unit,
) {
    var selectedFilter by remember { mutableStateOf(NotificationFilter.All) }
    val notifications = remember { demoNotifications() }
    val visibleItems = if (selectedFilter == NotificationFilter.All) {
        notifications
    } else {
        notifications.filter { it.filter == selectedFilter }
    }
    val urgentItems = notifications.filter { it.urgent }
    val unreadCount = notifications.count { it.unread }

    NotificationCompactSummary(
        unreadCount = unreadCount,
        urgentCount = urgentItems.size,
    )
    NotificationFilters(
        selectedFilter = selectedFilter,
        onFilterSelected = { selectedFilter = it },
    )
    if (urgentItems.isNotEmpty() && selectedFilter == NotificationFilter.All) {
        NotificationListHeader("Needs attention", "${urgentItems.size} now")
        NotificationList(items = urgentItems)
    }
    NotificationListHeader(
        title = if (selectedFilter == NotificationFilter.All) "Recent updates" else selectedFilter.label,
        action = "${visibleItems.size}",
    )
    NotificationList(items = visibleItems)
    NotificationQuickActions(
        onOpenOrders = onOpenOrders,
        onOpenCart = onOpenCart,
    )
}

@Composable
private fun NotificationCompactSummary(unreadCount: Int, urgentCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.64f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.52f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Inbox",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "$unreadCount unread / $urgentCount need attention",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            NotificationCountPill("$unreadCount")
        }
    }
}

@Composable
private fun NotificationListHeader(title: String, action: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            action,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun NotificationList(items: List<SequoNotificationItem>) {
    Column {
        items.forEachIndexed { index, item ->
            NotificationRow(item = item)
            if (index != items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 50.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.52f),
                )
            }
        }
    }
}

@Composable
private fun NotificationQuickActions(
    onOpenOrders: () -> Unit,
    onOpenCart: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        NotificationActionButton(
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            label = "Orders",
            onClick = onOpenOrders,
            modifier = Modifier.weight(1f),
        )
        NotificationActionButton(
            icon = Icons.Filled.ShoppingBasket,
            label = "Cart",
            onClick = onOpenCart,
            modifier = Modifier.weight(1f),
        )
        NotificationActionButton(
            icon = Icons.Filled.DoneAll,
            label = "Read",
            onClick = {},
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NotificationActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun NotificationFilters(
    selectedFilter: NotificationFilter,
    onFilterSelected: (NotificationFilter) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NotificationFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
            )
        }
        Spacer(Modifier.width(2.dp))
    }
}

@Composable
private fun NotificationRow(
    item: SequoNotificationItem,
) {
    var expanded by remember { mutableStateOf(false) }
    var cancelled by remember { mutableStateOf(false) }
    val hasExpandedActions = item.primaryAction != null || item.secondaryAction != null
    val rowAlpha = if (cancelled) 0.52f else 1f

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(enabled = hasExpandedActions && !cancelled) { expanded = !expanded }
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SequoIconMark(item.icon, item.accent.copy(alpha = rowAlpha), Modifier.size(38.dp))
            Column(
                modifier = Modifier.weight(1f).padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (cancelled) "${item.title} cancelled" else item.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (item.unread && !cancelled) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = rowAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (item.unread && !cancelled) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(item.accent),
                        )
                    }
                }
                Text(
                    if (cancelled) "The seller offer was refused and removed from checkout." else item.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = rowAlpha),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    item.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = rowAlpha),
                )
                when {
                    hasExpandedActions && !cancelled -> ExpandHint(expanded = expanded, accent = item.accent)
                    item.action != null && !cancelled -> NotificationActionPill(
                        label = item.action,
                        accent = item.accent,
                    )
                }
            }
        }
        if (expanded && hasExpandedActions && !cancelled) {
            NotificationExpandedActions(
                item = item,
                onAccept = { expanded = false },
                onRefuse = {
                    cancelled = true
                    expanded = false
                },
            )
        }
    }
}

@Composable
private fun ExpandHint(expanded: Boolean, accent: Color) {
    Surface(
        shape = CircleShape,
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
    ) {
        Icon(
            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.padding(5.dp).size(18.dp),
        )
    }
}

@Composable
private fun NotificationExpandedActions(
    item: SequoNotificationItem,
    onAccept: () -> Unit,
    onRefuse: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(start = 50.dp, end = 0.dp, bottom = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(item.accent.copy(alpha = 0.08f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item.expandedTitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item.expandedDetail?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item.secondaryAction?.let { label ->
                Surface(
                    onClick = onRefuse,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            item.primaryAction?.let { label ->
                Surface(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = item.accent,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationActionPill(label: String, accent: Color) {
    Surface(
        onClick = {},
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 6.dp, end = 8.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun NotificationCountPill(count: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
    ) {
        Text(
            count,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun demoNotifications(): List<SequoNotificationItem> {
    val activeOrder = recentOrders.first()
    return listOf(
        SequoNotificationItem(
            filter = NotificationFilter.Orders,
            title = "${activeOrder.id} arriving soon",
            detail = "${orderTitle(activeOrder)} is on the way. Keep your phone nearby for handoff.",
            time = "Now",
            action = "Track order",
            icon = Icons.Filled.LocalShipping,
            accent = SequoPrimary,
            unread = true,
            urgent = true,
        ),
        SequoNotificationItem(
            filter = NotificationFilter.Bargains,
            title = "Seller countered your offer",
            detail = "Hedzranawoe Electronics replied: ${formatCfa(145000)} for Dell Latitude. Offer expires tonight.",
            time = "4 min",
            expandedTitle = "Final price: ${formatCfa(145000)}",
            expandedDetail = "Accepting locks this price for checkout. Refusing cancels this bargain thread and removes the offer.",
            primaryAction = "Accept",
            secondaryAction = "Refuse",
            icon = Icons.Filled.Handshake,
            accent = SequoSecondary,
            unread = true,
            urgent = true,
        ),
        SequoNotificationItem(
            filter = NotificationFilter.Delivery,
            title = "Package B joined Package A",
            detail = "Two nearby sellers can be delivered together. Estimated delivery fee dropped by ${formatCfa(700)}.",
            time = "12 min",
            icon = Icons.Filled.Inventory2,
            accent = Color(0xFF5F7C44),
            unread = true,
        ),
        SequoNotificationItem(
            filter = NotificationFilter.Payments,
            title = "Payment confirmed",
            detail = "Yas Togo confirmed ${formatCfa(activeOrder.amountCfa)} for ${activeOrder.id}. Receipt is ready.",
            time = "18 min",
            icon = Icons.Filled.Payments,
            accent = Color(0xFF3C6E91),
        ),
        SequoNotificationItem(
            filter = NotificationFilter.Returns,
            title = "Return window reminder",
            detail = "SQ-2415 remains eligible for standard return review until tomorrow evening.",
            time = "1 h",
            action = "Return hub",
            icon = Icons.AutoMirrored.Filled.AssignmentReturn,
            accent = Color(0xFF8A6A3F),
        ),
        SequoNotificationItem(
            filter = NotificationFilter.Promos,
            title = "Fresh grocery price drop",
            detail = "Green pepper and fresh milk are trending near Tokoin with verified shop photos today.",
            time = "2 h",
            icon = Icons.Filled.LocalOffer,
            accent = Color(0xFF8F5576),
        ),
        SequoNotificationItem(
            filter = NotificationFilter.Orders,
            title = "Pickup code ready",
            detail = "Grand Marche Assigame is ready. Use pickup code ${recentOrders[2].pickupCode} only at handoff.",
            time = "Today",
            action = "Show code",
            icon = Icons.Filled.QrCode2,
            accent = Color(0xFF6F5EA8),
            unread = true,
            urgent = true,
        ),
        SequoNotificationItem(
            filter = NotificationFilter.Security,
            title = "New sign-in protected",
            detail = "A sign-in was checked for your account. No action needed if this was you.",
            time = "Yesterday",
            action = "Account",
            icon = Icons.Filled.Security,
            accent = Color(0xFF607D8B),
        ),
        SequoNotificationItem(
            filter = NotificationFilter.Bargains,
            title = "Offer expired",
            detail = "Your last offer for black running shoes expired. The listed price is still available.",
            time = "Yesterday",
            icon = Icons.Filled.TimerOff,
            accent = Color(0xFF795548),
        ),
    )
}
