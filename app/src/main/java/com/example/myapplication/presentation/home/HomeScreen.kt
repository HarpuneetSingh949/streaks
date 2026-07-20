package com.example.myapplication.presentation.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.domain.model.HabitCategory
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.Position
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddHabit: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Streaks", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Filled.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOrder.values().forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.displayName) },
                                onClick = {
                                    viewModel.setSortOrder(order)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddHabit) {
                Icon(Icons.Filled.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search habits...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.setCategory(null) },
                        label = { Text("All") }
                    )
                }
                items(HabitCategory.values()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { viewModel.setCategory(category) },
                        label = { Text(category.displayName) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                when (val state = uiState) {
                    is HomeUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is HomeUiState.Error -> {
                        Text(state.message, modifier = Modifier.align(Alignment.Center))
                    }
                    is HomeUiState.Success -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            com.example.myapplication.presentation.home.components.DashboardHeader(
                                totalHabits = state.totalHabitsCount,
                                completedHabits = state.completedTodayCount,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            if (state.habits.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    EmptyStateMessage()
                                }
                            } else {
                                var selectedHabitForJournal by remember { mutableStateOf<String?>(null) }
                                
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.habits, key = { it.habit.id }) { uiModel ->
                                        HabitCard(
                                            uiModel = uiModel,
                                            onClick = { onNavigateToDetail(uiModel.habit.id) },
                                            onToggleCompletion = { isCompleted ->
                                                viewModel.toggleCompletion(uiModel.habit.id, isCompleted)
                                            },
                                            onArchive = { viewModel.archiveHabit(uiModel.habit.id) },
                                            onAddJournal = { selectedHabitForJournal = uiModel.habit.id }
                                        )
                                    }
                                }
                                
                                if (selectedHabitForJournal != null) {
                                    var mood by remember { mutableStateOf("😊") }
                                    var notes by remember { mutableStateOf("") }
                                    
                                    AlertDialog(
                                        onDismissRequest = { selectedHabitForJournal = null },
                                        title = { Text("Journal Entry") },
                                        text = {
                                            Column {
                                                Text("How did it feel?", style = MaterialTheme.typography.bodyMedium)
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    val moods = listOf("🤩", "😊", "😐", "😫")
                                                    moods.forEach { m ->
                                                        FilterChip(
                                                            selected = mood == m,
                                                            onClick = { mood = m },
                                                            label = { Text(m, style = MaterialTheme.typography.headlineSmall) }
                                                        )
                                                    }
                                                }
                                                OutlinedTextField(
                                                    value = notes,
                                                    onValueChange = { notes = it },
                                                    label = { Text("Notes (Optional)") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    maxLines = 3
                                                )
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                viewModel.updateJournal(selectedHabitForJournal!!, mood, notes)
                                                selectedHabitForJournal = null
                                            }) {
                                                Text("Save")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { selectedHabitForJournal = null }) {
                                                Text("Cancel")
                                            }
                                        }
                                    )
                                }
                                
                                val achievement by viewModel.achievementEvent.collectAsState()
                                if (achievement != null) {
                                    AlertDialog(
                                        onDismissRequest = { viewModel.clearAchievement() },
                                        title = { Text("🎉 Milestone Reached!", style = MaterialTheme.typography.headlineMedium) },
                                        text = { Text("Amazing! You've reached a ${achievement!!.streakAchieved} day streak!") },
                                        confirmButton = {
                                            TextButton(onClick = { viewModel.clearAchievement() }) {
                                                Text("Awesome!")
                                            }
                                        }
                                    )
                                    
                                    KonfettiView(
                                        modifier = Modifier.fillMaxSize(),
                                        parties = listOf(
                                            Party(
                                                emitter = Emitter(duration = 2, TimeUnit.SECONDS).max(100),
                                                position = Position.Relative(0.5, 0.0),
                                                spread = 360,
                                                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def)
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No streaks yet.", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Create Your First Habit", style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HabitCard(
    uiModel: HabitUiModel,
    onClick: () -> Unit,
    onToggleCompletion: (Boolean) -> Unit,
    onArchive: () -> Unit,
    onAddJournal: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onToggleCompletion(!uiModel.isCompletedToday)
            }
            false // Always snap back
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                    Text(
                        if (uiModel.isCompletedToday) "Undo" else "Complete",
                        modifier = Modifier.padding(end = 24.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .animateContentSize()
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            showMenu = true
                        }
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiModel.habit.emoji,
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = uiModel.habit.name, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${uiModel.currentStreak} Days",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiModel.habit.category.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    com.example.myapplication.presentation.home.components.HabitProgressRing(
                        isCompletedToday = uiModel.isCompletedToday,
                        onToggleCompletion = onToggleCompletion
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (uiModel.isCompletedToday) "Undo Complete" else "Complete") },
                        onClick = {
                            onToggleCompletion(!uiModel.isCompletedToday)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Journal") },
                        onClick = {
                            // Close menu and trigger callback
                            showMenu = false
                            onAddJournal()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onClick()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Archive", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onArchive()
                            showMenu = false
                        }
                    )
                }
            }
        }
    )
}
