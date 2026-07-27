package dev.orestegabo.sequo.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.data.*
import dev.orestegabo.sequo.domain.*
import dev.orestegabo.sequo.logic.*
import dev.orestegabo.sequo.model.*
import dev.orestegabo.sequo.theme.*
import dev.orestegabo.sequo.ui.account.*
import dev.orestegabo.sequo.ui.app.*
import dev.orestegabo.sequo.ui.basket.*
import dev.orestegabo.sequo.ui.catalog.*
import dev.orestegabo.sequo.ui.chrome.*
import dev.orestegabo.sequo.ui.components.*
import dev.orestegabo.sequo.ui.home.*
import dev.orestegabo.sequo.ui.markets.*
import dev.orestegabo.sequo.ui.orders.*
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.*

internal val SequoPrimary = Color(0xFF2F6970)
internal val SequoSecondary = Color(0xFFB4874F)
internal val SequoAccent = Color(0xFFD8C6A3)
internal val SequoSurface = Color(0xFFFCF8F1)
internal val SequoBackground = Color(0xFFF3EEE5)
internal val SequoSurfaceVariant = Color(0xFFF0EAE0)
internal val SequoOnSurface = Color(0xFF1A1712)
internal val SequoOnSurfaceVariant = Color(0xFF5E5A52)
internal val SequoOutline = Color(0xFFD4CABB)

internal val sequoColorScheme = lightColorScheme(
    primary = SequoPrimary,
    onPrimary = Color.White,
    secondary = SequoSecondary,
    onSecondary = Color.White,
    tertiary = SequoAccent,
    onTertiary = SequoOnSurface,
    surface = SequoSurface,
    onSurface = SequoOnSurface,
    background = SequoBackground,
    onBackground = SequoOnSurface,
    primaryContainer = SequoPrimary.copy(alpha = 0.12f),
    onPrimaryContainer = SequoPrimary,
    secondaryContainer = SequoSecondary.copy(alpha = 0.14f),
    onSecondaryContainer = Color(0xFF3A2811),
    tertiaryContainer = SequoAccent.copy(alpha = 0.20f),
    onTertiaryContainer = Color(0xFF4E3F21),
    surfaceVariant = SequoSurfaceVariant,
    onSurfaceVariant = SequoOnSurfaceVariant,
    outline = SequoOutline,
)

internal data class SequoUiPalette(
    val ambientBottom: Color = SequoSurfaceVariant,
    val ambientLineStrong: Color = SequoPrimary.copy(alpha = 0.13f),
    val ambientLineSoft: Color = SequoAccent.copy(alpha = 0.10f),
    val ambientCirclePrimary: Color = SequoPrimary.copy(alpha = 0.08f),
    val ambientCircleSecondary: Color = SequoAccent.copy(alpha = 0.07f),
    val ambientPanelTop: Color = SequoPrimary.copy(alpha = 0.035f),
    val ambientPanelBottom: Color = SequoAccent.copy(alpha = 0.028f),
    val floatingShell: Color = SequoSurface,
    val floatingShellBorder: Color = SequoSecondary.copy(alpha = 0.20f),
)

internal val sequoUi = SequoUiPalette()
