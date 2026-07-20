package com.example.myapplication.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.StreaksRepository
import com.example.myapplication.domain.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val streaksRepository: StreaksRepository,
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            // Check if notifications are enabled
            val prefs = userPreferencesRepository.userPreferencesFlow.first()
            if (!prefs.notificationsEnabled) {
                return Result.success()
            }

            val userId = authRepository.currentUserUid ?: return Result.success()

            val habits = streaksRepository.getHabitsForUser(userId).first()
            if (habits.isEmpty()) {
                return Result.success()
            }

            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val completionsToday = streaksRepository.getCompletionsForDate(userId, today).first()
            
            val completedHabitIds = completionsToday.filter { it.isCompleted }.map { it.habitId }
            val incompleteHabits = habits.filter { it.id !in completedHabitIds }

            if (incompleteHabits.isNotEmpty()) {
                sendNotification(incompleteHabits.size)
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private fun sendNotification(incompleteCount: Int) {
        val channelId = "daily_reminders"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Reminders"
            val descriptionText = "Reminders to complete your habits"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use a better icon if available
            .setContentTitle("Don't break your streak! 🔥")
            .setContentText("You have $incompleteCount habits left to complete today.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(1001, builder.build())
            }
        }
    }
}
