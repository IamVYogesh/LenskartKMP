package com.yogesh.lenskart.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yogesh.lenskart.core.designsystem.LkButton
import com.yogesh.lenskart.core.designsystem.LkColors
import com.yogesh.lenskart.core.designsystem.LkImage
import com.yogesh.lenskart.core.designsystem.PriceText
import com.yogesh.lenskart.core.designsystem.RatingBadge
import com.yogesh.lenskart.core.designsystem.ShimmerBox
import com.yogesh.lenskart.core.designsystem.resources.*
import com.yogesh.lenskart.core.domain.model.Product
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProductCard(
    product: Product,
    isWishlisted: Boolean,
    onClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box {
            LkImage(product.thumbnail, product.name, Modifier.fillMaxWidth().aspectRatio(1f))
            RatingBadge(product.rating, product.reviewCount, Modifier.align(Alignment.BottomStart).padding(8.dp))
            IconButton(
                onClick = onToggleWishlist,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(36.dp)
                    .clip(CircleShape),
            ) {
                Icon(
                    if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isWishlisted) LkColors.Danger else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(product.brand, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(product.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            PriceText(product.priceInr, product.mrpInr, product.discountPercent)
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LkButton(stringResource(Res.string.retry), onRetry)
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(48.dp), Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.tertiary) {
        Text(
            stringResource(Res.string.offline_banner),
            Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiary,
        )
    }
}

@Composable
fun ProductGridSkeleton(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
    ) {
        items((1..6).toList()) { ShimmerBox(Modifier.fillMaxWidth().aspectRatio(0.75f).clip(RoundedCornerShape(14.dp))) }
    }
}

/** Reusable 2-column product grid used by Catalog, Search and Wishlist. */
@Composable
fun ProductGrid(
    products: List<Product>,
    wishlistIds: Set<String>,
    onProductClick: (String) -> Unit,
    onToggleWishlist: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), modifier = modifier, contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(products, key = { it.id }) { p ->
            ProductCard(p, p.id in wishlistIds, { onProductClick(p.id) }, { onToggleWishlist(p.id) })
        }
    }
}
