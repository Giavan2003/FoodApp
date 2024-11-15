package com.example.foodapp.fragment.Home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.foodapp.R
import com.example.foodapp.activity.Home.HomeActivity
import com.example.foodapp.activity.Home.ForgotActivity

import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    companion object {
        private const val TAG = "firebase - LOGIN"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.btnReset.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPasswordLogin.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                FailToast(requireContext(), "Please fill all the information").showToast()
            } else {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val idCurrentUser = FirebaseAuth.getInstance().currentUser?.uid
                            idCurrentUser?.let {
                                FirebaseDatabase.getInstance().getReference("Users")
                                    .child(it)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            SuccessfulToast(requireContext(), "Login successfully!").showToast()
                                            Intent(requireContext(), HomeActivity::class.java).also {
                                                startActivity(it)
                                                requireActivity().finish()
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            // Handle onCancelled error
                                        }
                                    })
                            }
                        } else {
                            FailToast(requireContext(), "Wrong email or password!").showToast()
                        }
                    }
            }
        }

        view?.findViewById<TextView>(R.id.forgotpassText)?.setOnClickListener {
            val intent = Intent(requireContext(), ForgotActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }
}