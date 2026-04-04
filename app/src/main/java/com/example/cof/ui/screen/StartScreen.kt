package com.example.cof.ui.screen

import androidx.compose.animation.*
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
    onStartClicked: (scalesSelected: Boolean, circleSelected: Boolean, majorSelected: Boolean, minorSelected: Boolean) -> Unit,
    viewModel: StartViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    StartScreenContent(
        uiState = uiState,
        onScalesToggle = viewModel::onScalesToggle,
        onCircleToggle = viewModel::onCircleToggle,
        onMajorToggle = viewModel::onMajorToggle,
        onMinorToggle = viewModel::onMinorToggle,
        onStartClicked = {
            onStartClicked(
                uiState.scalesSelected,
                uiState.circleSelected,
                uiState.majorSelected,
                uiState.minorSelected,
            )
        },
    )
}

@Composable
private fun StartScreenContent(
    uiState: StartUiState,
    onScalesToggle: (Boolean) -> Unit,
    onCircleToggle: (Boolean) -> Unit,
    onMajorToggle: (Boolean) -> Unit,
    onMinorToggle: (Boolean) -> Unit,
    onStartClicked: () -> Unit,
) {
    var bannerMessage by remember { mutableStateOf<String?>(null) }

    fun showBanner(msg: String) { bannerMessage = msg }

    fun showTypeToast()  = showBanner("Select Scales or Chords mode first")
    fun showStartToast() = showBanner(
        when {
            !uiState.scalesSelected && !uiState.circleSelected -> "Please select at least one mode"
            else -> "Please select Major, Minor, or both"
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            SectionLabel("Mode")
            Spacer(modifier = Modifier.height(12.dp))

            SelectionCard(label = "Scales", selected = uiState.scalesSelected, onClick = { onScalesToggle(!uiState.scalesSelected) })
            Spacer(modifier = Modifier.height(10.dp))
            SelectionCard(label = "Chords", selected = false, onClick = {}, disabled = true)
            Spacer(modifier = Modifier.height(10.dp))
            SelectionCard(label = "Circle", selected = uiState.circleSelected, onClick = { onCircleToggle(!uiState.circleSelected) })

            Spacer(modifier = Modifier.height(32.dp))

            SectionLabel("Type")
            Spacer(modifier = Modifier.height(12.dp))

            SelectionCard(
                label = "Major",
                selected = uiState.majorSelected && uiState.scalesSelected,
                onClick = { if (uiState.scalesSelected) onMajorToggle(!uiState.majorSelected) else showTypeToast() },
                dimmed = !uiState.scalesSelected,
            )
            Spacer(modifier = Modifier.height(10.dp))
            SelectionCard(
                label = "Minor",
                selected = uiState.minorSelected && uiState.scalesSelected,
                onClick = { if (uiState.scalesSelected) onMinorToggle(!uiState.minorSelected) else showTypeToast() },
                dimmed = !uiState.scalesSelected,
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(modifier = Modifier.fillMaxWidth().height(64.dp)) {
                Button(
                    onClick = onStartClicked,
                    enabled = uiState.isStartEnabled,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(text = "START", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                if (!uiState.isStartEnabled) {
                    Box(modifier = Modifier.fillMaxSize().clickable { showStartToast() })
                }
            }
        }

        TopBanner(
            message = bannerMessage,
            onDismiss = { bannerMessage = null },
        )
    }
}

@Composable
private fun TopBanner(message: String?, onDismiss: () -> Unit) {
    val visible = message != null

    LaunchedEffect(message) {
        if (message != null) {
            delay(2500)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit  = fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp)
            .padding(horizontal = 40.dp),
    ) {
        Box(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(24.dp))
                .background(Color(0xFF2A2A2A), RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = message ?: "",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SelectionCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    disabled: Boolean = false,
    dimmed: Boolean = false,
) {
    val backgroundColor = when {
        selected -> Color.White
        disabled -> Color(0xFF1C1C1C)
        dimmed   -> Color(0xFF0D0D0D)
        else     -> Color.Black
    }
    val textColor = when {
        selected -> Color.Black
        disabled -> Color(0xFF444444)
        dimmed   -> Color(0xFF555555)
        else     -> Color.White
    }
    val borderColor = when {
        selected -> Color.White
        disabled -> Color(0xFF333333)
        dimmed   -> Color(0xFF2A2A2A)
        else     -> Color(0xFF888888)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(72.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.5.dp, borderColor),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 28.sp, fontWeight = FontWeight.Medium, color = textColor)
        }
    }
}
