package id.rancak.app.presentation.ui.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakTextField
import id.rancak.app.presentation.viewmodel.BusinessType
import id.rancak.app.presentation.viewmodel.OutletSubmissionFormState

/**
 * Konten untuk kondisi outlet kosong.
 *
 * Tiga state:
 * - Empty  : tampilan "Belum Ada Outlet" dengan CTA pengajuan
 * - Form   : form pengajuan bergaya gradient header + white sheet
 * - Sukses : konfirmasi pengajuan berhasil
 *
 * Semua state menggunakan visual language yang sama dengan
 * [TenantPickerPortrait] — gradient primary, dekorasi lingkaran,
 * tipografi putih di atas gradient.
 */
@Composable
internal fun OutletSubmissionContent(
    state: OutletSubmissionFormState,
    onOpenForm: () -> Unit,
    onCloseForm: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onGmapsChange: (String) -> Unit,
    onNibChange: (String) -> Unit,
    onBusinessTypeChange: (BusinessType) -> Unit,
    onSubmit: () -> Unit,
    onReset: () -> Unit,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when {
        state.isSubmitted -> SubmissionSuccess(
            onReset  = onReset,
            modifier = modifier
        )
        state.isFormOpen  -> SubmissionForm(
            state                = state,
            onBack               = onCloseForm,
            onNameChange         = onNameChange,
            onPhoneChange        = onPhoneChange,
            onAddressChange      = onAddressChange,
            onGmapsChange        = onGmapsChange,
            onNibChange          = onNibChange,
            onBusinessTypeChange = onBusinessTypeChange,
            onSubmit             = onSubmit,
            modifier             = modifier
        )
        else -> EmptyOutletState(
            onOpenForm = onOpenForm,
            onLogout   = onLogout,
            modifier   = modifier
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers bersama
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun gradientColors(): Pair<Color, Color> {
    val primary = MaterialTheme.colorScheme.primary
    val dark    = Color(
        red   = (primary.red   * 0.55f).coerceIn(0f, 1f),
        green = (primary.green * 0.55f).coerceIn(0f, 1f),
        blue  = (primary.blue  * 0.55f).coerceIn(0f, 1f)
    )
    return primary to dark
}

@Composable
private fun GradientBackground(modifier: Modifier = Modifier) {
    val (primary, dark) = gradientColors()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(primary, dark)))
    )
}

