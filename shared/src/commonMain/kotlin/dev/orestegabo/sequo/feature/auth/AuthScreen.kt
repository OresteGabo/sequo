package dev.orestegabo.sequo.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.designsystem.component.Package2
import dev.orestegabo.sequo.core.designsystem.component.SequoShapes
import dev.orestegabo.sequo.feature.settings.AppLanguage
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.Res
import sequo.shared.generated.resources.auth_fingerprint
import sequo.shared.generated.resources.onboarding_history
import sequo.shared.generated.resources.onboarding_pickup_flow
import sequo.shared.generated.resources.onboarding_scan_arrivals
import sequo.shared.generated.resources.sequo_icon_green

@Composable
fun AuthScreen(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    emailAuthInProgress: Boolean = false,
    onEmailLogin: (email: String, password: String) -> Unit,
    onEmailSignUp: (email: String, password: String, name: String) -> Unit,
    googleSignInInProgress: Boolean = false,
    onGoogleLogin: () -> Unit,
    onAppleLogin: () -> Unit,
    onSkipAuth: () -> Unit,
    onPrivacyTermsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showEmailFallback by rememberSaveable { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primaryContainer.copy(alpha = 0.86f),
                        colorScheme.background,
                        colorScheme.surfaceContainerLow,
                    ),
                ),
            ),
    ) {
        AuthBackgroundIcons()

        if (showEmailFallback) {
            EmailFallbackPage(
                language = language,
                onLanguageChange = onLanguageChange,
                onBack = { showEmailFallback = false },
                emailAuthInProgress = emailAuthInProgress,
                onEmailLogin = onEmailLogin,
                onEmailSignUp = onEmailSignUp,
                googleSignInInProgress = googleSignInInProgress,
                onGoogleLogin = onGoogleLogin,
                onAppleLogin = onAppleLogin,
                onSkipAuth = onSkipAuth,
                onPrivacyTermsClick = onPrivacyTermsClick,
            )
        } else {
            SocialAuthPage(
                language = language,
                onLanguageChange = onLanguageChange,
                onAppleLogin = onAppleLogin,
                googleSignInInProgress = googleSignInInProgress,
                onGoogleLogin = onGoogleLogin,
                onEmailFallback = { showEmailFallback = true },
                onSkipAuth = onSkipAuth,
                onPrivacyTermsClick = onPrivacyTermsClick,
            )
        }
    }
}

@Composable
private fun SocialAuthPage(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onAppleLogin: () -> Unit,
    googleSignInInProgress: Boolean,
    onGoogleLogin: () -> Unit,
    onEmailFallback: () -> Unit,
    onSkipAuth: () -> Unit,
    onPrivacyTermsClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 22.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        BrandMark(
            language = language,
            onLanguageChange = onLanguageChange,
        )

        Column(verticalArrangement = Arrangement.Bottom) {
            OnboardingPager()
            Spacer(modifier = Modifier.size(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                AppleButton(onClick = onAppleLogin)
                GoogleButton(
                    loading = googleSignInInProgress,
                    onClick = onGoogleLogin,
                )

                Text(
                    text = "Use email instead",
                    color = colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onEmailFallback)
                        .padding(top = 2.dp, bottom = 6.dp),
                )

                GuestBrowseLink(onClick = onSkipAuth)

                TermsLine(onPrivacyTermsClick = onPrivacyTermsClick)
            }
        }
    }
}

@Composable
private fun OnboardingPager() {
    val slides = listOf(
        OnboardingSlide(
            title = "Scan every package",
            subtitle = "New seller drop-offs, courier arrivals, customer pickups, and returns all start from one QR scan.",
            illustration = Res.drawable.onboarding_scan_arrivals,
        ),
        OnboardingSlide(
            title = "Know the next action",
            subtitle = "See the right flow immediately: store a package, collect fees, open a locker, or receive a return.",
            illustration = Res.drawable.onboarding_pickup_flow,
        ),
        OnboardingSlide(
            title = "Keep the hub traceable",
            subtitle = "Follow locker status, sync state, pickups, and returns in one simple history.",
            illustration = Res.drawable.onboarding_history,
        ),
    )
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val colorScheme = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 14.dp,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            OnboardingCard(slide = slides[page])
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            slides.indices.forEach { index ->
                val selected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = if (selected) 22.dp else 8.dp, height = 8.dp)
                        .background(
                            color = if (selected) colorScheme.primary else colorScheme.outlineVariant,
                            shape = SequoShapes.IconCapsule,
                        ),
                )
            }
        }
    }
}

