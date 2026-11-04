package dev.orestegabo.sequo.ui.basket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.auth.SavedDeliveryAddress
import dev.orestegabo.sequo.core.catalog.CartLine
import dev.orestegabo.sequo.core.catalog.CartQuote
import dev.orestegabo.sequo.core.catalog.CartSnapshot
import dev.orestegabo.sequo.feature.settings.appText
import dev.orestegabo.sequo.logic.formatCfa
import kotlinx.coroutines.CancellationException
import sequo.shared.generated.resources.*

@Composable
internal fun BasketContent(
    cart: CartSnapshot?,
    busy: Boolean,
    failed: Boolean,
    onRefresh: () -> Unit,
    onQuantity: (CartLine, Int) -> Unit,
    loadAddresses: suspend () -> List<SavedDeliveryAddress>,
    loadQuote: suspend (String) -> CartQuote,
) {
    Text(appText(Res.string.nav_basket_title), style = MaterialTheme.typography.headlineSmall)
    if (busy) Text(appText(Res.string.cart_loading))
    if (failed) Text(appText(Res.string.cart_load_failed), color = MaterialTheme.colorScheme.error)
    TextButton(onClick = onRefresh, enabled = !busy) { Text(appText(Res.string.cart_refresh)) }
    if (cart == null) return
    if (cart.items.isEmpty()) {
        Text(appText(Res.string.cart_empty))
        return
    }

    for (line in cart.items) {
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Text(line.product.name, style = MaterialTheme.typography.titleMedium)
            val price = line.lineTotalCfa
            Text(if (line.available && price != null) formatCfa(price) else appText(Res.string.cart_item_unavailable))
            val optionNames = line.selections.flatMap { selection ->
                val group = line.product.customizations.firstOrNull { it.id == selection.groupId }
                group?.options.orEmpty().filter { it.id in selection.optionIds }.map { it.name }
            }
            if (optionNames.isNotEmpty()) Text(optionNames.joinToString())
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { onQuantity(line, line.quantity - 1) }, enabled = !busy && line.available && line.quantity > 1) {
                    Text("−")
                }
                Text(line.quantity.toString())
                TextButton(onClick = { onQuantity(line, line.quantity + 1) }, enabled = !busy && line.available && line.quantity < 99) {
                    Text("+")
                }
                TextButton(onClick = { onQuantity(line, 0) }, enabled = !busy) { Text(appText(Res.string.cart_remove)) }
            }
        }
    }

    CartAmount(appText(Res.string.basket_payment_items), cart.subtotalCfa)
    if (cart.items.any { !it.available } || failed) return

    var addresses by remember { mutableStateOf<List<SavedDeliveryAddress>?>(null) }
    var addressesFailed by remember { mutableStateOf(false) }
    var addressId by remember { mutableStateOf<String?>(null) }
    var quote by remember(cart, addressId) { mutableStateOf<CartQuote?>(null) }
    var quoteFailed by remember(cart, addressId) { mutableStateOf(false) }
    LaunchedEffect(cart) {
        try {
            addresses = loadAddresses().filter { it.id.isNotBlank() }
            addressesFailed = false
            addressId = addressId?.takeIf { id -> addresses.orEmpty().any { it.id == id } }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            addresses = null
            addressesFailed = true
        }
    }
    LaunchedEffect(cart, addressId, busy) {
        quote = null
        quoteFailed = false
        if (busy) return@LaunchedEffect
        val selected = addressId ?: return@LaunchedEffect
        try {
            quote = loadQuote(selected)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            quoteFailed = true
        }
    }
    Text(appText(Res.string.basket_delivery_home), style = MaterialTheme.typography.titleMedium)
    when {
        addressesFailed -> Text(appText(Res.string.cart_load_failed), color = MaterialTheme.colorScheme.error)
        addresses == null -> Text(appText(Res.string.cart_loading))
        addresses.orEmpty().isEmpty() -> Text(appText(Res.string.cart_no_addresses))
    }
    for (address in addresses.orEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = address.id == addressId, onClick = { addressId = address.id })
            Column {
                Text(address.label)
                Text(listOfNotNull(address.area, address.city).joinToString(", "))
            }
        }
    }
    when {
        quoteFailed -> Text(appText(Res.string.cart_quote_failed), color = MaterialTheme.colorScheme.error)
        addressId != null && quote == null -> Text(appText(Res.string.cart_loading))
        !busy -> quote?.let {
            CartAmount(appText(Res.string.basket_payment_items), it.productTotalCfa)
            CartAmount(appText(Res.string.cart_options), it.optionTotalCfa)
            CartAmount(appText(Res.string.cart_service_fee), it.serviceFeeCfa)
            CartAmount(appText(Res.string.basket_delivery), it.deliveryFeeCfa)
            CartAmount(appText(Res.string.basket_payment_total), it.totalCfa)
        }
    }
}

@Composable
private fun CartAmount(label: String, amount: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(formatCfa(amount))
    }
}
