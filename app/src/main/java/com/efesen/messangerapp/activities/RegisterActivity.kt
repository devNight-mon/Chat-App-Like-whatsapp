package com.efesen.messangerapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import com.efesen.messangerapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserId: String = ""

    private lateinit var registerBackButton: AppCompatImageButton
    private lateinit var registerButton: AppCompatButton
    private lateinit var userNameEditText: EditText
    private lateinit var registerEmail: EditText
    private lateinit var registerPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        bindUIComponent()

        handleButtonPress()


    }

    private fun bindUIComponent() {
        registerBackButton = binding.registerBackButton
        registerButton = binding.registerButton
        userNameEditText = binding.userNameRegister
        registerEmail = binding.emailRegister
        registerPassword = binding.passwordRegister
    }

    private fun handleButtonPress() {
        registerBackButton.setOnClickListener {
            val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val userName = userNameEditText.text.toString()
        val userRegisterEmail = registerEmail.text.toString()
        val userRegisterPassword = registerPassword.text.toString()

        if(userName == "") {
            Toast.makeText(this, "please write username.", Toast.LENGTH_SHORT).show()
        }
        else if (userRegisterEmail == "") {
            Toast.makeText(this, "please write email.", Toast.LENGTH_SHORT).show()
        }
        else if (userRegisterPassword == "") {
            Toast.makeText(this, "please write password.", Toast.LENGTH_SHORT).show()
        }
         else {
             mAuth.createUserWithEmailAndPassword(userRegisterEmail, userRegisterPassword).addOnCompleteListener { task ->
                 if(task.isSuccessful) {

                     firebaseUserId = mAuth.currentUser!!.uid
                     refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserId)

                     val userHashMap = HashMap<String, Any>()
                     userHashMap["uid"] = firebaseUserId
                     userHashMap["username"] = userName
                     userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/messengerapp-b9966.appspot.com/o/profile_photo.png?alt=media&token=ed8f5509-bcb5-4e60-92cf-f62bab56f9dc"
                     userHashMap["cover"] =   "https://firebasestorage.googleapis.com/v0/b/messengerapp-b9966.appspot.com/o/cover_photo.jpeg?alt=media&token=72243236-b435-4f9c-84ae-aab926a25e81"
                     userHashMap["status"] = "offline"
                     userHashMap["search"] = userName.toLowerCase()
                     userHashMap["facebook"] = "https://efesen.facebook.com"
                     userHashMap["instagram"] = "https://efe.instagram.com"
                     userHashMap["website"] = "https://www.google.com"

                     refUsers.updateChildren(userHashMap)
                         .addOnCompleteListener { task ->
                             if (task.isSuccessful) {
                                 val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                 startActivity(intent)
                                 finish()
                             }
                         }

                 } else {
                     Toast.makeText(this, "Error Message" + task.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                 }
             }
        }
    }
}