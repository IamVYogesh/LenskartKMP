package com.yogesh.lenskart.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogesh.lenskart.core.designsystem.LkImage
import com.yogesh.lenskart.core.designsystem.SectionHeader
import com.yogesh.lenskart.core.designsystem.ShimmerBox
import com.yogesh.lenskart.core.designsystem.resources.*
import com.yogesh.lenskart.core.domain.model.Banner
import com.yogesh.lenskart.core.domain.model.Category
import com.yogesh.lenskart.core.ui.CollectUiEvents
import com.yogesh.lenskart.core.ui.ErrorState
import com.yogesh.lenskart.core.ui.OfflineBanner
import com.yogesh.lenskart.core.ui.ProductCard
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Route = stateful entry point (wires ViewModel). Screen = stateless, previewable & testable. */
@Composable
fun HomeRoute(
    onSearchClick: () -> Unit,
    onCategoryClick: (Category?) -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    CollectUiEvents(viewModel.uiEvents) { event ->
        if (event is HomeUiEvent.ShowMessage) snackbar.showSnackbar(event.text)
    }
    HomeScreen(state, snackbar, viewModel, onSearchClick, onCategoryClick, onProductClick)
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    snackbar: SnackbarHostState,
    actions: HomeActions,
    onSearchClick: () -> Unit,
    onCategoryClick: (Category?) -> Unit,
    onProductClick: (String) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            if (state.isOffline) item { OfflineBanner() }
            item { SearchBar(onSearchClick) }
            item {
                SectionHeader(stringResource(Res.string.home_shop_by_category))
                Spacer(Modifier.height(8.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(Category.entries) { CategoryTile(it) { onCategoryClick(it) } }
                }
            }
            item {
                when {
                    state.banners.isNotEmpty() -> BannerCarousel(state.banners, onCategoryClick)
                    state.isLoading -> ShimmerBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(150.dp).clip(RoundedCornerShape(16.dp)))
                }
            }
            if (state.error != null) item { ErrorState(state.error, { actions.onRefresh() }) }
            if (state.trending.isNotEmpty()) {
                item { ProductRow(stringResource(Res.string.home_trending), state, state.trending, actions, onProductClick, onSeeAll = { onCategoryClick(null) }) }
                item { ProductRow(stringResource(Res.string.home_new_arrivals), state, state.newArrivals, actions, onProductClick, onSeeAll = { onCategoryClick(null) }) }
            }
        }
    }
}

@Composable
private fun SearchBar(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(Res.string.home_search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CategoryTile(category: Category, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick).width(84.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), Alignment.Center) {
            Text(category.emoji, style = MaterialTheme.typography.headlineSmall)
        }
        Text(category.displayName, style = MaterialTheme.typography.labelSmall, maxLines = 2)
    }
}

@Composable
private fun BannerCarousel(banners: List<Banner>, onBannerClick: (Category?) -> Unit) {
    val pager = rememberPagerState(pageCount = { banners.size })
    LaunchedEffect(banners.size) {
        while (banners.size > 1) {
            delay(4_000)
            pager.animateScrollToPage((pager.currentPage + 1) % banners.size)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(pager, contentPadding = PaddingValues(horizontal = 16.dp), pageSpacing = 12.dp) { page ->
            val banner = banners[page]
            Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp)).clickable { onBannerClick(banner.category) }) {
                LkImage(banner.imageUrl, banner.title, Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xCC000042), Color.Transparent))))
                Column(Modifier.align(Alignment.CenterStart).padding(20.dp)) {
                    Text(banner.title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(banner.subtitle, color = Color.White.copy(alpha = 0.9f))
                }
            }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.Center) {
            repeat(banners.size) { i ->
                Box(
                    Modifier.padding(3.dp).size(if (i == pager.currentPage) 18.dp else 6.dp, 6.dp).clip(CircleShape)
                        .background(if (i == pager.currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                )
            }
        }
    }
}

@Composable
private fun ProductRow(
    title: String,
    state: HomeUiState,
    products: List<com.yogesh.lenskart.core.domain.model.Product>,
    actions: HomeActions,
    onProductClick: (String) -> Unit,
    onSeeAll: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title, actionText = stringResource(Res.string.see_all), onAction = onSeeAll)
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(products, key = { it.id }) { p ->
                ProductCard(
                    p, p.id in state.wishlistIds, { onProductClick(p.id) }, { actions.onWishlistToggled(p.id) },
                    Modifier.width(170.dp),
                )
            }
        }
    }
}
