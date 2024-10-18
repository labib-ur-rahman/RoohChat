package com.softylur.roohchat.adapter

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.softylur.roohchat.ChatActivity
import com.softylur.roohchat.R
import com.softylur.roohchat.databinding.ItemInboxListBinding
import com.softylur.roohchat.databinding.ItemUserListBinding
import com.softylur.roohchat.model.InboxModel
import java.util.Calendar

class InboxAdapter(var context: Context, private var inboxList: ArrayList<InboxModel>) : RecyclerView.Adapter<InboxAdapter.UserViewHolder>() {
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = ItemInboxListBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_inbox_list, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = inboxList.size
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = inboxList[position]
        holder.binding.tvName.text = user.nameReceiver

        val calendar: Calendar = Calendar.getInstance()
        val timestamp: Long = user.lastMsgTime!!.toLong()
        calendar.setTimeInMillis(timestamp)
        holder.binding.tvTimeStamp.text = DateFormat.format("hh:mm a", calendar).toString()

        Glide.with(context)
            .load(user.photoUrlReceiver)
            .placeholder(R.drawable.profile_pic)
            .into(holder.binding.ivImage)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.nameReceiver)
            intent.putExtra("image", user.photoUrlReceiver)
            intent.putExtra("uid", user.uidReceiver)
            context.startActivity(intent)
        }

        if (user.statusReceiver == "typing...") holder.binding.tvMsg.text = user.statusReceiver
        else holder.binding.tvMsg.text = user.lastMsg
    }
}