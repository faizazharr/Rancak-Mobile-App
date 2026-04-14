package id.rancak.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun ProductCard(
    name: String,
    price: String,
    category: String?,
    imageUrl: String?,
    isAvailable: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(enabled = isAvailable) { onClick() },
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = if (isAvailable)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Category label — small uppercase, muted
            if (category != null) {
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
            }

            // Product name — up to 2 lines
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isAvailable)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )

            Spacer(Modifier.height(4.dp))

            // Price — bold, primary color
            Text(
                text = price,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isAvailable)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

// ── Previews ──

@Preview
@Composable
private fun ProductCardPreview() {
    RancakTheme {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProductCard(
                name = "Nasi Goreng Spesial",
                price = "Rp 35.000",
                category = "Makanan Berat",
                imageUrl = null,
                onClick = {},
                modifier = Modifier.width(160.dp)
            )
            ProductCard(
                name = "Es Teh Manis",
                price = "Rp 8.000",
                category = "Minuman",
                imageUrl = null,
                isAvailable = false,
                onClick = {},
                modifier = Modifier.width(160.dp)
            )
        }
    }
}
