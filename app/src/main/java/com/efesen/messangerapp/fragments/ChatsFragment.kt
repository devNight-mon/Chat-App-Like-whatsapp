package com.efesen.messangerapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.efesen.messangerapp.adapter.UserAdapter
import com.efesen.messangerapp.databinding.FragmentChatsBinding
import com.efesen.messangerapp.modelClasses.ChatList
import com.efesen.messangerapp.modelClasses.Users
import com.efesen.messangerapp.notifications.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging


class ChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var usersChatList: List<ChatList>? = null
    private var firebaseUser: FirebaseUser? = null
    private lateinit var fragmentContext: Context


    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(layoutInflater, container, false)

        binding.recyclerviewChatlist.setHasFixedSize(true)
        binding.recyclerviewChatlist.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        usersChatList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                (usersChatList as ArrayList).clear()

                for (dataSnapsshot in snapshot.children) {
                    val chatList = dataSnapsshot.getValue(ChatList::class.java)

                    (usersChatList as ArrayList).add(chatList!!)
                }
                retrieveChatList()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Token received successfully
                    val token = task.result
                    updateToken(token)
                } else {
                    // An error occurred while receiving tokens
                    val exception = task.exception

                }
            }

        return binding.root
    }

    private fun updateToken(token: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val tokenObject = Token(token)
        ref.child(firebaseUser!!.uid).setValue(tokenObject)
    }

    private fun retrieveChatList() {

        mUsers = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                (mUsers as ArrayList).clear()

                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(Users::class.java)

                    for (eachChatList in usersChatList!!) {

                        if (user?.getUID().equals(eachChatList.getId())) {
                            (mUsers as ArrayList).add(user!!)
                        }
                    }
                }
                userAdapter = UserAdapter(fragmentContext, (mUsers as ArrayList<Users>), true)
                binding.recyclerviewChatlist.adapter = userAdapter

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}