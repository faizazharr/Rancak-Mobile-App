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
import id.rancak.app.domain.model.TaxConfig
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakOutlinedButton
import id.rancak.app.presentation.components.RancakTextField
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.PrimaryGradientEnd
import id.rancak.app.presentation.designsystem.RancakTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxFormDialog(
    editing: TaxConfig?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, rate: String, applyTo: String, sortOrder: Int) -> Unit
) {
    var name      by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var rate      by remember(editing) { mutableStateOf(editing?.rate?.toString() ?: "") }
    var applyTo   by remember(editing) { mutableStateOf(editing?.applyTo ?: "after_discount") }
    var sortOrder by remember(editing) { mutableStateOf(editing?.sortOrder?.toString() ?: "0") }
    var expanded  by remember { mutableStateOf(false) }

    val applyToOptions = listOf(
        "after_discount" to "Setelah Diskon",
        "subtotal"       to "Dari Subtotal"
    )

    val rateNum = rate.toDoubleOrNull()
    val rateError = when {
        rate.isBlank() -> null
        rateNum == null -> "Tarif tidak valid"
        rateNum <= 0 -> "Tarif harus lebih dari 0"
        rateNum > 100 -> "Tarif tidak boleh melebihi 100%"
        else -> null
    }
    val canConfirm = !isSubmitting && name.isNotBlank() && rate.isNotBlank() && rateError == null

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editing == null) "Tambah Pajak" else "Edit Pajak") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Tarif (%) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = rateError != null,
                    supportingText = rateError?.let { err -> { Text(err) } } ?: { Text("Contoh: 11 untuk PPN 11%") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = applyToOptions.firstOrNull { it.first == applyTo }?.second ?: applyTo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Diterapkan Pada") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        applyToOptions.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { applyTo = value; expanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = sortOrder,
                    onValueChange = { sortOrder = it.filter { c -> c.isDigit() } },
                    label = { Text("Urutan Tampil") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), rate, applyTo, sortOrder.toIntOrNull() ?: 0) },
                enabled = canConfirm
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") } }
    )
}

// ── Inline panel — dipakai di tablet ─────────────────────────────────────────

@Composable
private fun TaxFormSection(
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
private fun TaxGradSaveButton(
    canConfirm: Boolean, isSubmitting: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    val gradStart by animateColorAsState(if (canConfirm) Primary else MaterialTheme.colorScheme.surfaceVariant, tween(250), "TaxGradS")
    val gradEnd   by animateColorAsState(if (canConfirm) PrimaryGradientEnd else MaterialTheme.colorScheme.surfaceVariant, tween(250), "TaxGradE")
    val textColor by animateColorAsState(if (canConfirm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, tween(250), "TaxText")
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
fun TaxFormPanel(
    editing: TaxConfig?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, rate: String, applyTo: String, sortOrder: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var name      by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var rate      by remember(editing) { mutableStateOf(editing?.rate?.toString() ?: "") }
    var applyTo   by remember(editing) { mutableStateOf(editing?.applyTo ?: "after_discount") }
    var sortOrder by remember(editing) { mutableStateOf(editing?.sortOrder?.toString() ?: "0") }
    var expanded  by remember { mutableStateOf(false) }

    val applyToOptions = listOf(
        "after_discount" to "Setelah Diskon",
        "subtotal"       to "Dari Subtotal"
    )
    val rateNum = rate.toDoubleOrNull()
    val rateError = when {
        rate.isBlank() -> null
        rateNum == null -> "Tarif tidak valid"
        rateNum <= 0 -> "Tarif harus lebih dari 0"
        rateNum > 100 -> "Tarif tidak boleh melebihi 100%"
        else -> null
    }
    val canConfirm = !isSubmitting && name.isNotBlank() && rate.isNotBlank() && rateError == null

    val title    = if (editing == null) "Tambah Pajak" else "Edit Pajak"
    val subtitle = if (editing == null) "Isi data pajak baru" else "Ubah informasi pajak"

    Column(modifier = modifier) {
        // ── Gradient header with section icon ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Primary, PrimaryGradientEnd)))
        ) {
            Icon(
                Icons.Default.Percent, null,
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
                ) { Icon(Icons.Default.Percent, null, Modifier.size(20.dp), tint = Color.White) }
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
            TaxFormSection("Identitas Pajak", Icons.Default.Info) {
                RancakTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = "Nama *",
                    singleLine    = true
                )
                RancakTextField(
                    value         = rate,
                    onValueChange = { rate = it.filter { c -> c.isDigit() || c == '.' } },
                    label         = "Tarif (%) *",
                    singleLine    = true,
                    isError       = rateError != null,
                    errorMessage  = rateError
                )
                if (rateError == null) {
                    Text(
                        "Contoh: 11 untuk PPN 11%",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            TaxFormSection("Penerapan", Icons.Default.Settings) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    RancakTextField(
                        value         = applyToOptions.firstOrNull { it.first == applyTo }?.second ?: applyTo,
                        onValueChange = {},
                        label         = "Diterapkan Pada",
                        singleLine    = true,
                        readOnly      = true,
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier      = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        applyToOptions.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { applyTo = value; expanded = false })
                        }
                    }
                }
                RancakTextField(
                    value         = sortOrder,
                    onValueChange = { sortOrder = it.filter { c -> c.isDigit() } },
                    label         = "Urutan Tampil",
                    singleLine    = true
                )
            }
        }

        // ── Bottom actions ───────────────────────────────────────────────────
        HorizontalDivider()
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RancakOutlinedButton(text = "Batal", onClick = onDismiss, enabled = !isSubmitting, modifier = Modifier.weight(1f))
            TaxGradSaveButton(
                canConfirm   = canConfirm,
                isSubmitting = isSubmitting,
                modifier     = Modifier.weight(1f)
            ) { onConfirm(name.trim(), rate, applyTo, sortOrder.toIntOrNull() ?: 0) }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun TaxFormDialogAddPreview() {
    RancakTheme {
        TaxFormDialog(
            editing      = null,
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _ -> }
        )
    }
}

@Preview
@Composable
private fun TaxFormDialogEditPreview() {
    RancakTheme {
        TaxFormDialog(
            editing      = TaxConfig("1", "PPN", 11.0, "after_discount", 1, true),
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _ -> }
        )
    }
}