@Composable
private fun OnboardingCard(slide: OnboardingSlide) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Image(
            painter = painterResource(slide.illustration),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(166.dp),
            contentScale = ContentScale.Fit,
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = slide.title,
                color = colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = slide.subtitle,
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val illustration: DrawableResource,
)

@Composable
private fun EmailFallbackPage(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onBack: () -> Unit,
    emailAuthInProgress: Boolean,
    onEmailLogin: (email: String, password: String) -> Unit,
    onEmailSignUp: (email: String, password: String, name: String) -> Unit,
    googleSignInInProgress: Boolean,
    onGoogleLogin: () -> Unit,
    onAppleLogin: () -> Unit,
    onSkipAuth: () -> Unit,
    onPrivacyTermsClick: () -> Unit,
) {
    var authMode by rememberSaveable { mutableStateOf(AuthMode.SignIn) }
    var authStep by rememberSaveable { mutableStateOf(AuthStep.Email) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var biometricConsent by rememberSaveable { mutableStateOf(true) }
    val colorScheme = MaterialTheme.colorScheme
    val isSignUp = authMode == AuthMode.SignUp
    val emailError = emailValidationError(email)
    val provider = authProviderForEmail(email)
    val canContinue = email.isNotBlank() && emailError == null
    val birthDateError = birthDateValidationError(birthDate)
    val nameError = nameValidationError(name)
    val passwordError = signUpPasswordValidationError(password)
    val confirmPasswordError = confirmPasswordValidationError(password, confirmPassword)
    val canSubmitCredentials = if (isSignUp) {
        name.isNotBlank() &&
            nameError == null &&
        birthDate.isNotBlank() &&
            birthDateError == null &&
            password.isNotBlank() &&
            passwordError == null &&
            confirmPassword.isNotBlank() &&
            confirmPasswordError == null
    } else {
        password.isNotBlank()
    }
    val providerHint = when (provider) {
        AuthProvider.Google -> "Gmail accounts continue securely with Google."
        AuthProvider.Apple -> "iCloud, Me, and Mac accounts continue securely with Apple."
        AuthProvider.Email -> if (isSignUp) {
            "We will start account setup for this email."
        } else {
            "We will continue with email for this account."
        }
    }
    val submitEmailCredentials = {
        if (isSignUp) {
            onEmailSignUp(email, password, name)
        } else {
            onEmailLogin(email, password)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        EmailHeader(
            language = language,
            onLanguageChange = onLanguageChange,
            onBack = onBack,
        )

        Column(verticalArrangement = Arrangement.Bottom) {
            Image(
                painter = painterResource(Res.drawable.auth_fingerprint),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(164.dp),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.size(18.dp))

            AuthModeTabs(
                selectedMode = authMode,
                onModeSelected = {
                    authMode = it
                    authStep = AuthStep.Email
                    password = ""
                    confirmPassword = ""
                    birthDate = ""
                    name = ""
                    biometricConsent = true
                },
            )

            Spacer(modifier = Modifier.size(18.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = when {
                        authStep == AuthStep.Credentials && isSignUp -> "Secure your account"
                        authStep == AuthStep.Credentials -> "Welcome back"
                        isSignUp -> "Create your account"
                        else -> "Find your account"
                    },
                    color = colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = when {
                        authStep == AuthStep.Credentials && isSignUp -> {
                            "Add your birth date and choose a strong password to finish setup."
                        }
                        authStep == AuthStep.Credentials -> {
                            "Enter your password to continue."
                        }
                        isSignUp -> {
                            "Enter your email and we will route you to the safest sign-up method."
                        }
                        else -> {
                            "Enter your email and we will route you to the right sign-in method."
                        }
                    },
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.size(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (authStep == AuthStep.Email) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = email,
                        onValueChange = {
                            email = it.trim()
                            authStep = AuthStep.Email
                        },
                        singleLine = true,
                        label = { Text("Email address") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.AlternateEmail,
                                contentDescription = null,
                                tint = colorScheme.primary,
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.Email,
                            imeAction = if (provider == AuthProvider.Email) ImeAction.Next else ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                if (canContinue && provider == AuthProvider.Email) {
                                    authStep = AuthStep.Credentials
                                }
                            },
                            onDone = {
                                if (canContinue) {
                                    when (provider) {
                                        AuthProvider.Google -> onGoogleLogin()
                                        AuthProvider.Apple -> onAppleLogin()
                                        AuthProvider.Email -> authStep = AuthStep.Credentials
                                    }
                                }
                            },
                        ),
                        isError = emailError != null,
                        supportingText = {
                            Text(emailError ?: providerHint)
                        },
                        shape = SequoShapes.Small,
                        colors = emailFieldColors(),
                    )

                    Spacer(modifier = Modifier.size(2.dp))

                    AuthProviderActionButton(
                        provider = provider,
                        mode = authMode,
                        enabled = canContinue && !googleSignInInProgress,
                        googleSignInInProgress = googleSignInInProgress,
                        onEmail = { authStep = AuthStep.Credentials },
                        onGoogle = onGoogleLogin,
                        onApple = onAppleLogin,
                    )
                } else {
                    EmailCredentialsForm(
                        mode = authMode,
                        name = name,
                        onNameChange = { name = it },
                        nameError = nameError,
                        email = email,
                        birthDate = birthDate,
                        onBirthDateChange = { birthDate = cleanBirthDateInput(it) },
                        birthDateError = birthDateError,
                        password = password,
                        onPasswordChange = { password = it },
                        passwordError = passwordError,
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = { confirmPassword = it },
                        confirmPasswordError = confirmPasswordError,
                        biometricConsent = biometricConsent,
                        onBiometricConsentChange = { biometricConsent = it },
                        canSubmit = canSubmitCredentials && !emailAuthInProgress,
                        onSubmit = submitEmailCredentials,
                    )

                    Button(
                        onClick = submitEmailCredentials,
                        enabled = canSubmitCredentials && !emailAuthInProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = SequoShapes.Small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary,
                        ),
                    ) {
                        if (emailAuthInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = if (emailAuthInProgress) "Please wait..." else if (isSignUp) "Create account" else "Sign in",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Text(
                    text = if (authStep == AuthStep.Credentials) "Change email" else "Back to Apple or Google",
                    color = colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = {
                                if (authStep == AuthStep.Credentials) {
                                    authStep = AuthStep.Email
                                    password = ""
                                    confirmPassword = ""
                                    birthDate = ""
                                    name = ""
                                    biometricConsent = true
                                } else {
                                    onBack()
                                }
                            },
                        )
                        .padding(vertical = 4.dp),
                )

                TermsLine(onPrivacyTermsClick = onPrivacyTermsClick)

                GuestBrowseLink(onClick = onSkipAuth)
            }
        }
    }
}

@Composable
private fun GuestBrowseLink(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.QrCodeScanner,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Browse catalog first",
            color = colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmailCredentialsForm(
    mode: AuthMode,
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    email: String,
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    birthDateError: String?,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    confirmPasswordError: String?,
    biometricConsent: Boolean,
    onBiometricConsentChange: (Boolean) -> Unit,
    canSubmit: Boolean,
    onSubmit: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isSignUp = mode == AuthMode.SignUp
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SequoShapes.Small,
        color = colorScheme.surface.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.42f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = email,
                color = colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (isSignUp) "Email sign-up" else "Email sign-in",
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }

    if (isSignUp) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            label = { Text("Full name") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = colorScheme.primary,
                )
            },
            isError = nameError != null,
            supportingText = {
                Text(nameError ?: "This name will appear in your Sequo profile.")
            },
            shape = SequoShapes.Small,
            colors = emailFieldColors(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = birthDate,
            onValueChange = onBirthDateChange,
            singleLine = true,
            label = { Text("Birth date") },
            placeholder = { Text("JJ/MM/AAAA") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = colorScheme.primary,
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            isError = birthDateError != null,
            supportingText = {
                Text(birthDateError ?: "Format: JJ/MM/AAAA.")
            },
            shape = SequoShapes.Small,
            colors = emailFieldColors(),
        )
    }

    PasswordTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = "Password",
        isError = isSignUp && passwordError != null,
        supportingText = if (isSignUp) {
            passwordError ?: "At least 10 characters with upper, lower, number, and symbol."
        } else {
            null
        },
        imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done,
        onImeAction = {
            if (isSignUp) {
                focusManager.moveFocus(FocusDirection.Down)
            } else {
                focusManager.clearFocus()
                if (canSubmit) onSubmit()
            }
        },
    )

    if (isSignUp) {
        PasswordTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirm password",
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError ?: "Repeat the same password.",
            imeAction = ImeAction.Done,
            onImeAction = {
                focusManager.clearFocus()
                if (canSubmit) onSubmit()
            },
        )
    } else {
        BiometricConsentRow(
            checked = biometricConsent,
            onCheckedChange = onBiometricConsentChange,
        )
    }
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    supportingText: String?,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = colorScheme.primary,
            )
        },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Hide password" else "Show password",
                    tint = colorScheme.onSurfaceVariant,
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() },
        ),
        isError = isError,
        supportingText = supportingText?.let { text -> { Text(text) } },
        shape = SequoShapes.Small,
        colors = emailFieldColors(),
    )
}

