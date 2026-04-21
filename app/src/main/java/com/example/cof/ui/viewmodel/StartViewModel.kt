package com.example.cof.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val PREFS_NAME = "cof_prefs"

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
    val soundEnabled: Boolean = false,
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

class StartViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<StartUiState> = _uiState.asStateFlow()

    // ── Load / save helpers ───────────────────────────────────────────────

    private fun loadState(): StartUiState {
        val notesMask = prefs.getInt("notesMask", 0xFFF)
        val notes = (0..11).filter { notesMask and (1 shl it) != 0 }.toSet()
        return StartUiState(
            scalesSelected    = prefs.getBoolean("scalesSelected",    false),
            chordsSelected    = prefs.getBoolean("chordsSelected",    false),
            circleSelected    = prefs.getBoolean("circleSelected",    false),
            majorSelected     = prefs.getBoolean("majorSelected",     false),
            minorSelected     = prefs.getBoolean("minorSelected",     false),
            chordMaj3Selected = prefs.getBoolean("chordMaj3Selected", false),
            chordMin3Selected = prefs.getBoolean("chordMin3Selected", false),
            chordMaj7Selected = prefs.getBoolean("chordMaj7Selected", false),
            chordMin7Selected = prefs.getBoolean("chordMin7Selected", false),
            selectedNoteIndices = notes,
            soundEnabled      = prefs.getBoolean("soundEnabled",      false),
        )
    }

    private fun save(state: StartUiState) {
        val mask = state.selectedNoteIndices.fold(0) { acc, i -> acc or (1 shl i) }
        prefs.edit()
            .putBoolean("scalesSelected",    state.scalesSelected)
            .putBoolean("chordsSelected",    state.chordsSelected)
            .putBoolean("circleSelected",    state.circleSelected)
            .putBoolean("majorSelected",     state.majorSelected)
            .putBoolean("minorSelected",     state.minorSelected)
            .putBoolean("chordMaj3Selected", state.chordMaj3Selected)
            .putBoolean("chordMin3Selected", state.chordMin3Selected)
            .putBoolean("chordMaj7Selected", state.chordMaj7Selected)
            .putBoolean("chordMin7Selected", state.chordMin7Selected)
            .putInt("notesMask",             mask)
            .putBoolean("soundEnabled",      state.soundEnabled)
            .apply()
    }

    // ── Toggle handlers ───────────────────────────────────────────────────

    fun onScalesToggle(checked: Boolean) = update { it.copy(scalesSelected = checked) }
    fun onChordsToggle(checked: Boolean) = update { it.copy(chordsSelected = checked) }
    fun onCircleToggle(checked: Boolean) = update { it.copy(circleSelected = checked) }
    fun onMajorToggle(checked: Boolean)  = update { it.copy(majorSelected  = checked) }
    fun onMinorToggle(checked: Boolean)  = update { it.copy(minorSelected  = checked) }

    fun onChordMaj3Toggle(checked: Boolean) = update { it.copy(chordMaj3Selected = checked) }
    fun onChordMin3Toggle(checked: Boolean) = update { it.copy(chordMin3Selected = checked) }
    fun onChordMaj7Toggle(checked: Boolean) = update { it.copy(chordMaj7Selected = checked) }
    fun onChordMin7Toggle(checked: Boolean) = update { it.copy(chordMin7Selected = checked) }

    fun onNotesSelected(notes: Set<Int>) = update { it.copy(selectedNoteIndices = notes) }

    fun onSoundToggle() = update { it.copy(soundEnabled = !it.soundEnabled) }

    private fun update(transform: (StartUiState) -> StartUiState) {
        _uiState.update(transform)
        save(_uiState.value)
    }
}
