package com.example.healthsync.ui.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.healthsync.R
import com.example.healthsync.data.local.AppDatabase
import com.example.healthsync.databinding.FragmentLogBinding
import com.example.healthsync.utils.ValidationUtils

class LogFragment : Fragment() {

    private var _binding: FragmentLogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LogViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getInstance(requireContext())
                return LogViewModel(database) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveLog()
            }
        }

        viewModel.saveState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LogViewModel.SaveState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                is LogViewModel.SaveState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(requireContext(), R.string.save_success, Toast.LENGTH_SHORT).show()
                    clearInputs()
                    findNavController().navigate(R.id.dashboardFragment)
                }
                is LogViewModel.SaveState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val steps = binding.etSteps.text.toString().toIntOrNull() ?: -1
        val heartRate = binding.etHeartRate.text.toString().toIntOrNull() ?: -1
        val sleep = binding.etSleep.text.toString().toFloatOrNull() ?: -1f
        val water = binding.etWater.text.toString().toFloatOrNull() ?: -1f

        return when {
            !ValidationUtils.isValidSteps(steps) -> {
                binding.etSteps.error = getString(R.string.validation_steps)
                false
            }
            !ValidationUtils.isValidHeartRate(heartRate) -> {
                binding.etHeartRate.error = getString(R.string.validation_heart_rate)
                false
            }
            !ValidationUtils.isValidSleep(sleep) -> {
                binding.etSleep.error = getString(R.string.validation_sleep)
                false
            }
            water < 0f -> {
                binding.etWater.error = getString(R.string.validation_water)
                false
            }
            else -> true
        }
    }

    private fun saveLog() {
        val steps = binding.etSteps.text.toString().toIntOrNull() ?: 0
        val heartRate = binding.etHeartRate.text.toString().toIntOrNull() ?: 0
        val sleep = binding.etSleep.text.toString().toFloatOrNull() ?: 0f
        val water = binding.etWater.text.toString().toFloatOrNull() ?: 0f
        val notes = binding.etNotes.text.toString().trim()

        viewModel.saveHealthLog(steps, heartRate, sleep, water, notes)
    }

    private fun clearInputs() {
        binding.etSteps.text?.clear()
        binding.etHeartRate.text?.clear()
        binding.etSleep.text?.clear()
        binding.etWater.text?.clear()
        binding.etNotes.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}