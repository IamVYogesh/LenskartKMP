package com.yogesh.lenskart.core.database

import app.cash.sqldelight.db.SqlDriver
import org.koin.core.module.Module
import org.koin.dsl.module

/** Each platform supplies a SqlDriver; everything above it is shared code. */
expect val databasePlatformModule: Module

val databaseModule = module {
    includes(databasePlatformModule)
    single { LenskartDatabase(get<SqlDriver>()) }
}
