package com.yogesh.lenskart

/**
 * Build-time config. In a real project generate this per build type/flavour with the
 * BuildKonfig Gradle plugin (KMP equivalent of Android's BuildConfig).
 */
object AppConfig {
    const val BASE_URL = "https://api.lenskart-demo.example/"
    const val USE_MOCK_BACKEND = true
    const val DEBUG = true
}
