package com.efesen.messangerapp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.efesen.messangerapp.R
import com.efesen.messangerapp.databinding.ActivityMainBinding
import com.efesen.messangerapp.fragments.ChatsFragment
import com.efesen.messangerapp.fragments.SearchFragment
import com.efesen.messangerapp.fragments.SettingsFragment
import com.efesen.messangerapp.modelClasses.Chat
import com.efesen.messangerapp.modelClasses.Users
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var toolbarName: TextView
    private lateinit var profileImageView:ImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: androidx.viewpager.widget.ViewPager
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private var refUsers: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = firebaseUser?.uid?.let {
            FirebaseDatabase.getInstance().reference.child("Users").child(it)
        }

        bindUIComponents()

      /*  viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(ChatsFragment(),"CHATS")
        viewPagerAdapter.addFragment(SearchFragment(),"SEARCH")
        viewPagerAdapter.addFragment(SettingsFragment(),"SETTINGS")

        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)*/

        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                var countUnReadMessages = 0

                for (dataSnapshot in snapshot.children) {
                   val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && !chat.isIsSeen()!!) {

                        countUnReadMessages += 1

                    }
                }
                if (countUnReadMessages == 0) {
                    viewPagerAdapter.addFragment(ChatsFragment(),"CHATS")
                } else {
                    viewPagerAdapter.addFragment(ChatsFragment(),"($countUnReadMessages) CHATS")
                }
                viewPagerAdapter.addFragment(SearchFragment(),"SEARCH")
                viewPagerAdapter.addFragment(SettingsFragment(),"SETTINGS")
                viewPager.adapter = viewPagerAdapter
                tabLayout.setupWithViewPager(viewPager)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        // Display username and profile picture
        refUsers?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    toolbarName.text = user?.getUserName()
                    Picasso.get().load(user?.getProfile()).placeholder(R.drawable.profile_photo).into(profileImageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun bindUIComponents() {
        toolbarName = binding.userName
        profileImageView = binding.profileImage
        tabLayout = binding.tabLayout
        viewPager = binding.viewPager
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

                return true
            }
        }
        return true
    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager) :
        androidx.fragment.app.FragmentPagerAdapter(fragmentManager) {

        private val fragments: ArrayList<androidx.fragment.app.Fragment>
        private val titles: ArrayList<String>

        init {
            fragments = ArrayList<androidx.fragment.app.Fragment>()
            titles = ArrayList<String>()
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return fragments[position]
        }

        fun addFragment(fragment: androidx.fragment.app.Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(index: Int): CharSequence {
            return titles[index]
        }
    }

    private fun updateStatus(status: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        ref.updateChildren(hashMap)
    }


    override fun onResume() {
        super.onResume()

        updateStatus("online")
    }

    override fun onPause() {
        super.onPause()
        updateStatus("offline")
    }
}