package com.yogesh.lenskart.feature.home

import com.yogesh.lenskart.core.domain.model.Banner
import com.yogesh.lenskart.core.domain.model.Product

data class HomeUiState(
    val isLoading: Boolean = true,
    val banners: List<Banner> = emptyList(),
    val trending: List<Product> = emptyList(),
    val newArrivals: List<Product> = emptyList(),
    val wishlistIds: Set<String> = emptySet(),
    val isOffline: Boolean = false,
    val error: String? = null,
)

interface HomeActions {
    fun onRefresh()
    fun onWishlistToggled(productId: String)
}

sealed interface HomeUiEvent {
    data class ShowMessage(val text: String) : HomeUiEvent
}
