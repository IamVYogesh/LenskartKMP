package com.yogesh.lenskart.core.domain.usecase

import com.yogesh.lenskart.core.common.AppError
import com.yogesh.lenskart.core.common.AppResult
import com.yogesh.lenskart.core.domain.model.Coupon
import com.yogesh.lenskart.core.domain.model.Order
import com.yogesh.lenskart.core.domain.model.Product
import com.yogesh.lenskart.core.domain.model.ProductFilter
import com.yogesh.lenskart.core.domain.repository.AuthRepository
import com.yogesh.lenskart.core.domain.repository.CartRepository
import com.yogesh.lenskart.core.domain.repository.ProductRepository
import com.yogesh.lenskart.core.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SearchProductsUseCase(
    private val products: ProductRepository,
    private val history: SearchHistoryRepository,
) {
    operator fun invoke(query: String): Flow<List<Product>> =
        products.observeProducts(ProductFilter(query = query))

    /** Called when the user submits / taps a result, so history only keeps intentional searches. */
    suspend fun commit(query: String) {
        val q = query.trim()
        if (q.length >= 2) history.record(q)
    }
}

class ApplyCouponUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(raw: String): AppResult<Coupon> {
        val code = raw.trim().uppercase()
        if (code.length < 3) return AppResult.Failure(AppError.Validation("Enter a valid coupon code"))
        return cart.applyCoupon(code)
    }
}

class PlaceOrderUseCase(
    private val cart: CartRepository,
    private val auth: AuthRepository,
) {
    suspend operator fun invoke(): AppResult<Order> {
        if (auth.observeSession().first() == null) return AppResult.Failure(AppError.Unauthorized)
        if (cart.observeCart().first().items.isEmpty()) {
            return AppResult.Failure(AppError.Validation("Your cart is empty"))
        }
        return cart.placeOrder()
    }
}
