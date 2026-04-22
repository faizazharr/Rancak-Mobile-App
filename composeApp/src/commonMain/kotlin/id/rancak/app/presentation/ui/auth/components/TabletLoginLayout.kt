package id.rancak.app.presentation.ui.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

/** Tata letak login untuk tablet: panel branding di kiri + form di kanan. */
@Composable
internal fun TabletLoginLayout(
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

        // ── Panel branding (kiri) ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(0.52f)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(listOf(primary, primary.copy(alpha = 0.80f)))
                )
        ) {
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
                modifier = Modifier.fillMaxSize().padding(52.dp),
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
                BrandingBullet(Icons.Default.Receipt,                "Kasir multi-device, satu dashboard")
                BrandingBullet(Icons.Default.ShoppingCart,           "Stok otomatis tiap transaksi")
                BrandingBullet(Icons.AutoMirrored.Filled.TrendingUp, "Laporan real-time & shift harian")
            }

            Row(
                modifier              = Modifier.align(Alignment.BottomStart).padding(24.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Powered by",
                    style = MaterialTheme.typography.labelSmall,
                    color = onPrimary.copy(alpha = 0.38f)
                )
                Image(
                    painter            = painterResource(Res.drawable.tias_logo),
                    contentDescription = "TIAS",
                    contentScale       = ContentScale.FillHeight,
                    modifier           = Modifier.height(18.dp).clip(RoundedCornerShape(4.dp))
                )
            }
        }

        // ── Panel form (kanan) ──────────────────────────────────────────────
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
                    .imePadding()
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

@Preview(widthDp = 1024, heightDp = 768)
@Composable
private fun TabletLoginLayoutPreview() {
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
