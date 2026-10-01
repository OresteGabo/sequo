package dev.orestegabo.sequo.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AssignmentReturn
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.theme.SequoPrimary
import dev.orestegabo.sequo.theme.SequoSecondary
import dev.orestegabo.sequo.ui.chrome.SequoIconMark
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.Res
import sequo.shared.generated.resources.sequohub_logo_mark

private enum class NotificationFilter(val label: String) {
    Today("Today"),
    Attention("Attention"),
    Promos("Promos"),
    Archived("Archived"),
}

private data class SequoNotificationItem(
    val id: String,
    val filter: NotificationFilter,
    val title: String,
    val detail: String,
    val time: String,
    val action: String? = null,
    val expandedTitle: String? = null,
    val expandedDetail: String? = null,
    val primaryAction: String? = null,
    val secondaryAction: String? = null,
    val secureCode: String? = null,
    val icon: ImageVector,
    val accent: Color,
    val unread: Boolean = false,
    val urgent: Boolean = false,
)

@Composable
internal fun NotificationsContent(
    onOpenOrders: () -> Unit,
) {
    var selectedFilter by remember { mutableStateOf(NotificationFilter.Today) }
    val notifications = remember { demoNotifications() }
    val archivedIds = remember { mutableStateListOf<String>() }
    val activeNotifications = notifications.filterNot { it.id in archivedIds }
    val archivedNotifications = notifications.filter { it.id in archivedIds }
    val urgentItems = activeNotifications.filter { it.urgent }
    val visibleItems = when (selectedFilter) {
        NotificationFilter.Today -> activeNotifications.filter { it.filter != NotificationFilter.Promos }
        NotificationFilter.Attention -> urgentItems
        NotificationFilter.Promos -> activeNotifications.filter { it.filter == NotificationFilter.Promos }
        NotificationFilter.Archived -> archivedNotifications
    }
    val unreadCount = activeNotifications.count { it.unread }

    NotificationCompactSummary(
        unreadCount = unreadCount,
        urgentCount = urgentItems.size,
        archivedCount = archivedIds.size,
    )
    NotificationFilters(
        selectedFilter = selectedFilter,
        onFilterSelected = { selectedFilter = it },
    )
    NotificationListHeader(
        title = when (selectedFilter) {
            NotificationFilter.Today -> "Today"
            NotificationFilter.Attention -> "Needs attention"
            NotificationFilter.Promos -> "Promos"
            NotificationFilter.Archived -> "Archived"
        },
        action = "${visibleItems.size}",
    )
    if (visibleItems.isEmpty()) {
        NotificationEmptyState(
            filter = selectedFilter,
            onOpenOrders = onOpenOrders,
        )
    } else {
        NotificationList(
            items = visibleItems,
            archived = selectedFilter == NotificationFilter.Archived,
            onArchive = { id ->
                if (id !in archivedIds) {
                    archivedIds.add(id)
                }
            },
            onRestore = { archivedIds.remove(it) },
        )
    }
}

