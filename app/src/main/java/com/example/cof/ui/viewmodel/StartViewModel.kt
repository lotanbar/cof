package com.example.cof.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class StartUiState(
    val scalesSelected: Boolean = false,
    val circleSelected: Boolean = false,
    val majorSelected: Boolean = false,
    val minorSelected: Boolean = false,
) {
    val isStartEnabled: Boolean
        get() {
            if (!scalesSelected && !circleSelected) return false
            // Scales always requires at least one of Major/Minor, regardless of Circle
            if (scalesSelected && !majorSelected && !minorSelected) return false
            return true
        }
}

class StartViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StartUiState())
    val uiState: StateFlow<StartUiState> = _uiState.asStateFlow()

    fun onScalesToggle(checked: Boolean) {
        _uiState.update { it.copy(scalesSelected = checked) }
    }

    fun onCircleToggle(checked: Boolean) {
        _uiState.update { it.copy(circleSelected = checked) }
    }

    fun onMajorToggle(checked: Boolean) {
        _uiState.update { it.copy(majorSelected = checked) }
    }

    fun onMinorToggle(checked: Boolean) {
        _uiState.update { it.copy(minorSelected = checked) }
    }
}
