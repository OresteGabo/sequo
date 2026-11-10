package dev.orestegabo.sequo.core.platform

import androidx.compose.runtime.Composable

internal enum class ArchiveSaveResult { Saved, Cancelled, Failed, Busy }
internal interface AccountArchiveSaver { suspend fun save(bytes: ByteArray): ArchiveSaveResult }
@Composable internal expect fun rememberAccountArchiveSaver(): AccountArchiveSaver
@Composable internal expect fun appVersionLabel(): String
