package dev.orestegabo.sequo.ui.account

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.auth.CurrentUser
import dev.orestegabo.sequo.core.auth.SavedDeliveryAddress
import dev.orestegabo.sequo.core.catalog.CustomerBenefits
import dev.orestegabo.sequo.core.network.NetworkConfig
import dev.orestegabo.sequo.feature.settings.*
import dev.orestegabo.sequo.feature.legal.LegalInitialTab
import dev.orestegabo.sequo.logic.*
import dev.orestegabo.sequo.ui.components.*
import kotlinx.coroutines.CancellationException
import sequo.shared.generated.resources.*

private enum class AccountDetail { Profile, Addresses, Benefits, About, Data, Delete }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountContent(
    currentUser: CurrentUser?,
    loadSavedAddresses: suspend () -> List<SavedDeliveryAddress>,
    loadBenefits: suspend () -> CustomerBenefits,
    isGuest: Boolean,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    themePreference: AppThemePreference,
    onThemePreferenceChange: (AppThemePreference) -> Unit,
    onOpenLegal: (LegalInitialTab) -> Unit,
    onSignInRequested: () -> Unit,
    onLogout: () -> Unit,
    accountAccessToken: suspend () -> String?,
    exportPreferences: Map<String, String>,
    pendingAdminLoginRequestCount: Int = 0,
    onAdminLoginRequestsClick: () -> Unit = {},
) {
    var detail by remember(currentUser?.id, isGuest) { mutableStateOf<AccountDetail?>(null) }
    val name = if (isGuest) appText(Res.string.account_guest_title)
        else currentUser?.displayName ?: appText(Res.string.account_customer)
    Surface(
        onClick = { if (isGuest) onSignInRequested() else detail = AccountDetail.Profile },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .45f),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isGuest) Icon(Icons.Filled.PersonOutline, null, tint = MaterialTheme.colorScheme.onPrimary)
                        else Text(name.firstOrNull { it.isLetter() }?.uppercase() ?: "S",
                            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(if (isGuest) appText(Res.string.settings_guest_hint) else appText(Res.string.settings_view_profile),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            if (isGuest) Button(onClick = onSignInRequested, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp)) { Text(appText(Res.string.account_sign_in_create)) }
        }
    }
    AppPreferencesCard(language, onLanguageChange, themePreference, onThemePreferenceChange)
    if (!isGuest) SettingsGroup(appText(Res.string.settings_your_account)) {
        SettingsRow(Icons.Filled.LocationOn, appText(Res.string.account_saved_places),
            subtitle = appText(Res.string.settings_addresses_hint), onClick = { detail = AccountDetail.Addresses })
        SettingsDivider()
        SettingsRow(Icons.Filled.LocalOffer, appText(Res.string.benefits_title),
            subtitle = appText(Res.string.settings_benefits_hint), onClick = { detail = AccountDetail.Benefits })
        if (pendingAdminLoginRequestCount > 0) {
            SettingsDivider()
            SettingsRow(Icons.Filled.Security, appText(Res.string.admin_login_title),
                value = pendingAdminLoginRequestCount.toString(), onClick = onAdminLoginRequestsClick)
        }
    }
    if (!isGuest) SettingsGroup(appText(Res.string.settings_data_privacy)) {
        SettingsRow(Icons.Filled.Download, appText(Res.string.settings_download_data),
            subtitle = appText(Res.string.settings_download_hint), onClick = { detail = AccountDetail.Data })
        SettingsDivider()
        SettingsRow(Icons.Filled.PersonRemove, appText(Res.string.settings_delete_account),
            destructive = true, onClick = { detail = AccountDetail.Delete })
    }
    SettingsGroup(appText(Res.string.settings_legal)) {
        SettingsRow(Icons.Filled.Info, appText(Res.string.settings_about), onClick = { detail = AccountDetail.About })
        SettingsDivider()
        SettingsRow(Icons.Filled.PrivacyTip, appText(Res.string.settings_privacy),
            onClick = { onOpenLegal(LegalInitialTab.Privacy) })
        SettingsDivider()
        SettingsRow(Icons.Filled.Description, appText(Res.string.settings_terms),
            onClick = { onOpenLegal(LegalInitialTab.Terms) })
    }
    if (!isGuest) {
        SettingsGroup {
            SettingsRow(Icons.AutoMirrored.Filled.Logout, appText(Res.string.account_sign_out),
                destructive = true, onClick = onLogout)
        }
    }
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text("SEQUO", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (NetworkConfig.Environment != "PRODUCTION") Text("${NetworkConfig.Environment} · ${NetworkConfig.ReleaseId}",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    detail?.let { page ->
        ModalBottomSheet(onDismissRequest = { detail = null }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)
                .padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(appText(when (page) {
                        AccountDetail.Profile -> Res.string.settings_profile
                        AccountDetail.Addresses -> Res.string.account_saved_places
                        AccountDetail.Benefits -> Res.string.benefits_title
                        AccountDetail.About -> Res.string.settings_about
                        AccountDetail.Data -> Res.string.settings_download_data
                        AccountDetail.Delete -> Res.string.settings_delete_account
                    }), modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { detail = null }) { Icon(Icons.Filled.Close, appText(Res.string.settings_close)) }
                }
                when (page) {
                    AccountDetail.Profile -> {
                        ProfileField(appText(Res.string.settings_name), name)
                        ProfileField(appText(Res.string.settings_email), currentUser?.email ?: appText(Res.string.account_no_email))
                        ProfileField(appText(Res.string.account_provider),
                            currentUser?.provider?.lowercase()?.replaceFirstChar { it.uppercase() } ?: appText(Res.string.account_unknown))
                    }
                    AccountDetail.Addresses -> SavedAddressesCard(currentUser?.id, loadSavedAddresses)
                    AccountDetail.Benefits -> DeliveryBenefitsCard(currentUser?.id, loadBenefits)
                    AccountDetail.About -> AboutSequoContent(onOpenLegal)
                    AccountDetail.Data, AccountDetail.Delete -> key(currentUser?.id, page) {
                        AccountDataContent(accountAccessToken, exportPreferences, deletion = page == AccountDetail.Delete)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val result = state.value
        when {
            result == null -> Text(appText(Res.string.account_addresses_loading))
            result.isFailure -> {
                Text(appText(Res.string.account_addresses_error))
                TextButton(onClick = { reload++ }) { Text(appText(Res.string.account_addresses_retry)) }
            }
            else -> {
                val addresses = result.getOrThrow()
                if (addresses.isEmpty()) SequoEmptyState(
                    visual = EmptyStateVisual.Addresses,
                    title = appText(Res.string.empty_addresses_title), detail = appText(Res.string.empty_addresses_detail),
                    compact = true,
                )
                addresses.forEach { address ->
                    AccountAddressRow(address.label, listOfNotNull(address.streetHint, address.area, address.city).joinToString(", "))
                }
            }
        }
    }
}
