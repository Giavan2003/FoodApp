package com.example.foodapp.fragment.Home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.foodapp.activity.Home.ForgotActivity
import com.example.foodapp.activity.Home.HomeActivity
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginFragment : Fragment() {
    private var binding: FragmentLoginBinding? = null
    private val TAG = "firebase - LOGIN"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding?.apply {
            btnReset.setOnClickListener {
                val email = edtEmail.text.toString()
                val password = edtPasswordLogin.text.toString()

                if (email.isEmpty() || password.isEmpty()) {
                    FailToast(context, "Please fill all the information").showToast()
                } else {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                FirebaseAuth.getInstance().currentUser?.uid?.let { idCurrentUser ->
                                    FirebaseDatabase.getInstance().getReference("Users").child(idCurrentUser)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                SuccessfulToast(context, "Login successfully!").showToast()
                                                Log.d(TAG, "Login successful")
                                                startActivity(Intent(context, HomeActivity::class.java))
                                                activity?.finish()
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                Log.w(TAG, "DatabaseError", error.toException())
                                            }
                                        })
                                }
                            } else {
                                Log.w(TAG, "Login failed", task.exception)
                                FailToast(context, "Wrong email or password!").showToast()
                            }
                        }
                }
            }

            forgotpassText.setOnClickListener {
                startActivity(Intent(context, ForgotActivity::class.java))
            }
        }?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
