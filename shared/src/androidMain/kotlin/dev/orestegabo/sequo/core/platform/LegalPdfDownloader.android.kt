package dev.orestegabo.sequo.core.platform

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import sequo.shared.generated.resources.Res
import sequo.shared.generated.resources.legal_pdf_create_failed
import sequo.shared.generated.resources.legal_pdf_open_failed
import sequo.shared.generated.resources.legal_pdf_save_failed
import sequo.shared.generated.resources.legal_pdf_saved

@Composable
actual fun rememberLegalPdfDownloader(): LegalPdfDownloader {
    val context = LocalContext.current
    return remember(context) { AndroidLegalPdfDownloader(context.applicationContext) }
}

private class AndroidLegalPdfDownloader(
    private val context: Context,
) : LegalPdfDownloader {
    override fun savePdf(
        fileName: String,
        title: String,
        body: String,
    ): LegalPdfResult = runCatching {
        val document = PdfDocument()
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
            val wrappedLines = wrapPdfText(paragraph.ifBlank { " " }, bodyPaint, pageWidth - margin * 2)
            wrappedLines.forEach { line ->
                if (y > pageHeight - margin) {
                    finishPage()
                }
                canvas.drawText(line, margin, y, bodyPaint)
                y += lineHeight
            }
            y += lineHeight * 0.35f
        }
        document.finishPage(page)

        val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, safeName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: return LegalPdfResult(false, Res.string.legal_pdf_create_failed)

        resolver.openOutputStream(uri)?.use { output ->
            document.writeTo(output)
        } ?: return LegalPdfResult(false, Res.string.legal_pdf_open_failed)
        document.close()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }

        LegalPdfResult(true, Res.string.legal_pdf_saved)
    }.getOrElse {
        LegalPdfResult(false, Res.string.legal_pdf_save_failed)
    }
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
