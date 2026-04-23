package id.rancak.app.presentation.ui.auth.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
 * Konten yang ditampilkan ketika pengguna belum memiliki outlet.
 * Tiga state: empty (CTA), form pengajuan, dan sukses.
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
    modifier: Modifier = Modifier
) {
    when {
        state.isSubmitted -> SubmissionSuccess(onReset = onReset, modifier = modifier)
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
        else -> EmptyOutletState(onOpenForm = onOpenForm, modifier = modifier)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyOutletState(
    onOpenForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            primary.copy(alpha = 0.18f),
                            primary.copy(alpha = 0.06f)
                        )
                    )
                )
                .border(1.dp, primary.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Storefront,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "Belum Ada Outlet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Anda belum memiliki outlet yang terdaftar.\n" +
                "Ajukan outlet baru untuk mulai menggunakan Rancak POS.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoStepRow(number = "1", text = "Isi data outlet & jenis usaha")
                InfoStepRow(number = "2", text = "Tim Rancak meninjau pengajuan Anda")
                InfoStepRow(number = "3", text = "Outlet aktif & siap digunakan")
            }
        }

        Spacer(Modifier.height(28.dp))

        RancakButton(
            text = "Ajukan Outlet Baru",
            onClick = onOpenForm,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun InfoStepRow(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Form
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FormHeader(onBack = onBack)

        FormSection(title = "Identitas Outlet", icon = Icons.Default.Storefront) {
            RancakTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = "Nama Outlet",
                placeholder = "Contoh: Warung Kopi Sinar",
                leadingIcon = { Icon(Icons.Default.Storefront, null) }
            )
            RancakTextField(
                value = state.phone,
                onValueChange = onPhoneChange,
                label = "Nomor Telepon",
                placeholder = "08xxxxxxxxxx",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Default.Phone, null) }
            )
        }

        FormSection(title = "Lokasi", icon = Icons.Default.LocationOn) {
            RancakTextField(
                value = state.address,
                onValueChange = onAddressChange,
                label = "Alamat Lengkap",
                placeholder = "Jalan, RT/RW, Kelurahan, Kota",
                singleLine = false,
                minLines = 2,
                maxLines = 4,
                leadingIcon = { Icon(Icons.Default.LocationOn, null) }
            )
            RancakTextField(
                value = state.gmapsUrl,
                onValueChange = onGmapsChange,
                label = "Link Google Maps (opsional)",
                placeholder = "https://maps.google.com/...",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                leadingIcon = { Icon(Icons.Default.Map, null) }
            )
        }

        FormSection(title = "Legalitas & Jenis Usaha", icon = Icons.Default.Description) {
            RancakTextField(
                value = state.nib,
                onValueChange = onNibChange,
                label = "NIB (Nomor Induk Berusaha)",
                placeholder = "13 digit NIB",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Description, null) }
            )
            BusinessTypeDropdown(
                selected = state.businessType,
                onSelect = onBusinessTypeChange
            )
        }

        state.error?.let { msg ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        RancakButton(
            text = "Kirim Pengajuan",
            onClick = onSubmit,
            isLoading = state.isSubmitting,
            enabled = state.isValid,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun FormHeader(onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Pengajuan Outlet Baru",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Lengkapi data di bawah ini",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AddBusiness,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
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
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        RancakTextField(
            value = selected?.label ?: "",
            onValueChange = {},
            label = "Jenis Usaha",
            placeholder = "Pilih jenis usaha",
            readOnly = true,
            leadingIcon = { Icon(Icons.Default.Category, null) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            BusinessType.values().forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label) },
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
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Pengajuan Terkirim",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Pengajuan outlet Anda sedang kami tinjau.\n" +
                "Anda akan dihubungi melalui kontak yang didaftarkan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        OutlinedButton(
            onClick = onReset,
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Ajukan Outlet Lain")
        }
    }
}
