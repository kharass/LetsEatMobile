package com.leshen.letseatmobile.login

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.leshen.letseatmobile.BaseActivity
import com.leshen.letseatmobile.databinding.ActivityForgetPasswordBinding

class ForgetPasswordActivity : BaseActivity() {
    private var binding: ActivityForgetPasswordBinding? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = Firebase.auth
        binding?.btnForgotPasswordSubmit?.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        val email = binding?.etForgotPasswordEmail?.text.toString()
        if (validateEmail(email))
        {
            showProgressBar()
            auth.sendPasswordResetEmail(email).addOnCompleteListener(this){task->
                if (task.isSuccessful)
                {
                    hideProgressBar()
                }
                else
                {
                    hideProgressBar()
                    showToast(this,"Reset password failed, try again latter")
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun validateEmail(email:String):Boolean
    {
        return if (TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding?.tilEmailForgetPassword?.error = "Enter valid email address"
            false
        } else {
            binding?.tilEmailForgetPassword?.error = null
            true
        }
    }
}