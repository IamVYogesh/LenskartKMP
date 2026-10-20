package com.yogesh.lenskart.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Shared MVVM base. androidx.lifecycle.ViewModel is multiplatform (Android, iOS, desktop).
 *
 *  - [uiState]  : observable, immutable snapshot of the screen (StateFlow). The View only renders it.
 *  - actions    : plain public functions on the concrete ViewModel (onXxx) that the View calls.
 *  - [uiEvents] : one-shot UI events (snackbar, navigation) delivered through a Channel so they are
 *                 never replayed on rotation / recomposition.
 *
 * The ViewModel talks only to the domain layer (use cases / repository interfaces), never to data,
 * network or database modules directly.
 */
abstract class BaseViewModel<UiState, UiEvent>(initialState: UiState) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEvents = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvents: Flow<UiEvent> = _uiEvents.receiveAsFlow()

    protected fun updateState(reduce: UiState.() -> UiState) = _uiState.update { it.reduce() }
    protected fun sendEvent(event: UiEvent) { viewModelScope.launch { _uiEvents.send(event) } }
}

@Composable
fun <T> CollectUiEvents(events: Flow<T>, onEvent: suspend (T) -> Unit) {
    val latest by rememberUpdatedState(onEvent)
    LaunchedEffect(events) { events.collect { latest(it) } }
}
