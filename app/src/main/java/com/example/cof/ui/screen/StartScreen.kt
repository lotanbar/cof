package com.example.cof.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cof.ui.viewmodel.StartUiState
import com.example.cof.ui.viewmodel.StartViewModel
import kotlinx.coroutines.delay

@Composable
fun StartScreen(
    onStartClicked: (
        scalesSelected: Boolean,
        circleSelected: Boolean,
        majorSelected: Boolean,
        minorSelected: Boolean,
        chordsSelected: Boolean,
        chordMaj3: Boolean,
        chordMin3: Boolean,
        chordMaj7: Boolean,
        chordMin7: Boolean,
    ) -> Unit,
    viewModel: StartViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    StartScreenContent(
        uiState = uiState,
        onScalesToggle = viewModel::onScalesToggle,
        onChordsToggle = viewModel::onChordsToggle,
        onCircleToggle = viewModel::onCircleToggle,
        onMajorToggle = viewModel::onMajorToggle,
        onMinorToggle = viewModel::onMinorToggle,
        onChordMaj3Toggle = viewModel::onChordMaj3Toggle,
        onChordMin3Toggle = viewModel::onChordMin3Toggle,
        onChordMaj7Toggle = viewModel::onChordMaj7Toggle,
        onChordMin7Toggle = viewModel::onChordMin7Toggle,
        onStartClicked = {
            onStartClicked(
                uiState.scalesSelected,
                uiState.circleSelected,
                uiState.majorSelected,
                uiState.minorSelected,
                uiState.chordsSelected,
                uiState.chordMaj3Selected,
                uiState.chordMin3Selected,
                uiState.chordMaj7Selected,
                uiState.chordMin7Selected,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartScreenContent(
    uiState: StartUiState,
    onScalesToggle: (Boolean) -> Unit,
    onChordsToggle: (Boolean) -> Unit,
    onCircleToggle: (Boolean) -> Unit,
    onMajorToggle: (Boolean) -> Unit,
    onMinorToggle: (Boolean) -> Unit,
    onChordMaj3Toggle: (Boolean) -> Unit,
    onChordMin3Toggle: (Boolean) -> Unit,
    onChordMaj7Toggle: (Boolean) -> Unit,
    onChordMin7Toggle: (Boolean) -> Unit,
    onStartClicked: () -> Unit,
) {
    var bannerMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(bannerMessage) {
        if (bannerMessage != null) {
            delay(2500)
            bannerMessage = null
        }
    }

    fun showBanner(msg: String) { bannerMessage = msg }
    fun showTypeToast()  = showBanner("Select Scales mode first")
    fun showStartToast() = showBanner(
        when {
            !uiState.scalesSelected && !uiState.chordsSelected && !uiState.circleSelected ->
                "Please select at least one mode"
            uiState.scalesSelected && !uiState.majorSelected && !uiState.minorSelected ->
                "Scales requires Major or Minor"
            uiState.chordsSelected && !uiState.anyChordTypeSelected ->
                "Chords requires at least one type"
            else -> "Please check your selections"
        }
    )

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Mode buttons — centered in the available space above the START button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Scales + inline Major / Minor
                    SelectionCard(label = "Scales", selected = uiState.scalesSelected, onClick = { onScalesToggle(!uiState.scalesSelected) })
                    AnimatedVisibility(
                        visible = uiState.scalesSelected,
                        enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                        exit  = shrinkVertically(tween(200)) + fadeOut(tween(200)),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            SubTypeCard("Major", uiState.majorSelected, { onMajorToggle(!uiState.majorSelected) }, Modifier.weight(1f))
                            SubTypeCard("Minor", uiState.minorSelected, { onMinorToggle(!uiState.minorSelected) }, Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chords + inline chord sub-types
                    SelectionCard(label = "Chords", selected = uiState.chordsSelected, onClick = { onChordsToggle(!uiState.chordsSelected) })
                    AnimatedVisibility(
                        visible = uiState.chordsSelected,
                        enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                        exit  = shrinkVertically(tween(200)) + fadeOut(tween(200)),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            SubTypeCard("Maj",  uiState.chordMaj3Selected, { onChordMaj3Toggle(!uiState.chordMaj3Selected) }, Modifier.weight(1f))
                            SubTypeCard("Min",  uiState.chordMin3Selected, { onChordMin3Toggle(!uiState.chordMin3Selected) }, Modifier.weight(1f))
                            SubTypeCard("Maj7", uiState.chordMaj7Selected, { onChordMaj7Toggle(!uiState.chordMaj7Selected) }, Modifier.weight(1f))
                            SubTypeCard("Min7", uiState.chordMin7Selected, { onChordMin7Toggle(!uiState.chordMin7Selected) }, Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SelectionCard(label = "Circle", selected = uiState.circleSelected, onClick = { onCircleToggle(!uiState.circleSelected) })
                }
            }

            // Validation banner floats just above the Start button
            AnimatedVisibility(
                visible = bannerMessage != null,
                enter = fadeIn(),
                exit  = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = bannerMessage ?: "",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(72.dp)) {
                OutlinedButton(
                    onClick = onStartClicked,
                    enabled = uiState.isStartEnabled,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, if (uiState.isStartEnabled) MaterialTheme.colorScheme.onSurface else Color(0xFF333333)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color(0xFF0D0D0D),
                        disabledContentColor = Color(0xFF444444),
                    ),
                ) {
                    Text(text = "START", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                }
                if (!uiState.isStartEnabled) {
                    Box(modifier = Modifier.fillMaxSize().clickable { showStartToast() })
                }
            }
        }
    } // CompositionLocalProvider
}

@Composable
private fun SubTypeCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color.Black,
        border = BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) Color(0xFFFFFFFF) else Color(0xFF666666),
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 17.sp, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SelectionCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    disabled: Boolean = false,
) {
    val backgroundColor = if (disabled) Color(0xFF1C1C1C) else Color.Black
    val textColor = if (disabled) Color(0xFF444444) else MaterialTheme.colorScheme.onSurface
    val borderColor = when {
        selected -> Color(0xFFFFFFFF)
        disabled -> Color(0xFF333333)
        else     -> Color(0xFF666666)
    }
    val borderWidth = if (selected) 1.5.dp else 1.dp

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(72.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 28.sp, fontWeight = FontWeight.Normal, color = textColor)
        }
    }
}
