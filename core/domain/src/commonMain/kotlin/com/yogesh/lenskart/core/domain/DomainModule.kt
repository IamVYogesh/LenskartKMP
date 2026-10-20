package com.yogesh.lenskart.core.domain

import com.yogesh.lenskart.core.domain.usecase.ApplyCouponUseCase
import com.yogesh.lenskart.core.domain.usecase.PlaceOrderUseCase
import com.yogesh.lenskart.core.domain.usecase.SearchProductsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::SearchProductsUseCase)
    factoryOf(::ApplyCouponUseCase)
    factoryOf(::PlaceOrderUseCase)
}
