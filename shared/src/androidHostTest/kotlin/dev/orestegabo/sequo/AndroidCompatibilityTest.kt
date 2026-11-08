package dev.orestegabo.sequo

import dev.orestegabo.sequo.core.auth.BiometricAvailability
import dev.orestegabo.sequo.core.auth.selectBiometricAvailability
import dev.orestegabo.sequo.core.platform.writeDocumentBytes
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidCompatibilityTest {
    @Test fun pre29BiometricsNeverResolveTheApi29Manager() {
        for (sdk in 24..28) {
            assertEquals(BiometricAvailability.Available, selectBiometricAvailability(sdk,
                legacy = { BiometricAvailability.Available }, modern = { error("API29 class unavailable") }))
        }
    }

    @Test fun modernBiometricsUseManagerOutcomeWithoutLegacyProbe() {
        assertEquals(BiometricAvailability.NoneEnrolled, selectBiometricAvailability(29,
            legacy = { error("Legacy probe should not run") }, modern = { BiometricAvailability.NoneEnrolled }))
    }

    @Test fun documentSavedOnlyAfterBytesAndCloseComplete() {
        var closed = false
        val bytes = byteArrayOf(1, 2, 3)
        val output = object : ByteArrayOutputStream() { override fun close() { closed = true; super.close() } }
        assertTrue(writeDocumentBytes(bytes) { output })
        assertTrue(closed)
        assertContentEquals(bytes, output.toByteArray())
    }

    @Test fun unavailableOrFailedDocumentStreamsNeverReportSaved() {
        assertFalse(writeDocumentBytes(byteArrayOf(1)) { null })
        assertFalse(writeDocumentBytes(byteArrayOf(1)) { object : OutputStream() {
            override fun write(value: Int) { throw IOException("fixture write failure") }
        } })
        assertFalse(writeDocumentBytes(byteArrayOf(1)) { object : ByteArrayOutputStream() {
            override fun close() { throw IOException("fixture close failure") }
        } })
    }

    @Test fun documentCancellationPropagates() {
        assertFailsWith<CancellationException> { writeDocumentBytes(byteArrayOf(1)) { throw CancellationException("fixture") } }
    }
}
