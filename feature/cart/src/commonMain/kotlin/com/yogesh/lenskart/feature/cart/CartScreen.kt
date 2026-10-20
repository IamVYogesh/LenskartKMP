package com.yogesh.lenskart.feature.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogesh.lenskart.core.common.formatInr
import com.yogesh.lenskart.core.designsystem.LkButton
import com.yogesh.lenskart.core.designsystem.LkImage
import com.yogesh.lenskart.core.designsystem.resources.*
import com.yogesh.lenskart.core.domain.model.CartItem
import com.yogesh.lenskart.core.domain.model.CartSummary
import com.yogesh.lenskart.core.ui.CollectUiEvents
import com.yogesh.lenskart.core.ui.EmptyState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CartRoute(onLoginRequired: () -> Unit, viewModel: CartViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val signInText = stringResource(Res.string.cart_sign_in_required)
    CollectUiEvents(viewModel.uiEvents) { event ->
        when (event) {
            is CartUiEvent.Message -> snackbar.showSnackbar(event.text)
            CartUiEvent.RequireLogin -> { snackbar.showSnackbar(signInText); onLoginRequired() }
        }
    }
    CartScreen(state, snackbar, viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(state: CartUiState, snackbar: SnackbarHostState, actions: CartActions) {
    val summary = state.summary
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(Res.string.cart_title), fontWeight = FontWeight.Bold) }) },
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            if (summary.items.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(summary.totalInr.formatInr(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                stringResource(Res.string.cart_savings, summary.savingsInr.formatInr()),
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                        LkButton(stringResource(Res.string.cart_checkout), { actions.onPlaceOrder() }, Modifier.weight(1f), loading = state.isPlacingOrder)
                    }
                }
            }
        },
    ) { padding ->
        if (summary.items.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), Alignment.Center) { EmptyState(stringResource(Res.string.cart_empty)) }
        } else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(summary.items, key = { it.productId + it.colorName }) { CartRow(it, actions) }
                item { CouponBox(state, actions) }
                item { PriceDetails(summary) }
            }
        }
    }
    state.placedOrder?.let { order ->
        AlertDialog(
            onDismissRequest = { actions.onDismissOrderDialog() },
            title = { Text(stringResource(Res.string.cart_order_placed)) },
            text = { Text(stringResource(Res.string.cart_order_message, order.id, order.totalInr.formatInr(), order.etaDays)) },
            confirmButton = { TextButton({ actions.onDismissOrderDialog() }) { Text(stringResource(Res.string.cart_ok)) } },
        )
    }
}

@Composable
private fun CartRow(item: CartItem, actions: CartActions) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LkImage(item.imageUrl, item.name, Modifier.size(84.dp).clip(RoundedCornerShape(10.dp)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.brand, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(item.name, fontWeight = FontWeight.SemiBold)
                Text(item.colorName, style = MaterialTheme.typography.labelMedium)
                Text((item.unitPriceInr * item.quantity).formatInr(), fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton({ actions.onDecrement(item.productId, item.colorName) }) {
                    Icon(if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove, null)
                }
                Text("${item.quantity}", fontWeight = FontWeight.Bold)
                IconButton({ actions.onIncrement(item.productId, item.colorName) }) { Icon(Icons.Default.Add, null) }
            }
        }
    }
}

@Composable
private fun CouponBox(state: CartUiState, actions: CartActions) {
    val coupon = state.summary.coupon
    if (coupon != null) {
        AssistChip(
            onClick = { actions.onRemoveCoupon() },
            label = { Text(stringResource(Res.string.cart_coupon_applied, coupon.code)) },
        )
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            state.couponInput, { actions.onCouponChanged(it) }, Modifier.weight(1f),
            placeholder = { Text(stringResource(Res.string.cart_coupon_hint)) }, singleLine = true,
        )
        LkButton(stringResource(Res.string.cart_apply), { actions.onApplyCoupon() }, enabled = state.couponInput.isNotBlank(), loading = state.isApplyingCoupon)
    }
}

@Composable
private fun PriceDetails(s: CartSummary) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.cart_price_details), fontWeight = FontWeight.Bold)
            PriceLine(stringResource(Res.string.cart_subtotal, s.itemCount), s.subtotalInr.formatInr())
            if (s.couponDiscountInr > 0) PriceLine(stringResource(Res.string.cart_coupon_discount), "-" + s.couponDiscountInr.formatInr())
            PriceLine(stringResource(Res.string.cart_delivery), if (s.deliveryFeeInr == 0L) stringResource(Res.string.cart_free) else s.deliveryFeeInr.formatInr())
            HorizontalDivider()
            PriceLine(stringResource(Res.string.cart_total), s.totalInr.formatInr(), bold = true)
        }
    }
}

@Composable
private fun PriceLine(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
}
