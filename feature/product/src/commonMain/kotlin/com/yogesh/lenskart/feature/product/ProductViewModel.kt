package com.yogesh.lenskart.feature.product

import androidx.lifecycle.viewModelScope
import com.yogesh.lenskart.core.common.ShareService
import com.yogesh.lenskart.core.common.formatInr
import com.yogesh.lenskart.core.domain.repository.CartRepository
import com.yogesh.lenskart.core.domain.repository.ProductRepository
import com.yogesh.lenskart.core.domain.repository.WishlistRepository
import com.yogesh.lenskart.core.ui.BaseViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productId: String,
    private val products: ProductRepository,
    private val wishlist: WishlistRepository,
    private val cart: CartRepository,
    private val shareService: ShareService,
) : BaseViewModel<ProductUiState, ProductUiEvent>(ProductUiState()), ProductActions {

    init {
        viewModelScope.launch {
            combine(products.observeProduct(productId), wishlist.observeIds()) { p, ids -> p to ids }
                .collect { (p, ids) -> updateState { copy(isLoading = false, product = p, isWishlisted = productId in ids) } }
        }
        // Deep-link safety: if the product isn't cached yet, pull the catalogue.
        viewModelScope.launch { products.refresh() }
    }

    override fun onColorSelected(index: Int) = updateState { copy(selectedColorIndex = index) }

    override fun onWishlistToggled() { viewModelScope.launch { wishlist.toggle(productId) } }

    override fun onAddToCart() {
        val product = uiState.value.product ?: return
        viewModelScope.launch {
            val color = product.colors.getOrNull(uiState.value.selectedColorIndex)?.name ?: "Default"
            cart.addItem(product.id, color)
            sendEvent(ProductUiEvent.AddedToCart)
        }
    }

    override fun onShare() {
        val product = uiState.value.product ?: return
        shareService.shareText("${product.name} by ${product.brand} for ${product.priceInr.formatInr()} - Lenskart KMP")
    }
}
