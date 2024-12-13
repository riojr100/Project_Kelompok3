package com.example.project_kelompok3

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log

class FragmentActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var addTaskButton: FloatingActionButton
    private lateinit var nextButtonAddTaskDialog: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment)

        auth = FirebaseAuth.getInstance()

        addTaskButton = findViewById(R.id.add_task)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        val homeFragment = HomeFragment()
        val calendarFragment = CalendarFragment()
        val focusFragment = FocusFragment()
        val profileFragment = ProfileFragment()

        loadFragment(homeFragment)
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            var pageName: String? = null
            when (item.itemId) {
//                home calendar focus profile
                R.id.nav_home -> {
                    selectedFragment = homeFragment
                    pageName = "home"
                }

                R.id.nav_calendar -> {
                    selectedFragment = calendarFragment
                    pageName = "calendar"
                }

                R.id.nav_focus -> {
                    selectedFragment = focusFragment
                    pageName = "focus"
                }

                R.id.nav_profile -> {
                    selectedFragment = profileFragment
                    pageName = "profile"
                }

                else -> {
                    selectedFragment = homeFragment
                    pageName = "home"
                }
            }
            loadFragment(selectedFragment)
            true
        }

        addTaskButton.setOnClickListener {
            showAddTaskDialog()
        }

        fetchAndApplySystemBarColor() // Apply system bar color on activity start
    }

    override fun onResume() {
        super.onResume()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            redirectToLogin()
        }
    }

    private fun fetchAndApplySystemBarColor() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val backgroundColor = document.getString("background_color") ?: "#8692f7" // Default lavender
                    applySystemBarColors(backgroundColor)
                }
                .addOnFailureListener { e ->
                    Log.e("FragmentActivity", "Error fetching background color: ${e.message}")
                    applySystemBarColors("#8692f7") // Fallback to default lavender color
                }
        }
    }

    private fun applySystemBarColors(colorHex: String) {
        try {
            val color = Color.parseColor(colorHex)
            window.statusBarColor = color // Change the status bar color
            window.navigationBarColor = color // Change the navigation bar color
        } catch (e: IllegalArgumentException) {
            // Handle invalid color format
            val defaultColor = Color.parseColor("#8692f7") // Default lavender
            window.statusBarColor = defaultColor
            window.navigationBarColor = defaultColor
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun showCalendarAndTimePicker(onDateTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        // DatePickerDialog to pick the date
        val datePickerDialog = DatePickerDialog(
            this, { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Set the selected date on the Calendar instance
                calendar.set(year, monthOfYear, dayOfMonth)

                // Once the date is picked, show TimePickerDialog to pick the time
                showTimePicker(calendar) { selectedDateTime ->
                    // Return the combined date and time
                    onDateTimeSelected(selectedDateTime)
                }

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Show the DatePicker dialog
        datePickerDialog.show()
    }

    private fun showTimePicker(calendar: Calendar, onTimeSelected: (String) -> Unit) {
        val timePickerDialog = TimePickerDialog(
            this, { _, hourOfDay: Int, minute: Int ->
                // Set the selected time on the Calendar instance
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                // Format the date and time together
                val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val formattedDateTime = dateTimeFormat.format(calendar.time)

                // Return the selected date and time
                onTimeSelected(formattedDateTime)

            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // true for 24-hour format, false for AM/PM format
        )
        // Show the TimePicker dialog
        timePickerDialog.show()
    }
    private fun showAddTaskDialog() {
        // Create the BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the custom layout for the dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.task_input_dialog, null)

        // Set the custom layout to the dialog
        bottomSheetDialog.setContentView(dialogView)

        // Get references to the input fields and buttons
        val taskTitle = dialogView.findViewById<EditText>(R.id.taskTitle)
        val taskDescription = dialogView.findViewById<EditText>(R.id.taskDescription)
        val reminderButton = dialogView.findViewById<ImageButton>(R.id.add_reminder)
        val submitTaskButton = dialogView.findViewById<ImageButton>(R.id.submit_task)
        val priorityTaskButton = dialogView.findViewById<ImageButton>(R.id.add_priority)
        val tagTaskButton = dialogView.findViewById<ImageButton>(R.id.add_tag)
        val stateTaskButton = dialogView.findViewById<ImageButton>(R.id.add_state)

        var dateTime: String = ""
        var priority: Int = 0
        var tag: String = ""
        var state: String = ""

        reminderButton.setOnClickListener {
            showCalendarAndTimePicker { selectedDate ->
                dateTime = selectedDate
            }
        }

        tagTaskButton.setOnClickListener {
            showTagPopup { selectedTag ->
                tag = selectedTag
            }
        }

        priorityTaskButton.setOnClickListener {
            showPriorityPopup { selectedPriority ->
                priority = selectedPriority
            }
        }

        stateTaskButton.setOnClickListener{
            showStatePopup { SelectedState ->
                state = SelectedState

            }
        }

        // Handle submit task button click
        submitTaskButton.setOnClickListener {
            val title = taskTitle.text.toString()
            val description = taskDescription.text.toString()

            if (title.isNotEmpty()) {
                if (description.isNotEmpty()) {
                    if (dateTime.isNotEmpty()) {
                        if (tag.isNotEmpty()) {
                            if(priority != 0){
                                val currentUser = auth.currentUser
                                if (currentUser != null){
                                    val task = hashMapOf(
                                        "title" to title,
                                        "description" to description,
                                        "tag" to tag,
                                        "priority" to priority,
                                        "state" to state,
                                        "dueDate" to dateTime
                                    )

                                    val userId = currentUser.uid
                                    val db = FirebaseFirestore.getInstance()
                                    // Reference to a collection and add the data
                                    db.collection("users").document(userId).collection("tasks")
                                        .add(task)
                                        .addOnSuccessListener { documentReference ->
                                            val taskId = documentReference.id
                                            val updatedTask = hashMapOf(
                                                "title" to title,
                                                "description" to description,
                                                "tag" to tag,
                                                "priority" to priority,
                                                "dueDate" to dateTime,
                                                "state" to state,
                                                "taskId" to taskId
                                            )
                                            db.collection("users").document(userId).collection("tasks")
                                                .document(taskId)
                                                .set(updatedTask)
                                                .addOnSuccessListener {
                                                    Toast.makeText(this, "Task added with ID: $taskId", Toast.LENGTH_SHORT).show()
                                                    bottomSheetDialog.dismiss()  // Close the dialog
                                                    loadFragment(HomeFragment())
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(this, "Error updating task with ID: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    Log.d("FragmentDebug", e.message.toString())
                                                }
                                            Toast.makeText(this, "Task added with ID: ${documentReference.id}", Toast.LENGTH_SHORT).show()
                                            bottomSheetDialog.dismiss()
                                        }
                                        .addOnFailureListener { e ->
                                            // Handle failure
                                            Toast.makeText(this, "Error adding task: ${e.message}", Toast.LENGTH_SHORT).show()
                                            Log.d("FragmentDebug", e.message.toString())
                                        }
                                }
                            }else{
                                Toast.makeText(this, "Please select priority", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            Toast.makeText(this, "Please select a tag", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(this, "Please enter a date", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please enter a task description", Toast.LENGTH_SHORT)
                        .show()

                }
            } else {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show()
            }
        }
        // Show the dialog
        bottomSheetDialog.show()
    }

//    This made me go clinically insane
    private fun showTagPopup(onTagSelected: (String) -> Unit) {
        var selectedTag: String = ""  // Store selected tag

        // Inflate the custom layout for the priority dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tag, null)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val priorityGrid = dialogView.findViewById<GridLayout>(R.id.priorityGrid)
        val taskCategories = listOf("University", "Work", "Home", "Fitness", "Shopping", "Meetings", "Errands", "Friends", "Family", "Others")
        for (category in taskCategories) {
            val button = Button(this)
            button.text = category
            button.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            button.setOnClickListener {
                selectedTag = category
                Toast.makeText(this, "Tag selected: $category", Toast.LENGTH_SHORT).show()
            }
            priorityGrid.addView(button)
        }

        // Handle Cancel button
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            alertDialog.dismiss()  // Dismiss the dialog
        }

        // Handle Save button
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            if (selectedTag.isNotEmpty()) {
                // Return the selected priority via the callback
                onTagSelected(selectedTag)
                alertDialog.dismiss()  // Dismiss the dialog
            } else {
                // Show a message if no priority is selected
                Toast.makeText(this, "Please select a tag", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the dialog
        alertDialog.show()
    }

    private fun showPriorityPopup(onPrioritySelected: (Int) -> Unit) {
        var selectedPriority: Int = -1  // Store selected priority

        // Inflate the custom layout for the priority dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_priority, null)

        // Create the AlertDialog
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true) // Disable dismiss on outside touch
            .create()

        // Get references to buttons in the dialog layout
        val priorityButtons = listOf(
            dialogView.findViewById<Button>(R.id.priority1),
            dialogView.findViewById<Button>(R.id.priority2),
            dialogView.findViewById<Button>(R.id.priority3),
            dialogView.findViewById<Button>(R.id.priority4),
            dialogView.findViewById<Button>(R.id.priority5),
            dialogView.findViewById<Button>(R.id.priority6),
            dialogView.findViewById<Button>(R.id.priority7),
            dialogView.findViewById<Button>(R.id.priority8),
            dialogView.findViewById<Button>(R.id.priority9),
            dialogView.findViewById<Button>(R.id.priority10)
        )

        // Handle priority button clicks
        priorityButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedPriority = index + 1  // Priority 1-10 based on button click
                // Optionally highlight the selected button (not required for functionality)
                Toast.makeText(this, "Priority selected: $selectedPriority", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Cancel button
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            alertDialog.dismiss()  // Dismiss the dialog
        }

        // Handle Save button
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            if (selectedPriority != -1) {
                // Return the selected priority via the callback
                onPrioritySelected(selectedPriority)
                alertDialog.dismiss()  // Dismiss the dialog
            } else {
                // Show a message if no priority is selected
                Toast.makeText(this, "Please select a priority", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the dialog
        alertDialog.show()
    }

    private fun showStatePopup(onStateSelected: (String) -> Unit){
        var selectedState: String = ""

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_state, null)

        // Create the AlertDialog
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true) // Disable dismiss on outside touch
            .create()

        val priorityGrid = dialogView.findViewById<GridLayout>(R.id.priorityGrid)
        val taskStates = listOf("unfinished", "wip", "finished")
        for (state in taskStates) {
            val button = Button(this)
            button.text = state
            button.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            button.setOnClickListener {
                selectedState = state
                Toast.makeText(this, "State selected: $state", Toast.LENGTH_SHORT).show()
            }
            priorityGrid.addView(button)
        }

        // Handle Cancel button
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            alertDialog.dismiss()  // Dismiss the dialog
        }

        // Handle Save button
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            if (selectedState.isNotEmpty()) {
                // Return the selected priority via the callback
                onStateSelected(selectedState)
                alertDialog.dismiss()  // Dismiss the dialog
            } else {
                // Show a message if no priority is selected
                Toast.makeText(this, "Please select a state", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the dialog
        alertDialog.show()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
    }

    public fun callThisFun(){
//        // Create the BottomSheetDialog
//        val bottomSheetDialog = BottomSheetDialog(this)
//
//        // Inflate the custom layout for the dialog
//        val dialogView = LayoutInflater.from(this).inflate(R.layout.task_edit_dialog, null)
//
//        // Set the custom layout to the dialog
//        bottomSheetDialog.setContentView(dialogView)
    }
    companion object {

        fun showEditTaskDialog(taskId: String, title: String, description: String, dueDate: String, tag: String, priority: Int, state: String) {
            FragmentActivity().callThisFun()
        }
    }

}