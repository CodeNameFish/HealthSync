package com.example.healthsync.ui.profile

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.healthsync.R
import com.example.healthsync.databinding.FragmentProfileBinding
import com.example.healthsync.ui.auth.LoginActivity
import com.example.healthsync.utils.SecurityManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadProfile()

        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            binding.progressBar.visibility = View.GONE
            if (profile != null) {
                binding.tvName.text = profile.name
                binding.tvEmail.text = profile.email
                binding.tvAge.text = getString(R.string.age_format, profile.age)
                binding.tvWeight.text = getString(R.string.weight_format, profile.weight)
            } else {
                Toast.makeText(requireContext(), R.string.profile_load_error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.aiStatus.observe(viewLifecycleOwner) { status ->
            binding.tvAIStatus.text = status
        }

        viewModel.updateState.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                if (result.isSuccess) {
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Update failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
                viewModel.clearUpdateState()
            }
        }

        viewModel.isBiometricEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.switchBiometric.setOnCheckedChangeListener(null)
            binding.switchBiometric.isChecked = enabled
            binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    SecurityManager.showBiometricPrompt(requireActivity()) { success ->
                        if (success) {
                            viewModel.setBiometricEnabled(true)
                        } else {
                            binding.switchBiometric.isChecked = false
                            Toast.makeText(requireContext(), "Biometric verification failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    viewModel.setBiometricEnabled(false)
                }
            }
        }

        binding.btnEditName.setOnClickListener {
            showEditNameDialog()
        }

        binding.btnGenerateReport.setOnClickListener {
            viewModel.generateReport()
        }

        viewModel.reportState.observe(viewLifecycleOwner) { state ->
            if (state == null) return@observe
            
            when (state) {
                is ProfileViewModel.ReportState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnGenerateReport.isEnabled = false
                }
                is ProfileViewModel.ReportState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnGenerateReport.isEnabled = true
                    
                    val result = state.result
                    if (result.publicPath != null) {
                        Toast.makeText(requireContext(), "Report saved to Downloads: ${result.publicPath}", Toast.LENGTH_LONG).show()
                    }
                    
                    // Reset state before navigating so toast doesn't reappear on back
                    viewModel.resetReportState()
                    
                    // Navigate to internal viewer
                    val action = ProfileFragmentDirections.actionProfileFragmentToReportViewerFragment(result.previewFile.absolutePath)
                    findNavController().navigate(action)
                }
                is ProfileViewModel.ReportState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnGenerateReport.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetReportState()
                }
            }
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }

        viewModel.logoutState.observe(viewLifecycleOwner) { state ->
            if (state is ProfileViewModel.LogoutState.Success) {
                Toast.makeText(requireContext(), R.string.logout_success, Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun showEditNameDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Change Username")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        input.setText(binding.tvName.text)
        input.setSelection(input.text.length)
        builder.setView(input)

        builder.setPositiveButton("Save") { dialog, _ ->
            val newName = input.text.toString()
            if (newName.isNotBlank()) {
                viewModel.updateName(newName)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}