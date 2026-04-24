package id.rancak.app.di

import com.russhwolf.settings.Settings
import id.rancak.app.data.local.OfflineSaleQueue
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.local.createSecureSettings
import id.rancak.app.data.local.db.AppDatabase
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.createHttpClient
import id.rancak.app.data.repository.*
import id.rancak.app.domain.repository.*
import id.rancak.app.data.sync.SyncScheduler
import id.rancak.app.presentation.viewmodel.*
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

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
    // Secure settings (encrypted storage) — dipakai TokenManager untuk
    // menyimpan token auth + user info. Android: EncryptedSharedPreferences,
    // iOS: Keychain. Dua namespace terpisah supaya auth data & offline queue
    // tidak bercampur di storage yang sama.
    single<Settings>(qualifier = org.koin.core.qualifier.named("secure-auth")) {
        createSecureSettings(namespace = "auth")
    }
    single<Settings>(qualifier = org.koin.core.qualifier.named("secure-queue")) {
        createSecureSettings(namespace = "offline_queue")
    }
    single { TokenManager(get(qualifier = org.koin.core.qualifier.named("secure-auth"))) }
    single { createHttpClient(get()) }
    single { RancakApiService(get()) }
    // Offline queue — disimpan di encrypted storage (berisi data transaksi
    // yang belum ter-sync; sensitif karena memuat item, harga, customer).
    single { OfflineSaleQueue(get(qualifier = org.koin.core.qualifier.named("secure-queue"))) }
    // Expose SyncManager as SyncScheduler so SaleRepositoryImpl stays platform-agnostic
    single<SyncScheduler> { get<id.rancak.app.data.sync.SyncManager>() }
}

// Repository bindings — semua memakai API backend sungguhan
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
    viewModelOf(::SplashViewModel)
    viewModelOf(::TenantPickerViewModel)
    viewModelOf(::PosViewModel)
    viewModelOf(::CartViewModel)
    viewModelOf(::PaymentViewModel)
    viewModelOf(::ShiftViewModel)
    viewModelOf(::TableViewModel)
    viewModelOf(::KdsViewModel)
    viewModelOf(::SalesHistoryViewModel)
    viewModelOf(::SplitBillViewModel)
    viewModelOf(::AddItemsToHeldOrderViewModel)
    viewModelOf(::OrderBoardViewModel)
    viewModelOf(::ReportViewModel)
    viewModelOf(::CashExpenseViewModel)
    viewModelOf(::SettingsViewModel)
}

val appModules = listOf(
    databaseModule,
    dataModule,
    repositoryModule,
    viewModelModule,
    platformModule,
)
