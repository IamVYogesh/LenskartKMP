package com.yogesh.lenskart.core.domain.model

enum class Category(val displayName: String, val emoji: String) {
    EYEGLASSES("Eyeglasses", "👓"),
    SUNGLASSES("Sunglasses", "🕶️"),
    COMPUTER_GLASSES("Computer Glasses", "🖥️"),
    CONTACT_LENSES("Contact Lenses", "👁️"),
}

enum class FrameShape(val displayName: String) {
    ROUND("Round"), RECTANGLE("Rectangle"), AVIATOR("Aviator"),
    WAYFARER("Wayfarer"), CAT_EYE("Cat-Eye"), HEXAGON("Hexagon"), NONE("N/A"),
}

enum class Gender(val displayName: String) { MEN("Men"), WOMEN("Women"), KIDS("Kids"), UNISEX("Unisex") }

enum class SortOption(val displayName: String) {
    POPULAR("Popular"), PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low"), RATING("Top Rated"),
}

enum class ThemeMode(val displayName: String) { SYSTEM("System"), LIGHT("Light"), DARK("Dark") }

data class ProductColor(val name: String, val hex: String)

data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val category: Category,
    val shape: FrameShape,
    val gender: Gender,
    val priceInr: Long,
    val mrpInr: Long,
    val rating: Double,
    val reviewCount: Int,
    val description: String,
    val images: List<String>,
    val colors: List<ProductColor>,
    val specs: Map<String, String>,
) {
    val discountPercent: Int
        get() = if (mrpInr > priceInr && mrpInr > 0) (((mrpInr - priceInr) * 100) / mrpInr).toInt() else 0
    val thumbnail: String get() = images.firstOrNull().orEmpty()
}

data class Banner(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val category: Category?,
)

data class User(val id: String, val name: String?, val phone: String)
data class Session(val token: String, val user: User)

data class Order(val id: String, val totalInr: Long, val itemCount: Int, val etaDays: Int)
