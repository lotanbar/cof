package com.example.cof.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cof.ui.viewmodel.CHROMATIC_NOTES
import com.example.cof.ui.viewmodel.ChordType
import com.example.cof.ui.viewmodel.CircleDirection
import com.example.cof.ui.viewmodel.CircleType
import com.example.cof.ui.viewmodel.QuizMode
import com.example.cof.ui.viewmodel.QuizViewModel
import com.example.cof.ui.viewmodel.ScaleType

// Strips Android's extra font padding so arrow characters sit truly centred
private val NoFontPadding = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    scalesSelected: Boolean,
    circleSelected: Boolean,
    majorSelected: Boolean,
    minorSelected: Boolean,
    chordsSelected: Boolean = false,
    chordMaj3Selected: Boolean = false,
    chordMin3Selected: Boolean = false,
    chordMaj7Selected: Boolean = false,
    chordMin7Selected: Boolean = false,
    onBack: () -> Unit,
    viewModel: QuizViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init(
            scalesEnabled = scalesSelected,
            circleEnabled = circleSelected,
            majorEnabled = majorSelected,
            minorEnabled = minorSelected,
            chordsEnabled = chordsSelected,
            chordMaj3Enabled = chordMaj3Selected,
            chordMin3Enabled = chordMin3Selected,
            chordMaj7Enabled = chordMaj7Selected,
            chordMin7Enabled = chordMin7Selected,
        )
    }

    val topLabel = when (uiState.mode) {
        QuizMode.CIRCLE -> "Circle"
        QuizMode.SCALES -> "Scales"
        QuizMode.CHORDS -> "Chords"
    }

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {

            // ── TOP BAR (5%) ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(5f)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(30.dp),
                    )
                }
                Text(
                    text = topLabel,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                // Invisible spacer balances the back-button so label is centred
                Spacer(modifier = Modifier.width(48.dp))
            }

            HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

            // ── MIDDLE (42.5%) ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(42.5f),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.mode == QuizMode.CIRCLE) {
                    val intervalLabel = if (uiState.circleType == CircleType.FIFTHS) "5" else "4"
                    val isDown = uiState.circleDirection == CircleDirection.DOWN
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (!isDown) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(88.dp),
                                )
                                Text(
                                    text = intervalLabel,
                                    fontSize = 100.sp,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = NoFontPadding,
                                )
                            }
                        }
                        Text(
                            text = CHROMATIC_NOTES[uiState.rootNoteIndex],
                            fontSize = 100.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            style = NoFontPadding,
                        )
                        if (isDown) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(88.dp),
                                )
                                Text(
                                    text = intervalLabel,
                                    fontSize = 100.sp,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = NoFontPadding,
                                )
                            }
                        }
                    }
                } else if (uiState.mode == QuizMode.SCALES) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = CHROMATIC_NOTES[uiState.rootNoteIndex],
                            fontSize = 100.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            style = NoFontPadding,
                        )
                        Text(
                            text = when (uiState.scaleType) {
                                ScaleType.MAJOR -> "Major"
                                ScaleType.MINOR -> "Minor"
                                null            -> ""
                            },
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            style = NoFontPadding,
                        )
                    }
                } else {
                    // CHORDS
                    val chordType = uiState.chordType
                    if (chordType != null) {
                        val suffix = when (chordType) {
                            ChordType.MAJ_TRIAD -> "major"
                            ChordType.MIN_TRIAD -> "minor"
                            ChordType.MAJ_7TH   -> "major 7"
                            ChordType.MIN_7TH   -> "minor 7"
                        }
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = CHROMATIC_NOTES[uiState.rootNoteIndex],
                                fontSize = 100.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                style = NoFontPadding,
                            )
                            Text(
                                text = suffix,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                style = NoFontPadding,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

            // ── NOTE BUTTONS (42.5%) ──────────────────────────────────────
            // 4 rows of 3 notes each.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(42.5f)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val noteRows: List<List<Int>> = listOf(
                    listOf(0, 1, 2),
                    listOf(3, 4, 5),
                    listOf(6, 7, 8),
                    listOf(9, 10, 11),
                )

                noteRows.forEach { rowIndices ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        rowIndices.forEach { noteIndex ->
                            val note = CHROMATIC_NOTES[noteIndex]
                            val isScales = uiState.mode == QuizMode.SCALES
                            val isChords = uiState.mode == QuizMode.CHORDS
                            val chordType = uiState.chordType  // local val for smart-cast
                            val answerIndex = if (uiState.showingAnswer) uiState.answerNoteIndices.indexOf(noteIndex) else -1
                            val showAnswerNumbers = uiState.answerNoteIndices.size > 1
                            val isRootInAnswer = uiState.showingAnswer && isScales && showAnswerNumbers &&
                                    noteIndex == uiState.rootNoteIndex
                            val chordAnswerDegrees = listOf("1  8", "3", "5", "7")
                            val bottomLabel = when {
                                // Chords answer display: degree labels (1  8, 3, 5, 7)
                                isChords && uiState.showingAnswer && answerIndex >= 0 ->
                                    chordAnswerDegrees.getOrNull(answerIndex)
                                // Chords selection display: degree labels from user taps
                                isChords && !uiState.showingAnswer && chordType != null ->
                                    chordSelectionLabel(noteIndex, uiState.rootNoteIndex, chordType, uiState.selectedNotes)
                                // Scales answer: root shows "1  8", others show degree number
                                isRootInAnswer -> "1  8"
                                answerIndex >= 0 && showAnswerNumbers -> (answerIndex + 2).toString()
                                // Scales selection: tap order
                                isScales && uiState.selectedNotes.indexOf(noteIndex) >= 0 ->
                                    (uiState.selectedNotes.indexOf(noteIndex) + 1).toString()
                                else -> null
                            }
                            val isSelected = when {
                                isScales || isChords -> noteIndex in uiState.selectedNotes
                                else -> uiState.selectedNoteIndex == noteIndex
                            }
                            NoteButton(
                                label = note,
                                selected = isSelected,
                                isAnswer = answerIndex >= 0,
                                isAccidental = noteIndex in uiState.answerAccidentalNoteIndices,
                                bottomLabel = bottomLabel,
                                modifier = Modifier
                                    .weight(if (note.contains('/')) 1.5f else 1.0f)
                                    .fillMaxHeight(),
                                onClick = { viewModel.selectNote(noteIndex) },
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

            // ── SHOW ANSWER + CLEAR + SUBMIT (10%) ───────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(10f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Hint button — note-tile border style (1dp dim normally, gold when active)
                val hintEnabled = !uiState.showWrong
                OutlinedButton(
                    onClick = { viewModel.showAnswer() },
                    enabled = hintEnabled,
                    modifier = Modifier.fillMaxHeight().aspectRatio(1f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        if (uiState.showingAnswer) 2.dp else 1.dp,
                        when {
                            !hintEnabled -> Color(0xFF1E1E1E)
                            uiState.showingAnswer -> Color(0xFFFFD700)
                            else -> Color(0xFF1E1E1E)
                        }
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black,
                        contentColor = if (uiState.showingAnswer) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color(0xFF0A0A0A),
                        disabledContentColor = Color(0xFF444444),
                    ),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Show Answer",
                        modifier = Modifier.size(24.dp),
                    )
                }

                // Clear button — same note-tile border style
                val clearEnabled = !uiState.showWrong && !uiState.showingAnswer && when (uiState.mode) {
                    QuizMode.SCALES, QuizMode.CHORDS -> uiState.selectedNotes.isNotEmpty()
                    QuizMode.CIRCLE -> uiState.selectedNoteIndex != null
                }
                OutlinedButton(
                    onClick = { viewModel.clearSelection() },
                    enabled = clearEnabled,
                    modifier = Modifier.fillMaxHeight().aspectRatio(1f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (clearEnabled) Color(0xFF1E1E1E) else Color(0xFF1E1E1E)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color(0xFF0A0A0A),
                        disabledContentColor = Color(0xFF444444),
                    ),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear selection",
                        modifier = Modifier.size(24.dp),
                    )
                }

                // Submit button
                val submitEnabled = !uiState.showWrong && !uiState.showingAnswer && when (uiState.mode) {
                    QuizMode.SCALES, QuizMode.CHORDS -> true
                    QuizMode.CIRCLE -> uiState.selectedNoteIndex != null
                }
                OutlinedButton(
                    onClick = { viewModel.submit() },
                    enabled = submitEnabled,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(2.dp, if (submitEnabled) MaterialTheme.colorScheme.onSurface else Color(0xFF333333)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color(0xFF0A0A0A),
                        disabledContentColor = Color(0xFF444444),
                    ),
                ) {
                    Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Normal)
                }
            }
        }

        // Subtle wrong-answer indicator — small, centred, non-intrusive
        AnimatedVisibility(
            visible = uiState.showWrong,
            enter = fadeIn(),
            exit  = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Text(
                text = "✗  Wrong",
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFF3333),
            )
        }
    }
    } // CompositionLocalProvider
}

