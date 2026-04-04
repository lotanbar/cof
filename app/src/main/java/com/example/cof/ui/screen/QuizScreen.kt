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

            // ── MIDDLE (50%) ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(50f),
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
                            fontWeight = FontWeight.Bold,
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
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = NoFontPadding,
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

            // ── NOTE BUTTONS (35%) — 6×2 grid ────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(35f)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Row 1: C  C#/Db  D  D#/Eb  E  F  (indices 0–5)
                // Row 2: F#/Gb  G  G#/Ab  A  A#/Bb  B  (indices 6–11)
                listOf(
                    CHROMATIC_NOTES.subList(0, 6),
                    CHROMATIC_NOTES.subList(6, 12),
                ).forEachIndexed { rowIndex, rowNotes ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        rowNotes.forEachIndexed { colIndex, note ->
                            val noteIndex = rowIndex * 6 + colIndex
                            NoteButton(
                                label = note,
                                selected = uiState.selectedNoteIndex == noteIndex,
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

            // ── SUBMIT (10%) ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(10f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = { viewModel.submit() },
                    enabled = uiState.selectedNoteIndex != null && !uiState.showWrong,
                    modifier = Modifier.fillMaxSize(),
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bgColor     = if (selected) Color.White else Color(0xFF080808)
    val textColor   = if (selected) Color.Black else Color(0xFFDDDDDD)
    val borderColor = if (selected) Color.White else Color(0xFF1E1E1E)

    // Split "C#/Db" into two lines for display
    val displayLines = label.split('/')

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            displayLines.forEach { line ->
                Text(
                    text = line,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                )
            }
        }
    }
}
