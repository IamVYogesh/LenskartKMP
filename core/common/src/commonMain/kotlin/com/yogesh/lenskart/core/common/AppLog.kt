package com.yogesh.lenskart.core.common

import co.touchlab.kermit.Logger

/** Thin facade over Kermit (KMP logger -> Logcat on Android, os_log on iOS, stdout on desktop). */
object AppLog {
    private val logger = Logger.withTag("LenskartKMP")
    fun d(message: String) = logger.d(message)
    fun e(message: String, throwable: Throwable? = null) = logger.e(message, throwable)
}
