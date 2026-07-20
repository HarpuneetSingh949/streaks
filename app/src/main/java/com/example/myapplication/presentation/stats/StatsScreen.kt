package com.example.myapplication.presentation.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.components.SimpleBarChart
import com.example.myapplication.presentation.components.SimpleLineChart
import com.example.myapplication.presentation.components.StatCard
import com.example.myapplication.domain.usecase.StatisticsData
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateToYearInReview: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onNavigateToYearInReview) {
                        Text("Year in Review")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is StatsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is StatsUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center))
                }
                is StatsUiState.Success -> {
                    StatsContent(data = state.data)
                }
            }
        }
    }
}

@Composable
fun StatsContent(data: StatisticsData) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Total Completions",
                    value = "${data.totalCompletions}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Completion %",
                    value = String.format(Locale.getDefault(), "%.1f%%", data.completionPercentage),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Consistency Score",
                    value = String.format(Locale.getDefault(), "%.1f", data.consistencyScore),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Active Habits",
                    value = "${data.activeHabits}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text("Streaks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Longest Streak",
                    value = "${data.longestStreakTotal} 🔥",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Current Streak",
                    value = "${data.currentStreakTotal} 🔥",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text("Weekly Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                SimpleBarChart(data = data.weeklyCompletionData)
            }
        }
        
        item {
            Text("Monthly Trends", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                SimpleLineChart(data = data.monthlyCompletionData)
            }
        }

        item {
            Text("Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Best Day",
                    value = data.bestWeekday ?: "-",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Worst Day",
                    value = data.worstWeekday ?: "-",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Most Done",
                    value = data.mostCompletedHabit ?: "-",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Least Done",
                    value = data.leastCompletedHabit ?: "-",
                    modifier = Modifier.weight(1f)
                )
            }
            if (data.smartInsight != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡",
                            style = MaterialTheme.typography.displaySmall,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Column {
                            Text("Smart Insight", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(data.smartInsight, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
    }
}
