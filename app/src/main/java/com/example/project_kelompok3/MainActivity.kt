package com.example.project_kelompok3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        // Cek apakah user sudah login
        if (currentUser == null) {
            // Jika belum login, tampilkan onboarding Step1
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, Step1())  // Pastikan ID 'R.id.main' ada di activity_main.xml
                .commit()
        } else {
            // Jika sudah login, langsung ke halaman utama
            val intent = Intent(this, HomeFragment::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Fungsi untuk berpindah ke step berikutnya
    fun navigateToNextStep(currentStep: Int) {
        val nextFragment = when (currentStep) {
            1 -> Step2()  // Ke Step 2
            2 -> Step3()  // Ke Step 3
            3 -> Step4()  // Ke Step 4
            else -> {
                // Setelah Step 4, arahkan ke HomeFragment atau halaman utama
                val intent = Intent(this, HomeFragment::class.java)
                startActivity(intent)
                finish()
                return
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.main, nextFragment)  // Pastikan ID 'R.id.main' ada di layout activity_main.xml
            .addToBackStack(null)
            .commit()
    }

    // Fungsi untuk berpindah ke step sebelumnya
    fun navigateToPreviousStep(currentStep: Int) {
        val previousFragment = when (currentStep) {
            2 -> Step1()  // Kembali ke Step 1
            3 -> Step2()  // Kembali ke Step 2
            else -> return  // Jika tidak ada step sebelumnya, jangan lakukan apa-apa
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.main, previousFragment)  // Pastikan ID 'R.id.main' ada di layout activity_main.xml
            .addToBackStack(null)
            .commit()
        }
}