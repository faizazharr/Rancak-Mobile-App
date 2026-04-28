package id.rancak.app.presentation.ui.pricing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.DiscountRule
import id.rancak.app.domain.model.Surcharge
import id.rancak.app.domain.model.TaxConfig
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.PricingManagementViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingManagementScreen(
    onBack: () -> Unit,
    viewModel: PricingManagementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { scope.launch { snackbarHostState.showSnackbar(it) }; viewModel.clearSuccessMessage() }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { scope.launch { snackbarHostState.showSnackbar("⚠ $it") }; viewModel.clearError() }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Surcharge", "Pajak", "Diskon")

    Scaffold(
        topBar = {
            RancakTopBar(title = "Harga & Diskon", icon = Icons.Default.LocalOffer, onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                when (selectedTab) {
                    0 -> viewModel.openSurchargeForm()
                    1 -> viewModel.openTaxForm()
                    else -> viewModel.openDiscountForm()
                }
            }) { Icon(Icons.Default.Add, contentDescription = "Tambah") }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title) })
                }
            }

            if (uiState.isLoading) {
                LoadingScreen()
            } else {
                when (selectedTab) {
                    0 -> SurchargeTab(uiState.surcharges, viewModel)
                    1 -> TaxTab(uiState.taxConfigs, viewModel)
                    else -> DiscountTab(uiState.discountRules, viewModel)
                }
            }
        }

        // ── Surcharge dialogs ────────────────────────────────────────────────
        if (uiState.showSurchargeForm) {
            SurchargeFormDialog(
                editing      = uiState.editingSurcharge,
                isSubmitting = uiState.isSubmitting,
                onDismiss    = viewModel::closeSurchargeForm,
                onConfirm    = viewModel::saveSurcharge
            )
        }
        if (uiState.showSurchargeDeleteConfirm && uiState.editingSurcharge != null) {
            DeleteConfirmDialog(
                name         = uiState.editingSurcharge!!.name,
                entity       = "surcharge",
                isSubmitting = uiState.isSubmitting,
                onConfirm    = viewModel::deleteSurcharge,
                onDismiss    = viewModel::closeSurchargeDeleteConfirm
            )
        }

        // ── Tax dialogs ──────────────────────────────────────────────────────
        if (uiState.showTaxForm) {
            TaxFormDialog(
                editing      = uiState.editingTax,
                isSubmitting = uiState.isSubmitting,
                onDismiss    = viewModel::closeTaxForm,
                onConfirm    = viewModel::saveTax
            )
        }
        if (uiState.showTaxDeleteConfirm && uiState.editingTax != null) {
            DeleteConfirmDialog(
                name         = uiState.editingTax!!.name,
                entity       = "pajak",
                isSubmitting = uiState.isSubmitting,
                onConfirm    = viewModel::deleteTax,
                onDismiss    = viewModel::closeTaxDeleteConfirm
            )
        }

        // ── Discount dialogs ─────────────────────────────────────────────────
        if (uiState.showDiscountForm) {
            DiscountFormDialog(
                editing      = uiState.editingDiscount,
                isSubmitting = uiState.isSubmitting,
                onDismiss    = viewModel::closeDiscountForm,
                onConfirm    = viewModel::saveDiscount
            )
        }
        if (uiState.showDiscountDeleteConfirm && uiState.editingDiscount != null) {
            DeleteConfirmDialog(
                name         = uiState.editingDiscount!!.name,
                entity       = "aturan diskon",
                isSubmitting = uiState.isSubmitting,
                onConfirm    = viewModel::deleteDiscount,
                onDismiss    = viewModel::closeDiscountDeleteConfirm
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Tabs
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun SurchargeTab(surcharges: List<Surcharge>, viewModel: PricingManagementViewModel) {
    if (surcharges.isEmpty()) {
        EmptyState("Belum ada surcharge")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(surcharges, key = { it.uuid }) { item ->
            val valueLabel = if (item.isPercentage) "${item.amount}%" else formatRupiah(item.amount)
            PricingCard(
                title     = item.name,
                subtitle  = "$valueLabel · ${item.orderType ?: "Semua"}",
                isActive  = item.isActive,
                onEdit    = { viewModel.openSurchargeForm(item) },
                onDelete  = { viewModel.openSurchargeDeleteConfirm(item) }
            )
        }
    }
}

@Composable
private fun TaxTab(taxConfigs: List<TaxConfig>, viewModel: PricingManagementViewModel) {
    if (taxConfigs.isEmpty()) {
        EmptyState("Belum ada konfigurasi pajak")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(taxConfigs, key = { it.uuid }) { item ->
            PricingCard(
                title     = item.name,
                subtitle  = "${item.rate}% · ${item.applyTo}",
                isActive  = item.isActive,
                onEdit    = { viewModel.openTaxForm(item) },
                onDelete  = { viewModel.openTaxDeleteConfirm(item) }
            )
        }
    }
}

@Composable
private fun DiscountTab(rules: List<DiscountRule>, viewModel: PricingManagementViewModel) {
    if (rules.isEmpty()) {
        EmptyState("Belum ada aturan diskon")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(rules, key = { it.uuid }) { item ->
            val valueLabel = if (item.discountType == "pct") "${item.discountValue}%" else formatRupiah(item.discountValue.toLong())
            PricingCard(
                title     = item.name,
                subtitle  = "$valueLabel · ${item.ruleType}",
                isActive  = item.isActive,
                onEdit    = { viewModel.openDiscountForm(item) },
                onDelete  = { viewModel.openDiscountDeleteConfirm(item) }
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Shared UI
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PricingCard(title: String, subtitle: String, isActive: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Surface(shape = MaterialTheme.shapes.small,
                        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
                        Text(if (isActive) "Aktif" else "Nonaktif",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.DeleteOutline, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(name: String, entity: String, isSubmitting: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Hapus ${entity.replaceFirstChar { it.uppercase() }}") },
        text  = { Text("Hapus $entity \"$name\"? Tindakan ini tidak dapat dibatalkan.") },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSubmitting) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp))
                else Text("Hapus", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") } }
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Form Dialogs
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurchargeFormDialog(
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

    val orderTypes = listOf("all" to "Semua", "dine_in" to "Dine In", "takeaway" to "Takeaway", "delivery" to "Delivery")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editing == null) "Tambah Surcharge" else "Edit Surcharge") },
        text  = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Nama *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                // Order type dropdown
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = orderTypes.firstOrNull { it.first == orderType }?.second ?: orderType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipe Pesanan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        orderTypes.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { orderType = value; expanded = false })
                        }
                    }
                }

                OutlinedTextField(value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Jumlah *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Berbasis Persentase", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isPercentage, onCheckedChange = { isPercentage = it })
                }
                if (isPercentage) {
                    OutlinedTextField(value = maxAmount,
                        onValueChange = { maxAmount = it.filter { c -> c.isDigit() } },
                        label = { Text("Maks. Nominal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onConfirm(orderType, name.trim(), amount, isPercentage, maxAmount.ifBlank { null }) },
                enabled  = !isSubmitting && name.isNotBlank() && amount.isNotBlank()
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaxFormDialog(
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

    val applyToOptions = listOf("after_discount" to "Setelah Diskon", "before_discount" to "Sebelum Diskon")

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editing == null) "Tambah Pajak" else "Edit Pajak") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Nama *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = rate,
                    onValueChange = { rate = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Tarif (%) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = applyToOptions.firstOrNull { it.first == applyTo }?.second ?: applyTo,
                        onValueChange = {}, readOnly = true,
                        label = { Text("Diterapkan Pada") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        applyToOptions.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { applyTo = value; expanded = false })
                        }
                    }
                }
                OutlinedTextField(value = sortOrder,
                    onValueChange = { sortOrder = it.filter { c -> c.isDigit() } },
                    label = { Text("Urutan Tampil") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick  = { onConfirm(name.trim(), rate, applyTo, sortOrder.toIntOrNull() ?: 0) },
                enabled  = !isSubmitting && name.isNotBlank() && rate.isNotBlank()
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") } }
    )
}

@Composable
private fun DiscountFormDialog(
    editing: DiscountRule?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, discountValue: Double, discountType: String,
                ruleType: String, isActive: Boolean, description: String?,
                maxDiscount: Double?, minPurchaseAmount: Double?) -> Unit
) {
    var name          by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var discountValue by remember(editing) { mutableStateOf(editing?.discountValue?.toString() ?: "") }
    var discountType  by remember(editing) { mutableStateOf(editing?.discountType ?: "pct") }
    var ruleType      by remember(editing) { mutableStateOf(editing?.ruleType ?: "always") }
    var isActive      by remember(editing) { mutableStateOf(editing?.isActive ?: true) }
    var description   by remember(editing) { mutableStateOf(editing?.description ?: "") }
    var maxDiscount   by remember(editing) { mutableStateOf(editing?.maxDiscount?.toString() ?: "") }
    var minPurchase   by remember(editing) { mutableStateOf(editing?.minPurchaseAmount?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editing == null) "Tambah Aturan Diskon" else "Edit Aturan Diskon") },
        text  = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Nama *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)

                Text("Tipe Diskon", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("pct" to "Persen (%)", "flat" to "Nominal (Rp)").forEach { (value, label) ->
                        FilterChip(selected = discountType == value, onClick = { discountType = value }, label = { Text(label) })
                    }
                }

                OutlinedTextField(value = discountValue,
                    onValueChange = { discountValue = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Nilai Diskon *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)

                Text("Jenis Aturan", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("always" to "Selalu", "time_based" to "Berbasis Waktu").forEach { (value, label) ->
                        FilterChip(selected = ruleType == value, onClick = { ruleType = value }, label = { Text(label) })
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = minPurchase,
                        onValueChange = { minPurchase = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Min. Pembelian") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = maxDiscount,
                        onValueChange = { maxDiscount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Maks. Diskon") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), singleLine = true)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Aktif", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = {
                    onConfirm(name.trim(), discountValue.toDoubleOrNull() ?: 0.0, discountType, ruleType, isActive,
                        description.ifBlank { null },
                        maxDiscount.toDoubleOrNull(), minPurchase.toDoubleOrNull())
                },
                enabled  = !isSubmitting && name.isNotBlank() && discountValue.isNotBlank()
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") } }
    )
}
