package com.example.project_kelompok3

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.project_kelompok3.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Load user name
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "User")
        binding.userNameTextView.text = userName

        // Set task stats
        binding.taskLeftTextView.text = getString(R.string.task_left)
        binding.taskDoneTextView.text = getString(R.string.task_done)

        // Set up menu navigation
        setupMenu(sharedPreferences)

        return binding.root
    }

    private fun setupMenu(sharedPreferences: SharedPreferences) {
        binding.changeAccountName.setOnClickListener {
            // Open change account name dialog
            val dialog = ChangeNameDialogFragment { newName ->
                saveUserName(sharedPreferences, newName)
            }
            dialog.show(parentFragmentManager, "ChangeNameDialog")
        }

        binding.changeAccountPassword.setOnClickListener {
            // Navigate to change password functionality
            AlertDialog.Builder(requireContext())
                .setTitle("Change Password")
                .setMessage("This feature is under construction.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.changeAccountImage.setOnClickListener {
            // Navigate to change profile image functionality
            AlertDialog.Builder(requireContext())
                .setTitle("Change Profile Image")
                .setMessage("This feature is under construction.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.aboutUs.setOnClickListener {
            // Navigate to About Us page
            AlertDialog.Builder(requireContext())
                .setTitle("About Us")
                .setMessage("This is a sample application developed by Team Kelompok3.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.faq.setOnClickListener {
            // Navigate to FAQ page
            AlertDialog.Builder(requireContext())
                .setTitle("FAQ")
                .setMessage("Frequently Asked Questions will be available soon.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.helpFeedback.setOnClickListener {
            // Navigate to Help & Feedback page
            AlertDialog.Builder(requireContext())
                .setTitle("Help & Feedback")
                .setMessage("Help and feedback options are under construction.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.supportUs.setOnClickListener {
            // Navigate to Support Us page
            AlertDialog.Builder(requireContext())
                .setTitle("Support Us")
                .setMessage("Support options will be available soon.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.logoutButton.setOnClickListener {
            // Show confirmation dialog before logging out
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.logout_confirmation_title))
                .setMessage(getString(R.string.logout_confirmation_message))
                .setPositiveButton(getString(R.string.logout_confirmation_yes)) { _, _ ->
                    // Clear session data but keep the name
                    sharedPreferences.edit().remove("session_token").apply()

                    // Navigate to the login screen
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton(getString(R.string.logout_confirmation_no), null)
                .show()
        }
    }

    private fun saveUserName(sharedPreferences: SharedPreferences, newName: String) {
        sharedPreferences.edit().putString("user_name", newName).apply()
        binding.userNameTextView.text = newName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Dialog Fragment for changing user name
class ChangeNameDialogFragment(private val onNameChanged: (String) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_change_name, null)
        val nameInput = view.findViewById<EditText>(R.id.nameInput)

        builder.setView(view)
            .setTitle("Change Account Name")
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString()
                if (newName.isNotEmpty()) {
                    onNameChanged(newName)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        return builder.create()
    }
}
