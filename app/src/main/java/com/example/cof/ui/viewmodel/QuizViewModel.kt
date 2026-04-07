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
enum class CircleDirection { UP, DOWN }

data class QuizUiState(
    val mode: QuizMode = QuizMode.CIRCLE,
    val circleType: CircleType = CircleType.FIFTHS,
    val circleDirection: CircleDirection = CircleDirection.UP,
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
    private var previousRootNoteIndex: Int? = null

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
            if (noteIndex in current) {
                _uiState.update { it.copy(selectedNotes = current - noteIndex) }
            } else {
                _uiState.update { it.copy(selectedNotes = current + noteIndex) }
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
            QuizMode.CIRCLE -> listOf(circleAnswer(state.rootNoteIndex, state.circleType, state.circleDirection))
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
                val correct = circleAnswer(state.rootNoteIndex, state.circleType, state.circleDirection)
                if (selected == correct) {
                    generateNextQuestion()
                } else {
                    showWrong()
                }
            }
            QuizMode.SCALES -> {
                val scaleType = state.scaleType ?: return
                if (isValidScaleAnswer(state.rootNoteIndex, scaleType, state.selectedNotes)) {
                    generateNextQuestion()
                } else {
                    showWrong()
                }
            }
        }
    }

    private fun showWrong() {
        _uiState.update { it.copy(showWrong = true) }
        viewModelScope.launch {
            delay(1000)
            _uiState.update { it.copy(showWrong = false) }
        }
    }

    // Returns the canonical answer (notes 2-8) used by Show Answer.
    private fun correctScaleNotes(rootIndex: Int, type: ScaleType): List<Int> {
        val intervals = if (type == ScaleType.MAJOR) MAJOR_INTERVALS else MINOR_INTERVALS
        val noteIndices = intervals.map { (rootIndex + it) % 12 }
        val sorted = noteIndices.sortedBy { (it - rootIndex - 1 + 12) % 12 }
        return sorted
    }

    // Accepts any of the three valid scale-answer forms:
    //   a) Notes 2-8  : [n1..n6, root]
    //   b) Notes 1-7  : [n0..n6]
    //   c) Accidentals: sharps/flats from the scale in ascending scale-degree order
    //      (empty list valid when scale has no accidentals, e.g. C major)
    private fun isValidScaleAnswer(rootIndex: Int, type: ScaleType, selected: List<Int>): Boolean {
        val intervals = if (type == ScaleType.MAJOR) MAJOR_INTERVALS else MINOR_INTERVALS
        val n = intervals.map { (rootIndex + it) % 12 }   // scale notes in degree order

        val form2to8 = n.drop(1) + n[0]
        val form1to7 = n

        val accidentalSet = setOf(1, 3, 6, 8, 10)
        val accidentals = n.filter { it in accidentalSet }

        return selected == form2to8 ||
               selected == form1to7 ||
               selected == accidentals
    }

    private fun generateNextQuestion() {
        val mode = pickMode()
        val circleType      = if (mode == QuizMode.CIRCLE) pickCircleType()      else CircleType.FIFTHS
        val circleDirection = if (mode == QuizMode.CIRCLE) pickCircleDirection() else CircleDirection.UP
        val scaleType       = if (mode == QuizMode.SCALES) pickScaleType()       else null
        val available = (0 until 12).filter { it != previousRootNoteIndex }
        val rootNoteIndex = available.random()
        previousRootNoteIndex = rootNoteIndex
        _uiState.value = QuizUiState(
            mode = mode,
            circleType = circleType,
            circleDirection = circleDirection,
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

    private fun pickCircleDirection(): CircleDirection =
        if (Random.nextBoolean()) CircleDirection.UP else CircleDirection.DOWN

    private fun pickScaleType(): ScaleType = when {
        majorEnabled && minorEnabled -> if (Random.nextBoolean()) ScaleType.MAJOR else ScaleType.MINOR
        majorEnabled -> ScaleType.MAJOR
        else -> ScaleType.MINOR
    }

    companion object {
        private val MAJOR_INTERVALS = listOf(0, 2, 4, 5, 7, 9, 11)
        private val MINOR_INTERVALS = listOf(0, 2, 3, 5, 7, 8, 10)

        fun circleAnswer(rootIndex: Int, type: CircleType, direction: CircleDirection): Int {
            val interval = if (type == CircleType.FIFTHS) 7 else 5
            return if (direction == CircleDirection.UP)
                (rootIndex + interval) % 12
            else
                (rootIndex - interval + 12) % 12
        }
    }
}
