package id.rancak.app.presentation.ui.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.LoginUiState
import org.jetbrains.compose.resources.painterResource
import rancak.composeapp.generated.resources.Res
import rancak.composeapp.generated.resources.tias_logo

/** Tata letak login untuk phone: kolom tunggal centered. */
@Composable
internal fun PhoneLoginLayout(
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
            .imePadding()
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
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Powered by",
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                fontSize = 10.sp
            )
            Image(
                painter            = painterResource(Res.drawable.tias_logo),
                contentDescription = "TIAS",
                contentScale       = ContentScale.FillHeight,
                modifier           = Modifier.height(16.dp).clip(RoundedCornerShape(4.dp))
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

@Preview
@Composable
private fun PhoneLoginLayoutPreview_Options() {
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
private fun PhoneLoginLayoutPreview_EmailForm() {
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
