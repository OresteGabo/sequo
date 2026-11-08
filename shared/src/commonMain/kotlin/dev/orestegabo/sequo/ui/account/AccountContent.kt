package dev.orestegabo.sequo.ui.account

import dev.orestegabo.sequo.core.network.NetworkConfig

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
import dev.orestegabo.sequo.core.catalog.CustomerBenefits
import dev.orestegabo.sequo.core.auth.CurrentUser
import dev.orestegabo.sequo.core.auth.SavedDeliveryAddress
import kotlinx.coroutines.CancellationException
import dev.orestegabo.sequo.feature.settings.appText
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
    loadSavedAddresses: suspend () -> List<SavedDeliveryAddress>,
    loadBenefits: suspend () -> CustomerBenefits,
    isGuest: Boolean,
    onSignInRequested: () -> Unit,
    onLogout: () -> Unit,
    pendingAdminLoginRequestCount: Int = 0,
    onAdminLoginRequestsClick: () -> Unit = {},
) {
    if (isGuest) {
        GuestAccountCard(onSignInRequested = onSignInRequested)
        return
    }

    AccountProfileCard(currentUser = currentUser, onLogout = onLogout)
    DeliveryBenefitsCard(currentUser?.id, loadBenefits)
    SavedAddressesCard(currentUser?.id, loadSavedAddresses)
    SequoSectionCard(title = appText(Res.string.account_supported_payments), action = appText(Res.string.account_no_cash)) {
        SupportedPaymentRow("Yas Togo", appText(Res.string.account_primary))
        SupportedPaymentRow("Moov Africa", appText(Res.string.account_backup))
    }
    SequoSectionCard(title = appText(Res.string.account_tools), action = appText(Res.string.account_secure)) {
        if (pendingAdminLoginRequestCount > 0) {
            SettingRow(appText(Res.string.admin_login_title), "$pendingAdminLoginRequestCount")
            Button(
                onClick = onAdminLoginRequestsClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(appText(Res.string.admin_login_refresh))
            }
        }
        SettingRow(appText(Res.string.account_payments), "Yas / Moov")
        SettingRow(appText(Res.string.account_returns), appText(Res.string.account_return_eligibility))
    }
    if (NetworkConfig.Environment != "PRODUCTION") {
        Text("${NetworkConfig.Environment} · ${NetworkConfig.ReleaseId}",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DeliveryBenefitsCard(userId: String?, load: suspend () -> CustomerBenefits) {
    var refresh by remember(userId) { mutableStateOf(0) }
    var benefits by remember(userId) { mutableStateOf<CustomerBenefits?>(null) }
    var loading by remember(userId) { mutableStateOf(true) }
    var failed by remember(userId) { mutableStateOf(false) }
    LaunchedEffect(userId, refresh) {
        benefits = null
        loading = true
        failed = false
        try { benefits = load() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { failed = true }
        finally { loading = false }
    }
    SequoSectionCard(title = appText(Res.string.benefits_title), action = "") {
        TextButton(onClick = { refresh++ }, enabled = !loading) { Text(appText(Res.string.cart_refresh)) }
        when {
            loading -> Text(appText(Res.string.benefits_loading))
            failed -> Text(appText(Res.string.benefits_error), color = MaterialTheme.colorScheme.error)
            else -> benefits?.let { actual ->
                val active = actual.subscriptionActive && actual.status == "ACTIVE"
                Text(appText(when (actual.status) {
                    "ACTIVE" -> if (active) Res.string.benefits_active else Res.string.benefits_unapproved
                    "PENDING_VERIFICATION" -> Res.string.benefits_pending_verification
                    "EXPIRED" -> Res.string.benefits_expired
                    else -> Res.string.benefits_unapproved
                }))
                if (active) {
                    actual.planName?.takeIf { it.isNotBlank() }?.let { Text(it) }
                    actual.monthlyPriceCfa?.let { Text(appText(Res.string.benefits_monthly_price) + " · " + formatCfa(it)) }
                    Text(appText(Res.string.benefits_subscription_discount) + " · ${actual.deliveryDiscountPercent}%")
                    if (actual.loyaltyEnabled) Text(appText(Res.string.benefits_loyalty_note))
                }
                Text(appText(Res.string.benefits_available_credit) + " · " + formatCfa(actual.availableReferralCreditCfa))
                Text(appText(Res.string.benefits_credit_note), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SavedAddressesCard(userId: String?, load: suspend () -> List<SavedDeliveryAddress>) {
    var reload by remember(userId) { mutableStateOf(0) }
    val state = remember(userId, reload) { mutableStateOf<Result<List<SavedDeliveryAddress>>?>(null) }
    LaunchedEffect(userId, reload) {
        try {
            state.value = Result.success(load())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            state.value = Result.failure(error)
        }
    }
    SequoSectionCard(title = appText(Res.string.account_saved_places), action = "") {
        val result = state.value
        when {
            result == null -> Text(appText(Res.string.account_addresses_loading))
            result.isFailure -> {
                Text(appText(Res.string.account_addresses_error))
                TextButton(onClick = { reload++ }) { Text(appText(Res.string.account_addresses_retry)) }
            }
            else -> {
                val addresses = result.getOrThrow()
                if (addresses.isEmpty()) Text(appText(Res.string.account_addresses_empty))
                addresses.forEach { address ->
                    AccountAddressRow(address.label, listOfNotNull(address.streetHint, address.area, address.city).joinToString(", "))
                }
            }
        }
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
                appText(Res.string.account_guest_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                appText(Res.string.account_guest_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onSignInRequested,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(appText(Res.string.account_sign_in_create))
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
                        currentUser?.displayName?.firstOrNull { it.isLetter() }?.uppercase() ?: "S",
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
                    currentUser?.displayName ?: appText(Res.string.account_customer),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    currentUser?.email ?: appText(Res.string.account_no_email),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${appText(Res.string.account_provider)}: ${currentUser?.provider?.lowercase()?.replaceFirstChar { it.uppercase() } ?: appText(Res.string.account_unknown)}",
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
                    contentDescription = appText(Res.string.account_sign_out),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
