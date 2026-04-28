package id.rancak.app.presentation.ui.pricing

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.pricing.components.*
import id.rancak.app.presentation.viewmodel.PricingManagementViewModel
import id.rancak.app.presentation.viewmodel.PricingManagementUiState
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ──────────────────────────────────────────────────────────────────────────────
// Screen (stateful)
// ──────────────────────────────────────────────────────────────────────────────

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

    PricingManagementContent(
        uiState    = uiState,
        onBack     = onBack,
        onAddSurcharge      = { viewModel.openSurchargeForm() },
        onAddTax            = { viewModel.openTaxForm() },
        onAddDiscount       = { viewModel.openDiscountForm() },
        onEditSurcharge     = viewModel::openSurchargeForm,
        onDeleteSurcharge   = viewModel::openSurchargeDeleteConfirm,
        onEditTax           = viewModel::openTaxForm,
        onDeleteTax         = viewModel::openTaxDeleteConfirm,
        onEditDiscount      = viewModel::openDiscountForm,
        onDeleteDiscount    = viewModel::openDiscountDeleteConfirm,
        onSaveSurcharge     = viewModel::saveSurcharge,
        onCloseSurchargeForm    = viewModel::closeSurchargeForm,
        onConfirmDeleteSurcharge = viewModel::deleteSurcharge,
        onCloseSurchargeDelete  = viewModel::closeSurchargeDeleteConfirm,
        onSaveTax               = viewModel::saveTax,
        onCloseTaxForm          = viewModel::closeTaxForm,
        onConfirmDeleteTax      = viewModel::deleteTax,
        onCloseTaxDelete        = viewModel::closeTaxDeleteConfirm,
        onSaveDiscount          = viewModel::saveDiscount,
        onCloseDiscountForm     = viewModel::closeDiscountForm,
        onConfirmDeleteDiscount = viewModel::deleteDiscount,
        onCloseDiscountDelete   = viewModel::closeDiscountDeleteConfirm,
        snackbarHostState   = snackbarHostState
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Content (pure UI — previewable)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun PricingManagementContent(
    uiState: PricingManagementUiState,
    onBack: () -> Unit,
    onAddSurcharge: () -> Unit,
    onAddTax: () -> Unit,
    onAddDiscount: () -> Unit,
    onEditSurcharge: (id.rancak.app.domain.model.Surcharge) -> Unit,
    onDeleteSurcharge: (id.rancak.app.domain.model.Surcharge) -> Unit,
    onEditTax: (id.rancak.app.domain.model.TaxConfig) -> Unit,
    onDeleteTax: (id.rancak.app.domain.model.TaxConfig) -> Unit,
    onEditDiscount: (id.rancak.app.domain.model.DiscountRule) -> Unit,
    onDeleteDiscount: (id.rancak.app.domain.model.DiscountRule) -> Unit,
    onSaveSurcharge: (orderType: String, name: String, amount: String, isPercentage: Boolean, maxAmount: String?) -> Unit,
    onCloseSurchargeForm: () -> Unit,
    onConfirmDeleteSurcharge: () -> Unit,
    onCloseSurchargeDelete: () -> Unit,
    onSaveTax: (name: String, rate: String, applyTo: String, sortOrder: Int) -> Unit,
    onCloseTaxForm: () -> Unit,
    onConfirmDeleteTax: () -> Unit,
    onCloseTaxDelete: () -> Unit,
    onSaveDiscount: (name: String, discountValue: Double, discountType: String, ruleType: String, isActive: Boolean, description: String?, maxDiscount: Double?, minPurchaseAmount: Double?) -> Unit,
    onCloseDiscountForm: () -> Unit,
    onConfirmDeleteDiscount: () -> Unit,
    onCloseDiscountDelete: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Surcharge", "Pajak", "Diskon")

    Scaffold(
        topBar = {
            RancakTopBar(title = "Harga & Diskon", icon = Icons.Default.LocalOffer, onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                when (selectedTab) {
                    0 -> onAddSurcharge()
                    1 -> onAddTax()
                    else -> onAddDiscount()
                }
            }) { Icon(Icons.Default.Add, contentDescription = "Tambah") }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            val isTablet = maxWidth >= 600.dp
        if (isTablet) {
            // Tablet: show all 3 sections side by side
            if (uiState.isLoading) {
                LoadingScreen()
            } else {
                Row(Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Surcharge", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            IconButton(onClick = onAddSurcharge, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null, Modifier.size(18.dp)) }
                        }
                        HorizontalDivider()
                        SurchargeTab(uiState.surcharges, onEdit = onEditSurcharge, onDelete = onDeleteSurcharge)
                    }
                    VerticalDivider(modifier = Modifier.fillMaxHeight())
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Pajak", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            IconButton(onClick = onAddTax, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null, Modifier.size(18.dp)) }
                        }
                        HorizontalDivider()
                        TaxTab(uiState.taxConfigs, onEdit = onEditTax, onDelete = onDeleteTax)
                    }
                    VerticalDivider(modifier = Modifier.fillMaxHeight())
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Diskon", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            IconButton(onClick = onAddDiscount, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null, Modifier.size(18.dp)) }
                        }
                        HorizontalDivider()
                        DiscountTab(uiState.discountRules, onEdit = onEditDiscount, onDelete = onDeleteDiscount)
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title) })
                }
            }

            if (uiState.isLoading) {
                LoadingScreen()
            } else {
                when (selectedTab) {
                    0 -> SurchargeTab(uiState.surcharges, onEdit = onEditSurcharge, onDelete = onDeleteSurcharge)
                    1 -> TaxTab(uiState.taxConfigs, onEdit = onEditTax, onDelete = onDeleteTax)
                    else -> DiscountTab(uiState.discountRules, onEdit = onEditDiscount, onDelete = onDeleteDiscount)
                }
            }
        }
        } // end else
        } // end BoxWithConstraints

        // ── Surcharge dialogs ────────────────────────────────────────────────
        if (uiState.showSurchargeForm) {
            SurchargeFormDialog(
                editing      = uiState.editingSurcharge,
                isSubmitting = uiState.isSubmitting,
                onDismiss    = onCloseSurchargeForm,
                onConfirm    = onSaveSurcharge
            )
        }
        if (uiState.showSurchargeDeleteConfirm && uiState.editingSurcharge != null) {
            PricingDeleteDialog(
                name         = uiState.editingSurcharge!!.name,
                entity       = "surcharge",
                isSubmitting = uiState.isSubmitting,
                onConfirm    = onConfirmDeleteSurcharge,
                onDismiss    = onCloseSurchargeDelete
            )
        }

        // ── Tax dialogs ──────────────────────────────────────────────────────
        if (uiState.showTaxForm) {
            TaxFormDialog(
                editing      = uiState.editingTax,
                isSubmitting = uiState.isSubmitting,
                onDismiss    = onCloseTaxForm,
                onConfirm    = onSaveTax
            )
        }
        if (uiState.showTaxDeleteConfirm && uiState.editingTax != null) {
            PricingDeleteDialog(
                name         = uiState.editingTax!!.name,
                entity       = "pajak",
                isSubmitting = uiState.isSubmitting,
                onConfirm    = onConfirmDeleteTax,
                onDismiss    = onCloseTaxDelete
            )
        }

        // ── Discount dialogs ─────────────────────────────────────────────────
        if (uiState.showDiscountForm) {
            DiscountFormDialog(
                editing      = uiState.editingDiscount,
                isSubmitting = uiState.isSubmitting,
                onDismiss    = onCloseDiscountForm,
                onConfirm    = onSaveDiscount
            )
        }
        if (uiState.showDiscountDeleteConfirm && uiState.editingDiscount != null) {
            PricingDeleteDialog(
                name         = uiState.editingDiscount!!.name,
                entity       = "aturan diskon",
                isSubmitting = uiState.isSubmitting,
                onConfirm    = onConfirmDeleteDiscount,
                onDismiss    = onCloseDiscountDelete
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Preview
// ──────────────────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun PricingManagementContentPreview() {
    RancakTheme {
        PricingManagementContent(
            uiState              = PricingManagementUiState(),
            onBack               = {},
            onAddSurcharge       = {},
            onAddTax             = {},
            onAddDiscount        = {},
            onEditSurcharge      = {},
            onDeleteSurcharge    = {},
            onEditTax            = {},
            onDeleteTax          = {},
            onEditDiscount       = {},
            onDeleteDiscount     = {},
            onSaveSurcharge      = { _, _, _, _, _ -> },
            onCloseSurchargeForm = {},
            onConfirmDeleteSurcharge = {},
            onCloseSurchargeDelete   = {},
            onSaveTax            = { _, _, _, _ -> },
            onCloseTaxForm       = {},
            onConfirmDeleteTax   = {},
            onCloseTaxDelete     = {},
            onSaveDiscount       = { _, _, _, _, _, _, _, _ -> },
            onCloseDiscountForm  = {},
            onConfirmDeleteDiscount = {},
            onCloseDiscountDelete   = {}
        )
    }
}
