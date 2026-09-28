package dev.orestegabo.sequo.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp

internal val SequoPrimary = Color(0xFF0B6B46)
internal val SequoSecondary = Color(0xFF087F4F)
internal val SequoAccent = Color(0xFF8A6200)
internal val SequoSurface = Color(0xFFFFFFFF)
internal val SequoBackground = Color(0xFFF7F8F4)
internal val SequoSurfaceVariant = Color(0xFFDDE6DF)
internal val SequoOnSurface = Color(0xFF0F1720)
internal val SequoOnSurfaceVariant = Color(0xFF3F4A44)
internal val SequoOutline = Color(0xFF6F7B73)

private val LightSequoColorScheme = lightColorScheme(
    primary = SequoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEFD9),
    onPrimaryContainer = Color(0xFF002C1C),
    secondary = SequoSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC4F3D8),
    onSecondaryContainer = Color(0xFF003821),
    tertiary = SequoAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE3A1),
    onTertiaryContainer = Color(0xFF2A1B00),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = SequoBackground,
    onBackground = SequoOnSurface,
    surface = SequoSurface,
    onSurface = SequoOnSurface,
    surfaceVariant = SequoSurfaceVariant,
    onSurfaceVariant = SequoOnSurfaceVariant,
    outline = SequoOutline,
    outlineVariant = Color(0xFFC5D0C8),
    scrim = Color.Black,
    inverseSurface = Color(0xFF25312B),
    inverseOnSurface = Color(0xFFF0F6EF),
    inversePrimary = Color(0xFF7AD8A9),
    surfaceDim = Color(0xFFD8DED8),
    surfaceBright = Color(0xFFFAFBF8),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF1F5F0),
    surfaceContainer = Color(0xFFEBF0EA),
    surfaceContainerHigh = Color(0xFFE5EBE4),
    surfaceContainerHighest = Color(0xFFDFE5DE),
)

private val DarkSequoColorScheme = darkColorScheme(
    primary = Color(0xFF7AD8A9),
    onPrimary = Color(0xFF003823),
    primaryContainer = Color(0xFF005234),
    onPrimaryContainer = Color(0xFFBFF4D7),
    secondary = Color(0xFF65D99D),
    onSecondary = Color(0xFF00391F),
    secondaryContainer = Color(0xFF0B5F3A),
    onSecondaryContainer = Color(0xFFC5F4D8),
    tertiary = Color(0xFFF7C74E),
    onTertiary = Color(0xFF3D2D00),
    tertiaryContainer = Color(0xFF654700),
    onTertiaryContainer = Color(0xFFFFE4A6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0B1110),
    onBackground = Color(0xFFE4ECE6),
    surface = Color(0xFF101917),
    onSurface = Color(0xFFE4ECE6),
    surfaceVariant = Color(0xFF3E4A44),
    onSurfaceVariant = Color(0xFFC4D0C8),
    outline = Color(0xFF89978E),
    outlineVariant = Color(0xFF3D4A43),
    scrim = Color.Black,
    inverseSurface = Color(0xFFE4ECE6),
    inverseOnSurface = Color(0xFF101917),
    inversePrimary = SequoPrimary,
    surfaceDim = Color(0xFF0B1110),
    surfaceBright = Color(0xFF303B36),
    surfaceContainerLowest = Color(0xFF060B0A),
    surfaceContainerLow = Color(0xFF121D1A),
    surfaceContainer = Color(0xFF17231F),
    surfaceContainerHigh = Color(0xFF21302A),
    surfaceContainerHighest = Color(0xFF2B3A34),
)

private val SequoTypography = Typography().let { baseline ->
    baseline.copy(
        headlineLarge = baseline.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
        headlineMedium = baseline.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
        headlineSmall = baseline.headlineSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        titleLarge = baseline.titleLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        titleMedium = baseline.titleMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        titleSmall = baseline.titleSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        bodyLarge = baseline.bodyLarge.copy(letterSpacing = 0.sp),
        bodyMedium = baseline.bodyMedium.copy(letterSpacing = 0.sp),
        bodySmall = baseline.bodySmall.copy(letterSpacing = 0.sp),
        labelLarge = baseline.labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        labelMedium = baseline.labelMedium.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.sp),
        labelSmall = baseline.labelSmall.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.sp),
    )
}

private val SequoShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(18.dp),
)

@Composable
internal fun SequoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme = if (darkTheme) DarkSequoColorScheme else LightSequoColorScheme,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SequoTypography,
        shapes = SequoShapes,
        content = content,
    )
}

internal val sequoColorScheme: ColorScheme = LightSequoColorScheme

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
