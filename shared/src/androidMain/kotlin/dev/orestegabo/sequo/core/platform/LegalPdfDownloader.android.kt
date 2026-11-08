package dev.orestegabo.sequo.core.platform

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import dev.orestegabo.sequo.feature.settings.appText
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.launch
import sequo.shared.generated.resources.Res
import sequo.shared.generated.resources.legal_pdf_cancelled
import sequo.shared.generated.resources.legal_pdf_choose_destination
import sequo.shared.generated.resources.legal_pdf_prepare_failed
import sequo.shared.generated.resources.legal_pdf_save_failed
import sequo.shared.generated.resources.legal_pdf_save_in_progress
import sequo.shared.generated.resources.legal_pdf_saved

@Composable
actual fun rememberLegalPdfDownloader(): LegalPdfDownloader {
    val context = LocalContext.current.applicationContext
    val saver = rememberAndroidDocumentSaver("application/pdf")
    val scope = rememberCoroutineScope()
    val saved = appText(Res.string.legal_pdf_saved)
    val failed = appText(Res.string.legal_pdf_save_failed)
    val cancelled = appText(Res.string.legal_pdf_cancelled)
    val busy = appText(Res.string.legal_pdf_save_in_progress)
    return remember(context, saver, scope, saved, failed, cancelled, busy) {
        AndroidLegalPdfDownloader { fileName, bytes ->
            scope.launch {
                val message = when (saver.save(fileName, bytes)) {
                    DocumentSaveResult.Saved -> saved
                    DocumentSaveResult.Cancelled -> cancelled
                    DocumentSaveResult.Failed -> failed
                    DocumentSaveResult.Busy -> busy
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}

private class AndroidLegalPdfDownloader(
    private val saveDocument: (String, ByteArray) -> Unit,
) : LegalPdfDownloader {
    override fun savePdf(fileName: String, title: String, body: String): LegalPdfResult = runCatching {
        val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_").take(120)
            .ifBlank { "sequo-document.pdf" }.let { if (it.endsWith(".pdf", true)) it else "$it.pdf" }
        val bytes = createPdfBytes(title, body)
        saveDocument(safeName, bytes)
        // This is a chooser/preparation result; only the completed write reports "saved".
        LegalPdfResult(true, Res.string.legal_pdf_choose_destination)
    }.getOrElse {
        LegalPdfResult(false, Res.string.legal_pdf_prepare_failed)
    }
}

private fun createPdfBytes(title: String, body: String): ByteArray = ByteArrayOutputStream().use { output ->
    val document = PdfDocument()
    try {
        val pageWidth = 595
        val pageHeight = 842
        val margin = 42f
        val lineHeight = 17f
        val titlePaint = Paint().apply {
            color = android.graphics.Color.rgb(15, 23, 32)
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = android.graphics.Color.rgb(35, 49, 43)
            textSize = 11f
            isAntiAlias = true
        }
        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = margin
        canvas.drawText(title, margin, y, titlePaint)
        y += lineHeight * 1.8f
        fun finishPage() {
            document.finishPage(page)
            pageNumber += 1
            page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = margin
        }
        body.split('\n').forEach { paragraph ->
            wrapPdfText(paragraph.ifBlank { " " }, bodyPaint, pageWidth - margin * 2).forEach { line ->
                if (y > pageHeight - margin) finishPage()
                canvas.drawText(line, margin, y, bodyPaint)
                y += lineHeight
            }
            y += lineHeight * 0.35f
        }
        document.finishPage(page)
        document.writeTo(output)
    } finally {
        document.close()
    }
    output.toByteArray()
}

private fun wrapPdfText(
    text: String,
    paint: Paint,
    maxWidth: Float,
): List<String> {
    if (text.isBlank()) return listOf("")
    val words = text.split(' ')
    val lines = mutableListOf<String>()
    var current = ""
    words.forEach { word ->
        val candidate = if (current.isEmpty()) word else "$current $word"
        if (paint.measureText(candidate) <= maxWidth) {
            current = candidate
        } else {
            if (current.isNotEmpty()) lines += current
            current = word
        }
    }
    if (current.isNotEmpty()) lines += current
    return lines
}
