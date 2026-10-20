package com.yogesh.lenskart.feature.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogesh.lenskart.core.designsystem.*
import com.yogesh.lenskart.core.designsystem.resources.*
import com.yogesh.lenskart.core.ui.CollectUiEvents
import com.yogesh.lenskart.core.ui.EmptyState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductRoute(
    productId: String,
    onBack: () -> Unit,
    onOpenCart: () -> Unit,
    onTryOn: (String) -> Unit,
    viewModel: ProductViewModel = koinViewModel(key = "product-$productId") { parametersOf(productId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val addedText = stringResource(Res.string.product_added)
    val viewCart = stringResource(Res.string.product_view_cart)
    CollectUiEvents(viewModel.uiEvents) { event ->
        if (event is ProductUiEvent.AddedToCart &&
            snackbar.showSnackbar(addedText, viewCart, duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed
        ) onOpenCart()
    }
    ProductScreen(state, snackbar, viewModel, onBack, onTryOn)
}

@Composable
fun ProductScreen(
    state: ProductUiState,
    snackbar: SnackbarHostState,
    actions: ProductActions,
    onBack: () -> Unit,
    onTryOn: (String) -> Unit,
) {
    val product = state.product
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (product != null) {
                Surface(shadowElevation = 8.dp) {
                    Row(Modifier.navigationBarsPadding().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton({ onTryOn(product.id) }, Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp)) {
                            Text(stringResource(Res.string.product_try_on))
                        }
                        LkButton(stringResource(Res.string.product_add_to_cart), { actions.onAddToCart() }, Modifier.weight(1.4f))
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> ShimmerBox(Modifier.fillMaxSize())
                product == null -> EmptyState(stringResource(Res.string.product_not_found))
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    item { ImagePager(product.images, product.name) }
                    item {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(product.brand, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            RatingBadge(product.rating, product.reviewCount)
                            PriceText(product.priceInr, product.mrpInr, product.discountPercent)
                            Text(stringResource(Res.string.product_colors), fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                product.colors.forEachIndexed { i, c ->
                                    val selected = i == state.selectedColorIndex
                                    Box(
                                        Modifier.size(36.dp).clip(CircleShape).background(parseHexColor(c.hex))
                                            .border(if (selected) 3.dp else 1.dp, if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline, CircleShape)
                                            .clickable { actions.onColorSelected(i) },
                                    )
                                }
                                Text(product.colors.getOrNull(state.selectedColorIndex)?.name.orEmpty(), style = MaterialTheme.typography.labelLarge)
                            }
                            HorizontalDivider()
                            Text(stringResource(Res.string.product_description), fontWeight = FontWeight.SemiBold)
                            Text(product.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(stringResource(Res.string.product_specs), fontWeight = FontWeight.SemiBold)
                            product.specs.forEach { (k, v) ->
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text(k, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(v, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
            // floating top actions
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp), Arrangement.SpaceBetween) {
                RoundIconButton({ onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back)) }
                if (product != null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoundIconButton({ actions.onShare() }) { Icon(Icons.Default.Share, null) }
                    RoundIconButton({ actions.onWishlistToggled() }) {
                        Icon(
                            if (state.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null,
                            tint = if (state.isWishlisted) LkColors.Danger else LocalContentColor.current,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(onClick = onClick, shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), shadowElevation = 2.dp) {
        Box(Modifier.size(40.dp), Alignment.Center) { content() }
    }
}

@Composable
private fun ImagePager(images: List<String>, name: String) {
    val pager = rememberPagerState(pageCount = { images.size })
    Box {
        HorizontalPager(pager) { page -> LkImage(images[page], name, Modifier.fillMaxWidth().aspectRatio(1f)) }
        Row(Modifier.align(Alignment.BottomCenter).padding(12.dp)) {
            repeat(images.size) { i ->
                Box(
                    Modifier.padding(3.dp).size(8.dp).clip(CircleShape)
                        .background(if (i == pager.currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                )
            }
        }
    }
}
