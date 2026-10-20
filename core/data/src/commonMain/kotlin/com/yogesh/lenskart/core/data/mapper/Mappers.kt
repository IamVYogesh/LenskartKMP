package com.yogesh.lenskart.core.data.mapper

import com.yogesh.lenskart.core.common.AppJson
import com.yogesh.lenskart.core.database.ProductEntity
import com.yogesh.lenskart.core.domain.model.*
import com.yogesh.lenskart.core.network.BannerDto
import com.yogesh.lenskart.core.network.ColorDto
import com.yogesh.lenskart.core.network.CouponResponse
import com.yogesh.lenskart.core.network.OrderResponse
import com.yogesh.lenskart.core.network.ProductDto
import com.yogesh.lenskart.core.network.UserDto
import kotlinx.serialization.encodeToString

private inline fun <reified E : Enum<E>> enumOrDefault(name: String, default: E): E =
    enumValues<E>().firstOrNull { it.name == name } ?: default

/** DTO -> DB row. Lists/maps are stored as JSON text columns. */
internal fun ProductDto.toEntity(nowMs: Long) = ProductEntity(
    id = id, name = name, brand = brand, category = category, shape = shape, gender = gender,
    priceInr = priceInr, mrpInr = mrpInr, rating = rating, reviewCount = reviewCount.toLong(),
    description = description,
    imagesJson = AppJson.encodeToString(images),
    colorsJson = AppJson.encodeToString(colors),
    specsJson = AppJson.encodeToString(specs),
    updatedAtEpochMs = nowMs,
)

/** DB row -> domain model. The UI never sees DTOs or SQLDelight rows. */
internal fun ProductEntity.toDomain() = Product(
    id = id, name = name, brand = brand,
    category = enumOrDefault(category, Category.EYEGLASSES),
    shape = enumOrDefault(shape, FrameShape.NONE),
    gender = enumOrDefault(gender, Gender.UNISEX),
    priceInr = priceInr, mrpInr = mrpInr, rating = rating, reviewCount = reviewCount.toInt(),
    description = description,
    images = AppJson.decodeFromString<List<String>>(imagesJson),
    colors = AppJson.decodeFromString<List<ColorDto>>(colorsJson).map { ProductColor(it.name, it.hex) },
    specs = AppJson.decodeFromString<Map<String, String>>(specsJson),
)

internal fun BannerDto.toDomain() = Banner(
    id = id, title = title, subtitle = subtitle, imageUrl = imageUrl,
    category = category?.let { c -> Category.entries.firstOrNull { it.name == c } },
)

internal fun UserDto.toDomain() = User(id = id, name = name, phone = phone)
internal fun CouponResponse.toDomain() = Coupon(code = code, percentOff = percentOff, maxDiscountInr = maxDiscountInr)
internal fun OrderResponse.toDomain(itemCount: Int) = Order(orderId, totalInr, itemCount, etaDays)
