package id.rancak.app.presentation.ui.auth.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import id.rancak.app.presentation.viewmodel.LoginUiState

/**
 * Orchestrator yang me-slide antara [LoginOptionsStep] dan [EmailFormStep]
 * sesuai flag `showEmailForm`.
 */
@Composable
internal fun LoginContent(
    uiState: LoginUiState,
    showEmailForm: Boolean,
    passwordVisible: Boolean,
    onPasswordToggle: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onGoogleToken: (String) -> Unit,
    onGoogleError: (String) -> Unit,
    onShowEmailForm: () -> Unit,
    onBackToOptions: () -> Unit
) {
    AnimatedContent(
        targetState = showEmailForm,
        transitionSpec = {
            if (targetState) {
                slideInHorizontally(tween(260)) { it } + fadeIn(tween(260)) togetherWith
                    slideOutHorizontally(tween(200)) { -it } + fadeOut(tween(200))
            } else {
                slideInHorizontally(tween(260)) { -it } + fadeIn(tween(260)) togetherWith
                    slideOutHorizontally(tween(200)) { it } + fadeOut(tween(200))
            }
        },
        label = "login_step"
    ) { isEmailForm ->
        if (isEmailForm) {
            EmailFormStep(
                uiState          = uiState,
                passwordVisible  = passwordVisible,
                onPasswordToggle = onPasswordToggle,
                onEmailChange    = onEmailChange,
                onPasswordChange = onPasswordChange,
                onLogin          = onLogin,
                onBack           = onBackToOptions
            )
        } else {
            LoginOptionsStep(
                uiState       = uiState,
                onEmailClick  = onShowEmailForm,
                onGoogleToken = onGoogleToken,
                onGoogleError = onGoogleError
            )
        }
    }
}
