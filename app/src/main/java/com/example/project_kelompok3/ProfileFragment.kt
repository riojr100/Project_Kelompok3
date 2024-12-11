package com.example.project_kelompok3

import ChangeNameDialogFragment
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.project_kelompok3.databinding.FragmentProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

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
        setupMenu()

        return binding.root
    }
    private fun saveUserName(newName: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("user_name", newName).apply()
        binding.userNameTextView.text = newName
    }

    private fun setupMenu() {
        binding.changeAccountName.setOnClickListener {
            val dialog = ChangeNameDialogFragment { newName: String ->
                saveUserName(newName)
            }
            dialog.show(parentFragmentManager, "ChangeNameDialog")
        }

        binding.changeAccountPassword.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
            val oldPasswordInput = dialogView.findViewById<EditText>(R.id.oldPasswordInput)
            val newPasswordInput = dialogView.findViewById<EditText>(R.id.newPasswordInput)

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Change Account Password")
                .setView(dialogView)
                .setCancelable(false)
                .create()

            dialogView.findViewById<View>(R.id.cancelButton).setOnClickListener {
                dialog.dismiss()
            }

            dialogView.findViewById<View>(R.id.submitButton).setOnClickListener {
                val oldPassword = oldPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()

                if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                    AlertDialog.Builder(requireContext())
                        .setMessage("Both fields must be filled!")
                        .setPositiveButton("OK", null)
                        .show()
                } else if (newPassword.length < 6) {
                    AlertDialog.Builder(requireContext())
                        .setMessage("The new password must be at least 6 characters long.")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    val user = FirebaseAuth.getInstance().currentUser
                    val email = user?.email

                    if (email != null) {
                        val credential = EmailAuthProvider.getCredential(email, oldPassword)

                        // Re-authenticate the user
                        user.reauthenticate(credential)
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    // Update password
                                    user.updatePassword(newPassword)
                                        .addOnCompleteListener { updateTask ->
                                            if (updateTask.isSuccessful) {
                                                AlertDialog.Builder(requireContext())
                                                    .setMessage("Password successfully changed!")
                                                    .setPositiveButton("OK") { _, _ -> dialog.dismiss() }
                                                    .show()
                                            } else {
                                                AlertDialog.Builder(requireContext())
                                                    .setMessage("Failed to update password. Try again later.")
                                                    .setPositiveButton("OK", null)
                                                    .show()
                                            }
                                        }
                                } else {
                                    AlertDialog.Builder(requireContext())
                                        .setMessage("Old password is incorrect.")
                                        .setPositiveButton("OK", null)
                                        .show()
                                }
                            }
                    }
                }
            }

            dialog.show()
        }

        binding.changeAccountImage.setOnClickListener {
            val options = arrayOf("Take Picture", "Import from Gallery")
            AlertDialog.Builder(requireContext())
                .setTitle("Change Account Image")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> openCamera()
                        1 -> openGallery()
                    }
                }
                .show()
        }

        binding.aboutUs.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("About Us")
                .setMessage("This is a sample application developed by Team Kelompok3.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.faq.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("FAQ")
                .setMessage("Frequently Asked Questions will be available soon.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.helpFeedback.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Help & Feedback")
                .setMessage("Help and feedback options are under construction.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.supportUs.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Support Us")
                .setMessage("Support options will be available soon.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.logoutButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.logout_confirmation_title))
                .setMessage(getString(R.string.logout_confirmation_message))
                .setPositiveButton(getString(R.string.logout_confirmation_yes)) { _, _ ->
                    val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().remove("session_token").apply()

                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton(getString(R.string.logout_confirmation_no), null)
                .show()
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), GALLERY_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            GALLERY_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    binding.profileImageView.setImageBitmap(imageBitmap)
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri: Uri? = data?.data
                    binding.profileImageView.setImageURI(selectedImageUri)
                }
            }
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val GALLERY_REQUEST_CODE = 101
        private const val CAMERA_PERMISSION_CODE = 102
        private const val GALLERY_PERMISSION_CODE = 103
    }
}
