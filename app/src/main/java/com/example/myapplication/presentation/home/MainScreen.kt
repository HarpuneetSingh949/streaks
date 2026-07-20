package com.example.myapplication.presentation.home

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.presentation.calendar.CalendarScreen
import com.example.myapplication.presentation.settings.SettingsScreen
import com.example.myapplication.presentation.stats.StatsScreen

@Composable
fun MainScreen(
    onNavigateToAddHabit: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Habits") },
                    label = { Text("Habits") },
                    selected = currentRoute == "habits",
                    onClick = {
                        if (currentRoute != "habits") {
                            bottomNavController.navigate("habits") {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Info, contentDescription = "Statistics") },
                    label = { Text("Statistics") },
                    selected = currentRoute == "stats",
                    onClick = {
                        if (currentRoute != "stats") {
                            bottomNavController.navigate("stats") {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = "Calendar") },
                    label = { Text("Calendar") },
                    selected = currentRoute == "calendar",
                    onClick = {
                        if (currentRoute != "calendar") {
                            bottomNavController.navigate("calendar") {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = {
                        if (currentRoute != "settings") {
                            bottomNavController.navigate("settings") {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = bottomNavController, 
                startDestination = "habits",
                enterTransition = { 
                    slideInHorizontally(
                        initialOffsetX = { 300 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = { 
                    slideOutHorizontally(
                        targetOffsetX = { -300 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -300 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { 300 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                composable("habits") {
                    HomeScreen(
                        onNavigateToAddHabit = onNavigateToAddHabit,
                        onNavigateToDetail = onNavigateToDetail,
                        onNavigateToSettings = {
                            bottomNavController.navigate("settings") {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable("stats") {
                    StatsScreen(
                        onNavigateToYearInReview = {
                            bottomNavController.navigate("year_in_review")
                        }
                    )
                }
                composable("year_in_review") {
                    com.example.myapplication.presentation.stats.YearInReviewScreen(
                        onNavigateBack = {
                            bottomNavController.popBackStack()
                        }
                    )
                }
                composable("calendar") {
                    CalendarScreen()
                }
                composable("settings") {
                    SettingsScreen(
                        onNavigateBack = {
                            // Back in settings from bottom nav goes to habits
                            bottomNavController.navigate("habits") {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToArchived = {
                            bottomNavController.navigate("archived")
                        }
                    )
                }
                composable("archived") {
                    com.example.myapplication.presentation.archive.ArchivedHabitsScreen(
                        onNavigateBack = {
                            bottomNavController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
