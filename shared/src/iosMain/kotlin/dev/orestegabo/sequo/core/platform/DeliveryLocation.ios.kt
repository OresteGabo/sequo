package dev.orestegabo.sequo.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.*
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

@Composable
internal actual fun rememberCurrentDeliveryLocation(): suspend () -> DeliveryCoordinates? {
    val source = remember { IosDeliveryLocation() }
    return remember(source) { { source.current() } }
}

@OptIn(ExperimentalForeignApi::class)
private class IosDeliveryLocation {
    suspend fun current(): DeliveryCoordinates? = withTimeoutOrNull(20_000) {
        suspendCancellableCoroutine { continuation ->
            val manager = CLLocationManager()
            fun finish(value: DeliveryCoordinates?) {
                manager.stopUpdatingLocation()
                manager.delegate = null
                if (continuation.isActive) continuation.resume(value)
            }
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                fun authorized() {
                    when (manager.authorizationStatus) {
                        kCLAuthorizationStatusAuthorizedAlways, kCLAuthorizationStatusAuthorizedWhenInUse -> manager.requestLocation()
                        kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> finish(null)
                        else -> manager.requestWhenInUseAuthorization()
                    }
                }
                override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) { authorized() }
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    val location = didUpdateLocations.lastOrNull() as? CLLocation
                    val point = location?.coordinate?.useContents { DeliveryCoordinates(latitude, longitude) }
                    finish(point?.takeIf { it.valid() })
                }
                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) { finish(null) }
            }
            manager.delegate = delegate
            manager.desiredAccuracy = kCLLocationAccuracyBest
            // Keep both manager and its weak delegate alive until the one-shot request finishes.
            continuation.invokeOnCancellation { manager.stopUpdatingLocation(); manager.delegate = null; delegate.hashCode() }
            delegate.authorized()
        }
    }
}
