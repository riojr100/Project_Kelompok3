package com.example.project_kelompok3

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FocusModeActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var startPauseButton: Button
    private lateinit var stopButton: Button
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
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        timerText.text = timeFormatted
    }
}



