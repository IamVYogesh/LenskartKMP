package com.yogesh.lenskart.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Injectable dispatchers => deterministic tests (swap in Unconfined / StandardTestDispatcher). */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = ioDispatcher()
    override val default: CoroutineDispatcher = Dispatchers.Default
}

/** expect/actual #1 (function): Kotlin/Native has no Dispatchers.IO, so each platform decides. */
expect fun ioDispatcher(): CoroutineDispatcher
