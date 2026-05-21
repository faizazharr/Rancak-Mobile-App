package id.rancak.app.presentation.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.presentation.components.ErrorBanner
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakTextField
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.ForgotPasswordUiState
import id.rancak.app.presentation.viewmodel.ForgotPasswordViewModel
import org.koin.compose.viewmodel.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Entry point — menghubungkan ForgotPasswordViewModel ke UI content.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onNavigateToResetPassword: () -> Unit = {}
) {
    val viewModel: ForgotPasswordViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForgotPasswordContent(
        uiState                  = uiState,
        onEmailChange            = viewModel::onEmailChange,
        onSend                   = viewModel::sendResetLink,
        onClearError             = viewModel::clearError,
        onBack                   = onBack,
        onNavigateToResetPassword = onNavigateToResetPassword
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Pure UI — form email + status sukses
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun ForgotPasswordContent(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onClearError: () -> Unit = {},
    onBack: () -> Unit = {},
    onNavigateToResetPassword: () -> Unit = {}
) {
    val emailError = when {
        uiState.email.isBlank() -> null
        !uiState.email.contains("@") || !uiState.email.contains(".") -> "Format email tidak valid"
        else -> null
    }
    val canSend = uiState.email.isNotBlank() && emailError == null && !uiState.isLoading

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RancakTopBar(
                title    = "Lupa Password",
                icon     = Icons.AutoMirrored.Filled.ArrowBack,
                onMenu   = onBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            ErrorBanner(
                error     = uiState.error,
                onDismiss = onClearError,
                modifier  = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.isSuccess) {
                    SuccessState(email = uiState.email, onBack = onBack, onNavigateToResetPassword = onNavigateToResetPassword)
                } else {
                    FormState(
                        uiState      = uiState,
                        emailError   = emailError,
                        canSend      = canSend,
                        onEmailChange = onEmailChange,
                        onSend       = onSend,
                        onNavigateToResetPassword = onNavigateToResetPassword
                    )
                }
            }
        }
    }
}

@Composable
private fun FormState(
    uiState: ForgotPasswordUiState,
    emailError: String?,
    canSend: Boolean,
    onEmailChange: (String) -> Unit,
    onSend: () -> Unit,
    onNavigateToResetPassword: () -> Unit = {}
) {
    Icon(
        imageVector        = Icons.Default.Email,
        contentDescription = null,
        modifier           = Modifier.size(56.dp),
        tint               = MaterialTheme.colorScheme.primary
    )
    Text(
        "Reset Password",
        style      = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign  = TextAlign.Center
    )
    Text(
        "Masukkan email yang terdaftar. Kami akan mengirimkan tautan untuk mereset password Anda.",
        style     = MaterialTheme.typography.bodyMedium,
        color     = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    RancakTextField(
        value         = uiState.email,
        onValueChange = onEmailChange,
        label         = "Email",
        isError       = emailError != null,
        errorMessage  = emailError,
        leadingIcon   = { Icon(Icons.Default.Email, contentDescription = null) }
    )

    RancakButton(
        text      = "Kirim Tautan Reset",
        onClick   = onSend,
        isLoading = uiState.isLoading,
        enabled   = canSend,
        modifier  = Modifier.fillMaxWidth()
    )

    TextButton(onClick = onNavigateToResetPassword) {
        Text(
            "Sudah punya kode reset? Reset sekarang",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SuccessState(email: String, onBack: () -> Unit, onNavigateToResetPassword: () -> Unit = {}) {
    Icon(
        imageVector        = Icons.Default.MarkEmailRead,
        contentDescription = null,
        modifier           = Modifier.size(64.dp),
        tint               = MaterialTheme.colorScheme.primary
    )
    Text(
        "Email Terkirim!",
        style      = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign  = TextAlign.Center
    )
    Text(
        "Tautan reset password telah dikirim ke $email. Periksa kotak masuk atau folder spam Anda.",
        style     = MaterialTheme.typography.bodyMedium,
        color     = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    RancakButton(
        text     = "Kembali ke Login",
        onClick  = onBack,
        modifier = Modifier.fillMaxWidth()
    )

    TextButton(onClick = onNavigateToResetPassword) {
        Text(
            "Reset password dengan kode",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "ForgotPassword – Form", widthDp = 390, heightDp = 844)
@Composable
private fun ForgotPasswordFormPreview() {
    RancakTheme {
        ForgotPasswordContent(
            uiState = ForgotPasswordUiState(email = "user@example.com")
        )
    }
}

@Preview(name = "ForgotPassword – Success", widthDp = 390, heightDp = 844)
@Composable
private fun ForgotPasswordSuccessPreview() {
    RancakTheme {
        ForgotPasswordContent(
            uiState = ForgotPasswordUiState(
                email     = "user@example.com",
                isSuccess = true
            )
        )
    }
}
