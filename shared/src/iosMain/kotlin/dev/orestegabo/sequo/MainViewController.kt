package dev.orestegabo.sequo

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(
    onHomeEntered: () -> Unit = {},
    openNotificationsRequest: Int = 0,
) = ComposeUIViewController {
    App(
        onHomeEntered = onHomeEntered,
        openNotificationsRequest = openNotificationsRequest,
    )
}
