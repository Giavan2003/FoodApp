package com.example.foodapp.activity.Cart_PlaceOrder

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.GlobalConfig
import com.example.foodapp.R
import com.example.foodapp.adapter.Cart.OrderProductAdapter
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.databinding.ActivityProceedOrderBinding
import com.example.foodapp.helper.FirebaseNotificationHelper
import com.example.foodapp.model.Address
import com.example.foodapp.model.Bill
import com.example.foodapp.model.CartInfo
import com.example.foodapp.model.Notification
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProceedOrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProceedOrderBinding
    private lateinit var orderProductAdapter: OrderProductAdapter
    private lateinit var cartInfoList: ArrayList<CartInfo>
    private lateinit var totalPriceDisplay: String
    private lateinit var userId: String
    private lateinit var changeAddressLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProceedOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initChangeAddressActivity()

        binding.recyclerViewOrderProduct.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@ProceedOrderActivity)
        }

        cartInfoList = intent.getSerializableExtra("buyProducts") as ArrayList<CartInfo>
        orderProductAdapter = OrderProductAdapter(this, cartInfoList)
        binding.recyclerViewOrderProduct.adapter = orderProductAdapter
        totalPriceDisplay = intent.getStringExtra("totalPrice") ?: ""
        userId = intent.getStringExtra("userId") ?: ""

        loadInfo()

        binding.complete.setOnClickListener {
            if (validateDate()) {
                handleCompleteOrder()
            }
        }

        binding.change.setOnClickListener {
            val intent = Intent(this, ChangeAddressActivity::class.java).apply {
                putExtra("userId", userId)
            }
            changeAddressLauncher.launch(intent)
        }
    }

    private fun validateDate(): Boolean {
        return if (GlobalConfig.choseAddressId == null) {
            FailToast(this, "You must choose delivery address!").showToast()
            false
        } else {
            true
        }
    }

    private fun initChangeAddressActivity() {
        changeAddressLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadAddressData()
            }
        }
    }

    private fun loadAddressData() {
        GlobalConfig.choseAddressId?.let { addressId ->
            FirebaseDatabase.getInstance().reference.child("Address").child(userId).child(addressId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.getValue(Address::class.java)?.let { address ->
                            binding.receiverName.text = address.receiverName
                            binding.detailAddress.text = address.detailAddress
                            binding.receiverPhoneNumber.text = address.receiverPhoneNumber
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun initToolbar() {
        window.apply {
            statusBarColor = Color.parseColor("#E8584D")
            navigationBarColor = Color.parseColor("#E8584D")
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Proceed order"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun loadInfo() {
        binding.totalPrice.text = totalPriceDisplay

        FirebaseDatabase.getInstance().reference.child("Address").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val address = ds.getValue(Address::class.java)
                        if (address?.state == "default") {
                            GlobalConfig.choseAddressId = address.addressId
                            binding.receiverName.text = address.receiverName
                            binding.detailAddress.text = address.detailAddress
                            binding.receiverPhoneNumber.text = address.receiverPhoneNumber
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun handleCompleteOrder() {
        // Implement your order completion logic here
    }

    private fun pushNotificationCartCompleteForSeller(bill: Bill) {
        val title2 = "New order"
        val content2 = "Hurry up! There is a new order. Go to Delivery Manage for customer serving!"
        val notification2 = FirebaseNotificationHelper.createNotification(title2,
            content2, bill.imageUrl ?: "", "None", "None", bill.billId ?: "", null)
        FirebaseNotificationHelper(this).addNotification(bill.senderId!!, notification2, object : FirebaseNotificationHelper.DataStatus {
            override fun DataIsLoaded(notificationList: List<Notification>, notificationListToNotify: List<Notification>) {}
            override fun DataIsInserted() {}
            override fun DataIsUpdated() {}
            override fun DataIsDeleted() {}
        })
    }
}
