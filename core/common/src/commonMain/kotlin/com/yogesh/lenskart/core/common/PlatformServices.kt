package com.yogesh.lenskart.core.common

import kotlinx.coroutines.flow.Flow
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Approach B for platform code: define an INTERFACE in common, implement per platform and
 * wire it through DI (see commonPlatformModule). Compare with expect/actual above.
 */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}

interface ShareService {
    fun shareText(text: String)
}

/** expect/actual #4 (a Koin module): every platform contributes its own bindings. */
expect val commonPlatformModule: Module

val commonModule: Module = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    includes(commonPlatformModule)
}