@Composable
private fun NotificationCompactSummary(unreadCount: Int, urgentCount: Int, archivedCount: Int) {
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
                    "$unreadCount unread / $urgentCount need attention / $archivedCount archived",
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
private fun NotificationList(
    items: List<SequoNotificationItem>,
    archived: Boolean = false,
    onArchive: (String) -> Unit,
    onRestore: (String) -> Unit = {},
) {
    Column {
        items.forEachIndexed { index, item ->
            key(item.id) {
                NotificationRow(
                    item = item,
                    archived = archived,
                    onArchive = { onArchive(item.id) },
                    onRestore = { onRestore(item.id) },
                )
                if (index != items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 50.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.52f),
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationEmptyState(
    filter: NotificationFilter,
    onOpenOrders: () -> Unit,
) {
    val content = emptyStateContent(filter)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.52f)),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(Res.drawable.sequohub_logo_mark),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .size(118.dp)
                    .alpha(0.055f),
            )
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = content.accent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, content.accent.copy(alpha = 0.18f)),
                ) {
                    Icon(
                        imageVector = content.icon,
                        contentDescription = null,
                        tint = content.accent,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                Text(
                    content.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Text(
                    content.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                content.actionLabel?.let { label ->
                    NotificationEmptyAction(
                        label = label,
                        accent = content.accent,
                        onClick = when (filter) {
                            NotificationFilter.Attention -> onOpenOrders
                            else -> ({})
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationEmptyAction(
    label: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier.padding(start = 13.dp, top = 8.dp, end = 11.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private data class NotificationEmptyContent(
    val icon: ImageVector,
    val title: String,
    val detail: String,
    val accent: Color,
    val actionLabel: String? = null,
)

@Composable
private fun emptyStateContent(filter: NotificationFilter): NotificationEmptyContent =
    when (filter) {
        NotificationFilter.Today -> NotificationEmptyContent(
            icon = Icons.Filled.NotificationsNone,
            title = "You are all caught up",
            detail = "Today’s order alerts, offer replies, pickup codes, payment updates, and security notices will appear here.",
            accent = SequoPrimary,
        )
        NotificationFilter.Attention -> NotificationEmptyContent(
            icon = Icons.Filled.PriorityHigh,
            title = "Nothing urgent",
            detail = "Time-sensitive pickup codes, expiring seller offers, failed payments, and delivery problems will be grouped here.",
            accent = SequoPrimary,
            actionLabel = "View orders",
        )
        NotificationFilter.Promos -> NotificationEmptyContent(
            icon = Icons.Filled.LocalOffer,
            title = "No promos right now",
            detail = "Useful price drops, seasonal product alerts, and campaign messages stay here instead of crowding urgent updates.",
            accent = Color(0xFF8F5576),
        )
        NotificationFilter.Archived -> NotificationEmptyContent(
            icon = Icons.Filled.Archive,
            title = "Nothing archived yet",
            detail = "Swipe a notification left to archive it. Archived notifications can be restored here until cleanup removes them from this inbox.",
            accent = MaterialTheme.colorScheme.primary,
        )
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
                leadingIcon = {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                },
                label = { Text(filter.label) },
            )
        }
        Spacer(Modifier.width(2.dp))
    }
}

private val NotificationFilter.icon: ImageVector
    get() = when (this) {
        NotificationFilter.Today -> Icons.Filled.Today
        NotificationFilter.Attention -> Icons.Filled.NotificationsActive
        NotificationFilter.Promos -> Icons.Filled.LocalOffer
        NotificationFilter.Archived -> Icons.Filled.Archive
    }

@Composable
private fun NotificationRow(
    item: SequoNotificationItem,
    archived: Boolean = false,
    onArchive: () -> Unit,
    onRestore: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    var cancelled by remember { mutableStateOf(false) }
    val secureCodeVisible = remember { mutableStateOf(false) }
    val hasExpandedActions = item.primaryAction != null || item.secondaryAction != null || item.secureCode != null
    val rowAlpha = when {
        cancelled -> 0.52f
        archived -> 0.72f
        else -> 1f
    }
    var dragOffset by remember { mutableStateOf(0f) }
    var archivePending by remember { mutableStateOf(false) }
    val maxRevealPx = with(LocalDensity.current) { 112.dp.toPx() }
    val archiveThresholdPx = with(LocalDensity.current) { 86.dp.toPx() }

    LaunchedEffect(archivePending) {
        if (archivePending) {
            delay(260)
            onArchive()
        }
    }

    AnimatedVisibility(
        visible = !archivePending,
        exit = fadeOut(animationSpec = tween(durationMillis = 180)) +
            shrinkVertically(
                animationSpec = tween(durationMillis = 260),
                shrinkTowards = Alignment.Top,
            ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SwipeArchiveBackground(
                modifier = Modifier.matchParentSize(),
                visible = dragOffset < -8f && !archived,
            )
            Column(
                modifier = Modifier
                    .offset { IntOffset(dragOffset.roundToInt(), 0) }
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(item.id) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (!archived && -dragOffset >= archiveThresholdPx) {
                                    archivePending = true
                                }
                                dragOffset = 0f
                            },
                            onDragCancel = { dragOffset = 0f },
                        ) { _, dragAmount ->
                            if (!archivePending && !archived) {
                                dragOffset = (dragOffset + dragAmount).coerceIn(-maxRevealPx, 0f)
                            }
                        }
                    },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = hasExpandedActions && !cancelled && !archivePending) { expanded = !expanded }
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
                            if (item.unread && !cancelled && !archived) {
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
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            item.time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = rowAlpha),
                        )
                        when {
                            archived -> NotificationActionPill(
                                label = "Restore",
                                accent = item.accent,
                                onClick = onRestore,
                            )
                            item.secureCode != null && !cancelled -> NotificationActionPill(
                                label = item.action ?: "Show",
                                accent = item.accent,
                                onClick = {
                                    secureCodeVisible.value = true
                                    expanded = true
                                },
                            )
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
                        showSecureCode = secureCodeVisible.value,
                        onAccept = { expanded = false },
                        onRefuse = {
                            cancelled = true
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeArchiveBackground(modifier: Modifier = Modifier, visible: Boolean) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (visible) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f)
                } else {
                    Color.Transparent
                },
            )
            .padding(end = 18.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        if (visible) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Archive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    "Archive",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
            }
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
    showSecureCode: Boolean,
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
        if (item.secureCode != null && showSecureCode) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, item.accent.copy(alpha = 0.24f)),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "Pickup code",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        item.secureCode,
                        style = MaterialTheme.typography.headlineSmall,
                        color = item.accent,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        "Show this only at handoff.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (item.primaryAction != null || item.secondaryAction != null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
private fun NotificationActionPill(label: String, accent: Color, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
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

private fun demoNotifications(): List<SequoNotificationItem> = emptyList()
