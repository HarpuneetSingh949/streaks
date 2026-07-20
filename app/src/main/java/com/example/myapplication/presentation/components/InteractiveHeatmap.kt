package com.example.myapplication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun InteractiveHeatmap(
    heatmapData: Map<String, Int>,
    activeHabitsCount: Int,
    weeksToDisplay: Int,
    onDateClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // Auto-scroll to the end (most recent)
    LaunchedEffect(weeksToDisplay) {
        if (weeksToDisplay > 0) {
            listState.scrollToItem(weeksToDisplay - 1)
        }
    }

    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val dayOfWeek = today.dayOfWeek.value
    val daysFromSunday = if (dayOfWeek == 7) 0 else dayOfWeek

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Y-Axis Labels
        Column(
            modifier = Modifier.padding(end = 8.dp, top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Aligns roughly with boxes
        ) {
            Text("Mon", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Wed", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Fri", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 10.sp)
        }

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(weeksToDisplay) { weekIndex ->
                Column(modifier = Modifier.padding(end = 4.dp)) {
                    for (day in 0 until 7) {
                        val totalDaysAgo = (weeksToDisplay - 1 - weekIndex) * 7 + (daysFromSunday - day)
                        val isFuture = totalDaysAgo < 0
                        val date = if (!isFuture) today.minusDays(totalDaysAgo.toLong()) else null
                        val dateStr = date?.format(formatter)
                        val completionsCount = if (!isFuture) heatmapData[dateStr] ?: 0 else 0

                        val boxColor = if (isFuture) {
                            Color.Transparent
                        } else {
                            getIntensityColor(completionsCount, activeHabitsCount)
                        }

                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .size(16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(boxColor)
                                .clickable(enabled = !isFuture) {
                                    if (dateStr != null) {
                                        onDateClick(dateStr)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

fun getIntensityColor(completions: Int, maxHabits: Int): Color {
    if (completions == 0) return Color(0xFF2D333B) // Empty (Dark Gray)
    
    // Similar to GitHub's 4 levels of intensity
    val ratio = if (maxHabits > 0) completions.toFloat() / maxHabits.toFloat() else 0f
    
    return when {
        ratio <= 0.25f -> Color(0xFF0E4429) // Dark Green
        ratio <= 0.5f -> Color(0xFF006D32)
        ratio <= 0.75f -> Color(0xFF26A641)
        else -> Color(0xFF39D353) // Bright Green
    }
}
