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
import id.rancak.app.domain.model.Surcharge
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakOutlinedButton
import id.rancak.app.presentation.components.RancakTextField
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakTheme

private val SurchargeGradientEnd = Color(0xFF0B7A60)

// ── Shared form state + validation logic ──────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurchargeFormDialog(
    editing: Surcharge?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (orderType: String, name: String, amount: String, isPercentage: Boolean, maxAmount: String?) -> Unit
) {
    var orderType    by remember(editing) { mutableStateOf(editing?.orderType ?: "all") }
    var name         by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var amount       by remember(editing) { mutableStateOf(editing?.amount?.toString() ?: "") }
    var isPercentage by remember(editing) { mutableStateOf(editing?.isPercentage ?: false) }
    var maxAmount    by remember(editing) { mutableStateOf(editing?.maxAmount?.toString() ?: "") }

    val amountNum = amount.toDoubleOrNull()
    val amountError = when {
        amount.isBlank() -> null
        amountNum == null -> "Nilai tidak valid"
        amountNum <= 0 -> "Nilai harus lebih dari 0"
        isPercentage && amountNum > 100 -> "Persen tidak boleh melebihi 100"
        else -> null
    }
    val maxAmountNum = maxAmount.toDoubleOrNull()
    val maxAmountError = when {
        !isPercentage || maxAmount.isBlank() -> null
        maxAmountNum == null -> "Nilai tidak valid"
        maxAmountNum <= 0 -> "Harus lebih dari 0"
        else -> null
    }
    val canConfirm = !isSubmitting &&
        name.isNotBlank() &&
        amount.isNotBlank() && amountError == null &&
        maxAmountError == null

    val orderTypes = listOf("all" to "Semua", "dine_in" to "Dine In", "takeaway" to "Takeaway", "delivery" to "Delivery")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editing == null) "Tambah Surcharge" else "Edit Surcharge") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = orderTypes.firstOrNull { it.first == orderType }?.second ?: orderType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipe Pesanan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        orderTypes.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { orderType = value; expanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(if (isPercentage) "Jumlah (%) *" else "Jumlah (Rp) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError != null,
                    supportingText = amountError?.let { err -> { Text(err) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Berbasis Persentase", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isPercentage, onCheckedChange = { isPercentage = it })
                }

                if (isPercentage) {
                    OutlinedTextField(
                        value = maxAmount,
                        onValueChange = { maxAmount = it.filter { c -> c.isDigit() } },
                        label = { Text("Maks. Nominal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = maxAmountError != null,
                        supportingText = maxAmountError?.let { err -> { Text(err) } } ?: { Text("Batasi nominal meski persennya besar") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(orderType, name.trim(), amount, isPercentage, maxAmount.ifBlank { null }) },
                enabled = canConfirm
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") } }
    )
}

// ── Inline panel — dipakai di tablet (menggantikan panel kanan) ───────────────

@Composable
private fun SurchargeFormSection(
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
private fun SurchargeGradSaveButton(
    canConfirm: Boolean, isSubmitting: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    val gradStart by animateColorAsState(if (canConfirm) Primary else MaterialTheme.colorScheme.surfaceVariant, tween(250), "SurGradS")
    val gradEnd   by animateColorAsState(if (canConfirm) SurchargeGradientEnd else MaterialTheme.colorScheme.surfaceVariant, tween(250), "SurGradE")
    val textColor by animateColorAsState(if (canConfirm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, tween(250), "SurText")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurchargeFormPanel(
    editing: Surcharge?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (orderType: String, name: String, amount: String, isPercentage: Boolean, maxAmount: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var orderType    by remember(editing) { mutableStateOf(editing?.orderType ?: "all") }
    var name         by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var amount       by remember(editing) { mutableStateOf(editing?.amount?.toString() ?: "") }
    var isPercentage by remember(editing) { mutableStateOf(editing?.isPercentage ?: false) }
    var maxAmount    by remember(editing) { mutableStateOf(editing?.maxAmount?.toString() ?: "") }

    val amountNum = amount.toDoubleOrNull()
    val amountError = when {
        amount.isBlank() -> null
        amountNum == null -> "Nilai tidak valid"
        amountNum <= 0 -> "Nilai harus lebih dari 0"
        isPercentage && amountNum > 100 -> "Persen tidak boleh melebihi 100"
        else -> null
    }
    val maxAmountNum = maxAmount.toDoubleOrNull()
    val maxAmountError = when {
        !isPercentage || maxAmount.isBlank() -> null
        maxAmountNum == null -> "Nilai tidak valid"
        maxAmountNum <= 0 -> "Harus lebih dari 0"
        else -> null
    }
    val canConfirm = !isSubmitting &&
        name.isNotBlank() &&
        amount.isNotBlank() && amountError == null &&
        maxAmountError == null

    val orderTypes = listOf("all" to "Semua", "dine_in" to "Dine In", "takeaway" to "Takeaway", "delivery" to "Delivery")
    var expanded by remember { mutableStateOf(false) }

    val title    = if (editing == null) "Tambah Surcharge" else "Edit Surcharge"
    val subtitle = if (editing == null) "Isi data surcharge baru" else "Ubah informasi surcharge"

    Column(modifier = modifier) {
        // ── Gradient header with section icon ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Primary, SurchargeGradientEnd)))
        ) {
            Icon(
                Icons.Default.AddCircle, null,
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
                ) { Icon(Icons.Default.AddCircle, null, Modifier.size(20.dp), tint = Color.White) }
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
            SurchargeFormSection("Identitas", Icons.Default.Info) {
                RancakTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = "Nama *",
                    singleLine    = true
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    RancakTextField(
                        value         = orderTypes.firstOrNull { it.first == orderType }?.second ?: orderType,
                        onValueChange = {},
                        label         = "Tipe Pesanan",
                        singleLine    = true,
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        readOnly      = true,
                        modifier      = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        orderTypes.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { orderType = value; expanded = false })
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            SurchargeFormSection("Konfigurasi Biaya", Icons.Default.Settings) {
                RancakTextField(
                    value         = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label         = if (isPercentage) "Jumlah (%) *" else "Jumlah (Rp) *",
                    singleLine    = true,
                    isError       = amountError != null,
                    errorMessage  = amountError
                )
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
                            Text("Berbasis Persentase", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Dihitung dari total order", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isPercentage, onCheckedChange = { isPercentage = it })
                    }
                }
                if (isPercentage) {
                    RancakTextField(
                        value         = maxAmount,
                        onValueChange = { maxAmount = it.filter { c -> c.isDigit() } },
                        label         = "Maks. Nominal (Rp)",
                        singleLine    = true,
                        isError       = maxAmountError != null,
                        errorMessage  = maxAmountError
                    )
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
            SurchargeGradSaveButton(
                canConfirm   = canConfirm,
                isSubmitting = isSubmitting,
                modifier     = Modifier.weight(1f)
            ) { onConfirm(orderType, name.trim(), amount, isPercentage, maxAmount.ifBlank { null }) }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun SurchargeFormDialogAddPreview() {
    RancakTheme {
        SurchargeFormDialog(
            editing      = null,
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _ -> }
        )
    }
}

@Preview
@Composable
private fun SurchargeFormDialogEditPreview() {
    RancakTheme {
        SurchargeFormDialog(
            editing      = Surcharge("1", "all", "Biaya Layanan", 5L, true, null, true, 0),
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _ -> }
        )
    }
}


