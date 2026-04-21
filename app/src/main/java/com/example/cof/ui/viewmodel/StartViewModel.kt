package com.example.cof.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class StartUiState(
    val scalesSelected: Boolean = false,
    val chordsSelected: Boolean = false,
    val circleSelected: Boolean = false,
    val majorSelected: Boolean = false,
    val minorSelected: Boolean = false,
    val chordMaj3Selected: Boolean = false,
    val chordMin3Selected: Boolean = false,
    val chordMaj7Selected: Boolean = false,
    val chordMin7Selected: Boolean = false,
    val selectedNoteIndices: Set<Int> = (0..11).toSet(),
) {
    val anyChordTypeSelected get() =
        chordMaj3Selected || chordMin3Selected || chordMaj7Selected || chordMin7Selected

    val isStartEnabled: Boolean
        get() {
            if (!scalesSelected && !chordsSelected && !circleSelected) return false
            if (scalesSelected && !majorSelected && !minorSelected) return false
            if (chordsSelected && !anyChordTypeSelected) return false
            return true
        }
}

class StartViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StartUiState())
    val uiState: StateFlow<StartUiState> = _uiState.asStateFlow()

    fun onScalesToggle(checked: Boolean) {
        _uiState.update { it.copy(scalesSelected = checked) }
    }

    fun onChordsToggle(checked: Boolean) {
        _uiState.update { it.copy(chordsSelected = checked) }
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

    fun onChordMaj3Toggle(checked: Boolean) {
        _uiState.update { it.copy(chordMaj3Selected = checked) }
    }

    fun onChordMin3Toggle(checked: Boolean) {
        _uiState.update { it.copy(chordMin3Selected = checked) }
    }

    fun onChordMaj7Toggle(checked: Boolean) {
        _uiState.update { it.copy(chordMaj7Selected = checked) }
    }

    fun onChordMin7Toggle(checked: Boolean) {
        _uiState.update { it.copy(chordMin7Selected = checked) }
    }

    fun onNotesSelected(notes: Set<Int>) {
        _uiState.update { it.copy(selectedNoteIndices = notes) }
    }
}
