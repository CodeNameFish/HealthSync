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
import com.example.healthsync.databinding.FragmentDashboardBinding
import com.example.healthsync.utils.DateUtils
import com.example.healthsync.utils.NetworkUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

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
        viewModel.loadData()

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvGreeting.text = getString(R.string.greeting, name.substringBefore("@"))
        }

        viewModel.todayLog.observe(viewLifecycleOwner) { log ->
            if (log != null) {
                binding.tvSteps.text = log.steps.toString()
                binding.tvHeartRate.text = getString(R.string.heart_rate_format, log.heartRate)
                binding.tvSleep.text = getString(R.string.sleep_format, log.sleepHours)
                binding.tvWater.text = getString(R.string.water_format, log.waterIntake)
            }
        }

        viewModel.weeklyLogs.observe(viewLifecycleOwner) { logs ->
            if (logs.isNotEmpty()) {
                setupStepsChart(logs)
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

    private fun setupStepsChart(logs: List<com.example.healthsync.data.model.HealthLog>) {
        val entries = logs.mapIndexed { index, log ->
            BarEntry(index.toFloat(), log.steps.toFloat())
        }

        val dataSet = BarDataSet(entries, "Steps").apply {
            color = "#2E7D32".toColorInt()
            valueTextColor = Color.BLACK
            valueTextSize = 10f
        }

        val barData = BarData(dataSet)
        binding.chartSteps.data = barData

        val dates = logs.map { log ->
            log.date.substring(5)
        }

        binding.chartSteps.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dates)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
        }

        binding.chartSteps.apply {
            description.isEnabled = false
            legend.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}