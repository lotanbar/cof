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
private const val ROUTE_QUIZ = "quiz/{scales}/{circle}/{major}/{minor}"

@Composable
fun CofNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_START) {

        composable(ROUTE_START) {
            StartScreen(
                onStartClicked = { scales, circle, major, minor ->
                    navController.navigate("quiz/$scales/$circle/$major/$minor")
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
            ),
        ) { backStackEntry ->
            val scales = backStackEntry.arguments?.getBoolean("scales") ?: false
            val circle = backStackEntry.arguments?.getBoolean("circle") ?: false
            val major  = backStackEntry.arguments?.getBoolean("major")  ?: false
            val minor  = backStackEntry.arguments?.getBoolean("minor")  ?: false

            QuizScreen(
                scalesSelected = scales,
                circleSelected = circle,
                majorSelected = major,
                minorSelected = minor,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
