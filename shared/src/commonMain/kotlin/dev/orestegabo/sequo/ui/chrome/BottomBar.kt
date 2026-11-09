package dev.orestegabo.sequo.ui.chrome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.orestegabo.sequo.design.SequoNavDestination
import dev.orestegabo.sequo.design.SequoNavigationBar
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
    SequoNavigationBar(
        modifier = modifier,
        destinations = destinations.map { destination ->
            SequoNavDestination(destination.name, destination.bottomLabel(), destination.icon,
                when (destination) {
                    SequoSection.Basket -> pendingBasketCount
                    SequoSection.Notifications -> notificationUnreadCount
                    else -> 0
                })
        },
        selectedKey = currentDestination.name,
        onSelect = { key -> destinations.firstOrNull { it.name == key }?.let(onDestinationSelected) },
    )
}

@Composable
private fun SequoSection.bottomLabel(): String =
    when (this) {
        SequoSection.Home -> appText(Res.string.bottom_home)
        SequoSection.Markets -> appText(Res.string.nav_markets_title)
        SequoSection.Basket -> appText(Res.string.nav_basket_title)
        SequoSection.Orders -> appText(Res.string.bottom_orders)
        SequoSection.Notifications -> appText(Res.string.nav_notifications_title)
        SequoSection.Settings -> appText(Res.string.drawer_settings)
    }
