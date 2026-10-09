package dev.orestegabo.sequo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dev.orestegabo.sequo.core.auth.GoogleSignInResult
import dev.orestegabo.sequo.feature.settings.AppLanguage
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.security.SecureRandom
import kotlin.coroutines.resume

private const val GoogleSignInTag = "SequoGoogleSignIn"
private const val SequoNotificationChannelId = "sequo_home_updates_v2"
private const val HomeWelcomeNotificationId = 1001
private const val OpenNotificationsAction = "dev.orestegabo.sequo.OPEN_NOTIFICATIONS"
private const val NotificationPreferencesName = "sequo_notification_preferences"
private const val HomeWelcomeNotificationShownKey = "home_welcome_notification_shown"

class MainActivity : ComponentActivity() {
    private val credentialManager by lazy {
        CredentialManager.create(this)
    }
    private val secureRandom = SecureRandom()
    private val openNotificationsRequest = mutableStateOf(0)
    private var legacyGoogleSignInContinuation: CancellableContinuation<GoogleSignInResult>? = null
    private val notificationPreferences by lazy {
        getSharedPreferences(NotificationPreferencesName, Context.MODE_PRIVATE)
    }
    private var homeNotificationPending = false
    private var homeNotificationPendingLanguage = AppLanguage.English
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && homeNotificationPending) {
            homeNotificationPending = false
            showHomeWelcomeNotification(homeNotificationPendingLanguage)
        }
    }
    private val legacyGoogleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        completeLegacyGoogleSignIn(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        handleNotificationIntent(intent)

        setContent {
            App(
                onGoogleSignIn = ::signInWithGoogle,
                onGoogleSignOut = {
                    try {
                        credentialManager.clearCredentialState(ClearCredentialStateRequest())
                    } catch (error: Throwable) {
                        Log.w(GoogleSignInTag, "Could not clear Google credential state.", error)
                    }
                },
                onHomeEntered = ::notifyHomeReached,
                openNotificationsRequest = openNotificationsRequest.value,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.action == OpenNotificationsAction) {
            openNotificationsRequest.value += 1
        }
    }

    private fun notifyHomeReached(language: AppLanguage) {
        if (hasShownHomeWelcomeNotification()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            homeNotificationPending = true
            homeNotificationPendingLanguage = language
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        showHomeWelcomeNotification(language)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            SequoNotificationChannelId,
            "Sequo updates",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Helpful Sequo updates, guest-safe notices, and account reminders."
            enableVibration(true)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun showHomeWelcomeNotification(language: AppLanguage = AppLanguage.English) {
        if (hasShownHomeWelcomeNotification()) return
        markHomeWelcomeNotificationShown()

        val openNotificationsIntent = Intent(this, MainActivity::class.java).apply {
            action = OpenNotificationsAction
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openNotificationsPendingIntent = PendingIntent.getActivity(
            this,
            HomeWelcomeNotificationId,
            openNotificationsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = when (language) {
            AppLanguage.French -> "Bienvenue sur Sequo"
            AppLanguage.English -> "Welcome to Sequo"
        }
        val body = when (language) {
            AppLanguage.French -> "Vous pouvez parcourir les produits en invite. Connectez-vous quand vous voulez enregistrer, commander ou suivre."
            AppLanguage.English -> "You can browse products as a guest. Sign in only when you are ready to save, order, or track."
        }
        val longBody = when (language) {
            AppLanguage.French -> "Vous pouvez parcourir les produits en invite. Connectez-vous seulement quand vous etes pret a enregistrer un panier, passer commande, suivre la livraison ou gerer vos details prives."
            AppLanguage.English -> "You can browse products as a guest. Sign in only when you are ready to save a basket, place an order, track delivery, or manage private account details."
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(this, SequoNotificationChannelId)
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(this)
        }
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(openNotificationsPendingIntent)
            .setCategory(android.app.Notification.CATEGORY_STATUS)
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .setDefaults(android.app.Notification.DEFAULT_ALL)
            .setStyle(
                android.app.Notification.BigTextStyle().bigText(
                    longBody,
                ),
            )
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(HomeWelcomeNotificationId, notification)
    }

    private fun hasShownHomeWelcomeNotification(): Boolean =
        notificationPreferences.getBoolean(HomeWelcomeNotificationShownKey, false)

    private fun markHomeWelcomeNotificationShown() {
        notificationPreferences.edit()
            .putBoolean(HomeWelcomeNotificationShownKey, true)
            .apply()
    }

    private suspend fun signInWithGoogle(): GoogleSignInResult {
        val googleIdOption = GetSignInWithGoogleOption.Builder(
            getString(R.string.google_server_client_id),
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
        } catch (error: GetCredentialCancellationException) {
            Log.e(GoogleSignInTag, "Credential Manager cancelled Google sign-in after account selection.", error)
            if (error.message?.contains("Account reauth failed", ignoreCase = true) == true) {
                Log.w(GoogleSignInTag, "Retrying Google sign-in with Play Services fallback.")
                signInWithLegacyGoogleClient()
            } else {
                GoogleSignInResult.Failure(
                    error.message?.takeIf { it.isNotBlank() }
                        ?: "Google sign-in was cancelled or interrupted. Please try again.",
                )
            }
        } catch (error: GoogleIdTokenParsingException) {
            Log.e(GoogleSignInTag, "Google returned an invalid ID token credential.", error)
            GoogleSignInResult.Failure("Google returned an invalid sign-in response. Please update Google Play services and try again.")
        } catch (error: GetCredentialException) {
            Log.e(GoogleSignInTag, "Credential Manager failed.", error)
            GoogleSignInResult.Failure(error.message ?: "Google sign-in failed.")
        } catch (error: IllegalArgumentException) {
            Log.e(GoogleSignInTag, "Google sign-in response was invalid.", error)
            GoogleSignInResult.Failure(error.message ?: "Google sign-in response was invalid.")
        } catch (error: Throwable) {
            Log.e(GoogleSignInTag, "Unexpected Google sign-in failure.", error)
            GoogleSignInResult.Failure(error.message ?: "Google sign-in failed unexpectedly.")
        }
    }

    private suspend fun signInWithLegacyGoogleClient(): GoogleSignInResult =
        suspendCancellableCoroutine { continuation ->
            if (legacyGoogleSignInContinuation != null) {
                continuation.resume(GoogleSignInResult.Failure("Google sign-in is already running."))
                return@suspendCancellableCoroutine
            }

            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_server_client_id))
                .requestEmail()
                .requestProfile()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

            continuation.invokeOnCancellation {
                if (legacyGoogleSignInContinuation === continuation) {
                    legacyGoogleSignInContinuation = null
                }
            }

            googleSignInClient.signOut().addOnCompleteListener {
                if (!continuation.isActive) return@addOnCompleteListener
                legacyGoogleSignInContinuation = continuation
                runCatching {
                    legacyGoogleSignInLauncher.launch(googleSignInClient.signInIntent)
                }.onFailure { error ->
                    if (legacyGoogleSignInContinuation === continuation) {
                        legacyGoogleSignInContinuation = null
                    }
                    Log.e(GoogleSignInTag, "Could not launch Play Services Google sign-in fallback.", error)
                    continuation.resume(
                        GoogleSignInResult.Failure(error.message ?: "Google sign-in could not be opened."),
                    )
                }
            }
        }

    private fun completeLegacyGoogleSignIn(result: ActivityResult) {
        val continuation = legacyGoogleSignInContinuation ?: return
        legacyGoogleSignInContinuation = null
        if (!continuation.isActive) return

        val account = try {
            GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
        } catch (error: ApiException) {
            Log.e(GoogleSignInTag, "Play Services Google sign-in failed with status ${error.statusCode}.", error)
            continuation.resume(
                if (result.data == null) {
                    GoogleSignInResult.Cancelled
                } else {
                    GoogleSignInResult.Failure(error.message ?: "Google sign-in failed.")
                },
            )
            return
        } catch (error: Throwable) {
            Log.e(GoogleSignInTag, "Play Services Google sign-in failed.", error)
            continuation.resume(GoogleSignInResult.Failure(error.message ?: "Google sign-in failed."))
            return
        }

        val idToken = account.idToken
        if (idToken.isNullOrBlank()) {
            Log.w(GoogleSignInTag, "Play Services Google sign-in did not return an ID token.")
            continuation.resume(GoogleSignInResult.Failure("Google ID token missing. Check OAuth client configuration."))
            return
        }

        Log.d(
            GoogleSignInTag,
            "Play Services Google ID token received for ${account.email}; length=${idToken.length}; ${idToken.safeGoogleTokenSummary()}",
        )
        continuation.resume(
            GoogleSignInResult.Success(
                idToken = idToken,
                displayName = account.displayName,
                email = account.email,
                profilePictureUri = account.photoUrl?.toString(),
            ),
        )
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
