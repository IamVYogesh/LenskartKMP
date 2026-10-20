package com.yogesh.lenskart.feature.wishlist

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.yogesh.lenskart.core.designsystem.resources.*
import com.yogesh.lenskart.core.domain.model.Product
import com.yogesh.lenskart.core.domain.repository.WishlistRepository
import com.yogesh.lenskart.core.ui.EmptyState
import com.yogesh.lenskart.core.ui.BaseViewModel
import com.yogesh.lenskart.core.ui.ProductGrid
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

data class WishlistUiState(val products: List<Product> = emptyList(), val isLoading: Boolean = true)

interface WishlistActions {
    fun onWishlistToggled(productId: String)
}

class WishlistViewModel(private val wishlist: WishlistRepository) :
    BaseViewModel<WishlistUiState, Nothing>(WishlistUiState()), WishlistActions {

    init {
        viewModelScope.launch { wishlist.observeProducts().collect { updateState { copy(products = it, isLoading = false) } } }
    }

    override fun onWishlistToggled(productId: String) { viewModelScope.launch { wishlist.toggle(productId) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistRoute(onProductClick: (String) -> Unit, viewModel: WishlistViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(Res.string.wishlist_title), fontWeight = FontWeight.Bold) }) },
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        if (state.products.isEmpty() && !state.isLoading) {
            Box(Modifier.padding(padding).fillMaxSize()) { EmptyState(stringResource(Res.string.wishlist_empty)) }
        } else {
            ProductGrid(
                state.products, state.products.map { it.id }.toSet(), onProductClick,
                viewModel::onWishlistToggled, Modifier.padding(padding),
            )
        }
    }
}

val wishlistModule = module { viewModelOf(::WishlistViewModel) }
