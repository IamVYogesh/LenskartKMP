package com.yogesh.lenskart.core.network.mock

import com.yogesh.lenskart.core.network.BannerDto
import com.yogesh.lenskart.core.network.ColorDto
import com.yogesh.lenskart.core.network.ProductDto

/** Fictional brands + picsum placeholder images: no real Lenskart assets are used. */
internal object MockCatalog {
    private class Seed(
        val id: String, val name: String, val brand: String, val category: String, val shape: String,
        val gender: String, val price: Long, val mrp: Long, val rating: Double, val reviews: Int,
    )

    private val palette = listOf(
        ColorDto("Matte Black", "#1C1C1E"), ColorDto("Tortoise", "#8B5A2B"), ColorDto("Navy", "#1F2A5C"),
        ColorDto("Crystal", "#D8E2E8"), ColorDto("Gold", "#C9A227"), ColorDto("Rose", "#C48793"),
    )

    private val seeds = listOf(
        Seed("lk-101", "Air Flex Round", "Aerolite", "EYEGLASSES", "ROUND", "UNISEX", 1499, 2499, 4.5, 3210),
        Seed("lk-102", "Classic Rectangle", "Visora", "EYEGLASSES", "RECTANGLE", "MEN", 999, 1799, 4.3, 5120),
        Seed("lk-103", "Cat-Eye Luxe", "Northfold", "EYEGLASSES", "CAT_EYE", "WOMEN", 1899, 3199, 4.6, 1890),
        Seed("lk-104", "Hex Edge", "Clearview", "EYEGLASSES", "HEXAGON", "UNISEX", 1299, 2199, 4.2, 940),
        Seed("lk-105", "Pilot Aviator", "Aerolite", "SUNGLASSES", "AVIATOR", "MEN", 1799, 2999, 4.4, 2760),
        Seed("lk-106", "Wayfarer Noir", "Visora", "SUNGLASSES", "WAYFARER", "UNISEX", 1599, 2799, 4.7, 6410),
        Seed("lk-107", "Sunset Cat-Eye", "Northfold", "SUNGLASSES", "CAT_EYE", "WOMEN", 2099, 3499, 4.5, 1320),
        Seed("lk-108", "Retro Round Shades", "Clearview", "SUNGLASSES", "ROUND", "UNISEX", 1399, 2399, 4.1, 870),
        Seed("lk-109", "Blue-Cut Screen Pro", "Visora", "COMPUTER_GLASSES", "RECTANGLE", "UNISEX", 1199, 1999, 4.4, 4480),
        Seed("lk-110", "Focus Round Blue-Cut", "Aerolite", "COMPUTER_GLASSES", "ROUND", "UNISEX", 1099, 1899, 4.3, 2210),
        Seed("lk-111", "Kidoze Flex", "Kidoze", "EYEGLASSES", "RECTANGLE", "KIDS", 899, 1499, 4.6, 780),
        Seed("lk-112", "Kidoze Sun Wayfarer", "Kidoze", "SUNGLASSES", "WAYFARER", "KIDS", 799, 1399, 4.4, 410),
        Seed("lk-113", "Slim Metal Aviator", "Northfold", "EYEGLASSES", "AVIATOR", "WOMEN", 2299, 3799, 4.5, 1010),
        Seed("lk-114", "Heritage Wayfarer", "Clearview", "EYEGLASSES", "WAYFARER", "MEN", 1699, 2899, 4.3, 1530),
        Seed("lk-201", "Daily Clear 30-Pack", "Visora", "CONTACT_LENSES", "NONE", "UNISEX", 1299, 1599, 4.5, 3800),
        Seed("lk-202", "Monthly Aqua 6-Pack", "Aerolite", "CONTACT_LENSES", "NONE", "UNISEX", 1699, 2099, 4.4, 2900),
    )

    val products: List<ProductDto> = seeds.mapIndexed { i, s ->
        val isLens = s.category == "CONTACT_LENSES"
        ProductDto(
            id = s.id, name = s.name, brand = s.brand, category = s.category, shape = s.shape, gender = s.gender,
            priceInr = s.price, mrpInr = s.mrp, rating = s.rating, reviewCount = s.reviews,
            description = if (isLens) {
                "Ultra-thin, breathable lenses with built-in UV blocking. Comfortable for up to 14 hours of wear."
            } else {
                "Lightweight ${s.shape.lowercase().replace('_', '-')} frame with spring hinges and anti-glare coating. Made for all-day comfort."
            },
            images = (1..4).map { "https://picsum.photos/seed/${s.id}-$it/800/800" },
            colors = if (isLens) listOf(ColorDto("Clear", "#CFE8F3")) else listOf(palette[i % 6], palette[(i + 2) % 6], palette[(i + 4) % 6]),
            specs = if (isLens) {
                mapOf("Material" to "Silicone hydrogel", "Water content" to "58%", "Replacement" to if (s.id == "lk-201") "Daily" else "Monthly")
            } else {
                mapOf("Frame material" to "TR90 / Acetate", "Frame width" to "Medium (135 mm)", "Weight" to "18 g", "Lens" to "Anti-glare, UV400", "Warranty" to "1 year")
            },
        )
    }

    val banners = listOf(
        BannerDto("b1", "Buy 1 Get 1 Free", "On all eyeglasses this week", "https://picsum.photos/seed/lk-banner-1/1000/500", "EYEGLASSES"),
        BannerDto("b2", "Summer Shades", "Sunglasses from ₹799", "https://picsum.photos/seed/lk-banner-2/1000/500", "SUNGLASSES"),
        BannerDto("b3", "Screen-time care", "Blue-cut computer glasses", "https://picsum.photos/seed/lk-banner-3/1000/500", "COMPUTER_GLASSES"),
    )
}
