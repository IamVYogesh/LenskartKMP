package com.yogesh.lenskart.core.domain.repository

import com.yogesh.lenskart.core.common.AppResult
import com.yogesh.lenskart.core.domain.model.*
import kotlinx.coroutines.flow.Flow

/** Domain-layer contracts. Features depend on these; core:data provides the implementations. */
interface ProductRepository {
    /** Offline-first: always reads the local cache. Call [refresh] to sync from network. */
    fun observeProducts(filter: ProductFilter = ProductFilter()): Flow<List<Product>>
    fun observeProduct(id: String): Flow<Product?>
    suspend fun refresh(): AppResult<Unit>
    suspend fun getBanners(): AppResult<List<Banner>>
}

interface CartRepository {
    fun observeCart(): Flow<CartSummary>
    suspend fun addItem(productId: String, colorName: String)
    suspend fun setQuantity(productId: String, colorName: String, quantity: Int)
    suspend fun applyCoupon(code: String): AppResult<Coupon>
    suspend fun removeCoupon()
    suspend fun placeOrder(): AppResult<Order>
}

interface WishlistRepository {
    fun observeIds(): Flow<Set<String>>
    fun observeProducts(): Flow<List<Product>>
    suspend fun toggle(productId: String)
}

interface SearchHistoryRepository {
    fun observeRecent(): Flow<List<String>>
    suspend fun record(query: String)
    suspend fun clear()
}

interface AuthRepository {
    fun observeSession(): Flow<Session?>
    suspend fun requestOtp(phone: String): AppResult<Unit>
    suspend fun verifyOtp(phone: String, otp: String): AppResult<Session>
    suspend fun logout()
}

interface SettingsRepository {
    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
