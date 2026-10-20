package com.yogesh.lenskart.core.domain.model

data class CartItem(
    val productId: String,
    val name: String,
    val brand: String,
    val imageUrl: String,
    val colorName: String,
    val unitPriceInr: Long,
    val unitMrpInr: Long,
    val quantity: Int,
)

data class Coupon(val code: String, val percentOff: Int, val maxDiscountInr: Long)

/** All money maths lives in the domain layer: pure Kotlin, trivially unit-testable on every target. */
data class CartSummary(val items: List<CartItem> = emptyList(), val coupon: Coupon? = null) {
    val itemCount: Int get() = items.sumOf { it.quantity }
    val subtotalInr: Long get() = items.sumOf { it.unitPriceInr * it.quantity }
    val mrpTotalInr: Long get() = items.sumOf { it.unitMrpInr * it.quantity }
    val couponDiscountInr: Long
        get() = coupon?.let { minOf(subtotalInr * it.percentOff / 100, it.maxDiscountInr) } ?: 0L
    val deliveryFeeInr: Long
        get() = if (items.isEmpty() || subtotalInr >= FREE_DELIVERY_THRESHOLD_INR) 0L else DELIVERY_FEE_INR
    val totalInr: Long get() = subtotalInr - couponDiscountInr + deliveryFeeInr
    val savingsInr: Long get() = (mrpTotalInr - subtotalInr) + couponDiscountInr

    companion object {
        const val FREE_DELIVERY_THRESHOLD_INR = 1500L
        const val DELIVERY_FEE_INR = 99L
    }
}
