package com.yogesh.lenskart.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

actual val databasePlatformModule: Module = module {
    single<SqlDriver> { NativeSqliteDriver(LenskartDatabase.Schema, "lenskart.db") }
}
