package com.efesen.messangerapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.efesen.messangerapp.R
import com.efesen.messangerapp.adapter.ChatsAdapter
import com.efesen.messangerapp.databinding.ActivityMessageChatBinding
import com.efesen.messangerapp.interfaces.APIService
import com.efesen.messangerapp.constants.Constants
import com.efesen.messangerapp.modelClasses.Chat
import com.efesen.messangerapp.modelClasses.Users
import com.efesen.messangerapp.notifications.Client
import com.efesen.messangerapp.notifications.Data
import com.efesen.messangerapp.notifications.MyResponse
import com.efesen.messangerapp.notifications.Sender
import com.efesen.messangerapp.notifications.Token
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageChatBinding

    private lateinit var sendButton: ImageView
    private lateinit var attachButton: ImageView
    private lateinit var userNameTv: TextView
    private lateinit var userProfileImg:ImageView
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatEditText: EditText

    private var userIdVisit: String = ""
    private var firebaseUser: FirebaseUser? = null
    var chatsAdapter: ChatsAdapter? = null
    var mChatList: List<Chat>?   = null
    private var reference: DatabaseReference? = null

    private var notify = false
    private var apiService: APIService? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityMessageChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindUIComponents()

        setSupportActionBar(binding.toolbarChat)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbarChat.setNavigationOnClickListener {
            finish()
        }

        apiService = Client.Client.getClient(Constants.BASE_URL)!!.create(APIService::class.java)

        intent = intent
        userIdVisit = intent.getStringExtra("visit_id").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        chatRecyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
            linearLayoutManager.stackFromEnd = true
        chatRecyclerView.layoutManager = linearLayoutManager

        val reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val user: Users? = snapshot.getValue(Users::class.java)
                userNameTv.text = user?.getUserName()
                Picasso.get().load(user?.getProfile()).into(userProfileImg)

                retrieveMessages(firebaseUser!!.uid, userIdVisit, user!!.getProfile())
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        handleButtonPress()
    }

    private fun bindUIComponents() {

        sendButton = binding.sendMessageBtn
        attachButton = binding.attachImageFileBtn
        userNameTv = binding.userNameChat
        userProfileImg = binding.profileImageChat
        chatRecyclerView = binding.chatsRecyclerview
        chatEditText = binding.textMessage
    }

    private fun handleButtonPress() {
        sendButton.setOnClickListener {
            notify = true
            val message = chatEditText.text.toString()
            if (message == "") {
                Toast.makeText(this@MessageChatActivity, "Please write a message first...", Toast.LENGTH_SHORT).show()
            } else {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            chatEditText.setText("")
        }

        attachButton.setOnClickListener {
            notify = true
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 438)
        }
        seenMessage(userIdVisit)
    }


    private fun sendMessageToUser(senderId: String, receiverId: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey

        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatsListReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)

                    chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                chatsListReference.child("id").setValue(userIdVisit)
                            }
                            val chatsListReceiverReference = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatList")
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid)
                            chatsListReceiverReference.child("id").setValue(firebaseUser!!.uid)
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                }
            }
        //implement the push notifications using firebase fcm

        val userReference = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser!!.uid)
        userReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val user = snapshot.getValue(Users::class.java)
                if (notify) {
                    sendNotification(receiverId, user!!.getUserName(), message)
                }
                notify = false
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun sendNotification(receiverId: String, userName: String?, message: String) {

        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val token: Token? = dataSnapshot.getValue(Token::class.java)

                    val data = Data(
                        firebaseUser!!.uid,
                        R.mipmap.ic_launcher,
                        "$userName: $message",
                        "New Message",
                        userIdVisit
                    )
                    val sender = Sender(data,token!!.getToken().toString())
                    apiService!!.sendNotification(sender)
                        .enqueue(object : Callback<MyResponse>{
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                if(response.code() == 200) {
                                    if (response.body()!!.success !== 1) {
                                        Toast.makeText(this@MessageChatActivity, "Failed, Nothing happened", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {

                            }

                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.data != null) {
            val progressBar =  ProgressDialog(this)
            progressBar.setMessage("image is uploading, please wait....")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            val uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {

                        Log.e("Upload Failed", it.toString())
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId

                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)

                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                progressBar.dismiss()

                                //implement the push notifications using firebase fcm
                                val reference = FirebaseDatabase.getInstance().reference
                                    .child("Users").child(firebaseUser!!.uid)
                                reference.addValueEventListener(object: ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {

                                        val user = snapshot.getValue(Users::class.java)
                                        if (notify) {
                                            sendNotification(userIdVisit, user!!.getUserName(), "sent you an image.")
                                        }
                                        notify = false
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }

                                })
                            }
                        }
                }
            }
        }
    }

    private fun retrieveMessages(senderId: String, receiverId: String, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (p0 in snapshot.children) {
                    val chat = p0.getValue(Chat::class.java)

                    if (chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                        || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId)) {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatsAdapter = ChatsAdapter((mChatList as ArrayList<Chat>), receiverImageUrl!!)
                    chatRecyclerView.adapter = chatsAdapter

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    var seenListener: ValueEventListener? = null
    private fun seenMessage(userId: String) {
         reference = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListener = reference!!.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userId)) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun onPause() {
        super.onPause()
        reference!!
        seenListener?.let {
            reference?.removeEventListener(it)
        }
    }
}