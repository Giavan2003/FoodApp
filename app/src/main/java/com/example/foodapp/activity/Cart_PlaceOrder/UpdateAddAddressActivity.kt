package com.example.foodapp.activity.Cart_PlaceOrder

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.foodapp.GlobalConfig
import com.example.foodapp.R
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
        setupUIForMode()

        binding.updateComplete.setOnClickListener {
            if (validateAddressInfo()) {
                handleAddressUpdateOrAdd()
            }
        }
    }

    private fun initToolbar() {
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = if (mode == "update") "Update address" else "Add address"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun setupUIForMode() {
        when (mode) {
            "add - default" -> setupAddDefaultUI()
            "add - non-default" -> setupAddNonDefaultUI()
            "update" -> setupUpdateUI()
        }
    }

    private fun setupAddDefaultUI() {
        binding.updateComplete.text = "Complete"
        binding.setDefault.isChecked = true
        binding.setDefault.isEnabled = false
    }

    private fun setupAddNonDefaultUI() {
        binding.updateComplete.text = "Complete"
    }

    private fun setupUpdateUI() {
        binding.updateComplete.text = "Update"
        val addressId = GlobalConfig.updateAddressId
        if (addressId != null) {
            loadAddressDetails(addressId)
        }
    }

    private fun loadAddressDetails(addressId: String) {
        FirebaseDatabase.getInstance().getReference()
            .child("Address")
            .child(userId)
            .child(addressId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val address = snapshot.getValue(Address::class.java)
                    address?.let { populateAddressFields(it) }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun populateAddressFields(address: Address) {
        binding.fullName.setText(address.receiverName)
        binding.phoneNumber.setText(address.receiverPhoneNumber)
        binding.detailAddress.setText(address.detailAddress)
        if (address.state == "default") {
            binding.setDefault.isEnabled = false
            binding.setDefault.isChecked = true
        } else {
            binding.setDefault.isChecked = false
        }
    }

    private fun handleAddressUpdateOrAdd() {
        val addressId = FirebaseDatabase.getInstance().getReference().push().key ?: ""
        GlobalConfig.choseAddressId = addressId

        val temp = Address(
            addressId,
            binding.detailAddress.text.toString().trim(),
            if (binding.setDefault.isChecked) "default" else "",
            binding.fullName.text.toString().trim(),
            binding.phoneNumber.text.toString().trim()
        )

        if (mode == "add - default" || mode == "add - non-default") {
            addNewAddress(temp)
        } else {
            updateAddress(temp, addressId)
        }
    }

    private fun addNewAddress(address: Address) {
        val addressId = address.addressId
        if (addressId != null) {
            FirebaseDatabase.getInstance().getReference()
                .child("Address")
                .child(userId)
                .child(addressId)
                .setValue(address)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        handlePostAddSuccess(address)
                    }
                }
        } else {

            Log.e("AddNewAddress", "Address ID is null!")
        }
    }


    private fun handlePostAddSuccess(address: Address) {
        if (address.state == "default") {
            updateOtherAddressesToNonDefault(address)
        }
        SuccessfulToast(this, "Added new address!").showToast()
        GlobalConfig.choseAddressId = address.addressId
        setResult(RESULT_OK)
        finish()
    }

    private fun updateOtherAddressesToNonDefault(address: Address) {
        FirebaseDatabase.getInstance().getReference()
            .child("Address")
            .child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val updates = mutableMapOf<String, Any>()
                    for (ds in snapshot.children) {
                        val existingAddress = ds.getValue(Address::class.java)
                        if (existingAddress?.addressId != address.addressId) {
                            updates["/Address/$userId/${existingAddress?.addressId}/state"] = ""
                        }
                    }

                    if (updates.isNotEmpty()) {
                        FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun updateAddress(address: Address, addressId: String) {
        FirebaseDatabase.getInstance().getReference()
            .child("Address")
            .child(userId)
            .child(addressId)
            .setValue(address)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    SuccessfulToast(this, "Updated chosen address!").showToast()
                    setResult(RESULT_OK)
                    finish()
                }
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
