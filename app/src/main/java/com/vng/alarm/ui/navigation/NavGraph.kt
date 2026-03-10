package com.vng.alarm.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vng.alarm.ui.screens.add_edit_alarm.AddEditAlarmScreen
import com.vng.alarm.ui.screens.home.HomeScreen
import com.vng.alarm.ui.screens.ringtone_picker.RingtonePickerScreen
import com.vng.alarm.ui.screens.settings.SettingsScreen
import com.vng.alarm.ui.screens.splash.SplashScreen

@Stable
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object AddEditAlarm : Screen("add_edit_alarm/{alarmId}") {
        fun passAlarmId(alarmId: Int = -1): String = "add_edit_alarm/$alarmId"
    }
    object RingtonePicker : Screen("ringtone_picker")
    object Settings : Screen("settings")
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                animationSpec = tween(300)
            ) { it }
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                animationSpec = tween(300)
            ) { it }
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                animationSpec = tween(300)
            ) { -it }
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                animationSpec = tween(300)
            ) { -it }
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.popBackStack()
                    navController.navigate(Screen.Home.route)
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddAlarm = {
                    navController.navigate(Screen.AddEditAlarm.passAlarmId())
                },
                onNavigateToEditAlarm = { alarmId ->
                    navController.navigate(Screen.AddEditAlarm.passAlarmId(alarmId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.AddEditAlarm.route,
            arguments = listOf(navArgument("alarmId") { defaultValue = -1 })
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getInt("alarmId") ?: -1
            AddEditAlarmScreen(
                alarmId = if (alarmId == -1) null else alarmId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRingtonePicker = {
                    navController.navigate(Screen.RingtonePicker.route)
                }
            )
        }

        composable(Screen.RingtonePicker.route) {
            RingtonePickerScreen(
                onRingtoneSelected = { uri ->
                    // Handle ringtone selection
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_ringtone", uri)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}