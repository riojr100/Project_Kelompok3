package com.example.project_kelompok3

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Check if the app is being launched for the first time
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch) {
            // Show onboarding or logo screen
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, Step1()) // Ensure 'Step1' shows the onboarding or logo
                .commit()

            // Mark that the app has been launched for the first time
            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
        } else {
            handleUserNavigation()
        }
    }

    private fun handleUserNavigation() {
        val currentUser = auth.currentUser

        // Fetch and apply the saved background color to the system bar
        currentUser?.let {
            firestore.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    val backgroundColor = document.getString("background_color") ?: "#8692f7" // Default lavender
                    applySystemBarColors(backgroundColor)
                }
                .addOnFailureListener {
                    // Handle error fetching background color
                }
        }

        // Check if the user is logged in
        if (currentUser == null) {
            // Navigate to login or onboarding if not logged in
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, Step1()) // Ensure 'Step1' exists in activity_main.xml
                .commit()
        } else {
            // Navigate to the main fragment activity
            val intent = Intent(this, FragmentActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Function to apply system bar colors dynamically
    private fun applySystemBarColors(colorHex: String) {
        try {
            val color = Color.parseColor(colorHex)
            window.statusBarColor = color // Change the status bar color
            window.navigationBarColor = color // Change the navigation bar color
        } catch (e: IllegalArgumentException) {
            // If the colorHex is invalid, fallback to default
            val defaultColor = Color.parseColor("#8692f7") // Default lavender
            window.statusBarColor = defaultColor
            window.navigationBarColor = defaultColor
        }
    }

    // Function to navigate to the next step
    fun navigateToNextStep(currentStep: Int) {
        val nextFragment = when (currentStep) {
            1 -> Step2() // Go to Step 2
            2 -> Step3() // Go to Step 3
            3 -> Step4() // Go to Step 4
            else -> {
                // After Step 4, navigate to the main activity or home
                val intent = Intent(this, FragmentActivity::class.java)
                startActivity(intent)
                finish()
                return
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.main, nextFragment) // Ensure 'R.id.main' exists in activity_main.xml
            .addToBackStack(null)
            .commit()
    }

    // Function to navigate to the previous step
    fun navigateToPreviousStep(currentStep: Int) {
        val previousFragment = when (currentStep) {
            2 -> Step1() // Back to Step 1
            3 -> Step2() // Back to Step 2
            else -> return // If no previous step, do nothing
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.main, previousFragment) // Ensure 'R.id.main' exists in activity_main.xml
            .addToBackStack(null)
            .commit()
    }
}
