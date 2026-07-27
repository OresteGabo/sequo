package dev.orestegabo.sequo.ui.account

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

@Composable
internal fun AccountContent() {
    SequoAppBar(
        title = "Afi K.",
        subtitle = "Subscriber profile",
        leadingIcon = Icons.Filled.Person,
        actions = listOf(
            AppBarAction(Icons.Filled.Settings, "Account settings", emphasized = true),
            AppBarAction(Icons.Filled.Notifications, "Notification settings"),
        ),
    )
    SequoStatusStrip(
        icon = Icons.Filled.CheckCircle,
        title = "Subscription active",
        detail = "15% delivery-fee discount plus 500 CFA parrainage credit.",
        tag = "15%",
    )
    SequoPassCard()
    SequoSectionCard(title = "Saved places", action = "Lome") {
        AccountAddressRow("Home", "Tokoin Gbadago, near Pharmacie des Etoiles")
        AccountAddressRow("Family", "Adidogome, carrefour Limousine")
        AccountAddressRow("Office", "Be-Kpota, route du marche")
    }
    SequoSectionCard(title = "Supported payments", action = "no cash") {
        SupportedPaymentRow("Yas Togo", "Primary mobile money validation before order completion")
        SupportedPaymentRow("Moov Africa", "Backup mobile money validation for checkout")
    }
    SequoSectionCard(title = "Account tools", action = "secure") {
        SettingRow("Payments", "Yas Togo and Moov Africa validation")
        SettingRow("Returns", "Point de Relai drop-off within 72 hours")
        SettingRow("Parrainage", "Delivery credit, never cash payout")
        SettingRow("Subscription", "15% delivery fee discount active")
    }
}
