package id.rancak.app.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// Expect — platform provides click + credential logic
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Tombol Google Sign-In lintas platform.
 *
 * Android → Credential Manager (GetSignInWithGoogleOption)
 * iOS     → GoogleSignInBridge → GIDSignIn (iOS SDK via Swift)
 */
@Composable
expect fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onIdToken: (String) -> Unit,
    onError: (String) -> Unit
)

// ─────────────────────────────────────────────────────────────────────────────
// Shared visual — dipakai oleh kedua actual
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Visual tombol "Masuk dengan Google" yang konsisten di semua platform.
 * Actual hanya perlu menyediakan [onClick] dan [isLoading].
 */
@Composable
internal fun GoogleSignInButtonContent(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (enabled && !isLoading) Color(0xFFDADCE0) else Color(0xFFDADCE0).copy(alpha = 0.5f)
        ),
        shadowElevation = if (enabled && !isLoading) 2.dp else 0.dp,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Loading spinner
            AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color(0xFF4285F4),
                    strokeWidth = 2.5.dp
                )
            }

            // Button content
            AnimatedVisibility(visible = !isLoading, enter = fadeIn(), exit = fadeOut()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GoogleGLogo(size = 22.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Masuk dengan Google",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (enabled) Color(0xFF3C4043) else Color(0xFF3C4043).copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Google "G" logo — canvas 4-warna
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GoogleGLogo(size: Dp = 22.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s   = this.size.minDimension
        val sw  = s * 0.16f                    // stroke width
        val r   = (s - sw) / 2                 // radius (inside stroke)
        val cx  = s / 2f
        val cy  = s / 2f
        val tl  = Offset(cx - r, cy - r)
        val sz  = Size(r * 2, r * 2)

        fun arc(color: Color, start: Float, sweep: Float) = drawArc(
            color = color, startAngle = start, sweepAngle = sweep,
            useCenter = false,
            topLeft = tl, size = sz,
            style = Stroke(width = sw, cap = StrokeCap.Butt)
        )

        // Urutan searah jarum jam dari kanan atas
        arc(Color(0xFF4285F4), -30f,  90f)   // biru  — kanan bawah
        arc(Color(0xFF34A853),  60f,  90f)   // hijau — bawah kanan
        arc(Color(0xFFFBBC05), 150f,  70f)   // kuning— bawah kiri
        arc(Color(0xFFEA4335), 220f, 110f)   // merah — kiri & atas

        // Batang horizontal tengah (biru) — ciri khas huruf G
        drawLine(
            color     = Color(0xFF4285F4),
            start     = Offset(cx, cy),
            end       = Offset(cx + r - sw * 0.4f, cy),
            strokeWidth = sw
        )
    }
}
