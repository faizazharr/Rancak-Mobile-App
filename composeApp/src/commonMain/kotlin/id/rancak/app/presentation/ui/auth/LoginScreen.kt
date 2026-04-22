package id.rancak.app.presentation.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.components.ErrorBanner
import id.rancak.app.presentation.ui.auth.components.PhoneLoginLayout
import id.rancak.app.presentation.ui.auth.components.TabletLoginLayout
import id.rancak.app.presentation.viewmodel.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Entry point login — menghubungkan [LoginViewModel] ke layout responsif
 * (phone / tablet). Semua UI panel ada di `auth/components/`.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var showEmailForm   by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isTablet = maxWidth >= 600.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            ErrorBanner(
                error     = uiState.error,
                onDismiss = viewModel::clearError,
                modifier  = Modifier.fillMaxWidth()
            )
        }

        if (isTablet) {
            TabletLoginLayout(
                uiState          = uiState,
                showEmailForm    = showEmailForm,
                passwordVisible  = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible },
                onEmailChange    = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLogin          = viewModel::login,
                onGoogleToken    = viewModel::loginWithGoogle,
                onGoogleError    = viewModel::setError,
                onShowEmailForm  = { showEmailForm = true },
                onBackToOptions  = { showEmailForm = false }
            )
        } else {
            PhoneLoginLayout(
                uiState          = uiState,
                showEmailForm    = showEmailForm,
                passwordVisible  = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible },
                onEmailChange    = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLogin          = viewModel::login,
                onGoogleToken    = viewModel::loginWithGoogle,
                onGoogleError    = viewModel::setError,
                onShowEmailForm  = { showEmailForm = true },
                onBackToOptions  = { showEmailForm = false }
            )
        }
    }
}
