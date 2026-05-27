package id.rancak.app.presentation.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.Product
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.PrimaryGradientEnd
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.components.RancakFormDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween

// ── Shared form body ──────────────────────────────────────────────────────────

@Composable
private fun StockAdjustBody(
    product: Product,
    adjustType: String,
    onAdjustTypeChange: (String) -> Unit,
    quantityText: String,
    onQuantityChange: (String) -> Unit,
    noteText: String,
    onNoteChange: (String) -> Unit,
    qtyError: String?
) {
    val sem = RancakColors.semantic
    val stockColor = when {
        product.stock <= 0 -> MaterialTheme.colorScheme.error
        product.stock <= 5 -> sem.warning
        else               -> sem.success
    }

    // ── Stock info card ───────────────────────────────────────────────────
    Surface(
        shape           = MaterialTheme.shapes.medium,
        color           = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(36.dp)
                    .background(Primary.copy(alpha = 0.12f), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory, null, tint = Primary, modifier = Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (!product.category?.name.isNullOrBlank()) {
                    Text(
                        product.category!!.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${product.stock.toStockDisplay()} ${product.unit ?: ""}".trim(),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = stockColor
                )
                Text("stok saat ini", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    // ── Adjust type selector ──────────────────────────────────────────────
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "JENIS PENYESUAIAN",
            style         = MaterialTheme.typography.labelSmall,
            fontWeight    = FontWeight.Bold,
            color         = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.6.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StockTypeButton(
                label     = "Masuk (+)",
                icon      = Icons.Default.AddCircleOutline,
                selected  = adjustType == "in",
                color     = sem.success,
                modifier  = Modifier.weight(1f),
                onClick   = { onAdjustTypeChange("in") }
            )
            StockTypeButton(
                label    = "Keluar (−)",
                icon     = Icons.Default.RemoveCircleOutline,
                selected = adjustType == "out",
                color    = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
                onClick  = { onAdjustTypeChange("out") }
            )
        }
    }

    // ── Quantity field ────────────────────────────────────────────────────
    OutlinedTextField(
        value           = quantityText,
        onValueChange   = onQuantityChange,
        label           = { Text("Jumlah *") },
        placeholder     = { Text("Masukkan jumlah") },
        leadingIcon     = {
            Icon(
                if (adjustType == "in") Icons.Default.AddCircleOutline else Icons.Default.RemoveCircleOutline,
                null,
                tint = if (adjustType == "in") sem.success else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError         = qtyError != null,
        supportingText  = qtyError?.let { { Text(it) } },
        modifier        = Modifier.fillMaxWidth(),
        singleLine      = true,
        shape           = MaterialTheme.shapes.medium
    )

    // ── Note field ────────────────────────────────────────────────────────
    OutlinedTextField(
        value         = noteText,
        onValueChange = onNoteChange,
        label         = { Text("Catatan (opsional)") },
        placeholder   = { Text("Contoh: stok fisik, retur supplier…") },
        leadingIcon   = { Icon(Icons.Default.Notes, null, modifier = Modifier.size(20.dp)) },
        modifier      = Modifier.fillMaxWidth(),
        maxLines      = 3,
        shape         = MaterialTheme.shapes.medium
    )
}

@Composable
private fun StockTypeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor      by animateColorAsState(if (selected) color.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface, tween(220), label = "StockTypeBg")
    val borderColor  by animateColorAsState(if (selected) color else MaterialTheme.colorScheme.outlineVariant, tween(220), label = "StockTypeBorder")
    val borderWidth  by animateDpAsState(if (selected) 1.5.dp else 1.dp, tween(220), label = "StockTypeBorderW")
    val contentColor by animateColorAsState(if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant, tween(220), label = "StockTypeContent")
    Surface(
        modifier        = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication        = null,
            onClick           = onClick
        ),
        shape           = MaterialTheme.shapes.medium,
        color           = bgColor,
        border          = androidx.compose.foundation.BorderStroke(width = borderWidth, color = borderColor)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color      = contentColor
            )
        }
    }
}

