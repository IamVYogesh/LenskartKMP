package com.yogesh.lenskart.core.domain.model

data class ProductFilter(
    val category: Category? = null,
    val shapes: Set<FrameShape> = emptySet(),
    val genders: Set<Gender> = emptySet(),
    val maxPriceInr: Long? = null,
    val query: String = "",
    val sort: SortOption = SortOption.POPULAR,
) {
    val activeFilterCount: Int
        get() = (if (shapes.isNotEmpty()) 1 else 0) + (if (genders.isNotEmpty()) 1 else 0) + (if (maxPriceInr != null) 1 else 0)
}

fun ProductFilter.apply(products: List<Product>): List<Product> {
    val q = query.trim()
    val filtered = products.filter { p ->
        (category == null || p.category == category) &&
            (shapes.isEmpty() || p.shape in shapes) &&
            (genders.isEmpty() || p.gender in genders || p.gender == Gender.UNISEX) &&
            (maxPriceInr == null || p.priceInr <= maxPriceInr) &&
            (
                q.isEmpty() ||
                    p.name.contains(q, ignoreCase = true) ||
                    p.brand.contains(q, ignoreCase = true) ||
                    p.shape.displayName.contains(q, ignoreCase = true) ||
                    p.category.displayName.contains(q, ignoreCase = true)
                )
    }
    return when (sort) {
        SortOption.POPULAR -> filtered.sortedByDescending { it.reviewCount }
        SortOption.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.priceInr }
        SortOption.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.priceInr }
        SortOption.RATING -> filtered.sortedByDescending { it.rating }
    }
}
