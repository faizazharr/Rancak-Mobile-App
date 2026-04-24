package id.rancak.app.presentation.ui.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Tenant
import id.rancak.app.presentation.designsystem.RancakTheme

/** Layout landscape / tablet — panel hero kiri + daftar outlet kanan. */
@Composable
internal fun TenantPickerLandscape(
    tenants: List<Tenant>,
    selectedTenant: Tenant?,
    onSelectTenant: (Tenant) -> Unit,
    onAddOutlet: (() -> Unit)? = null
) {
    val primary     = MaterialTheme.colorScheme.primary
    val primaryDark = Color(
        red   = (primary.red   * 0.55f).coerceIn(0f, 1f),
        green = (primary.green * 0.55f).coerceIn(0f, 1f),
        blue  = (primary.blue  * 0.55f).coerceIn(0f, 1f)
    )

    Row(modifier = Modifier.fillMaxSize()) {

        // ── Kiri: panel hero gradient ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.38f)
                .background(Brush.verticalGradient(listOf(primary, primaryDark))),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier.size(220.dp).offset(x = (-60).dp, y = (-60).dp)
                    .clip(CircleShape).background(Color.White.copy(alpha = 0.07f))
            )
            Box(
                Modifier.size(160.dp).align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 40.dp)
                    .clip(CircleShape).background(Color.White.copy(alpha = 0.05f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Store, null, Modifier.size(38.dp), tint = Color.White)
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "Pilih Outlet",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White,
                    textAlign  = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tentukan outlet untuk\nmemulai sesi kasir",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = Color.White.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))

                // Badge jumlah outlet
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(1.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "${tenants.size} outlet tersedia",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    )
                }

                if (onAddOutlet != null) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick  = onAddOutlet,
                        border   = BorderStroke(1.dp, Color.White.copy(alpha = 0.40f)),
                        shape    = MaterialTheme.shapes.medium,
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ajukan Outlet Baru", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // ── Kanan: daftar outlet ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.62f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Pilih salah satu outlet di bawah:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    if (maxWidth >= 480.dp) {
                        // Grid 2 kolom untuk tablet
                        LazyVerticalGrid(
                            columns               = GridCells.Fixed(2),
                            verticalArrangement   = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding        = PaddingValues(bottom = 8.dp),
                            modifier              = Modifier.fillMaxSize()
                        ) {
                            items(tenants) { tenant ->
                                GlassOutletCardLight(
                                    name       = tenant.name,
                                    isSelected = selectedTenant == tenant,
                                    colorIndex = tenants.indexOf(tenant),
                                    onClick    = { onSelectTenant(tenant) }
                                )
                            }
                        }
                    } else {
                        // List biasa untuk landscape phone
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding      = PaddingValues(bottom = 8.dp),
                            modifier            = Modifier.fillMaxSize()
                        ) {
                            items(tenants) { tenant ->
                                GlassOutletCardLight(
                                    name       = tenant.name,
                                    isSelected = selectedTenant == tenant,
                                    colorIndex = tenants.indexOf(tenant),
                                    onClick    = { onSelectTenant(tenant) }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Preview(name = "Landscape", widthDp = 844, heightDp = 390)
@Composable
private fun TenantPickerLandscapePreview() {
    RancakTheme {
        val tenants = listOf(
            Tenant("1", "Warung Rancak"),
            Tenant("2", "Cafe Sederhana"),
            Tenant("3", "Kedai Kopi")
        )
        TenantPickerLandscape(tenants, tenants.first(), {})
    }
}

@Preview(name = "Tablet", widthDp = 1024, heightDp = 768)
@Composable
private fun TenantPickerLandscape_TabletPreview() {
    RancakTheme {
        val tenants = listOf(
            Tenant("1", "Warung Rancak"), Tenant("2", "Cafe Sederhana"),
            Tenant("3", "Kedai Kopi"),   Tenant("4", "Toko Serba Ada")
        )
        TenantPickerLandscape(tenants, tenants.first(), {})
    }
}
