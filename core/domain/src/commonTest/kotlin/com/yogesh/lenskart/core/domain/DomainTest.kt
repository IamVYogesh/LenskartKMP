package com.yogesh.lenskart.core.domain

import com.yogesh.lenskart.core.domain.model.*
import kotlin.test.Test
import kotlin.test.assertEquals

private fun product(id: String, price: Long, shape: FrameShape = FrameShape.ROUND, gender: Gender = Gender.UNISEX) =
    Product(id, "Frame $id", "Visora", Category.EYEGLASSES, shape, gender, price, price + 500, 4.0, 10, "", emptyList(), emptyList(), emptyMap())

class ProductFilterTest {
    private val all = listOf(product("a", 1000), product("b", 2000, FrameShape.AVIATOR), product("c", 1500, gender = Gender.MEN))

    @Test fun filtersByShape() =
        assertEquals(listOf("b"), ProductFilter(shapes = setOf(FrameShape.AVIATOR)).apply(all).map { it.id })

    @Test fun filtersByMaxPrice() =
        assertEquals(setOf("a", "c"), ProductFilter(maxPriceInr = 1500).apply(all).map { it.id }.toSet())

    @Test fun sortsByPriceAscending() =
        assertEquals(listOf("a", "c", "b"), ProductFilter(sort = SortOption.PRICE_LOW_TO_HIGH).apply(all).map { it.id })

    @Test fun searchesByName() =
        assertEquals(listOf("b"), ProductFilter(query = "frame b").apply(all).map { it.id })
}

class CartSummaryTest {
    private fun item(price: Long, qty: Int) = CartItem("p", "n", "b", "", "Black", price, price + 500, qty)

    @Test fun addsDeliveryFeeBelowThreshold() {
        val s = CartSummary(listOf(item(999, 1)))
        assertEquals(99L, s.deliveryFeeInr)
        assertEquals(1098L, s.totalInr)
    }

    @Test fun freeDeliveryAboveThreshold() = assertEquals(0L, CartSummary(listOf(item(800, 2))).deliveryFeeInr)

    @Test fun couponIsCappedAtMaxDiscount() {
        val s = CartSummary(listOf(item(5000, 1)), Coupon("BIG", 50, 500))
        assertEquals(500L, s.couponDiscountInr)
        assertEquals(4500L, s.totalInr)
    }
}
