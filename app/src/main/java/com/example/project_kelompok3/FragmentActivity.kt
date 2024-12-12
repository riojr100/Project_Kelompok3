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

class FragmentActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var addTaskButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            when (item.itemId) {
                R.id.nav_home -> selectedFragment = homeFragment
                R.id.nav_calendar -> selectedFragment = calendarFragment
                R.id.nav_focus -> selectedFragment = focusFragment
                R.id.nav_profile -> selectedFragment = profileFragment
            }
            selectedFragment?.let { loadFragment(it) }
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

    private fun showAddTaskDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.task_input_dialog, null)
        bottomSheetDialog.setContentView(dialogView)

        val taskTitle = dialogView.findViewById<EditText>(R.id.taskTitle)
        val taskDescription = dialogView.findViewById<EditText>(R.id.taskDescription)
        val reminderButton = dialogView.findViewById<ImageButton>(R.id.add_reminder)
        val submitTaskButton = dialogView.findViewById<ImageButton>(R.id.submit_task)
        val priorityTaskButton = dialogView.findViewById<ImageButton>(R.id.add_priority)
        val tagTaskButton = dialogView.findViewById<ImageButton>(R.id.add_tag)

        var dateTime: String = ""
        var priority: Int = 0
        var tag: String = ""

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

        submitTaskButton.setOnClickListener {
            val title = taskTitle.text.toString()
            val description = taskDescription.text.toString()

            if (title.isNotEmpty() && description.isNotEmpty() && dateTime.isNotEmpty() && tag.isNotEmpty() && priority != 0) {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val task = hashMapOf(
                        "title" to title,
                        "description" to description,
                        "tag" to tag,
                        "priority" to priority,
                        "dueDate" to dateTime
                    )
                    val userId = currentUser.uid
                    firestore.collection("users").document(userId).collection("tasks")
                        .add(task)
                        .addOnSuccessListener { documentReference ->
                            val taskId = documentReference.id
                            val updatedTask = task + ("taskId" to taskId)
                            firestore.collection("users").document(userId).collection("tasks")
                                .document(taskId)
                                .set(updatedTask)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Task added successfully!", Toast.LENGTH_SHORT).show()
                                    bottomSheetDialog.dismiss()
                                    loadFragment(HomeFragment())
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FragmentActivity", "Error updating task: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FragmentActivity", "Error adding task: ${e.message}")
                        }
                }
            } else {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }

    private fun showCalendarAndTimePicker(onDateTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                showTimePicker(calendar) { dateTime ->
                    onDateTimeSelected(dateTime)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(calendar: Calendar, onTimeSelected: (String) -> Unit) {
        val timePickerDialog = TimePickerDialog(
            this, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val formattedDateTime = dateTimeFormat.format(calendar.time)
                onTimeSelected(formattedDateTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun showTagPopup(onTagSelected: (String) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tag, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        val priorityGrid = dialogView.findViewById<GridLayout>(R.id.priorityGrid)
        val taskCategories = listOf("University", "Work", "Home", "Fitness", "Shopping", "Meetings", "Errands", "Friends", "Family", "Others")

        for (category in taskCategories) {
            val button = Button(this).apply {
                text = category
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setOnClickListener {
                    onTagSelected(category)
                    alertDialog.dismiss()
                }
            }
            priorityGrid.addView(button)
        }

        dialogView.findViewById<Button>(R.id.cancelButton)?.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun showPriorityPopup(onPrioritySelected: (Int) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_priority, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()

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

        priorityButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                onPrioritySelected(index + 1)
                alertDialog.dismiss()
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton)?.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
    }
}
