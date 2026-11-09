package dev.orestegabo.sequo.feature.settings

import androidx.compose.runtime.compositionLocalOf

data class ShoppingPreferences(val largerText: Boolean = false, val tapFeedback: Boolean = true)

data class ShoppingPreferencesControl(
    val value: ShoppingPreferences = ShoppingPreferences(),
    val onChange: (ShoppingPreferences) -> Unit = {},
)

val LocalShoppingPreferences = compositionLocalOf { ShoppingPreferencesControl() }
