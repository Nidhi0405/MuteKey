package com.bbm.applock.util

fun aggregateHourlyToDays(
    hourlyMap: Map<String, List<Long>>,
    days: Int
): Map<String, List<Long>> {
    val result = mutableMapOf<String, List<Long>>()
    hourlyMap.forEach { (pkg, hours) ->
        val dayBuckets = MutableList(days) { 0L }
        hours.forEachIndexed { index, value ->
            val dayIndex = index / 24
            if (dayIndex < days) {
                dayBuckets[dayIndex] += value
            }
        }
        result[pkg] = dayBuckets
    }
    return result
}