package dev.orestegabo.sequo.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.*
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject
import platform.posix.*
import kotlin.coroutines.resume

@Composable internal actual fun rememberAccountArchiveSaver(): AccountArchiveSaver = remember { IosAccountArchiveSaver() }
@Composable internal actual fun appVersionLabel(): String = remember {
    val bundle = NSBundle.mainBundle
    "${bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") ?: "—"} (${bundle.objectForInfoDictionaryKey("CFBundleVersion") ?: "—"})"
}

@OptIn(ExperimentalForeignApi::class)
private class IosAccountArchiveSaver : AccountArchiveSaver {
    private var delegate: NSObject? = null
    override suspend fun save(bytes: ByteArray): ArchiveSaveResult {
        if (delegate != null) return ArchiveSaveResult.Busy
        var controller = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return ArchiveSaveResult.Failed
        while (controller.presentedViewController != null) controller = controller.presentedViewController!!
        val path = NSTemporaryDirectory().trimEnd('/') + "/Sequo-data-${NSUUID().UUIDString}.zip"
        val file = fopen(path, "wb") ?: return ArchiveSaveResult.Failed
        val count = bytes.usePinned { fwrite(it.addressOf(0), 1u, bytes.size.toULong(), file) }
        val closed = fclose(file)
        if (count != bytes.size.toULong() || closed != 0) {
            NSFileManager.defaultManager.removeItemAtPath(path, null)
            return ArchiveSaveResult.Failed
        }
        return suspendCancellableCoroutine { continuation ->
            val picker = UIDocumentPickerViewController(forExportingURLs = listOf(NSURL.fileURLWithPath(path)), asCopy = true)
            fun finish(result: ArchiveSaveResult) {
                delegate = null
                NSFileManager.defaultManager.removeItemAtPath(path, null)
                if (continuation.isActive) continuation.resume(result)
            }
            val callbacks = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                    finish(if (didPickDocumentsAtURLs.isNotEmpty()) ArchiveSaveResult.Saved else ArchiveSaveResult.Failed)
                }
                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) { finish(ArchiveSaveResult.Cancelled) }
            }
            delegate = callbacks
            picker.delegate = callbacks
            continuation.invokeOnCancellation {
                picker.dismissViewControllerAnimated(false, null)
                delegate = null
                NSFileManager.defaultManager.removeItemAtPath(path, null)
            }
            controller.presentViewController(picker, animated = true, completion = null)
        }
    }
}
