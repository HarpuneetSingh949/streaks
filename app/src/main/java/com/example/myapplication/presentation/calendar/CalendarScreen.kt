package com.example.myapplication.presentation.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.components.InteractiveHeatmap
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class TimeRange(val title: String, val weeks: Int) {
    MONTH("Month", 5),
    SIX_MONTHS("6 Months", 26),
    YEAR("Year", 52),
    LIFETIME("Lifetime", 104) // Roughly 2 years for now, can be dynamic
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedRange by remember { mutableStateOf(TimeRange.MONTH) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // When selected date changes and is not null, show the bottom sheet
    LaunchedEffect(uiState) {
        if (uiState is CalendarUiState.Success) {
            val state = uiState as CalendarUiState.Success
            if (state.selectedDate != null && !showBottomSheet) {
                showBottomSheet = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is CalendarUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CalendarUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center))
                }
                is CalendarUiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    ) {
                        // Time Range Selector
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            TimeRange.values().forEachIndexed { index, range ->
                                SegmentedButton(
                                    selected = selectedRange == range,
                                    onClick = { selectedRange = range },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = TimeRange.values().size)
                                ) {
                                    Text(range.title)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Contribution Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Heatmap
                        InteractiveHeatmap(
                            heatmapData = state.heatmapIntensity,
                            activeHabitsCount = state.allHabits.size,
                            weeksToDisplay = selectedRange.weeks,
                            onDateClick = { date ->
                                viewModel.selectDate(date)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (showBottomSheet && state.selectedDate != null) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                showBottomSheet = false
                                viewModel.selectDate(null)
                            },
                            sheetState = sheetState
                        ) {
                            DayDetailsContent(
                                date = state.selectedDate,
                                details = state.selectedDateDetails,
                                onToggle = { habitId, isCompleted ->
                                    viewModel.toggleCompletion(habitId, state.selectedDate, isCompleted)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayDetailsContent(
    date: String,
    details: List<DailyDetail>,
    onToggle: (String, Boolean) -> Unit
) {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val displayFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    val displayDate = try {
        LocalDate.parse(date, formatter).format(displayFormatter)
    } catch (e: Exception) {
        date
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
        Text(displayDate, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Completed: ${details.count { it.isCompleted }} / ${details.size}", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (details.isEmpty()) {
            Text("No active habits for this date.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(details) { detail ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(detail.habit.emoji, style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(detail.habit.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Checkbox(
                                checked = detail.isCompleted,
                                onCheckedChange = { onToggle(detail.habit.id, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
