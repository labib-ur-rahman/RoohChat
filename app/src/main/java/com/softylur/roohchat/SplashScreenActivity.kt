package com.softylur.roohchat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.softylur.roohchat.databinding.ActivitySplashScreenBinding

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()


        val upFromBottom = AnimationUtils.loadAnimation(this, R.anim.animation1)

        binding.logoImageView.animation = upFromBottom
        binding.appNameTextView.animation = upFromBottom

        // Delay the splash screen for 3 seconds, then move to AllUserActivity
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if user is already logged in and move to AllUserActivity if true, otherwise move to VerificationActivity
            if (auth.currentUser != null) {
                // Start the home page activity
                startActivity(Intent(this, InboxActivity::class.java))
            } else {
                // Start the verification activity
                startActivity(Intent(this, VerificationActivity::class.java))
            }
            // Close this activity
            finish()
        }, 3000) // 3 seconds delay

    }
}