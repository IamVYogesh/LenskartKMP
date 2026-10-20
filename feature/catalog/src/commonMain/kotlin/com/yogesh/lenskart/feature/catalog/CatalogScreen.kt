package com.yogesh.lenskart.feature.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogesh.lenskart.core.common.formatInr
import com.yogesh.lenskart.core.designsystem.resources.*
import com.yogesh.lenskart.core.domain.model.*
import com.yogesh.lenskart.core.ui.EmptyState
import com.yogesh.lenskart.core.ui.ProductGrid
import com.yogesh.lenskart.core.ui.ProductGridSkeleton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CatalogRoute(
    initialCategory: String,
    onProductClick: (String) -> Unit,
    viewModel: CatalogViewModel = koinViewModel(key = "catalog-$initialCategory") { parametersOf(initialCategory) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CatalogScreen(state, viewModel, onProductClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(state: CatalogUiState, actions: CatalogActions, onProductClick: (String) -> Unit) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(Res.string.catalog_title), fontWeight = FontWeight.Bold) }) },
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(state.filter.category == null, { actions.onCategorySelected(null) }, { Text(stringResource(Res.string.catalog_all)) })
                }
                items(Category.entries) { c ->
                    FilterChip(state.filter.category == c, { actions.onCategorySelected(c) }, { Text(c.displayName) })
                }
            }
            ToolbarRow(state, actions)
            when {
                state.isLoading -> ProductGridSkeleton(Modifier.weight(1f))
                state.products.isEmpty() -> EmptyState(stringResource(Res.string.empty_products))
                else -> ProductGrid(
                    state.products, state.wishlistIds, onProductClick,
                    { actions.onWishlistToggled(it) }, Modifier.weight(1f),
                )
            }
        }
    }
    if (state.showFilters) FilterSheet(state.filter, actions)
}

@Composable
private fun ToolbarRow(state: CatalogUiState, actions: CatalogActions) {
    var sortMenu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(stringResource(Res.string.catalog_results, state.products.size), style = MaterialTheme.typography.labelLarge)
        Row {
            Box {
                TextButton({ sortMenu = true }) { Text("${stringResource(Res.string.catalog_sort)}: ${state.filter.sort.displayName}") }
                DropdownMenu(sortMenu, { sortMenu = false }) {
                    SortOption.entries.forEach { s ->
                        DropdownMenuItem({ Text(s.displayName) }, { actions.onSortSelected(s); sortMenu = false })
                    }
                }
            }
            val count = state.filter.activeFilterCount
            TextButton({ actions.onShowFilters() }) {
                Text(stringResource(Res.string.catalog_filters) + if (count > 0) " ($count)" else "")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterSheet(filter: ProductFilter, actions: CatalogActions) {
    ModalBottomSheet(onDismissRequest = { actions.onHideFilters() }) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(Res.string.filter_shape), fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FrameShape.entries.filter { it != FrameShape.NONE }.forEach { s ->
                    FilterChip(s in filter.shapes, { actions.onShapeToggled(s) }, { Text(s.displayName) })
                }
            }
            Text(stringResource(Res.string.filter_gender), fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Gender.entries.forEach { g ->
                    FilterChip(g in filter.genders, { actions.onGenderToggled(g) }, { Text(g.displayName) })
                }
            }
            val max = 3000f
            val cap = filter.maxPriceInr
            val current = cap?.toFloat() ?: max
            Text(
                stringResource(Res.string.filter_max_price) + ": " +
                    if (cap == null) stringResource(Res.string.filter_any_price) else cap.formatInr(),
                fontWeight = FontWeight.Bold,
            )
            Slider(
                value = current, valueRange = 500f..max, steps = 9,
                onValueChange = { v -> actions.onMaxPriceChanged(if (v >= max) null else v.toLong() / 100 * 100) },
            )
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                OutlinedButton({ actions.onClearFilters() }, Modifier.weight(1f)) { Text(stringResource(Res.string.filter_clear)) }
                Button({ actions.onHideFilters() }, Modifier.weight(1f)) { Text(stringResource(Res.string.filter_apply)) }
            }
        }
    }
}
