package id.rancak.app.di

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.createHttpClient
import id.rancak.app.data.repository.*
import id.rancak.app.domain.repository.*
import id.rancak.app.presentation.viewmodel.*
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single { TokenManager() }
    single { createHttpClient(get()) }
    single { RancakApiService(get()) }
}

val repositoryModule = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::ProductRepositoryImpl) bind ProductRepository::class
    singleOf(::SaleRepositoryImpl) bind SaleRepository::class
    singleOf(::OperationsRepositoryImpl) bind OperationsRepository::class
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
}

val appModules = listOf(dataModule, repositoryModule, viewModelModule)
