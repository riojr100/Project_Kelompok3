package com.example.project_kelompok3

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.*

class FocusFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_focus, container, false)

        timerText = view.findViewById(R.id.timerText)
        progressBar = view.findViewById(R.id.progressBar)
        startPauseButton = view.findViewById(R.id.startPauseButton)
        stopButton = view.findViewById(R.id.stopButton)
        editTimerButton = view.findViewById(R.id.editTimerButton)

        progressBar.max = 100 // Full progress

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

        editTimerButton.setOnClickListener {
            showEditTimerDialog()
        }

        updateCountDownText()

        return view
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()

                // Calculate progress as a float to ensure smooth decrement
                val progress = ((millisUntilFinished.toFloat() / focusTimeInMillis.toFloat()) * 100).toInt()

                // Ensure progress is smoothly updated and reaches 0 at the right time
                progressBar.progress = if (progress >= 0) progress else 0
            }

            override fun onFinish() {
                isTimerRunning = false
                isTimerPaused = false
                startPauseButton.text = getString(R.string.start_focusing)
                stopButton.visibility = View.GONE
                progressBar.progress = 0 // Set progress to 0 when finished
            }
        }.start()

        isTimerRunning = true
        isTimerPaused = false
        startPauseButton.text = getString(R.string.pause)
        stopButton.visibility = View.VISIBLE
    }

    private fun pauseTimer() {
        countDownTimer.cancel()
        isTimerRunning = false
        isTimerPaused = true
        startPauseButton.text = getString(R.string.resume)
    }

    private fun stopTimer() {
        countDownTimer.cancel()
        isTimerRunning = false
        isTimerPaused = false
        timeLeftInMillis = focusTimeInMillis
        updateCountDownText()

        progressBar.progress = 100
        startPauseButton.text = getString(R.string.start_focusing)
        stopButton.visibility = View.GONE
    }

    private fun updateCountDownText() {
        val hours = (timeLeftInMillis / 1000) / 3600
        val minutes = ((timeLeftInMillis / 1000) % 3600) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
        timerText.text = timeFormatted
    }

    private fun showEditTimerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_timer, null)
        val hourPicker = dialogView.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker = dialogView.findViewById<NumberPicker>(R.id.minutePicker)
        val secondPicker = dialogView.findViewById<NumberPicker>(R.id.secondPicker)

        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        secondPicker.minValue = 0
        secondPicker.maxValue = 59

        AlertDialog.Builder(requireContext())
            .setTitle("Set Timer")
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                val hours = hourPicker.value
                val minutes = minutePicker.value
                val seconds = secondPicker.value
                focusTimeInMillis = ((hours * 3600) + (minutes * 60) + seconds) * 1000L
                timeLeftInMillis = focusTimeInMillis

                updateCountDownText()
                progressBar.progress = 100
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
