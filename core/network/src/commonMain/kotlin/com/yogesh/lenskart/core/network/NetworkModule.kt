package com.yogesh.lenskart.core.network

import com.yogesh.lenskart.core.network.mock.MockLenskartBackend
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val networkModule = module {
    single<HttpClientEngine> {
        if (get<NetworkConfig>().useMockBackend) MockLenskartBackend.engine() else platformHttpEngine()
    }
    single<HttpClient> {
        val config = get<NetworkConfig>()
        val tokens = get<TokenProvider>()
        createHttpClient(get(), config.baseUrl, { tokens.token() }, config.isDebug)
    }
    singleOf(::LenskartApi)
}
