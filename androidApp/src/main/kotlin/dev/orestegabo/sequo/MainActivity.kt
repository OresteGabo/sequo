package dev.orestegabo.sequo

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dev.orestegabo.sequo.core.auth.GoogleSignInResult
import org.json.JSONObject
import java.security.SecureRandom

private const val GoogleSignInTag = "SequoGoogleSignIn"

class MainActivity : ComponentActivity() {
    private val credentialManager by lazy {
        CredentialManager.create(this)
    }
    private val secureRandom = SecureRandom()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(onGoogleSignIn = ::signInWithGoogle)
        }
    }

    private suspend fun signInWithGoogle(): GoogleSignInResult {
        val googleIdOption = GetSignInWithGoogleOption.Builder(
            getString(R.string.google_client_id),
        )
            .setNonce(generateGoogleSignInNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                context = this,
                request = request,
            )
            val credential = result.credential
            Log.d(GoogleSignInTag, "Credential Manager returned ${credential::class.simpleName} type=${credential.type}")

            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                if (googleCredential.idToken.isBlank()) {
                    Log.w(GoogleSignInTag, "Google credential did not include an ID token.")
                    return GoogleSignInResult.Failure("Google ID token missing. Check OAuth client configuration.")
                }
                Log.d(
                    GoogleSignInTag,
                    "Google ID token received for ${googleCredential.id}; length=${googleCredential.idToken.length}; ${googleCredential.idToken.safeGoogleTokenSummary()}",
                )
                GoogleSignInResult.Success(
                    idToken = googleCredential.idToken,
                    displayName = googleCredential.displayName,
                    email = googleCredential.id,
                    profilePictureUri = googleCredential.profilePictureUri?.toString(),
                )
            } else {
                GoogleSignInResult.Failure("Google returned an unsupported credential.")
            }
        } catch (_: GetCredentialCancellationException) {
            Log.d(GoogleSignInTag, "Google sign-in was cancelled.")
            GoogleSignInResult.Cancelled
        } catch (error: GoogleIdTokenParsingException) {
            Log.e(GoogleSignInTag, "Google returned an invalid ID token credential.", error)
            GoogleSignInResult.Failure("Google returned an invalid sign-in response. Please update Google Play services and try again.")
        } catch (error: GetCredentialException) {
            Log.e(GoogleSignInTag, "Credential Manager failed.", error)
            GoogleSignInResult.Failure(error.message ?: "Google sign-in failed.")
        } catch (error: IllegalArgumentException) {
            Log.e(GoogleSignInTag, "Google sign-in response was invalid.", error)
            GoogleSignInResult.Failure(error.message ?: "Google sign-in response was invalid.")
        }
    }

    private fun generateGoogleSignInNonce(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun String.safeGoogleTokenSummary(): String =
        runCatching {
            val payload = split('.').getOrNull(1).orEmpty()
            val paddedPayload = payload.padEnd(payload.length + (4 - payload.length % 4) % 4, '=')
            val json = JSONObject(
                String(Base64.decode(paddedPayload, Base64.URL_SAFE or Base64.NO_WRAP)),
            )
            "aud=${json.optString("aud", "missing")} iss=${json.optString("iss", "missing")} " +
                "emailVerified=${json.opt("email_verified") ?: "missing"} subPresent=${json.optString("sub").isNotBlank()}"
        }.getOrDefault("claims=unreadable")
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
