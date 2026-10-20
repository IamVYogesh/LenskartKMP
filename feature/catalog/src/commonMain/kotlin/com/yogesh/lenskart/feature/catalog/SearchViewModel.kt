package com.yogesh.lenskart.feature.catalog

import androidx.lifecycle.viewModelScope
import com.yogesh.lenskart.core.domain.repository.SearchHistoryRepository
import com.yogesh.lenskart.core.domain.repository.WishlistRepository
import com.yogesh.lenskart.core.domain.usecase.SearchProductsUseCase
import com.yogesh.lenskart.core.ui.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val searchProducts: SearchProductsUseCase,
    private val history: SearchHistoryRepository,
    private val wishlist: WishlistRepository,
) : BaseViewModel<SearchUiState, Nothing>(SearchUiState()), SearchActions {

    private val queryFlow = MutableStateFlow("")

    init {
        // Debounced "search as you type": cancels in-flight work via flatMapLatest.
        viewModelScope.launch {
            queryFlow.debounce(250).distinctUntilChanged()
                .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else searchProducts(q) }
                .collect { updateState { copy(results = it) } }
        }
        viewModelScope.launch { history.observeRecent().collect { updateState { copy(recent = it) } } }
        viewModelScope.launch { wishlist.observeIds().collect { updateState { copy(wishlistIds = it) } } }
    }

    override fun onQueryChanged(query: String) {
        updateState { copy(query = query) }
        queryFlow.value = query
    }

    override fun onSubmit(query: String) { viewModelScope.launch { searchProducts.commit(query) } }
    override fun onProductOpened(productId: String) { viewModelScope.launch { searchProducts.commit(uiState.value.query) } }
    override fun onWishlistToggled(productId: String) { viewModelScope.launch { wishlist.toggle(productId) } }
    override fun onClearHistory() { viewModelScope.launch { history.clear() } }
}
