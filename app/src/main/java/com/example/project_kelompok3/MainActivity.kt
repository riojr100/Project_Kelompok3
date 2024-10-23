package com.example.project_kelompok3

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        // Setelah beberapa detik, pindah ke Step1
        Handler(Looper.getMainLooper()).postDelayed({
            if (currentUser == null){
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this@MainActivity, FragmentActivity::class.java)
                startActivity(intent)
                finish()  // Tutup MainActivity agar pengguna tidak bisa kembali ke sini
            }

        }, 2000) // 2000 ms = 2 detik
    }
}