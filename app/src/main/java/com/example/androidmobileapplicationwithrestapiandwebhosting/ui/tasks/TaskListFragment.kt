package com.example.androidmobileapplicationwithrestapiandwebhosting.ui.tasks

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidmobileapplicationwithrestapiandwebhosting.R
import com.example.androidmobileapplicationwithrestapiandwebhosting.api.RetrofitClient
import com.example.androidmobileapplicationwithrestapiandwebhosting.databinding.FragmentTaskListBinding
import com.example.androidmobileapplicationwithrestapiandwebhosting.model.Task
import kotlinx.coroutines.launch

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TaskAdapter
    private var allTasks = listOf<Task>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()

        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.navigation_add_task)
        }
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipPending -> "Pending"
                R.id.chipInProgress -> "In Progress"
                R.id.chipCompleted -> "Completed"
                else -> "All"
            }
            applyFilters(filter)
        }
    }

    private fun applyFilters(statusFilter: String = "All", query: String? = null) {
        var filteredList = allTasks

        if (statusFilter != "All") {
            filteredList = filteredList.filter { it.status.equals(statusFilter, ignoreCase = true) }
        }

        if (!query.isNullOrBlank()) {
            filteredList = filteredList.filter {
                (it.title?.contains(query, ignoreCase = true) ?: false) ||
                (it.description?.contains(query, ignoreCase = true) ?: false)
            }
        }

        adapter.submitList(filteredList)
        binding.llEmptyState.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onEdit = { task ->
                val action = TaskListFragmentDirections.actionNavigationTasksToNavigationAddTask(task.id ?: -1)
                findNavController().navigate(action)
            },
            onDelete = { task ->
                showDeleteConfirmation(task.id!!)
            }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(context)
        binding.rvTasks.adapter = adapter
        
        setupSwipeToDelete()
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = adapter.currentList[position]
                showDeleteConfirmation(task.id!!)
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvTasks)
    }

    private fun showDeleteConfirmation(taskId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.msg_confirm_delete)
            .setPositiveButton(android.R.string.ok) { _, _ -> deleteTask(taskId) }
            .setNegativeButton(android.R.string.cancel) { _, _ -> 
                adapter.notifyDataSetChanged() 
            }
            .setOnCancelListener { adapter.notifyDataSetChanged() }
            .show()
    }

    private fun loadTasks() {
        val prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getTasks(userId)
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    allTasks = response.body()?.tasks ?: emptyList()
                    // Re-apply current filters to the new data
                    val checkedChipId = binding.chipGroupFilters.checkedChipId
                    val currentFilter = when (checkedChipId) {
                        R.id.chipPending -> "Pending"
                        R.id.chipInProgress -> "In Progress"
                        R.id.chipCompleted -> "Completed"
                        else -> "All"
                    }
                    applyFilters(currentFilter)
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteTask(taskId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.deleteTask(taskId)
                if (response.isSuccessful) {
                    loadTasks()
                    Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.task_list_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val checkedChipId = binding.chipGroupFilters.checkedChipId
                val currentFilter = when (checkedChipId) {
                    R.id.chipPending -> "Pending"
                    R.id.chipInProgress -> "In Progress"
                    R.id.chipCompleted -> "Completed"
                    else -> "All"
                }
                applyFilters(currentFilter, newText)
                return true
            }
        })
    }

    private fun filterTasks(query: String?) {
        // Deprecated in favor of applyFilters
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
