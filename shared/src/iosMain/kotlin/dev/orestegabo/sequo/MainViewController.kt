package dev.orestegabo.sequo

import androidx.compose.ui.window.ComposeUIViewController
import dev.orestegabo.sequo.core.auth.GoogleSignInResult
import dev.orestegabo.sequo.feature.settings.AppLanguage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun MainViewController(
    onGoogleSignIn: (((GoogleSignInResult) -> Unit) -> Unit) = { complete ->
        complete(GoogleSignInResult.Failure("Google sign-in is not configured on this platform yet."))
    },
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
        onHomeEntered = onHomeEntered,
        openNotificationsRequest = openNotificationsRequest,
    )
}
