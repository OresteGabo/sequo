package dev.orestegabo.sequo.feature.settings

import androidx.compose.runtime.Composable

interface AppPreferencesStore {
    suspend fun getLanguage(): AppLanguage?
    suspend fun saveLanguage(language: AppLanguage)
    suspend fun getThemePreference(): AppThemePreference?
    suspend fun saveThemePreference(themePreference: AppThemePreference)
}

@Composable
expect fun rememberAppPreferencesStore(): AppPreferencesStore
