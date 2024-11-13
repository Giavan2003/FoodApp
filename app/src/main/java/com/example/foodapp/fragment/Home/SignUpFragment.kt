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
    private var binding: FragmentSignUpBinding? = null
    private var dialog: LoadingDialog? = null
    private var apiService: APIService? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val view = binding?.root

        binding?.btnSignUp?.setOnClickListener {
            if (check()) {

                val apiService: APIService = RetrofitClient.retrofit!!.create(APIService::class.java)
                val phone = binding?.edtPhone?.text.toString()
                val name = binding?.edtName?.text.toString()
                val email = binding?.edtEmail?.text.toString()
                val pass = binding?.edtPass?.text.toString()
                dialog = LoadingDialog(requireContext())
                dialog?.show()

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.user?.uid?.let { uid ->
                                SuccessfulToast(requireContext(), uid).showToast()
                                val tmp = User(uid, email, "", name, "01/01/2000", phone)
                                val cart = Cart(FirebaseDatabase.getInstance().reference.push().key ?: "", 0, 0, uid)
                                val userDTO = UserDTO(uid, name, email, pass)

                                apiService?.signUp(userDTO)?.enqueue(object : Callback<String> {
                                    override fun onResponse(call: Call<String>, response: Response<String>) {
                                        if (response.isSuccessful) {
                                            Log.d("noti", "Thanh cong")
                                        } else {
                                            Log.d("noti", "That bai")
                                        }
                                    }

                                    override fun onFailure(call: Call<String>, t: Throwable) {
                                        Log.d("noti", "Failure: ${t.message}")
                                    }
                                })

                                FirebaseDatabase.getInstance().getReference("Users").child(tmp.userId!!)
                                    .setValue(tmp).addOnCompleteListener { task ->
                                        dialog?.dismiss()
                                        if (task.isSuccessful) {
                                            FirebaseDatabase.getInstance().getReference("Carts").child(cart.cartId!!)
                                                .setValue(cart)
                                            SuccessfulToast(requireContext(), "Create account successfully").showToast()
                                        } else {
                                            FailToast(requireContext(), "Create account unsuccessfully").showToast()
                                        }
                                    }
                            }
                        } else {
                            dialog?.dismiss()
                            FailToast(requireContext(), "Create account unsuccessfully").showToast()
                            Log.w("REGISTER", "createUserWithEmail:failure", task.exception)
                        }
                    }
            }
        }

        return view
    }

    private fun check(): Boolean {
        val phone = binding?.edtPhone?.text.toString()
        val name = binding?.edtName?.text.toString()
        val email = binding?.edtEmail?.text.toString()
        val pass = binding?.edtPass?.text.toString()

        return when {
            phone.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty() -> {
                createDialog("Điền đầy đủ thông tin").show()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                createDialog("Email không đúng định dạng").show()
                false
            }
            !phone.matches(Regex("(03|05|07|08|09|01[2689])[0-9]{8}\\b")) -> {
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
            .setNegativeButton("Ok") { dialogInterface, _ -> dialogInterface.dismiss() }
            .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
