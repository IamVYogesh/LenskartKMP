package com.yogesh.lenskart.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.yogesh.lenskart.core.common.AppResult
import com.yogesh.lenskart.core.common.DispatcherProvider
import com.yogesh.lenskart.core.common.map
import com.yogesh.lenskart.core.data.mapper.toDomain
import com.yogesh.lenskart.core.data.mapper.toEntity
import com.yogesh.lenskart.core.database.LenskartDatabase
import com.yogesh.lenskart.core.domain.model.Banner
import com.yogesh.lenskart.core.domain.model.Product
import com.yogesh.lenskart.core.domain.model.ProductFilter
import com.yogesh.lenskart.core.domain.model.apply
import com.yogesh.lenskart.core.domain.repository.ProductRepository
import com.yogesh.lenskart.core.network.LenskartApi
import com.yogesh.lenskart.core.network.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/** Offline-first: UI observes SQLite; [refresh] pulls from the network and upserts. */
internal class ProductRepositoryImpl(
    private val api: LenskartApi,
    private val db: LenskartDatabase,
    private val dispatchers: DispatcherProvider,
) : ProductRepository {

    override fun observeProducts(filter: ProductFilter): Flow<List<Product>> =
        db.productQueries.selectAll().asFlow().mapToList(dispatchers.io)
            .map { rows -> filter.apply(rows.map { it.toDomain() }) }

    override fun observeProduct(id: String): Flow<Product?> =
        db.productQueries.selectById(id).asFlow().mapToOneOrNull(dispatchers.io).map { it?.toDomain() }

    override suspend fun refresh(): AppResult<Unit> =
        safeApiCall { api.getProducts() }.map { response ->
            val now = Clock.System.now().toEpochMilliseconds()
            withContext(dispatchers.io) {
                db.transaction { response.products.forEach { db.productQueries.insertOrReplace(it.toEntity(now)) } }
            }
        }

    override suspend fun getBanners(): AppResult<List<Banner>> =
        safeApiCall { api.getBanners() }.map { r -> r.banners.map { it.toDomain() } }
}
