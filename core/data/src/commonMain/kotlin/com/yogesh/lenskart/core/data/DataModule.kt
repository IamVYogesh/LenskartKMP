package com.yogesh.lenskart.core.data

import com.yogesh.lenskart.core.data.repository.*
import com.yogesh.lenskart.core.domain.repository.*
import com.yogesh.lenskart.core.network.TokenProvider
import org.koin.dsl.module

/** Binds domain interfaces to their implementations (implementations stay `internal`). */
val dataModule = module {
    single<TokenProvider> { PrefsTokenProvider(get()) }
    single<ProductRepository> { ProductRepositoryImpl(get(), get(), get()) }
    single<CartRepository> { CartRepositoryImpl(get(), get(), get()) }
    single<WishlistRepository> { WishlistRepositoryImpl(get(), get()) }
    single<SearchHistoryRepository> { SearchHistoryRepositoryImpl(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
}
