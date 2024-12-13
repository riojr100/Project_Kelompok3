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
import com.bumptech.glide.Glide
import com.example.project_kelompok3.databinding.FragmentProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var tasksListener: ListenerRegistration? = null // Listener for tasks

    companion object {
        private const val GALLERY_REQUEST_CODE = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Initialize "Task Left" to 0
        binding.taskLeftTextView.text = "0 Task(s) left"

        // Load user profile and task data
        loadUserProfile()
        setupRealtimeTaskLeftListener()

        // Set up button listeners
        setupMenu()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // Remove Firestore listener when the view is destroyed
        tasksListener?.remove()
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("name") ?: "User"
                        val avatarBase64 = document.getString("avatarBase64")

                        binding.userNameTextView.text = userName
                        sharedPreferences.edit().putString("user_name", userName).apply()

                        if (!avatarBase64.isNullOrEmpty()) {
                            val avatarBitmap = decodeBase64ToBitmap(avatarBase64)
                            Glide.with(this)
                                .load(avatarBitmap)
                                .circleCrop()
                                .into(binding.profileImageView)
                        }
                    } else {
                        val defaultData = hashMapOf("name" to "User", "avatarBase64" to "")
                        firestore.collection("users").document(user.uid).set(defaultData)
                            .addOnSuccessListener {
                                binding.userNameTextView.text = "User"
                                sharedPreferences.edit().putString("user_name", "User").apply()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to initialize profile: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupRealtimeTaskLeftListener() {
        val user = auth.currentUser
        if (user != null) {
            val tasksRef = firestore.collection("users")
                .document(user.uid)
                .collection("tasks")

            // Remove any existing listener before setting up a new one
            tasksListener?.remove()

            tasksListener = tasksRef.addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error listening to tasks: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    val taskCount = querySnapshot.size()
                    binding.taskLeftTextView.text = "$taskCount Task(s) left"
                }
            }
        }
    }


    private fun setupMenu() {
        binding.appSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.flFragment, AppSettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.changeAccountName.setOnClickListener {
            val dialog = ChangeNameDialogFragment { newName ->
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
                    Toast.makeText(requireContext(), "Both fields must be filled!", Toast.LENGTH_SHORT).show()
                } else if (newPassword.length < 6) {
                    Toast.makeText(requireContext(), "New password must be at least 6 characters long!", Toast.LENGTH_SHORT).show()
                } else {
                    handlePasswordChange(oldPassword, newPassword, dialog)
                }
            }

            dialog.show()
        }

        binding.changeAccountImage.setOnClickListener {
            openGallery()
        }

        binding.faq.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("FAQ")
                .setMessage("Frequently Asked Questions will be available soon.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.aboutUs.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("About Us")
                .setMessage("This app was created by Team Kelompok3.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.helpFeedback.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Help & Feedback")
                .setMessage("This feature is under construction.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.supportUs.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Support Us")
                .setMessage("Support options will be added soon.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.logoutButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    // Clear listeners when logging out
                    tasksListener?.remove()
                    tasksListener = null

                    val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().remove("session_token").apply()

                    auth.signOut()

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
                Glide.with(this)
                    .load(bitmap)
                    .circleCrop()
                    .into(binding.profileImageView)
                uploadAvatarToFirestore(bitmap)
            }
        }
    }

    private fun uploadAvatarToFirestore(bitmap: Bitmap) {
        val user = auth.currentUser
        if (user != null) {
            val base64Avatar = encodeImageToBase64(bitmap)

            if (base64Avatar.length > 500_000) {
                Toast.makeText(requireContext(), "Avatar is too large to save. Please use a smaller image.", Toast.LENGTH_LONG).show()
                return
            }

            val userDocRef = firestore.collection("users").document(user.uid)

            userDocRef.update("avatarBase64", base64Avatar)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Avatar updated successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to save avatar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveUserName(newName: String) {
        val user = auth.currentUser
        if (user != null) {
            val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userDocRef = firestore.collection("users").document(user.uid)

            userDocRef.update("name", newName)
                .addOnSuccessListener {
                    sharedPreferences.edit().putString("user_name", newName).apply()
                    binding.userNameTextView.text = newName
                    Toast.makeText(requireContext(), "Name updated successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update name: ${e.message}", Toast.LENGTH_SHORT).show()
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
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
