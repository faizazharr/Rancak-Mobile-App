package id.rancak.app.presentation.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import rancak.composeapp.generated.resources.Res
import rancak.composeapp.generated.resources.tias_logo
import id.rancak.app.presentation.auth.GoogleSignInButton
import id.rancak.app.presentation.components.ErrorBanner
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakTextField
import id.rancak.app.presentation.viewmodel.LoginUiState
import id.rancak.app.presentation.viewmodel.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible  by remember { mutableStateOf(false) }
    var showEmailForm    by remember { mutableStateOf(false) }

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

// ─────────────────────────────────────────────────────────────────────────────
// TABLET — split panel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TabletLoginLayout(
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
    val primary   = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Row(Modifier.fillMaxSize()) {

        // ── Kiri: branding ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(0.52f)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(primary, primary.copy(alpha = 0.80f))
                    )
                )
        ) {
            // Dekorasi lingkaran
            Box(
                Modifier.size(340.dp).offset(x = (-90).dp, y = (-90).dp)
                    .clip(CircleShape).background(onPrimary.copy(alpha = 0.06f))
            )
            Box(
                Modifier.size(220.dp).align(Alignment.BottomEnd)
                    .offset(x = 70.dp, y = 70.dp)
                    .clip(CircleShape).background(onPrimary.copy(alpha = 0.07f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(52.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(onPrimary.copy(alpha = 0.17f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "R",
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = onPrimary,
                        fontSize   = 34.sp
                    )
                }
                Spacer(Modifier.height(32.dp))
                Text(
                    "Rancak",
                    style      = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color      = onPrimary,
                    fontSize   = 36.sp,
                    lineHeight = 42.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Kelola transaksi kasir\nlebih cepat dan akurat.",
                    style      = MaterialTheme.typography.titleMedium,
                    color      = onPrimary.copy(alpha = 0.76f),
                    lineHeight = 26.sp
                )
                Spacer(Modifier.height(48.dp))
                BrandingBullet(Icons.Default.Receipt,     "Kasir multi-device, satu dashboard")
                BrandingBullet(Icons.Default.ShoppingCart,"Stok otomatis tiap transaksi")
                BrandingBullet(Icons.Default.TrendingUp,  "Laporan real-time & shift harian")
            }

            // Footer — branding TIAS
            Row(
                modifier          = Modifier.align(Alignment.BottomStart).padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Powered by",
                    style = MaterialTheme.typography.labelSmall,
                    color = onPrimary.copy(alpha = 0.38f)
                )
                // Logo TIAS — latar gelap intentional (file PNG berlatar hitam)
                androidx.compose.foundation.Image(
                    painter            = painterResource(Res.drawable.tias_logo),
                    contentDescription = "TIAS",
                    contentScale       = ContentScale.FillHeight,
                    modifier           = Modifier
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        // ── Kanan: form ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(0.48f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()                                   // naik saat keyboard muncul
                    .padding(horizontal = 40.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Masuk",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Pilih cara masuk ke akun Anda",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(36.dp))
                LoginContent(
                    uiState          = uiState,
                    showEmailForm    = showEmailForm,
                    passwordVisible  = passwordVisible,
                    onPasswordToggle = onPasswordToggle,
                    onEmailChange    = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onLogin          = onLogin,
                    onGoogleToken    = onGoogleToken,
                    onGoogleError    = onGoogleError,
                    onShowEmailForm  = onShowEmailForm,
                    onBackToOptions  = onBackToOptions
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PHONE — centered single column
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PhoneLoginLayout(
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()                                       // naik saat keyboard muncul
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "R",
                style      = MaterialTheme.typography.displaySmall,
                color      = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Rancak",
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        // "Powered by TIAS" — tagline di bawah judul
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Powered by",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                fontSize = 10.sp
            )
            // Logo TIAS
            androidx.compose.foundation.Image(
                painter            = painterResource(Res.drawable.tias_logo),
                contentDescription = "TIAS",
                contentScale       = ContentScale.FillHeight,
                modifier           = Modifier
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Masuk ke akun Anda untuk melanjutkan",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        LoginContent(
            uiState          = uiState,
            showEmailForm    = showEmailForm,
            passwordVisible  = passwordVisible,
            onPasswordToggle = onPasswordToggle,
            onEmailChange    = onEmailChange,
            onPasswordChange = onPasswordChange,
            onLogin          = onLogin,
            onGoogleToken    = onGoogleToken,
            onGoogleError    = onGoogleError,
            onShowEmailForm  = onShowEmailForm,
            onBackToOptions  = onBackToOptions
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Login content — dua tahap: pilihan | form email
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoginContent(
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

// ─────────────────────────────────────────────────────────────────────────────
// Tahap 1: Pilihan login (Google | Email)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoginOptionsStep(
    uiState: LoginUiState,
    onEmailClick: () -> Unit,
    onGoogleToken: (String) -> Unit,
    onGoogleError: (String) -> Unit
) {
    val outline  = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary  = MaterialTheme.colorScheme.primary

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Google — outlined
        GoogleSignInButton(
            modifier  = Modifier.fillMaxWidth(),
            enabled   = !uiState.isLoading && !uiState.isGoogleLoading,
            onIdToken = onGoogleToken,
            onError   = onGoogleError
        )

        Spacer(Modifier.height(16.dp))

        // Divider "atau"
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                "  atau  ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // Continue with Email — filled primary
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(primary)
                .clickable(onClick = onEmailClick)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Email, null,
                    Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    "Lanjut dengan Email",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tahap 2: Form email + password
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmailFormStep(
    uiState: LoginUiState,
    passwordVisible: Boolean,
    onPasswordToggle: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tombol kembali
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ArrowBack, null,
                    Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "Masuk dengan Email",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(20.dp))

        RancakTextField(
            value         = uiState.email,
            onValueChange = onEmailChange,
            label         = "Email",
            leadingIcon   = { Icon(Icons.Default.Email, contentDescription = null) }
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value         = uiState.password,
            onValueChange = onPasswordChange,
            label         = { Text("Password") },
            leadingIcon   = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon  = {
                IconButton(onClick = onPasswordToggle) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction    = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onLogin() }),
            singleLine  = true,
            shape       = MaterialTheme.shapes.medium,
            modifier    = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(6.dp))

        // Lupa password — rata kanan
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = { /* forgot password */ }) {
                Text(
                    "Lupa Password?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        RancakButton(
            text      = "Masuk",
            onClick   = onLogin,
            isLoading = uiState.isLoading,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Branding bullet — hanya dipakai tablet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BrandingBullet(icon: ImageVector, label: String) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Row(
        modifier              = Modifier.padding(bottom = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(onPrimary.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(17.dp), tint = onPrimary)
        }
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = onPrimary.copy(alpha = 0.86f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun PhoneLoginPreview() {
    RancakTheme {
        PhoneLoginLayout(
            uiState          = LoginUiState(),
            showEmailForm    = false,
            passwordVisible  = false,
            onPasswordToggle = {},
            onEmailChange    = {},
            onPasswordChange = {},
            onLogin          = {},
            onGoogleToken    = {},
            onGoogleError    = {},
            onShowEmailForm  = {},
            onBackToOptions  = {}
        )
    }
}

@Preview
@Composable
private fun PhoneEmailFormPreview() {
    RancakTheme {
        PhoneLoginLayout(
            uiState          = LoginUiState(email = "user@example.com"),
            showEmailForm    = true,
            passwordVisible  = false,
            onPasswordToggle = {},
            onEmailChange    = {},
            onPasswordChange = {},
            onLogin          = {},
            onGoogleToken    = {},
            onGoogleError    = {},
            onShowEmailForm  = {},
            onBackToOptions  = {}
        )
    }
}

@Preview(widthDp = 1024, heightDp = 768)
@Composable
private fun TabletLoginPreview() {
    RancakTheme {
        TabletLoginLayout(
            uiState          = LoginUiState(),
            showEmailForm    = false,
            passwordVisible  = false,
            onPasswordToggle = {},
            onEmailChange    = {},
            onPasswordChange = {},
            onLogin          = {},
            onGoogleToken    = {},
            onGoogleError    = {},
            onShowEmailForm  = {},
            onBackToOptions  = {}
        )
    }
}
