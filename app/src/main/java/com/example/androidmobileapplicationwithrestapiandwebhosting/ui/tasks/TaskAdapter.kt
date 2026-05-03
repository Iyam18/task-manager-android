package com.example.androidmobileapplicationwithrestapiandwebhosting.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.TextViewCompat
import com.example.androidmobileapplicationwithrestapiandwebhosting.R
import com.example.androidmobileapplicationwithrestapiandwebhosting.databinding.ItemTaskBinding
import com.example.androidmobileapplicationwithrestapiandwebhosting.model.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            val context = binding.root.context
            binding.tvTaskTitle.text = task.title ?: "No Title"
            binding.tvTaskDescription.text = task.description ?: "No Description"
            val status = task.status?.lowercase(Locale.ROOT) ?: "pending"
            binding.tvStatus.text = status.replaceFirstChar { it.uppercase() }

            val (bgColorRes, textColorRes) = when (status) {
                "completed" -> Pair(R.color.status_completed_bg, R.color.status_completed)
                "pending" -> Pair(R.color.status_pending_bg, R.color.status_pending)
                "in progress" -> Pair(R.color.status_in_progress_bg, R.color.status_in_progress)
                else -> Pair(R.color.surface_variant, R.color.on_surface_variant)
            }

            binding.cardStatus.setCardBackgroundColor(ContextCompat.getColor(context, bgColorRes))
            binding.tvStatus.setTextColor(ContextCompat.getColor(context, textColorRes))

            // Priority Badge
            val priority = task.priority?.lowercase(Locale.ROOT) ?: "medium"
            binding.tvPriority.text = priority.replaceFirstChar { it.uppercase() }
            
            val (priorityBg, priorityText) = when (priority) {
                "high" -> Pair(R.color.priority_high_bg, R.color.priority_high)
                "medium" -> Pair(R.color.priority_medium_bg, R.color.priority_medium)
                "low" -> Pair(R.color.priority_low_bg, R.color.priority_low)
                else -> Pair(R.color.outline_variant, R.color.on_surface_variant)
            }
            binding.cardPriority.setCardBackgroundColor(ContextCompat.getColor(context, priorityBg))
            binding.tvPriority.setTextColor(ContextCompat.getColor(context, priorityText))

            // Due Date & Overdue Indicator
            if (!task.dueDate.isNullOrBlank()) {
                binding.tvDueDate.text = task.dueDate
                val isOverdue = isTaskOverdue(task.dueDate)
                if (isOverdue && task.status?.lowercase(Locale.ROOT) != "completed") {
                    binding.tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.priority_high))
                    binding.tvDueDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_warning, 0, 0, 0)
                    TextViewCompat.setCompoundDrawableTintList(binding.tvDueDate, ContextCompat.getColorStateList(context, R.color.priority_high))
                } else {
                    binding.tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.on_surface_variant))
                    binding.tvDueDate.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_my_calendar, 0, 0, 0)
                    TextViewCompat.setCompoundDrawableTintList(binding.tvDueDate, ContextCompat.getColorStateList(context, R.color.on_surface_variant))
                }
            } else {
                binding.tvDueDate.text = context.getString(R.string.msg_no_due_date)
                binding.tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.on_surface_variant))
                binding.tvDueDate.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_my_calendar, 0, 0, 0)
                TextViewCompat.setCompoundDrawableTintList(binding.tvDueDate, ContextCompat.getColorStateList(context, R.color.on_surface_variant))
            }

            binding.btnEdit.setOnClickListener { onEdit(task) }
            binding.btnDelete.setOnClickListener { onDelete(task) }
        }

        private fun isTaskOverdue(dueDateStr: String): Boolean {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dueDate = sdf.parse(dueDateStr)
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                dueDate?.before(today) ?: false
            } catch (_: Exception) {
                false
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem == newItem
    }
}
