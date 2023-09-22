package com.efesen.messangerapp.notifications


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService

/**
 * Created by Efe Şen on 20.09.2023.
 */
class MyFirebaseInstanceId: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            updateToken(token)
        }
    }

    private fun updateToken(token: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        if (firebaseUser != null) {
            ref.child(firebaseUser.uid).setValue(token)
        }

    }
}