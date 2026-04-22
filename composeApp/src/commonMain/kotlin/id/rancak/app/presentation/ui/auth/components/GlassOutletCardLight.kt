package id.rancak.app.presentation.ui.auth.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
 * Kartu outlet versi "light" — dipakai di landscape/tablet di atas background
 * terang (panel kanan). Memakai Material Card + shadow halus.
 */
@Composable
internal fun GlassOutletCardLight(
    name: String,
    isSelected: Boolean,
    colorIndex: Int,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val cardBg by animateColorAsState(
        targetValue   = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200), label = "bg"
    )
    val elevation by animateDpAsState(
        targetValue   = if (isSelected) 4.dp else 1.dp,
        animationSpec = tween(200), label = "elev"
    )
    val accentColors = listOf(
        primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error
    )
    val accent  = accentColors[colorIndex % accentColors.size]
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = MaterialTheme.shapes.large,
        colors    = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border    = if (isSelected)
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = Brush.linearGradient(listOf(primary, primary.copy(0.5f)))
            )
        else CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accent else accent.copy(0.15f)),
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
                    color      = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                 else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Outlet Kasir",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = if (isSelected) primary.copy(0.8f)
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle, null,
                    tint     = primary,
                    modifier = Modifier.size(26.dp)
                )
            } else {
                Box(
                    Modifier.size(26.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
            }
        }
    }
}

@Preview
@Composable
private fun GlassOutletCardLightPreview() {
    RancakTheme {
        Column(
            modifier              = Modifier.padding(16.dp),
            verticalArrangement   = Arrangement.spacedBy(10.dp)
        ) {
            GlassOutletCardLight("Warung Rancak",  isSelected = true,  colorIndex = 0, onClick = {})
            GlassOutletCardLight("Cafe Sederhana", isSelected = false, colorIndex = 1, onClick = {})
        }
    }
}
