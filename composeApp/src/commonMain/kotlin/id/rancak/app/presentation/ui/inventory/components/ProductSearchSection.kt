package id.rancak.app.presentation.ui.inventory.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Product
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet

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

    if (isTablet) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Column {
                    Text(
                        "Pilih Produk",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${available.size} produk tersedia",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value         = query,
                        onValueChange = { query = it },
                        placeholder   = { Text("Cari produk…") },
                        leadingIcon   = { Icon(Icons.Default.Search, null) },
                        trailingIcon  = if (query.isNotEmpty()) {
                            { IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, null) } }
                        } else null,
                        modifier   = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                        if (available.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (query.isBlank()) "Semua produk sudah ditambahkan"
                                        else "Produk tidak ditemukan",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(available, key = { it.uuid }) { product ->
                                val isSelected = product.uuid in selectedUuids
                                ListItem(
                                    headlineContent   = { Text(product.name) },
                                    supportingContent = { Text("Stok sistem: ${formatStockValue(product.stock)}") },
                                    leadingContent    = {
                                        Checkbox(
                                            checked         = isSelected,
                                            onCheckedChange = { checked ->
                                                selectedUuids = if (checked) selectedUuids + product.uuid
                                                else selectedUuids - product.uuid
                                            }
                                        )
                                    },
                                    modifier = Modifier.clickable {
                                        selectedUuids = if (isSelected) selectedUuids - product.uuid
                                        else selectedUuids + product.uuid
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val entries = selectedUuids.associateWith { uuid ->
                            products.find { it.uuid == uuid }?.stock?.toString() ?: "0"
                        }
                        onConfirm(entries)
                    },
                    enabled = selectedUuids.isNotEmpty()
                ) {
                    Text(
                        if (selectedUuids.isEmpty()) "Pilih produk terlebih dahulu"
                        else "Tambah ${selectedUuids.size} Produk"
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Batal") }
            }
        )
    } else {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Pilih Produk",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${available.size} produk tersedia",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Search
                OutlinedTextField(
                    value         = query,
                    onValueChange = { query = it },
                    placeholder   = { Text("Cari produk…") },
                    leadingIcon   = { Icon(Icons.Default.Search, null) },
                    trailingIcon  = if (query.isNotEmpty()) {
                        { IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, null) } }
                    } else null,
                    modifier   = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )

                HorizontalDivider()

                // Product list
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    if (available.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (query.isBlank()) "Semua produk sudah ditambahkan"
                                    else "Produk tidak ditemukan",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(available, key = { it.uuid }) { product ->
                            val isSelected = product.uuid in selectedUuids
                            ListItem(
                                headlineContent   = { Text(product.name) },
                                supportingContent = { Text("Stok sistem: ${formatStockValue(product.stock)}") },
                                leadingContent    = {
                                    Checkbox(
                                        checked         = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedUuids = if (checked) selectedUuids + product.uuid
                                            else selectedUuids - product.uuid
                                        }
                                    )
                                },
                                modifier = Modifier.clickable {
                                    selectedUuids = if (isSelected) selectedUuids - product.uuid
                                    else selectedUuids + product.uuid
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }

                // Confirm button
                Button(
                    onClick = {
                        val entries = selectedUuids.associateWith { uuid ->
                            products.find { it.uuid == uuid }?.stock?.toString() ?: "0"
                        }
                        onConfirm(entries)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp, top = 8.dp),
                    enabled = selectedUuids.isNotEmpty()
                ) {
                    Text(
                        if (selectedUuids.isEmpty()) "Pilih produk terlebih dahulu"
                        else "Tambah ${selectedUuids.size} Produk"
                    )
                }
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Pilih Produk",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${available.size} produk tersedia",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            // Search
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Cari produk…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = if (query.isNotEmpty()) {
                    { IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, null) } }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            HorizontalDivider()

            // Product list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (available.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (query.isBlank()) "Semua produk sudah ditambahkan"
                                else "Produk tidak ditemukan",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(available, key = { it.uuid }) { product ->
                        val isSelected = product.uuid in selectedUuids
                        ListItem(
                            headlineContent = { Text(product.name) },
                            supportingContent = { Text("Stok sistem: ${formatStockValue(product.stock)}") },
                            leadingContent = {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedUuids = if (checked) selectedUuids + product.uuid
                                        else selectedUuids - product.uuid
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                selectedUuids = if (isSelected) selectedUuids - product.uuid
                                else selectedUuids + product.uuid
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }

            // Confirm button
            Button(
                onClick = {
                    val entries = selectedUuids.associateWith { uuid ->
                        products.find { it.uuid == uuid }?.stock?.toString() ?: "0"
                    }
                    onConfirm(entries)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp, top = 8.dp),
                enabled = selectedUuids.isNotEmpty()
            ) {
                Text(
                    if (selectedUuids.isEmpty()) "Pilih produk terlebih dahulu"
                    else "Tambah ${selectedUuids.size} Produk"
                )
            }
        }
    }
}

