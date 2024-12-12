package com.example.project_kelompok3

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project_kelompok3.databinding.FragmentAppSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppSettingsFragment : Fragment() {

    private var _binding: FragmentAppSettingsBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppSettingsBinding.inflate(inflater, container, false)

        val user = auth.currentUser

        // Pre-select the current background color from Firebase
        user?.let {
            firestore.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    val currentColor = document.getString("background_color") ?: "#8692f7" // Default lavender
                    updateSelectedColor(currentColor)
                    applySystemBarColors(currentColor) // Update system bar color
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to fetch color preference", Toast.LENGTH_SHORT).show()
                }
        }

        // Set listeners for color selection
        binding.colorRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedColor = when (checkedId) {
                R.id.colorBlue -> "#7cd1e5"
                R.id.colorOrange -> "#e38e4d"
                R.id.colorGreen -> "#d6fd7f"
                R.id.colorDefault -> "#8692f7" // Default lavender
                else -> "#8692f7"
            }

            // Update Firebase and apply the new color dynamically
            user?.let {
                firestore.collection("users").document(it.uid)
                    .update("background_color", selectedColor)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Color preference saved!", Toast.LENGTH_SHORT).show()
                        applySystemBarColors(selectedColor) // Update the system bar dynamically
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save color preference", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        return binding.root
    }

    private fun updateSelectedColor(currentColor: String?) {
        when (currentColor) {
            "#7cd1e5" -> binding.colorBlue.isChecked = true
            "#e38e4d" -> binding.colorOrange.isChecked = true
            "#d6fd7f" -> binding.colorGreen.isChecked = true
            "#8692f7" -> binding.colorDefault.isChecked = true
        }
    }

    private fun applySystemBarColors(colorHex: String) {
        try {
            val color = Color.parseColor(colorHex)
            requireActivity().window.statusBarColor = color
            requireActivity().window.navigationBarColor = color
        } catch (e: IllegalArgumentException) {
            // Handle invalid color hex
            val defaultColor = Color.parseColor("#8692f7") // Default lavender
            requireActivity().window.statusBarColor = defaultColor
            requireActivity().window.navigationBarColor = defaultColor
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
