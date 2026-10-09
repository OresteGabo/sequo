package dev.orestegabo.sequo.core.platform

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource

data class LegalPdfResult(
    val success: Boolean,
    val message: StringResource,
)

interface LegalPdfDownloader {
    fun savePdf(
        fileName: String,
        title: String,
        body: String,
    ): LegalPdfResult
}

@Composable
expect fun rememberLegalPdfDownloader(): LegalPdfDownloader
