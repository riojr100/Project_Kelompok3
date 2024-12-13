package com.example.project_kelompok3

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var emptyTaskImage: ImageView
    private lateinit var emptyTaskMessage: TextView
    private lateinit var taskList: RecyclerView
    private lateinit var taskAdapter: TaskAdapter

    private lateinit var selectionDialog: ConstraintLayout

    private lateinit var dialogDeleteButton: ImageButton
    private lateinit var dialogExitButton: ImageButton

    private val tasks = mutableListOf<Task>()  // List of Task objects

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize views
        emptyTaskImage = view.findViewById(R.id.empty_task)
        emptyTaskMessage = view.findViewById(R.id.empty_task_msg)
        taskList = view.findViewById(R.id.task_list)
        selectionDialog = view.findViewById(R.id.selection_dialog)

        dialogDeleteButton = view.findViewById(R.id.delete_selection_button)
        dialogExitButton = view.findViewById(R.id.exit_selection_button)

        // Set up RecyclerView
        taskList.layoutManager = LinearLayoutManager(context)
        taskAdapter = TaskAdapter(tasks, this)
        taskList.adapter = taskAdapter

        // Get the current logged-in user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid  // Get the user ID of the currently logged-in user
            // Fetch tasks for the current user from Firestore
            getTasksFromFirestore(userId)
        } else {
            // Handle case where no user is logged in
            Log.e("HomeFragment", "No user is logged in")
            showEmptyViews()
        }

        dialogDeleteButton.setOnClickListener {
            taskAdapter.deleteSelected()
        }

        dialogExitButton.setOnClickListener {
            taskAdapter.clearSelections()
            hideSelectionDialog()
        }

        return view
    }

    // Function to get tasks from Firestore for the current user
    private fun getTasksFromFirestore(userId: String) {
        // Clear the task list to avoid duplicates
        tasks.clear()

        db.collection("users").document(userId).collection("tasks")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    hideEmptyViews()  // Hide empty task views
                    for (document in documents) {
                        val task = Task(
                            taskId = document.getString("taskId") ?: "No ID",
                            title = document.getString("title") ?: "No Title",
                            description = document.getString("description") ?: "No Desc",
                            priority = document.getLong("priority")?.toInt() ?: -1,
                            state = document.getString("state") ?: "No State",
                            tag = document.getString("tag") ?: "No Tag",
                            dueDate = document.getString("dueDate") ?: "No Date"
                        )
                        tasks.add(task)
                    }
                    taskAdapter.notifyDataSetChanged()
                    passTasksToCalendar()
                } else {
                    showEmptyViews()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error getting tasks: ", exception)
                showEmptyViews()
            }
    }

    private fun passTasksToCalendar() {
        val tasksByDate = tasks.groupBy { it.dueDate }
        val calendarFragment = parentFragmentManager.findFragmentById(R.id.fragment_calendar) as? CalendarFragment
        calendarFragment?.setNotesData(tasksByDate)
    }

    fun showEditDialog(id : String, title: String, desc: String, due: String, tag: String, priority: Int, state: String){
        val editDialog = TaskEditDialog.newInstance(id, title, desc, due, tag, priority, state)
        editDialog.show(parentFragmentManager, "TaskEditDialog")
    }

    fun hideEditDialog(){

    }

    fun showSelectionDialog(){
        selectionDialog.visibility = View.VISIBLE
    }
    fun hideSelectionDialog(){
        selectionDialog.visibility = View.GONE
    }

    private fun hideEmptyViews() {
        emptyTaskImage.visibility = View.GONE
        emptyTaskMessage.visibility = View.GONE
        taskList.visibility = View.VISIBLE
    }

    private fun showEmptyViews() {
        emptyTaskImage.visibility = View.VISIBLE
        emptyTaskMessage.visibility = View.VISIBLE
        taskList.visibility = View.GONE
    }
}
