package com.example.project_kelompok3

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Adapter for RecyclerView
class TaskAdapter(private val tasks: List<Task>, private val listener: HomeFragment) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    interface TaskAdapterListener {
        fun showSelectionDialog()
        fun hideSelectionDialog()
    }

    private val selectedTasks = mutableSetOf<Int>()
    var isSelectionOn = false

    // ViewHolder class to hold the task item views
    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskTitle: TextView = view.findViewById(R.id.task_title)
        val taskDueDate: TextView = view.findViewById(R.id.task_due_date)
        val taskUnchecked: ImageView = view.findViewById(R.id.unchecked)
        val taskChecked: ImageView = view.findViewById(R.id.checked)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskTitle.text = task.title
        holder.taskDueDate.text = task.dueDate

        if (isSelectionOn) {
            holder.taskUnchecked.visibility = View.VISIBLE
            holder.taskChecked.visibility = View.GONE
        } else {
            holder.taskUnchecked.visibility = View.GONE
            holder.taskChecked.visibility = View.GONE
        }

        if (selectedTasks.contains(position)) {
            holder.itemView.setBackgroundColor(android.graphics.Color.LTGRAY)
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)
        }
//        holder.taskUnchecked.visibility = View.VISIBLE
        holder.itemView.setOnLongClickListener {
            isSelectionOn = true
            if (isSelectionOn) {
                isSelectionOn = true
                listener.showSelectionDialog()
                this.notifyDataSetChanged()
//                toggleSelection(position)  // Toggle the selection state of the item

            }
            true
        }
        holder.itemView.setOnClickListener {
            if (isSelectionOn) {
//                toggleSelection(position)
                if (selectedTasks.contains(position)) {
                    holder.taskUnchecked.visibility = View.VISIBLE
                    holder.taskChecked.visibility = View.GONE
                    selectedTasks.remove(position)  // Unselect if already selected
                } else {
                    holder.taskUnchecked.visibility = View.GONE
                    holder.taskChecked.visibility = View.VISIBLE
                    selectedTasks.add(position)  // Select the item
                }
            } else {
                // Handle regular click behavior if necessary
            }
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    private fun toggleSelection(position: Int) {
        if (selectedTasks.contains(position)) {
            selectedTasks.remove(position)  // Unselect if already selected
        } else {
            selectedTasks.add(position)  // Select the item
        }
        notifyItemChanged(position)  // Notify the adapter that the item has changed

        // If no items are selected, exit selection mode
        if (selectedTasks.isEmpty()) {
            isSelectionOn = false
        }
    }

    fun deleteSelected() {
        // Loop over the selected tasks
        val tasksToDelete = selectedTasks.map { tasks[it] }  // Get selected tasks
        val db = FirebaseFirestore.getInstance()  // Firestore instance

        for (task in tasksToDelete) {
            // Delete each selected task from Firestore
            db.collection("users")
                .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .collection("tasks")
                .document(task.taskId)
                .delete()
                .addOnSuccessListener {
                    // Remove the task from the local list once it's deleted in Firestore
                    tasks.toMutableList().remove(task)

                    // Notify the adapter that the item has been removed
                    notifyDataSetChanged()

                    // Optionally, you can update the UI to show a message when all tasks are deleted
                    if (selectedTasks.isEmpty()) {
                        listener.hideSelectionDialog()
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the failure (e.g., show an error message)
                    Log.e("TaskAdapter", "Error deleting task", exception)
                }
        }

        // Clear the selection after deletion
        clearSelections()
    }

    fun clearSelections() {
        selectedTasks.clear()
        isSelectionOn = false
        notifyDataSetChanged()  // Notify the adapter to refresh all items
    }
}