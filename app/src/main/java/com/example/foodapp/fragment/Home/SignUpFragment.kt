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

        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        binding.btnSignUp.setOnClickListener {
            if (check()) {
                val phone = binding.edtPhone.text.toString()
                val fulname = binding.edtFullName.text.toString()
                val username = binding.edtName.text.toString()
                val email = binding.edtEmail.text.toString()
                val pass = binding.edtPass.text.toString()

                dialog = LoadingDialog(requireContext())
                dialog.show()

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = task.result?.user?.uid
                            if (userId != null) {
                                val tmp = User(userId,fulname,email,pass,"","01/01/2000",username,phone,true)
                                val cartId =
                                    FirebaseDatabase.getInstance().reference.push().key ?: ""
                                val cart = Cart(cartId, 0, 0, userId)

                                FirebaseDatabase.getInstance().reference.child("Users")
                                    .child(tmp.userId!!)
                                    .setValue(tmp).addOnCompleteListener { userTask ->
                                        if (userTask.isSuccessful) {
                                            dialog.dismiss()
                                            FirebaseDatabase.getInstance().reference.child("Carts")
                                                .child(cart.cartId!!).setValue(cart)
                                            SuccessfulToast(
                                                requireContext(),
                                                "Create account successfully"
                                            ).showToast()
                                        } else {
                                            dialog.dismiss()
                                            FailToast(
                                                requireContext(),
                                                "Create account unsuccessfully"
                                            ).showToast()
                                        }
                                    }
                            } else {
                                dialog.dismiss()
                                FailToast(requireContext(), "Failed to get user ID").showToast()
                            }
                        } else {
                            createDialog("Email đã tồn tại").show()
                            dialog.dismiss()
                        }
                    }
            }
        }


            return binding.root
    }

    private fun check(): Boolean {
        val phone = binding.edtPhone.text.toString()
        val fullName = binding.edtFullName.text.toString()
        val userName = binding.edtName.text.toString()
        val email = binding.edtEmail.text.toString()
        val pass = binding.edtPass.text.toString()


        return when {
            phone.isEmpty() || fullName.isEmpty() || email.isEmpty() || pass.isEmpty() || userName.isEmpty() -> {
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

