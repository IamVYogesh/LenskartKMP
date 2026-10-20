package com.yogesh.lenskart.core.common

import android.os.Build
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.UUID

actual class Platform actual constructor() {
    actual val name: String = "Android"
    actual val version: String = Build.VERSION.RELEASE
}

actual fun randomUuid(): String = UUID.randomUUID().toString()

actual fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
