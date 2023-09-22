package com.efesen.messangerapp.adapter

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.efesen.messangerapp.R
import com.efesen.messangerapp.activities.ViewFullImageActivity
import com.efesen.messangerapp.modelClasses.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by Efe Şen on 18.09.2023.
 */
class ChatsAdapter(mChatList: List<Chat>, imageUrl: String): RecyclerView.Adapter<ChatsAdapter.ViewHolder?>() {
    private val mChatList :List<Chat>
    private val imageUrl : String
    var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    init {
        this.mChatList = mChatList
        this.imageUrl = imageUrl
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView? = null
        var showTextMessage: TextView? = null
        var leftImageView: ImageView? = null
        var  textSeen: TextView? = null

        var rightImageView: ImageView? = null

        init {
            profileImage = itemView.findViewById(R.id.message_item_profile_image)
            showTextMessage = itemView.findViewById(R.id.message_item_show_text_msg)
            leftImageView = itemView.findViewById(R.id.left_image_view)
            textSeen = itemView.findViewById(R.id.text_seen)

            rightImageView = itemView.findViewById(R.id.right_image_view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return if (position == 1) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item_right, parent,false)
            ViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item_left, parent,false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = mChatList[position]

        Picasso.get().load(imageUrl).into(holder.profileImage)

        // images messages
        if (chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals("")) {


            //image message - right side
            if (chat.getSender().equals(firebaseUser!!.uid)) {
                holder.showTextMessage!!.visibility = View.GONE
                holder.rightImageView!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.rightImageView)

                holder.rightImageView!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Full Image",
                        "Delete Image",
                        "Cancel"
                    )

                    val builder: androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Select Action")
                    builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                       when(which) {
                           0 -> {
                               val intent = Intent(holder.itemView.context,ViewFullImageActivity::class.java)
                               intent.putExtra("url",chat.getUrl())
                               holder.itemView.context.startActivity(intent)
                           }
                           1 -> {
                                deleteSentMessage(position,holder)
                           }
                       }

                    })
                    builder.show()
                }

            }  //image message - left side
            else if (!chat.getSender().equals(firebaseUser!!.uid)) {
                holder.showTextMessage!!.visibility = View.GONE
                holder.leftImageView!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.leftImageView)

                holder.leftImageView!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Full Image",
                        "Cancel"
                    )

                    val builder: androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Select Action")
                    builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                        if (which == 0) {
                            val intent = Intent(holder.itemView.context, ViewFullImageActivity::class.java)
                            intent.putExtra("url", chat.getUrl())
                            holder.itemView.context.startActivity(intent)
                        }

                    })
                    builder.show()
                }
            }
            //text Messages
        } else {
            holder.showTextMessage!!.text = chat.getMessage()

            if (firebaseUser.uid == chat.getSender()) {
                holder.showTextMessage!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "Delete Message",
                        "Cancel"
                    )

                    val builder: androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Select Action")
                    builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                        if (which == 0) {
                            deleteSentMessage(position,holder)
                        }
                    })
                    builder.show()
                }
            }
        }

        // sent and seen message
        if (position == mChatList.size -1) {

          if (chat.isIsSeen() == true) {
              holder.textSeen?.text = "Seen"

              if (chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals("")) {
                  val lp: RelativeLayout.LayoutParams? = holder.textSeen!!.layoutParams as LayoutParams?
                  lp!!.setMargins(0,245,10,0)
                  holder.textSeen!!.layoutParams = lp
              }
          } else {
              holder.textSeen?.text = "Sent"

          }

        } else {
            holder.textSeen?.visibility = View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int {

        return if (mChatList[position].getSender().equals(firebaseUser!!.uid)) {
            1
        } else {
            0
        }
    }

    private fun deleteSentMessage(position: Int, holder: ViewHolder) {
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(mChatList.get(position).getMessageId()!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(holder.itemView.context, "Deleted.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(holder.itemView.context, "Failed, Not Deleted.", Toast.LENGTH_SHORT).show()
                }
            }

    }
}