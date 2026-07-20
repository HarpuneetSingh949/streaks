package com.example.myapplication.widget

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.GlanceTheme
import com.example.myapplication.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StreaksWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        val authRepo = entryPoint.authRepository()
        val streaksRepo = entryPoint.streaksRepository()
        
        val userId = authRepo.currentUserUid
        
        if (userId == null) {
            provideContent {
                GlanceTheme {
                    Box(
                        modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.background).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Please sign in.", style = TextStyle(color = GlanceTheme.colors.onBackground))
                    }
                }
            }
            return
        }

        val completions = streaksRepo.getAllCompletionsForUser(userId).first()
        val habits = streaksRepo.getHabitsForUser(userId).first()
        
        // Count completions per date
        val completedDatesCount = completions
            .filter { it.isCompleted }
            .groupBy { it.date }
            .mapValues { it.value.size }

        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        
        // Let's show the last 28 days (4 weeks x 7 days)
        // We'll build columns of weeks. Each column has 7 rows (days).
        val totalDays = 28
        val startDate = today.minusDays((totalDays - 1).toLong())
        
        // Generate list of pairs: (LocalDate, Count)
        val dailyData = (0 until totalDays).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val dateStr = date.format(formatter)
            date to (completedDatesCount[dateStr] ?: 0)
        }

        // Chunk into weeks (each week is a column)
        val weeks = dailyData.chunked(7)

        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surfaceVariant)
                        .padding(16.dp)
                ) {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Habit Heatmap",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                            Spacer(GlanceModifier.defaultWeight())
                            Text(
                                "${habits.size} Habits",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        
                        Spacer(GlanceModifier.height(12.dp))
                        
                        Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            weeks.forEach { week ->
                                Column(modifier = GlanceModifier.padding(horizontal = 2.dp)) {
                                    week.forEach { (date, count) ->
                                        val color = when {
                                            count == 0 -> ColorProvider(Color(0x20888888)) // Empty / light
                                            count == 1 -> GlanceTheme.colors.primaryContainer
                                            count == 2 -> GlanceTheme.colors.primary
                                            else -> GlanceTheme.colors.tertiary
                                        }
                                        
                                        Box(
                                            modifier = GlanceModifier
                                                .size(16.dp)
                                                .padding(bottom = 4.dp)
                                                .background(color)
                                        ) {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
