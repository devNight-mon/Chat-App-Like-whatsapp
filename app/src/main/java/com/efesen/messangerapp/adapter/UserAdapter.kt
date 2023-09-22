package com.efesen.messangerapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.efesen.messangerapp.activities.MessageChatActivity
import com.efesen.messangerapp.R
import com.efesen.messangerapp.activities.VisitUserProfileActivity
import com.efesen.messangerapp.modelClasses.Chat
import com.efesen.messangerapp.modelClasses.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by Efe Şen on 13.09.2023.
 */
class UserAdapter(
    mContext: Context,
    mUsers: List<Users>,
    isChatCheck:Boolean
): RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private val mContext: Context
    private val mUsers: List<Users>
    private val isChatCheck:  Boolean
    var lastMessage: String = ""

    init {
        this.mContext = mContext
        this.mUsers = mUsers
        this.isChatCheck = isChatCheck

    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var userNameText: TextView
        var profileImageView: CircleImageView
        var onlineStatus:CircleImageView
        var offlineStatus: CircleImageView
        var lastMessageText: TextView

        init {
            userNameText = itemView.findViewById(R.id.user_name)
            profileImageView = itemView.findViewById(R.id.profile_image)
            onlineStatus = itemView.findViewById(R.id.image_status_online)
            offlineStatus = itemView.findViewById(R.id.image_status_offline)
            lastMessageText = itemView.findViewById(R.id.last_message)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
       val view = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout,viewGroup,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: Users = mUsers[position]
        holder.userNameText.text = user.getUserName()
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile_photo).into(holder.profileImageView)

        if(isChatCheck) {
            retrieveLastMessage(user.getUID(), holder.lastMessageText)
        } else {
            holder.lastMessageText.visibility = View.GONE
        }

        if (isChatCheck) {
            if (user.getStatus() == "online") {
                holder.onlineStatus.visibility = View.VISIBLE
                holder.offlineStatus.visibility = View.GONE
            } else {
                holder.onlineStatus.visibility = View.GONE
                holder.offlineStatus.visibility = View.VISIBLE
            }
        } else {
            holder.onlineStatus.visibility = View.GONE
            holder.offlineStatus.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            )
            val builder: AlertDialog.Builder  = AlertDialog.Builder(mContext)
            builder.setTitle("What do you want?")
            builder.setItems(options, DialogInterface.OnClickListener { dialog, position ->
                when(position) {
                    0 -> {

                        val intent = Intent(mContext, MessageChatActivity::class.java)
                        intent.putExtra("visit_id", user.getUID())
                        mContext.startActivity(intent)

                    }
                    1 -> {
                        val intent = Intent(mContext, VisitUserProfileActivity::class.java)
                        intent.putExtra("visit_id", user.getUID())
                        mContext.startActivity(intent)
                    }
                }
            })
            builder.show()
        }
    }

    private fun retrieveLastMessage(chatUserId: String?, lastMessageText: TextView) {
        lastMessage = "defaultMsg"

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val chat: Chat? = dataSnapshot.getValue(Chat::class.java)

                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver() == firebaseUser!!.uid && chat.getSender() == chatUserId || chat.getReceiver() == chatUserId && chat.getSender() == firebaseUser.uid) {
                            lastMessage = chat.getMessage()!!
                        }
                    }

                }
                when(lastMessage) {
                    "defaultMsg" -> lastMessageText.text = "No Message"
                    "Sent you An Image." -> lastMessageText.text = "image sent."
                    else -> lastMessageText.text = lastMessage
                }
                lastMessage = "defaultMsg"
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
}