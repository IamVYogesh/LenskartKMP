package com.yogesh.lenskart.core.common

/** Typed success/failure wrapper used by every repository so UI never sees raw exceptions. */
sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

sealed class AppError(val message: String) {
    data object Offline : AppError("You appear to be offline")
    data object Unauthorized : AppError("Please sign in to continue")
    data class Server(val code: Int, val detail: String) : AppError(detail)
    data class Validation(val detail: String) : AppError(detail)
    data class Unknown(val detail: String = "Something went wrong") : AppError(detail)
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

inline fun <T> AppResult<T>.onSuccess(block: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) block(data)
    return this
}

inline fun <T> AppResult<T>.onFailure(block: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) block(error)
    return this
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.data
