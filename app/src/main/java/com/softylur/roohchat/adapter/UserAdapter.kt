package com.softylur.roohchat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.softylur.roohchat.ChatActivity
import com.softylur.roohchat.R
import com.softylur.roohchat.databinding.ItemUserListBinding
import com.softylur.roohchat.model.InboxModel
import com.softylur.roohchat.model.User

class UserAdapter(var context: Context, private var userList: ArrayList<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private val database = FirebaseDatabase.getInstance()

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = ItemUserListBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user_list, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = userList.size
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.tvName.text = user.name
        holder.binding.tvBio.text = user.bio
        holder.binding.tvStatus.text = user.status
        Glide.with(context)
            .load(user.profileImage)
            .placeholder(R.drawable.profile_pic)
            .into(holder.binding.ivImage)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("image", user.profileImage)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent)
        }
    }
}