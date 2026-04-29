package id.rancak.app.presentation.ui.pricing.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Voucher
import id.rancak.app.presentation.components.DatePickerField
import id.rancak.app.presentation.designsystem.RancakTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherFormContent(
    editing: Voucher?,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onConfirm: (
        code: String, name: String, discountType: String, discountValue: String,
        validFrom: String, description: String?, maxDiscount: String?,
        minPurchase: String, usageLimit: Int?, validUntil: String?, isActive: Boolean
    ) -> Unit
) {
    var code           by remember(editing) { mutableStateOf(editing?.code ?: "") }
    var name           by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var discountType   by remember(editing) { mutableStateOf(editing?.discountType ?: "percentage") }
    var discountValue  by remember(editing) { mutableStateOf(editing?.discountValue?.toString() ?: "") }
    var validFrom      by remember(editing) { mutableStateOf(editing?.validFrom?.take(10) ?: "") }
    var validUntil     by remember(editing) { mutableStateOf(editing?.validUntil?.take(10) ?: "") }
    var description    by remember(editing) { mutableStateOf(editing?.description ?: "") }
    var maxDiscount    by remember(editing) { mutableStateOf(editing?.maxDiscount?.toString() ?: "") }
    var minPurchase    by remember(editing) { mutableStateOf(editing?.minPurchase?.toString() ?: "0") }
    var usageLimitText by remember(editing) { mutableStateOf(editing?.usageLimit?.toString() ?: "") }
    var isActive       by remember(editing) { mutableStateOf(editing?.isActive ?: true) }

    val isPct = discountType == "percentage"

    val discountValueNum = discountValue.toDoubleOrNull()
    val discountValueError: String? = when {
        discountValue.isBlank() -> null
        discountValueNum == null -> "Nilai tidak valid"
        isPct && discountValueNum > 100 -> "Persen tidak boleh melebihi 100"
        discountValueNum <= 0 -> "Nilai harus lebih dari 0"
        else -> null
    }
    val validUntilError: String? = when {
        validUntil.isBlank() || validFrom.isBlank() -> null
        validUntil <= validFrom -> "Harus setelah tanggal berlaku"
        else -> null
    }
    val canConfirm = !isSubmitting &&
        code.isNotBlank() && name.isNotBlank() &&
        discountValue.isNotBlank() && discountValueError == null &&
        validFrom.isNotBlank() && validUntilError == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (editing == null) "Tambah Voucher" else "Edit Voucher",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isSubmitting) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack, enabled = !isSubmitting,
                        modifier = Modifier.weight(1f)
                    ) { Text("Batal") }
                    Button(
                        onClick = {
                            onConfirm(
                                code.trim(), name.trim(), discountType, discountValue, validFrom,
                                description.ifBlank { null },
                                if (isPct) maxDiscount.ifBlank { null } else null,
                                minPurchase.ifBlank { "0" }, usageLimitText.toIntOrNull(),
                                validUntil.ifBlank { null }, isActive
                            )
                        },
                        enabled = canConfirm,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSubmitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Simpan")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Informasi Dasar ──────────────────────────────────────────────
            item {
                Text("Informasi Dasar", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
            item {
                OutlinedTextField(
                    value = code, onValueChange = { code = it.uppercase() },
                    label = { Text("Kode Voucher *") },
                    supportingText = { Text("Unik, huruf kapital/angka") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nama Voucher *") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Deskripsi (opsional)") },
                    modifier = Modifier.fillMaxWidth(), maxLines = 3
                )
            }

            // ── Konfigurasi Diskon ───────────────────────────────────────────
            item { HorizontalDivider() }
            item {
                Text("Konfigurasi Diskon", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
            item {
                Text("Tipe Diskon", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("percentage" to "Persen (%)", "nominal" to "Nominal (Rp)").forEach { (value, label) ->
                        FilterChip(selected = discountType == value,
                            onClick = { discountType = value }, label = { Text(label) })
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = discountValue,
                    onValueChange = { discountValue = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(if (isPct) "Nilai Diskon (%) *" else "Nilai Diskon (Rp) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = discountValueError != null,
                    supportingText = if (discountValueError != null) ({ Text(discountValueError) }) else null,
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
            if (isPct) {
                item {
                    OutlinedTextField(
                        value = maxDiscount,
                        onValueChange = { maxDiscount = it.filter { c -> c.isDigit() } },
                        label = { Text("Maks. Nominal Diskon (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = { Text("Batasi nilai Rp meski persennya besar") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = minPurchase,
                    onValueChange = { minPurchase = it.filter { c -> c.isDigit() } },
                    label = { Text("Min. Pembelian (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("0 = tidak ada minimum") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }

            // ── Periode Berlaku ──────────────────────────────────────────────
            item { HorizontalDivider() }
            item {
                Text("Periode Berlaku", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
            item {
                DatePickerField(
                    label = "Berlaku Dari *",
                    value = validFrom,
                    onDateSelected = { validFrom = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                DatePickerField(
                    label = "Berlaku Sampai (opsional)",
                    value = validUntil,
                    onDateSelected = { validUntil = it },
                    isError = validUntilError != null,
                    supportingText = if (validUntilError != null) ({ Text(validUntilError) }) else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Penggunaan & Status ──────────────────────────────────────────
            item { HorizontalDivider() }
            item {
                Text("Penggunaan & Status", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
            item {
                OutlinedTextField(
                    value = usageLimitText,
                    onValueChange = { usageLimitText = it.filter { c -> c.isDigit() } },
                    label = { Text("Batas Penggunaan") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Kosongkan = tidak terbatas") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Voucher Aktif", style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium)
                        Text("Voucher bisa digunakan kasir",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VoucherFormPanel — embedded panel for tablet split layout
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherFormPanel(
    editing: Voucher?,
    isSubmitting: Boolean,
    onClose: () -> Unit,
    onConfirm: (
        code: String, name: String, discountType: String, discountValue: String,
        validFrom: String, description: String?, maxDiscount: String?,
        minPurchase: String, usageLimit: Int?, validUntil: String?, isActive: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var code           by remember(editing) { mutableStateOf(editing?.code ?: "") }
    var name           by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var discountType   by remember(editing) { mutableStateOf(editing?.discountType ?: "percentage") }
    var discountValue  by remember(editing) { mutableStateOf(editing?.discountValue?.toString() ?: "") }
    var validFrom      by remember(editing) { mutableStateOf(editing?.validFrom?.take(10) ?: "") }
    var validUntil     by remember(editing) { mutableStateOf(editing?.validUntil?.take(10) ?: "") }
    var description    by remember(editing) { mutableStateOf(editing?.description ?: "") }
    var maxDiscount    by remember(editing) { mutableStateOf(editing?.maxDiscount?.toString() ?: "") }
    var minPurchase    by remember(editing) { mutableStateOf(editing?.minPurchase?.toString() ?: "0") }
    var usageLimitText by remember(editing) { mutableStateOf(editing?.usageLimit?.toString() ?: "") }
    var isActive       by remember(editing) { mutableStateOf(editing?.isActive ?: true) }

    val isPct = discountType == "percentage"
    val discountValueNum = discountValue.toDoubleOrNull()
    val discountValueError = when {
        discountValue.isBlank() -> null
        discountValueNum == null -> "Nilai tidak valid"
        isPct && discountValueNum > 100 -> "Persen tidak boleh melebihi 100"
        discountValueNum <= 0 -> "Nilai harus lebih dari 0"
        else -> null
    }
    val validUntilError = when {
        validUntil.isBlank() || validFrom.isBlank() -> null
        validUntil <= validFrom -> "Harus setelah tanggal berlaku"
        else -> null
    }
    val canConfirm = !isSubmitting &&
        code.isNotBlank() && name.isNotBlank() &&
        discountValue.isNotBlank() && discountValueError == null &&
        validFrom.isNotBlank() && validUntilError == null

    Column(modifier.fillMaxSize()) {
        // ── Panel header ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (editing == null) "Tambah Voucher" else "Edit: ${editing.code}",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onClose, enabled = !isSubmitting) {
                Icon(Icons.Default.Close, contentDescription = "Tutup")
            }
        }
        HorizontalDivider()

        // ── Scrollable form fields ────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Informasi Dasar", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
            item {
                OutlinedTextField(
                    value = code, onValueChange = { code = it.uppercase() },
                    label = { Text("Kode Voucher *") },
                    supportingText = { Text("Unik, huruf kapital/angka") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nama Voucher *") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Deskripsi (opsional)") },
                    modifier = Modifier.fillMaxWidth(), maxLines = 3
                )
            }
            item { HorizontalDivider() }
            item {
                Text("Konfigurasi Diskon", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
            item {
                Text("Tipe Diskon", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("percentage" to "Persen (%)", "nominal" to "Nominal (Rp)").forEach { (value, label) ->
                        FilterChip(selected = discountType == value,
                            onClick = { discountType = value }, label = { Text(label) })
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = discountValue,
                    onValueChange = { discountValue = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(if (isPct) "Nilai Diskon (%) *" else "Nilai Diskon (Rp) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = discountValueError != null,
                    supportingText = if (discountValueError != null) ({ Text(discountValueError) }) else null,
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
            if (isPct) {
                item {
                    OutlinedTextField(
                        value = maxDiscount,
                        onValueChange = { maxDiscount = it.filter { c -> c.isDigit() } },
                        label = { Text("Maks. Nominal Diskon (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = { Text("Batasi nilai Rp meski persennya besar") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = minPurchase,
                    onValueChange = { minPurchase = it.filter { c -> c.isDigit() } },
                    label = { Text("Min. Pembelian (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("0 = tidak ada minimum") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
            item { HorizontalDivider() }
            item {
                Text("Periode Berlaku", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
            item {
                DatePickerField(
                    label = "Berlaku Dari *",
                    value = validFrom,
                    onDateSelected = { validFrom = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                DatePickerField(
                    label = "Berlaku Sampai (opsional)",
                    value = validUntil,
                    onDateSelected = { validUntil = it },
                    isError = validUntilError != null,
                    supportingText = if (validUntilError != null) ({ Text(validUntilError) }) else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { HorizontalDivider() }
            item {
                Text("Penggunaan & Status", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
            item {
                OutlinedTextField(
                    value = usageLimitText,
                    onValueChange = { usageLimitText = it.filter { c -> c.isDigit() } },
                    label = { Text("Batas Penggunaan") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Kosongkan = tidak terbatas") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Voucher Aktif", style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium)
                        Text("Voucher bisa digunakan kasir",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        }

        // ── Bottom actions ────────────────────────────────────────────────────
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onClose, enabled = !isSubmitting,
                modifier = Modifier.weight(1f)
            ) { Text("Batal") }
            Button(
                onClick = {
                    onConfirm(
                        code.trim(), name.trim(), discountType, discountValue, validFrom,
                        description.ifBlank { null },
                        if (isPct) maxDiscount.ifBlank { null } else null,
                        minPurchase.ifBlank { "0" }, usageLimitText.toIntOrNull(),
                        validUntil.ifBlank { null }, isActive
                    )
                },
                enabled = canConfirm,
                modifier = Modifier.weight(1f)
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun VoucherFormContentAddPreview() {
    RancakTheme {
        VoucherFormContent(editing = null, isSubmitting = false, onBack = {},
            onConfirm = { _, _, _, _, _, _, _, _, _, _, _ -> })
    }
}

