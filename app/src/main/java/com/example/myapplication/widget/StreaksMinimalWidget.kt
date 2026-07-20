package com.example.myapplication.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.sp
import dagger.hilt.android.EntryPointAccessors
import com.example.myapplication.di.WidgetEntryPoint
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Color

class StreaksMinimalWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        val authRepo = entryPoint.authRepository()
        val streaksRepo = entryPoint.streaksRepository()
        
        val userId = authRepo.currentUserUid
        
        if (userId == null) {
            provideContent {
                Box(
                    modifier = GlanceModifier.fillMaxSize().background(ColorProvider(Color.White)).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Please sign in.")
                }
            }
            return
        }

        val completions = streaksRepo.getAllCompletionsForUser(userId).first()
        val completedDates = completions
            .filter { it.isCompleted }
            .groupBy { it.date }
        
        // Calculate Streak
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        var currentStreak = 0
        var checkDate = today
        
        val todayStr = checkDate.format(formatter)
        if (completedDates.containsKey(todayStr)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        } else {
            val yesterdayStr = checkDate.minusDays(1).format(formatter)
            if (completedDates.containsKey(yesterdayStr)) {
                checkDate = checkDate.minusDays(1)
            }
        }
        
        while(completedDates.containsKey(checkDate.format(formatter))) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color(0xFF1E1E1E)))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥", style = TextStyle(fontSize = 28.sp))
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text("$currentStreak", style = TextStyle(color = ColorProvider(Color.White), fontSize = 24.sp, fontWeight = FontWeight.Bold))
                    Text("Days", style = TextStyle(color = ColorProvider(Color.Gray), fontSize = 12.sp))
                }
            }
        }
    }
}
