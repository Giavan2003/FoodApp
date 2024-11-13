package com.example.foodapp.activity.Home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ActivityForgotBinding
import com.google.firebase.auth.FirebaseAuth


class ForgotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")

        binding.btnReset.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            if (email.isEmpty()) {
                FailToast(this, "Please enter the email you want to reset password").showToast()
            } else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        SuccessfulToast(this, "Reset password successfully! Please check your email").showToast()
                        finish()
                    } else {
                        FailToast(this, "Make sure your entered email is correct!").showToast()
                    }
                }
            }
        }

        binding.signupBtn.setOnClickListener {
            finish()
        }
    }
}
