package id.rancak.app.di

import com.russhwolf.settings.Settings
import id.rancak.app.data.local.OfflineSaleQueue
import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.createHttpClient
import id.rancak.app.data.repository.*
import id.rancak.app.data.repository.fake.*
import id.rancak.app.domain.repository.*
import id.rancak.app.presentation.viewmodel.*
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

// ─────────────────────────────────────────────────────────────────────────────
// Set DEMO_MODE = true  → pakai data dummy (tidak butuh server)
// Set DEMO_MODE = false → pakai API backend sungguhan
// ─────────────────────────────────────────────────────────────────────────────
const val DEMO_MODE = true

val dataModule = module {
    single { TokenManager() }
    single { createHttpClient(get()) }
    single { RancakApiService(get()) }
    // Offline queue — persisted via multiplatform-settings
    single { OfflineSaleQueue(Settings()) }
}

// Repository dengan data dummy (tidak butuh koneksi)
val demoRepositoryModule = module {
    single<AuthRepository>       { FakeAuthRepository() }
    single<ProductRepository>    { FakeProductRepository() }
    single<SaleRepository>       { FakeSaleRepository() }
    single<OperationsRepository> { FakeOperationsRepository() }
    single<FinanceRepository>    { FakeFinanceRepository() }
}

// Repository dengan API sungguhan
val repositoryModule = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::ProductRepositoryImpl) bind ProductRepository::class
    // SaleRepositoryImpl now takes OfflineSaleQueue + SyncManager
    singleOf(::SaleRepositoryImpl) bind SaleRepository::class
    singleOf(::OperationsRepositoryImpl) bind OperationsRepository::class
    singleOf(::FinanceRepositoryImpl) bind FinanceRepository::class
}

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::TenantPickerViewModel)
    viewModelOf(::PosViewModel)
    viewModelOf(::CartViewModel)
    viewModelOf(::PaymentViewModel)
    viewModelOf(::ShiftViewModel)
    viewModelOf(::TableViewModel)
    viewModelOf(::KdsViewModel)
    viewModelOf(::SalesHistoryViewModel)
    viewModelOf(::OrderBoardViewModel)
    viewModelOf(::ReportViewModel)
    viewModelOf(::CashExpenseViewModel)
}

val appModules = if (DEMO_MODE) {
    // Demo: skip dataModule (tidak perlu Ktor / API service)
    listOf(demoRepositoryModule, viewModelModule, platformModule)
} else {
    listOf(dataModule, repositoryModule, viewModelModule, platformModule)
}
