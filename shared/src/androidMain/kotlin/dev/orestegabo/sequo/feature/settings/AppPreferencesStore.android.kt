package dev.orestegabo.sequo.feature.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberAppPreferencesStore(): AppPreferencesStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidAppPreferencesStore(context) }
}

private class AndroidAppPreferencesStore(context: Context) : AppPreferencesStore {
    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    override suspend fun getShoppingPreferences() = ShoppingPreferences(
        largerText = preferences.getBoolean("larger_text", false),
        tapFeedback = preferences.getBoolean("tap_feedback", true),
    )

    override suspend fun saveShoppingPreferences(preferences: ShoppingPreferences) {
        this.preferences.edit().putBoolean("larger_text", preferences.largerText)
            .putBoolean("tap_feedback", preferences.tapFeedback).apply()
    }

    override suspend fun getLanguage(): AppLanguage? =
        preferences.getString(LanguageKey, null)?.let { value ->
            runCatching { AppLanguage.valueOf(value) }.getOrNull()
        }

    override suspend fun saveLanguage(language: AppLanguage) {
        preferences.edit().putString(LanguageKey, language.name).apply()
    }

    override suspend fun getThemePreference(): AppThemePreference? =
        preferences.getString(ThemeKey, null)?.let { value ->
            runCatching { AppThemePreference.valueOf(value) }.getOrNull()
        }

    override suspend fun saveThemePreference(themePreference: AppThemePreference) {
        preferences.edit().putString(ThemeKey, themePreference.name).apply()
    }

    private companion object {
        const val PreferencesName = "sequo_app_preferences"
        const val LanguageKey = "language"
        const val ThemeKey = "theme"
    }
}
