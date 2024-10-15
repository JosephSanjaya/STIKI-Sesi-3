package com.joseph.praktisi.utils

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class BaseViewModel<State, Event>(
    defaultState: State
) : ViewModel(), OnEventReceiver<Event> {

    protected val _uiState = MutableStateFlow(defaultState)
    val uiState = _uiState
        .onStart { onUiStateSubscribed() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DEFAULT_SUBSCRIBED_TIME),
            defaultState
        )

    private val _uiEvent = MutableSharedFlow<Event>()
    val uiEvent = _uiEvent
        .onEach(::onEventSideEffect)
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    protected open fun onUiStateSubscribed() = Unit

    override fun onEvent(event: Event) {
        viewModelScope.launch(Dispatchers.Main.immediate) { _uiEvent.emit(event) }
    }

    open fun onEventSideEffect(event: Event) {
        Log.d(TAG, "Received event: $event")
    }

    companion object {
        private const val DEFAULT_SUBSCRIBED_TIME = 5_000L
        const val TAG = "QR-ViewModel"
    }
}
