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
            // Auto-submit on single tap
            val correct = when (state.circleType) {
                CircleType.FIFTHS  -> (state.rootNoteIndex + 7) % 12
                CircleType.FOURTHS -> (state.rootNoteIndex + 5) % 12
            }
            if (noteIndex == correct) generateNextQuestion() else showWrongThenClear()
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
            QuizMode.SCALES -> correctAccidentals(state.rootNoteIndex, state.scaleType ?: return)
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
                val correct = correctAccidentals(state.rootNoteIndex, scaleType)
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

    private fun correctAccidentals(rootIndex: Int, type: ScaleType): List<Int> =
        KEY_ACCIDENTALS[Pair(rootIndex, type)] ?: emptyList()

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
        // Ordered accidentals (sharps or flats) for every key, mapped to chromatic indices.
        // Sharps order: F#(6) C#(1) G#(8) D#(3) A#(10) [E#=F(5)] [B#=C(0)]
        // Flats order:  Bb(10) Eb(3) Ab(8) Db(1) Gb(6)  [Cb=B(11)] [Fb=E(4)]
        private val KEY_ACCIDENTALS: Map<Pair<Int, ScaleType>, List<Int>> = mapOf(
            // ── Major keys ───────────────────────────────────────────────────
            Pair(0,  ScaleType.MAJOR) to emptyList(),                           // C  — none
            Pair(7,  ScaleType.MAJOR) to listOf(6),                             // G  — F#
            Pair(2,  ScaleType.MAJOR) to listOf(6, 1),                         // D  — F# C#
            Pair(9,  ScaleType.MAJOR) to listOf(6, 1, 8),                      // A  — F# C# G#
            Pair(4,  ScaleType.MAJOR) to listOf(6, 1, 8, 3),                   // E  — F# C# G# D#
            Pair(11, ScaleType.MAJOR) to listOf(6, 1, 8, 3, 10),               // B  — F# C# G# D# A#
            Pair(6,  ScaleType.MAJOR) to listOf(10, 3, 8, 1, 6, 11),          // Gb — Bb Eb Ab Db Gb Cb(B)
            Pair(1,  ScaleType.MAJOR) to listOf(10, 3, 8, 1, 6),               // Db — Bb Eb Ab Db Gb
            Pair(8,  ScaleType.MAJOR) to listOf(10, 3, 8, 1),                  // Ab — Bb Eb Ab Db
            Pair(3,  ScaleType.MAJOR) to listOf(10, 3, 8),                     // Eb — Bb Eb Ab
            Pair(10, ScaleType.MAJOR) to listOf(10, 3),                        // Bb — Bb Eb
            Pair(5,  ScaleType.MAJOR) to listOf(10),                           // F  — Bb
            // ── Minor keys ───────────────────────────────────────────────────
            Pair(9,  ScaleType.MINOR) to emptyList(),                          // Am — none
            Pair(4,  ScaleType.MINOR) to listOf(6),                            // Em — F#
            Pair(11, ScaleType.MINOR) to listOf(6, 1),                        // Bm — F# C#
            Pair(6,  ScaleType.MINOR) to listOf(6, 1, 8),                     // F#m— F# C# G#
            Pair(1,  ScaleType.MINOR) to listOf(6, 1, 8, 3),                  // C#m— F# C# G# D#
            Pair(8,  ScaleType.MINOR) to listOf(6, 1, 8, 3, 10),              // G#m— F# C# G# D# A#
            Pair(3,  ScaleType.MINOR) to listOf(10, 3, 8, 1, 6, 11),         // Ebm— Bb Eb Ab Db Gb Cb(B)
            Pair(10, ScaleType.MINOR) to listOf(10, 3, 8, 1, 6),              // Bbm— Bb Eb Ab Db Gb
            Pair(5,  ScaleType.MINOR) to listOf(10, 3, 8, 1),                 // Fm — Bb Eb Ab Db
            Pair(0,  ScaleType.MINOR) to listOf(10, 3, 8),                    // Cm — Bb Eb Ab
            Pair(7,  ScaleType.MINOR) to listOf(10, 3),                       // Gm — Bb Eb
            Pair(2,  ScaleType.MINOR) to listOf(10),                          // Dm — Bb
        )
    }
}
