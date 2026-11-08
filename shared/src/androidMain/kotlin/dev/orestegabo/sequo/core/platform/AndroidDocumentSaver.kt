package dev.orestegabo.sequo.core.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.orestegabo.sequo.feature.settings.AppLanguage
import dev.orestegabo.sequo.feature.settings.appText
import java.io.OutputStream
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import sequo.shared.generated.resources.*

internal enum class DocumentSaveResult { Saved, Cancelled, Failed, Busy }

internal interface AndroidDocumentSaver {
    suspend fun save(fileName: String, bytes: ByteArray): DocumentSaveResult
}

private data class DocumentMessages(val saved: String, val cancelled: String, val failed: String, val interrupted: String)
private class PendingDocument(
    val bytes: ByteArray,
    var continuation: CancellableContinuation<DocumentSaveResult>?,
    val messages: DocumentMessages,
) {
    var interrupted = false
}

private class DocumentSaveState : ViewModel() {
    var pending: PendingDocument? = null
    override fun onCleared() {
        pending?.continuation?.cancel()
        pending = null
        super.onCleared()
    }
}

private class DocumentExportService(
    val state: DocumentSaveState,
    val launch: (Intent) -> Unit,
)
private val LocalDocumentExportService = staticCompositionLocalOf<DocumentExportService?> { null }

/** Always register at the Activity root, independent of navigation or a photo/legal composable. */
@Composable
fun AndroidDocumentExportHost(content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val state = viewModel { DocumentSaveState() }
    val fallbackLanguage = if (context.resources.configuration.locales[0].language == "fr") AppLanguage.French else AppLanguage.English
    val interruptedMessage = appText(Res.string.export_interrupted, fallbackLanguage)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val request = state.pending
        if (request == null) {
            // A process restart cannot restore bytes held only in memory. Never claim a saved/empty document.
            Toast.makeText(context, interruptedMessage, Toast.LENGTH_LONG).show()
            return@rememberLauncherForActivityResult
        }
        if (request.interrupted) {
            state.pending = null
            Toast.makeText(context, request.messages.interrupted, Toast.LENGTH_LONG).show()
            return@rememberLauncherForActivityResult
        }
        val uri = result.data?.data.takeIf { result.resultCode == Activity.RESULT_OK }
        state.viewModelScope.launch {
            val outcome = try {
                when {
                    uri == null -> DocumentSaveResult.Cancelled
                    uri.scheme != "content" -> DocumentSaveResult.Failed
                    withContext(Dispatchers.IO) {
                        writeDocumentBytes(request.bytes) { context.contentResolver.openOutputStream(uri, "w") }
                    } -> DocumentSaveResult.Saved
                    else -> DocumentSaveResult.Failed
                }
            } finally {
                if (state.pending === request) state.pending = null
            }
            val waiter = request.continuation
            if (waiter?.isActive == true) {
                waiter.resume(outcome)
            } else {
                // Configuration recreation cancelled the old UI scope, while the native export survived.
                val message = when (outcome) {
                    DocumentSaveResult.Saved -> request.messages.saved
                    DocumentSaveResult.Cancelled -> request.messages.cancelled
                    else -> request.messages.failed
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }
    val service = remember(state, launcher) { DocumentExportService(state) { launcher.launch(it) } }
    CompositionLocalProvider(LocalDocumentExportService provides service, content = content)
}

/** ACTION_CREATE_DOCUMENT supports every configured Android SDK without broad storage permissions. */
@Composable
internal fun rememberAndroidDocumentSaver(mimeType: String): AndroidDocumentSaver {
    val activity = LocalContext.current.findExportActivity()
    val service = LocalDocumentExportService.current
    val messages = DocumentMessages(
        saved = appText(if (mimeType == "application/pdf") Res.string.legal_pdf_saved else Res.string.photo_export_saved),
        cancelled = appText(if (mimeType == "application/pdf") Res.string.legal_pdf_cancelled else Res.string.photo_export_cancelled),
        failed = appText(if (mimeType == "application/pdf") Res.string.legal_pdf_save_failed else Res.string.photo_export_save_failed),
        interrupted = appText(Res.string.export_interrupted),
    )
    return remember(service, activity, mimeType, messages) {
        object : AndroidDocumentSaver {
            override suspend fun save(fileName: String, bytes: ByteArray): DocumentSaveResult =
                suspendCancellableCoroutine { continuation ->
                    if (service == null || activity == null) {
                        continuation.resume(DocumentSaveResult.Failed)
                        return@suspendCancellableCoroutine
                    }
                    if (service.state.pending != null) {
                        continuation.resume(DocumentSaveResult.Busy)
                        return@suspendCancellableCoroutine
                    }
                    val request = PendingDocument(bytes, continuation, messages)
                    service.state.pending = request
                    continuation.invokeOnCancellation {
                        request.continuation = null
                        // Rotation is allowed to finish the explicitly requested native export.
                        request.interrupted = !activity.isChangingConfigurations
                    }
                    if (!continuation.isActive) {
                        service.state.pending = null
                        return@suspendCancellableCoroutine
                    }
                    try {
                        service.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = mimeType
                            putExtra(Intent.EXTRA_TITLE, fileName)
                        })
                    } catch (cancelled: CancellationException) {
                        service.state.pending = null
                        throw cancelled
                    } catch (_: Exception) {
                        service.state.pending = null
                        if (continuation.isActive) continuation.resume(DocumentSaveResult.Failed)
                    }
                }
        }
    }
}

private tailrec fun Context.findExportActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findExportActivity()
    else -> null
}

/** Report saved only after all bytes, flush and close succeed; leave user documents intact on failure. */
internal fun writeDocumentBytes(bytes: ByteArray, openStream: () -> OutputStream?): Boolean = try {
    val stream = openStream()
    if (stream == null) false else {
        stream.use {
            it.write(bytes)
            it.flush()
        }
        true
    }
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (_: Exception) {
    false
}
