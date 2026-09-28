package dev.orestegabo.sequo.core.auth

import androidx.compose.runtime.Composable

interface AuthSessionStore {
    suspend fun save(session: AuthSession)
    suspend fun get(): AuthSession?
    suspend fun clear()
}

@Composable
expect fun rememberAuthSessionStore(): AuthSessionStore
