package com.yogesh.lenskart.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val databasePlatformModule: Module = module {
    single<SqlDriver> { AndroidSqliteDriver(LenskartDatabase.Schema, androidContext(), "lenskart.db") }
}
