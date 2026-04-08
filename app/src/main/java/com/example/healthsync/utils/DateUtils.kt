package com.example.healthsync.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun getTodayString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))

    fun formatTime(timestamp: Long): String =
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))

    fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis
    }
}