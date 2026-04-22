package id.rancak.app.presentation.ui.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakTheme

/**
 * Bullet point kecil dengan icon bulat — dipakai di panel branding
 * login (hanya tablet).
 */
@Composable
internal fun BrandingBullet(icon: ImageVector, label: String) {
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

@Preview
@Composable
private fun BrandingBulletPreview() {
    RancakTheme {
        // Wrap in primary-tinted background so bullet is readable.
        Box(Modifier.background(Color(0xFF0D9373)).padding(24.dp)) {
            BrandingBullet(Icons.Default.Receipt, "Kasir multi-device, satu dashboard")
        }
    }
}
