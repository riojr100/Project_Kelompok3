package com.example.project_kelompok3

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var emptyTaskImage: ImageView
    private lateinit var emptyTaskMessage: TextView
    private lateinit var taskList: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
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

        // Set up RecyclerView
        taskList.layoutManager = LinearLayoutManager(context)
        taskAdapter = TaskAdapter(tasks)
        taskList.adapter = taskAdapter

        // Get the current logged-in user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid // Get the user ID of the currently logged-in user
            // Fetch tasks for the current user from Firestore
            getTasksFromFirestore(userId)
        } else {
            // Handle case where no user is logged in
            Log.e("HomeFragment", "No user is logged in")
            showEmptyViews()
        }

        return view
    }

    // Function to get tasks from Firestore for the current user
    private fun getTasksFromFirestore(userId: String) {
        db.collection("users").document(userId).collection("tasks")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    hideEmptyViews() // Hide empty task views
                    for (document in documents) {
                        val task = Task(
                            title = document.getString("title") ?: "No Title",
                            dueDate = document.getString("dueDate") ?: "No Date"
                        )
                        tasks.add(task)
                    }
                    taskAdapter.notifyDataSetChanged() // Update RecyclerView
                } else {
                    showEmptyViews() // Show empty task views if no data
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error getting tasks: ", exception)
                showEmptyViews()
            }
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
