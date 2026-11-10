package dev.orestegabo.sequo.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberAccountArchiveSaver(): AccountArchiveSaver {
    val saver = rememberAndroidDocumentSaver("application/zip")
    return remember(saver) { object : AccountArchiveSaver {
        override suspend fun save(bytes: ByteArray) = when (saver.save("Sequo-data.zip", bytes)) {
            DocumentSaveResult.Saved -> ArchiveSaveResult.Saved
            DocumentSaveResult.Cancelled -> ArchiveSaveResult.Cancelled
            DocumentSaveResult.Failed -> ArchiveSaveResult.Failed
            DocumentSaveResult.Busy -> ArchiveSaveResult.Busy
        }
    } }
}

@Composable
internal actual fun appVersionLabel(): String {
    val context = LocalContext.current
    return remember(context) {
        @Suppress("DEPRECATION")
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        @Suppress("DEPRECATION")
        "${info.versionName ?: "—"} (${info.versionCode})"
    }
}
