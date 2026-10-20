package com.yogesh.lenskart.feature.home

import androidx.lifecycle.viewModelScope
import com.yogesh.lenskart.core.common.AppResult
import com.yogesh.lenskart.core.common.NetworkMonitor
import com.yogesh.lenskart.core.common.onSuccess
import com.yogesh.lenskart.core.domain.model.ProductFilter
import com.yogesh.lenskart.core.domain.repository.ProductRepository
import com.yogesh.lenskart.core.domain.repository.WishlistRepository
import com.yogesh.lenskart.core.ui.BaseViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val products: ProductRepository,
    private val wishlist: WishlistRepository,
    networkMonitor: NetworkMonitor,
) : BaseViewModel<HomeUiState, HomeUiEvent>(HomeUiState()), HomeActions {

    init {
        // Local DB is the single source of truth; combine it with wishlist + connectivity.
        viewModelScope.launch {
            combine(products.observeProducts(ProductFilter()), wishlist.observeIds(), networkMonitor.isOnline) { list, ids, online ->
                Triple(list, ids, online)
            }.collect { (list, ids, online) ->
                updateState {
                    copy(
                        trending = list.sortedByDescending { it.reviewCount }.take(8),
                        newArrivals = list.sortedByDescending { it.id }.take(8),
                        wishlistIds = ids,
                        isOffline = !online,
                    )
                }
            }
        }
        onRefresh()
    }

    override fun onRefresh() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            val result = products.refresh()
            products.getBanners().onSuccess { banners -> updateState { copy(banners = banners) } }
            updateState {
                copy(
                    isLoading = false,
                    error = if (result is AppResult.Failure && trending.isEmpty()) result.error.message else null,
                )
            }
            if (result is AppResult.Failure) sendEvent(HomeUiEvent.ShowMessage(result.error.message))
        }
    }

    override fun onWishlistToggled(productId: String) { viewModelScope.launch { wishlist.toggle(productId) } }
}
