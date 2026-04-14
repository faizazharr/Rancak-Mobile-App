package id.rancak.app.presentation.ui.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Tenant
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.TenantPickerViewModel
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TenantPickerScreen(
    onTenantSelected: () -> Unit,
    viewModel: TenantPickerViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadTenants() }
    LaunchedEffect(uiState.isConfirmed) { if (uiState.isConfirmed) onTenantSelected() }

    // Tap kartu → langsung pilih & masuk (tanpa tombol konfirmasi)
    val onSelectAndConfirm: (Tenant) -> Unit = { tenant ->
        viewModel.selectTenant(tenant)
        viewModel.confirm()
    }

    Scaffold { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(
                message = uiState.error!!,
                onRetry = viewModel::loadTenants,
                modifier = Modifier.padding(padding)
            )
            else -> BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
                val isWide = maxWidth > maxHeight || maxWidth >= 600.dp

                if (isWide) {
                    TenantPickerLandscape(
                        tenants        = uiState.tenants,
                        selectedTenant = uiState.selectedTenant,
                        onSelectTenant = onSelectAndConfirm
                    )
                } else {
                    TenantPickerPortrait(
                        tenants        = uiState.tenants,
                        selectedTenant = uiState.selectedTenant,
                        onSelectTenant = onSelectAndConfirm
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Portrait layout (phone normal)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TenantPickerPortrait(
    tenants: List<Tenant>,
    selectedTenant: Tenant?,
    onSelectTenant: (Tenant) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
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
                verticalAlignment = Alignment.CenterVertically,
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Outlet Tersedia",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f))
                        .border(1.dp, Color.White.copy(alpha = 0.30f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text("${tenants.size}", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Daftar outlet
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(tenants) { tenant ->
                    GlassOutletCard(
                        name = tenant.name,
                        isSelected = selectedTenant == tenant,
                        colorIndex = tenants.indexOf(tenant),
                        onClick = { onSelectTenant(tenant) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Landscape / Tablet layout  →  kiri: hero panel | kanan: daftar outlet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TenantPickerLandscape(
    tenants: List<Tenant>,
    selectedTenant: Tenant?,
    onSelectTenant: (Tenant) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
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
            // Dekorasi
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .offset(x = (-60).dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.07f))
            )
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
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
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tentukan outlet untuk\nmemulai sesi kasir",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.72f),
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
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        // ── Kanan: daftar + tombol ─────────────────────────────────────────
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

                // Grid 2 kolom jika lebar cukup (tablet), list biasa jika landscape phone
                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    if (maxWidth >= 480.dp) {
                        // Grid 2 kolom untuk tablet
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(tenants) { tenant ->
                                GlassOutletCardLight(
                                    name = tenant.name,
                                    isSelected = selectedTenant == tenant,
                                    colorIndex = tenants.indexOf(tenant),
                                    onClick = { onSelectTenant(tenant) }
                                )
                            }
                        }
                    } else {
                        // List biasa untuk landscape phone
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(tenants) { tenant ->
                                GlassOutletCardLight(
                                    name = tenant.name,
                                    isSelected = selectedTenant == tenant,
                                    colorIndex = tenants.indexOf(tenant),
                                    onClick = { onSelectTenant(tenant) }
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


// ─────────────────────────────────────────────────────────────────────────────
// Glass card (untuk portrait — di atas gradient gelap)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GlassOutletCard(
    name: String,
    isSelected: Boolean,
    colorIndex: Int,
    onClick: () -> Unit
) {
    val glassAlpha by animateColorAsState(
        targetValue = if (isSelected) Color.White.copy(0.28f) else Color.White.copy(0.12f),
        animationSpec = tween(200), label = "glass"
    )
    val borderAlpha by animateColorAsState(
        targetValue = if (isSelected) Color.White.copy(0.70f) else Color.White.copy(0.25f),
        animationSpec = tween(200), label = "border"
    )
    val accentColors = listOf(
        Color(0xFFFFD580), Color(0xFF80D4FF), Color(0xFFB8FF80), Color(0xFFFF9580)
    )
    val accent = accentColors[colorIndex % accentColors.size]
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
            verticalAlignment = Alignment.CenterVertically,
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
                Text(initial, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) Color.White else accent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold, color = Color.White)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(top = 3.dp)
                ) {
                    Box(Modifier.size(5.dp).clip(CircleShape)
                        .background(if (isSelected) accent else Color.White.copy(0.40f)))
                    Text("Outlet Kasir", style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.65f))
                }
            }
            if (isSelected) {
                Box(Modifier.size(28.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CheckCircle, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
            } else {
                Box(Modifier.size(26.dp).border(1.5.dp, Color.White.copy(0.40f), CircleShape))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Light card (untuk landscape/tablet — di atas background putih/terang)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GlassOutletCardLight(
    name: String,
    isSelected: Boolean,
    colorIndex: Int,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val cardBg by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                      else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200), label = "bg"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 1.dp,
        animationSpec = tween(200), label = "elev"
    )
    val accentColors = listOf(primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error)
    val accent = accentColors[colorIndex % accentColors.size]
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = if (isSelected)
            CardDefaults.outlinedCardBorder().copy(width = 2.dp,
                brush = Brush.linearGradient(listOf(primary, primary.copy(0.5f))))
        else CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape)
                    .background(if (isSelected) accent else accent.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) Color.White else accent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface)
                Text("Outlet Kasir", style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) primary.copy(0.8f)
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp))
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = primary, modifier = Modifier.size(26.dp))
            } else {
                Box(Modifier.size(26.dp)
                    .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Portrait", widthDp = 390, heightDp = 844)
@Composable
private fun PortraitPreview() {
    RancakTheme {
        val tenants = listOf(
            Tenant("1", "Warung Rancak"), Tenant("2", "Cafe Sederhana"), Tenant("3", "Kedai Kopi")
        )
        TenantPickerPortrait(tenants, tenants.first(), {})
    }
}

@Preview(name = "Landscape", widthDp = 844, heightDp = 390)
@Composable
private fun LandscapePreview() {
    RancakTheme {
        val tenants = listOf(
            Tenant("1", "Warung Rancak"), Tenant("2", "Cafe Sederhana"), Tenant("3", "Kedai Kopi")
        )
        TenantPickerLandscape(tenants, tenants.first(), {})
    }
}

@Preview(name = "Tablet", widthDp = 1024, heightDp = 768)
@Composable
private fun TabletPreview() {
    RancakTheme {
        val tenants = listOf(
            Tenant("1", "Warung Rancak"), Tenant("2", "Cafe Sederhana"),
            Tenant("3", "Kedai Kopi"), Tenant("4", "Toko Serba Ada")
        )
        TenantPickerLandscape(tenants, tenants.first(), {})
    }
}
