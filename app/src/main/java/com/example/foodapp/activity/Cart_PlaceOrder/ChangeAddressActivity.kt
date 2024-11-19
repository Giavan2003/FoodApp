package com.example.foodapp.activity.Cart_PlaceOrder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.GlobalConfig
import com.example.foodapp.Interface.IAddressAdapterListener
import com.example.foodapp.R
import com.example.foodapp.adapter.AddressAdapter
import com.example.foodapp.custom.CustomMessageBox.CustomAlertDialog
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ActivityChangeAddressBinding
import com.example.foodapp.databinding.ItemAddressBinding
import com.example.foodapp.model.Address
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChangeAddressActivity : AppCompatActivity() {
    private lateinit var userId: String
    private lateinit var binding: ActivityChangeAddressBinding
    private lateinit var addressAdapter: AddressAdapter
    private lateinit var addressList: MutableList<Address>
    private lateinit var updateAddAddressLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val UPDATE_ADDRESS_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("userId") ?: throw IllegalArgumentException("userId is required")


        initToolbar()
        initUpdateAddAddressActivity()

        binding.recyclerViewAddress.setHasFixedSize(true)
        binding.recyclerViewAddress.layoutManager = LinearLayoutManager(this)
        addressList = mutableListOf()
        addressAdapter = AddressAdapter(this, addressList, userId).apply {
            setAddressAdapterListener(object : IAddressAdapterListener {
                override fun onCheckedChanged(selectedAddress: Address) {
                    GlobalConfig.choseAddressId = selectedAddress.addressId
                    setResult(RESULT_OK, Intent())
                    finish()
                }

                override fun onDeleteAddress() {
                    loadInfo()
                }
            })
        }
        binding.recyclerViewAddress.adapter = addressAdapter

        loadInfo()

        binding.add1.setOnClickListener {
            FirebaseDatabase.getInstance().reference.child("Address").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val intent = Intent(this@ChangeAddressActivity, UpdateAddAddressActivity::class.java).apply {
                            putExtra("userId", userId)
                            putExtra("mode", if (snapshot.childrenCount == 0L) "add - default" else "add - non-default")
                        }
                        updateAddAddressLauncher.launch(intent)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UPDATE_ADDRESS_REQUEST_CODE) {
            loadInfo()
        }
    }

    private fun initUpdateAddAddressActivity() {
        updateAddAddressLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadInfo()
            }
        }
    }

    private fun loadInfo() {
        FirebaseDatabase.getInstance().reference.child("Address").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    addressList.clear()


                    snapshot.children.forEach { ds ->
                        val address = ds.getValue(Address::class.java)
                        if (address != null) {

                            if (address.addressId == GlobalConfig.choseAddressId) {
                                addressList.add(0, address)
                            } else {

                                addressList.add(address)
                            }
                        }
                    }


                    addressAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


    private fun initToolbar() {
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Change address"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            setResult(RESULT_OK, Intent())
            finish()
        }
    }
}



