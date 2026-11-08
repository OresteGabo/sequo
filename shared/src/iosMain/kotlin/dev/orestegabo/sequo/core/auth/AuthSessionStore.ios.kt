package dev.orestegabo.sequo.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberAuthSessionStore(): AuthSessionStore =
    remember { IosAuthSessionStore() }

private class IosAuthSessionStore(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
    private val json: Json = Json,
    private val keychain: IosAuthKeychainStore = IosAuthKeychainStore(),
) : AuthSessionStore {
    private val mutation = Mutex()
    override suspend fun save(session: AuthSession) = mutation.withLock {
        keychain.save(AuthSessionKey, json.encodeToString(session))
        userDefaults.removeObjectForKey(AuthSessionKey)
    }

    override suspend fun get(): AuthSession? = mutation.withLock {
        keychain.get(AuthSessionKey)?.let { return@withLock json.decodeFromString<AuthSession>(it) }
        val legacy = userDefaults.stringForKey(AuthSessionKey) ?: return@withLock null
        val supported = runCatching { json.decodeFromString<AuthSession>(legacy) }.getOrNull() ?: return@withLock null
        val encoded = json.encodeToString(supported)
        keychain.save(AuthSessionKey, encoded)
        check(keychain.get(AuthSessionKey) == encoded) { "Saved sign-in could not be protected. Try again." }
        userDefaults.removeObjectForKey(AuthSessionKey)
        supported
    }

    override suspend fun saveRememberedUser(user: CurrentUser) {
        userDefaults.setObject(
            value = json.encodeToString(user),
            forKey = RememberedUserKey,
        )
    }

    override suspend fun getRememberedUser(): CurrentUser? =
        userDefaults.stringForKey(RememberedUserKey)?.let { encodedUser ->
            runCatching { json.decodeFromString<CurrentUser>(encodedUser) }.getOrNull()
        }

    override suspend fun clear() = mutation.withLock {
        keychain.clear(AuthSessionKey)
        userDefaults.removeObjectForKey(AuthSessionKey)
        userDefaults.removeObjectForKey(RememberedUserKey)
    }

    private companion object {
        const val AuthSessionKey = "auth_session"
        const val RememberedUserKey = "remembered_user"
    }
}
