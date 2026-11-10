package dev.orestegabo.sequo.core.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

private class LocationPermission { var pending: CancellableContinuation<Boolean>? = null }
@Composable
internal actual fun rememberCurrentDeliveryLocation(): suspend () -> DeliveryCoordinates? {
    val context = LocalContext.current
    val permission = remember { LocationPermission() }
    fun allowed() = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permission.pending?.let { if (it.isActive) it.resume(allowed()) }; permission.pending = null
    }
    return remember(context, launcher) {
        {
            withTimeoutOrNull(25_000) {
                val granted = allowed() || suspendCancellableCoroutine { next ->
                    permission.pending = next
                    next.invokeOnCancellation { if (permission.pending === next) permission.pending = null }
                    launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }
                if (!granted) null else {
                    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val candidates = if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER) else listOf(LocationManager.NETWORK_PROVIDER)
                    val provider = candidates.firstOrNull { manager.isProviderEnabled(it) }
                    if (provider == null) null else suspendCancellableCoroutine { next ->
                        val listener = object : LocationListener {
                            override fun onLocationChanged(location: Location) {
                                manager.removeUpdates(this)
                                if (next.isActive) next.resume(DeliveryCoordinates(location.latitude, location.longitude).takeIf { it.valid() })
                            }
                            override fun onProviderDisabled(provider: String) { manager.removeUpdates(this); if (next.isActive) next.resume(null) }
                            override fun onProviderEnabled(provider: String) {}
                            @Deprecated("Legacy callback") override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        }
                        next.invokeOnCancellation { manager.removeUpdates(listener) }
                        try { manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper()) }
                        catch (_: Exception) { if (next.isActive) next.resume(null) }
                    }
                }
            }
        }
    }
}
