package com.uteating.foodapp.fragment.Home

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.foodapp.Interface.APIService
import com.example.foodapp.RetrofitClient
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.FragmentSignUpBinding
import com.example.foodapp.dialog.LoadingDialog
import com.example.foodapp.model.Cart
import com.example.foodapp.model.User
import com.example.foodapp.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var dialog: LoadingDialog
    private lateinit var apiService: APIService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        binding.btnSignUp.setOnClickListener {
            if (check()) {
                apiService = RetrofitClient.retrofit!!.create(APIService::class.java)
                val phone = binding.edtPhone.text.toString()
                val name = binding.edtName.text.toString()
                val email = binding.edtEmail.text.toString()
                val pass = binding.edtPass.text.toString()

                dialog = LoadingDialog(requireContext())
                dialog.show()

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = task.result?.user?.uid
                            if (userId != null) {
                                val user = User(userId, email, "", name, "01/01/2000", phone)
                                val cartId = FirebaseDatabase.getInstance().reference.push().key ?: ""
                                val cart = Cart(cartId, 0, 0, userId)

                                val userDTO = UserDTO(userId, name, email, pass)
                                apiService.signUp(userDTO).enqueue(object : Callback<String> {
                                    override fun onResponse(call: Call<String>, response: Response<String>) {
                                        if (response.isSuccessful) {
                                            // Log success if needed
                                        } else {
                                            // Log failure if needed
                                        }
                                    }

                                    override fun onFailure(call: Call<String>, t: Throwable) {
                                        // Handle failure
                                    }
                                })


                                FirebaseDatabase.getInstance().getReference("Users")
                                    .child(userId)
                                    .setValue(user)
                                    .addOnCompleteListener { userTask ->
                                        if (userTask.isSuccessful) {

                                            FirebaseDatabase.getInstance().getReference("Carts")
                                                .child(cartId)
                                                .setValue(cart)

                                            SuccessfulToast(requireContext(), "Create account successfully").showToast()
                                            dialog.dismiss()
                                        } else {
                                            dialog.dismiss()
                                            FailToast(requireContext(), "Create account unsuccessfully").showToast()
                                        }
                                    }
                            } else {
                                dialog.dismiss()
                                FailToast(requireContext(), "Failed to get user ID").showToast()  // Thông báo nếu userId là null
                            }
                        } else {
                            dialog.dismiss()
                            FailToast(requireContext(), "Create account unsuccessfully").showToast()
                        }

                    }
            }
        }

        return binding.root
    }

    private fun check(): Boolean {
        val phone = binding.edtPhone.text.toString()
        val name = binding.edtName.text.toString()
        val email = binding.edtEmail.text.toString()
        val pass = binding.edtPass.text.toString()

        return when {
            phone.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty() -> {
                createDialog("Điền đầy đủ thông tin").show()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                createDialog("Email không đúng định dạng").show()
                false
            }
            !phone.matches("(03|05|07|08|09|01[2689])[0-9]{8}\\b".toRegex()) -> {
                createDialog("Số điện thoại không hợp lệ").show()
                false
            }
            else -> true
        }
    }

    private fun createDialog(message: String): AlertDialog {
        return AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setTitle("Notice")
            .setNegativeButton("Ok") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
    }
}
