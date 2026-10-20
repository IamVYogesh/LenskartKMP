package com.yogesh.lenskart.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val databasePlatformModule: Module = module {
    single<SqlDriver> {
        val dir = File(System.getProperty("user.home"), ".lenskartkmp").apply { mkdirs() }
        val file = File(dir, "lenskart.db")
        val isNew = !file.exists()
        JdbcSqliteDriver("jdbc:sqlite:${file.absolutePath}").also { if (isNew) LenskartDatabase.Schema.create(it) }
    }
}
