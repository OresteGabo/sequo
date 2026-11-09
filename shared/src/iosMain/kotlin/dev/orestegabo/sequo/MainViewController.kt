package dev.orestegabo.sequo

import androidx.compose.ui.window.ComposeUIViewController
import dev.orestegabo.sequo.core.auth.AppleSignInResult
import dev.orestegabo.sequo.core.auth.GoogleSignInFailureReason
import dev.orestegabo.sequo.core.auth.GoogleSignInResult
import dev.orestegabo.sequo.feature.settings.AppLanguage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun MainViewController(
    onGoogleSignIn: (((GoogleSignInResult) -> Unit) -> Unit) = { complete ->
        complete(GoogleSignInResult.Failure(GoogleSignInFailureReason.NotConfigured))
    },
    onAppleSignIn: ((String, (AppleSignInResult) -> Unit) -> Unit) = { _, complete -> complete(AppleSignInResult.Unavailable) },
    onGoogleSignOut: () -> Unit = {},
    onHomeEntered: (AppLanguage) -> Unit = {},
    openNotificationsRequest: Int = 0,
) = ComposeUIViewController {
        App(
        onGoogleSignIn = {
            suspendCancellableCoroutine { continuation ->
                onGoogleSignIn { result ->
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }
            }
        },
        onAppleSignIn = { nonce ->
            suspendCancellableCoroutine { continuation ->
                onAppleSignIn(nonce) { result ->
                    if (continuation.isActive) continuation.resume(result)
                }
            }
        },
        onGoogleSignOut = { onGoogleSignOut() },
        onHomeEntered = onHomeEntered,
        openNotificationsRequest = openNotificationsRequest,
    )
}
