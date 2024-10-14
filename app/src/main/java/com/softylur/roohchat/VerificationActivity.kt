package com.softylur.roohchat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.hbb20.CountryCodePicker
import com.softylur.roohchat.databinding.ActivityVerificationBinding

class VerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var countryCodePicker : CountryCodePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        countryCodePicker = binding.countryCodePicker
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.textInputEditText.requestFocus()

        countryCodePicker.registerCarrierNumberEditText(binding.textInputEditText)

        binding.btnContinue.setOnClickListener {

            if (binding.textInputEditText.text.toString().length >= 10){

                val intent = Intent(this, OTPActivity::class.java)
                intent.putExtra("phone_number", countryCodePicker.fullNumberWithPlus)
                startActivity(intent)
            } else {
                binding.textInputEditText.error = "Please enter valid phone number"
            }
        }
    }
}