@Composable
private fun BiometricConsentRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Icon(
            imageVector = Icons.Filled.Fingerprint,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = "Use biometric unlock on this device",
            color = colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AuthModeTabs(
    selectedMode: AuthMode,
    onModeSelected: (AuthMode) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = SequoShapes.NavItem,
        color = colorScheme.surface.copy(alpha = 0.68f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.58f)),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuthMode.entries.forEach { mode ->
                val selected = mode == selectedMode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    onClick = { onModeSelected(mode) },
                    shape = SequoShapes.IconCapsule,
                    color = if (selected) colorScheme.primary else Color.Transparent,
                    contentColor = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                    tonalElevation = if (selected) 2.dp else 0.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthProviderActionButton(
    provider: AuthProvider,
    mode: AuthMode,
    enabled: Boolean,
    googleSignInInProgress: Boolean,
    onEmail: () -> Unit,
    onGoogle: () -> Unit,
    onApple: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val label = when (provider) {
        AuthProvider.Google -> if (googleSignInInProgress) "Signing in..." else "Continue with Google"
        AuthProvider.Apple -> "Continue with Apple"
        AuthProvider.Email -> if (mode == AuthMode.SignUp) "Continue with email" else "Continue"
    }
    val icon = when (provider) {
        AuthProvider.Google -> GoogleIcon
        AuthProvider.Apple -> AppleIcon
        AuthProvider.Email -> Icons.Filled.AlternateEmail
    }
    val onClick = when (provider) {
        AuthProvider.Google -> onGoogle
        AuthProvider.Apple -> onApple
        AuthProvider.Email -> onEmail
    }
    val containerColor = when (provider) {
        AuthProvider.Apple -> Color(0xFF090A0C)
        else -> colorScheme.primary
    }
    val contentColor = when (provider) {
        AuthProvider.Apple -> Color.White
        else -> colorScheme.onPrimary
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = SequoShapes.Small,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        if (provider == AuthProvider.Google && googleSignInInProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (provider == AuthProvider.Google) Color.Unspecified else contentColor,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EmailHeader(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onBack: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                onClick = onBack,
                shape = SequoShapes.Card,
                color = colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "Sequo",
                    color = colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                )
                Text(
                    text = "Shopping app",
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        LanguageFlagSwitch(
            language = language,
            onLanguageChange = onLanguageChange,
        )
    }
}

@Composable
private fun emailFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.74f),
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.78f),
    cursorColor = MaterialTheme.colorScheme.primary,
)

private enum class AuthStep {
    Email,
    Credentials,
}

private enum class AuthMode(val label: String) {
    SignIn("Sign in"),
    SignUp("Sign up"),
}

private enum class AuthProvider {
    Email,
    Google,
    Apple,
}

private fun emailValidationError(email: String): String? {
    if (email.isBlank()) return null
    val trimmed = email.trim()
    val hasSingleAt = trimmed.count { it == '@' } == 1
    val domain = trimmed.substringAfter('@', missingDelimiterValue = "")
    val isValid = hasSingleAt &&
        trimmed.indexOf('@') > 0 &&
        domain.contains('.') &&
        !trimmed.contains(' ') &&
        domain.substringAfterLast('.').length >= 2
    return if (isValid) null else "Enter a valid email address."
}

private fun cleanBirthDateInput(value: String): String {
    val digits = value.filter(Char::isDigit).take(8)
    return buildString {
        digits.forEachIndexed { index, char ->
            if (index == 2 || index == 4) append('/')
            append(char)
        }
    }
}

private fun birthDateValidationError(value: String): String? {
    if (value.isBlank()) return null
    val parts = value.split("/")
    if (parts.size != 3 || parts[0].length != 2 || parts[1].length != 2 || parts[2].length != 4) {
        return "Use the format JJ/MM/AAAA."
    }
    val day = parts[0].toIntOrNull() ?: return "Use a valid day."
    val month = parts[1].toIntOrNull() ?: return "Use a valid month."
    val year = parts[2].toIntOrNull() ?: return "Use a valid year."
    if (year !in 1900..2026) return "Use a valid year."
    if (month !in 1..12) return "Use a valid month."
    val maxDay = when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return if (day in 1..maxDay) null else "Use a valid day."
}

private fun nameValidationError(value: String): String? {
    if (value.isBlank()) return null
    val trimmed = value.trim()
    return when {
        trimmed.length < 2 -> "Enter at least 2 characters."
        trimmed.length > 80 -> "Name is too long."
        else -> null
    }
}

private fun isLeapYear(year: Int): Boolean =
    year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

private fun signUpPasswordValidationError(password: String): String? {
    if (password.isBlank()) return null
    val missing = buildList {
        if (password.length < 10) add("10 characters")
        if (password.none { it.isUpperCase() }) add("uppercase letter")
        if (password.none { it.isLowerCase() }) add("lowercase letter")
        if (password.none { it.isDigit() }) add("number")
        if (password.none { !it.isLetterOrDigit() }) add("symbol")
    }
    return if (missing.isEmpty()) null else "Missing: ${missing.joinToString()}."
}

private fun confirmPasswordValidationError(
    password: String,
    confirmPassword: String,
): String? {
    if (confirmPassword.isBlank()) return null
    return if (password == confirmPassword) null else "Passwords do not match."
}

private fun authProviderForEmail(email: String): AuthProvider {
    val domain = email.trim()
        .lowercase()
        .substringAfter('@', missingDelimiterValue = "")
    return when (domain) {
        "gmail.com", "googlemail.com" -> AuthProvider.Google
        "icloud.com", "me.com", "mac.com" -> AuthProvider.Apple
        else -> AuthProvider.Email
    }
}

@Composable
private fun BrandMark(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SequoMark(
                modifier = Modifier
                    .width(82.dp)
                    .height(82.dp),
            )
        }
        LanguageFlagSwitch(
            language = language,
            onLanguageChange = onLanguageChange,
        )
    }
}

