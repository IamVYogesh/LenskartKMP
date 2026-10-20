package com.yogesh.lenskart

import com.yogesh.lenskart.core.common.commonModule
import com.yogesh.lenskart.core.data.dataModule
import com.yogesh.lenskart.core.database.databaseModule
import com.yogesh.lenskart.core.datastore.datastoreModule
import com.yogesh.lenskart.core.domain.domainModule
import com.yogesh.lenskart.core.network.NetworkConfig
import com.yogesh.lenskart.core.network.networkModule
import com.yogesh.lenskart.feature.account.accountModule
import com.yogesh.lenskart.feature.cart.cartModule
import com.yogesh.lenskart.feature.catalog.catalogModule
import com.yogesh.lenskart.feature.home.homeModule
import com.yogesh.lenskart.feature.product.productModule
import com.yogesh.lenskart.feature.tryon.tryOnModule
import com.yogesh.lenskart.feature.wishlist.wishlistModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private val configModule = module {
    single { NetworkConfig(AppConfig.BASE_URL, AppConfig.USE_MOCK_BACKEND, AppConfig.DEBUG) }
}

val appModules = listOf(
    configModule, commonModule, databaseModule, datastoreModule, networkModule, dataModule, domainModule,
    homeModule, catalogModule, productModule, cartModule, wishlistModule, accountModule, tryOnModule,
)

/** Single entry point for every platform. Safe to call twice (iOS can recreate controllers). */
fun initKoin(config: KoinAppDeclaration? = null) {
    if (GlobalContext.getOrNull() != null) return
    startKoin {
        config?.invoke(this)
        modules(appModules)
    }
}
