package dev.orestegabo.sequo.core.auth

import kotlinx.serialization.Serializable

sealed interface AppleSignInResult {
    data class Success(val idToken: String) : AppleSignInResult
    data object Cancelled : AppleSignInResult
    data object Failed : AppleSignInResult
    data object Unavailable : AppleSignInResult
}

@Serializable
data class AppleSignInChallenge(val challengeId: String, val nonce: String)
