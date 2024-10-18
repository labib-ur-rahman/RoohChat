package com.softylur.roohchat

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.softylur.roohchat.adapter.InboxAdapter
import com.softylur.roohchat.databinding.ActivityInboxBinding
import com.softylur.roohchat.model.InboxModel

class InboxActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInboxBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var inboxList: ArrayList<InboxModel>
    private lateinit var inboxAdapter: InboxAdapter
    private val tag = "InboxActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInboxBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        inboxList = ArrayList<InboxModel>()

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, SetupProfileActivity::class.java))
        }

        binding.btnMessage.setOnClickListener {
            startActivity(Intent(this, AllUserActivity::class.java))
        }

        binding.rvUser.layoutManager = LinearLayoutManager(this)
        inboxAdapter = InboxAdapter(this, inboxList)
        binding.rvUser.adapter = inboxAdapter

        database.reference.child("inbox")
            .child(currentUser.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(tag, "Chats Enter")
                    inboxList.clear()
                    for (snapshot1 in snapshot.children) {
                        val user1 = snapshot1.getValue(InboxModel::class.java)

                        if (user1?.uidReceiver.equals(auth.uid).not()) {
                            user1?.let { inboxList.add(it) }

                            Log.d(tag, user1?.lastMsg.toString())
                        }
                    }
                    inboxAdapter.notifyDataSetChanged()
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
            database.reference.child("users")
                .child(it)
                .child("status")
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
            database.reference.child("users")
                .child(it)
                .child("status")
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
            database.reference.child("users")
                .child(it)
                .child("status")
                .setValue("offline")
        }
    }
}