package com.example.myapplication.presentation.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.domain.model.ThemeConfig
import com.example.myapplication.domain.model.WidgetStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToArchived: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showWidgetDialog by remember { mutableStateOf(false) }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.onEvent(SettingsEvent.ExportJson(it)) }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { viewModel.onEvent(SettingsEvent.ExportCsv(it)) }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onEvent(SettingsEvent.ImportJson(it)) }
    }

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            onNavigateToLogin()
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onEvent(SettingsEvent.ClearMessage)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure? This will delete all your data permanently.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.onEvent(SettingsEvent.DeleteAccount)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    ThemeConfig.values().forEach { theme ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onEvent(SettingsEvent.SetTheme(theme))
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.preferences.themeConfig == theme,
                                onClick = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(theme.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showWidgetDialog) {
        AlertDialog(
            onDismissRequest = { showWidgetDialog = false },
            title = { Text("Select Widget Style") },
            text = {
                Column {
                    WidgetStyle.values().forEach { style ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onEvent(SettingsEvent.SetWidgetStyle(style))
                                    showWidgetDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.preferences.widgetStyle == style,
                                onClick = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(style.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWidgetDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    SettingsSectionTitle("Appearance")
                    
                    SettingsClickableItem(
                        title = "Theme",
                        subtitle = uiState.preferences.themeConfig.displayName,
                        onClick = { showThemeDialog = true }
                    )
                    SettingsSwitchItem(
                        title = "Dynamic Colors",
                        subtitle = "Use Material You colors based on wallpaper",
                        checked = uiState.preferences.useDynamicColors,
                        onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleDynamicColors(it)) }
                    )
                    SettingsSwitchItem(
                        title = "Animations",
                        subtitle = "Enable UI transitions and animations",
                        checked = uiState.preferences.animationsEnabled,
                        onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleAnimations(it)) }
                    )
                }

                item {
                    SettingsSectionTitle("Notifications & Widgets")
                    
                    SettingsSwitchItem(
                        title = "Daily Reminders",
                        subtitle = "Receive push notifications for incomplete habits",
                        checked = uiState.preferences.notificationsEnabled,
                        onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleNotifications(it)) }
                    )
                    SettingsClickableItem(
                        title = "Widget Style",
                        subtitle = uiState.preferences.widgetStyle.displayName,
                        onClick = { showWidgetDialog = true }
                    )
                }

                item {
                    SettingsSectionTitle("Data & Backup")
                    
                    SettingsClickableItem(
                        title = "Archived Habits",
                        subtitle = "Restore or permanently delete archived habits",
                        onClick = onNavigateToArchived
                    )
                    SettingsClickableItem(
                        title = "Export to JSON",
                        subtitle = "Save a complete backup of your data",
                        onClick = { exportJsonLauncher.launch("streaks_backup.json") }
                    )
                    SettingsClickableItem(
                        title = "Import from JSON",
                        subtitle = "Restore a backup from a JSON file",
                        onClick = { importJsonLauncher.launch(arrayOf("application/json")) }
                    )
                    SettingsClickableItem(
                        title = "Export to CSV",
                        subtitle = "Export habits and history as a spreadsheet",
                        onClick = { exportCsvLauncher.launch("streaks_data.csv") }
                    )
                }

                item {
                    SettingsSectionTitle("About")
                    
                    SettingsClickableItem(
                        title = "Privacy Policy",
                        subtitle = "Read how we handle your data",
                        onClick = { /* TODO */ }
                    )
                    SettingsClickableItem(
                        title = "Open Source Licenses",
                        subtitle = "Libraries used in this app",
                        onClick = { /* TODO */ }
                    )
                    SettingsClickableItem(
                        title = "App Version",
                        subtitle = "1.0.0",
                        onClick = { /* Do nothing */ }
                    )
                }

                item {
                    SettingsSectionTitle("Danger Zone")
                    
                    SettingsClickableItem(
                        title = "Sign Out",
                        subtitle = "Sign out of your account",
                        onClick = { viewModel.onEvent(SettingsEvent.SignOut) },
                        titleColor = MaterialTheme.colorScheme.primary
                    )
                    SettingsClickableItem(
                        title = "Delete Account",
                        subtitle = "Permanently delete all your data",
                        onClick = { showDeleteConfirm = true },
                        titleColor = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = titleColor)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
