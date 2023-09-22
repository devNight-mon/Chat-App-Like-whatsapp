package com.efesen.messangerapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import com.efesen.messangerapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var mAuth: FirebaseAuth


    private lateinit var loginBackButton: AppCompatImageButton
    private lateinit var loginButton: AppCompatButton
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        bindUIComponent()

        handleButtonPress()
    }

    private fun bindUIComponent() {
        loginBackButton = binding.loginBackButton
        loginButton = binding.loginButton
        loginEmail = binding.emailLogin
        loginPassword = binding.passwordLogin
    }

    private fun handleButtonPress() {
        loginBackButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = loginEmail.text.toString()
        val password = loginPassword.text.toString()

         if (email == "") {
            Toast.makeText(this@LoginActivity, "please write email.", Toast.LENGTH_SHORT).show()
        }
        else if (password == "") {
            Toast.makeText(this@LoginActivity, "please write password.", Toast.LENGTH_SHORT).show()
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this@LoginActivity, "Error Message" + task.exception?.message.toString(), Toast.LENGTH_SHORT).show()

                    }
                }
         }
    }
}