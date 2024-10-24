package com.example.project_kelompok3

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var taskList: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()  // List of Task objects
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var calendarLayout: LinearLayout
    private lateinit var prevWeekButton: ImageView
    private lateinit var nextWeekButton: ImageView
    private lateinit var monthTextView: TextView  // To display the current month and year
    private var currentCalendar = Calendar.getInstance()  // Track the current calendar week

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // Initialize RecyclerView
        taskList = view.findViewById(R.id.recyclerViewTasks)
        taskList.layoutManager = LinearLayoutManager(context)
        taskAdapter = TaskAdapter(tasks, HomeFragment())
        taskList.adapter = taskAdapter

        // Initialize calendar and navigation buttons
        calendarLayout = view.findViewById(R.id.calendarLayout)
        prevWeekButton = view.findViewById(R.id.prevWeekButton)
        nextWeekButton = view.findViewById(R.id.nextWeekButton)
        monthTextView = view.findViewById(R.id.monthTextView)  // TextView for current month and year

        // Get the current logged-in user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            // Populate calendar for the current week
            updateMonthYear()  // Show the current month and year
            populateCalendar(userId)
        }

        // Handle previous week navigation
        prevWeekButton.setOnClickListener {
            currentCalendar.add(Calendar.WEEK_OF_YEAR, -1)
            updateMonthYear()  // Update the month and year when week changes
            val userId = auth.currentUser?.uid
            if (userId != null) {
                populateCalendar(userId)
            }
        }

        // Handle next week navigation
        nextWeekButton.setOnClickListener {
            currentCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            updateMonthYear()  // Update the month and year when week changes
            val userId = auth.currentUser?.uid
            if (userId != null) {
                populateCalendar(userId)
            }
        }

        return view
    }

    // Function to populate the calendar view dynamically for the current week
    private fun populateCalendar(userId: String) {
        // Clear existing buttons before repopulating
        calendarLayout.removeAllViews()

        val dateFormat = SimpleDateFormat("EEE d", Locale.getDefault())  // Format for day of week and day of month
        val fullDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())  // Full date format for matching Firebase

        // Set calendar to the beginning of the week (Sunday)
        currentCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        // Create buttons for the current week (Sunday to Saturday)
        for (i in 0..6) {
            val dayButton = Button(context)
            val displayDate = dateFormat.format(currentCalendar.time)  // e.g., "Sun 6"
            val fullDate = fullDateFormat.format(currentCalendar.time)  // e.g., "25/10/2024" for Firebase matching

            // Set the button text
            dayButton.text = displayDate

            // Apply the custom background
            dayButton.background = resources.getDrawable(R.drawable.calendar_day_background, null)

            // Set click listener for each date button
            dayButton.setOnClickListener {
                // Call the function to get tasks for this date
                getTasksForSelectedDate(userId, fullDate)  // Pass only the date part
            }

            // Add the button to the LinearLayout
            calendarLayout.addView(dayButton)

            // Move to the next day
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Reset calendar to the current date after the loop
        currentCalendar.add(Calendar.DAY_OF_MONTH, -7)  // Rewind back after the loop
    }

    // Function to fetch tasks for the selected date
    private fun getTasksForSelectedDate(userId: String, date: String) {
        tasks.clear()  // Clear the list to avoid duplicates

        Log.d("CalendarFragment", "Fetching tasks for date: $date")  // Log the selected date

        // Fetch tasks where `dueDate` matches the selected date (ignoring time)
        db.collection("users").document(userId).collection("tasks")
            .whereEqualTo("dueDate", date)  // Only match the date part
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val task = Task(
                            taskId = document.getString("taskId") ?: "No ID",
                            title = document.getString("title") ?: "No Title",
                            dueDate = document.getString("dueDate") ?: "No Date"
                        )
                        tasks.add(task)
                    }
                    taskAdapter.notifyDataSetChanged()  // Update RecyclerView
                } else {
                    Log.e("CalendarFragment", "No tasks found for this date")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CalendarFragment", "Error getting tasks: ", exception)
            }
    }

    // Function to update the displayed month and year
    private fun updateMonthYear() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())  // Format for month and year
        val currentMonthYear = monthFormat.format(currentCalendar.time)
        monthTextView.text = currentMonthYear  // Set the text for the current month and year
    }
}
