package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.presentation.add_edit.AddHabitScreen
import com.example.myapplication.presentation.auth.LoginScreen
import com.example.myapplication.presentation.home.MainScreen
import com.example.myapplication.presentation.settings.SettingsScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.myapplication.domain.model.ThemeConfig
import com.example.myapplication.domain.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val userPreferences by userPreferencesRepository.userPreferencesFlow.collectAsState(initial = null)
            
            val isDarkTheme = when (userPreferences?.themeConfig) {
                ThemeConfig.LIGHT -> false
                ThemeConfig.DARK -> true
                else -> isSystemInDarkTheme()
            }
            
            val useDynamicColors = userPreferences?.useDynamicColors ?: true

            MyApplicationTheme(
                darkTheme = isDarkTheme,
                dynamicColor = useDynamicColors
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") {
                            MainScreen(
                                onNavigateToAddHabit = {
                                    navController.navigate("add_habit")
                                },
                                onNavigateToDetail = { habitId ->
                                    navController.navigate("detail/$habitId")
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true } // Clear stack
                                    }
                                }
                            )
                        }
                        composable("add_habit") {
                            AddHabitScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("detail/{habitId}") { backStackEntry ->
                            // The ViewModel will extract habitId from SavedStateHandle
                            com.example.myapplication.presentation.detail.DetailScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}