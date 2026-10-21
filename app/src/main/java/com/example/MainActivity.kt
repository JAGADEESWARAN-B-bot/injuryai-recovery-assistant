package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AssessmentResultScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmergencyAlertScreen
import com.example.ui.screens.InjuryDetailScreen
import com.example.ui.screens.NewAssessmentScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecoveryTrackingScreen
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.InjuryViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: InjuryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundLight
                ) {
                    InjuryAIApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun InjuryAIApp(viewModel: InjuryViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToNewAssessment = {
                    navController.navigate("new_assessment")
                },
                onNavigateToDetail = { injuryId ->
                    viewModel.selectInjury(injuryId)
                    navController.navigate("injury_detail/$injuryId")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToEmergency = {
                    navController.navigate("emergency")
                }
            )
        }

        composable("new_assessment") {
            NewAssessmentScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAssessmentCompleted = { injuryId ->
                    navController.navigate("assessment_result/$injuryId") {
                        popUpTo("dashboard")
                    }
                },
                onNavigateToEmergency = {
                    navController.navigate("emergency")
                }
            )
        }

        composable(
            route = "assessment_result/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val injuryId = backStackEntry.arguments?.getString("id") ?: ""
            viewModel.selectInjury(injuryId)
            AssessmentResultScreen(
                injuryId = injuryId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRecoveryTracker = { id ->
                    navController.navigate("recovery_tracker/$id")
                },
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onNavigateToEmergency = {
                    navController.navigate("emergency")
                }
            )
        }

        composable(
            route = "injury_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val injuryId = backStackEntry.arguments?.getString("id") ?: ""
            viewModel.selectInjury(injuryId)
            InjuryDetailScreen(
                injuryId = injuryId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAssessmentResult = { id ->
                    navController.navigate("assessment_result/$id")
                },
                onNavigateToRecoveryTracker = { id ->
                    navController.navigate("recovery_tracker/$id")
                }
            )
        }

        composable(
            route = "recovery_tracker/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val injuryId = backStackEntry.arguments?.getString("id") ?: ""
            viewModel.selectInjury(injuryId)
            RecoveryTrackingScreen(
                injuryId = injuryId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogSubmitted = {
                    navController.popBackStack()
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("emergency") {
            EmergencyAlertScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
