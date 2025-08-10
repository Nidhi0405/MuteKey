package com.applock.data.localdb

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString() // ISO format: "yyyy-MM-dd"
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.toString()
    }

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let { LocalTime.parse(it) }
    }

    @TypeConverter
    fun fromDayOfWeekList(days: List<DayOfWeek>): String =
        days.joinToString(",") { it.name }

    @TypeConverter
    fun toDayOfWeekList(data: String): List<DayOfWeek> =
        if (data.isEmpty()) emptyList()
        else data.split(",").map { DayOfWeek.valueOf(it) }
}