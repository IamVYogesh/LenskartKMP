package com.yogesh.lenskart.feature.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogesh.lenskart.core.designsystem.resources.*
import com.yogesh.lenskart.core.ui.EmptyState
import com.yogesh.lenskart.core.ui.ProductGrid
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchRoute(onBack: () -> Unit, onProductClick: (String) -> Unit, viewModel: SearchViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        state, viewModel, onBack,
        onProductClick = { id -> viewModel.onProductOpened(id); onProductClick(id) },
    )
}

@Composable
fun SearchScreen(state: SearchUiState, actions: SearchActions, onBack: () -> Unit, onProductClick: (String) -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    Scaffold(contentWindowInsets = WindowInsets.systemBars) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back)) }
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { actions.onQueryChanged(it) },
                    modifier = Modifier.weight(1f).focusRequester(focus),
                    placeholder = { Text(stringResource(Res.string.search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { actions.onSubmit(state.query) }),
                )
            }
            when {
                state.query.isBlank() -> RecentSearches(state.recent, actions)
                state.results.isEmpty() -> EmptyState(stringResource(Res.string.search_no_results, state.query))
                else -> ProductGrid(state.results, state.wishlistIds, onProductClick, { actions.onWishlistToggled(it) })
            }
        }
    }
}

@Composable
private fun RecentSearches(recent: List<String>, actions: SearchActions) {
    if (recent.isEmpty()) return
    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(stringResource(Res.string.search_recent), fontWeight = FontWeight.Bold)
            TextButton({ actions.onClearHistory() }) { Text(stringResource(Res.string.search_clear_history)) }
        }
        recent.forEach { q ->
            Text(q, Modifier.fillMaxWidth().clickable { actions.onQueryChanged(q) }.padding(vertical = 10.dp))
        }
    }
}
