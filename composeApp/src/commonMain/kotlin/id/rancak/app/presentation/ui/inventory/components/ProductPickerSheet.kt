package id.rancak.app.presentation.ui.inventory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.rancak.app.domain.model.Product
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.PrimaryGradientEnd
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet


// ── Shared private composables ────────────────────────────────────────────────

@Composable
private fun ProductItem(product: Product, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .background(if (isSelected) Primary.copy(alpha = 0.07f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Checkbox(
            checked         = isSelected,
            onCheckedChange = { onToggle() },
            modifier        = Modifier.size(20.dp)
        )
        Box(
            modifier         = Modifier
                .size(36.dp)
                .background(
                    if (isSelected) Primary.copy(alpha = 0.14f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Inventory, null,
                tint     = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (product.category != null) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            product.category.name,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            val stockLabel = buildString {
                append("Stok sistem: ${formatStockValue(product.stock)}")
                if (!product.unit.isNullOrBlank()) append(" ${product.unit}")
            }
            Text(
                stockLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 58.dp))
}

@Composable
private fun ProductEmptyState(query: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            if (query.isBlank()) "Semua produk sudah ditambahkan" else "Produk tidak ditemukan",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GradientConfirmButton(selectedCount: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isEnabled = selectedCount > 0
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (isEnabled) Brush.horizontalGradient(listOf(Primary, GradientEnd))
                else Brush.horizontalGradient(listOf(surfaceVariant, surfaceVariant))
            )
            .clickable(
                enabled           = isEnabled,
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isEnabled) {
                Box(
                    modifier         = Modifier.size(20.dp).background(Color.White.copy(alpha = 0.20f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$selectedCount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Text(
                if (!isEnabled) "Pilih produk terlebih dahulu" else "Tambah Produk",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color      = if (isEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPickerSheet(
    products: ImmutableList<Product>,
    existingUuids: ImmutableSet<String>,
    onConfirm: (entries: Map<String, String>) -> Unit,
    onDismiss: () -> Unit,
    isTablet: Boolean = false
) {
    var query by remember { mutableStateOf("") }
    var selectedUuids by remember { mutableStateOf<Set<String>>(emptySet()) }
    val available = remember(products, existingUuids, query) {
        products
            .filter { it.uuid !in existingUuids }
            .let { list ->
                if (query.isBlank()) list
                else list.filter { it.name.contains(query, ignoreCase = true) }
            }
    }

    fun handleConfirm() {
        val entries = selectedUuids.associateWith { uuid ->
            products.find { it.uuid == uuid }?.stock?.toString() ?: "0"
        }
        onConfirm(entries)
    }

    if (isTablet) {
        Dialog(
            onDismissRequest = onDismiss,
            properties       = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier  = Modifier.width(520.dp),
                shape     = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // ── Gradient header ───────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(Primary, GradientEnd)))
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier         = Modifier
                                    .size(42.dp)
                                    .background(Color.White.copy(alpha = 0.18f), MaterialTheme.shapes.medium),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Inventory, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text(
                                    "Pilih Produk",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = Color.White
                                )
                                Text(
                                    "${available.size} produk tersedia",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.78f)
                                )
                            }
                        }
                    }

                    // ── Body ──────────────────────────────────────────────────
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value         = query,
                            onValueChange = { query = it },
                            placeholder   = { Text("Cari produk…") },
                            leadingIcon   = { Icon(Icons.Default.Search, null) },
                            trailingIcon  = if (query.isNotEmpty()) {
                                { IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, null) } }
                            } else null,
                            modifier   = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape      = MaterialTheme.shapes.medium
                        )

                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                            if (available.isEmpty()) {
                                item { ProductEmptyState(query) }
                            } else {
                                items(available, key = { it.uuid }) { product ->
                                    val isSelected = product.uuid in selectedUuids
                                    ProductItem(
                                        product    = product,
                                        isSelected = isSelected,
                                        onToggle   = {
                                            selectedUuids = if (isSelected) selectedUuids - product.uuid
                                            else selectedUuids + product.uuid
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Batal") }
                            GradientConfirmButton(
                                selectedCount = selectedUuids.size,
                                modifier      = Modifier.weight(2f),
                                onClick       = ::handleConfirm
                            )
                        }
                    }
                }
            }
        }
    } else {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ── Header ────────────────────────────────────────────────────
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 4.dp, bottom = 4.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(36.dp)
                            .background(Primary.copy(alpha = 0.12f), MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Inventory, null, tint = Primary, modifier = Modifier.size(18.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Pilih Produk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${available.size} produk tersedia",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Tutup") }
                }

                OutlinedTextField(
                    value         = query,
                    onValueChange = { query = it },
                    placeholder   = { Text("Cari produk…") },
                    leadingIcon   = { Icon(Icons.Default.Search, null) },
                    trailingIcon  = if (query.isNotEmpty()) {
                        { IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, null) } }
                    } else null,
                    modifier   = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    shape      = MaterialTheme.shapes.medium
                )

                HorizontalDivider()

                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    if (available.isEmpty()) {
                        item { ProductEmptyState(query) }
                    } else {
                        items(available, key = { it.uuid }) { product ->
                            val isSelected = product.uuid in selectedUuids
                            ProductItem(
                                product    = product,
                                isSelected = isSelected,
                                onToggle   = {
                                    selectedUuids = if (isSelected) selectedUuids - product.uuid
                                    else selectedUuids + product.uuid
                                }
                            )
                        }
                    }
                }

                GradientConfirmButton(
                    selectedCount = selectedUuids.size,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp, top = 8.dp),
                    onClick       = ::handleConfirm
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPickerSheet(
    products: ImmutableList<Product>,
    existingUuids: ImmutableSet<String>,
    onConfirm: (entries: Map<String, String>) -> Unit,
    onDismiss: () -> Unit
) = ProductPickerSheet(
    products      = products,
    existingUuids = existingUuids,
    onConfirm     = onConfirm,
    onDismiss     = onDismiss,
    isTablet      = false
)

