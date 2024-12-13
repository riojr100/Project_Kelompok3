package com.example.project_kelompok3


import android.content.Context
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskEditDialog : BottomSheetDialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    private lateinit var taskButton: ImageButton
    private lateinit var taskTitle: TextView
    private lateinit var taskDesc: TextView
    private lateinit var taskTime: TextView
    private lateinit var taskTag: TextView
    private lateinit var taskPriority: TextView
    private lateinit var taskState: TextView
    private lateinit var submitButton: ImageButton


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the custom layout for the bottom sheet
        val view = inflater.inflate(R.layout.task_edit_dialog, container, false)

        taskButton = view.findViewById(R.id.imageButton2)

        taskTitle = view.findViewById(R.id.task_title)
        taskDesc = view.findViewById(R.id.task_desc)
        taskTime = view.findViewById(R.id.edit_time_button)
        taskTag = view.findViewById(R.id.edit_tag_button)
        taskPriority = view.findViewById(R.id.edit_priority_button)
        taskState = view.findViewById(R.id.edit_state_button)

        submitButton = view.findViewById(R.id.submit_task)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUserUid = auth.currentUser?.uid
//        if (currentUserUid == null){
//
//        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isAdded) return
        // Retrieve arguments passed to the dialog
        val taskId = arguments?.getString("id")
        val _title = arguments?.getString("title")
        val _desc = arguments?.getString("desc")
        val _due = arguments?.getString("due")
        val _tag = arguments?.getString("tag")
        val _priority = arguments?.getInt("priority")
        val _state = arguments?.getString("state")

        taskTitle.text = _title
        taskDesc.text = _desc
        taskTime.text = _due
        taskTag.text = _tag
        taskPriority.text = _priority.toString()
        taskState.text = _state

        var title: String = _title ?: "No Title"
        var desc: String = _desc ?: "No Desc"
        var dateTime: String = _due ?: "No Due"
        var priority: Int = _priority?.toInt() ?: 0
        var tag: String = _tag ?: "No Tag"
        var state: String = _state ?: "No State"


        taskTime.setOnClickListener {
            showCalendarAndTimePicker { selectedDate ->
                dateTime = selectedDate
                taskTime.text = dateTime
            }
        }

        taskTag.setOnClickListener {
            showTagPopup { selectedTag ->
                tag = selectedTag
                taskTag.text = tag
            }
        }

        taskPriority.setOnClickListener {
            showPriorityPopup { selectedPriority ->
                priority = selectedPriority
                taskPriority.text = priority.toString()
            }
        }

        taskState.setOnClickListener {
            showStatePopup { selectedState ->
                state = selectedState
                taskState.text = state
            }
        }

        taskButton.setOnClickListener{
            showTaskNameDescPopup(title, desc){ Title, Desc ->
                title = Title
                desc = Desc
                taskTitle.setText(Title)
                taskDesc.setText(Desc)
            }
        }

        submitButton.setOnClickListener{
            val currentUserUid = auth.currentUser?.uid
            if (currentUserUid != null){
                val userDocRef = db.collection("users").document(currentUserUid).collection("tasks").document(taskId.toString())
                val updates = mapOf(
                    "title" to title,
                    "description" to desc,
                    "tag" to tag,
                    "priority" to priority,
                    "dueDate" to dateTime,
                    "state" to state
                )
                userDocRef.update(updates).addOnSuccessListener {
//                    Log.d("DBUpdate","Database Successfully Updated")
                    Toast.makeText(requireContext(), "Database Successfully Updated", Toast.LENGTH_SHORT).show()
                    dismiss()
                }.addOnFailureListener{ e ->
                    Log.e("DBError", "Database Failed to Update")
                }
            }else{
                Log.e("Auth", "Authentication Error")
            }
        }

        // Handle button clicks or other UI interactions here
        val button = view.findViewById<ImageButton>(R.id.back_edit)
        button.setOnClickListener {
            dismiss() // Close the bottom sheet
        }
    }

    //    ============
    private fun showCalendarAndTimePicker(onDateTimeSelected: (String) -> Unit) {
        if (isAdded) {
            val calendar = Calendar.getInstance()
            // DatePickerDialog to pick the date
            val datePickerDialog = DatePickerDialog(
                requireContext(), { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
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
    }

    private fun showTimePicker(calendar: Calendar, onTimeSelected: (String) -> Unit) {
        if (!isAdded) return
        val timePickerDialog = TimePickerDialog(
            requireContext(), { _, hourOfDay: Int, minute: Int ->
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
        if (!isAdded) return
        // Create the BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        // Inflate the custom layout for the dialog
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.task_input_dialog, null)

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


        priorityTaskButton.setOnClickListener {
            showPriorityPopup { selectedPriority ->
                priority = selectedPriority
            }
        }

        stateTaskButton.setOnClickListener {
            showStatePopup { SelectedState ->
                state = SelectedState

            }
        }

        // Handle submit task button click
        submitTaskButton.setOnClickListener {
            val title = taskTitle.text.toString()
            val description = taskDescription.text.toString()

//            if (title.isNotEmpty()) {
//                if (description.isNotEmpty()) {
//                    if (dateTime.isNotEmpty()) {
//                        if (tag.isNotEmpty()) {
//                            if(priority != 0){
//                                val currentUser = auth.currentUser
//                                if (currentUser != null){
//                                    val task = hashMapOf(
//                                        "title" to title,
//                                        "description" to description,
//                                        "tag" to tag,
//                                        "priority" to priority,
//                                        "state" to state,
//                                        "dueDate" to dateTime
//                                    )
//
//                                    val userId = currentUser.uid
//                                    val db = FirebaseFirestore.getInstance()
//                                    // Reference to a collection and add the data
//                                    db.collection("users").document(userId).collection("tasks")
//                                        .add(task)
//                                        .addOnSuccessListener { documentReference ->
//                                            val taskId = documentReference.id
//                                            val updatedTask = hashMapOf(
//                                                "title" to title,
//                                                "description" to description,
//                                                "tag" to tag,
//                                                "priority" to priority,
//                                                "dueDate" to dateTime,
//                                                "state" to state,
//                                                "taskId" to taskId
//                                            )
//                                            db.collection("users").document(userId).collection("tasks")
//                                                .document(taskId)
//                                                .set(updatedTask)
//                                                .addOnSuccessListener {
//                                                    Toast.makeText(requireContext(), "Task added with ID: $taskId", Toast.LENGTH_SHORT).show()
//                                                    bottomSheetDialog.dismiss()  // Close the dialog
//                                                }
//                                                .addOnFailureListener { e ->
//                                                    Toast.makeText(requireContext(), "Error updating task with ID: ${e.message}", Toast.LENGTH_SHORT).show()
//                                                    Log.d("FragmentDebug", e.message.toString())
//                                                }
//                                            Toast.makeText(requireContext(), "Task added with ID: ${documentReference.id}", Toast.LENGTH_SHORT).show()
//                                            bottomSheetDialog.dismiss()
//                                        }
//                                        .addOnFailureListener { e ->
//                                            // Handle failure
//                                            Toast.makeText(requireContext(), "Error adding task: ${e.message}", Toast.LENGTH_SHORT).show()
//                                            Log.d("FragmentDebug", e.message.toString())
//                                        }
//                                }
//                            }else{
//                                Toast.makeText(requireContext(), "Please select priority", Toast.LENGTH_SHORT)
//                                    .show()
//                            }
//                        } else {
//                            Toast.makeText(requireContext(), "Please select a tag", Toast.LENGTH_SHORT)
//                                .show()
//                        }
//                    } else {
//                        Toast.makeText(requireContext(), "Please enter a date", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(requireContext(), "Please enter a task description", Toast.LENGTH_SHORT)
//                        .show()
//
//                }
//            } else {
//                Toast.makeText(requireContext(), "Please enter a task title", Toast.LENGTH_SHORT).show()
//            }
        }
        // Show the dialog
        bottomSheetDialog.show()
    }

    private fun showTaskNameDescPopup(
        initialTaskName: String,
        initialTaskDesc: String,
        onTaskNameChanged: (String, String) -> Unit
    ) {
        if (!isAdded) return
        var taskName: String = initialTaskName
        var taskDesc: String = initialTaskDesc

        // Inflate the custom layout for the priority dialog
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_task_name_and_desc, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        alertDialog.show()

        val taskNameField = dialogView.findViewById<EditText>(R.id.task_name_input)
        val taskDescField = dialogView.findViewById<EditText>(R.id.task_desc_input)

        taskNameField.setText(taskName)
        taskDescField.setText(taskDesc)

        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            taskName = taskNameField.text.toString()
            taskDesc = taskDescField.text.toString()

            onTaskNameChanged(taskName, taskDesc)
            alertDialog.dismiss()
        }

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener{
            alertDialog.dismiss()
        }

    }

    //    This made me go clinically insane
    private fun showTagPopup(onTagSelected: (String) -> Unit) {
        if (!isAdded) return
        var selectedTag: String = ""  // Store selected tag

        // Inflate the custom layout for the priority dialog
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_tag, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val priorityGrid = dialogView.findViewById<GridLayout>(R.id.priorityGrid)
        val taskCategories = listOf(
            "University",
            "Work",
            "Home",
            "Fitness",
            "Shopping",
            "Meetings",
            "Errands",
            "Friends",
            "Family",
            "Others"
        )
        for (category in taskCategories) {
            val button = Button(requireContext())
            button.text = category
            button.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            button.setOnClickListener {
                selectedTag = category
                Toast.makeText(requireContext(), "Tag selected: $category", Toast.LENGTH_SHORT)
                    .show()
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
                Toast.makeText(requireContext(), "Please select a tag", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the dialog
        alertDialog.show()
    }

    private fun showPriorityPopup(onPrioritySelected: (Int) -> Unit) {
        var selectedPriority: Int = -1  // Store selected priority

        // Inflate the custom layout for the priority dialog
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_priority, null)

        // Create the AlertDialog
        val alertDialog = AlertDialog.Builder(requireContext())
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
                Toast.makeText(
                    requireContext(),
                    "Priority selected: $selectedPriority",
                    Toast.LENGTH_SHORT
                ).show()
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
                Toast.makeText(requireContext(), "Please select a priority", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Show the dialog
        alertDialog.show()
    }

    private fun showStatePopup(onStateSelected: (String) -> Unit) {
        if (!isAdded) return
        var selectedState: String = ""

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_state, null)

        // Create the AlertDialog
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true) // Disable dismiss on outside touch
            .create()

        val priorityGrid = dialogView.findViewById<GridLayout>(R.id.priorityGrid)
        val taskStates = listOf("unfinished", "wip", "finished")
        for (state in taskStates) {
            val button = Button(requireContext())
            button.text = state
            button.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            button.setOnClickListener {
                selectedState = state
                Toast.makeText(requireContext(), "State selected: $state", Toast.LENGTH_SHORT)
                    .show()
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
                Toast.makeText(requireContext(), "Please select a state", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the dialog
        alertDialog.show()
    }

    //    ============
    companion object {
        fun newInstance(
            id: String,
            title: String,
            desc: String,
            due: String,
            tag: String,
            priority: Int,
            state: String
        ): TaskEditDialog {
            val dialog = TaskEditDialog()
            val args = Bundle()
            args.putString("id", id)
            args.putString("title", title)
            args.putString("desc", desc)
            args.putString("due", due)
            args.putString("tag", tag)
            args.putInt("priority", priority)
            args.putString("state", state)
            dialog.arguments = args
            return dialog
        }
    }
}