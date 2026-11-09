package dev.orestegabo.sequo.feature.settings

import androidx.compose.runtime.Composable

/** Opens OS-managed permissions; never pretends to enable notifications inside Sequo. */
@Composable
expect fun rememberOpenNotificationSettings(): () -> Boolean
