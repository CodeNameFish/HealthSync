package com.example.healthsync.utils

import com.example.healthsync.data.model.HealthLog

object AnomalyDetector {

    data class Anomaly(
        val type: String,
        val message: String,
        val severity: Severity
    )

    enum class Severity {
        LOW, MEDIUM, HIGH
    }

    fun detectAnomalies(currentLog: HealthLog, previousLogs: List<HealthLog>): List<Anomaly> {
        val anomalies = mutableListOf<Anomaly>()

        // Heart rate anomaly detection
        if (previousLogs.isNotEmpty()) {
            val avgHeartRate = previousLogs.takeLast(7).map { it.heartRate }.average()
            if (currentLog.heartRate > avgHeartRate * 1.3) {
                anomalies.add(Anomaly(
                    "High Heart Rate",
                    "Your heart rate is 30% higher than usual. Consider resting.",
                    Severity.HIGH
                ))
            } else if (currentLog.heartRate < avgHeartRate * 0.7) {
                anomalies.add(Anomaly(
                    "Low Heart Rate",
                    "Your heart rate is unusually low today.",
                    Severity.MEDIUM
                ))
            }
        }

        // Step count anomaly
        if (previousLogs.isNotEmpty()) {
            val avgSteps = previousLogs.takeLast(7).map { it.steps }.average()
            if (currentLog.steps < avgSteps * 0.5 && avgSteps > 3000) {
                anomalies.add(Anomaly(
                    "Low Activity",
                    "You're much less active than usual. Time to move!",
                    Severity.MEDIUM
                ))
            } else if (currentLog.steps > avgSteps * 1.5) {
                anomalies.add(Anomaly(
                    "High Activity",
                    "Great job! You're more active than usual today!",
                    Severity.LOW
                ))
            }
        }

        // Sleep anomaly
        if (currentLog.sleepHours < 6.0) {
            anomalies.add(Anomaly(
                "Sleep Deprivation",
                "You're not getting enough sleep. Aim for 7-9 hours.",
                Severity.HIGH
            ))
        } else if (currentLog.sleepHours > 10.0) {
            anomalies.add(Anomaly(
                "Excessive Sleep",
                "You've slept more than usual. Check your energy levels.",
                Severity.LOW
            ))
        }

        // Water intake anomaly
        if (currentLog.waterIntake < 1.5) {
            anomalies.add(Anomaly(
                "Dehydration Risk",
                "You're not drinking enough water. Stay hydrated!",
                Severity.HIGH
            ))
        }

        return anomalies
    }
}