package com.example.project_kelompok3

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setelah beberapa detik, pindah ke Step1
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@MainActivity, Step1::class.java)
            startActivity(intent)
            finish()  // Tutup MainActivity agar pengguna tidak bisa kembali ke sini
        }, 2000) // 2000 ms = 2 detik
    }
}