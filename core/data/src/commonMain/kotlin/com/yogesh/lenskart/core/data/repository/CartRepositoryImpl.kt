package com.yogesh.lenskart.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.yogesh.lenskart.core.common.AppJson
import com.yogesh.lenskart.core.common.AppResult
import com.yogesh.lenskart.core.common.DispatcherProvider
import com.yogesh.lenskart.core.common.map
import com.yogesh.lenskart.core.common.onSuccess
import com.yogesh.lenskart.core.data.mapper.toDomain
import com.yogesh.lenskart.core.database.CartLine
import com.yogesh.lenskart.core.database.LenskartDatabase
import com.yogesh.lenskart.core.domain.model.CartItem
import com.yogesh.lenskart.core.domain.model.CartSummary
import com.yogesh.lenskart.core.domain.model.Coupon
import com.yogesh.lenskart.core.domain.model.Order
import com.yogesh.lenskart.core.domain.repository.CartRepository
import com.yogesh.lenskart.core.network.LenskartApi
import com.yogesh.lenskart.core.network.OrderItemDto
import com.yogesh.lenskart.core.network.OrderRequest
import com.yogesh.lenskart.core.network.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal class CartRepositoryImpl(
    private val api: LenskartApi,
    private val db: LenskartDatabase,
    private val dispatchers: DispatcherProvider,
) : CartRepository {

    private val coupon = MutableStateFlow<Coupon?>(null)

    override fun observeCart(): Flow<CartSummary> =
        combine(db.cartQueries.selectCartItems().asFlow().mapToList(dispatchers.io), coupon) { rows, c ->
            CartSummary(
                items = rows.map { r ->
                    CartItem(
                        productId = r.productId, name = r.name, brand = r.brand,
                        imageUrl = AppJson.decodeFromString<List<String>>(r.imagesJson).firstOrNull().orEmpty(),
                        colorName = r.colorName, unitPriceInr = r.priceInr, unitMrpInr = r.mrpInr,
                        quantity = r.quantity.toInt(),
                    )
                },
                coupon = c,
            )
        }

    override suspend fun addItem(productId: String, colorName: String) = withContext(dispatchers.io) {
        val current = db.cartQueries.selectLine(productId, colorName).executeAsOneOrNull()?.quantity ?: 0L
        db.cartQueries.upsert(CartLine(productId, colorName, current + 1))
    }

    override suspend fun setQuantity(productId: String, colorName: String, quantity: Int) =
        withContext(dispatchers.io) {
            if (quantity <= 0) db.cartQueries.deleteLine(productId, colorName)
            else db.cartQueries.upsert(CartLine(productId, colorName, quantity.toLong()))
        }

    override suspend fun applyCoupon(code: String): AppResult<Coupon> {
        val subtotal = observeCart().first().subtotalInr
        return safeApiCall { api.validateCoupon(code, subtotal) }
            .map { it.toDomain() }
            .onSuccess { coupon.value = it }
    }

    override suspend fun removeCoupon() { coupon.value = null }

    override suspend fun placeOrder(): AppResult<Order> {
        val summary = observeCart().first()
        val request = OrderRequest(
            items = summary.items.map { OrderItemDto(it.productId, it.colorName, it.quantity) },
            couponCode = summary.coupon?.code,
            totalInr = summary.totalInr,
        )
        return safeApiCall { api.placeOrder(request) }
            .map { it.toDomain(summary.itemCount) }
            .onSuccess {
                withContext(dispatchers.io) { db.cartQueries.deleteAll() }
                coupon.value = null
            }
    }
}
