package com.yogesh.lenskart.core.network

import com.yogesh.lenskart.core.common.AppError
import com.yogesh.lenskart.core.common.AppResult
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Converts any exception into a typed [AppResult.Failure]. Never swallows coroutine cancellation. */
suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: ResponseException) {
    val code = e.response.status.value
    val serverMessage = runCatching { e.response.body<ErrorDto>().message }.getOrNull()
    if (code == 401) AppResult.Failure(AppError.Unauthorized)
    else AppResult.Failure(AppError.Server(code, serverMessage ?: "Server error ($code)"))
} catch (e: HttpRequestTimeoutException) {
    AppResult.Failure(AppError.Offline)
} catch (e: IOException) {
    AppResult.Failure(AppError.Offline)
} catch (e: Exception) {
    AppResult.Failure(AppError.Unknown(e.message ?: "Something went wrong"))
}
