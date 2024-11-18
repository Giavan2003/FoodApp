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
import com.example.foodapp.activity.Home.HomeActivity
import com.example.foodapp.adapter.Cart.OrderProductAdapter
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ActivityProceedOrderBinding
import com.example.foodapp.helper.FirebaseNotificationHelper
import com.example.foodapp.model.Address
import com.example.foodapp.model.Bill
import com.example.foodapp.model.Cart
import com.example.foodapp.model.CartInfo
import com.example.foodapp.model.Notification
import com.example.foodapp.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

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

        binding.recyclerViewOrderProduct.setHasFixedSize(true)
        binding.recyclerViewOrderProduct.layoutManager = LinearLayoutManager(this)
        cartInfoList = intent.getSerializableExtra("buyProducts") as ArrayList<CartInfo>
        orderProductAdapter = OrderProductAdapter(this, cartInfoList)
        binding.recyclerViewOrderProduct.adapter = orderProductAdapter
        totalPriceDisplay = intent.getStringExtra("totalPrice").toString()
        userId = intent.getStringExtra("userId").toString()

        loadInfo()

        binding.complete.setOnClickListener {
            if (validateDate()) {
                val cartInfoMap = hashMapOf<String, CartInfo>()
                cartInfoList.forEach { cartInfo ->
                    cartInfoMap[cartInfo.productId!!] = cartInfo
                }

                val cartInfoKeySet = cartInfoMap.keys
                val filterCartInfoMap = hashMapOf<String, MutableList<CartInfo>>()
                val filterCartInfoPriceMap = hashMapOf<String, Long>()
                val filterCartInfoImageUrlMap = hashMapOf<String, String>()

                FirebaseDatabase.getInstance().reference.child("Products")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.children.forEach { ds ->
                                val product = ds.getValue(Product::class.java)
                                cartInfoKeySet.forEach { productId ->
                                    if (product?.productId == productId) {
                                        val publisherId = product.publisherId ?: return
                                        if (filterCartInfoMap.containsKey(publisherId)) {
                                            cartInfoMap[productId]?.let { filterCartInfoMap[publisherId]?.add(it)
                                            }
                                            val totalPrice = filterCartInfoPriceMap[publisherId] ?: 0L
                                            val productAmount = cartInfoMap[productId]?.amount ?: 0
                                            filterCartInfoPriceMap[publisherId] = totalPrice + product.productPrice * productAmount.toLong()

                                        } else {
                                            cartInfoMap[productId]?.let { filterCartInfoMap[publisherId] = mutableListOf(it)
                                            }
                                            val productAmount = cartInfoMap[productId]?.amount ?: 0
                                            filterCartInfoPriceMap[publisherId] = product.productPrice * productAmount.toLong()
                                            filterCartInfoImageUrlMap[publisherId] = product.productImage1 ?: ""
                                        }


                                    }
                                }
                            }

                            val filterCartInfoKeySet = filterCartInfoMap.keys
                            filterCartInfoKeySet.forEach { senderId ->
                                val billId = FirebaseDatabase.getInstance().reference.push().key
                                val formatter = SimpleDateFormat("dd/MM/yyyy")
                                val date = Date()
                                val bill = Bill(
                                    GlobalConfig.choseAddressId, billId, formatter.format(date), "Confirm",
                                    false, userId, senderId, filterCartInfoPriceMap[senderId]!!,
                                    filterCartInfoImageUrlMap[senderId]!!
                                )
                                FirebaseDatabase.getInstance().reference.child("Bills").child(billId!!)
                                    .setValue(bill).addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            filterCartInfoMap[senderId]?.forEach { cartInfo ->
                                                val billInfoId = FirebaseDatabase.getInstance().reference.push().key
                                                val map1 = hashMapOf<String, Any>(
                                                    "amount" to cartInfo.amount,
                                                    "billInfoId" to billInfoId!!,
                                                    "productId" to cartInfo.productId!!
                                                )
                                                FirebaseDatabase.getInstance().reference.child("BillInfos")
                                                    .child(billId).child(billInfoId).setValue(map1)

                                                FirebaseDatabase.getInstance().reference.child("Carts")
                                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            snapshot.children.forEach { ds ->
                                                                val cart = ds.getValue(Cart::class.java)
                                                                if (cart?.userId == userId) {
                                                                    FirebaseDatabase.getInstance().reference.child("CartInfos")
                                                                        .child(cart.cartId!!).child(cartInfo.cartInfoId!!)
                                                                        .removeValue()
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })
                                            }
                                        }
                                    }
                                pushNotificationCartCompleteForSeller(bill)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })

                FirebaseDatabase.getInstance().reference.child("Products")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot1: DataSnapshot) {
                            FirebaseDatabase.getInstance().reference.child("Carts")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot2: DataSnapshot) {
                                        var totalAmount = 0
                                        var totalPrice: Long = 0
                                        snapshot1.children.forEach { ds ->
                                            val product = ds.getValue(Product::class.java)
                                            cartInfoList.forEach { cartInfo ->
                                                if (cartInfo.productId == product?.productId) {
                                                    totalAmount += cartInfo.amount
                                                    totalPrice += (cartInfo.amount * (product?.productPrice?.toLong() ?: 0L))

                                                }
                                            }
                                        }

                                        snapshot2.children.forEach { ds ->
                                            val cart = ds.getValue(Cart::class.java)
                                            if (cart?.userId == userId) {
                                                FirebaseDatabase.getInstance().reference.child("Carts")
                                                    .child(cart.cartId!!).child("totalAmount")
                                                    .setValue(cart.totalAmount - totalAmount)
                                                FirebaseDatabase.getInstance().reference.child("Carts")
                                                    .child(cart.cartId!!).child("totalPrice")
                                                    .setValue(cart.totalPrice - totalPrice)
                                            }
                                        }
                                        SuccessfulToast(this@ProceedOrderActivity, "Order created successfully!").showToast()

                                        cartInfoList.clear()
                                        val intent = Intent(this@ProceedOrderActivity, HomeActivity::class.java)
                                        setResult(RESULT_OK, intent)
                                        finish()
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
        }

        binding.change.setOnClickListener {
            val intent = Intent(this@ProceedOrderActivity, ChangeAddressActivity::class.java)
            intent.putExtra("userId", userId)
            changeAddressLauncher.launch(intent)
        }
    }

    private fun validateDate(): Boolean {
        if (GlobalConfig.choseAddressId == null) {
            FailToast(this, "You must choose delivery address!").showToast()
            return false
        }
        return true
    }

    private fun initChangeAddressActivity() {
        changeAddressLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadAddressData()
            }
        }
    }

    private fun loadAddressData() {
        if (GlobalConfig.choseAddressId != null) {
            FirebaseDatabase.getInstance().reference.child("Address")
                .child(userId).child(GlobalConfig.choseAddressId!!).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val address = snapshot.getValue(Address::class.java)
                        binding.receiverName.text = address?.receiverName
                        binding.detailAddress.text = address?.detailAddress
                        binding.receiverPhoneNumber.text = address?.receiverPhoneNumber
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun initToolbar() {
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Proceed order"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun loadInfo() {
        binding.totalPrice.text = totalPriceDisplay

        FirebaseDatabase.getInstance().reference.child("Address").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { ds ->
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

