package com.softylur.roohchat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.softylur.roohchat.databinding.ActivityOtpBinding
import com.softylur.roohchat.util.AndroidUtil
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {

    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var storedVerificationId: String
    private lateinit var binding: ActivityOtpBinding
    private lateinit var mAuth: FirebaseAuth
    private var timeOutSecond = 60L
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAuth = FirebaseAuth.getInstance()

        phoneNumber = intent.extras?.getString("phone_number").toString()
        binding.tvNumber.text = "Verify $phoneNumber"

        sendOTP(phoneNumber, false)

        binding.btnNext.setOnClickListener {
            val codeOTP = binding.otpView.text.toString()
            val credential =  PhoneAuthProvider.getCredential(storedVerificationId, codeOTP)
            signInWithPhoneAuthCredential(credential)
        }

        binding.btnResendOtp.visibility = View.GONE
        binding.tvResendText.setOnClickListener {
            sendOTP(phoneNumber, true)
        }
    }

    //######################################################################################
    private fun sendOTP(phoneNumber: String, isResend: Boolean) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    setInProgress(false)
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Show a message and update the UI
                    setInProgress(false)

                    AndroidUtil.lToast(this@OTPActivity, "OTP Verification Failed")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken,
                ) {
                    super.onCodeSent(verificationId, token)

                    // Save verification ID and resending token so we can use them later
                    setInProgress(false)
                    storedVerificationId = verificationId
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    binding.otpView.requestFocus()
                    // resendToken = token

                    AndroidUtil.lToast(this@OTPActivity, "OTP Send Successfully")
                }
            }) // OnVerificationStateChangedCallbacks

        if (isResend) {
            PhoneAuthProvider.verifyPhoneNumber(options.setForceResendingToken(resendToken).build())
        } else {
            PhoneAuthProvider.verifyPhoneNumber(options.build())
        }
        binding.otpView.setOtpCompletionListener { otp->
            val credential =  PhoneAuthProvider.getCredential(storedVerificationId, otp)
            mAuth.signInWithCredential(credential)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        setInProgress(false)

                        // val user = task.result?.user
                        val intent = Intent(this@OTPActivity, SetupProfileActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        // Sign in failed, display a message and update the UI
                        AndroidUtil.lToast(this@OTPActivity, "OTP Verification Failed")

                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                        // Update UI
                    }
                }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        binding.tvResendText.text = "Resend OTP in $timeOutSecond Seconds"

        startResendTimer()
        setInProgress(true)

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    setInProgress(false)

                    // val user = task.result?.user
                    val intent = Intent(this, SetupProfileActivity::class.java)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    // Sign in failed, display a message and update the UI
                    AndroidUtil.lToast(this@OTPActivity, "OTP Verification Failed")

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private fun startResendTimer() {
        binding.tvResendText.isEnabled = false

//      Timer().scheduleAtFixedRate(object : TimerTask(){
        Timer().schedule(object : TimerTask(){
            override fun run() {
                timeOutSecond--

                runOnUiThread {
                    if (timeOutSecond <= 0) {
                        binding.tvResendText.text = getString(R.string.resend_otp)
                        binding.tvResendText.isEnabled = true
                        cancel()
                    } else {
                        binding.tvResendText.text = "Resend OTP in $timeOutSecond Seconds"
                    }
                }
            }
        }, 0, 1000)
    }

    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnNext.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnNext.visibility = View.VISIBLE
        }
    }
}
