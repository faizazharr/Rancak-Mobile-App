package id.rancak.app.presentation.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.presentation.components.ErrorBanner
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakTextField
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.viewmodel.ResetPasswordUiState
import id.rancak.app.presentation.viewmodel.ResetPasswordViewModel
import org.koin.compose.viewmodel.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    val viewModel: ResetPasswordViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onSuccess()
    }

    ResetPasswordContent(
        uiState                = uiState,
        onTokenChange          = viewModel::onTokenChange,
        onNewPasswordChange    = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onReset                = viewModel::resetPassword,
        onClearError           = viewModel::clearError,
        onBack                 = onBack
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Pure UI
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun ResetPasswordContent(
    uiState: ResetPasswordUiState,
    onTokenChange: (String) -> Unit = {},
    onNewPasswordChange: (String) -> Unit = {},
    onConfirmPasswordChange: (String) -> Unit = {},
    onReset: () -> Unit = {},
    onClearError: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val passwordError = when {
        uiState.newPassword.isBlank() -> null
        uiState.newPassword.length < 8 -> "Minimal 8 karakter"
        else -> null
    }
    val confirmError = when {
        uiState.confirmPassword.isBlank() -> null
        uiState.confirmPassword != uiState.newPassword -> "Konfirmasi tidak cocok"
        else -> null
    }
    val canSubmit = uiState.token.isNotBlank() &&
        uiState.newPassword.length >= 8 &&
        uiState.newPassword == uiState.confirmPassword &&
        !uiState.isLoading

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RancakTopBar(
                title  = "Reset Password",
                icon   = Icons.AutoMirrored.Filled.ArrowBack,
                onMenu = onBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
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
                    SuccessState(onBack = onBack)
                } else {
                    ResetFormState(
                        uiState                 = uiState,
                        passwordError           = passwordError,
                        confirmError            = confirmError,
                        canSubmit               = canSubmit,
                        onTokenChange           = onTokenChange,
                        onNewPasswordChange     = onNewPasswordChange,
                        onConfirmPasswordChange = onConfirmPasswordChange,
                        onReset                 = onReset
                    )
                }
            }
        }
    }
}

@Composable
private fun ResetFormState(
    uiState: ResetPasswordUiState,
    passwordError: String?,
    confirmError: String?,
    canSubmit: Boolean,
    onTokenChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onReset: () -> Unit
) {
    Icon(
        imageVector        = Icons.Default.Key,
        contentDescription = null,
        modifier           = Modifier.size(56.dp),
        tint               = MaterialTheme.colorScheme.primary
    )
    Text(
        "Buat Password Baru",
        style      = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign  = TextAlign.Center
    )
    Text(
        "Masukkan kode yang dikirim ke email Anda beserta password baru yang diinginkan.",
        style     = MaterialTheme.typography.bodyMedium,
        color     = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    RancakTextField(
        value         = uiState.token,
        onValueChange = onTokenChange,
        label         = "Kode Reset (dari email)",
        leadingIcon   = { Icon(Icons.Default.Key, contentDescription = null) }
    )

    // Password fields use OutlinedTextField directly for visualTransformation support
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value         = uiState.newPassword,
            onValueChange = onNewPasswordChange,
            label         = { Text("Password Baru") },
            isError       = passwordError != null,
            singleLine    = true,
            leadingIcon   = { Icon(Icons.Default.Lock, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            shape    = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        if (passwordError != null) {
            Text(
                passwordError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value         = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label         = { Text("Konfirmasi Password") },
            isError       = confirmError != null,
            singleLine    = true,
            leadingIcon   = { Icon(Icons.Default.Lock, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            shape    = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        if (confirmError != null) {
            Text(
                confirmError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }

    RancakButton(
        text      = "Reset Password",
        onClick   = onReset,
        isLoading = uiState.isLoading,
        enabled   = canSubmit,
        modifier  = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SuccessState(onBack: () -> Unit) {
    Icon(
        imageVector        = Icons.Default.CheckCircle,
        contentDescription = null,
        modifier           = Modifier.size(64.dp),
        tint               = MaterialTheme.colorScheme.primary
    )
    Text(
        "Password Berhasil Diubah!",
        style      = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign  = TextAlign.Center
    )
    Text(
        "Password Anda telah berhasil diubah. Silakan login dengan password baru Anda.",
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
}
