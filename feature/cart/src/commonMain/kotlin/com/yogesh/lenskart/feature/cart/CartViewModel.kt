package com.yogesh.lenskart.feature.cart

import androidx.lifecycle.viewModelScope
import com.yogesh.lenskart.core.common.AppError
import com.yogesh.lenskart.core.common.AppResult
import com.yogesh.lenskart.core.domain.repository.CartRepository
import com.yogesh.lenskart.core.domain.usecase.ApplyCouponUseCase
import com.yogesh.lenskart.core.domain.usecase.PlaceOrderUseCase
import com.yogesh.lenskart.core.ui.BaseViewModel
import kotlinx.coroutines.launch

class CartViewModel(
    private val cart: CartRepository,
    private val applyCoupon: ApplyCouponUseCase,
    private val placeOrder: PlaceOrderUseCase,
) : BaseViewModel<CartUiState, CartUiEvent>(CartUiState()), CartActions {

    init {
        viewModelScope.launch { cart.observeCart().collect { s -> updateState { copy(summary = s) } } }
    }

    override fun onIncrement(productId: String, colorName: String) = changeQty(productId, colorName, +1)
    override fun onDecrement(productId: String, colorName: String) = changeQty(productId, colorName, -1)

    override fun onCouponChanged(text: String) = updateState { copy(couponInput = text) }

    override fun onApplyCoupon() {
        viewModelScope.launch {
            updateState { copy(isApplyingCoupon = true) }
            when (val r = applyCoupon(uiState.value.couponInput)) {
                is AppResult.Success -> {
                    updateState { copy(couponInput = "") }
                    sendEvent(CartUiEvent.Message("Coupon ${r.data.code} applied"))
                }
                is AppResult.Failure -> sendEvent(CartUiEvent.Message(r.error.message))
            }
            updateState { copy(isApplyingCoupon = false) }
        }
    }

    override fun onRemoveCoupon() { viewModelScope.launch { cart.removeCoupon() } }

    override fun onPlaceOrder() {
        viewModelScope.launch {
            updateState { copy(isPlacingOrder = true) }
            when (val r = placeOrder()) {
                is AppResult.Success -> updateState { copy(placedOrder = r.data) }
                is AppResult.Failure ->
                    if (r.error is AppError.Unauthorized) sendEvent(CartUiEvent.RequireLogin)
                    else sendEvent(CartUiEvent.Message(r.error.message))
            }
            updateState { copy(isPlacingOrder = false) }
        }
    }

    override fun onDismissOrderDialog() = updateState { copy(placedOrder = null) }

    private fun changeQty(productId: String, colorName: String, delta: Int) {
        val current = uiState.value.summary.items
            .firstOrNull { it.productId == productId && it.colorName == colorName }?.quantity ?: return
        viewModelScope.launch { cart.setQuantity(productId, colorName, current + delta) }
    }
}
