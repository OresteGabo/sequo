package dev.orestegabo.sequo.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberSecureTokenStorage(): SecureTokenStorage = remember { IosSecureTokenStorage() }

private class IosSecureTokenStorage(private val keychain: IosAuthKeychainStore = IosAuthKeychainStore()) : SecureTokenStorage {
    override suspend fun saveRefreshToken(token: String) = keychain.save("refresh_token", token)
    override suspend fun getRefreshToken(): String? = keychain.get("refresh_token")
    override suspend fun clearSession() = keychain.clear("refresh_token")
}