@Composable
private fun SequoMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.sequo_icon_green),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

@Composable
private fun LanguageFlagSwitch(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val useEnglish = language == AppLanguage.English
    Surface(
        modifier = Modifier
            .width(82.dp)
            .height(44.dp),
        onClick = {
            onLanguageChange(
                if (useEnglish) {
                    AppLanguage.French
                } else {
                    AppLanguage.English
                },
            )
        },
        shape = SequoShapes.NavItem,
        color = colorScheme.surface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.68f)),
        tonalElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = if (useEnglish) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    FlagIcon(flag = LanguageFlag.French, dimmed = useEnglish)
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    FlagIcon(flag = LanguageFlag.British, dimmed = !useEnglish)
                }
            }
            Surface(
                modifier = Modifier.size(36.dp),
                shape = SequoShapes.IconCapsule,
                color = colorScheme.surface,
                border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.34f)),
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    FlagIcon(
                        flag = if (useEnglish) LanguageFlag.British else LanguageFlag.French,
                        dimmed = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun FlagIcon(
    flag: LanguageFlag,
    dimmed: Boolean,
) {
    val alpha = if (dimmed) 0.36f else 1f
    Canvas(
        modifier = Modifier
            .size(width = 22.dp, height = 16.dp)
            .clip(SequoShapes.Small),
    ) {
        when (flag) {
            LanguageFlag.French -> {
                drawRect(Color(0xFF1B3F8B).copy(alpha = alpha), size = Size(size.width / 3f, size.height))
                drawRect(
                    Color.White.copy(alpha = alpha),
                    topLeft = Offset(size.width / 3f, 0f),
                    size = Size(size.width / 3f, size.height),
                )
                drawRect(
                    Color(0xFFE23D3D).copy(alpha = alpha),
                    topLeft = Offset(size.width * 2f / 3f, 0f),
                    size = Size(size.width / 3f, size.height),
                )
            }
            LanguageFlag.British -> {
                drawRect(Color(0xFF163B7A).copy(alpha = alpha))
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 5.dp.toPx(),
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 5.dp.toPx(),
                )
                drawLine(
                    color = Color(0xFFC8102E).copy(alpha = alpha),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx(),
                )
                drawLine(
                    color = Color(0xFFC8102E).copy(alpha = alpha),
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2.dp.toPx(),
                )
                drawRect(
                    Color.White.copy(alpha = alpha),
                    topLeft = Offset(size.width / 2f - 2.5.dp.toPx(), 0f),
                    size = Size(5.dp.toPx(), size.height),
                )
                drawRect(
                    Color.White.copy(alpha = alpha),
                    topLeft = Offset(0f, size.height / 2f - 2.5.dp.toPx()),
                    size = Size(size.width, 5.dp.toPx()),
                )
                drawRect(
                    Color(0xFFC8102E).copy(alpha = alpha),
                    topLeft = Offset(size.width / 2f - 1.4.dp.toPx(), 0f),
                    size = Size(2.8.dp.toPx(), size.height),
                )
                drawRect(
                    Color(0xFFC8102E).copy(alpha = alpha),
                    topLeft = Offset(0f, size.height / 2f - 1.4.dp.toPx()),
                    size = Size(size.width, 2.8.dp.toPx()),
                )
            }
        }
    }
}

