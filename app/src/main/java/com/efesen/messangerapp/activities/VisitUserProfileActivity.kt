package com.efesen.messangerapp.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.efesen.messangerapp.R
import com.efesen.messangerapp.databinding.ActivityVisitUserProfileBinding
import com.efesen.messangerapp.modelClasses.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class VisitUserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVisitUserProfileBinding

    private var userVisitId : String = ""
    private var user: Users? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userVisitId = intent.getStringExtra("visit_id").toString()

        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(userVisitId)
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                     user = snapshot.getValue(Users::class.java)

                    binding.usernameDisplay.text = user!!.getUserName()
                    Picasso.get().load(user?.getProfile()).into(binding.profileDisplay)
                    Picasso.get().load(user?.getCover()).into(binding.coverDidplay)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        binding.facebookDisplay.setOnClickListener {
            val uri = Uri.parse(user?.getFacebook())

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        binding.instagramDisplay.setOnClickListener {
            val uri = Uri.parse(user?.getInstagram())

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        binding.websiteDisplay.setOnClickListener {
            val uri = Uri.parse(user?.getWebsite())

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        binding.sendMsgBtn.setOnClickListener {
            val intent = Intent(this@VisitUserProfileActivity, MessageChatActivity::class.java)
            intent.putExtra("visit_id", user?.getUID())
            startActivity(intent)
        }
    }

}