package com.softylur.roohchat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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
import com.google.firebase.storage.FirebaseStorage
import com.softylur.roohchat.adapter.MessageAdapter
import com.softylur.roohchat.databinding.ActivityChatBinding
import com.softylur.roohchat.model.Message
import com.softylur.roohchat.model.User
import java.util.Calendar
import java.util.Date

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    lateinit var adapter: MessageAdapter
    private lateinit var messagesList: ArrayList<Message>
    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var logInUser: FirebaseUser
    lateinit var senderUid: String
    private lateinit var receiverUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        logInUser = auth.currentUser!!
        messagesList = ArrayList()

        val nameReceiver = intent.getStringExtra("name").toString()
        val imageReceiver = intent.getStringExtra("image").toString()
        receiverUid = intent.getStringExtra("uid").toString()
        senderUid = FirebaseAuth.getInstance().uid.toString()

        binding.tvName.text = nameReceiver
        Glide.with(this)
            .load(imageReceiver)
            .placeholder(R.drawable.profile_pic)
            .into(binding.ivProfilePic)
        binding.btnBack.setOnClickListener { finish() }
        database.reference.child("presence")
            .child(receiverUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        binding.tvPresence.text = snapshot.value.toString()
//
//                        if (status == "offline") {
//                            binding.tvPresence.text = snapshot.value.toString()
//                        } else {
//                            binding.tvPresence.text = snapshot.value.toString()
//                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        senderRoom = "$senderUid-SL-$receiverUid"
        receiverRoom = "$receiverUid-SL-$senderUid"



        adapter = MessageAdapter(this@ChatActivity, messagesList, senderRoom, receiverRoom, imageReceiver, nameReceiver)

        binding.rvChatMessage.layoutManager = LinearLayoutManager(this)
        binding.rvChatMessage.adapter = adapter
        database.reference.child("chats")
            .child(senderRoom)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messagesList.clear()
                    for (snapshot1 in snapshot.children) {
                        val message = snapshot1.getValue(Message::class.java)
                        message!!.messageId = snapshot1.key
                        messagesList.add(message)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        binding.btnSend.setOnClickListener {
            val messageText = binding.etMessageBox.text.toString()
            val date = Date()
            val message = Message(messageText, senderUid, date.time)

            binding.etMessageBox.setText("")
            val randomKey = database.reference.push().key
            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lastMsgTime"] = date.time

            database.reference.child("chats")
                .child(senderRoom)
                .updateChildren(lastMsgObj)
            database.reference.child("chats")
                .child(receiverRoom)
                .updateChildren(lastMsgObj)

            database.reference.child("chats").child(senderRoom)
                .child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database.reference.child("chats")
                        .child(receiverRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener {}
                }
        }
        binding.btnAttachment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        val handler = Handler()
        //val handler = android.os.Handler(Looper.getMainLooper()) // ar jonno problem hote pare
        //val handler = android.os.Handler() // ar jonno problem hote pare

        binding.etMessageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                database.reference.child("presence")
                    .child(senderUid)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping = Runnable {
                database.reference.child("presence")
                    .child(senderUid)
                    .setValue("Online")
            }
        })
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25) {
            if (data != null) {
                if (data.data != null) {
                    val selectedImage = data.data
                    val calendar = Calendar.getInstance()
                    val ref = storage.reference.child("chats")
                        .child(calendar.timeInMillis.toString() + "")
                    ref.putFile(selectedImage!!)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                ref.downloadUrl.addOnSuccessListener { uri ->
                                    val filePath = uri.toString()
                                    val messageTxt = binding.etMessageBox.text.toString()
                                    val date = Date()
                                    val messagesList = Message(messageTxt, senderUid, date.time)
                                    messagesList.message = "photo"
                                    messagesList.imageUrl = filePath
                                    binding.etMessageBox.setText("")
                                    val randomKey = database.reference.push().key
                                    val lastMsgObj = HashMap<String, Any>()
                                    lastMsgObj["lastMsg"] = messagesList.message!!
                                    lastMsgObj["lastMsgTime"] = date.time
                                    database.reference.child("chats")
                                        .child(senderRoom)
                                        .updateChildren(lastMsgObj)
                                    database.reference.child("chats")
                                        .child(receiverRoom)
                                        .updateChildren(lastMsgObj)
                                    database.reference.child("chats")
                                        .child(senderRoom)
                                        .child("messages")
                                        .child(randomKey!!)
                                        .setValue(messagesList).addOnSuccessListener {
                                            database.reference.child("chats")
                                                .child(receiverRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(messagesList)
                                                .addOnSuccessListener {}
                                        }
                                }
                            }
                        }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        currentId?.let {
            database.reference.child("presence")
                .child(it)
                .setValue("online")
        }
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        currentId?.let {
            database.reference.child("presence")
                .child(it)
                .setValue("offline")
        }
    }
}