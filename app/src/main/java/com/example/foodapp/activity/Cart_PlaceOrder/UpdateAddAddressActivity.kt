package com.example.foodapp.activity.Cart_PlaceOrder

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.GlobalConfig
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ActivityUpdateAddAddressBinding
import com.example.foodapp.model.Address
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Patterns

class UpdateAddAddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateAddAddressBinding
    private lateinit var userId: String
    private lateinit var mode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateAddAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("userId") ?: ""
        mode = intent.getStringExtra("mode") ?: ""

        initToolbar()

        when (mode) {
            "add - default" -> {
                binding.updateComplete.text = "Complete"
                binding.setDefault.isChecked = true
                binding.setDefault.isEnabled = false
            }
            "add - non-default" -> {
                binding.updateComplete.text = "Complete"
            }
            "update" -> {
                binding.updateComplete.text = "Update"
                FirebaseDatabase.getInstance().reference.child("Address").child(userId)
                    .child(GlobalConfig.updateAddressId!!)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val address = snapshot.getValue(Address::class.java)
                            address?.let {
                                binding.fullName.setText(it.receiverName)
                                binding.phoneNumber.setText(it.receiverPhoneNumber)
                                binding.detailAddress.setText(it.detailAddress)
                                if (it.state == "default") {
                                    binding.setDefault.isEnabled = false
                                    binding.setDefault.isChecked = true
                                } else {
                                    binding.setDefault.isChecked = false
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
        }

        binding.updateComplete.setOnClickListener {
            if (validateAddressInfo()) {
                if (binding.updateComplete.text == "Complete") {
                    handleAddAddress()
                } else {
                    handleUpdateAddress()
                }
            }
        }
    }

    private fun handleAddAddress() {
        val addressId = FirebaseDatabase.getInstance().reference.push().key ?: return
        GlobalConfig.choseAddressId = addressId

        val address = Address(
            addressId,
            binding.detailAddress.text.toString().trim(),
            if (binding.setDefault.isChecked) "default" else "",
            binding.fullName.text.toString().trim(),
            binding.phoneNumber.text.toString().trim()
        )

        FirebaseDatabase.getInstance().reference.child("Address").child(userId).child(addressId)
            .setValue(address).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (binding.setDefault.isChecked) {
                        updateOtherAddressesToNonDefault(addressId)
                    }
                    showToastAndFinish("Added new address!")
                }
            }
    }

    private fun handleUpdateAddress() {
        val address = Address(
            GlobalConfig.updateAddressId,
            binding.detailAddress.text.toString().trim(),
            if (binding.setDefault.isChecked) "default" else "",
            binding.fullName.text.toString().trim(),
            binding.phoneNumber.text.toString().trim()
        )

        FirebaseDatabase.getInstance().reference.child("Address").child(userId).child(GlobalConfig.updateAddressId!!)
            .setValue(address).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (binding.setDefault.isChecked) {
                        updateOtherAddressesToNonDefault(GlobalConfig.updateAddressId!!)
                    }
                    showToastAndFinish("Updated chosen address!")
                }
            }
    }

    private fun updateOtherAddressesToNonDefault(currentAddressId: String) {
        FirebaseDatabase.getInstance().reference.child("Address").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val address = ds.getValue(Address::class.java)
                        if (address != null && address.addressId != currentAddressId) {
                            FirebaseDatabase.getInstance().reference.child("Address").child(userId)
                                .child(address.addressId!!).child("state").setValue("")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showToastAndFinish(message: String) {
        SuccessfulToast(this, message).showToast()
        setResult(RESULT_OK, Intent())
        finish()
    }

    private fun initToolbar() {
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = if (mode == "update") "Update address" else "Add address"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            setResult(RESULT_OK, Intent())
            finish()
        }
    }

    private fun validateAddressInfo(): Boolean {
        return when {
            binding.fullName.text.isNullOrEmpty() -> {
                FailToast(this, "Tên người nhận không được phép để trống!").showToast()
                false
            }
            !isValidString( binding.fullName.text.toString()) ->{
                FailToast(this, "Tên không hợp lệ!").showToast()
                false
            }
            binding.phoneNumber.text.isNullOrEmpty() -> {
                FailToast(this, "Vui lòng điền đầy đủ số điẹn thoại!").showToast()
                false
            }
            !isValidPhoneNumber(binding.phoneNumber.text.toString()) -> {
                FailToast(this, "Số điện thoại không hợp lệ!").showToast()
                false
            }
            binding.detailAddress.text.isNullOrEmpty() -> {
                FailToast(this, "Địa chỉ không được phép để trống").showToast()
                false
            }

            else -> true
        }
    }
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val phonePattern = "^[0-9]{10}$"
        return phoneNumber.matches(Regex(phonePattern))
    }
    private fun isValidString(input: String): Boolean {
        val pattern = "^[a-zA-Z\\s]+$"
        return input.matches(Regex(pattern))
    }

}


