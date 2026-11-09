package dev.orestegabo.sequo.ui.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.catalog.ProductOptionsClient
import dev.orestegabo.sequo.core.catalog.ProductOptionsQuote
import dev.orestegabo.sequo.feature.settings.appText
import dev.orestegabo.sequo.logic.*
import dev.orestegabo.sequo.model.*
import dev.orestegabo.sequo.ui.components.SequoPrimaryButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import sequo.shared.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProductOptionsPanel(
    product: SequoProduct,
    showPricePreview: Boolean = true,
    onAdd: suspend (String, List<ProductOptionSelection>) -> Int?,
) {
    var selected by remember(product.id, product.customizations) { mutableStateOf(emptyMap<String, Set<String>>()) }
    var busy by remember(product.id) { mutableStateOf(false) }
    var failed by remember(product.id) { mutableStateOf(false) }
    var confirmedPrice by remember(product.id) { mutableStateOf<Int?>(null) }
    var quote by remember(product.id) { mutableStateOf<ProductOptionsQuote?>(null) }
    var quoting by remember(product.id) { mutableStateOf(false) }
    var quoteFailed by remember(product.id) { mutableStateOf(false) }
    var retry by remember(product.id) { mutableStateOf(0) }
    val client = remember { ProductOptionsClient() }
    DisposableEffect(client) { onDispose { client.close() } }
    val complete = productSelectionsComplete(product.customizations, selected)
    val selections = product.customizations.mapNotNull { group -> selected[group.id]?.takeIf { it.isNotEmpty() }
        ?.let { ProductOptionSelection(group.id, it) } }
    LaunchedEffect(product.id, selections, complete, retry) {
        quote = null; quoteFailed = false; quoting = false
        if (complete) {
            quoting = true
            try { quote = client.quote(product.id, selections) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { quoteFailed = true }
            finally { quoting = false }
        }
    }
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        product.customizations.forEach { group ->
            val choices = selected[group.id].orEmpty()
            val max = minOf(group.maxChoices, group.options.size)
            Text(group.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(if (group.type in setOf("SIZE", "COLOR", "MEASURE")) appText(Res.string.selling_choose_one)
                else appText(Res.string.product_option_choices) + " ${group.minChoices}–$max",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            fun select(option: ProductOption) {
                val next = when {
                    option.id in choices -> choices - option.id
                    max == 1 -> setOf(option.id)
                    else -> choices + option.id
                }
                selected = selected + (group.id to next)
                quote = null; confirmedPrice = null; failed = false
            }
            if (group.type in setOf("SIZE", "COLOR", "MEASURE")) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    group.options.forEach { option ->
                        FilterChip(selected = option.id in choices, enabled = !busy && option.active,
                            onClick = { select(option) },
                            label = { Text(option.displayLabel(group) + if (option.priceDeltaCfa > 0) " +${formatCfa(option.priceDeltaCfa)}" else "") })
                    }
                }
                if (group.type == "MEASURE") Text(appText(Res.string.selling_portion_hint),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else group.options.forEach { option ->
                val checked = option.id in choices
                val enabled = !busy && option.active && max > 0 && (checked || max == 1 || choices.size < max)
                Row(Modifier.fillMaxWidth().toggleable(value = checked, enabled = enabled,
                    role = if (max == 1) Role.RadioButton else Role.Checkbox,
                    onValueChange = { select(option) }).padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (max == 1) RadioButton(selected = checked, enabled = enabled, onClick = null)
                    else Checkbox(checked = checked, enabled = enabled, onCheckedChange = null)
                    Text(option.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    val suffix = when {
                        !option.active -> appText(Res.string.product_option_unavailable)
                        option.priceDeltaCfa > 0 -> "+${formatCfa(option.priceDeltaCfa)}"
                        else -> ""
                    }
                    if (suffix.isNotBlank()) Text(suffix, style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (!complete) Text(appText(Res.string.selling_required_choices), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (quoting) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (quoteFailed) {
            Text(appText(Res.string.selling_price_error), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { retry++ }) { Text(appText(Res.string.cart_refresh)) }
        }
        if (showPricePreview) quote?.let { actual ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(appText(Res.string.selling_selected_price), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Text(formatCfa(actual.unitPriceCfa), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
        if (failed) Text(appText(Res.string.product_option_error), color = MaterialTheme.colorScheme.error)
        confirmedPrice?.let { Text(appText(Res.string.product_option_added) + " · ${formatCfa(it)}") }
        SequoPrimaryButton(label = appText(if (product.customizations.isEmpty()) Res.string.home_add else Res.string.product_option_add),
            enabled = !busy && complete && quote != null && !quoting,
            modifier = Modifier.fillMaxWidth(), onClick = {
                busy = true; failed = false; confirmedPrice = null
                scope.launch {
                    try { confirmedPrice = onAdd(product.id, selections) }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { failed = true }
                    finally { busy = false }
                }
            })
    }
}
