package com.example.healthsync.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.healthsync.R
import com.example.healthsync.data.model.HealthLog
import com.example.healthsync.databinding.FragmentDashboardBinding
import com.example.healthsync.utils.DateUtils
import com.example.healthsync.utils.HealthScoreCalculator
import com.example.healthsync.utils.NetworkUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), R.string.no_internet, Toast.LENGTH_LONG).show()
        }

        binding.tvDate.text = DateUtils.formatDate(System.currentTimeMillis())
        updateGreeting()

        val today = LocalDate.now()
        val sevenDaysAgo = today.minusDays(6)
        val fmt = DateTimeFormatter.ofPattern("MMM d")
        binding.tvChartBadge.text = "${sevenDaysAgo.format(fmt)} – ${today.format(fmt)}"

        viewModel.loadData()

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvGreeting.text = getString(R.string.greeting, name.substringBefore("@"))
        }

        viewModel.todayLog.observe(viewLifecycleOwner) { log ->
            if (log != null) {
                updateMetrics(log)
                updateHealthScore(log)
            } else {
                resetMetrics()
            }
        }

        viewModel.weeklyLogs.observe(viewLifecycleOwner) { logs ->
            if (logs.isNotEmpty()) setupStepsChart(logs)
        }

        viewModel.aiInsight.observe(viewLifecycleOwner) { insight ->
            if (insight != null) {
                binding.cardAIInsight.visibility = View.VISIBLE
                binding.tvAIInsight.text = insight
            } else {
                binding.cardAIInsight.visibility = View.GONE
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        binding.fabLogHealth.setOnClickListener {
            findNavController().navigate(R.id.logFragment)
        }
    }

    private fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
        binding.tvHeaderLabel.text = greeting
    }

    private fun resetMetrics() {
        binding.tvSteps.text = "0"
        binding.tvHeartRate.text = "--"
        binding.tvSleep.text = "0.0"
        binding.tvWater.text = "0.0"
        binding.tvHealthScore.text = "0"
    }

    private fun updateMetrics(log: HealthLog) {
        binding.tvSteps.text = log.steps.toString()
        binding.tvStepsTrend.text = when {
            log.steps >= 10_000 -> "✓ goal reached"
            log.steps >= 7_500  -> "↑ almost there"
            else                -> "↓ below goal"
        }
        binding.tvStepsTrend.setTextColor(
            if (log.steps >= 10_000) requireContext().getColor(R.color.green_400)
            else requireContext().getColor(R.color.text_hint)
        )

        binding.tvHeartRate.text = getString(R.string.heart_rate_format, log.heartRate)
        binding.tvHeartRate.setTextColor(
            when {
                log.heartRate > 100 -> requireContext().getColor(R.color.red_400)
                log.heartRate in 60..80 -> requireContext().getColor(R.color.green_400)
                else -> requireContext().getColor(R.color.amber_400)
            }
        )
        binding.tvHeartTrend.text = when {
            log.heartRate in 60..80 -> "✓ normal"
            log.heartRate > 100     -> "↑ elevated"
            log.heartRate < 50      -> "↓ low"
            else                    -> "~ within range"
        }

        binding.tvSleep.text = getString(R.string.sleep_format, log.sleepHours)
        binding.tvSleep.setTextColor(
            when {
                log.sleepHours in 7f..9f -> requireContext().getColor(R.color.green_400)
                log.sleepHours < 6f      -> requireContext().getColor(R.color.red_400)
                else                     -> requireContext().getColor(R.color.amber_400)
            }
        )
        binding.tvSleepTrend.text = when {
            log.sleepHours in 7f..9f -> "✓ on target"
            log.sleepHours < 6f      -> "↓ too low"
            log.sleepHours > 10f     -> "↑ too much"
            else                     -> "~ close to goal"
        }

        binding.tvWater.text = getString(R.string.water_format, log.waterIntake)
        binding.tvWater.setTextColor(
            if (log.waterIntake >= 2f) requireContext().getColor(R.color.blue_400)
            else requireContext().getColor(R.color.amber_400)
        )
        binding.tvWaterTrend.text = when {
            log.waterIntake >= 2f   -> "✓ on target"
            log.waterIntake >= 1.5f -> "~ keep going"
            else                    -> "↓ drink more"
        }
    }

    private fun updateHealthScore(log: HealthLog) {
        binding.tvHealthScore.text = HealthScoreCalculator.calculateDailyScore(log).toString()
    }

    private fun setupStepsChart(logs: List<HealthLog>) {
        val entries = logs.mapIndexed { i, log -> BarEntry(i.toFloat(), log.steps.toFloat()) }
        val todayIdx = logs.size - 1
        val colors = logs.indices.map { i ->
            if (i == todayIdx) "#2E7D32".toColorInt() else "#C8E6C5".toColorInt()
        }
        val dataSet = BarDataSet(entries, "Steps").apply {
            setColors(colors)
            valueTextColor = Color.TRANSPARENT
        }
        binding.chartSteps.data = BarData(dataSet).apply { barWidth = 0.6f }
        binding.chartSteps.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(logs.map { it.date.substring(5) })
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            textColor = resources.getColor(R.color.text_hint, null)
            textSize = 9f
            setDrawAxisLine(false)
        }
        binding.chartSteps.apply {
            description.isEnabled = false
            legend.isEnabled = false
            axisLeft.apply {
                axisMinimum = 0f
                gridColor = resources.getColor(R.color.bg_page, null)
                textColor = resources.getColor(R.color.text_hint, null)
                textSize = 9f
                setDrawAxisLine(false)
            }
            axisRight.isEnabled = false
            setTouchEnabled(false)
            setExtraOffsets(0f, 4f, 0f, 4f)
            animateY(600)
            invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
        updateGreeting()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}