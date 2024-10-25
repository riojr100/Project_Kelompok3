package com.example.project_kelompok3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class Step4 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for Step 4
        return inflater.inflate(R.layout.activity_step4, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Define login button and create account button
        val loginButton: Button = view.findViewById(R.id.login_button)
        val createAccountButton: Button = view.findViewById(R.id.create_account_button)

        // When login button is clicked, navigate to LoginActivity
        loginButton.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()  // End onboarding process
        }

        // When create account button is clicked, navigate to SignUpActivity
        createAccountButton.setOnClickListener {
            val intent = Intent(activity, SignUpActivity::class.java)
            startActivity(intent)
            activity?.finish()  // End onboarding process
        }
    }
}
