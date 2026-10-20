package com.yogesh.lenskart.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataStorePlatformModule: Module = module {
    single<DataStore<Preferences>> {
        val context = androidContext()
        createDataStore { context.filesDir.resolve(DATASTORE_FILE).absolutePath }
    }
}
