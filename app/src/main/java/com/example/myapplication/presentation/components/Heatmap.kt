package com.example.myapplication.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun HeatmapComponent(
    completedDates: Set<LocalDate>,
    modifier: Modifier = Modifier
) {
    val boxSize = 12.dp
    val spacing = 4.dp
    val weeksToShow = 15 // Roughly 3-4 months
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    
    val today = LocalDate.now()
    // Calculate the start date (bottom right will be today)
    // We draw columns (weeks) from left to right.
    // Rows (days of week) from top to bottom.
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height((boxSize * 7) + (spacing * 6))
            .padding(16.dp)
    ) {
        val boxSizePx = boxSize.toPx()
        val spacingPx = spacing.toPx()
        
        // Calculate offset so today is the last block
        val currentDayOfWeek = today.dayOfWeek.value // 1 (Mon) - 7 (Sun)
        
        val totalDays = (weeksToShow - 1) * 7 + currentDayOfWeek
        val startDate = today.minusDays((totalDays - 1).toLong())
        
        for (week in 0 until weeksToShow) {
            for (dayIndex in 0 until 7) {
                val dayOffset = (week * 7) + dayIndex
                val currentDate = startDate.plusDays(dayOffset.toLong())
                
                if (currentDate.isAfter(today)) break
                
                val isCompleted = completedDates.contains(currentDate)
                val color = if (isCompleted) primaryColor else emptyColor
                
                val x = week * (boxSizePx + spacingPx)
                val y = dayIndex * (boxSizePx + spacingPx)
                
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(boxSizePx, boxSizePx),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }
    }
}
