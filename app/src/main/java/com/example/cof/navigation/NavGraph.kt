package com.example.cof.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cof.ui.screen.QuizScreen
import com.example.cof.ui.screen.StartScreen

private const val ROUTE_START = "start"
private const val ROUTE_QUIZ = "quiz/{scales}/{circle}/{major}/{minor}/{chords}/{cMaj3}/{cMin3}/{cMaj7}/{cMin7}"

@Composable
fun CofNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_START) {

        composable(ROUTE_START) {
            StartScreen(
                onStartClicked = { scales, circle, major, minor, chords, cMaj3, cMin3, cMaj7, cMin7 ->
                    navController.navigate("quiz/$scales/$circle/$major/$minor/$chords/$cMaj3/$cMin3/$cMaj7/$cMin7")
                }
            )
        }

        composable(
            route = ROUTE_QUIZ,
            arguments = listOf(
                navArgument("scales") { type = NavType.BoolType },
                navArgument("circle") { type = NavType.BoolType },
                navArgument("major")  { type = NavType.BoolType },
                navArgument("minor")  { type = NavType.BoolType },
                navArgument("chords") { type = NavType.BoolType },
                navArgument("cMaj3")  { type = NavType.BoolType },
                navArgument("cMin3")  { type = NavType.BoolType },
                navArgument("cMaj7")  { type = NavType.BoolType },
                navArgument("cMin7")  { type = NavType.BoolType },
            ),
        ) { backStackEntry ->
            val scales  = backStackEntry.arguments?.getBoolean("scales") ?: false
            val circle  = backStackEntry.arguments?.getBoolean("circle") ?: false
            val major   = backStackEntry.arguments?.getBoolean("major")  ?: false
            val minor   = backStackEntry.arguments?.getBoolean("minor")  ?: false
            val chords  = backStackEntry.arguments?.getBoolean("chords") ?: false
            val cMaj3   = backStackEntry.arguments?.getBoolean("cMaj3")  ?: false
            val cMin3   = backStackEntry.arguments?.getBoolean("cMin3")  ?: false
            val cMaj7   = backStackEntry.arguments?.getBoolean("cMaj7")  ?: false
            val cMin7   = backStackEntry.arguments?.getBoolean("cMin7")  ?: false

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
                onBack = { navController.popBackStack() },
            )
        }
    }
}
