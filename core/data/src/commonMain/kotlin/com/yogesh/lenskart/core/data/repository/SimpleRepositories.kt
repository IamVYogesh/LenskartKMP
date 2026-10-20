package com.yogesh.lenskart.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.yogesh.lenskart.core.common.AppError
import com.yogesh.lenskart.core.common.AppJson
import com.yogesh.lenskart.core.common.AppResult
import com.yogesh.lenskart.core.common.DispatcherProvider
import com.yogesh.lenskart.core.common.map
import com.yogesh.lenskart.core.common.onSuccess
import com.yogesh.lenskart.core.data.mapper.toDomain
import com.yogesh.lenskart.core.database.LenskartDatabase
import com.yogesh.lenskart.core.datastore.AppPreferences
import com.yogesh.lenskart.core.domain.model.Product
import com.yogesh.lenskart.core.domain.model.Session
import com.yogesh.lenskart.core.domain.model.ThemeMode
import com.yogesh.lenskart.core.domain.repository.AuthRepository
import com.yogesh.lenskart.core.domain.repository.SearchHistoryRepository
import com.yogesh.lenskart.core.domain.repository.SettingsRepository
import com.yogesh.lenskart.core.domain.repository.WishlistRepository
import com.yogesh.lenskart.core.network.LenskartApi
import com.yogesh.lenskart.core.network.TokenProvider
import com.yogesh.lenskart.core.network.UserDto
import com.yogesh.lenskart.core.network.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString

internal class WishlistRepositoryImpl(
    private val db: LenskartDatabase,
    private val dispatchers: DispatcherProvider,
) : WishlistRepository {
    override fun observeIds(): Flow<Set<String>> =
        db.wishlistQueries.selectIds().asFlow().mapToList(dispatchers.io).map { it.toSet() }

    override fun observeProducts(): Flow<List<Product>> =
        db.wishlistQueries.selectProducts().asFlow().mapToList(dispatchers.io).map { rows -> rows.map { it.toDomain() } }

    override suspend fun toggle(productId: String) = withContext(dispatchers.io) {
        db.transaction {
            if (db.wishlistQueries.exists(productId).executeAsOne() > 0L) {
                db.wishlistQueries.delete(productId)
            } else {
                db.wishlistQueries.insert(productId, Clock.System.now().toEpochMilliseconds())
            }
        }
    }
}

internal class SearchHistoryRepositoryImpl(
    private val db: LenskartDatabase,
    private val dispatchers: DispatcherProvider,
) : SearchHistoryRepository {
    override fun observeRecent(): Flow<List<String>> =
        db.searchHistoryQueries.selectRecent().asFlow().mapToList(dispatchers.io)

    override suspend fun record(query: String) = withContext(dispatchers.io) {
        db.searchHistoryQueries.upsert(query, Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun clear() = withContext(dispatchers.io) { db.searchHistoryQueries.deleteAll() }
}

internal class AuthRepositoryImpl(
    private val api: LenskartApi,
    private val prefs: AppPreferences,
) : AuthRepository {

    override fun observeSession(): Flow<Session?> = combine(prefs.authToken, prefs.userJson) { token, userJson ->
        if (token != null && userJson != null) {
            runCatching { Session(token, AppJson.decodeFromString<UserDto>(userJson).toDomain()) }.getOrNull()
        } else null
    }

    override suspend fun requestOtp(phone: String): AppResult<Unit> {
        if (!Regex("^[6-9][0-9]{9}$").matches(phone)) {
            return AppResult.Failure(AppError.Validation("Enter a valid 10-digit mobile number"))
        }
        return safeApiCall { api.requestOtp(phone) }.map { }
    }

    override suspend fun verifyOtp(phone: String, otp: String): AppResult<Session> =
        safeApiCall { api.verifyOtp(phone, otp) }
            .onSuccess { prefs.saveSession(it.token, AppJson.encodeToString(it.user)) }
            .map { Session(it.token, it.user.toDomain()) }

    override suspend fun logout() = prefs.clearSession()
}

internal class SettingsRepositoryImpl(private val prefs: AppPreferences) : SettingsRepository {
    override fun observeThemeMode(): Flow<ThemeMode> =
        prefs.themeMode.map { saved -> ThemeMode.entries.firstOrNull { it.name == saved } ?: ThemeMode.SYSTEM }

    override suspend fun setThemeMode(mode: ThemeMode) = prefs.setThemeMode(mode.name)
}

/** Bridges DataStore -> network layer so core:network never depends on core:datastore. */
internal class PrefsTokenProvider(private val prefs: AppPreferences) : TokenProvider {
    override suspend fun token(): String? = prefs.authToken.first()
}
