package dev.orestegabo.sequo.ui.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.orestegabo.sequo.feature.settings.appText
import dev.orestegabo.sequo.logic.formatCfa
import dev.orestegabo.sequo.model.ProductOptionSelection
import dev.orestegabo.sequo.model.SequoProduct
import dev.orestegabo.sequo.ui.components.SequoPrimaryButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import sequo.shared.generated.resources.*

@Composable
internal fun ProductOptionsPanel(
    product: SequoProduct,
    onAdd: suspend (String, List<ProductOptionSelection>) -> Int?,
) {
    var selected by remember(product.id, product.customizations) { mutableStateOf(emptyMap<String, Set<String>>()) }
    var busy by remember(product.id) { mutableStateOf(false) }
    var failed by remember(product.id) { mutableStateOf(false) }
    var confirmedPrice by remember(product.id) { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    Column {
        product.customizations.forEach { group ->
            val choices = selected[group.id].orEmpty()
            val max = minOf(group.maxChoices, group.options.size)
            Text(group.name, style = MaterialTheme.typography.titleSmall)
            Text(appText(Res.string.product_option_choices) + " ${group.minChoices}–$max")
            group.options.forEach { option ->
                val checked = option.id in choices
                FilterChip(
                    selected = checked,
                    enabled = !busy && option.active && max > 0 && (checked || max == 1 || choices.size < max),
                    onClick = {
                        val next = when {
                            checked -> choices - option.id
                            max == 1 -> setOf(option.id)
                            else -> choices + option.id
                        }
                        selected = selected + (group.id to next)
                        failed = false
                        confirmedPrice = null
                    },
                    label = {
                        val suffix = when {
                            !option.active -> " · " + appText(Res.string.product_option_unavailable)
                            option.priceDeltaCfa > 0 -> " +${formatCfa(option.priceDeltaCfa)}"
                            else -> ""
                        }
                        Text(option.name + suffix)
                    },
                )
            }
        }
        if (failed) Text(appText(Res.string.product_option_error), color = MaterialTheme.colorScheme.error)
        confirmedPrice?.let { Text(appText(Res.string.product_option_added) + " · ${formatCfa(it)}") }
        SequoPrimaryButton(
            label = appText(if (product.customizations.isEmpty()) Res.string.home_add else Res.string.product_option_add),
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val selections = product.customizations.mapNotNull { group ->
                    selected[group.id]?.takeIf { it.isNotEmpty() }?.let { ProductOptionSelection(group.id, it) }
                }
                busy = true
                failed = false
                confirmedPrice = null
                scope.launch {
                    try {
                        confirmedPrice = onAdd(product.id, selections)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        failed = true
                    } finally {
                        busy = false
                    }
                }
            },
        )
    }
}
