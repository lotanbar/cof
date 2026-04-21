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

enum class QuizMode { SCALES, CIRCLE, CHORDS }
enum class ScaleType { MAJOR, MINOR }
enum class ChordType { MAJ_TRIAD, MIN_TRIAD, MAJ_7TH, MIN_7TH }
enum class CircleType { FOURTHS, FIFTHS }
enum class CircleDirection { UP, DOWN }

data class QuizUiState(
    val mode: QuizMode = QuizMode.CIRCLE,
    val circleType: CircleType = CircleType.FIFTHS,
    val circleDirection: CircleDirection = CircleDirection.UP,
    val scaleType: ScaleType? = null,
    val chordType: ChordType? = null,
    val rootNoteIndex: Int = 0,
    val selectedNoteIndex: Int? = null,                       // Circle mode: single selection
    val selectedNotes: List<Int> = emptyList(),               // Scales mode: ordered sequence
    val showWrong: Boolean = false,
    val showingAnswer: Boolean = false,
    val answerNoteIndices: List<Int> = emptyList(),           // populated when showingAnswer = true
    val answerAccidentalNoteIndices: Set<Int> = emptySet(),   // subset of answerNoteIndices that are accidentals
)

class QuizViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var initialized = false
    private var scalesEnabled = false
    private var circleEnabled = true
    private var majorEnabled = false
    private var minorEnabled = false
    private var chordsEnabled = false
    private var chordMaj3Enabled = false
    private var chordMin3Enabled = false
    private var chordMaj7Enabled = false
    private var chordMin7Enabled = false
    private var allowedNoteIndices: Set<Int> = (0..11).toSet()
    private var previousRootNoteIndex: Int? = null

    fun init(
        scalesEnabled: Boolean,
        circleEnabled: Boolean,
        majorEnabled: Boolean,
        minorEnabled: Boolean,
        chordsEnabled: Boolean = false,
        chordMaj3Enabled: Boolean = false,
        chordMin3Enabled: Boolean = false,
        chordMaj7Enabled: Boolean = false,
        chordMin7Enabled: Boolean = false,
        allowedNoteIndices: Set<Int> = (0..11).toSet(),
    ) {
        if (initialized) return
        initialized = true
        this.scalesEnabled = scalesEnabled
        this.circleEnabled = circleEnabled
        this.majorEnabled = majorEnabled
        this.minorEnabled = minorEnabled
        this.chordsEnabled = chordsEnabled
        this.chordMaj3Enabled = chordMaj3Enabled
        this.chordMin3Enabled = chordMin3Enabled
        this.chordMaj7Enabled = chordMaj7Enabled
        this.chordMin7Enabled = chordMin7Enabled
        this.allowedNoteIndices = allowedNoteIndices.ifEmpty { (0..11).toSet() }
        generateNextQuestion()
    }

    fun selectNote(noteIndex: Int) {
        if (_uiState.value.showWrong) return

        // Dismiss answer display on any tap
        if (_uiState.value.showingAnswer) {
            _uiState.update { it.copy(showingAnswer = false, answerNoteIndices = emptyList(), answerAccidentalNoteIndices = emptySet()) }
        }

        val state = _uiState.value
        if (state.mode == QuizMode.CIRCLE) {
            _uiState.update { it.copy(selectedNoteIndex = noteIndex) }
        } else if (state.mode == QuizMode.SCALES) {
            val current = state.selectedNotes
            if (noteIndex in current) {
                _uiState.update { it.copy(selectedNotes = current - noteIndex) }
            } else {
                _uiState.update { it.copy(selectedNotes = current + noteIndex) }
            }
        } else {
            // CHORDS: last-note undo; root may appear twice (degree 1 and 8)
            val current = state.selectedNotes
            if (current.isNotEmpty() && current.last() == noteIndex) {
                _uiState.update { it.copy(selectedNotes = current.dropLast(1)) }
            } else {
                val isRoot = noteIndex == state.rootNoteIndex
                val rootCount = current.count { it == state.rootNoteIndex }
                val alreadyInList = noteIndex in current
                if (!alreadyInList || (isRoot && rootCount < 2)) {
                    _uiState.update { it.copy(selectedNotes = current + noteIndex) }
                }
            }
        }
    }

    fun showAnswer() {
        val state = _uiState.value
        if (state.showWrong) return
        if (state.showingAnswer) {
            _uiState.update { it.copy(showingAnswer = false, answerNoteIndices = emptyList(), answerAccidentalNoteIndices = emptySet()) }
            return
        }
        when (state.mode) {
            QuizMode.CIRCLE -> {
                val answer = listOf(circleAnswer(state.rootNoteIndex, state.circleType, state.circleDirection))
                _uiState.update { it.copy(
                    showingAnswer = true,
                    answerNoteIndices = answer,
                    selectedNoteIndex = null,
                    selectedNotes = emptyList(),
                )}
            }
            QuizMode.SCALES -> {
                val scaleType = state.scaleType ?: return
                val scaleNotes = correctScaleNotes(state.rootNoteIndex, scaleType)
                val accidentalMap = if (scaleType == ScaleType.MAJOR) MAJOR_ACCIDENTALS else MINOR_ACCIDENTALS
                val accidentals = accidentalMap[state.rootNoteIndex] ?: emptySet()
                _uiState.update { it.copy(
                    showingAnswer = true,
                    answerNoteIndices = scaleNotes,
                    answerAccidentalNoteIndices = scaleNotes.filter { it in accidentals }.toSet(),
                    selectedNoteIndex = null,
                    selectedNotes = emptyList(),
                )}
            }
            QuizMode.CHORDS -> {
                val chordType = state.chordType ?: return
                val intervals = CHORD_INTERVALS[chordType]!!
                val chordNotes = intervals.map { (state.rootNoteIndex + it) % 12 }
                val accidentalSet = setOf(1, 3, 6, 8, 10)
                _uiState.update { it.copy(
                    showingAnswer = true,
                    answerNoteIndices = chordNotes,
                    answerAccidentalNoteIndices = chordNotes.filter { it in accidentalSet }.toSet(),
                    selectedNoteIndex = null,
                    selectedNotes = emptyList(),
                )}
            }
        }
    }

    fun clearSelection() {
        if (_uiState.value.showWrong) return
        _uiState.update { it.copy(selectedNotes = emptyList(), selectedNoteIndex = null) }
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
            QuizMode.CHORDS -> {
                val chordType = state.chordType ?: return
                if (isValidChordAnswer(state.rootNoteIndex, chordType, state.selectedNotes)) {
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
        val chordType       = if (mode == QuizMode.CHORDS) pickChordType()       else null
        val available = allowedNoteIndices.filter { it != previousRootNoteIndex }
        val pool = available.ifEmpty { allowedNoteIndices.toList() }
        val rootNoteIndex = pool.random()
        previousRootNoteIndex = rootNoteIndex
        _uiState.value = QuizUiState(
            mode = mode,
            circleType = circleType,
            circleDirection = circleDirection,
            scaleType = scaleType,
            chordType = chordType,
            rootNoteIndex = rootNoteIndex,
        )
    }

    private fun pickMode(): QuizMode {
        val modes = mutableListOf<QuizMode>().apply {
            if (scalesEnabled) add(QuizMode.SCALES)
            if (circleEnabled) add(QuizMode.CIRCLE)
            if (chordsEnabled) add(QuizMode.CHORDS)
        }
        return modes.random()
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

    private fun pickChordType(): ChordType {
        val available = mutableListOf<ChordType>().apply {
            if (chordMaj3Enabled) add(ChordType.MAJ_TRIAD)
            if (chordMin3Enabled) add(ChordType.MIN_TRIAD)
            if (chordMaj7Enabled) add(ChordType.MAJ_7TH)
            if (chordMin7Enabled) add(ChordType.MIN_7TH)
        }
        return available.random()
    }

    private fun isValidChordAnswer(rootIndex: Int, type: ChordType, selected: List<Int>): Boolean {
        val intervals = CHORD_INTERVALS[type]!!
        val n = intervals.map { (rootIndex + it) % 12 }
        val d1 = n[0]; val d3 = n[1]; val d5 = n[2]
        return if (type == ChordType.MAJ_TRIAD || type == ChordType.MIN_TRIAD) {
            selected == listOf(d1, d3, d5) ||
            selected == listOf(d1, d3, d5, d1) ||
            selected == listOf(d3, d5, d1)
        } else {
            val d7 = n[3]
            selected == listOf(d1, d3, d5, d7) ||
            selected == listOf(d1, d3, d5, d7, d1) ||
            selected == listOf(d3, d5, d7, d1)
        }
    }

    companion object {
        private val MAJOR_INTERVALS = listOf(0, 2, 4, 5, 7, 9, 11)
        private val MINOR_INTERVALS = listOf(0, 2, 3, 5, 7, 8, 10)

        val CHORD_INTERVALS = mapOf(
            ChordType.MAJ_TRIAD to listOf(0, 4, 7),
            ChordType.MIN_TRIAD to listOf(0, 3, 7),
            ChordType.MAJ_7TH   to listOf(0, 4, 7, 11),
            ChordType.MIN_7TH   to listOf(0, 3, 7, 10),
        )

        private val CHORD_ROOT_NAMES = listOf(
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
        )

        // Returns (rootName, typeSuffix) for display in the middle section.
        fun chordDisplayLabel(rootIndex: Int, chordType: ChordType): Pair<String, String> {
            val root = CHORD_ROOT_NAMES[rootIndex]
            val suffix = when (chordType) {
                ChordType.MAJ_TRIAD -> "maj"
                ChordType.MIN_TRIAD -> "min"
                ChordType.MAJ_7TH   -> "maj7"
                ChordType.MIN_7TH   -> "min7"
            }
            return Pair(root, suffix)
        }

        // For each root (0–11) the set of chromatic indices that are theoretically
        // accidentals (sharped or flatted notes) in the major/natural-minor scale.
        // Uses conventional enharmonic spellings (e.g. Db not C#, F# not Gb for root 6).
        // Notably: F# major includes E# (= index 5, displayed as F) as an accidental.
        private val MAJOR_ACCIDENTALS: Map<Int, Set<Int>> = mapOf(
            0  to emptySet(),
            1  to setOf(1, 3, 6, 8, 10),          // Db major
            2  to setOf(1, 6),                     // D major
            3  to setOf(3, 8, 10),                 // Eb major
            4  to setOf(1, 3, 6, 8),               // E major
            5  to setOf(10),                       // F major
            6  to setOf(1, 3, 5, 6, 8, 10),        // F# major (E#=5 is accidental)
            7  to setOf(6),                        // G major
            8  to setOf(1, 3, 8, 10),              // Ab major
            9  to setOf(1, 6, 8),                  // A major
            10 to setOf(3, 10),                    // Bb major
            11 to setOf(1, 3, 6, 8, 10),           // B major
        )

        // Natural minor (Aeolian) accidentals per root.
        // E.g. D# minor includes E# (=5) and D#(3): {1,3,5,6,8,10}.
        private val MINOR_ACCIDENTALS: Map<Int, Set<Int>> = mapOf(
            0  to setOf(3, 8, 10),                 // C minor
            1  to setOf(1, 3, 6, 8),               // C# minor
            2  to setOf(10),                       // D minor
            3  to setOf(1, 3, 5, 6, 8, 10),        // D# minor (E#=5 is accidental)
            4  to setOf(6),                        // E minor
            5  to setOf(1, 3, 8, 10),              // F minor
            6  to setOf(1, 6, 8),                  // F# minor
            7  to setOf(3, 10),                    // G minor
            8  to setOf(1, 3, 6, 8, 10),           // G# minor
            9  to emptySet(),                      // A minor
            10 to setOf(1, 3, 6, 8, 10),           // Bb minor
            11 to setOf(1, 6),                     // B minor
        )

        fun circleAnswer(rootIndex: Int, type: CircleType, direction: CircleDirection): Int {
            val interval = if (type == CircleType.FIFTHS) 7 else 5
            return if (direction == CircleDirection.UP)
                (rootIndex + interval) % 12
            else
                (rootIndex - interval + 12) % 12
        }
    }
}
