package id.rancak.app.presentation.ui.pricing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakDesign
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.pricing.components.DiscountFormDialog
import id.rancak.app.presentation.ui.pricing.components.DiscountTab
import id.rancak.app.presentation.ui.pricing.components.PricingDeleteDialog
import id.rancak.app.presentation.ui.pricing.components.SurchargeFormDialog
import id.rancak.app.presentation.ui.pricing.components.SurchargeTab
import id.rancak.app.presentation.ui.pricing.components.TaxFormDialog
import id.rancak.app.presentation.ui.pricing.components.TaxTab
import id.rancak.app.presentation.viewmodel.PricingManagementViewModel
import id.rancak.app.presentation.viewmodel.PricingManagementUiState
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.viewmodel.koinViewModel

// ──────────────────────────────────────────────────────────────────────────────
// Screen (stateful)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun PricingManagementScreen(
    onBack: () -> Unit
) {
    val viewModel: PricingManagementViewModel = koinViewModel()
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
        onToggleSurchargeActive = viewModel::toggleSurchargeActive,
        onToggleTaxActive       = viewModel::toggleTaxActive,
        onToggleDiscountActive  = viewModel::toggleDiscountActive,
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
    onToggleSurchargeActive: (id.rancak.app.domain.model.Surcharge, Boolean) -> Unit = { _, _ -> },
    onToggleTaxActive: (id.rancak.app.domain.model.TaxConfig, Boolean) -> Unit = { _, _ -> },
    onToggleDiscountActive: (id.rancak.app.domain.model.DiscountRule, Boolean) -> Unit = { _, _ -> },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val sections = listOf(
        PricingSection("Surcharge", Icons.Default.AddCircle,    uiState.surcharges.size,    uiState.surcharges.count { it.isActive }),
        PricingSection("Pajak",     Icons.Default.Percent,      uiState.taxConfigs.size,    uiState.taxConfigs.count { it.isActive }),
        PricingSection("Diskon",    Icons.Default.LocalOffer,   uiState.discountRules.size, uiState.discountRules.count { it.isActive })
    )
    val activeSection = sections[selectedTab]

    val onAddCurrent: () -> Unit = {
        when (selectedTab) {
            0 -> onAddSurcharge()
            1 -> onAddTax()
            else -> onAddDiscount()
        }
    }

    Scaffold(
        topBar = {
            RancakTopBar(title = "Harga & Diskon", icon = Icons.Default.LocalOffer, onMenu = onBack)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddCurrent,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Tambah ${activeSection.label}") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            val isTablet = maxWidth >= 600.dp

            if (isTablet) {
                // ── Tablet: master-detail (sidebar kiri + list kanan) ─────────
                Row(Modifier.fillMaxSize()) {
                    // Sidebar: summary cards stacked vertical
                    Column(
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sections.forEachIndexed { i, section ->
                            SectionSummaryCard(
                                section    = section,
                                isSelected = selectedTab == i,
                                onClick    = { selectedTab = i },
                                modifier   = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Detail: header + content list
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        SectionDetailHeader(activeSection)
                        Box(Modifier.fillMaxSize()) {
                            if (uiState.isLoading) {
                                LoadingScreen()
                            } else {
                                when (selectedTab) {
                                    0 -> SurchargeTab(uiState.surcharges.toImmutableList(), onEdit = onEditSurcharge, onDelete = onDeleteSurcharge, onToggleActive = onToggleSurchargeActive)
                                    1 -> TaxTab(uiState.taxConfigs.toImmutableList(), onEdit = onEditTax, onDelete = onDeleteTax, onToggleActive = onToggleTaxActive)
                                    else -> DiscountTab(uiState.discountRules.toImmutableList(), onEdit = onEditDiscount, onDelete = onDeleteDiscount, onToggleActive = onToggleDiscountActive)
                                }
                            }
                        }
                    }
                }
            } else {
                // ── Phone: sequential (summary row di atas, list di bawah) ────
                Column(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sections.forEachIndexed { i, section ->
                            SectionSummaryCard(
                                section    = section,
                                isSelected = selectedTab == i,
                                onClick    = { selectedTab = i },
                                modifier   = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    SectionDetailHeader(activeSection)

                    Box(Modifier.fillMaxSize()) {
                        if (uiState.isLoading) {
                            LoadingScreen()
                        } else {
                            when (selectedTab) {
                                0 -> SurchargeTab(uiState.surcharges.toImmutableList(), onEdit = onEditSurcharge, onDelete = onDeleteSurcharge, onToggleActive = onToggleSurchargeActive)
                                1 -> TaxTab(uiState.taxConfigs.toImmutableList(), onEdit = onEditTax, onDelete = onDeleteTax, onToggleActive = onToggleTaxActive)
                                else -> DiscountTab(uiState.discountRules.toImmutableList(), onEdit = onEditDiscount, onDelete = onDeleteDiscount, onToggleActive = onToggleDiscountActive)
                            }
                        }
                    }
                }
            }
        }

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
                name         = uiState.editingSurcharge.name,
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
                name         = uiState.editingTax.name,
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
                name         = uiState.editingDiscount.name,
                entity       = "aturan diskon",
                isSubmitting = uiState.isSubmitting,
                onConfirm    = onConfirmDeleteDiscount,
                onDismiss    = onCloseDiscountDelete
            )
        }
    }
}

@Composable
private fun SectionDetailHeader(activeSection: PricingSection) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            activeSection.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            activeSection.label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.weight(1f))
        AssistChip(
            onClick = {},
            enabled = false,
            label = { Text("${activeSection.activeCount}/${activeSection.totalCount} aktif") }
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

// ──────────────────────────────────────────────────────────────────────────────
// Section model + summary card
// ──────────────────────────────────────────────────────────────────────────────

private data class PricingSection(
    val label: String,
    val icon: ImageVector,
    val totalCount: Int,
    val activeCount: Int
)

@Composable
private fun SectionSummaryCard(
    section: PricingSection,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
    val onContainer = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                      else MaterialTheme.colorScheme.onSurface
    val border = if (isSelected) BorderStroke(RancakDesign.sizes.borderEmphasis, MaterialTheme.colorScheme.primary)
                 else BorderStroke(RancakDesign.sizes.borderThin, MaterialTheme.colorScheme.outlineVariant)

    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer),
        border = border,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) RancakDesign.elevation.cardSelected else RancakDesign.elevation.none
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    section.icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    section.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Text(
                "${section.totalCount}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (section.totalCount == 0) "Belum ada"
                else "${section.activeCount} aktif",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) onContainer.copy(alpha = 0.75f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
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
