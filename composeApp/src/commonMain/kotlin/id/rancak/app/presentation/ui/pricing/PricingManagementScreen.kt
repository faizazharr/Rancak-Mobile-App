package id.rancak.app.presentation.ui.pricing

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakDesign
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.pricing.components.DiscountFormDialog
import id.rancak.app.presentation.ui.pricing.components.DiscountFormPanel
import id.rancak.app.presentation.ui.pricing.components.DiscountTab
import id.rancak.app.presentation.ui.pricing.components.PricingDeleteDialog
import id.rancak.app.presentation.ui.pricing.components.SurchargeFormDialog
import id.rancak.app.presentation.ui.pricing.components.SurchargeFormPanel
import id.rancak.app.presentation.ui.pricing.components.SurchargeTab
import id.rancak.app.presentation.ui.pricing.components.TaxFormDialog
import id.rancak.app.presentation.ui.pricing.components.TaxFormPanel
import id.rancak.app.presentation.ui.pricing.components.TaxTab
import id.rancak.app.presentation.viewmodel.PricingManagementViewModel
import id.rancak.app.presentation.viewmodel.PricingManagementUiState
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.viewmodel.koinViewModel

private val PricingGradientEnd = Color(0xFF0B7A60)

// ──────────────────────────────────────────────────────────────────────────────
// Screen (stateful)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun PricingManagementScreen(
    onBack: () -> Unit,
    onBundleManagement: () -> Unit = {}
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
        onBundleManagement      = onBundleManagement,
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
    onBundleManagement: () -> Unit = {},
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
    val closeAllForms: () -> Unit = {
        onCloseSurchargeForm()
        onCloseTaxForm()
        onCloseDiscountForm()
    }
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

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

    Scaffold(
        topBar = {
            RancakTopBar(title = "Harga & Diskon", icon = Icons.Default.LocalOffer, onMenu = onBack)
        },
        floatingActionButton = {
            // FAB hanya di phone — tablet pakai inline button di SectionDetailHeader
            if (!isTablet) {
                ExtendedFloatingActionButton(
                    onClick = onAddCurrent,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Tambah ${activeSection.label}") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            val isTabletInner = maxWidth >= 600.dp
            if (isTabletInner) {
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
                                onClick    = { if (selectedTab != i) closeAllForms(); selectedTab = i },
                                modifier   = Modifier.fillMaxWidth()
                            )
                        }
                        // Bundle nav card
                        BundleNavCard(onClick = onBundleManagement, modifier = Modifier.fillMaxWidth())
                    }

                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Detail: inline form panel OR header + content list
                    val showingForm = uiState.showSurchargeForm || uiState.showTaxForm || uiState.showDiscountForm
                    if (showingForm) {
                        when {
                            uiState.showSurchargeForm -> SurchargeFormPanel(
                                editing      = uiState.editingSurcharge,
                                isSubmitting = uiState.isSubmitting,
                                onDismiss    = onCloseSurchargeForm,
                                onConfirm    = onSaveSurcharge,
                                modifier     = Modifier.weight(1f).fillMaxHeight()
                            )
                            uiState.showTaxForm -> TaxFormPanel(
                                editing      = uiState.editingTax,
                                isSubmitting = uiState.isSubmitting,
                                onDismiss    = onCloseTaxForm,
                                onConfirm    = onSaveTax,
                                modifier     = Modifier.weight(1f).fillMaxHeight()
                            )
                            else -> DiscountFormPanel(
                                editing      = uiState.editingDiscount,
                                isSubmitting = uiState.isSubmitting,
                                onDismiss    = onCloseDiscountForm,
                                onConfirm    = onSaveDiscount,
                                modifier     = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            SectionDetailHeader(activeSection, onAdd = onAddCurrent)
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
                                onClick    = { if (selectedTab != i) closeAllForms(); selectedTab = i },
                                modifier   = Modifier.weight(1f)
                            )
                        }
                        BundleNavCard(onClick = onBundleManagement, modifier = Modifier.weight(1f))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    SectionDetailHeader(activeSection, onAdd = null)

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

        // ── Surcharge dialogs (phone only — tablet uses inline panel) ────────
        if (uiState.showSurchargeForm && !isTablet) {
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

        // ── Tax dialogs (phone only) ─────────────────────────────────────────
        if (uiState.showTaxForm && !isTablet) {
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

        // ── Discount dialogs (phone only) ────────────────────────────────────
        if (uiState.showDiscountForm && !isTablet) {
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
    } // end Scaffold
    } // end BoxWithConstraints
}

@Composable
private fun SectionDetailHeader(activeSection: PricingSection, onAdd: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored icon box
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Primary.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                activeSection.icon,
                contentDescription = null,
                tint     = Primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            activeSection.label,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(8.dp))
        // Active count pill
        val hasActive = activeSection.activeCount > 0
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = if (hasActive) Primary.copy(0.12f) else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                "${activeSection.activeCount}/${activeSection.totalCount} aktif",
                style    = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color    = if (hasActive) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        // Inline add button — tablet only
        if (onAdd != null) {
            FilledTonalButton(
                onClick        = onAdd,
                shape          = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                colors         = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Primary.copy(0.12f),
                    contentColor   = Primary
                )
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("Tambah", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
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
    val bg by animateColorAsState(
        if (isSelected) Primary.copy(0.10f) else MaterialTheme.colorScheme.surface,
        tween(200), label = "CardBg"
    )
    val borderColor by animateColorAsState(
        if (isSelected) Primary else MaterialTheme.colorScheme.outlineVariant,
        tween(200), label = "CardBorder"
    )
    val iconTint by animateColorAsState(
        if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
        tween(200), label = "CardIcon"
    )
    val labelColor by animateColorAsState(
        if (isSelected) Primary else MaterialTheme.colorScheme.onSurface,
        tween(200), label = "CardLabel"
    )
    val accentBar by animateColorAsState(
        if (isSelected) Primary else Color.Transparent,
        tween(220), label = "CardAccent"
    )

    Card(
        onClick   = onClick,
        modifier  = modifier,
        colors    = CardDefaults.cardColors(containerColor = bg),
        border    = BorderStroke(if (isSelected) RancakDesign.sizes.borderEmphasis else RancakDesign.sizes.borderThin, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) RancakDesign.elevation.cardSelected else RancakDesign.elevation.none
        ),
        shape     = MaterialTheme.shapes.medium
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left accent bar
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentBar)
            )
            Column(
                modifier            = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        section.icon,
                        contentDescription = null,
                        tint     = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        section.label,
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = labelColor,
                        maxLines   = 1
                    )
                }
                Text(
                    "${section.totalCount}",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = labelColor
                )
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = if (isSelected) Primary.copy(0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        "${section.activeCount} aktif",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
// ──────────────────────────────────────────────────────────────────────────────
// Bundle nav card — navigates to BundleManagementScreen
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun BundleNavCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick   = onClick,
        modifier  = modifier,
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(RancakDesign.sizes.borderThin, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = RancakDesign.elevation.none),
        shape     = MaterialTheme.shapes.medium
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(Modifier.width(4.dp).fillMaxHeight().background(Color.Transparent))
            Column(
                modifier            = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Bundle",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Kelola",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
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
