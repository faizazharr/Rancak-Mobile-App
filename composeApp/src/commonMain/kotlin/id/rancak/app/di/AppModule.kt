package id.rancak.app.di

import com.russhwolf.settings.Settings
import id.rancak.app.data.local.OfflineSaleQueue
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.local.db.AppDatabase
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

// DAOs exposed from the platform-provided AppDatabase singleton
val databaseModule = module {
    single { get<AppDatabase>().productDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().cartDao() }
    single { get<AppDatabase>().saleDao() }
    single { get<AppDatabase>().shiftDao() }
    single { get<AppDatabase>().tableDao() }
}

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
    single<ProductRepository> { ProductRepositoryImpl(get(), get(), get(), get()) }
    single<SaleRepository> { SaleRepositoryImpl(get(), get(), get(), get(), get()) }
    single<OperationsRepository> { OperationsRepositoryImpl(get(), get(), get(), get()) }
    singleOf(::FinanceRepositoryImpl) bind FinanceRepository::class
}

val viewModelModule = module {
    single { SettingsStore() }
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
    viewModelOf(::SettingsViewModel)
}

val appModules = if (DEMO_MODE) {
    // Demo: skip dataModule (tidak perlu Ktor / API service)
    // databaseModule included so CartViewModel always gets CartDao
    listOf(databaseModule, demoRepositoryModule, viewModelModule, platformModule)
} else {
    listOf(databaseModule, dataModule, repositoryModule, viewModelModule, platformModule)
}
