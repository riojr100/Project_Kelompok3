package com.example.project_kelompok3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class Step2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_step2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Connect the buttons with their IDs
        val nextButton: Button = view.findViewById(R.id.next_button)
        val backButton: Button = view.findViewById(R.id.back_button)

        // Next button click listener
        nextButton.setOnClickListener {
            (activity as MainActivity).navigateToNextStep(2) // Pindah ke Step 3
        }

        // Back button click listener
        backButton.setOnClickListener {
            (activity as MainActivity).navigateToPreviousStep(2) // Kembali ke Step 1
        }
    }
}
