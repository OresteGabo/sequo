package dev.orestegabo.sequo.feature.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberOpenNotificationSettings(): () -> Boolean {
    val context = LocalContext.current
    return remember(context) {
        {
            val details = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
            val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else details
            runCatching { context.startActivity(notification.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); true }
                .getOrElse { runCatching { context.startActivity(details.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); true }.getOrDefault(false) }
        }
    }
}