@Composable
private fun NoteButton(
    label: String,
    selected: Boolean,
    isAnswer: Boolean,
    isAccidental: Boolean = false,
    bottomLabel: String? = null,  // shown bottom-left (e.g. "2", "1  8", or selection order)
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bgColor = Color(0xFF000000)  // no fill for any hint tile
    val contentColor = when {
        isAnswer -> Color(0xFFFFD700)
        else     -> MaterialTheme.colorScheme.onSurface
    }
    val borderColor = when {
        isAnswer   -> Color(0xFFFFD700)
        selected   -> MaterialTheme.colorScheme.onSurface
        else       -> Color(0xFF1E1E1E)
    }
    val borderWidth = if (selected || isAnswer) 2.dp else 1.dp

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal,
            color = contentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center),
        )
        if (bottomLabel != null) {
            Text(
                text = bottomLabel,
                fontSize = if (isAnswer) 16.sp else 13.sp,
                fontWeight = FontWeight.Normal,
                color = contentColor,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 6.dp, bottom = 3.dp),
            )
        }
    }
}

/**
 * Returns the chord degree label for a note button in CHORDS selection mode.
 * Root shows "1" on first tap, "1  8" when tapped twice. Other chord tones show their degree (3/5/7).
 * Returns null if the note is not selected or not a chord tone.
 */
private fun chordSelectionLabel(
    noteIndex: Int,
    rootIndex: Int,
    chordType: ChordType,
    selectedNotes: List<Int>,
): String? {
    val intervals = QuizViewModel.CHORD_INTERVALS[chordType] ?: return null
    val interval = (noteIndex - rootIndex + 12) % 12
    val degreeIndex = intervals.indexOf(interval)
    if (degreeIndex < 0) return null  // note not in this chord
    val degrees = listOf(1, 3, 5, 7).take(intervals.size)
    val degree = degrees[degreeIndex]
    return if (degree == 1) {
        when (selectedNotes.count { it == noteIndex }) {
            0    -> null
            1    -> "1"
            else -> "1  8"
        }
    } else {
        if (noteIndex in selectedNotes) degree.toString() else null
    }
}
