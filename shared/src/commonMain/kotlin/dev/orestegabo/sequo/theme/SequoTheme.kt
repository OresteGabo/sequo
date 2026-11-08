package dev.orestegabo.sequo.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.orestegabo.sequo.design.SequoLightColors
import dev.orestegabo.sequo.design.SequoDarkColors
import dev.orestegabo.sequo.design.SequoTypography
import dev.orestegabo.sequo.design.SequoThemeShapes

internal val SequoPrimary = SequoLightColors.primary
internal val SequoSecondary = SequoLightColors.secondary
internal val SequoAccent = SequoLightColors.tertiary
internal val SequoSurface = SequoLightColors.surface
internal val SequoBackground = SequoLightColors.background
internal val SequoSurfaceVariant = SequoLightColors.surfaceVariant
internal val SequoOnSurface = SequoLightColors.onSurface
internal val SequoOnSurfaceVariant = SequoLightColors.onSurfaceVariant
internal val SequoOutline = SequoLightColors.outline

@Composable
internal fun SequoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme = if (darkTheme) SequoDarkColors else SequoLightColors,
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = colorScheme, typography = SequoTypography,
        shapes = SequoThemeShapes, content = content)
}

internal val sequoColorScheme: ColorScheme = SequoLightColors

internal data class SequoUiPalette(
    val ambientBottom: Color = SequoSurfaceVariant,
    val ambientLineStrong: Color = SequoPrimary.copy(alpha = 0.13f),
    val ambientLineSoft: Color = SequoAccent.copy(alpha = 0.10f),
    val ambientCirclePrimary: Color = SequoPrimary.copy(alpha = 0.08f),
    val ambientCircleSecondary: Color = SequoAccent.copy(alpha = 0.08f),
    val ambientPanelTop: Color = SequoPrimary.copy(alpha = 0.05f),
    val ambientPanelBottom: Color = SequoAccent.copy(alpha = 0.04f),
    val floatingShell: Color = SequoSurface,
    val floatingShellBorder: Color = SequoOutline.copy(alpha = 0.45f),
)

internal val sequoUi = SequoUiPalette()
