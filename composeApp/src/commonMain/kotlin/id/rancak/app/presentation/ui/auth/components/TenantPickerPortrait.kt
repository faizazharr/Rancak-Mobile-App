package id.rancak.app.presentation.ui.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Tenant
import id.rancak.app.presentation.designsystem.RancakTheme

/** Layout portrait (phone) — gradient penuh + list kartu glass. */
@Composable
internal fun TenantPickerPortrait(
    tenants: List<Tenant>,
    selectedTenant: Tenant?,
    onSelectTenant: (Tenant) -> Unit
) {
    val primary     = MaterialTheme.colorScheme.primary
    val primaryDark = Color(
        red   = (primary.red   * 0.55f).coerceIn(0f, 1f),
        green = (primary.green * 0.55f).coerceIn(0f, 1f),
        blue  = (primary.blue  * 0.55f).coerceIn(0f, 1f)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Latar gradient penuh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(primary, primaryDark)))
        )
        // Dekorasi lingkaran
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = 80.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(28.dp))

            // Header kompak
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Store, null, Modifier.size(28.dp), tint = Color.White)
                }
                Column {
                    Text(
                        "Pilih Outlet",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                    Text(
                        "Tentukan outlet untuk sesi kasir",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.70f)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Label + badge
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Outlet Tersedia",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White.copy(alpha = 0.85f)
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f))
                        .border(1.dp, Color.White.copy(alpha = 0.30f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        "${tenants.size}",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Daftar outlet
            LazyColumn(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding      = PaddingValues(bottom = 8.dp)
            ) {
                items(tenants) { tenant ->
                    GlassOutletCard(
                        name       = tenant.name,
                        isSelected = selectedTenant == tenant,
                        colorIndex = tenants.indexOf(tenant),
                        onClick    = { onSelectTenant(tenant) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun TenantPickerPortraitPreview() {
    RancakTheme {
        val tenants = listOf(
            Tenant("1", "Warung Rancak"),
            Tenant("2", "Cafe Sederhana"),
            Tenant("3", "Kedai Kopi")
        )
        TenantPickerPortrait(tenants, tenants.first(), {})
    }
}
