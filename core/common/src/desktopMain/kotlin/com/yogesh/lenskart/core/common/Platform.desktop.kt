package com.yogesh.lenskart.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.UUID

actual class Platform actual constructor() {
    actual val name: String = System.getProperty("os.name") ?: "Desktop"
    actual val version: String = "JVM " + (System.getProperty("java.version") ?: "?")
}

actual fun randomUuid(): String = UUID.randomUUID().toString()

actual fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
