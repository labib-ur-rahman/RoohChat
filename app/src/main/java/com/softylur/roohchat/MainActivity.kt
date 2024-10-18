package com.softylur.roohchat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.softylur.roohchat.adapter.UserAdapter
import com.softylur.roohchat.databinding.ActivityAllUserBinding
import com.softylur.roohchat.model.User


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllUserBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var logInUser: FirebaseUser
   // private lateinit var userList: ArrayList<User>
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllUserBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        logInUser = auth.currentUser!!
        //userList = ArrayList<User>()

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, SetupProfileActivity::class.java))
        }

        binding.rvUser.layoutManager = LinearLayoutManager(this)
        //userAdapter = UserAdapter(this, userList)
        binding.rvUser.adapter = userAdapter

        database.reference.child("users").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                //userList.clear()
                for (snapshot1 in snapshot.children) {
                    val user = snapshot1.getValue(User::class.java)
                    if (user?.uid.equals(auth.uid).not()) {
                        //user?.let { userList.add(it) }
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun onResume() {
        super.onResume()
        val currentId = auth.uid
        currentId?.let {
            database.reference.child("presence")
                .child(it)
                .setValue("online")
        }
    }

    override fun onPause() {
        super.onPause()
        val currentId = auth.uid
        currentId?.let {
            database.reference.child("presence")
                .child(it)
                .setValue("offline")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val currentId = auth.uid
        currentId?.let {
            database.reference.child("presence")
                .child(it)
                .setValue("offline")
        }
    }
}