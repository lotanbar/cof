package com.example.cof.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cof.ui.screen.NoteSelectionScreen
import com.example.cof.ui.screen.QuizScreen
import com.example.cof.ui.screen.StartScreen
import com.example.cof.ui.viewmodel.StartViewModel

private const val ROUTE_START = "start"
private const val ROUTE_NOTE_SELECTION = "note_selection"
private const val ROUTE_QUIZ = "quiz/{scales}/{circle}/{major}/{minor}/{chords}/{cMaj3}/{cMin3}/{cMaj7}/{cMin7}/{notesMask}"

@Composable
fun CofNavGraph() {
    val navController = rememberNavController()
    val startViewModel: StartViewModel = viewModel()

    NavHost(navController = navController, startDestination = ROUTE_START) {

        composable(ROUTE_START) {
            val uiState by startViewModel.uiState.collectAsState()
            StartScreen(
                onStartClicked = { scales, circle, major, minor, chords, cMaj3, cMin3, cMaj7, cMin7 ->
                    val mask = uiState.selectedNoteIndices.fold(0) { acc, i -> acc or (1 shl i) }
                    navController.navigate("quiz/$scales/$circle/$major/$minor/$chords/$cMaj3/$cMin3/$cMaj7/$cMin7/$mask")
                },
                onSelectNotesClicked = { navController.navigate(ROUTE_NOTE_SELECTION) },
                viewModel = startViewModel,
            )
        }

        composable(ROUTE_NOTE_SELECTION) {
            val uiState by startViewModel.uiState.collectAsState()
            NoteSelectionScreen(
                initialSelected = uiState.selectedNoteIndices,
                onDone = { notes ->
                    startViewModel.onNotesSelected(notes)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = ROUTE_QUIZ,
            arguments = listOf(
                navArgument("scales")    { type = NavType.BoolType },
                navArgument("circle")    { type = NavType.BoolType },
                navArgument("major")     { type = NavType.BoolType },
                navArgument("minor")     { type = NavType.BoolType },
                navArgument("chords")    { type = NavType.BoolType },
                navArgument("cMaj3")     { type = NavType.BoolType },
                navArgument("cMin3")     { type = NavType.BoolType },
                navArgument("cMaj7")     { type = NavType.BoolType },
                navArgument("cMin7")     { type = NavType.BoolType },
                navArgument("notesMask") { type = NavType.IntType; defaultValue = 0xFFF },
            ),
        ) { backStackEntry ->
            val uiState by startViewModel.uiState.collectAsState()
            val scales    = backStackEntry.arguments?.getBoolean("scales")    ?: false
            val circle    = backStackEntry.arguments?.getBoolean("circle")    ?: false
            val major     = backStackEntry.arguments?.getBoolean("major")     ?: false
            val minor     = backStackEntry.arguments?.getBoolean("minor")     ?: false
            val chords    = backStackEntry.arguments?.getBoolean("chords")    ?: false
            val cMaj3     = backStackEntry.arguments?.getBoolean("cMaj3")     ?: false
            val cMin3     = backStackEntry.arguments?.getBoolean("cMin3")     ?: false
            val cMaj7     = backStackEntry.arguments?.getBoolean("cMaj7")     ?: false
            val cMin7     = backStackEntry.arguments?.getBoolean("cMin7")     ?: false
            val notesMask = backStackEntry.arguments?.getInt("notesMask")     ?: 0xFFF
            val allowedNotes = (0..11).filter { notesMask and (1 shl it) != 0 }.toSet()

            QuizScreen(
                scalesSelected = scales,
                circleSelected = circle,
                majorSelected = major,
                minorSelected = minor,
                chordsSelected = chords,
                chordMaj3Selected = cMaj3,
                chordMin3Selected = cMin3,
                chordMaj7Selected = cMaj7,
                chordMin7Selected = cMin7,
                allowedNoteIndices = allowedNotes,
                soundEnabled = uiState.soundEnabled,
                onSoundToggle = startViewModel::onSoundToggle,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
