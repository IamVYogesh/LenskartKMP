package com.yogesh.lenskart

import android.app.Application
import org.koin.android.ext.koin.androidContext

class LenskartApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@LenskartApp) }
    }
}
