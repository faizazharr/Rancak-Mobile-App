package id.rancak.app.presentation.ui.auth.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakTheme

/**
 * Kartu outlet versi "glass" — dipakai di portrait/phone di atas gradient
 * gelap. Aksen warna berbeda per index sehingga tiap kartu terasa unik.
 */
@Composable
internal fun GlassOutletCard(
    name: String,
    isSelected: Boolean,
    colorIndex: Int,
    onClick: () -> Unit
) {
    val glassAlpha by animateColorAsState(
        targetValue   = if (isSelected) Color.White.copy(0.28f) else Color.White.copy(0.12f),
        animationSpec = tween(200), label = "glass"
    )
    val borderAlpha by animateColorAsState(
        targetValue   = if (isSelected) Color.White.copy(0.70f) else Color.White.copy(0.25f),
        animationSpec = tween(200), label = "border"
    )
    val accentColors = listOf(
        Color(0xFFFFD580), Color(0xFF80D4FF), Color(0xFFB8FF80), Color(0xFFFF9580)
    )
    val accent  = accentColors[colorIndex % accentColors.size]
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(glassAlpha)
            .border(if (isSelected) 1.5.dp else 1.dp, borderAlpha, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accent.copy(0.9f) else accent.copy(0.20f))
                    .border(1.dp, if (isSelected) accent else accent.copy(0.40f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initial,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = if (isSelected) Color.White else accent
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier              = Modifier.padding(top = 3.dp)
                ) {
                    Box(
                        Modifier.size(5.dp).clip(CircleShape)
                            .background(if (isSelected) accent else Color.White.copy(0.40f))
                    )
                    Text(
                        "Outlet Kasir",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.65f)
                    )
                }
            }
            if (isSelected) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            } else {
                Box(Modifier.size(26.dp).border(1.5.dp, Color.White.copy(0.40f), CircleShape))
            }
        }
    }
}

@Preview
@Composable
private fun GlassOutletCardPreview() {
    RancakTheme {
        val primary = MaterialTheme.colorScheme.primary
        Box(
            Modifier
                .background(Brush.verticalGradient(listOf(primary, primary.copy(alpha = 0.55f))))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassOutletCard("Warung Rancak",  isSelected = true,  colorIndex = 0, onClick = {})
                GlassOutletCard("Cafe Sederhana", isSelected = false, colorIndex = 1, onClick = {})
            }
        }
    }
}
