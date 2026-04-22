package id.rancak.app.presentation.ui.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.auth.GoogleSignInButton
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.LoginUiState

/**
 * Tahap 1 login: dua pilihan utama — Google SSO atau lanjut dengan email.
 */
@Composable
internal fun LoginOptionsStep(
    uiState: LoginUiState,
    onEmailClick: () -> Unit,
    onGoogleToken: (String) -> Unit,
    onGoogleError: (String) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GoogleSignInButton(
            modifier  = Modifier.fillMaxWidth(),
            enabled   = !uiState.isLoading && !uiState.isGoogleLoading,
            onIdToken = onGoogleToken,
            onError   = onGoogleError
        )

        Spacer(Modifier.height(16.dp))

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

@Preview
@Composable
private fun LoginOptionsStepPreview() {
    RancakTheme {
        Column(Modifier.padding(24.dp)) {
            LoginOptionsStep(
                uiState       = LoginUiState(),
                onEmailClick  = {},
                onGoogleToken = {},
                onGoogleError = {}
            )
        }
    }
}
