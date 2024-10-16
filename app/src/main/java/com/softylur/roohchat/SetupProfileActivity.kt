package com.softylur.roohchat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.softylur.roohchat.databinding.ActivitySetupProfileBinding
import com.softylur.roohchat.databinding.DialogLogoutWarningBinding
import com.softylur.roohchat.model.User
import com.softylur.roohchat.util.AndroidUtil


class SetupProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImage: Uri
    private val tag = "SetupProfileActivity"

    @SuppressLint("InflateParams")
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
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        currentUser = Firebase.auth.currentUser!!

        // Setting user's name, profile pic and phone number in EditText, ImageView and TextView respectively
        binding.textInputEditText.setText(currentUser.displayName)
        binding.proName.text = currentUser.displayName
        binding.proNumber.text = currentUser.phoneNumber
        Glide.with(this)
            .load(currentUser.photoUrl)
            .placeholder(R.drawable.profile_pic)
            .into(binding.proPic)
        setInProgress(false)

        // Back and Logout Button Functionality
        binding.backLogout.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_logout_warning, null)
            val dialogBinding: DialogLogoutWarningBinding =
                DialogLogoutWarningBinding.bind(dialogView)
            builder.setView(dialogBinding.root)
            builder.setCanceledOnTouchOutside(false)
            builder.show()

            dialogBinding.btnYesLogOut.setOnClickListener {
                auth.signOut()
                startActivity(Intent(this, VerificationActivity::class.java))
                builder.dismiss()
                finish()
            }
            dialogBinding.btnNoLogOut.setOnClickListener { builder.dismiss() }
            dialogBinding.btnCloseLogOut.setOnClickListener { builder.dismiss() }

            if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
            builder.show()
        }
        binding.backHomeBtn.setOnClickListener {
            val name = binding.textInputEditText.text.toString()
            if (name.isEmpty()) {
                AndroidUtil.lToast(this, "Please complete your profile information")
                binding.textInputEditText.error = "Name is required"
                return@setOnClickListener
            } else {
                startActivity(Intent(this, HomePageActivity::class.java))
                finish()
            }
        }

        // Image Picker Functionality
        binding.proPic.setOnClickListener { callImagePicker() }
        binding.proCamera.setOnClickListener { callImagePicker() }

        // Save Button Functionality
        binding.btnSetupProfile.setOnClickListener {
            setInProgress(true)
            try {
                setInProgress(false)
                Log.d(tag, "selectedImage : $selectedImage")
                setProfileInformation()
            } catch (e: Exception) {
                Log.d(tag, "selectedImage is null")
                setInProgress(false)
                AndroidUtil.lToast(this, "Profile Picture is required")
            }
        }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            setInProgress(true)

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val uri = data?.data!! //filePath
                    val storage = FirebaseStorage.getInstance()
                    val phoneNumber = currentUser.phoneNumber
                    val ref = storage.reference.child("profile_images/$phoneNumber/profile.jpg")

                    ref.putFile(uri).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            AndroidUtil.lToast(this@SetupProfileActivity, "Image Selected")
                            setInProgress(false)
                        }
                    }

                    // Use Uri object instead of File to avoid storage permissions
                    binding.proPic.setImageURI(uri)
                    selectedImage = uri
                }

                ImagePicker.RESULT_ERROR -> {
                    AndroidUtil.lToast(this, ImagePicker.getError(data))
                    setInProgress(false)
                }

                else -> {
                    AndroidUtil.lToast(this, "Task Cancelled")
                    setInProgress(false)
                }
            }
        }

    private fun callImagePicker() {
        setInProgress(false)

        ImagePicker.with(this)
            .cropSquare()                             //Crop square image, its same as crop(1f, 1f)
            .compress(1024)                    //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            )    //Final image resolution will be less than 1080 x 1080(Optional)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }

    private fun setProfileInformation() {
        setInProgress(true)

        val name: String = binding.textInputEditText.text.toString()
        val phoneNumber = currentUser.phoneNumber
        val strRef = storage.reference.child("profile_images/$phoneNumber/profile.jpg")

        if (name.isEmpty()) binding.textInputEditText.error = "Name is required"

        Log.d(tag, "Enter try block : $selectedImage")
        strRef.putFile(selectedImage).addOnSuccessListener {
            AndroidUtil.lToast(this, "Uploaded Image Successfully")

            strRef.downloadUrl.addOnSuccessListener { uri ->
                val userImgURL = uri.toString()
                val userUid = currentUser.uid
                val userNumber = currentUser.phoneNumber
                val userName = binding.textInputEditText.text.toString()

                val rootRef = FirebaseDatabase.getInstance().getReference()
                val usersRef = rootRef.child("users")
                val updates = HashMap<String, Any>()
                userImgURL.also { updates["profileImage"] = it }

                val userInfo = User(userUid, userName, userNumber, userImgURL)
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                    photoUri = Uri.parse(userImgURL)
                }

                currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) Log.d(tag, "User profile updated.")
                    }

                usersRef.child(userUid)
                    .setValue(userInfo)
                    .addOnSuccessListener {
                        setInProgress(false)
                        val inputBox = binding.textInputEditText.text.toString()
                        if (inputBox.isEmpty()) {
                            binding.textInputEditText.error = "Name is required"
                        } else {
                            val intent = Intent(this, HomePageActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
            }
        }.addOnFailureListener {
            setInProgress(false)

            AndroidUtil.lToast(
                this@SetupProfileActivity,
                "Failed to update profile picture"
            )
        }
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.progressBarShow.visibility = View.VISIBLE
            binding.progressBarHide.visibility = View.GONE
        } else {
            binding.progressBarShow.visibility = View.GONE
            binding.progressBarHide.visibility = View.VISIBLE
        }
    }

}