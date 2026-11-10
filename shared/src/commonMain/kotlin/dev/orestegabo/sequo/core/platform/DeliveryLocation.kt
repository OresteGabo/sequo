package dev.orestegabo.sequo.core.platform

import androidx.compose.runtime.Composable
import io.ktor.http.Url

internal data class DeliveryCoordinates(val latitude: Double, val longitude: Double) {
    fun valid() = latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0
    fun mapLink() = "https://maps.apple.com/?ll=$latitude,$longitude"
}

/** Parse an explicitly shared pin, never guess coordinates from a street address or map viewport. */
internal fun deliveryPin(link: String): DeliveryCoordinates? = runCatching {
    val url = Url(link.trim())
    val host = url.host.lowercase()
    val pair = when {
        host == "maps.apple.com" -> url.parameters["coordinate"] ?: url.parameters["ll"]
        host == "www.openstreetmap.org" || host == "openstreetmap.org" ->
            url.parameters["mlat"]?.let { lat -> url.parameters["mlon"]?.let { lon -> "$lat,$lon" } }
        host in setOf("maps.google.com", "www.google.com", "google.com") -> url.parameters["q"] ?: url.parameters["query"]
        else -> null
    } ?: return null
    val numbers = pair.split(',').map { it.trim().toDoubleOrNull() ?: return null }
    if (numbers.size != 2) return null
    DeliveryCoordinates(numbers[0], numbers[1]).takeIf { it.valid() }
}.getOrNull()

@Composable internal expect fun rememberCurrentDeliveryLocation(): suspend () -> DeliveryCoordinates?
