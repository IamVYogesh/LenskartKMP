package com.yogesh.lenskart.feature.product

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val productModule = module {
    viewModel { params -> ProductViewModel(params.get<String>(), get(), get(), get(), get()) }
}
