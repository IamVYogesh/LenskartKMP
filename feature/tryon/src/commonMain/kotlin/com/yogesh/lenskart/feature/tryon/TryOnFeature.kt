package com.yogesh.lenskart.feature.tryon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.yogesh.lenskart.core.designsystem.LkColors
import com.yogesh.lenskart.core.designsystem.parseHexColor
import com.yogesh.lenskart.core.designsystem.resources.*
import com.yogesh.lenskart.core.domain.model.Category
import com.yogesh.lenskart.core.domain.model.FrameShape
import com.yogesh.lenskart.core.domain.model.Product
import com.yogesh.lenskart.core.domain.model.ProductFilter
import com.yogesh.lenskart.core.domain.repository.ProductRepository
import com.yogesh.lenskart.core.ui.BaseViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

data class TryOnUiState(val frames: List<Product> = emptyList(), val selectedId: String? = null) {
    val selected: Product? get() = frames.firstOrNull { it.id == selectedId }
}

interface TryOnActions {
    fun onFrameSelected(productId: String)
}

class TryOnViewModel(initialProductId: String, private val products: ProductRepository) :
    BaseViewModel<TryOnUiState, Nothing>(TryOnUiState(selectedId = initialProductId.ifBlank { null })), TryOnActions {

    init {
        viewModelScope.launch {
            val frames = products.observeProducts(ProductFilter()).first()
                .filter { it.category == Category.EYEGLASSES || it.category == Category.SUNGLASSES || it.category == Category.COMPUTER_GLASSES }
            updateState { copy(frames = frames, selectedId = selectedId ?: frames.firstOrNull()?.id) }
        }
    }

    override fun onFrameSelected(productId: String) = updateState { copy(selectedId = productId) }
}

@Composable
fun TryOnRoute(productId: String, onClose: () -> Unit, viewModel: TryOnViewModel = koinViewModel(key = "tryon-$productId") { parametersOf(productId) }) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        PlatformCameraPreview(Modifier.fillMaxSize())

        // Draggable / pinch-zoomable glasses overlay drawn with Canvas (no native code needed)
        state.selected?.let { p ->
            Box(
                Modifier.align(Alignment.Center).size(260.dp, 120.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 2.5f)
                            offset += pan
                        }
                    },
            ) { GlassesOverlay(p.shape, parseHexColor(p.colors.firstOrNull()?.hex ?: "#1C1C1E")) }
        }

        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            IconButton(onClose) { Icon(Icons.Default.Close, stringResource(Res.string.close), tint = Color.White) }
            Text(stringResource(Res.string.tryon_title), color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.size(48.dp))
        }

        Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.tryon_hint), color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.labelMedium)
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.frames, key = { it.id }) { p ->
                    FilterChip(
                        selected = p.id == state.selectedId, onClick = { viewModel.onFrameSelected(p.id) },
                        label = { Text(p.name) }, shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LkColors.Mint, selectedLabelColor = LkColors.Navy),
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassesOverlay(shape: FrameShape, color: Color) {
    Canvas(Modifier.fillMaxSize()) {
        val stroke = Stroke(width = 8.dp.toPx())
        val lensW = size.width * 0.40f
        val lensH = size.height * 0.75f
        val top = (size.height - lensH) / 2
        val left = Offset(size.width * 0.04f, top)
        val right = Offset(size.width * 0.56f, top)
        val lens = Size(lensW, lensH)
        val corner = when (shape) {
            FrameShape.ROUND -> CornerRadius(lensH / 2)
            FrameShape.RECTANGLE, FrameShape.WAYFARER -> CornerRadius(10.dp.toPx())
            FrameShape.CAT_EYE, FrameShape.AVIATOR -> CornerRadius(lensH / 3)
            else -> CornerRadius(14.dp.toPx())
        }
        drawRoundRect(color, left, lens, corner, stroke)
        drawRoundRect(color, right, lens, corner, stroke)
        drawLine(color, Offset(left.x + lensW, size.height / 2), Offset(right.x, size.height / 2), 8.dp.toPx())
        drawRoundRect(color.copy(alpha = 0.18f), left, lens, corner)
        drawRoundRect(color.copy(alpha = 0.18f), right, lens, corner)
    }
}

val tryOnModule = module { viewModel { params -> TryOnViewModel(params.get<String>(), get()) } }
