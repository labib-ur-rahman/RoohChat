package com.softylur.roohchat.adapter

import android.app.AlertDialog
import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.softylur.roohchat.R
import com.softylur.roohchat.databinding.ChatReceiveMsgBinding
import com.softylur.roohchat.databinding.ChatSentMsgBinding
import com.softylur.roohchat.databinding.LayoutDeleteBinding
import com.softylur.roohchat.databinding.LayoutDeleteEveryoneBinding
import com.softylur.roohchat.model.Message
import com.softylur.roohchat.model.User
import java.util.Calendar

class MessageAdapter(
    private var context: Context,
    messagesList: ArrayList<Message>?,
    senderRoom: String,
    receiverRoom: String,
    imageReceiver: String,
    nameReceiver: String) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    private lateinit var messagesList: ArrayList<Message>
    private val ITEM_SENT = 1
    private val ITEM_REVEIVE = 2
    private var senderRoom: String
    private var receiverRoom: String
    private var imageReceiver: String
    private var nameReceiver: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT){
            val view = LayoutInflater.from(context).inflate(R.layout.chat_sent_msg,parent,false)
            SentMsgViewHolder(view)
        } else{
            val view = LayoutInflater.from(context).inflate(R.layout.chat_receive_msg,parent,false)
            ReceiveMsgViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val messagesList = messagesList[position]
        return if (FirebaseAuth.getInstance().uid == messagesList.senderId){
            ITEM_SENT
        } else {
            ITEM_REVEIVE
        }
    }

    override fun getItemCount(): Int = messagesList.size


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val messagesList = messagesList[position]
        if (holder.javaClass == SentMsgViewHolder::class.java){
            val viewHolder = holder as SentMsgViewHolder
            if (messagesList.message.equals("photo")){
                viewHolder.binding.cvAttachmentImage.visibility = View.VISIBLE
                viewHolder.binding.tvMessage.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(messagesList.imageUrl)
                    .placeholder(R.drawable.ic_landscape_placeholder_svg)
                    .into(viewHolder.binding.ivAttachmentImage)
            }

            // Realtime database theke "users" node er current user er sob data ber kora hoace
            // users >>> uid >>> name = value
            val logInUser = FirebaseAuth.getInstance().currentUser!!
            FirebaseDatabase.getInstance().getReference("users").child(logInUser.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val logInUserInfo = snapshot.getValue(User::class.java)!!
                        Glide.with(context)
                            .load(logInUserInfo.profileImage)
                            .placeholder(R.drawable.profile_pic)
                            .into(viewHolder.binding.ivSenderPicture)
                        Glide.with(context)
                            .load(logInUserInfo.profileImage)
                            .placeholder(R.drawable.profile_pic)
                            .into(viewHolder.binding.ivSenderPicture2)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            val calendar: Calendar = Calendar.getInstance()
            calendar.setTimeInMillis(messagesList.timeStamp)
            holder.binding.tvTime.text = DateFormat.format("hh:mm a", calendar).toString()
            holder.binding.tvTimeImage.text = DateFormat.format("hh:mm a", calendar).toString()

            viewHolder.binding.tvMessage.text = messagesList.message

            viewHolder.itemView.setOnLongClickListener {
                val builder = AlertDialog.Builder(context,R.style.CustomAlertDialog).create()
                val view = LayoutInflater.from(context).inflate(R.layout.layout_delete_everyone, null)
                val binding: LayoutDeleteEveryoneBinding = LayoutDeleteEveryoneBinding.bind(view)
                builder.setView(binding.root)
                builder.setCanceledOnTouchOutside(false)
                builder.show()

                binding.everyone.setOnClickListener {
                    messagesList.message = "This message is deleted"
                    messagesList.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(messagesList)
                    }
                    builder.dismiss()
                }
                binding.delete.setOnClickListener {
                    messagesList.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(null)
                    }
                    builder.dismiss()
                }
                binding.cancel.setOnClickListener { builder.dismiss() }
                builder.show()

                false
            }
        } else {
            val viewHolder = holder as ReceiveMsgViewHolder
            if (messagesList.message.equals("photo")){
                viewHolder.binding.cvAttachmentImage.visibility = View.VISIBLE
                viewHolder.binding.tvMessage.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(messagesList.imageUrl)
                    .placeholder(R.drawable.ic_landscape_placeholder_svg)
                    .into(viewHolder.binding.ivAttachmentImage)
            }
            viewHolder.binding.tvMessage.text = messagesList.message
            Glide.with(context)
                .load(imageReceiver)
                .placeholder(R.drawable.profile_pic)
                .into(viewHolder.binding.ivReceiverPicture)

            viewHolder.itemView.setOnLongClickListener {
                val builder = AlertDialog.Builder(context,R.style.CustomAlertDialog)
                    .create()
                val view = LayoutInflater.from(context).inflate(R.layout.layout_delete, null)
                val binding = LayoutDeleteBinding.bind(view)
                builder.setView(binding.root)
                builder.setCanceledOnTouchOutside(false)
                builder.show()

                binding.delete.setOnClickListener {
                    messagesList.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(null)
                    }
                    builder.dismiss()
                }
                binding.cancel.setOnClickListener { builder.dismiss() }
                builder.show()

                false
            }
        }
    }

    inner class SentMsgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding: ChatSentMsgBinding = ChatSentMsgBinding.bind(itemView)
    }
    inner class ReceiveMsgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding: ChatReceiveMsgBinding = ChatReceiveMsgBinding.bind(itemView)
    }
    init {
        if (messagesList != null){
            this.messagesList = messagesList
        }
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
        this.imageReceiver = imageReceiver
        this.nameReceiver = nameReceiver
    }

}