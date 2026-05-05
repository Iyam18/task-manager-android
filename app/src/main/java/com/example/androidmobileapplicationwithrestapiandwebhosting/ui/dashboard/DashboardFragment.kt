package com.example.androidmobileapplicationwithrestapiandwebhosting.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.androidmobileapplicationwithrestapiandwebhosting.R
import com.example.androidmobileapplicationwithrestapiandwebhosting.api.RetrofitClient
import com.example.androidmobileapplicationwithrestapiandwebhosting.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Start entry animation
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        binding.root.startAnimation(fadeIn)

        loadDashboardData()
    }

    private fun loadDashboardData() {
        val prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getDashboardSummary(userId)
                if (response.isSuccessful && _binding != null) {
                    val summary = response.body()
                    val total = summary?.get("total") ?: 0
                    val pending = summary?.get("pending") ?: 0
                    val inProgress = summary?.get("in_progress") ?: 0
                    val completed = summary?.get("completed") ?: 0

                    binding.tvTotalTasks.text = total.toString()
                    binding.tvPendingTasks.text = pending.toString()
                    binding.tvInProgressTasks.text = inProgress.toString()
                    binding.tvCompletedTasks.text = completed.toString()

                    if (total > 0) {
                        val progress = (completed.toFloat() / total.toFloat() * 100).toInt()
                        binding.progressTasks.progress = progress
                        binding.tvProgressText.text = getString(R.string.progress_format, progress)
                    } else {
                        binding.progressTasks.progress = 0
                        binding.tvProgressText.text = getString(R.string.progress_format, 0)
                    }
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(context, getString(R.string.msg_dashboard_load_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
