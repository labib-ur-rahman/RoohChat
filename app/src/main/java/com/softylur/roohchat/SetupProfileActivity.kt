package com.softylur.roohchat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.softylur.roohchat.databinding.ActivitySetupProfileBinding
import com.softylur.roohchat.model.User
import com.softylur.roohchat.util.AndroidUtil
import java.util.HashMap

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImage: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.profileImage.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 45)
        }

        binding.btnSetupProfile.setOnClickListener {
            val name: String = binding.textInputEditText.text.toString()
            if(name.isEmpty()){
                binding.textInputEditText.error = "Name is required"
                return@setOnClickListener
            }
            if (selectedImage != null){
                val phoneNumber = auth.currentUser?.phoneNumber
                //val ref = storage.reference.child("profile_images/$phoneNumber/profile.jpg")
                val strRef = storage.reference.child("profile_images/$phoneNumber/profile.jpg")
                strRef.putFile(selectedImage).addOnSuccessListener { task ->
                    AndroidUtil.lToast(this@SetupProfileActivity, "Image has been uploaded")

                    strRef.downloadUrl.addOnSuccessListener { uri->
                        val imgRef = uri.toString()
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        val phoneNumber = auth.currentUser!!.phoneNumber
                        val name = binding.textInputEditText.text.toString()

                        val rootRef = FirebaseDatabase.getInstance().getReference()
                        val usersRef = rootRef.child("users")
                        val updates = HashMap<String, Any>()
                        imgRef.also { updates["profileImage"] = it }

                        val user = User(uid, name, phoneNumber, imgRef)

                        uid?.let { it1 ->
                            usersRef.child(it1)
                                .setValue(user)
                                .addOnSuccessListener {
                                val intent = Intent(this, HomePageActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            /*usersRef.child(it1).updateChildren(updates).addOnSuccessListener {
                                val intent = Intent(this, HomePageActivity::class.java)
                                startActivity(intent)
                                finish()
                            }*/
                        }
                    }
                }.addOnFailureListener {
                    AndroidUtil.lToast(this@SetupProfileActivity, "Failed to update profile picture")
                }

                /*
                ref.putFile(selectedImage).addOnCompleteListener{ task ->
                    if (task.isSuccessful){
                        ref.downloadUrl.addOnCompleteListener { uri ->
                            val imageUrl = uri.toString()
                            val uid = auth.uid
                            val phoneNumber = auth.currentUser!!.phoneNumber
                            val name = binding.textInputEditText.text.toString()
                            val user = User(uid, name, phoneNumber, imageUrl)

                            database.reference
                                .child("users")
                                .child(uid!!)
                                .setValue(user)
                                .addOnCompleteListener {
                                    val intent = Intent(this, HomePageActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                    } else{
                        val uid = auth.uid
                        val phoneNumber = auth.currentUser!!.phoneNumber
                        val name = binding.textInputEditText.text.toString()
                        val user = User(uid, name, phoneNumber, "No Image")

                        database.reference
                            .child("users")
                            .child(uid!!)
                            .setValue(user)
                            .addOnCanceledListener {
                                val intent = Intent(this, HomePageActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                }*/
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null){
            if (data.data != null){
                val uri = data.data  //filePath
                val storage = FirebaseStorage.getInstance()
                //val time = Date().time
                //val ref = storage.reference.child("profile_images/" + auth.currentUser!!.uid + ".jpg")
                val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber





                val ref = storage.reference.child("profile_images/$phoneNumber/profile.jpg")

                ref.putFile(uri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        AndroidUtil.lToast(this@SetupProfileActivity, "Image Selected")
                    }
                }

                binding.profileImage.setImageURI(data.data)
                selectedImage = data.data!!
            }
        }
    }
}