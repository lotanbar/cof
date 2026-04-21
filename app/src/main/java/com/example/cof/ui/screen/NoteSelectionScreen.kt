package com.example.cof.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cof.ui.viewmodel.CHROMATIC_NOTES

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSelectionScreen(
    initialSelected: Set<Int>,
    onDone: (Set<Int>) -> Unit,
    onBack: () -> Unit,
) {
    var selected by remember { mutableStateOf(initialSelected) }

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {
            // ── TOP BAR ──────────────────────────────────────────────────
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
                    text = "Select Notes",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

            // Centre the grid at the same height proportion as QuizScreen (42.5% of screen)
            Spacer(modifier = Modifier.weight(21.25f))

            // ── NOTE GRID ────────────────────────────────────────────────
            val noteRows: List<List<Int>> = listOf(
                listOf(0, 1, 2),
                listOf(3, 4, 5),
                listOf(6, 7, 8),
                listOf(9, 10, 11),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(42.5f)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                noteRows.forEach { rowIndices ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        rowIndices.forEach { noteIndex ->
                            val note = CHROMATIC_NOTES[noteIndex]
                            val isSelected = noteIndex in selected
                            SelectableNoteButton(
                                label = note,
                                selected = isSelected,
                                modifier = Modifier
                                    .weight(if (note.contains('/')) 1.5f else 1.0f)
                                    .fillMaxHeight(),
                                onClick = {
                                    selected = if (isSelected) selected - noteIndex else selected + noteIndex
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(21.25f))

            HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

            // ── SELECT BUTTON ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(10f)
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            ) {
                OutlinedButton(
                    onClick = { onDone(selected) },
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        2.dp,
                        if (selected.isNotEmpty()) MaterialTheme.colorScheme.onSurface else Color(0xFF333333),
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color(0xFF0D0D0D),
                        disabledContentColor = Color(0xFF444444),
                    ),
                ) {
                    Text("SELECT", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SelectableNoteButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.onSurface else Color(0xFF1E1E1E)
    val borderWidth = if (selected) 2.dp else 1.dp

    Box(
        modifier = modifier
            .background(Color.Black, RoundedCornerShape(6.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}
