package com.yogesh.lenskart.core.network.mock

import com.yogesh.lenskart.core.common.AppJson
import com.yogesh.lenskart.core.network.*
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString

/**
 * In-process fake server built on Ktor's MockEngine. The REAL HttpClient pipeline (JSON, auth,
 * retry, timeouts, error mapping) runs against it, so swapping to a real backend is just
 * NetworkConfig.useMockBackend = false. Demo OTP: 123456. Coupons: SAVE10, WELCOME20.
 */
object MockLenskartBackend {
    private val coupons = mapOf(
        "SAVE10" to CouponResponse("SAVE10", 10, 500),
        "WELCOME20" to CouponResponse("WELCOME20", 20, 1000),
    )

    fun engine(latencyMs: Long = 350): HttpClientEngine = MockEngine { request ->
        delay(latencyMs)
        val path = request.url.encodedPath.trimEnd('/')
        when {
            request.method == HttpMethod.Get && path.endsWith("/v1/products") ->
                json(AppJson.encodeToString(ProductsResponse(MockCatalog.products)))
            request.method == HttpMethod.Get && path.endsWith("/v1/banners") ->
                json(AppJson.encodeToString(BannersResponse(MockCatalog.banners)))
            request.method == HttpMethod.Post && path.endsWith("/v1/auth/otp") ->
                json(AppJson.encodeToString(OtpResponse("req-1", 60)))
            request.method == HttpMethod.Post && path.endsWith("/v1/auth/verify") -> {
                val body = AppJson.decodeFromString<VerifyRequest>(request.bodyText())
                if (body.otp == "123456") {
                    json(AppJson.encodeToString(VerifyResponse("mock-token-${body.phone}", UserDto("u-${body.phone}", "Lens Lover", body.phone))))
                } else error(HttpStatusCode.BadRequest, "Invalid OTP. Try 123456")
            }
            request.method == HttpMethod.Post && path.endsWith("/v1/coupons/validate") -> {
                val body = AppJson.decodeFromString<CouponRequest>(request.bodyText())
                coupons[body.code.uppercase()]?.let { json(AppJson.encodeToString(it)) }
                    ?: error(HttpStatusCode.NotFound, "Coupon not valid")
            }
            request.method == HttpMethod.Post && path.endsWith("/v1/orders") -> {
                val body = AppJson.decodeFromString<OrderRequest>(request.bodyText())
                json(AppJson.encodeToString(OrderResponse("LK${(100000..999999).random()}", body.totalInr, 4)), HttpStatusCode.Created)
            }
            else -> error(HttpStatusCode.NotFound, "No mock for $path")
        }
    }

    private fun HttpRequestData.bodyText(): String = (body as? TextContent)?.text.orEmpty()

    private fun MockRequestHandleScope.json(content: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(content, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun MockRequestHandleScope.error(status: HttpStatusCode, message: String) =
        json(AppJson.encodeToString(ErrorDto(message)), status)
}
