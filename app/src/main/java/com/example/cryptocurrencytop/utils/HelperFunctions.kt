package com.example.cryptocurrencytop.utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

fun convertPercentOfMinutesToSeconds(periodPercent: Int): Int {
    val periodSeconds = (0.6 * periodPercent).toInt()
    if (periodSeconds > 60) return 60
    if (periodSeconds < 1) return 1
    return periodSeconds
}

fun getTimeHMSFromTimestamp(timestamp: Long, shouldIncludeMillis: Boolean = false): String {
    val stamp = Timestamp(timestamp)
    val date = Date(stamp.time)
    val pattern = if (shouldIncludeMillis) {
        "HH:mm:ss.S"
    } else {
        "HH:mm:ss"
    }
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(date)
}