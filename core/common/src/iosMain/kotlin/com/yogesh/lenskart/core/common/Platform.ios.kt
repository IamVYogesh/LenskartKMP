package com.yogesh.lenskart.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSUUID
import platform.UIKit.UIDevice

actual class Platform actual constructor() {
    actual val name: String = UIDevice.currentDevice.systemName()
    actual val version: String = UIDevice.currentDevice.systemVersion
}

actual fun randomUuid(): String = NSUUID().UUIDString()

// Kotlin/Native has no Dispatchers.IO: Default is a shared background pool.
actual fun ioDispatcher(): CoroutineDispatcher = Dispatchers.Default