private enum class LanguageFlag {
    French,
    British,
}

@Composable
private fun AppleButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = SequoShapes.Small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF090A0C),
            contentColor = Color.White,
        ),
    ) {
        Icon(
            imageVector = AppleIcon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Continue with Apple",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GoogleButton(
    loading: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = SequoShapes.Small,
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.72f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colorScheme.surface.copy(alpha = 0.9f),
            contentColor = colorScheme.onSurface,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = colorScheme.primary,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = GoogleIcon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (loading) "Signing in..." else "Continue with Google",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TermsLine(onPrivacyTermsClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "By continuing you accept ",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Privacy & Terms",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onPrivacyTermsClick),
        )
    }
}

@Composable
private fun BoxScope.AuthBackgroundIcons() {
    val colorScheme = MaterialTheme.colorScheme
    BackgroundIcon(
        icon = Icons.Filled.Package2,
        contentDescription = null,
        tint = colorScheme.primary.copy(alpha = 0.11f),
        size = 132,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 28.dp, y = 88.dp)
            .rotate(14f),
    )
    BackgroundIcon(
        icon = Icons.Filled.QrCodeScanner,
        contentDescription = null,
        tint = colorScheme.tertiary.copy(alpha = 0.13f),
        size = 92,
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(x = (-26).dp, y = (-40).dp)
            .rotate(-12f),
    )
    BackgroundIcon(
        icon = Icons.Filled.Lock,
        contentDescription = null,
        tint = colorScheme.primary.copy(alpha = 0.08f),
        size = 116,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = 30.dp, y = (-130).dp)
            .rotate(-10f),
    )
}