@Composable
private fun GradientSaveButton(canConfirm: Boolean, isSubmitting: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val gradStart by animateColorAsState(if (canConfirm) Primary      else MaterialTheme.colorScheme.surfaceVariant, tween(250), label = "GradSaveStart")
    val gradEnd   by animateColorAsState(if (canConfirm) PrimaryGradientEnd  else MaterialTheme.colorScheme.surfaceVariant, tween(250), label = "GradSaveEnd")
    val textColor by animateColorAsState(if (canConfirm) Color.White  else MaterialTheme.colorScheme.onSurfaceVariant, tween(250), label = "GradSaveText")
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Brush.horizontalGradient(listOf(gradStart, gradEnd)))
            .clickable(
                enabled           = canConfirm,
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSubmitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
        else Text(
            "Simpan",
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color      = textColor
        )
    }
}

// ── Dialog (phone) ────────────────────────────────────────────────────────────

@Composable
fun StockAdjustDialog(
    product: Product,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (type: String, qty: Double, note: String?) -> Unit
) {
    var adjustType   by remember { mutableStateOf("in") }
    var quantityText by remember { mutableStateOf("") }
    var noteText     by remember { mutableStateOf("") }

    val qty      = quantityText.toDoubleOrNull()
    val qtyError = when {
        quantityText.isBlank() -> null
        qty == null            -> "Angka tidak valid"
        qty <= 0               -> "Harus lebih dari 0"
        else                   -> null
    }
    val canConfirm = !isSubmitting && qty != null && qty > 0

    RancakFormDialog(
        icon             = Icons.Default.SwapVert,
        title            = "Sesuaikan Stok",
        subtitle         = product.name,
        onDismissRequest = onDismiss,
        confirmLabel     = "Simpan",
        onConfirm        = { onConfirm(adjustType, qty!!, noteText.ifBlank { null }) },
        confirmEnabled   = canConfirm,
        isSubmitting     = isSubmitting
    ) {
        StockAdjustBody(
            product            = product,
            adjustType         = adjustType,
            onAdjustTypeChange = { adjustType = it },
            quantityText       = quantityText,
            onQuantityChange   = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
            noteText           = noteText,
            onNoteChange       = { noteText = it },
            qtyError           = qtyError
        )
    }
}

// ── Inline panel (tablet) ─────────────────────────────────────────────────────

@Composable
fun StockAdjustPanel(
    product: Product,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (type: String, qty: Double, note: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var adjustType   by remember(product) { mutableStateOf("in") }
    var quantityText by remember(product) { mutableStateOf("") }
    var noteText     by remember(product) { mutableStateOf("") }

    val qty      = quantityText.toDoubleOrNull()
    val qtyError = when {
        quantityText.isBlank() -> null
        qty == null            -> "Angka tidak valid"
        qty <= 0               -> "Harus lebih dari 0"
        else                   -> null
    }
    val canConfirm = !isSubmitting && qty != null && qty > 0

    Column(modifier = modifier) {
        // ── Gradient header ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Primary, PrimaryGradientEnd)))
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = { if (!isSubmitting) onDismiss() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        "Sesuaikan Stok",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    Text(
                        product.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
            }
        }

        // ── Form fields ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .widthIn(max = 560.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StockAdjustBody(
                product            = product,
                adjustType         = adjustType,
                onAdjustTypeChange = { adjustType = it },
                quantityText       = quantityText,
                onQuantityChange   = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                noteText           = noteText,
                onNoteChange       = { noteText = it },
                qtyError           = qtyError
            )
        }

        // ── Action buttons ────────────────────────────────────────────────────
        HorizontalDivider()
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            TextButton(onClick = { if (!isSubmitting) onDismiss() }, enabled = !isSubmitting) { Text("Batal") }
            Spacer(Modifier.width(10.dp))
            GradientSaveButton(canConfirm = canConfirm, isSubmitting = isSubmitting, modifier = Modifier.width(140.dp)) {
                onConfirm(adjustType, qty!!, noteText.ifBlank { null })
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun StockAdjustDialogPreview() {
    RancakTheme {
        StockAdjustDialog(
            product = Product(
                uuid = "1", sku = null, barcode = null, name = "Nasi Goreng",
                description = null, category = Category("c1", "Makanan", null),
                price = 25000L, stock = 8.0, unit = "porsi",
                imageUrl = null, isActive = true, hasExpiry = false, updatedAt = null
            ),
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _ -> }
        )
    }
}
