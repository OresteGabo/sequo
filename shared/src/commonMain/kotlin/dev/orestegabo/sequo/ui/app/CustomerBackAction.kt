package dev.orestegabo.sequo.ui.app

internal enum class CustomerBackAction { CloseDrawer, CloseSearch, CloseProduct, NotificationOrigin, Home, None }

/** Reuse existing close/tab actions; this deliberately does not invent a navigation stack. */
internal fun customerBackAction(
    drawerOpen: Boolean,
    searchOpen: Boolean,
    productOpen: Boolean,
    hasNotificationOrigin: Boolean,
    isHome: Boolean,
): CustomerBackAction = when {
    drawerOpen -> CustomerBackAction.CloseDrawer
    searchOpen -> CustomerBackAction.CloseSearch
    productOpen -> CustomerBackAction.CloseProduct
    hasNotificationOrigin -> CustomerBackAction.NotificationOrigin
    !isHome -> CustomerBackAction.Home
    else -> CustomerBackAction.None
}
