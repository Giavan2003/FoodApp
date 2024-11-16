package com.example.foodapp.activity.Cart_PlaceOrder

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.foodapp.GlobalConfig
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ActivityUpdateAddAddressBinding
import com.example.foodapp.model.Address
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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

                FirebaseDatabase.getInstance().reference.child("Address")
                    .child(userId).child(GlobalConfig.updateAddressId!!)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val address = snapshot.getValue(Address::class.java)
                            address?.let {
                                binding.fullName.setText(it.receiverName)
                                binding.phoneNumber.setText(it.receiverPhoneNumber)
                                binding.detailAddress.setText(it.detailAddress)
                                if (it.state == "default") {
                                    binding.setDefault.isChecked = true
                                    binding.setDefault.isEnabled = false
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
                    handleCompleteMode()
                } else {
                    handleUpdateMode()
                }
            }
        }
    }

    private fun handleCompleteMode() {
        val addressId = FirebaseDatabase.getInstance().reference.push().key ?: return
        GlobalConfig.choseAddressId = addressId

        val temp = Address(
            addressId,
            binding.detailAddress.text.toString().trim(),
            if (binding.setDefault.isChecked) "default" else "",
            binding.fullName.text.toString().trim(),
            binding.phoneNumber.text.toString().trim()
        )

        FirebaseDatabase.getInstance().reference.child("Address").child(userId).child(addressId)
            .setValue(temp).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (binding.setDefault.isChecked) {
                        updateOtherAddresses(addressId)
                    }
                    SuccessfulToast(this, "Added new address!").showToast()
                    GlobalConfig.choseAddressId = addressId
                    setResult(RESULT_OK, Intent())
                    finish()
                }
            }
    }

    private fun handleUpdateMode() {
        val temp = Address(
            GlobalConfig.updateAddressId,
            binding.detailAddress.text.toString().trim(),
            if (binding.setDefault.isChecked) "default" else "",
            binding.fullName.text.toString().trim(),
            binding.phoneNumber.text.toString().trim()
        )

        FirebaseDatabase.getInstance().reference.child("Address").child(userId).child(GlobalConfig.updateAddressId!!)
            .setValue(temp).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (binding.setDefault.isChecked) {
                        updateOtherAddresses(GlobalConfig.updateAddressId!!)
                    }
                    SuccessfulToast(this, "Updated chosen address!").showToast()
                    setResult(RESULT_OK, Intent())
                    finish()
                }
            }
    }

    private fun updateOtherAddresses(currentAddressId: String) {
        FirebaseDatabase.getInstance().reference.child("Address").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val address = ds.getValue(Address::class.java)
                        if (address != null && address.addressId != currentAddressId) {
                            FirebaseDatabase.getInstance().reference.child("Address")
                                .child(userId).child(address.addressId!!).child("state").setValue("")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
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
            binding.fullName.text.toString().isEmpty() -> {
                FailToast(this, "Receiver name must not be empty!").showToast()
                false
            }
            binding.phoneNumber.text.toString().isEmpty() -> {
                FailToast(this, "Receiver phone number must not be empty!").showToast()
                false
            }
            binding.detailAddress.text.toString().isEmpty() -> {
                FailToast(this, "Detail address must not be empty!").showToast()
                false
            }
            else -> true
        }
    }
}