/** Lingkaran dekoratif semi-transparan persis seperti di TenantPickerPortrait. */
@Composable
private fun BoxScope.PortraitDecorations() {
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
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyOutletState(
    onOpenForm: () -> Unit,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        GradientBackground()
        PortraitDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ikon outlet — glass style persis header TenantPickerPortrait
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Storefront,
                    contentDescription = null,
                    tint     = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Belum Ada Outlet",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.White,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Anda belum memiliki outlet yang terdaftar.\nAjukan outlet baru untuk mulai menggunakan Rancak POS.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Info card — glass style
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Cara Pengajuan",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White.copy(alpha = 0.90f)
                )
                InfoStep(number = "1", text = "Isi data outlet dan jenis usaha Anda")
                InfoStep(number = "2", text = "Tim Rancak meninjau pengajuan")
                InfoStep(number = "3", text = "Outlet aktif dan siap digunakan")
            }

            Spacer(Modifier.height(28.dp))

            // Tombol CTA — putih di atas gradient
            Button(
                onClick  = onOpenForm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape  = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor   = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ajukan Outlet Baru", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            // Tombol logout — ganti akun jika tersangkut
            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    tint     = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Keluar / Ganti Akun",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun InfoStep(number: String, text: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f))
                .border(1.dp, Color.White.copy(alpha = 0.40f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Text(text, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Form pengajuan — gradient header + white bottom sheet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SubmissionForm(
    state: OutletSubmissionFormState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onGmapsChange: (String) -> Unit,
    onNibChange: (String) -> Unit,
    onBusinessTypeChange: (BusinessType) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        GradientBackground()
        PortraitDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // ── Gradient header ───────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tombol kembali — glass style
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint     = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Pengajuan Outlet",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                    Text(
                        "Lengkapi data di bawah ini",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.70f)
                    )
                }

                // Brand icon — glass
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Storefront, null, Modifier.size(22.dp), tint = Color.White)
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── White sheet ───────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape    = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color    = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Panduan singkat
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Isi semua kolom wajib untuk mengirim pengajuan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Identitas Outlet
                    FormSection(
                        title      = "Identitas Outlet",
                        icon       = Icons.Default.Storefront,
                        stepNumber = "01"
                    ) {
                        RancakTextField(
                            value         = state.name,
                            onValueChange = onNameChange,
                            label         = "Nama Outlet",
                            placeholder   = "Contoh: Warung Kopi Sinar",
                            leadingIcon   = { Icon(Icons.Default.Storefront, null, Modifier.size(20.dp)) }
                        )
                        RancakTextField(
                            value          = state.phone,
                            onValueChange  = onPhoneChange,
                            label          = "Nomor Telepon",
                            placeholder    = "08xxxxxxxxxx",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon    = { Icon(Icons.Default.Phone, null, Modifier.size(20.dp)) }
                        )
                    }

                    // Lokasi
                    FormSection(
                        title      = "Lokasi",
                        icon       = Icons.Default.LocationOn,
                        stepNumber = "02"
                    ) {
                        RancakTextField(
                            value         = state.address,
                            onValueChange = onAddressChange,
                            label         = "Alamat Lengkap",
                            placeholder   = "Jalan, RT/RW, Kelurahan, Kota",
                            singleLine    = false,
                            minLines      = 2,
                            maxLines      = 4,
                            leadingIcon   = { Icon(Icons.Default.LocationOn, null, Modifier.size(20.dp)) }
                        )
                        RancakTextField(
                            value          = state.gmapsUrl,
                            onValueChange  = onGmapsChange,
                            label          = "Link Google Maps (opsional)",
                            placeholder    = "https://maps.google.com/...",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            leadingIcon    = { Icon(Icons.Default.Map, null, Modifier.size(20.dp)) }
                        )
                    }

                    // Legalitas & Jenis Usaha
                    FormSection(
                        title      = "Legalitas & Jenis Usaha",
                        icon       = Icons.Default.Description,
                        stepNumber = "03"
                    ) {
                        RancakTextField(
                            value          = state.nib,
                            onValueChange  = onNibChange,
                            label          = "NIB (Nomor Induk Berusaha)",
                            placeholder    = "13 digit NIB",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon    = { Icon(Icons.Default.Description, null, Modifier.size(20.dp)) }
                        )
                        BusinessTypeDropdown(
                            selected = state.businessType,
                            onSelect = onBusinessTypeChange
                        )
                    }

                    // Error
                    if (state.error != null) {
                        Surface(
                            shape    = RoundedCornerShape(12.dp),
                            color    = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier              = Modifier.padding(12.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint     = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text  = state.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "* Nama, telepon, alamat, NIB, dan jenis usaha wajib diisi",
                            style     = MaterialTheme.typography.labelSmall,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.fillMaxWidth()
                        )
                        RancakButton(
                            text      = "Kirim Pengajuan",
                            onClick   = onSubmit,
                            isLoading = state.isSubmitting,
                            enabled   = state.isValid,
                            modifier  = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * FormSection bergaya mini gradient banner:
 * - Header gradient teal (menyerupai layar utama) dengan glass icon box
 * - Step badge ("01", "02", ...) di pojok kanan header
 * - Dekorasi angka besar semi-transparan sebagai latar header
 * - Konten field di bawah dengan background surface bersih
 */
@Composable
private fun FormSection(
    title:      String,
    icon:       ImageVector,
    stepNumber: String,
    content:    @Composable ColumnScope.() -> Unit
) {
    val (primary, dark) = gradientColors()
    val cardShape = RoundedCornerShape(18.dp)
    val bannerShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)

    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = cardShape,
        shadowElevation = 3.dp,
        color           = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // ── Gradient banner header ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(bannerShape)
                    .background(Brush.horizontalGradient(listOf(primary, dark)))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Angka dekoratif besar di latar
                Text(
                    stepNumber,
                    style      = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White.copy(alpha = 0.07f),
                    modifier   = Modifier.align(Alignment.CenterEnd).offset(x = 4.dp)
                )

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Glass icon box
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(1.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint     = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Judul section
                    Text(
                        title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                        modifier   = Modifier.weight(1f)
                    )

                    // Step badge pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.20f))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stepNumber,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color.White
                        )
                    }
                }
            }

            // ── Field inputs ──────────────────────────────────────────────
            Column(
                modifier            = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BusinessTypeDropdown(
    selected: BusinessType?,
    onSelect: (BusinessType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded          = expanded,
        onExpandedChange  = { expanded = !expanded }
    ) {
        RancakTextField(
            value         = selected?.label ?: "",
            onValueChange = {},
            label         = "Jenis Usaha",
            placeholder   = "Pilih jenis usaha",
            readOnly      = true,
            leadingIcon   = { Icon(Icons.Default.Category, null, Modifier.size(20.dp)) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier      = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            BusinessType.entries.forEach { type ->
                DropdownMenuItem(
                    text    = { Text(type.label) },
                    onClick = {
                        onSelect(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sukses
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SubmissionSuccess(
    onReset:  () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        GradientBackground()
        PortraitDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ikon sukses — glass circle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint     = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Pengajuan Terkirim!",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.White,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Pengajuan Anda sedang kami tinjau.\nAnda akan dihubungi melalui kontak yang didaftarkan.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Tombol ajukan lagi — white outlined di atas gradient
            OutlinedButton(
                onClick  = onReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape  = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Ajukan Outlet Lain", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
