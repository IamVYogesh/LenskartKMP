package com.yogesh.lenskart.feature.catalog

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val catalogModule = module {
    // ViewModel with a runtime parameter (navigation argument)
    viewModel { params -> CatalogViewModel(params.get<String>(), get(), get()) }
    viewModelOf(::SearchViewModel)
}
