package com.example.healthsync.utils

import com.example.healthsync.data.model.HealthLog

object HealthScoreCalculator {

    fun calculateDailyScore(log: HealthLog): Int {
        var score = 0

        // Steps score (max 30 points)
        score += when {
            log.steps >= 10000 -> 30
            log.steps >= 7500 -> 25
            log.steps >= 5000 -> 20
            log.steps >= 3000 -> 15
            log.steps >= 1000 -> 10
            else -> 5
        }

        // Heart rate score (max 25 points)
        score += when (log.heartRate) {
            in 60..80 -> 25
            in 50..59, in 81..90 -> 20
            in 91..100 -> 15
            in 101..120 -> 10
            else -> 5
        }

        // Sleep score (max 25 points)
        score += when (log.sleepHours) {
            in 7.0..9.0 -> 25
            in 6.0..6.9, in 9.1..10.0 -> 20
            in 5.0..5.9 -> 15
            in 4.0..4.9 -> 10
            else -> 5
        }

        // Water intake score (max 20 points)
        score += when {
            log.waterIntake >= 3.0 -> 20
            log.waterIntake >= 2.0 -> 15
            log.waterIntake >= 1.5 -> 10
            log.waterIntake >= 1.0 -> 5
            else -> 0
        }

        return score
    }

    fun getHealthRating(score: Int): Pair<String, String> {
        return when (score) {
            in 85..100 -> Pair("Excellent!", "🌟 Outstanding health metrics!")
            in 70..84 -> Pair("Good!", "👍 Keep up the great work!")
            in 50..69 -> Pair("Fair", "⚠️ Room for improvement")
            else -> Pair("Needs Attention", "💪 Let's work on your health goals")
        }
    }
}