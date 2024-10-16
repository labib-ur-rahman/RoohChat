package com.softylur.roohchat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.softylur.roohchat.databinding.ActivityOtpBinding
import com.softylur.roohchat.util.AndroidUtil
import java.util.concurrent.TimeUnit


class OTPActivity : AppCompatActivity() {

    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var verificationId: String
    private lateinit var binding: ActivityOtpBinding
    private lateinit var mAuth: FirebaseAuth
    private val timeOut = 60L
    private lateinit var phoneNumber: String
    private val tag = "OTPActivity"
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    @SuppressLint("SetTextI18n")
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

        // Receiving data from Verification activity
        phoneNumber = intent.extras?.getString("phone_number").toString()
        binding.tvNumber.text = "Verify $phoneNumber"

        // Called sendOTP Function and Sending OTP to user given phone number
        sendOTP(phoneNumber)

        /* If the user has already sent an OTP, then we will not show the resend button
           If the user not sent an OTP again using ResendOTP Button, then we will show  the resend button after the timer completes */
        setInProgress(true)
        startResendTimer()

        binding.tvResendBtn.setOnClickListener {
            startResendTimer()
            sendOTP(phoneNumber)
        }
    }

    //######################################################################################
    private fun sendOTP(phoneNumber: String) {

        /* Step : 2 => Send a verification code to the user's phone
        ==> [OnVerificationStateChangedCallbacks], it's contains implementations of the
            callback functions that handle the results of the request
        -----------------------------------------------------------------------------------*/
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(tag, "onVerificationCompleted: $credential")

                setInProgress(false) // otpView Enabled and Hide progress bar
                signInWithPhoneAuthCredential(credential)

            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the phone number format is not valid.
                Log.w(tag, "onVerificationFailed", e)

                setInProgress(false) // otpView Enabled and Hide progress bar
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> AndroidUtil.lToast(this@OTPActivity,"Invalid request")
                    is FirebaseTooManyRequestsException -> AndroidUtil.lToast(this@OTPActivity,"The SMS quota for the project has been exceeded")
                    is FirebaseAuthMissingActivityForRecaptchaException -> AndroidUtil.lToast(this@OTPActivity, "reCAPTCHA verification attempted with null Activity")
                    else -> AndroidUtil.lToast(this@OTPActivity, "Failed to verify phone number")
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                //super.onCodeSent(verificationId, token)
                Log.d(tag, "onCodeSent: $verificationId")

                setInProgress(false) // otpView Enabled and Hide progress bar

                // Save verification ID and resending token, so we can use them later
                this@OTPActivity.verificationId = verificationId
                resendToken = token

                binding.otpView.requestFocus()
                binding.otpView.post {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.otpView, 0)
                }

                AndroidUtil.lToast(this@OTPActivity, "OTP Send Successfully")
            }

        }

        /* Step : 1 => Send a verification code to the user's phone
        ==> Pass phone number to the [PhoneAuthProvider.verifyPhoneNumber] method to request
            that Firebase verify the user's phone number
        -----------------------------------------------------------------------------------*/
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(timeOut, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this@OTPActivity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        binding.otpView.setOtpCompletionListener { code ->
            Log.d(tag, "User's entered OTP is: $code")

            /* Step : 3 => Create a PhoneAuthCredential object
            ==> [PhoneAuthCredential] object, using the verification code and the verification ID
                which the user enters the verification code that Firebase sent to there phone
            -----------------------------------------------------------------------------------*/

            try {
                Log.d(tag, "Enter try block and VerificationID is: $verificationId")

                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                signInWithPhoneAuthCredential(credential)

            } catch (e: Exception) {
                Log.d(tag, "credential Catch: $e")

                binding.otpView.error = "The verification code entered was invalid"
            }

        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        setInProgress(true) // otpView Disable and Show Progress Bar

        /* Step : 4 => Sign in the user
        ==> complete the sign-in flow by passing the [PhoneAuthCredential] object
            to [FirebaseAuth.signInWithCredential]
        -----------------------------------------------------------------------------------*/

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                Log.d(tag, "SignInWithCredential called & Task is: $task")

                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(tag, "signInWithCredential: Success")

                    val intent = Intent(this, SetupProfileActivity::class.java)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(tag, "signInWithCredential: Failed ", task.exception)

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        setInProgress(false) // otpView Enabled and Hide Progress Bar
                        binding.otpView.error = "The verification code entered was invalid"
                        AndroidUtil.lToast(this@OTPActivity, "The verification code from SMS/TOTP is invalid. Please check and enter the correct verification code again.")
                    }
                    // Update UI
                    AndroidUtil.lToast(this@OTPActivity, "The verification code from SMS/TOTP is invalid. Please check and enter the correct verification code again.")
                }
            }
    }

    private fun startResendTimer() {
        binding.tvResendText.isEnabled = false
        binding.tvResendTimer.visibility = View.VISIBLE

        object : CountDownTimer(60000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val timer = millisUntilFinished / 1000
                binding.tvResendTimer.text = getString(R.string.in_seconds, timer.toString())
                binding.tvResendBtn.isEnabled = false
                binding.tvResendBtn.setTextColor(
                    ContextCompat.getColor(
                        this@OTPActivity,
                        R.color.primary_black
                    )
                )
                binding.tvResendTimer.setTextColor(
                    ContextCompat.getColor(
                        this@OTPActivity,
                        R.color.resend_otp_btn_active_color
                    )
                )
            }

            override fun onFinish() {
                binding.tvResendTimer.visibility = View.INVISIBLE
                binding.tvResendBtn.isEnabled = true
                binding.tvResendBtn.setTextColor(
                    ContextCompat.getColor(
                        this@OTPActivity,
                        R.color.resend_otp_btn_active_color
                    )
                )
                binding.tvResendTimer.setTextColor(
                    ContextCompat.getColor(
                        this@OTPActivity,
                        R.color.primary_black
                    )
                )
            }
        }.start()
    }

    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.otpView.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.otpView.isEnabled = true
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}
