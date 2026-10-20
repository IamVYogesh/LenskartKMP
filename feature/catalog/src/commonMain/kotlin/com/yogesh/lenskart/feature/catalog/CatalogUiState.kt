package com.yogesh.lenskart.feature.catalog

import com.yogesh.lenskart.core.domain.model.*

data class CatalogUiState(
    val filter: ProductFilter = ProductFilter(),
    val products: List<Product> = emptyList(),
    val wishlistIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val showFilters: Boolean = false,
)

interface CatalogActions {
    fun onCategorySelected(category: Category?)
    fun onShapeToggled(shape: FrameShape)
    fun onGenderToggled(gender: Gender)
    fun onMaxPriceChanged(maxPriceInr: Long?)
    fun onSortSelected(sort: SortOption)
    fun onWishlistToggled(productId: String)
    fun onClearFilters()
    fun onShowFilters()
    fun onHideFilters()
}

data class SearchUiState(
    val query: String = "",
    val recent: List<String> = emptyList(),
    val results: List<Product> = emptyList(),
    val wishlistIds: Set<String> = emptySet(),
)

interface SearchActions {
    fun onQueryChanged(query: String)
    fun onSubmit(query: String)
    fun onProductOpened(productId: String)
    fun onWishlistToggled(productId: String)
    fun onClearHistory()
}
