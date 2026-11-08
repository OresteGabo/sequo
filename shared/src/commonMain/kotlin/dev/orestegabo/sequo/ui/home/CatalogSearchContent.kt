package dev.orestegabo.sequo.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.catalog.CatalogSnapshot
import dev.orestegabo.sequo.core.catalog.searchProducts
import dev.orestegabo.sequo.feature.settings.AppLanguage
import dev.orestegabo.sequo.feature.settings.LocalAppLanguage
import dev.orestegabo.sequo.model.SequoProductListing

@Composable
internal fun CatalogSearchContent(
    query: String, catalog: CatalogSnapshot?, loading: Boolean, failed: Boolean,
    onReload: () -> Unit, onAddProduct: (String) -> Unit,
    onProductSelected: (SequoProductListing) -> Unit,
) {
    val french = LocalAppLanguage.current == AppLanguage.French
    val matches = remember(catalog, query) { catalog?.searchProducts(query).orEmpty() }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(if (french) "Rechercher des produits" else "Search products", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(if (french) "Produits du catalogue actuel" else "Products in the current catalog", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onReload, enabled = !loading) { Text(if (french) "Actualiser" else "Refresh") }
    }
    when {
        loading -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(20.dp))
            Text(if (french) "Chargement du catalogue…" else "Loading the catalog…")
        }
        failed || catalog == null -> SearchMessage(
            if (french) "Catalogue indisponible" else "Catalog unavailable",
            if (french) "Actualisez pour rechercher les produits disponibles." else "Refresh to search available products.",
        )
        query.isBlank() -> SearchMessage(
            if (french) "Que recherchez-vous ?" else "What are you looking for?",
            if (french) "Saisissez un produit, une boutique ou un quartier." else "Enter a product, shop or neighborhood.",
        )
        matches.isEmpty() -> SearchMessage(
            if (french) "Aucun produit correspondant" else "No matching products",
            if (french) "Essayez un autre nom de produit, de boutique ou de quartier." else "Try another product, shop or neighborhood.",
        )
        else -> {
            Text(if (french) matches.size.toString() + " produit(s)" else matches.size.toString() + " product(s)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            matches.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { listing ->
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(listing.shop.name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            FlashProductCard(
                                shop = listing.shop, product = listing.product,
                                onAddProduct = onAddProduct,
                                onNegotiateClick = { onProductSelected(listing) },
                                onProductClick = { onProductSelected(listing) },
                            )
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SearchMessage(title: String, detail: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = .6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
