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
import dev.orestegabo.sequo.core.auth.CurrentUser
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
internal fun AccountContent(
    currentUser: CurrentUser?,
    isGuest: Boolean,
    onSignInRequested: () -> Unit,
    onLogout: () -> Unit,
) {
    if (isGuest) {
        GuestAccountCard(onSignInRequested = onSignInRequested)
        return
    }

    AccountProfileCard(currentUser = currentUser, onLogout = onLogout)
    SequoStatusStrip(
        icon = Icons.Filled.CheckCircle,
        title = "Subscription active",
        detail = "15% off / 500 CFA credit",
        tag = "15%",
    )
    SequoPassCard()
    SequoSectionCard(title = "Saved places", action = "Lome") {
        AccountAddressRow("Home", "Tokoin Gbadago, near Pharmacie des Etoiles")
        AccountAddressRow("Family", "Adidogome, carrefour Limousine")
        AccountAddressRow("Office", "Be-Kpota, route du marche")
    }
    SequoSectionCard(title = "Supported payments", action = "no cash") {
        SupportedPaymentRow("Yas Togo", "Primary")
        SupportedPaymentRow("Moov Africa", "Backup")
    }
    SequoSectionCard(title = "Account tools", action = "secure") {
        SettingRow("Payments", "Yas / Moov")
        SettingRow("Returns", "72 hours")
        SettingRow("Parrainage", "Delivery credit")
        SettingRow("Subscription", "15% active")
    }
}

@Composable
private fun GuestAccountCard(onSignInRequested: () -> Unit) {
    SequoCard(shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(15.dp),
                )
            }
            Text(
                "Guest browsing",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                "Create an account or sign in when you are ready to save baskets, place orders, track delivery, and manage payments.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onSignInRequested,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Sign in or create account")
            }
        }
    }
}

@Composable
private fun AccountProfileCard(
    currentUser: CurrentUser?,
    onLogout: () -> Unit,
) {
    SequoCard(shape = RoundedCornerShape(24.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        currentUser?.displayName?.firstOrNull()?.uppercase() ?: "S",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    currentUser?.displayName ?: "Sequo customer",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    currentUser?.email ?: "No email loaded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "Provider: ${currentUser?.provider?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "unknown"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sign out",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
