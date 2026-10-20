package com.yogesh.lenskart.core.network

import kotlinx.serialization.Serializable

@Serializable data class ColorDto(val name: String, val hex: String)

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val brand: String,
    val category: String,
    val shape: String,
    val gender: String,
    val priceInr: Long,
    val mrpInr: Long,
    val rating: Double,
    val reviewCount: Int,
    val description: String,
    val images: List<String>,
    val colors: List<ColorDto>,
    val specs: Map<String, String> = emptyMap(),
)

@Serializable data class ProductsResponse(val products: List<ProductDto>)

@Serializable
data class BannerDto(val id: String, val title: String, val subtitle: String, val imageUrl: String, val category: String? = null)
@Serializable data class BannersResponse(val banners: List<BannerDto>)

@Serializable data class UserDto(val id: String, val name: String? = null, val phone: String)
@Serializable data class OtpRequest(val phone: String)
@Serializable data class OtpResponse(val requestId: String, val expiresInSec: Int)
@Serializable data class VerifyRequest(val phone: String, val otp: String)
@Serializable data class VerifyResponse(val token: String, val user: UserDto)

@Serializable data class CouponRequest(val code: String, val subtotalInr: Long)
@Serializable data class CouponResponse(val code: String, val percentOff: Int, val maxDiscountInr: Long)

@Serializable data class OrderItemDto(val productId: String, val colorName: String, val quantity: Int)
@Serializable data class OrderRequest(val items: List<OrderItemDto>, val couponCode: String?, val totalInr: Long)
@Serializable data class OrderResponse(val orderId: String, val totalInr: Long, val etaDays: Int)

@Serializable data class ErrorDto(val message: String)
