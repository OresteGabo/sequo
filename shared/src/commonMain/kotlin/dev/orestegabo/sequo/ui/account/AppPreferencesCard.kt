package dev.orestegabo.sequo.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.feature.settings.*
import sequo.shared.generated.resources.*

@Composable
internal fun AppPreferencesCard(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    themePreference: AppThemePreference,
    onThemePreferenceChange: (AppThemePreference) -> Unit,
) {
    val shopping = LocalShoppingPreferences.current
    val openNotificationSettings = rememberOpenNotificationSettings()
    var textSizeOpen by remember { mutableStateOf(false) }
    var notificationSettingsFailed by remember { mutableStateOf(false) }
    var languageOpen by remember { mutableStateOf(false) }
    var themeOpen by remember { mutableStateOf(false) }
    val languages = listOf(AppLanguage.English to appText(Res.string.settings_english),
        AppLanguage.French to appText(Res.string.settings_french))
    val themes = listOf(AppThemePreference.System to appText(Res.string.drawer_theme_system),
        AppThemePreference.Light to appText(Res.string.drawer_theme_light),
        AppThemePreference.Dark to appText(Res.string.drawer_theme_dark))
    SettingsGroup(appText(Res.string.settings_preferences)) {
        SettingsRow(Icons.Filled.Translate, appText(Res.string.drawer_language),
            value = languages.first { it.first == language }.second, onClick = { languageOpen = true })
        SettingsDivider()
        SettingsRow(Icons.Filled.DarkMode, appText(Res.string.settings_appearance),
            value = themes.first { it.first == themePreference }.second, onClick = { themeOpen = true })
    }
    SettingsGroup(appText(Res.string.settings_comfort_alerts)) {
        SettingsRow(Icons.Filled.TextFields, appText(Res.string.settings_text_size),
            value = appText(if (shopping.value.largerText) Res.string.settings_text_larger else Res.string.settings_text_device),
            subtitle = appText(Res.string.settings_text_size_hint), onClick = { textSizeOpen = true })
        SettingsDivider()
        SettingsToggleRow(Icons.Filled.Vibration, appText(Res.string.settings_tap_feedback),
            appText(Res.string.settings_tap_feedback_hint), shopping.value.tapFeedback,
            onChange = { shopping.onChange(shopping.value.copy(tapFeedback = it)) })
        SettingsDivider()
        SettingsRow(Icons.Filled.NotificationsNone, appText(Res.string.settings_phone_notifications),
            subtitle = appText(Res.string.settings_phone_notifications_hint),
            onClick = { notificationSettingsFailed = !openNotificationSettings() })
    }
    if (notificationSettingsFailed) Text(appText(Res.string.settings_phone_notifications_failed),
        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    if (textSizeOpen) PreferenceDialog(appText(Res.string.settings_text_size), shopping.value.largerText,
        listOf(false to appText(Res.string.settings_text_device), true to appText(Res.string.settings_text_larger)),
        onDismiss = { textSizeOpen = false }, onSelect = { shopping.onChange(shopping.value.copy(largerText = it)); textSizeOpen = false })
    if (languageOpen) PreferenceDialog(appText(Res.string.drawer_language), language, languages,
        onDismiss = { languageOpen = false }, onSelect = { onLanguageChange(it); languageOpen = false })
    if (themeOpen) PreferenceDialog(appText(Res.string.settings_appearance), themePreference, themes,
        onDismiss = { themeOpen = false }, onSelect = { onThemePreferenceChange(it); themeOpen = false })
}

@Composable
private fun <T> PreferenceDialog(title: String, selected: T, options: List<Pair<T, String>>,
    onDismiss: () -> Unit, onSelect: (T) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) },
        text = {
            Column(Modifier.fillMaxWidth().selectableGroup()) {
                options.forEach { (value, label) ->
                    Row(Modifier.fillMaxWidth().selectable(selected = value == selected, role = Role.RadioButton,
                        onClick = { onSelect(value) }).padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = value == selected, onClick = null)
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(appText(Res.string.settings_close)) } })
}
