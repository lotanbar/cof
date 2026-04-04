package com.example.cof.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cof.ui.viewmodel.CHROMATIC_NOTES
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
    onBack: () -> Unit,
    viewModel: QuizViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init(scalesSelected, circleSelected, majorSelected, minorSelected)
    }

    val topLabel = when (uiState.mode) {
        QuizMode.CIRCLE -> "Circle"
        QuizMode.SCALES -> "Scales"
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
                        tint = Color(0xFFCCCCCC),
                        modifier = Modifier.size(30.dp),
                    )
                }
                Text(
                    text = topLabel,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFCCCCCC),
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
                                    tint = Color(0xFFCCCCCC),
                                    modifier = Modifier.size(88.dp),
                                )
                                Text(
                                    text = intervalLabel,
                                    fontSize = 100.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFFCCCCCC),
                                    style = NoFontPadding,
                                )
                            }
                        }
                        Text(
                            text = CHROMATIC_NOTES[uiState.rootNoteIndex],
                            fontSize = 100.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFCCCCCC),
                            textAlign = TextAlign.Center,
                            style = NoFontPadding,
                        )
                        if (isDown) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = Color(0xFFCCCCCC),
                                    modifier = Modifier.size(88.dp),
                                )
                                Text(
                                    text = intervalLabel,
                                    fontSize = 100.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFFCCCCCC),
                                    style = NoFontPadding,
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = CHROMATIC_NOTES[uiState.rootNoteIndex],
                            fontSize = 100.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFCCCCCC),
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
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFCCCCCC),
                            textAlign = TextAlign.Center,
                            style = NoFontPadding,
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

            // ── NOTE BUTTONS (35%) ────────────────────────────────────────
            // All 12 notes, ordered from one semitone above root up to root itself.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(35f)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val startNote = (uiState.rootNoteIndex + 1) % 12
                val noteRows: List<List<Int>> = (0 until 12)
                    .map { (startNote + it) % 12 }
                    .chunked(3)

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
                        tint = if (uiState.showingAnswer) Color(0xFFFFD700) else Color(0xFFCCCCCC),
                    )
                }

                // Submit button — both modes
                val submitEnabled = !uiState.showWrong && !uiState.showingAnswer && when (uiState.mode) {
                    QuizMode.SCALES -> true
                    QuizMode.CIRCLE -> uiState.selectedNoteIndex != null
                }
                OutlinedButton(
                    onClick = { viewModel.submit() },
                    enabled = submitEnabled,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(2.dp, if (submitEnabled) Color(0xFFCCCCCC) else Color(0xFF333333)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black,
                        contentColor = Color(0xFFCCCCCC),
                        disabledContainerColor = Color(0xFF0A0A0A),
                        disabledContentColor = Color(0xFF444444),
                    ),
                ) {
                    Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Normal)
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
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFFF5555),
                )
            }
        }
    }
    } // CompositionLocalProvider
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
        else       -> Color(0xFF000000)
    }
    val textColor   = when {
        isAnswer   -> Color(0xFFFFD700)
        else       -> Color(0xFFCCCCCC)
    }
    val borderColor = when {
        isAnswer   -> Color(0xFFFFD700)
        selected   -> Color(0xFFCCCCCC)
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
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center),
        )
        if (orderNumber != null) {
            Text(
                text = orderNumber.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = when {
                    isAnswer -> Color(0xFFFFD700)
                    selected -> Color(0xFF888888)
                    else     -> Color(0xFF888888)
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(3.dp),
            )
        }
    }
}
