package com.example.healthsync.utils

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel

class HealthAIProcessor(context: Context) {
    private var interpreter: Interpreter? = null

    init {
        try {
            val modelFile = context.assets.openFd("health_trend_model.tflite")
            val inputStream = FileInputStream(modelFile.fileDescriptor)
            val fileChannel = inputStream.channel
            val buffer = fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                modelFile.startOffset,
                modelFile.declaredLength
            )
            interpreter = Interpreter(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun analyzeTrend(inputData: FloatArray): String {
        // inputData: [heartRate, sleepHours, steps, waterIntake]
        val hr = inputData[0]
        val sleep = inputData[1]
        val steps = inputData[2]
        val water = inputData[3]

        val output = Array(1) { FloatArray(1) }
        interpreter?.run(inputData, output)

        val scoreImpact = if (output[0][0] > 0.8) {
            "⚠️ Potential Fatigue Detected."
        } else {
            "✨ Optimal Balance."
        }

        val recommendation = when {
            hr > 100 && sleep < 6 -> "Your heart rate is high after low sleep. Recommendation: Skip high-intensity training and focus on hydration."
            steps < 3000 && water < 1.2 -> "Low activity and low water intake. Recommendation: Take a 15-minute walk and drink 500ml of water to boost metabolism."
            sleep > 9 && hr < 60 -> "Long sleep and low resting HR. Recommendation: Great recovery! You are ready for a challenging workout today."
            steps > 10000 && water < 2.0 -> "High activity but low hydration. Recommendation: You've burned a lot! Increase water intake to 3L to prevent muscle cramps."
            hr in 60.0..80.0 && sleep in 7.0..9.0 && steps > 5000 -> "Perfect sync! Recommendation: Maintain this routine; your body is in the ideal 'Performance Zone'."
            else -> "Steady trend. Recommendation: Keep monitoring your sleep and heart rate consistency."
        }

        return "$scoreImpact\n$recommendation"
    }

    fun close() {
        interpreter?.close()
    }
}