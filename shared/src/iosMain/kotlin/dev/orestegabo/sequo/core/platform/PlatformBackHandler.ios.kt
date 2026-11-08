package dev.orestegabo.sequo.core.platform

import androidx.compose.runtime.Composable

// iOS retains the existing visible navigation controls; it has no Android system Back action.
@Suppress("UNUSED_PARAMETER")
@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
