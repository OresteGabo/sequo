package dev.orestegabo.sequo.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Translate
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
