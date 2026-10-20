package com.yogesh.lenskart.feature.catalog

import androidx.lifecycle.viewModelScope
import com.yogesh.lenskart.core.domain.model.Category
import com.yogesh.lenskart.core.domain.model.FrameShape
import com.yogesh.lenskart.core.domain.model.Gender
import com.yogesh.lenskart.core.domain.model.ProductFilter
import com.yogesh.lenskart.core.domain.model.SortOption
import com.yogesh.lenskart.core.domain.repository.ProductRepository
import com.yogesh.lenskart.core.domain.repository.WishlistRepository
import com.yogesh.lenskart.core.ui.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
/** [initialCategory] arrives from navigation args via Koin `parametersOf`. */
class CatalogViewModel(
    initialCategory: String,
    private val products: ProductRepository,
    private val wishlist: WishlistRepository,
) : BaseViewModel<CatalogUiState, Nothing>(
    CatalogUiState(filter = ProductFilter(category = Category.entries.firstOrNull { it.name == initialCategory })),
), CatalogActions {

    private val filterFlow = uiState.map { it.filter }.distinctUntilChanged()

    init {
        viewModelScope.launch {
            filterFlow
                .flatMapLatest { f -> products.observeProducts(f) }
                .combine(wishlist.observeIds()) { list, ids -> list to ids }
                .collect { (list, ids) -> updateState { copy(products = list, wishlistIds = ids, isLoading = false) } }
        }
        viewModelScope.launch { products.refresh() }
    }

    override fun onCategorySelected(category: Category?) = updateFilter { copy(category = category) }
    override fun onShapeToggled(shape: FrameShape) = updateFilter { copy(shapes = shapes.toggle(shape)) }
    override fun onGenderToggled(gender: Gender) = updateFilter { copy(genders = genders.toggle(gender)) }
    override fun onMaxPriceChanged(maxPriceInr: Long?) = updateFilter { copy(maxPriceInr = maxPriceInr) }
    override fun onSortSelected(sort: SortOption) = updateFilter { copy(sort = sort) }
    override fun onClearFilters() = updateFilter { copy(shapes = emptySet(), genders = emptySet(), maxPriceInr = null) }
    override fun onWishlistToggled(productId: String) { viewModelScope.launch { wishlist.toggle(productId) } }
    override fun onShowFilters() = updateState { copy(showFilters = true) }
    override fun onHideFilters() = updateState { copy(showFilters = false) }

    private fun updateFilter(change: ProductFilter.() -> ProductFilter) = updateState { copy(filter = filter.change()) }
    private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
}
