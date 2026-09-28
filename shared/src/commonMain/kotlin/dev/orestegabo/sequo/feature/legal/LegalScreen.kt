package dev.orestegabo.sequo.feature.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.designsystem.component.SequoShapes
import dev.orestegabo.sequo.core.platform.rememberLegalPdfDownloader

@Composable
fun LegalScreen(
    onBack: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(LegalTab.Privacy) }
    var downloadMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedDocument = selectedTab.document
    val pdfDownloader = rememberLegalPdfDownloader()
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .safeContentPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                onClick = onBack,
                shape = SequoShapes.Card,
                color = colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.onPrimary,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Privacy & Terms",
                    color = colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Togo jurisdiction - draft for legal review",
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        LegalTabs(
            selectedTab = selectedTab,
            onTabSelected = {
                selectedTab = it
                downloadMessage = null
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = selectedDocument.title,
                color = colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = selectedDocument.summary,
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )

            selectedDocument.sections.forEach { section ->
                LegalSectionView(section = section)
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        downloadMessage?.let { message ->
            Text(
                text = message,
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            onClick = {
                val result = pdfDownloader.savePdf(
                    fileName = selectedDocument.fileName,
                    title = selectedDocument.title,
                    body = selectedDocument.toPlainText(),
                )
                downloadMessage = result.message
            },
            shape = SequoShapes.Small,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Download PDF",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun LegalTabs(
    selectedTab: LegalTab,
    onTabSelected: (LegalTab) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = SequoShapes.NavItem,
        color = colorScheme.surface.copy(alpha = 0.82f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = colorScheme.outlineVariant.copy(alpha = 0.58f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegalTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    onClick = { onTabSelected(tab) },
                    shape = SequoShapes.IconCapsule,
                    color = if (selected) colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegalSectionView(section: LegalSection) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = section.title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        section.paragraphs.forEach { paragraph ->
            Text(
                text = paragraph,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private enum class LegalTab(
    val label: String,
    val document: LegalDocument,
) {
    Privacy("Privacy", privacyPolicyDocument),
    Terms("Terms", termsDocument),
}

private data class LegalDocument(
    val title: String,
    val fileName: String,
    val summary: String,
    val sections: List<LegalSection>,
) {
    fun toPlainText(): String =
        buildString {
            appendLine(title)
            appendLine()
            appendLine(summary)
            sections.forEach { section ->
                appendLine()
                appendLine(section.title)
                section.paragraphs.forEach { paragraph ->
                    appendLine(paragraph)
                }
            }
        }
}

private data class LegalSection(
    val title: String,
    val paragraphs: List<String>,
)

private val privacyPolicyDocument = LegalDocument(
    title = "Sequo Privacy Policy",
    fileName = "Sequo_Privacy_Policy_Togo.pdf",
    summary = "This policy explains how Sequo handles personal and operational data for relay point operations in Togo. It is a product draft and should be reviewed by counsel before production publication.",
    sections = listOf(
        LegalSection(
            title = "1. Controller and scope",
            paragraphs = listOf(
                "Sequo is the mobile operating app used by Sequo relay point partner shops to receive, store, release, and return packages. This policy applies to hub staff, hub managers, Sequo collection agents, customers who interact with the hub, and support/admin users whose actions are recorded through the Sequo platform.",
                "Processing is governed by applicable law in the Republic of Togo, including Togolese rules on personal data protection, electronic transactions, cybersecurity, and related regulations. If a stronger contractual or regulatory rule applies to a specific service, that stricter rule controls.",
            ),
        ),
        LegalSection(
            title = "2. Data we process",
            paragraphs = listOf(
                "Account and staff data: name, email address, authentication provider, role, assigned hub, session metadata, language preference, device notification settings, and support/account-deletion requests.",
                "Hub operations data: hub ID, locker ID, package ID, return ID, collection batch ID, QR or numeric validation status, timestamps, workflow state, staff user, condition flags, storage age, fee status, support holds, incidents, and audit records.",
                "Customer verification data: the app records only the result of identity verification and, when policy requires it, the ID type and masked reference. Full ID images should not be stored unless a compliance policy explicitly authorizes it.",
                "Technical data: app family, device notification token, app/session identifiers, network and security events, rate-limit signals, and crash or diagnostic information needed to protect the service.",
            ),
        ),
        LegalSection(
            title = "3. Why we process data",
            paragraphs = listOf(
                "We process data to authenticate users, operate hub locker workflows, validate QR and numeric codes, prevent unauthorized package release, verify customer identity at pickup or return drop-off, calculate package age and storage fee status from backend rules, synchronize offline-safe actions, and maintain audit trails.",
                "We also process data to notify customers and staff, investigate incidents, detect fraud or abuse, enforce Sequo operational restrictions, support account deletion, and comply with legal or regulatory obligations.",
            ),
        ),
        LegalSection(
            title = "4. Security and minimization",
            paragraphs = listOf(
                "QR tokens and numeric codes are treated as single-use validation credentials. The mobile app must not expose raw secrets, delivery PINs, full IDs, private customer data, access tokens, refresh tokens, passwords, FCM tokens, or webhook signatures in logs or UI.",
                "Backend state is authoritative. Offline actions may be queued only when safe and must sync with idempotency keys. Fee-due or restricted package releases must not rely on offline-only validation unless the backend has provided a recent signed validation token or Support creates an audited override.",
                "Authentication tokens must be stored in platform secure storage. The app should use provider login for Gmail/Google and iCloud/Apple email domains where configured, and backend controls must enforce the same rule.",
            ),
        ),
        LegalSection(
            title = "5. Sharing and processors",
            paragraphs = listOf(
                "Data may be shared with authorized Sequo operations, Support/Admin users, hub partner managers, delivery or collection agents, notification providers, payment or wallet providers, hosting and security providers, and legal authorities when required by law.",
                "Hub partner staff only receive the minimum information needed for the next safe counter action. They must not open sealed packages, approve refunds, reject returns for product reasons, or override Sequo restrictions unless Support creates an audited exception.",
            ),
        ),
        LegalSection(
            title = "6. Retention",
            paragraphs = listOf(
                "Operational records are kept only as long as needed for package custody, disputes, fee reconciliation, support investigations, security, audit, tax/accounting, and legal compliance. Account deletion requests are handled through Sequo account-deletion workflows and may not remove records that must be retained for lawful operational or audit reasons.",
                "Returned item records, storage-fee records, incident records, and collection handoff records may be retained after a workflow closes to prove custody and resolve disputes.",
            ),
        ),
        LegalSection(
            title = "7. Your choices and rights",
            paragraphs = listOf(
                "Depending on applicable Togolese law and the user role, a person may request access, correction, deletion, restriction, objection, or information about personal data processing. Requests should be sent through Sequo Support or the account-deletion request flow when available.",
                "Users may manage app language, notification preferences, and device notification registration where the app exposes these controls. Some operational notifications, such as pickup codes, deadlines, fee notices, or security alerts, may still be required for service operation.",
            ),
        ),
    ),
)

private val termsDocument = LegalDocument(
    title = "Sequo Terms of Use",
    fileName = "Sequo_Terms_of_Use_Togo.pdf",
    summary = "These terms describe acceptable use of Sequo by relay point partner staff and managers in Togo. They are a product draft and should be reviewed by counsel before production publication.",
    sections = listOf(
        LegalSection(
            title = "1. Acceptance and jurisdiction",
            paragraphs = listOf(
                "By using Sequo, the user accepts these Terms of Use and any partner agreement, operating policy, or support instruction that applies to their hub. These terms are governed by the laws of the Republic of Togo, unless a mandatory rule provides otherwise.",
                "Sequo may update these terms to reflect changes in law, security, product functions, payment methods, storage policies, or hub operating rules. Continued use after publication or notice means the updated terms apply.",
            ),
        ),
        LegalSection(
            title = "2. Authorized users",
            paragraphs = listOf(
                "Sequo is for authorized hub partner staff, hub managers, Sequo collection agents, and Support/Admin users. Accounts must not be shared. Each user is responsible for actions performed under their account and must keep their login method secure.",
                "A hub may be paused or restricted by Sequo operations, system policy, or approved automation for risk, capacity, compliance, maintenance, partner, or operational reasons. Partner staff cannot override those restrictions from the app.",
            ),
        ),
        LegalSection(
            title = "3. Hub responsibilities",
            paragraphs = listOf(
                "Hub staff must use guided workflows to receive packages, assign lockers, validate pickup or return codes, verify customer identity, record visible package condition, collect configured fees, and hand packages only to authorized customers, delegates, riders, or collection agents.",
                "Staff must not open sealed packages unless Support creates an audited exception. Staff must not release packages without a valid QR or numeric code plus identity verification. Staff must create an incident for damaged, leaking, unsafe, suspicious, wrong-hub, invalid-code, or missing-parcel situations.",
            ),
        ),
        LegalSection(
            title = "4. Storage, fees, and returns",
            paragraphs = listOf(
                "The default free storage window is 14 days after the package becomes available for pickup. Extra storage fees may apply after the free window according to backend/Admin configuration. Fee amounts must come from backend rules and must be visible before handover.",
                "By default, uncollected packages may enter return-to-seller handling after about one month, subject to active configuration and Support holds. Normal customer pickup may be blocked after the return threshold unless Support grants a documented exception.",
                "Customer returns must be authorized by Sequo, presented with a valid return QR or numeric code, and dropped off within 72 hours of delivery unless Support creates an override. Hub staff receive and store returns only; Sequo performs final validation before refund processing.",
            ),
        ),
        LegalSection(
            title = "5. Prohibited use",
            paragraphs = listOf(
                "Users must not bypass validation, share credentials, store unauthorized items, accept food or perishable items for standard hub storage when blocked by policy, release packages after invalid codes, falsify condition records, hide incidents, misuse customer data, or use Sequo for non-Sequo packages without authorization.",
                "Users must not attempt to access other hubs, raw tokens, backend routes, logs, or customer records beyond their assigned role. Any suspected fraud, coercion, bribery, suspicious pickup, or security issue must be escalated to Support.",
            ),
        ),
        LegalSection(
            title = "6. Availability and offline use",
            paragraphs = listOf(
                "Sequo may operate with limited connectivity for safe queued actions, but backend state remains authoritative. The app may block actions when it cannot safely verify current state, restrictions, fees, identity requirements, or token validity.",
                "Sequo may suspend accounts, restrict hubs, revoke sessions, rate-limit authentication, or require re-authentication when needed for security, compliance, abuse prevention, or operations.",
            ),
        ),
        LegalSection(
            title = "7. Liability and disputes",
            paragraphs = listOf(
                "Hub partners are responsible for following Sequo operating instructions, protecting packages in their custody, and recording handoffs accurately. Sequo is responsible for backend validation, support decisions, final return validation, and platform rules under the applicable agreements.",
                "Disputes about fees, identity mismatch, late pickup, return eligibility, damaged packages, missing parcels, or account actions should be escalated through Sequo Support. Courts and authorities in Togo may have jurisdiction where required by applicable law.",
            ),
        ),
    ),
)
