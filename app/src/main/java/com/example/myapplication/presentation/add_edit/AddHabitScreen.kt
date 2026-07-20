package com.example.myapplication.presentation.add_edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddHabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Habit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Templates", style = MaterialTheme.typography.labelLarge)
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HabitTemplates.templates) { template ->
                    ElevatedFilterChip(
                        selected = false,
                        onClick = { viewModel.onEvent(AddHabitEvent.ApplyTemplate(template.name, template.emoji, template.category)) },
                        label = { Text("${template.emoji} ${template.name}") }
                    )
                }
            }

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onEvent(AddHabitEvent.EnteredName(it)) },
                label = { Text("Habit Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.emoji,
                onValueChange = { viewModel.onEvent(AddHabitEvent.EnteredEmoji(it)) },
                label = { Text("Emoji") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Category", style = MaterialTheme.typography.labelLarge)
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(com.example.myapplication.domain.model.HabitCategory.values()) { category ->
                    FilterChip(
                        selected = uiState.category == category,
                        onClick = { viewModel.onEvent(AddHabitEvent.SelectedCategory(category)) },
                        label = { Text(category.displayName) }
                    )
                }
            }

            Text("Frequency", style = MaterialTheme.typography.labelLarge)
            var showFrequencyMenu by remember { mutableStateOf(false) }
            val currentFreqName = when(uiState.frequency) {
                is com.example.myapplication.domain.model.HabitFrequency.Daily -> "Daily"
                is com.example.myapplication.domain.model.HabitFrequency.Weekdays -> "Weekdays"
                is com.example.myapplication.domain.model.HabitFrequency.Weekends -> "Weekends"
                is com.example.myapplication.domain.model.HabitFrequency.Weekly -> "Weekly"
                is com.example.myapplication.domain.model.HabitFrequency.Monthly -> "Monthly"
                else -> "Custom"
            }
            
            Box {
                OutlinedButton(onClick = { showFrequencyMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(currentFreqName)
                }
                DropdownMenu(
                    expanded = showFrequencyMenu,
                    onDismissRequest = { showFrequencyMenu = false }
                ) {
                    val frequencies = listOf(
                        com.example.myapplication.domain.model.HabitFrequency.Daily,
                        com.example.myapplication.domain.model.HabitFrequency.Weekdays,
                        com.example.myapplication.domain.model.HabitFrequency.Weekends,
                        com.example.myapplication.domain.model.HabitFrequency.Weekly,
                        com.example.myapplication.domain.model.HabitFrequency.Monthly
                    )
                    frequencies.forEach { freq ->
                        val label = when(freq) {
                            is com.example.myapplication.domain.model.HabitFrequency.Daily -> "Daily"
                            is com.example.myapplication.domain.model.HabitFrequency.Weekdays -> "Weekdays"
                            is com.example.myapplication.domain.model.HabitFrequency.Weekends -> "Weekends"
                            is com.example.myapplication.domain.model.HabitFrequency.Weekly -> "Weekly"
                            is com.example.myapplication.domain.model.HabitFrequency.Monthly -> "Monthly"
                            else -> "Custom"
                        }
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.onEvent(AddHabitEvent.SelectedFrequency(freq))
                                showFrequencyMenu = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onEvent(AddHabitEvent.EnteredNotes(it)) },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onEvent(AddHabitEvent.SaveHabit) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Habit")
            }
        }
    }
}
