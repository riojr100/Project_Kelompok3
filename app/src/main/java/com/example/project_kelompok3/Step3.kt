package com.example.project_kelompok3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class Step3 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_step3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Connect the buttons with their IDs
        val getStartedButton: Button = view.findViewById(R.id.get_started_button) // Sesuaikan dengan ID di XML
        val backButton: Button = view.findViewById(R.id.back_button)

        // Get Started button click listener
        getStartedButton.setOnClickListener {
            (activity as MainActivity).navigateToNextStep(3) // Pindah ke Step 4
        }

        // Back button click listener
        backButton.setOnClickListener {
            (activity as MainActivity).navigateToPreviousStep(3) // Kembali ke Step 2
        }
    }
}
