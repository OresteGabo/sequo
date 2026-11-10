package dev.orestegabo.sequo.ui.basket

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.auth.SavedDeliveryAddress
import dev.orestegabo.sequo.core.catalog.DeliveryAddressInput
import dev.orestegabo.sequo.core.platform.*
import dev.orestegabo.sequo.feature.settings.appText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import sequo.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeliveryAddressEditor(initial: SavedDeliveryAddress?, onDismiss: () -> Unit,
    onSave: suspend (String?, DeliveryAddressInput) -> Unit) {
    var label by remember { mutableStateOf(initial?.label.orEmpty()) }
    var recipient by remember { mutableStateOf(initial?.recipientName.orEmpty()) }
    var phone by remember { mutableStateOf(initial?.phoneNumber.orEmpty()) }
    var city by remember { mutableStateOf(initial?.city.orEmpty()) }
    var area by remember { mutableStateOf(initial?.area.orEmpty()) }
    var street by remember { mutableStateOf(initial?.streetHint.orEmpty()) }
    var pin by remember { mutableStateOf(initial?.latitude?.let { lat -> initial.longitude?.let { DeliveryCoordinates(lat, it).mapLink() } }.orEmpty()) }
    var busy by remember { mutableStateOf(false) }
    var locating by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var locationFailed by remember { mutableStateOf(false) }
    val currentLocation = rememberCurrentDeliveryLocation()
    val scope = rememberCoroutineScope()
    val coordinates = deliveryPin(pin)
    val complete = label.isNotBlank() && recipient.isNotBlank() && phone.count { it.isDigit() } in 8..15 && city.isNotBlank() && coordinates != null
    ModalBottomSheet(onDismissRequest = { if (!busy && !locating) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)
            .padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(appText(if (initial == null) Res.string.cart_add_address else Res.string.cart_edit_address), style = MaterialTheme.typography.headlineSmall)
            Text(appText(Res.string.cart_address_detail), style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(label, { label = it.take(180); failed = false }, label = { Text(appText(Res.string.cart_address_label)) },
                singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(recipient, { recipient = it.take(180); failed = false }, label = { Text(appText(Res.string.cart_address_recipient)) },
                singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(phone, { phone = it.take(64); failed = false }, label = { Text(appText(Res.string.cart_address_phone)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(city, { city = it.take(120); failed = false }, label = { Text(appText(Res.string.cart_address_city)) },
                singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(area, { area = it.take(160); failed = false }, label = { Text(appText(Res.string.cart_address_area)) },
                singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(street, { street = it.take(240); failed = false }, label = { Text(appText(Res.string.cart_address_street)) },
                enabled = !busy, modifier = Modifier.fillMaxWidth())
            OutlinedButton(onClick = {
                locating = true; locationFailed = false
                scope.launch {
                    try {
                        val point = currentLocation()
                        if (point == null) locationFailed = true else pin = point.mapLink()
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { locationFailed = true }
                    finally { locating = false }
                }
            }, enabled = !busy && !locating, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.MyLocation, contentDescription = null); Spacer(Modifier.width(8.dp))
                Text(appText(if (locating) Res.string.cart_address_locating else Res.string.cart_address_current_location))
            }
            if (locationFailed) Text(appText(Res.string.cart_address_location_failed), color = MaterialTheme.colorScheme.error)
            OutlinedTextField(pin, { pin = it.take(2000); failed = false }, label = { Text(appText(Res.string.cart_address_pin)) },
                supportingText = { Text(appText(Res.string.cart_address_pin_hint)) }, isError = pin.isNotBlank() && coordinates == null,
                trailingIcon = { if (pin.isNotBlank()) IconButton(onClick = { pin = "" }, enabled = !busy && !locating) {
                    Icon(Icons.Filled.Close, appText(Res.string.cart_address_clear_pin))
                } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                enabled = !busy && !locating, modifier = Modifier.fillMaxWidth())
            if (coordinates != null) Text(appText(Res.string.cart_address_pin_ready), color = MaterialTheme.colorScheme.primary)
            if (!complete) Text(appText(Res.string.cart_address_required), style = MaterialTheme.typography.bodySmall)
            if (failed) Text(appText(Res.string.cart_address_save_failed), color = MaterialTheme.colorScheme.error)
            Button(onClick = {
                val point = coordinates ?: return@Button
                busy = true; failed = false
                scope.launch {
                    try { onSave(initial?.id, DeliveryAddressInput(label.trim(), recipient.trim(), phone.trim(), city.trim(),
                        area.trim().ifBlank { null }, street.trim().ifBlank { null }, point.latitude, point.longitude, initial?.kind ?: "HOME")) }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { failed = true }
                    finally { busy = false }
                }
            }, enabled = complete && !busy && !locating, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
                Text(appText(if (busy) Res.string.cart_address_saving else Res.string.cart_address_save))
            }
            TextButton(onClick = onDismiss, enabled = !busy && !locating, modifier = Modifier.fillMaxWidth()) {
                Text(appText(Res.string.cart_address_cancel))
            }
        }
    }
}
