package com.example.myapplication.domain.model

import java.time.DayOfWeek

sealed class HabitFrequency {
    object Daily : HabitFrequency()
    object Weekdays : HabitFrequency()
    object Weekends : HabitFrequency()
    object Weekly : HabitFrequency() // Any 1 day in the week
    object Monthly : HabitFrequency() // Any 1 day in the month
    data class EveryXDays(val days: Int) : HabitFrequency() // e.g., Every 3 days
    data class Custom(val daysOfWeek: Set<DayOfWeek>) : HabitFrequency() // Specific days, e.g. Mon, Wed, Fri
    
    // Serialization helper
    fun toJsonString(): String {
        return com.google.gson.Gson().toJson(FrequencyData.fromFrequency(this))
    }

    companion object {
        fun fromJsonString(json: String?): HabitFrequency {
            if (json == null) return Daily
            return try {
                val data = com.google.gson.Gson().fromJson(json, FrequencyData::class.java)
                data.toFrequency()
            } catch (e: Exception) {
                Daily
            }
        }
    }
}

// Data class to facilitate Gson serialization
data class FrequencyData(
    val type: String,
    val interval: Int? = null,
    val days: List<String>? = null
) {
    companion object {
        fun fromFrequency(freq: HabitFrequency): FrequencyData {
            return when (freq) {
                is HabitFrequency.Daily -> FrequencyData("Daily")
                is HabitFrequency.Weekdays -> FrequencyData("Weekdays")
                is HabitFrequency.Weekends -> FrequencyData("Weekends")
                is HabitFrequency.Weekly -> FrequencyData("Weekly")
                is HabitFrequency.Monthly -> FrequencyData("Monthly")
                is HabitFrequency.EveryXDays -> FrequencyData("EveryXDays", interval = freq.days)
                is HabitFrequency.Custom -> FrequencyData("Custom", days = freq.daysOfWeek.map { it.name })
            }
        }
    }

    fun toFrequency(): HabitFrequency {
        return when (type) {
            "Daily" -> HabitFrequency.Daily
            "Weekdays" -> HabitFrequency.Weekdays
            "Weekends" -> HabitFrequency.Weekends
            "Weekly" -> HabitFrequency.Weekly
            "Monthly" -> HabitFrequency.Monthly
            "EveryXDays" -> HabitFrequency.EveryXDays(interval ?: 1)
            "Custom" -> HabitFrequency.Custom(days?.map { DayOfWeek.valueOf(it) }?.toSet() ?: emptySet())
            else -> HabitFrequency.Daily
        }
    }
}
