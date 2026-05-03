package com.example.androidmobileapplicationwithrestapiandwebhosting.ui.tasks

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.androidmobileapplicationwithrestapiandwebhosting.R
import com.example.androidmobileapplicationwithrestapiandwebhosting.api.RetrofitClient
import com.example.androidmobileapplicationwithrestapiandwebhosting.databinding.FragmentAddEditTaskBinding
import com.example.androidmobileapplicationwithrestapiandwebhosting.model.Task
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddEditTaskFragment : Fragment() {

    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!
    private val args: AddEditTaskFragmentArgs by navArgs()
    private var isEditMode = false
    private var selectedDueDate: String? = null
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val currentUserId = prefs.getInt("user_id", -1)

        isEditMode = args.taskId != -1
        if (isEditMode) {
            binding.tvTitleLabel.text = getString(R.string.label_edit_task)
            binding.btnSave.text = getString(R.string.btn_update_task)
            loadTaskDetails(args.taskId)
        } else {
            binding.tvTitleLabel.text = getString(R.string.label_create_task)
            binding.btnSave.text = getString(R.string.btn_create_task)
        }
        binding.tilTitle.visibility = View.VISIBLE

        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            saveTask(currentUserId)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                selectedDueDate = sdf.format(selectedCalendar.time)
                binding.btnSelectDate.text = selectedDueDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun loadTaskDetails(taskId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getTask(taskId)
                if (response.isSuccessful) {
                    val task = response.body()?.task
                    task?.let {
                        binding.etTitle.setText(it.title)
                        binding.etDescription.setText(it.description)
                        
                        val statusArray = resources.getStringArray(R.array.task_status_options)
                        val statusIndex = statusArray.indexOf(it.status)
                        if (statusIndex >= 0) {
                            binding.spinnerStatus.setSelection(statusIndex)
                        }

                        val priorityArray = resources.getStringArray(R.array.task_priority_options)
                        val priorityIndex = priorityArray.indexOf(it.priority)
                        if (priorityIndex >= 0) {
                            binding.spinnerPriority.setSelection(priorityIndex)
                        }

                        if (!it.dueDate.isNullOrBlank()) {
                            selectedDueDate = it.dueDate
                            binding.btnSelectDate.text = it.dueDate
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading task details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTask(userId: Int) {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val status = binding.spinnerStatus.selectedItem.toString()
        val priority = binding.spinnerPriority.selectedItem.toString()

        if (title.isEmpty()) {
            Toast.makeText(context, getString(R.string.msg_title_required), Toast.LENGTH_SHORT).show()
            return
        }

        // Explicitly include the ID if we are in edit mode
        val task = Task(
            id = if (isEditMode) args.taskId else null,
            userId = userId,
            title = title,
            description = description,
            status = status,
            priority = priority,
            dueDate = selectedDueDate
        )

        lifecycleScope.launch {
            try {
                val response = if (isEditMode) {
                    RetrofitClient.instance.updateTask(args.taskId, task)
                } else {
                    RetrofitClient.instance.createTask(task)
                }

                if (response.isSuccessful) {
                    val successMsg = if (isEditMode) getString(R.string.msg_task_update_success) else getString(R.string.msg_task_create_success)
                    Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.msg_server_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
