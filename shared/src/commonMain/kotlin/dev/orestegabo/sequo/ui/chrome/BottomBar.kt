package dev.orestegabo.sequo.ui.chrome

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.feature.settings.appText
import dev.orestegabo.sequo.model.SequoSection
import dev.orestegabo.sequo.model.sequoPrimaryDestinations
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
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            destinations.forEach { destination ->
                val selected = destination == currentDestination
                val count = when (destination) {
                    SequoSection.Basket -> pendingBasketCount
                    SequoSection.Notifications -> notificationUnreadCount
                    else -> 0
                }
                Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                    .selectable(selected, role = Role.Tab, onClick = { onDestinationSelected(destination) })
                    .padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(shape = RoundedCornerShape(12.dp), modifier = Modifier.size(52.dp, 30.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface) {
                        Box(contentAlignment = Alignment.Center) {
                            BadgedBox(badge = { if (count > 0) Badge { Text(if (count > 99) "99+" else "$count") } }) {
                                Icon(destination.icon, null, modifier = Modifier.size(22.dp),
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Text(destination.bottomLabel(), style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun SequoSection.bottomLabel(): String = when (this) {
    SequoSection.Home -> appText(Res.string.bottom_home)
    SequoSection.Markets -> appText(Res.string.nav_markets_title)
    SequoSection.Basket -> appText(Res.string.nav_basket_title)
    SequoSection.Orders -> appText(Res.string.bottom_orders)
    SequoSection.Notifications -> appText(Res.string.nav_notifications_title)
    SequoSection.Settings -> appText(Res.string.drawer_settings)
}
