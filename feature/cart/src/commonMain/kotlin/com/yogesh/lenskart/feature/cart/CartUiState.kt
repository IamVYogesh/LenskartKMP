package com.yogesh.lenskart.feature.cart

import com.yogesh.lenskart.core.domain.model.CartSummary
import com.yogesh.lenskart.core.domain.model.Order

data class CartUiState(
    val summary: CartSummary = CartSummary(),
    val couponInput: String = "",
    val isApplyingCoupon: Boolean = false,
    val isPlacingOrder: Boolean = false,
    val placedOrder: Order? = null,
)

interface CartActions {
    fun onIncrement(productId: String, colorName: String)
    fun onDecrement(productId: String, colorName: String)
    fun onCouponChanged(text: String)
    fun onApplyCoupon()
    fun onRemoveCoupon()
    fun onPlaceOrder()
    fun onDismissOrderDialog()
}

/** One-shot UI events (never replayed). */
sealed interface CartUiEvent {
    data class Message(val text: String) : CartUiEvent
    data object RequireLogin : CartUiEvent
}
