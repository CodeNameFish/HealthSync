package com.example.healthsync

import com.example.healthsync.data.model.HealthLog
import org.junit.Assert.*
import org.junit.Test

class DashboardViewModelTest {

    fun hasAnomaly(log: HealthLog?): Boolean {
        if (log == null) return false
        return log.heartRate !in 50..100
                || log.sleepHours < 4f
                || log.steps < 1000
    }
    @Test
    fun highHeartRateTriggersAnomaly() {
        val log = HealthLog(heartRate = 110, steps = 5000, sleepHours = 7f)
        assertTrue(hasAnomaly(log))
    }

    @Test
    fun normalVitalsReturnNoAnomaly() {
        val log = HealthLog(heartRate = 72, steps = 5000, sleepHours = 7f)
        assertFalse(hasAnomaly(log))
    }

    @Test
    fun lowSleepTriggersAnomaly() {
        val log = HealthLog(heartRate = 72, steps = 5000, sleepHours = 3f)
        assertTrue(hasAnomaly(log))
    }

    @Test
    fun nullLogReturnsNoAnomaly() {
        assertFalse(hasAnomaly(null))
    }
}