@Composable
private fun BackgroundIcon(
    icon: ImageVector,
    contentDescription: String?,
    tint: Color,
    size: Int,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size.dp),
    )
}

private val GoogleIcon: ImageVector
    get() {
        if (_googleIcon != null) {
            return _googleIcon!!
        }
        _googleIcon = ImageVector.Builder(
            name = "Brand.Google",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF4285F4))) {
                moveTo(23.49f, 12.27f)
                curveTo(23.49f, 11.48f, 23.42f, 10.73f, 23.3f, 10f)
                horizontalLineTo(12f)
                verticalLineTo(14.51f)
                horizontalLineTo(18.47f)
                curveTo(18.19f, 15.96f, 17.36f, 17.19f, 16.11f, 18.02f)
                verticalLineTo(20.95f)
                horizontalLineTo(19.89f)
                curveTo(22.09f, 18.92f, 23.49f, 15.93f, 23.49f, 12.27f)
                close()
            }
            path(fill = SolidColor(Color(0xFF34A853))) {
                moveTo(12f, 24f)
                curveTo(15.24f, 24f, 17.95f, 22.93f, 19.89f, 20.95f)
                lineTo(16.11f, 18.02f)
                curveTo(15.06f, 18.72f, 13.72f, 19.13f, 12f, 19.13f)
                curveTo(8.87f, 19.13f, 6.22f, 17.02f, 5.27f, 14.18f)
                horizontalLineTo(1.36f)
                verticalLineTo(17.2f)
                curveTo(3.29f, 21.04f, 7.26f, 24f, 12f, 24f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFBBC05))) {
                moveTo(5.27f, 14.18f)
                curveTo(5.03f, 13.48f, 4.9f, 12.74f, 4.9f, 12f)
                curveTo(4.9f, 11.26f, 5.03f, 10.52f, 5.27f, 9.82f)
                verticalLineTo(6.8f)
                horizontalLineTo(1.36f)
                curveTo(0.57f, 8.38f, 0.11f, 10.14f, 0.11f, 12f)
                curveTo(0.11f, 13.86f, 0.57f, 15.62f, 1.36f, 17.2f)
                lineTo(5.27f, 14.18f)
                close()
            }
            path(fill = SolidColor(Color(0xFFEA4335))) {
                moveTo(12f, 4.87f)
                curveTo(13.76f, 4.87f, 15.35f, 5.48f, 16.59f, 6.67f)
                lineTo(19.97f, 3.29f)
                curveTo(17.94f, 1.4f, 15.23f, 0f, 12f, 0f)
                curveTo(7.26f, 0f, 3.29f, 2.96f, 1.36f, 6.8f)
                lineTo(5.27f, 9.82f)
                curveTo(6.22f, 6.98f, 8.87f, 4.87f, 12f, 4.87f)
                close()
            }
        }.build()
        return _googleIcon!!
    }

