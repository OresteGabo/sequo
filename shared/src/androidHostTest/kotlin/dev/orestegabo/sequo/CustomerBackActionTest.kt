package dev.orestegabo.sequo

import dev.orestegabo.sequo.ui.app.CustomerBackAction
import dev.orestegabo.sequo.ui.app.customerBackAction
import kotlin.test.Test
import kotlin.test.assertEquals

class CustomerBackActionTest {
    private fun action(drawer: Boolean = false, search: Boolean = false, product: Boolean = false,
        notification: Boolean = false, home: Boolean = true) = customerBackAction(drawer, search, product, notification, home)

    @Test fun drawerClosesBeforeOtherContent() { assertEquals(CustomerBackAction.CloseDrawer, action(drawer = true, search = true, product = true, home = false)) }
    @Test fun searchClosesBeforeUnderlyingProductOrTab() { assertEquals(CustomerBackAction.CloseSearch, action(search = true, product = true, home = false)) }
    @Test fun productBackClosesDetailRatherThanExiting() { assertEquals(CustomerBackAction.CloseProduct, action(product = true)) }
    @Test fun notificationsReturnToTheirRecordedOrigin() { assertEquals(CustomerBackAction.NotificationOrigin, action(notification = true, home = false)) }
    @Test fun otherTabsReturnHomeWithoutInventingHistory() { assertEquals(CustomerBackAction.Home, action(home = false)) }
    @Test fun homeLeavesSystemExitBehaviorAvailable() { assertEquals(CustomerBackAction.None, action()) }
}
