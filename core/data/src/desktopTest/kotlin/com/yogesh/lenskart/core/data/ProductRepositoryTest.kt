package com.yogesh.lenskart.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.yogesh.lenskart.core.common.AppResult
import com.yogesh.lenskart.core.common.DispatcherProvider
import com.yogesh.lenskart.core.data.repository.ProductRepositoryImpl
import com.yogesh.lenskart.core.database.LenskartDatabase
import com.yogesh.lenskart.core.domain.model.Banner
import com.yogesh.lenskart.core.domain.model.Category
import com.yogesh.lenskart.core.domain.model.ProductFilter
import com.yogesh.lenskart.core.network.LenskartApi
import com.yogesh.lenskart.core.network.createHttpClient
import com.yogesh.lenskart.core.network.mock.MockLenskartBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Integration test: real Ktor pipeline (against MockEngine) + real SQLite (in-memory JDBC). */
class ProductRepositoryTest {
    private val testDispatchers = object : DispatcherProvider {
        override val main = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
    }

    private fun repo(): ProductRepositoryImpl {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { LenskartDatabase.Schema.create(it) }
        val client = createHttpClient(MockLenskartBackend.engine(0), "https://mock.lenskart.test/", { null }, false)
        return ProductRepositoryImpl(LenskartApi(client), LenskartDatabase(driver), testDispatchers)
    }

    @Test
    fun refreshPopulatesLocalCache() = runTest {
        val repository = repo()
        repository.observeProducts().test {
            assertTrue(awaitItem().isEmpty())
            assertIs<AppResult.Success<Unit>>(repository.refresh())
            assertTrue(awaitItem().isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun filtersByCategoryFromCache() = runTest {
        val repository = repo()
        repository.refresh()
        repository.observeProducts(ProductFilter(category = Category.SUNGLASSES)).test {
            assertTrue(awaitItem().all { it.category == Category.SUNGLASSES })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun bannersComeFromNetwork() = runTest {
        val result = repo().getBanners()
        assertIs<AppResult.Success<List<Banner>>>(result)
        assertEquals(3, result.data.size)
    }
}
