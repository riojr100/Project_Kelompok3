package com.example.project_kelompok3

import ChangeNameDialogFragment
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.project_kelompok3.databinding.FragmentProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val GALLERY_REQUEST_CODE = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        loadUserProfile()
        setupMenu()

        return binding.root
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedName = sharedPreferences.getString("user_name", "User") // Default to "User"

        binding.userNameTextView.text = savedName

        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("name") ?: "User"
                        val avatarBase64 = document.getString("avatarBase64")

                        // Update UI and SharedPreferences
                        if (userName != savedName) {
                            sharedPreferences.edit().putString("user_name", userName).apply()
                            binding.userNameTextView.text = userName
                        }

                        // Decode and load avatar
                        if (!avatarBase64.isNullOrEmpty()) {
                            val avatarBitmap = decodeBase64ToBitmap(avatarBase64)
                            binding.profileImageView.setImageBitmap(avatarBitmap)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupMenu() {
        // Change Account Name
        binding.changeAccountName.setOnClickListener {
            val dialog = ChangeNameDialogFragment { newName: String ->
                saveUserName(newName)
            }
            dialog.show(parentFragmentManager, "ChangeNameDialog")
        }

        // Change Account Password
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
                    Toast.makeText(requireContext(), "Both fields must be filled!", Toast.LENGTH_SHORT).show()
                } else if (newPassword.length < 6) {
                    Toast.makeText(requireContext(), "New password must be at least 6 characters long!", Toast.LENGTH_SHORT).show()
                } else {
                    handlePasswordChange(oldPassword, newPassword, dialog)
                }
            }

            dialog.show()
        }

        // Change Account Image
        binding.changeAccountImage.setOnClickListener {
            openGallery()
        }

        // FAQ
        binding.faq.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("FAQ")
                .setMessage("Frequently Asked Questions will be available soon.")
                .setPositiveButton("OK", null)
                .show()
        }

        // About Us
        binding.aboutUs.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("About Us")
                .setMessage("This app was created by Team Kelompok3.")
                .setPositiveButton("OK", null)
                .show()
        }

        // Help & Feedback
        binding.helpFeedback.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Help & Feedback")
                .setMessage("This feature is under construction.")
                .setPositiveButton("OK", null)
                .show()
        }

        // Support Us
        binding.supportUs.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Support Us")
                .setMessage("Support options will be added soon.")
                .setPositiveButton("OK", null)
                .show()
        }

        // Logout
        binding.logoutButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().remove("session_token").apply()

                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                binding.profileImageView.setImageBitmap(bitmap) // Update UI
                uploadAvatarToFirestore(bitmap) // Save to Firestore
            }
        }
    }

    private fun uploadAvatarToFirestore(bitmap: Bitmap) {
        val user = auth.currentUser
        if (user != null) {
            val base64Avatar = encodeImageToBase64(bitmap)

            // Validate the size of the Base64 string
            if (base64Avatar.length > 500_000) { // Limit to ~500 KB
                Toast.makeText(requireContext(), "Avatar is too large to save. Please use a smaller image.", Toast.LENGTH_LONG).show()
                return
            }

            val userDocRef = firestore.collection("users").document(user.uid)

            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userDocRef.update("avatarBase64", base64Avatar)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Avatar updated successfully!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to save avatar: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        val userData = hashMapOf(
                            "name" to binding.userNameTextView.text.toString(),
                            "avatarBase64" to base64Avatar
                        )
                        userDocRef.set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Avatar saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to save avatar: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to access Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveUserName(newName: String) {
        val user = auth.currentUser
        if (user != null) {
            val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userDocRef = firestore.collection("users").document(user.uid)

            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userDocRef.update("name", newName)
                            .addOnSuccessListener {
                                sharedPreferences.edit().putString("user_name", newName).apply()
                                binding.userNameTextView.text = newName
                                Toast.makeText(requireContext(), "Name updated successfully!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to update name: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        val userData = hashMapOf(
                            "name" to newName,
                            "avatarBase64" to ""
                        )
                        userDocRef.set(userData)
                            .addOnSuccessListener {
                                sharedPreferences.edit().putString("user_name", newName).apply()
                                binding.userNameTextView.text = newName
                                Toast.makeText(requireContext(), "Name saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to save name: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to access Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun handlePasswordChange(oldPassword: String, newPassword: String, dialog: AlertDialog) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        if (email != null) {
            val credential = EmailAuthProvider.getCredential(email, oldPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(requireContext(), "Password successfully changed!", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                } else {
                                    Toast.makeText(requireContext(), "Failed to update password. Try again later.", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(requireContext(), "Old password is incorrect.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true) // Resize to 200x200
        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream) // Compress to 80%
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
