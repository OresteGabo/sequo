package dev.orestegabo.sequo.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

@Composable
actual fun rememberLegalPdfDownloader(): LegalPdfDownloader =
    remember { IosLegalPdfDownloader() }

private class IosLegalPdfDownloader : LegalPdfDownloader {
    @OptIn(ExperimentalForeignApi::class)
    override fun savePdf(
        fileName: String,
        title: String,
        body: String,
    ): LegalPdfResult = runCatching {
        val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val path = NSTemporaryDirectory().trimEnd('/') + "/$safeName"
        val pdfText = buildSimplePdf(title = title, body = body)
        val bytes = pdfText.encodeToByteArray()
        if (!writeBytesToFile(path = path, bytes = bytes)) {
            return LegalPdfResult(false, "Could not create the PDF file.")
        }

        val controller = topViewController()
            ?: return LegalPdfResult(false, "Could not open the iOS share sheet.")
        val activityController = UIActivityViewController(
            activityItems = listOf(NSURL.fileURLWithPath(path)),
            applicationActivities = null,
        )
        controller.presentViewController(
            viewControllerToPresent = activityController,
            animated = true,
            completion = null,
        )
        LegalPdfResult(true, "Choose Files, Messages, WhatsApp, Mail, AirDrop, or another app.")
    }.getOrElse { error ->
        LegalPdfResult(false, error.message ?: "Could not prepare the PDF.")
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun writeBytesToFile(
    path: String,
    bytes: ByteArray,
): Boolean {
    val file = fopen(path, "wb") ?: return false
    val written = bytes.usePinned { pinned ->
        fwrite(
            pinned.addressOf(0).reinterpret<ByteVar>(),
            1u,
            bytes.size.toULong(),
            file,
        )
    }
    fclose(file)
    return written == bytes.size.toULong()
}

private fun buildSimplePdf(
    title: String,
    body: String,
): String {
    val lines = buildList {
        add(title)
        add("")
        body.split('\n').forEach { paragraph ->
            wrapPdfText(paragraph.ifBlank { " " }, 86).forEach(::add)
            add("")
        }
    }.map { it.toPdfAscii() }
    val linesPerPage = 42
    val pages = lines.chunked(linesPerPage).ifEmpty { listOf(listOf("")) }
    val fontObjectId = 3 + pages.size * 2
    val objects = mutableListOf<Pair<Int, String>>()

    objects += 1 to "<< /Type /Catalog /Pages 2 0 R >>"
    objects += 2 to "<< /Type /Pages /Kids [${pages.indices.joinToString(" ") { "${3 + it * 2} 0 R" }}] /Count ${pages.size} >>"
    pages.forEachIndexed { index, pageLines ->
        val pageObjectId = 3 + index * 2
        val contentObjectId = pageObjectId + 1
        val stream = buildPageStream(pageLines)
        objects += pageObjectId to "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 $fontObjectId 0 R >> >> /Contents $contentObjectId 0 R >>"
        objects += contentObjectId to "<< /Length ${stream.length} >>\nstream\n$stream\nendstream"
    }
    objects += fontObjectId to "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"

    val output = StringBuilder()
    val offsets = mutableMapOf<Int, Int>()
    output.append("%PDF-1.4\n")
    objects.forEach { (id, content) ->
        offsets[id] = output.length
        output.append("$id 0 obj\n")
        output.append(content)
        output.append("\nendobj\n")
    }
    val xrefStart = output.length
    val objectCount = fontObjectId + 1
    output.append("xref\n")
    output.append("0 $objectCount\n")
    output.append("0000000000 65535 f \n")
    (1 until objectCount).forEach { id ->
        val offset = offsets[id] ?: 0
        output.append(offset.toString().padStart(10, '0'))
        output.append(" 00000 n \n")
    }
    output.append("trailer\n")
    output.append("<< /Size $objectCount /Root 1 0 R >>\n")
    output.append("startxref\n")
    output.append("$xrefStart\n")
    output.append("%%EOF")
    return output.toString()
}

private fun buildPageStream(lines: List<String>): String =
    buildString {
        append("BT\n")
        append("/F1 11 Tf\n")
        append("42 800 Td\n")
        lines.forEachIndexed { index, line ->
            if (index == 0) append("/F1 18 Tf\n")
            if (index == 1) append("/F1 11 Tf\n")
            append("(")
            append(line.escapePdfText())
            append(") Tj\n")
            append("0 -17 Td\n")
        }
        append("ET")
    }

private fun wrapPdfText(
    text: String,
    maxChars: Int,
): List<String> {
    if (text.isBlank()) return listOf("")
    val lines = mutableListOf<String>()
    var current = ""
    text.split(' ').forEach { word ->
        val candidate = if (current.isEmpty()) word else "$current $word"
        if (candidate.length <= maxChars) {
            current = candidate
        } else {
            if (current.isNotEmpty()) lines += current
            current = word
        }
    }
    if (current.isNotEmpty()) lines += current
    return lines
}

private fun String.escapePdfText(): String =
    replace("\\", "\\\\")
        .replace("(", "\\(")
        .replace(")", "\\)")

private fun String.toPdfAscii(): String =
    map { character ->
        if (character.code in 32..126) character else '-'
    }.joinToString(separator = "")

private fun topViewController(): UIViewController? {
    val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return null
    var topController = rootController
    while (topController.presentedViewController != null) {
        topController = topController.presentedViewController!!
    }
    return topController
}
