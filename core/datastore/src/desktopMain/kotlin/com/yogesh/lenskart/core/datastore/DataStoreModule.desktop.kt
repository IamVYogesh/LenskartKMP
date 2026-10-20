package com.yogesh.lenskart.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val dataStorePlatformModule: Module = module {
    single<DataStore<Preferences>> {
        createDataStore {
            val dir = File(System.getProperty("user.home"), ".lenskartkmp").apply { mkdirs() }
            File(dir, DATASTORE_FILE).absolutePath
        }
    }
}
