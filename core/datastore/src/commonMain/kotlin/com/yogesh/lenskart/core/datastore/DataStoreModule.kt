package com.yogesh.lenskart.core.datastore

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

expect val dataStorePlatformModule: Module

val datastoreModule = module {
    includes(dataStorePlatformModule)
    singleOf(::AppPreferences)
}
