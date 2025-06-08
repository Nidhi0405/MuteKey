package com.applock.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Schedule(
    val id: Int = 0,
    val name: String,
    val isActive: Boolean = false
)

@Serializable
data class TimeSlots(
    val id: Int = 0,
    val start: Long,
    val end: Long,
    val scheduleId: Int, // foreign key
)

@Serializable
data class BlockedApps(
    val id: Int,
    val name: String,
    val packageName: String,
    val timeSlotId: Int, // foreign key
)