package dev.orestegabo.sequo.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.feature.settings.AppLanguage
import dev.orestegabo.sequo.feature.settings.AppThemePreference
import dev.orestegabo.sequo.feature.settings.appText
import dev.orestegabo.sequo.ui.components.SequoSectionCard
import sequo.shared.generated.resources.*

@Composable
internal fun AppPreferencesCard(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    themePreference: AppThemePreference,
    onThemePreferenceChange: (AppThemePreference) -> Unit,
) {
    SequoSectionCard(title = appText(Res.string.settings_preferences), action = "") {
        PreferenceChoices(
            Icons.Filled.Translate, appText(Res.string.drawer_language), language,
            listOf(AppLanguage.English to appText(Res.string.settings_english), AppLanguage.French to appText(Res.string.settings_french)),
            onLanguageChange,
        )
        PreferenceChoices(
            Icons.Filled.DarkMode, appText(Res.string.drawer_theme), themePreference,
            listOf(
                AppThemePreference.System to appText(Res.string.drawer_theme_system),
                AppThemePreference.Light to appText(Res.string.drawer_theme_light),
                AppThemePreference.Dark to appText(Res.string.drawer_theme_dark),
            ),
            onThemePreferenceChange,
        )
    }
}

@Composable
private fun <T> PreferenceChoices(icon: ImageVector, title: String, selected: T, options: List<Pair<T, String>>, onSelect: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(title, style = MaterialTheme.typography.titleSmall)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, label) ->
                FilterChip(selected = value == selected, onClick = { onSelect(value) }, label = { Text(label) })
            }
        }
    }
}