private var _googleIcon: ImageVector? = null

private val AppleIcon: ImageVector
    get() {
        if (_appleIcon != null) {
            return _appleIcon!!
        }
        _appleIcon = ImageVector.Builder(
            name = "Brand.Apple",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.05f, 12.54f)
                curveTo(17.02f, 9.95f, 19.16f, 8.71f, 19.25f, 8.65f)
                curveTo(18.04f, 6.88f, 16.17f, 6.64f, 15.53f, 6.62f)
                curveTo(13.96f, 6.46f, 12.44f, 7.56f, 11.64f, 7.56f)
                curveTo(10.83f, 7.56f, 9.61f, 6.64f, 8.29f, 6.67f)
                curveTo(6.59f, 6.7f, 5.0f, 7.69f, 4.13f, 9.25f)
                curveTo(2.33f, 12.36f, 3.67f, 16.94f, 5.39f, 19.45f)
                curveTo(6.25f, 20.67f, 7.25f, 22.04f, 8.55f, 21.99f)
                curveTo(9.82f, 21.94f, 10.3f, 21.18f, 11.84f, 21.18f)
                curveTo(13.37f, 21.18f, 13.82f, 21.99f, 15.15f, 21.96f)
                curveTo(16.52f, 21.94f, 17.38f, 20.73f, 18.21f, 19.49f)
                curveTo(19.2f, 18.08f, 19.6f, 16.68f, 19.62f, 16.61f)
                curveTo(19.58f, 16.59f, 17.08f, 15.63f, 17.05f, 12.54f)
                close()
                moveTo(14.5f, 4.95f)
                curveTo(15.2f, 4.09f, 15.67f, 2.93f, 15.54f, 1.75f)
                curveTo(14.53f, 1.8f, 13.27f, 2.45f, 12.54f, 3.29f)
                curveTo(11.89f, 4.04f, 11.31f, 5.25f, 11.47f, 6.38f)
                curveTo(12.61f, 6.47f, 13.77f, 5.79f, 14.5f, 4.95f)
                close()
            }
        }.build()
        return _appleIcon!!
    }

private var _appleIcon: ImageVector? = null
