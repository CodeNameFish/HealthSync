package com.example.healthsync.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.healthsync.R
import com.example.healthsync.databinding.FragmentProfileBinding
import com.example.healthsync.ui.auth.LoginActivity

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

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Edit profile - Coming in next update", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}