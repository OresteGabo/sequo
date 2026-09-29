package dev.orestegabo.sequo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.theme.SequoPrimary
import dev.orestegabo.sequo.theme.SequoSecondary
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.Res
import sequo.shared.generated.resources.sequohub_logo_mark

internal enum class SequoErrorKind {
    Offline,
    Server,
    Payment,
    SessionExpired,
    Location,
    OrderSync,
    Unknown,
}

internal data class SequoErrorAction(
    val label: String,
    val onClick: () -> Unit,
)

@Composable
internal fun SequoErrorPage(
    kind: SequoErrorKind,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    secondaryAction: SequoErrorAction? = null,
    technicalNote: String? = null,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        SequoErrorPanel(
            kind = kind,
            onRetry = onRetry,
            secondaryAction = secondaryAction,
            technicalNote = technicalNote,
        )
    }
}

@Composable
internal fun SequoErrorPanel(
    kind: SequoErrorKind,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    secondaryAction: SequoErrorAction? = null,
    technicalNote: String? = null,
) {
    val content = errorContent(kind)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.50f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f)),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(Res.drawable.sequohub_logo_mark),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(132.dp)
                    .alpha(0.055f),
            )
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = content.accent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, content.accent.copy(alpha = 0.20f)),
                ) {
                    Icon(
                        imageVector = content.icon,
                        contentDescription = null,
                        tint = content.accent,
                        modifier = Modifier.padding(14.dp),
                    )
                }
                Text(
                    content.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    content.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                technicalNote?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    secondaryAction?.let { action ->
                        ErrorActionButton(
                            label = action.label,
                            onClick = action.onClick,
                            accent = content.accent,
                            emphasized = false,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    ErrorActionButton(
                        label = content.primaryAction,
                        onClick = onRetry,
                        accent = content.accent,
                        emphasized = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
internal fun SequoErrorBanner(
    kind: SequoErrorKind,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
) {
    val content = errorContent(kind)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = content.accent.copy(alpha = 0.09f),
        border = BorderStroke(1.dp, content.accent.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(content.icon, contentDescription = null, tint = content.accent, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    content.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    content.shortDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                onClick = onRetry,
                shape = RoundedCornerShape(999.dp),
                color = content.accent.copy(alpha = 0.12f),
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = content.primaryAction,
                    tint = content.accent,
                    modifier = Modifier.padding(8.dp).size(17.dp),
                )
            }
        }
    }
}

@Composable
private fun ErrorActionButton(
    label: String,
    onClick: () -> Unit,
    accent: Color,
    emphasized: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (emphasized) accent else MaterialTheme.colorScheme.surface,
        border = if (emphasized) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (emphasized) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class ErrorStateContent(
    val icon: ImageVector,
    val title: String,
    val detail: String,
    val shortDetail: String,
    val primaryAction: String,
    val accent: Color,
)

@Composable
private fun errorContent(kind: SequoErrorKind): ErrorStateContent =
    when (kind) {
        SequoErrorKind.Offline -> ErrorStateContent(
            icon = Icons.Filled.CloudOff,
            title = "No internet connection",
            detail = "Sequo could not refresh this page. Check your connection, then try again. Already loaded orders can still appear from local state.",
            shortDetail = "Check connection and retry.",
            primaryAction = "Try again",
            accent = SequoPrimary,
        )
        SequoErrorKind.Server -> ErrorStateContent(
            icon = Icons.Filled.SyncProblem,
            title = "Sequo is taking longer than usual",
            detail = "The server did not answer correctly. Your order and payment data stay protected; retry in a moment.",
            shortDetail = "Server did not answer correctly.",
            primaryAction = "Retry",
            accent = Color(0xFF6F5EA8),
        )
        SequoErrorKind.Payment -> ErrorStateContent(
            icon = Icons.Filled.Payments,
            title = "Payment was not completed",
            detail = "No order should be confirmed until the payment provider verifies it. You can retry or choose another payment method.",
            shortDetail = "Payment provider did not confirm.",
            primaryAction = "Retry payment",
            accent = Color(0xFF8A6A3F),
        )
        SequoErrorKind.SessionExpired -> ErrorStateContent(
            icon = Icons.Filled.Lock,
            title = "Session expired",
            detail = "For account security, sign in again before viewing orders, pickup codes, payments, or returns.",
            shortDetail = "Sign in again to continue.",
            primaryAction = "Sign in",
            accent = SequoSecondary,
        )
        SequoErrorKind.Location -> ErrorStateContent(
            icon = Icons.Filled.LocationOff,
            title = "Delivery area unavailable",
            detail = "We could not confirm your delivery area. You can retry location, or choose a saved address from your account.",
            shortDetail = "Choose or refresh delivery area.",
            primaryAction = "Retry location",
            accent = Color(0xFF5F7C44),
        )
        SequoErrorKind.OrderSync -> ErrorStateContent(
            icon = Icons.Filled.Inventory2,
            title = "Order status is not synced",
            detail = "Some package, pickup, or bargaining details may be stale. Refresh before paying, accepting an offer, or showing a pickup code.",
            shortDetail = "Refresh before important actions.",
            primaryAction = "Refresh",
            accent = Color(0xFF3C6E91),
        )
        SequoErrorKind.Unknown -> ErrorStateContent(
            icon = Icons.Filled.Warning,
            title = "Something went wrong",
            detail = "This part of Sequo could not load. Try again, or contact support if it keeps happening.",
            shortDetail = "Try again or contact support.",
            primaryAction = "Try again",
            accent = MaterialTheme.colorScheme.error,
        )
    }
