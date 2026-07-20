package com.example.myapplication.presentation.stats

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.components.StatCard
import com.example.myapplication.domain.usecase.StatisticsData
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearInReviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // We would use a library like Compose to Bitmap here. For simplicity in this demo,
    // we'll just mock the share function.
    fun shareYearInReview() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "I just reviewed my year in Streaks! I have a consistency score of ${if (uiState is StatsUiState.Success) (uiState as StatsUiState.Success).data.consistencyScore else 0}%. Join me in building better habits!")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Year in Review", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { shareYearInReview() }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
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
                    YearInReviewContent(data = state.data)
                }
            }
        }
    }
}

@Composable
fun YearInReviewContent(data: StatisticsData) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Your Streaks Journey",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Consistency Score", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = String.format(Locale.getDefault(), "%.1f%%", data.consistencyScore),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("You are in the top 5% of users! (Mock)", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Total Completions",
                value = "${data.totalCompletions}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Active Days",
                value = "${data.activeDays}",
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Longest Streak",
                value = "${data.longestStreakTotal} 🔥",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Most Completed",
                value = data.mostCompletedHabit ?: "-",
                modifier = Modifier.weight(1f)
            )
        }
        
        Text("Category Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.categoryBreakdown.forEach { (category, percentage) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(category, modifier = Modifier.width(100.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(percentage / 100f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Text(String.format(Locale.getDefault(), "%.1f%%", percentage), modifier = Modifier.width(60.dp).padding(start = 8.dp))
                }
            }
        }
    }
}
