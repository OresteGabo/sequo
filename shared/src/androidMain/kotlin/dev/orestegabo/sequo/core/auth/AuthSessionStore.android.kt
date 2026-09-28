package dev.orestegabo.sequo.core.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
actual fun rememberAuthSessionStore(): AuthSessionStore {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        AndroidAuthSessionStore(context = context)
    }
}

private class AndroidAuthSessionStore(
    context: Context,
    private val json: Json = Json,
) : AuthSessionStore {
    private val preferences = EncryptedSharedPreferences.create(
        context,
        "sequo_auth_session",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override suspend fun save(session: AuthSession) {
        preferences.edit()
            .putString(AuthSessionKey, json.encodeToString(session))
            .apply()
    }

    override suspend fun get(): AuthSession? =
        preferences.getString(AuthSessionKey, null)?.let { encodedSession ->
            runCatching { json.decodeFromString<AuthSession>(encodedSession) }.getOrNull()
        }

    override suspend fun clear() {
        preferences.edit()
            .remove(AuthSessionKey)
            .apply()
    }

    private companion object {
        const val AuthSessionKey = "auth_session"
    }
}
