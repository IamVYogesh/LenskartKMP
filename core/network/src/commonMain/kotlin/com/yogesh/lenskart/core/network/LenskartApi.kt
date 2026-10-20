package com.yogesh.lenskart.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

/** Typed REST surface. Paths are relative to NetworkConfig.baseUrl (DefaultRequest plugin). */
class LenskartApi(private val client: HttpClient) {
    suspend fun getProducts(): ProductsResponse = client.get("v1/products").body()
    suspend fun getBanners(): BannersResponse = client.get("v1/banners").body()
    suspend fun requestOtp(phone: String): OtpResponse = client.post("v1/auth/otp") { setBody(OtpRequest(phone)) }.body()
    suspend fun verifyOtp(phone: String, otp: String): VerifyResponse =
        client.post("v1/auth/verify") { setBody(VerifyRequest(phone, otp)) }.body()
    suspend fun validateCoupon(code: String, subtotalInr: Long): CouponResponse =
        client.post("v1/coupons/validate") { setBody(CouponRequest(code, subtotalInr)) }.body()
    suspend fun placeOrder(request: OrderRequest): OrderResponse = client.post("v1/orders") { setBody(request) }.body()
}
