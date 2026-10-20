package com.yogesh.lenskart.feature.cart

import com.yogesh.lenskart.core.common.AppError
import com.yogesh.lenskart.core.common.AppResult
import com.yogesh.lenskart.core.domain.model.*
import com.yogesh.lenskart.core.domain.repository.AuthRepository
import com.yogesh.lenskart.core.domain.repository.CartRepository
import com.yogesh.lenskart.core.domain.usecase.ApplyCouponUseCase
import com.yogesh.lenskart.core.domain.usecase.PlaceOrderUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeCartRepository(items: List<CartItem>) : CartRepository {
    val cart = MutableStateFlow(CartSummary(items))
    override fun observeCart(): Flow<CartSummary> = cart
    override suspend fun addItem(productId: String, colorName: String) {}
    override suspend fun setQuantity(productId: String, colorName: String, quantity: Int) {
        cart.value = CartSummary(cart.value.items.mapNotNull { if (it.productId == productId) if (quantity > 0) it.copy(quantity = quantity) else null else it })
    }
    override suspend fun applyCoupon(code: String): AppResult<Coupon> = AppResult.Failure(AppError.Validation("nope"))
    override suspend fun removeCoupon() {}
    override suspend fun placeOrder(): AppResult<Order> = AppResult.Success(Order("LK1", cart.value.totalInr, cart.value.itemCount, 4))
}

private class FakeAuth(private val loggedIn: Boolean) : AuthRepository {
    override fun observeSession(): Flow<Session?> = MutableStateFlow(if (loggedIn) Session("t", User("1", null, "9999999999")) else null)
    override suspend fun requestOtp(phone: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun verifyOtp(phone: String, otp: String): AppResult<Session> = AppResult.Failure(AppError.Unknown())
    override suspend fun logout() {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {
    private val item = CartItem("p1", "Frame", "Visora", "", "Black", 1000, 1500, 1)

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun vm(loggedIn: Boolean = true): CartViewModel {
        val repo = FakeCartRepository(listOf(item))
        return CartViewModel(repo, ApplyCouponUseCase(repo), PlaceOrderUseCase(repo, FakeAuth(loggedIn)))
    }

    @Test fun incrementUpdatesQuantity() = runTest {
        val vm = vm()
        vm.onIncrement("p1", "Black")
        assertEquals(2, vm.uiState.value.summary.items.single().quantity)
    }

    @Test fun decrementToZeroRemovesItem() = runTest {
        val vm = vm()
        vm.onDecrement("p1", "Black")
        assertEquals(0, vm.uiState.value.summary.items.size)
    }

    @Test fun placingOrderWhenLoggedInShowsDialog() = runTest {
        val vm = vm(loggedIn = true)
        vm.onPlaceOrder()
        assertEquals("LK1", vm.uiState.value.placedOrder?.id)
    }

    @Test fun placingOrderLoggedOutRequestsLogin() = runTest {
        val vm = vm(loggedIn = false)
        vm.onPlaceOrder()
        assertEquals(null, vm.uiState.value.placedOrder)
    }
}
