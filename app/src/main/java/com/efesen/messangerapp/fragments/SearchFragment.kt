package com.efesen.messangerapp.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.efesen.messangerapp.adapter.UserAdapter
import com.efesen.messangerapp.databinding.FragmentSearchBinding
import com.efesen.messangerapp.modelClasses.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding

    private lateinit var userAdapter: UserAdapter
    private var mUsers: List<Users>? = null
    private var recyclerView:RecyclerView? = null
    private var searchEditText:EditText? = null
    private lateinit var fragmentContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(layoutInflater, container,false)

        mUsers = ArrayList()

        retrieveAllUsers()

        recyclerView = binding.searchRcy
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        searchEditText = binding.searchUsers

        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(cs: CharSequence?, p1: Int, p2: Int, p3: Int) {
              searchForUsers(cs.toString().toLowerCase())
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        return binding.root
    }

    private fun retrieveAllUsers() {
        val firebaseUserID = FirebaseAuth.getInstance().currentUser?.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")

        refUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                if (searchEditText?.text.toString() == "") {
                    for (p0 in snapshot.children) {
                        val user: Users? = p0.getValue(Users::class.java)

                        // control is I cant show my profile in search
                        if(!user!!.getUID().equals(firebaseUserID)) {
                            (mUsers as ArrayList<Users>).add(user)

                        }
                    }
                    userAdapter = UserAdapter(fragmentContext, mUsers!!, false)
                    recyclerView?.adapter = userAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun searchForUsers(str :String) {
        val firebaseUserID = FirebaseAuth.getInstance().currentUser?.uid
        val queryUsers = FirebaseDatabase.getInstance().reference.child("Users")
            .orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")

        queryUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                (mUsers as ArrayList<Users>).clear()

                for (p0 in snapshot.children) {
                    val user: Users? = p0.getValue(Users::class.java)

                    // control is I cant show my profile in search
                    if(!user!!.getUID().equals(firebaseUserID)) {
                        (mUsers as ArrayList<Users>).add(user)
                    }
                }
                userAdapter = UserAdapter(context!!, mUsers!!, false)
                recyclerView?.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

}