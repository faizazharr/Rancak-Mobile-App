package id.rancak.app.di

import com.russhwolf.settings.Settings
import id.rancak.app.data.local.OfflineSaleQueue
import id.rancak.app.data.local.OpenBillStore
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.local.createSecureSettings
import id.rancak.app.data.local.db.AppDatabase
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.createHttpClient
import id.rancak.app.data.repository.AdminRepositoryImpl
import id.rancak.app.data.repository.AuthRepositoryImpl
import id.rancak.app.data.repository.BillingRepositoryImpl
import id.rancak.app.data.repository.CartRepositoryImpl
import id.rancak.app.data.repository.DeviceConfigRepositoryImpl
import id.rancak.app.data.repository.FinanceRepositoryImpl
import id.rancak.app.data.repository.GroupsRepositoryImpl
import id.rancak.app.data.repository.InventoryRepositoryImpl
import id.rancak.app.data.repository.OperationsRepositoryImpl
import id.rancak.app.data.repository.ProductRepositoryImpl
import id.rancak.app.data.repository.ReservationRepositoryImpl
import id.rancak.app.data.repository.SaleRepositoryImpl
import id.rancak.app.domain.repository.AdminRepository
import id.rancak.app.domain.repository.AuthRepository
import id.rancak.app.domain.repository.BillingRepository
import id.rancak.app.domain.repository.CartRepository
import id.rancak.app.domain.repository.DeviceConfigRepository
import id.rancak.app.domain.repository.FinanceRepository
import id.rancak.app.domain.repository.GroupsRepository
import id.rancak.app.domain.repository.InventoryRepository
import id.rancak.app.domain.repository.OperationsRepository
import id.rancak.app.domain.repository.ProductRepository
import id.rancak.app.domain.repository.ReservationRepository
import id.rancak.app.domain.repository.SaleRepository
import id.rancak.app.domain.repository.UserSessionProvider
import id.rancak.app.data.sync.SyncScheduler
import id.rancak.app.presentation.viewmodel.AddItemsToHeldOrderViewModel
import id.rancak.app.presentation.viewmodel.BillingViewModel
import id.rancak.app.presentation.viewmodel.CartViewModel
import id.rancak.app.presentation.viewmodel.CashExpenseViewModel
import id.rancak.app.presentation.viewmodel.HoldOrderViewModel
import id.rancak.app.presentation.viewmodel.KdsViewModel
import id.rancak.app.presentation.viewmodel.LoginViewModel
import id.rancak.app.presentation.viewmodel.OpenBillViewModel
import id.rancak.app.presentation.viewmodel.OrderBoardViewModel
import id.rancak.app.presentation.viewmodel.PaymentViewModel
import id.rancak.app.presentation.viewmodel.PosViewModel
import id.rancak.app.presentation.viewmodel.PricingManagementViewModel
import id.rancak.app.presentation.viewmodel.ProductManagementViewModel
import id.rancak.app.presentation.viewmodel.ReportViewModel
import id.rancak.app.presentation.viewmodel.ReservationViewModel
import id.rancak.app.presentation.viewmodel.RefundViewModel
import id.rancak.app.presentation.viewmodel.SalesHistoryViewModel
import id.rancak.app.presentation.viewmodel.SettingsViewModel
import id.rancak.app.presentation.viewmodel.ShiftViewModel
import id.rancak.app.presentation.viewmodel.SplashViewModel
import id.rancak.app.presentation.viewmodel.SplitBillViewModel
import id.rancak.app.presentation.viewmodel.StockOpnameViewModel
import id.rancak.app.presentation.viewmodel.TableViewModel
import id.rancak.app.presentation.viewmodel.TenantPickerViewModel
import id.rancak.app.presentation.viewmodel.VoucherManagementViewModel
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
    single<AuthRepository> { get<AuthRepositoryImpl>() }
    single<UserSessionProvider> { get<AuthRepositoryImpl>() }
    singleOf(::AuthRepositoryImpl)
    single<ProductRepository> { ProductRepositoryImpl(get(), get(), get(), get()) }
    single<SaleRepository> { SaleRepositoryImpl(get(), get(), get(), get(), get()) }
    single<OperationsRepository> { OperationsRepositoryImpl(get(), get(), get(), get()) }
    singleOf(::FinanceRepositoryImpl) bind FinanceRepository::class
    singleOf(::DeviceConfigRepositoryImpl) bind DeviceConfigRepository::class
    singleOf(::InventoryRepositoryImpl) bind InventoryRepository::class
    singleOf(::ReservationRepositoryImpl) bind ReservationRepository::class
    singleOf(::AdminRepositoryImpl) bind AdminRepository::class
    singleOf(::BillingRepositoryImpl) bind BillingRepository::class
    singleOf(::GroupsRepositoryImpl) bind GroupsRepository::class
    singleOf(::CartRepositoryImpl) bind CartRepository::class
}

val viewModelModule = module {
    single { SettingsStore() }
    single { OpenBillStore() }
    viewModelOf(::LoginViewModel)
    viewModelOf(::SplashViewModel)
    viewModelOf(::TenantPickerViewModel)
    viewModelOf(::PosViewModel)
    viewModelOf(::HoldOrderViewModel)
    viewModelOf(::OpenBillViewModel)
    viewModelOf(::CartViewModel)
    viewModelOf(::PaymentViewModel)
    viewModelOf(::ShiftViewModel)
    viewModelOf(::TableViewModel)
    viewModelOf(::ReservationViewModel)
    viewModelOf(::KdsViewModel)
    viewModelOf(::SalesHistoryViewModel)
    viewModelOf(::RefundViewModel)
    viewModelOf(::SplitBillViewModel)
    viewModelOf(::AddItemsToHeldOrderViewModel)
    viewModelOf(::OrderBoardViewModel)
    viewModelOf(::ReportViewModel)
    viewModelOf(::CashExpenseViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProductManagementViewModel)
    viewModelOf(::BillingViewModel)
    viewModelOf(::StockOpnameViewModel)
    viewModelOf(::VoucherManagementViewModel)
    viewModelOf(::PricingManagementViewModel)
}

val appModules = listOf(
    databaseModule,
    dataModule,
    repositoryModule,
    viewModelModule,
    platformModule,
)
