package id.rancak.app.presentation.ui.pricing.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.DiscountRule
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakFormDialog
import id.rancak.app.presentation.components.RancakOutlinedButton
import id.rancak.app.presentation.components.RancakTextField
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakTheme

private val DiscountGradientEnd = Color(0xFF0B7A60)

@Composable
fun DiscountFormDialog(
    editing: DiscountRule?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String, discountValue: Double, discountType: String,
        ruleType: String, isActive: Boolean, description: String?,
        maxDiscount: Double?, minPurchaseAmount: Double?
    ) -> Unit
) {
    var name          by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var discountValue by remember(editing) { mutableStateOf(editing?.discountValue?.toString() ?: "") }
    var discountType  by remember(editing) { mutableStateOf(editing?.discountType ?: "pct") }
    var ruleType      by remember(editing) { mutableStateOf(editing?.ruleType ?: "always") }
    var isActive      by remember(editing) { mutableStateOf(editing?.isActive ?: true) }
    var description   by remember(editing) { mutableStateOf(editing?.description ?: "") }
    var maxDiscount   by remember(editing) { mutableStateOf(editing?.maxDiscount?.toString() ?: "") }
    var minPurchase   by remember(editing) { mutableStateOf(editing?.minPurchaseAmount?.toString() ?: "") }

    val isPct = discountType == "pct"
    val discountNum = discountValue.toDoubleOrNull()
    val discountError = when {
        discountValue.isBlank() -> null
        discountNum == null -> "Nilai tidak valid"
        discountNum <= 0 -> "Nilai harus lebih dari 0"
        isPct && discountNum > 100 -> "Persen tidak boleh melebihi 100"
        else -> null
    }
    val maxDiscountNum = maxDiscount.toDoubleOrNull()
    val maxDiscountError = when {
        maxDiscount.isBlank() -> null
        maxDiscountNum == null -> "Nilai tidak valid"
        maxDiscountNum <= 0 -> "Harus lebih dari 0"
        else -> null
    }
    val canConfirm = !isSubmitting &&
        name.isNotBlank() &&
        discountValue.isNotBlank() && discountError == null &&
        maxDiscountError == null

    RancakFormDialog(
        icon             = Icons.Default.LocalOffer,
        title            = if (editing == null) "Tambah Aturan Diskon" else "Edit Aturan Diskon",
        subtitle         = if (editing == null) "Buat aturan diskon baru" else "Perbarui aturan diskon",
        onDismissRequest = onDismiss,
        confirmLabel     = "Simpan",
        onConfirm        = {
            onConfirm(
                name.trim(), discountValue.toDoubleOrNull() ?: 0.0, discountType,
                ruleType, isActive, description.ifBlank { null },
                maxDiscount.toDoubleOrNull(), minPurchase.toDoubleOrNull()
            )
        },
        confirmEnabled   = canConfirm,
        isSubmitting     = isSubmitting
    ) {
        OutlinedTextField(
            value         = name,
            onValueChange = { name = it },
            label         = { Text("Nama *") },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
            shape         = MaterialTheme.shapes.medium
        )
        OutlinedTextField(
            value         = description,
            onValueChange = { description = it },
            label         = { Text("Deskripsi") },
            modifier      = Modifier.fillMaxWidth(),
            maxLines      = 2,
            shape         = MaterialTheme.shapes.medium
        )

        Text("Tipe Diskon", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("pct" to "Persen (%)", "flat" to "Nominal (Rp)").forEach { (value, label) ->
                FilterChip(
                    selected = discountType == value,
                    onClick  = { discountType = value },
                    label    = { Text(label) }
                )
            }
        }

        OutlinedTextField(
            value           = discountValue,
            onValueChange   = { discountValue = it.filter { c -> c.isDigit() || c == '.' } },
            label           = { Text(if (isPct) "Nilai Diskon (%) *" else "Nilai Diskon (Rp) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError         = discountError != null,
            supportingText  = discountError?.let { err -> { Text(err) } },
            modifier        = Modifier.fillMaxWidth(),
            singleLine      = true,
            shape           = MaterialTheme.shapes.medium
        )

        Text("Jenis Aturan", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("always" to "Selalu", "time_based" to "Berbasis Waktu").forEach { (value, label) ->
                FilterChip(
                    selected = ruleType == value,
                    onClick  = { ruleType = value },
                    label    = { Text(label) }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value           = minPurchase,
                onValueChange   = { minPurchase = it.filter { c -> c.isDigit() || c == '.' } },
                label           = { Text("Min. Pembelian (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText  = { Text("0 = tanpa minimum") },
                modifier        = Modifier.weight(1f),
                singleLine      = true,
                shape           = MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value           = maxDiscount,
                onValueChange   = { maxDiscount = it.filter { c -> c.isDigit() || c == '.' } },
                label           = { Text("Maks. Diskon (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError         = maxDiscountError != null,
                supportingText  = maxDiscountError?.let { err -> { Text(err) } },
                modifier        = Modifier.weight(1f),
                singleLine      = true,
                shape           = MaterialTheme.shapes.medium
            )
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Aktif", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = isActive, onCheckedChange = { isActive = it })
        }
    }
}

// ── Inline panel — dipakai di tablet ─────────────────────────────────────────

@Composable
private fun DiscountFormSection(
    title: String, icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier         = Modifier.size(22.dp).clip(MaterialTheme.shapes.small)
                    .background(Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = Primary, modifier = Modifier.size(12.dp)) }
            Text(
                title,
                style         = MaterialTheme.typography.labelMedium,
                fontWeight    = FontWeight.SemiBold,
                color         = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        }
        content()
    }
}

@Composable
private fun DiscountGradSaveButton(
    canConfirm: Boolean, isSubmitting: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    val gradStart by animateColorAsState(if (canConfirm) Primary else MaterialTheme.colorScheme.surfaceVariant, tween(250), "DiscGradS")
    val gradEnd   by animateColorAsState(if (canConfirm) DiscountGradientEnd else MaterialTheme.colorScheme.surfaceVariant, tween(250), "DiscGradE")
    val textColor by animateColorAsState(if (canConfirm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, tween(250), "DiscText")
    Box(
        modifier = modifier.height(44.dp).clip(MaterialTheme.shapes.medium)
            .background(Brush.horizontalGradient(listOf(gradStart, gradEnd)))
            .clickable(enabled = canConfirm && !isSubmitting, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSubmitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
        else Text("Simpan", color = textColor, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DiscountFormPanel(
    editing: DiscountRule?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String, discountValue: Double, discountType: String,
        ruleType: String, isActive: Boolean, description: String?,
        maxDiscount: Double?, minPurchaseAmount: Double?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var name          by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var discountValue by remember(editing) { mutableStateOf(editing?.discountValue?.toString() ?: "") }
    var discountType  by remember(editing) { mutableStateOf(editing?.discountType ?: "pct") }
    var ruleType      by remember(editing) { mutableStateOf(editing?.ruleType ?: "always") }
    var isActive      by remember(editing) { mutableStateOf(editing?.isActive ?: true) }
    var description   by remember(editing) { mutableStateOf(editing?.description ?: "") }
    var maxDiscount   by remember(editing) { mutableStateOf(editing?.maxDiscount?.toString() ?: "") }
    var minPurchase   by remember(editing) { mutableStateOf(editing?.minPurchaseAmount?.toString() ?: "") }

    val isPct = discountType == "pct"
    val discountNum = discountValue.toDoubleOrNull()
    val discountError = when {
        discountValue.isBlank() -> null
        discountNum == null -> "Nilai tidak valid"
        discountNum <= 0 -> "Nilai harus lebih dari 0"
        isPct && discountNum > 100 -> "Persen tidak boleh melebihi 100"
        else -> null
    }
    val maxDiscountNum = maxDiscount.toDoubleOrNull()
    val maxDiscountError = when {
        maxDiscount.isBlank() -> null
        maxDiscountNum == null -> "Nilai tidak valid"
        maxDiscountNum <= 0 -> "Harus lebih dari 0"
        else -> null
    }
    val canConfirm = !isSubmitting &&
        name.isNotBlank() &&
        discountValue.isNotBlank() && discountError == null &&
        maxDiscountError == null

    val title    = if (editing == null) "Tambah Aturan Diskon" else "Edit Aturan Diskon"
    val subtitle = if (editing == null) "Isi data aturan diskon baru" else "Ubah informasi aturan diskon"

    Column(modifier = modifier) {
        // ── Gradient header with section icon ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Primary, DiscountGradientEnd)))
        ) {
            Icon(
                Icons.Default.LocalOffer, null,
                modifier = Modifier.size(80.dp).align(Alignment.CenterEnd).padding(end = 12.dp),
                tint     = Color.White.copy(alpha = 0.10f)
            )
            Row(
                modifier              = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                }
                Box(
                    modifier         = Modifier.size(36.dp).clip(MaterialTheme.shapes.medium).background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.LocalOffer, null, Modifier.size(20.dp), tint = Color.White) }
                Column {
                    Text(title,    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall,   color = Color.White.copy(alpha = 0.78f))
                }
            }
        }

        // ── Form fields ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            DiscountFormSection("Informasi", Icons.Default.Info) {
                RancakTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = "Nama *",
                    singleLine    = true
                )
                RancakTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = "Deskripsi",
                    maxLines      = 2
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            DiscountFormSection("Konfigurasi Diskon", Icons.Default.Settings) {
                Text("Tipe Diskon", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("pct" to "Persen (%)", "flat" to "Nominal (Rp)").forEach { (value, label) ->
                        FilterChip(
                            selected = discountType == value,
                            onClick  = { discountType = value },
                            label    = { Text(label) }
                        )
                    }
                }
                RancakTextField(
                    value         = discountValue,
                    onValueChange = { discountValue = it.filter { c -> c.isDigit() || c == '.' } },
                    label         = if (isPct) "Nilai Diskon (%) *" else "Nilai Diskon (Rp) *",
                    singleLine    = true,
                    isError       = discountError != null,
                    errorMessage  = discountError
                )
                Text("Jenis Aturan", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("always" to "Selalu", "time_based" to "Berbasis Waktu").forEach { (value, label) ->
                        FilterChip(
                            selected = ruleType == value,
                            onClick  = { ruleType = value },
                            label    = { Text(label) }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            DiscountFormSection("Batas & Status", Icons.Default.CheckCircle) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RancakTextField(
                        value         = minPurchase,
                        onValueChange = { minPurchase = it.filter { c -> c.isDigit() || c == '.' } },
                        label         = "Min. Pembelian (Rp)",
                        singleLine    = true,
                        modifier      = Modifier.weight(1f)
                    )
                    RancakTextField(
                        value         = maxDiscount,
                        onValueChange = { maxDiscount = it.filter { c -> c.isDigit() || c == '.' } },
                        label         = "Maks. Diskon (Rp)",
                        singleLine    = true,
                        isError       = maxDiscountError != null,
                        errorMessage  = maxDiscountError,
                        modifier      = Modifier.weight(1f)
                    )
                }
                Surface(
                    shape    = MaterialTheme.shapes.medium,
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Aktif", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Diskon diterapkan ke transaksi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }
                }
            }
        }

        // ── Bottom actions ───────────────────────────────────────────────────
        HorizontalDivider()
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RancakOutlinedButton(text = "Batal", onClick = onDismiss, enabled = !isSubmitting, modifier = Modifier.weight(1f))
            DiscountGradSaveButton(
                canConfirm   = canConfirm,
                isSubmitting = isSubmitting,
                modifier     = Modifier.weight(1f)
            ) {
                onConfirm(
                    name.trim(), discountValue.toDoubleOrNull() ?: 0.0, discountType,
                    ruleType, isActive, description.ifBlank { null },
                    maxDiscount.toDoubleOrNull(), minPurchase.toDoubleOrNull()
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun DiscountFormDialogAddPreview() {
    RancakTheme {
        DiscountFormDialog(
            editing      = null,
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _, _, _, _ -> }
        )
    }
}

@Preview
@Composable
private fun DiscountFormDialogEditPreview() {
    RancakTheme {
        DiscountFormDialog(
            editing      = DiscountRule("1", "Diskon Happy Hour", null, "time_based", "pct", 10.0, null, null, null, null, 0, false, null, true),
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _, _, _, _ -> }
        )
    }
}

