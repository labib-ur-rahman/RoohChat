package com.softylur.roohchat

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hbb20.CountryCodePicker
import com.softylur.roohchat.databinding.ActivityVerificationBinding

class VerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationBinding
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

        binding.etPhoneNumber.requestFocus()

        countryCodePicker.registerCarrierNumberEditText(binding.etPhoneNumber)

        binding.btnContinue.setOnClickListener {
            if (binding.etPhoneNumber.text.toString().length == 10){
                // User enters maximum 10 number then go to next activity
                val intent = Intent(this, OTPActivity::class.java)
                intent.putExtra("phone_number", countryCodePicker.fullNumberWithPlus)
                startActivity(intent)
            } else {
                binding.etPhoneNumber.error = "Please enter valid phone number"
            }
        }
    }
}