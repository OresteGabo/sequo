package dev.orestegabo.sequo.logic

import dev.orestegabo.sequo.model.*

internal fun productMeasureSymbol(unit: String?): String? = when (unit) {
    "KG" -> "kg"; "G" -> "g"; "L" -> "L"; "ML" -> "mL"; "M" -> "m"; "CM" -> "cm"; "UNIT" -> "pc"
    else -> null
}

internal fun ProductOption.displayLabel(group: ProductOptionGroup): String {
    val amount = measureAmount ?: return name
    val measured = "$amount ${productMeasureSymbol(group.unit) ?: return name}"
    return if (name.replace(" ", "").equals(measured.replace(" ", ""), ignoreCase = true)) measured else "$name ($measured)"
}

internal fun SequoProduct.priceUnitSuffix(): String = customizations.firstOrNull { it.type == "MEASURE" }
    ?.unit?.let(::productMeasureSymbol)?.let { " / $it" }.orEmpty()

internal fun formatProductPrice(product: SequoProduct, compact: Boolean = false): String =
    (if (compact) formatCompactCfa(product.priceCfa) else formatCfa(product.priceCfa)) + product.priceUnitSuffix()

internal fun productSelectionsComplete(groups: List<ProductOptionGroup>, selected: Map<String, Set<String>>): Boolean =
    groups.all { group ->
        val ids = selected[group.id].orEmpty()
        ids.size in group.minChoices..group.maxChoices && ids.all { id -> group.options.any { it.id == id && it.active } }
    }
