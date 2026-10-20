package com.yogesh.lenskart.feature.product

import com.yogesh.lenskart.core.domain.model.Product

data class ProductUiState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val selectedColorIndex: Int = 0,
    val isWishlisted: Boolean = false,
)

interface ProductActions {
    fun onColorSelected(index: Int)
    fun onWishlistToggled()
    fun onAddToCart()
    fun onShare()
}

sealed interface ProductUiEvent {
    data object AddedToCart : ProductUiEvent
}
