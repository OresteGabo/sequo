package dev.orestegabo.sequo.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.CFRelease
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

@Composable
actual fun rememberSecureTokenStorage(): SecureTokenStorage =
    remember { IosSecureTokenStorage() }

@OptIn(ExperimentalForeignApi::class)
private class IosSecureTokenStorage : SecureTokenStorage {
    override suspend fun saveRefreshToken(token: String) {
        val tokenData = token.toNSData() ?: return
        SecItemDelete(baseQuery())

        val status = SecItemAdd(
            (baseQueryMap() + mapOf<Any?, Any?>(
                kSecValueData to tokenData,
                kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
            )).toCFDictionary(),
            null,
        )
        check(status == errSecSuccess) { "Keychain failed to save refresh token: $status" }
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override suspend fun getRefreshToken(): String? =
        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(
                (baseQueryMap() + mapOf<Any?, Any?>(
                    kSecReturnData to true,
                    kSecMatchLimit to kSecMatchLimitOne,
                )).toCFDictionary(),
                result.ptr,
            )
            when (status) {
                errSecSuccess -> {
                    val data = result.value as? NSData
                    data?.toUtf8String()
                }
                errSecItemNotFound -> null
                else -> null
            }.also {
                result.value?.let { value -> CFRelease(value) }
            }
        }

    override suspend fun clearSession() {
        SecItemDelete(baseQuery())
    }

    private fun baseQuery(): CFDictionaryRef =
        baseQueryMap().toCFDictionary()

    private fun baseQueryMap(): Map<Any?, Any?> =
        mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to KeychainService,
            kSecAttrAccount to RefreshTokenAccount,
        )

    private companion object {
        const val KeychainService = "dev.orestegabo.sequo.auth"
        const val RefreshTokenAccount = "refresh_token"
    }
}

@Suppress("CAST_NEVER_SUCCEEDS")
@OptIn(ExperimentalForeignApi::class)
private fun Map<Any?, Any?>.toCFDictionary(): CFDictionaryRef =
    this as CFDictionaryRef

@OptIn(BetaInteropApi::class)
private fun String.toNSData(): NSData? =
    NSString.create(string = this).dataUsingEncoding(NSUTF8StringEncoding)

@Suppress("CAST_NEVER_SUCCEEDS")
@OptIn(BetaInteropApi::class)
private fun NSData.toUtf8String(): String? =
    NSString.create(data = this, encoding = NSUTF8StringEncoding)?.toString()
