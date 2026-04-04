package com.example.cof.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

val CHROMATIC_NOTES = listOf(
    "C", "C#/Db", "D", "D#/Eb", "E", "F",
    "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B",
)

enum class QuizMode { SCALES, CIRCLE }
enum class ScaleType { MAJOR, MINOR }
enum class CircleType { FOURTHS, FIFTHS }

data class QuizUiState(
    val mode: QuizMode = QuizMode.CIRCLE,
    val circleType: CircleType = CircleType.FIFTHS,
    val scaleType: ScaleType? = null,
    val rootNoteIndex: Int = 0,
    val selectedNoteIndex: Int? = null,         // Circle mode: single selection
    val selectedNotes: List<Int> = emptyList(),  // Scales mode: ordered sequence
    val showWrong: Boolean = false,
    val showingAnswer: Boolean = false,
    val answerNoteIndices: List<Int> = emptyList(), // populated when showingAnswer = true
)

class QuizViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var initialized = false
    private var scalesEnabled = false
    private var circleEnabled = true
    private var majorEnabled = false
    private var minorEnabled = false

    fun init(
        scalesEnabled: Boolean,
        circleEnabled: Boolean,
        majorEnabled: Boolean,
        minorEnabled: Boolean,
    ) {
        if (initialized) return
        initialized = true
        this.scalesEnabled = scalesEnabled
        this.circleEnabled = circleEnabled
        this.majorEnabled = majorEnabled
        this.minorEnabled = minorEnabled
        generateNextQuestion()
    }

    fun selectNote(noteIndex: Int) {
        if (_uiState.value.showWrong) return

        // Dismiss answer display on any tap
        if (_uiState.value.showingAnswer) {
            _uiState.update { it.copy(showingAnswer = false, answerNoteIndices = emptyList()) }
        }

        val state = _uiState.value
        if (state.mode == QuizMode.CIRCLE) {
            _uiState.update { it.copy(selectedNoteIndex = noteIndex) }
        } else {
            val current = state.selectedNotes
            when {
                current.isNotEmpty() && current.last() == noteIndex ->
                    // One-step undo: tapping the last selected note removes it
                    _uiState.update { it.copy(selectedNotes = current.dropLast(1)) }
                noteIndex !in current ->
                    _uiState.update { it.copy(selectedNotes = current + noteIndex) }
                // Tapping a non-last already-selected note → do nothing
            }
        }
    }

    fun showAnswer() {
        val state = _uiState.value
        if (state.showWrong) return
        if (state.showingAnswer) {
            _uiState.update { it.copy(showingAnswer = false, answerNoteIndices = emptyList()) }
            return
        }
        val answer: List<Int> = when (state.mode) {
            QuizMode.CIRCLE -> listOf(when (state.circleType) {
                CircleType.FIFTHS  -> (state.rootNoteIndex + 7) % 12
                CircleType.FOURTHS -> (state.rootNoteIndex + 5) % 12
            })
            QuizMode.SCALES -> correctScaleNotes(state.rootNoteIndex, state.scaleType ?: return)
        }
        _uiState.update { it.copy(
            showingAnswer = true,
            answerNoteIndices = answer,
            selectedNoteIndex = null,
            selectedNotes = emptyList(),
        ) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.showWrong) return

        when (state.mode) {
            QuizMode.CIRCLE -> {
                val selected = state.selectedNoteIndex ?: return
                val correct = when (state.circleType) {
                    CircleType.FIFTHS  -> (state.rootNoteIndex + 7) % 12
                    CircleType.FOURTHS -> (state.rootNoteIndex + 5) % 12
                }
                if (selected == correct) {
                    generateNextQuestion()
                } else {
                    showWrongThenClear()
                }
            }
            QuizMode.SCALES -> {
                val scaleType = state.scaleType ?: return
                val correct = correctScaleNotes(state.rootNoteIndex, scaleType)
                if (state.selectedNotes == correct) {
                    generateNextQuestion()
                } else {
                    showWrongThenClear()
                }
            }
        }
    }

    private fun showWrongThenClear() {
        _uiState.update { it.copy(showWrong = true, selectedNoteIndex = null, selectedNotes = emptyList()) }
        viewModelScope.launch {
            delay(1000)
            _uiState.update { it.copy(showWrong = false) }
        }
    }

    // Returns all 7 scale notes sorted in ascending cycle order:
    // starting from one semitone above root, wrapping back to root.
    private fun correctScaleNotes(rootIndex: Int, type: ScaleType): List<Int> {
        val intervals = if (type == ScaleType.MAJOR) MAJOR_INTERVALS else MINOR_INTERVALS
        val noteIndices = intervals.map { (rootIndex + it) % 12 }
        return noteIndices.sortedBy { (it - rootIndex - 1 + 12) % 12 }
    }

    private fun generateNextQuestion() {
        val mode = pickMode()
        val circleType = if (mode == QuizMode.CIRCLE) pickCircleType() else CircleType.FIFTHS
        val scaleType  = if (mode == QuizMode.SCALES)  pickScaleType()  else null
        val rootNoteIndex = Random.nextInt(12)
        _uiState.value = QuizUiState(
            mode = mode,
            circleType = circleType,
            scaleType = scaleType,
            rootNoteIndex = rootNoteIndex,
        )
    }

    private fun pickMode(): QuizMode = when {
        scalesEnabled && circleEnabled -> if (Random.nextBoolean()) QuizMode.SCALES else QuizMode.CIRCLE
        scalesEnabled -> QuizMode.SCALES
        else -> QuizMode.CIRCLE
    }

    private fun pickCircleType(): CircleType =
        if (Random.nextBoolean()) CircleType.FIFTHS else CircleType.FOURTHS

    private fun pickScaleType(): ScaleType = when {
        majorEnabled && minorEnabled -> if (Random.nextBoolean()) ScaleType.MAJOR else ScaleType.MINOR
        majorEnabled -> ScaleType.MAJOR
        else -> ScaleType.MINOR
    }

    companion object {
        private val MAJOR_INTERVALS = listOf(0, 2, 4, 5, 7, 9, 11)
        private val MINOR_INTERVALS = listOf(0, 2, 3, 5, 7, 8, 10)
    }
}
