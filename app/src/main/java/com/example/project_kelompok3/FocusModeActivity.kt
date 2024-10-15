package com.example.project_kelompok3

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FocusModeActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var startPauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var editTimerButton: Button
    private var focusTimeInMillis: Long = 3600000 // Default 1 hour
    private lateinit var countDownTimer: CountDownTimer
    private var isTimerRunning: Boolean = false
    private var isTimerPaused: Boolean = false
    private var timeLeftInMillis: Long = focusTimeInMillis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus_mode)

        timerText = findViewById(R.id.timerText)
        progressBar = findViewById(R.id.progressBar)
        startPauseButton = findViewById(R.id.startPauseButton)
        stopButton = findViewById(R.id.stopButton)
        editTimerButton = findViewById(R.id.editTimerButton)

        progressBar.max = 100 // Full progress

        // Set initial visibility of Stop button to GONE
        stopButton.visibility = View.GONE

        startPauseButton.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        stopButton.setOnClickListener {
            stopTimer()
        }

        // Add the listener for the Edit Timer button
        editTimerButton.setOnClickListener {
            showEditTimerDialog()
        }

        updateCountDownText()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()

                // Update circular progress bar
                val progress = ((timeLeftInMillis.toDouble() / focusTimeInMillis.toDouble()) * 100).toInt()
                progressBar.progress = progress
            }

            override fun onFinish() {
                isTimerRunning = false
                isTimerPaused = false
                startPauseButton.text = "Start Focusing"
                stopButton.visibility = View.GONE // Hide the Stop button after the timer finishes
            }
        }.start()

        isTimerRunning = true
        isTimerPaused = false
        startPauseButton.text = "Pause"
        stopButton.visibility = View.VISIBLE // Show the Stop button when the timer starts
    }

    private fun pauseTimer() {
        countDownTimer.cancel()
        isTimerRunning = false
        isTimerPaused = true
        startPauseButton.text = "Resume"
    }

    private fun stopTimer() {
        countDownTimer.cancel()
        isTimerRunning = false
        isTimerPaused = false
        timeLeftInMillis = focusTimeInMillis // Reset the timer
        updateCountDownText()

        // Reset the progress bar
        progressBar.progress = 100
        startPauseButton.text = "Start Focusing"
        stopButton.visibility = View.GONE // Hide the Stop button when timer is stopped
    }

    private fun updateCountDownText() {
        val hours = (timeLeftInMillis / 1000) / 3600
        val minutes = ((timeLeftInMillis / 1000) % 3600) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted: String

        // If hours is greater than 0, include hours in the format
        timeFormatted = if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }

        timerText.text = timeFormatted
    }

    // Show the dialog to input hours, minutes, and seconds
    private fun showEditTimerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_timer, null)
        val hourPicker = dialogView.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker = dialogView.findViewById<NumberPicker>(R.id.minutePicker)
        val secondPicker = dialogView.findViewById<NumberPicker>(R.id.secondPicker)

        // Set picker ranges
        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        secondPicker.minValue = 0
        secondPicker.maxValue = 59

        // Create and show the dialog
        AlertDialog.Builder(this)
            .setTitle("Set Timer")
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                // Get the values from the NumberPickers
                val hours = hourPicker.value
                val minutes = minutePicker.value
                val seconds = secondPicker.value

                // Convert to milliseconds
                focusTimeInMillis = ((hours * 3600) + (minutes * 60) + seconds) * 1000L
                timeLeftInMillis = focusTimeInMillis

                // Update the timer text and progress bar
                updateCountDownText()
                progressBar.progress = 100
            }
            .setNegativeButton("Cancel", null) // Dismiss the dialog if "Cancel" is pressed
            .show()
    }
}
