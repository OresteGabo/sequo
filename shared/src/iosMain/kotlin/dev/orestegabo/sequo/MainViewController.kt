package dev.orestegabo.sequo

import androidx.compose.ui.window.ComposeUIViewController
import dev.orestegabo.sequo.feature.settings.AppLanguage

fun MainViewController(
    onHomeEntered: (AppLanguage) -> Unit = {},
    openNotificationsRequest: Int = 0,
) = ComposeUIViewController {
    App(
        onHomeEntered = onHomeEntered,
        openNotificationsRequest = openNotificationsRequest,
    )
}
