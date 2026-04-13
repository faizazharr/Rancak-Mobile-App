package id.rancak.app.presentation.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CartViewModel
import id.rancak.app.presentation.viewmodel.PaymentViewModel
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBack: () -> Unit,
    onPaymentComplete: () -> Unit,
    cartViewModel: CartViewModel = koinViewModel(),
    paymentViewModel: PaymentViewModel = koinViewModel()
) {
    val cartState by cartViewModel.uiState.collectAsState()
    val paymentState by paymentViewModel.uiState.collectAsState()

    LaunchedEffect(paymentState.completedSale) {
        if (paymentState.completedSale != null) {
            // brief delay then navigate
            onPaymentComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pembayaran") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        if (paymentState.completedSale != null) {
            // Success State
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Transaksi Berhasil!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    paymentState.completedSale?.invoiceNo ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    formatRupiah(paymentState.completedSale?.total ?: 0),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if ((paymentState.completedSale?.changeAmount ?: 0) > 0) {
                    Text(
                        "Kembalian: ${formatRupiah(paymentState.completedSale?.changeAmount ?: 0)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Order Summary Card
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Ringkasan Pesanan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        SummaryRow(
                            label = "${cartState.itemCount} item",
                            value = formatRupiah(cartState.subtotal)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SummaryRow(
                            label = "Total",
                            value = formatRupiah(cartState.subtotal),
                            isBold = true,
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Payment Method
                Text(
                    "Metode Pembayaran",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        PaymentMethodChip(
                            method = method.value,
                            isSelected = paymentState.selectedMethod == method,
                            onClick = { paymentViewModel.selectMethod(method) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Paid Amount
                if (paymentState.selectedMethod == PaymentMethod.CASH) {
                    Text(
                        "Jumlah Bayar",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = paymentState.paidAmount,
                        onValueChange = paymentViewModel::setPaidAmount,
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )

                    // Quick amount buttons
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val quickAmounts = listOf(
                            cartState.subtotal,
                            ((cartState.subtotal / 10000) + 1) * 10000,
                            ((cartState.subtotal / 50000) + 1) * 50000,
                            ((cartState.subtotal / 100000) + 1) * 100000
                        ).distinct().sorted()

                        quickAmounts.take(4).forEach { amount ->
                            OutlinedButton(
                                onClick = { paymentViewModel.setPaidAmount(amount.toString()) },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.small,
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text(
                                    formatRupiah(amount),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Error
                if (paymentState.error != null) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = paymentState.error!!,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                RancakButton(
                    text = "Proses Pembayaran",
                    onClick = {
                        paymentViewModel.processPayment(
                            items = cartState.items,
                            orderType = cartState.orderType,
                            tableUuid = cartState.tableUuid,
                            customerName = cartState.customerName,
                            note = cartState.note
                        )
                    },
                    isLoading = paymentState.isProcessing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
private fun PaymentFormPreview() {
    RancakTheme {
        Column(Modifier.padding(16.dp)) {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Ringkasan Pesanan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    SummaryRow(label = "3 item", value = "Rp 75.000")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    SummaryRow(label = "Total", value = "Rp 75.000", isBold = true, valueColor = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Metode Pembayaran", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethodChip(method = "Cash", isSelected = true, onClick = {}, modifier = Modifier.weight(1f))
                PaymentMethodChip(method = "QRIS", isSelected = false, onClick = {}, modifier = Modifier.weight(1f))
                PaymentMethodChip(method = "Card", isSelected = false, onClick = {}, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
            RancakButton(text = "Proses Pembayaran", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview
@Composable
private fun PaymentSuccessPreview() {
    RancakTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.CheckCircle, null, Modifier.size(80.dp), MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            Text("Transaksi Berhasil!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("INV-2024-0001", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("Rp 75.000", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Kembalian: Rp 25.000", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
