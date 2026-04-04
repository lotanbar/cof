package com.example.cof.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cof.ui.viewmodel.CHROMATIC_NOTES
import com.example.cof.ui.viewmodel.CircleType
import com.example.cof.ui.viewmodel.QuizMode
import com.example.cof.ui.viewmodel.QuizViewModel
import com.example.cof.ui.viewmodel.ScaleType

// Strips Android's extra font padding so arrow characters sit truly centred
private val NoFontPadding = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))

@Composable
fun QuizScreen(
    scalesSelected: Boolean,
    circleSelected: Boolean,
    majorSelected: Boolean,
    minorSelected: Boolean,
    onBack: () -> Unit,
    viewModel: QuizViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init(scalesSelected, circleSelected, majorSelected, minorSelected)
    }

    val topLabel = when (uiState.mode) {
        QuizMode.CIRCLE -> when (uiState.circleType) {
            CircleType.FIFTHS  -> "Circle · Fifths"
            CircleType.FOURTHS -> "Circle · Fourths"
        }
        QuizMode.SCALES -> when (uiState.scaleType) {
            ScaleType.MAJOR -> "Scales · Major"
            ScaleType.MINOR -> "Scales · Minor"
            null            -> "Scales"
        }
    }

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
                        tint = Color.White,
                        modifier = Modifier.size(30.dp),
                    )
                }
                Text(
                    text = topLabel,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                // Invisible spacer balances the back-button so label is centred
                Spacer(modifier = Modifier.width(48.dp))
            }

            HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

            // ── MIDDLE (35%) ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(35f),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.mode == QuizMode.CIRCLE) {
                    // Arrow left/right of the note in a Row
                    val arrowText = if (uiState.circleType == CircleType.FIFTHS) "→" else "←"
                    val isLeft = uiState.circleType == CircleType.FOURTHS
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        if (isLeft) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color(0xFFCCCCCC),
                                modifier = Modifier.size(88.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = CHROMATIC_NOTES[uiState.rootNoteIndex],
                            fontSize = 100.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = NoFontPadding,
                        )
                        if (!isLeft) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFFCCCCCC),
                                modifier = Modifier.size(88.dp),
                            )
                        }
                    }
                } else {
                    Text(
                        text = CHROMATIC_NOTES[uiState.rootNoteIndex],
                        fontSize = 100.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = NoFontPadding,
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

            // ── NOTE BUTTONS (50%) — 3×4 grid ────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(50f)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Row 1: C  C#/Db  D        (indices 0–2)
                // Row 2: D#/Eb  E  F        (indices 3–5)
                // Row 3: F#/Gb  G  G#/Ab    (indices 6–8)
                // Row 4: A  A#/Bb  B        (indices 9–11)
                listOf(
                    CHROMATIC_NOTES.subList(0, 3),
                    CHROMATIC_NOTES.subList(3, 6),
                    CHROMATIC_NOTES.subList(6, 9),
                    CHROMATIC_NOTES.subList(9, 12),
                ).forEachIndexed { rowIndex, rowNotes ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        rowNotes.forEachIndexed { colIndex, note ->
                            val noteIndex = rowIndex * 3 + colIndex
                            val isScales = uiState.mode == QuizMode.SCALES
                            val orderIndex = if (isScales) uiState.selectedNotes.indexOf(noteIndex) else -1
                            val answerIndex = if (uiState.showingAnswer) uiState.answerNoteIndices.indexOf(noteIndex) else -1
                            NoteButton(
                                label = note,
                                selected = if (isScales) orderIndex >= 0 else uiState.selectedNoteIndex == noteIndex,
                                isAnswer = answerIndex >= 0,
                                orderNumber = when {
                                    answerIndex >= 0 && uiState.answerNoteIndices.size > 1 -> answerIndex + 1
                                    orderIndex >= 0 -> orderIndex + 1
                                    else -> null
                                },
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

            // ── SHOW ANSWER + SUBMIT (10%) ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(10f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Show Answer icon button
                IconButton(
                    onClick = { viewModel.showAnswer() },
                    enabled = !uiState.showWrong,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Show Answer",
                        tint = if (uiState.showingAnswer) Color(0xFFFFD700) else Color.White,
                    )
                }

                // Submit button — both modes
                Button(
                    onClick = { viewModel.submit() },
                    enabled = !uiState.showWrong && !uiState.showingAnswer && when (uiState.mode) {
                        QuizMode.SCALES -> true
                        QuizMode.CIRCLE -> uiState.selectedNoteIndex != null
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Wrong-answer flash overlay (at root BoxScope level to avoid ColumnScope clash)
        AnimatedVisibility(
            visible = uiState.showWrong,
            enter = fadeIn(),
            exit  = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x55FF0000)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Wrong",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5555),
                )
            }
        }
    }
}

@Composable
private fun NoteButton(
    label: String,
    selected: Boolean,
    isAnswer: Boolean,
    orderNumber: Int?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bgColor     = when {
        isAnswer   -> Color(0xFF3A2E00)
        selected   -> Color.White
        else       -> Color(0xFF000000)
    }
    val textColor   = when {
        isAnswer   -> Color(0xFFFFD700)
        selected   -> Color.Black
        else       -> Color.White
    }
    val borderColor = when {
        isAnswer   -> Color(0xFFFFD700)
        selected   -> Color.White
        else       -> Color(0xFF1E1E1E)
    }

    val displayLines = label.split('/')

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            displayLines.forEach { line ->
                Text(
                    text = line,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp,
                )
            }
        }
        if (orderNumber != null) {
            Text(
                text = orderNumber.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isAnswer -> Color(0xFFFFD700)
                    selected -> Color(0xFF555555)
                    else     -> Color(0xFF888888)
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(3.dp),
            )
        }
    }
}
