package com.yogesh.lenskart.core.network

import com.yogesh.lenskart.core.common.AppJson
import com.yogesh.lenskart.core.common.AppLog
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

class NetworkConfig(val baseUrl: String, val useMockBackend: Boolean, val isDebug: Boolean)

/** Supplies the bearer token lazily; implemented in core:data on top of DataStore. */
fun interface TokenProvider {
    suspend fun token(): String?
}

fun createHttpClient(
    engine: HttpClientEngine,
    baseUrl: String,
    tokenProvider: suspend () -> String?,
    isDebug: Boolean,
): HttpClient = HttpClient(engine) {
    expectSuccess = true // non-2xx => ResponseException => mapped by safeApiCall
    install(ContentNegotiation) { json(AppJson) }
    install(HttpTimeout) {
        connectTimeoutMillis = 10_000
        requestTimeoutMillis = 20_000
    }
    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 2)
        exponentialDelay()
    }
    install(Logging) {
        level = if (isDebug) LogLevel.INFO else LogLevel.NONE
        logger = object : Logger {
            override fun log(message: String) = AppLog.d(message)
        }
    }
    install(Auth) {
        bearer {
            loadTokens { tokenProvider()?.let { BearerTokens(it, null) } }
            sendWithoutRequest { true }
        }
    }
    defaultRequest {
        url(baseUrl)
        contentType(ContentType.Application.Json)
    }
}
