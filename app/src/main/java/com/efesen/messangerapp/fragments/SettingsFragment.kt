package com.efesen.messangerapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.efesen.messangerapp.databinding.FragmentSettingsBinding
import com.efesen.messangerapp.modelClasses.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso


class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private var userReference: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null
    private val RequestCode = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = null
    private var socialChecker: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = firebaseUser?.uid?.let {
                FirebaseDatabase.getInstance().reference.child("Users").child(it)
            }
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        userReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user : Users? = snapshot.getValue(Users::class.java)

                    if(context != null) {
                        binding.usernameSettings.text = user?.getUserName()
                        Picasso.get().load(user?.getProfile()).into(binding.profileImageSettings)
                        Picasso.get().load(user?.getCover()).into(binding.cover)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        binding.profileImageSettings.setOnClickListener {
            pickImage()
        }

        binding.cover.setOnClickListener {
            coverChecker = "cover"
            pickImage()
        }

        binding.setFacebook.setOnClickListener {
            socialChecker = "facebook"
            setSocialLinks()
        }

        binding.setInstagram.setOnClickListener {
            socialChecker = "instagram"
            setSocialLinks()
        }

        binding.setWebsite.setOnClickListener {
            socialChecker = "website"
            setSocialLinks()
        }
        return binding.root
    }

    private fun setSocialLinks() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context, androidx.appcompat.R.style.Theme_AppCompat_DayNight_Dialog_Alert)

        if (socialChecker == "website") {
            builder.setTitle("Write URL: ")
        } else {
            builder.setTitle("Write username: ")
        }
        val editText = EditText(context)

        if (socialChecker == "website") {
            editText.hint = "e.g www.google.com"
        } else {
            editText.hint = "dev_efe_"
        }
        builder.setView(editText)

        builder.setPositiveButton("Create", DialogInterface.OnClickListener {
            dialog, which ->
            val str = editText.text.toString()

            if (str == "") {
                Toast.makeText(context, "Please write something...", Toast.LENGTH_LONG).show()
            } else {
                saveSocialLink(str)
            }
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener {
                dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

    private fun saveSocialLink(str: String) {
        val mapSocial = HashMap<String, Any>()



        when(socialChecker) {
            "facebook" -> {
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }
            "instagram" -> {
                mapSocial["instagram"] = "https://m.instagram.com/$str"
            }

            "website" -> {
                mapSocial["website"] = "https://m.website.com/$str"
            }
        }
        userReference!!.updateChildren(mapSocial).addOnCompleteListener {
            task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "saved Successfully...", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pickImage() {

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, RequestCode)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data?.data != null) {
            imageUri = data.data
            if(imageUri != null) {
                Toast.makeText(context, "uploading...", Toast.LENGTH_LONG).show()
                uploadImageToDatabase()
            }else {
                Toast.makeText(context, "Image URI is null", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToDatabase() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Image is uploading, please wait...")
        progressBar.show()

        if (imageUri != null) {
            val fileRef = storageRef!!.child("${System.currentTimeMillis()}.jpg")

            val uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    progressBar.dismiss()
                    task.exception?.let { throw it }
                }
                fileRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result.toString()
                    println("Download URL: $downloadUrl")

                    if (coverChecker == "cover") {
                        val mapCoverImage =  HashMap<String, Any>()
                        mapCoverImage["cover"] = downloadUrl
                        userReference!!.updateChildren(mapCoverImage)
                        coverChecker = ""
                    } else {
                        val mapProfileImage = HashMap<String, Any>()
                        mapProfileImage["profile"] = downloadUrl
                        userReference!!.updateChildren(mapProfileImage)
                        coverChecker = ""
                    }
                    progressBar.dismiss()
                } else {
                    val errorMessage = task.exception?.message ?: "Unknown error"
                    Log.e("Upload Failed", errorMessage)
                    progressBar.dismiss()
                    Toast.makeText(context, "Upload failed, Please try again", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}