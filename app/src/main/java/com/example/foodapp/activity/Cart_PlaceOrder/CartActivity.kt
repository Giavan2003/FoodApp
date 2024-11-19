package com.example.foodapp.activity.Cart_PlaceOrder

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.Interface.IAdapterItemListener
import com.example.foodapp.adapter.Cart.CartProductAdapter
import com.example.foodapp.databinding.ActivityCartBinding
import com.example.foodapp.model.Cart
import com.example.foodapp.model.CartInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartActivity : AppCompatActivity() {
    private lateinit var userId: String
    private lateinit var binding: ActivityCartBinding

    private lateinit var cartProductAdapter: CartProductAdapter

    private val cartInfoList: MutableList<CartInfo> = ArrayList()

    private var isCheckAll = false
    private var buyProducts = ArrayList<CartInfo?>()
    private lateinit var proceedOrderLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userId = intent.getStringExtra("userId") ?: ""
        initToolbar()
        initProceedOrderLauncher()



        binding.recyclerViewCartProduct.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@CartActivity)
        }
        getCartProducts()
        binding.proceedOrder.setOnClickListener {
            val intent = Intent(this@CartActivity, ProceedOrderActivity::class.java).apply {
                putExtra("buyProducts", buyProducts)
                putExtra("totalPrice", binding.totalPrice.text.toString())
                putExtra("userId", userId)
            }
            proceedOrderLauncher.launch(intent)
        }
    }

    private fun initProceedOrderLauncher() {
        proceedOrderLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    finish()
                }
            }
    }

    private fun reloadCartProducts() {
        FirebaseDatabase.getInstance().reference.child("Carts").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) { snapshot.children.forEach { ds ->
                        val cart = ds.getValue(Cart::class.java)
                        if (cart?.userId == userId) {
                            FirebaseDatabase.getInstance().reference.child("CartInfo's")
                                .child(cart.cartId.orEmpty())
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        cartInfoList.clear()
                                        snapshot.children.mapNotNullTo(cartInfoList) { it.getValue(CartInfo::class.java) }
                                        cartProductAdapter.notifyDataSetChanged()
                                    }
                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        cartProductAdapter.saveStates(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        cartProductAdapter.restoreStates(savedInstanceState)
    }

    private fun getCartProducts() {
        FirebaseDatabase.getInstance().reference.child("Carts").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) { snapshot.children.forEach { ds ->
                        val cart = ds.getValue(Cart::class.java)
                        if (cart?.userId == userId) {
                            cartProductAdapter = CartProductAdapter(this@CartActivity, cartInfoList, cart.cartId!!, isCheckAll, userId)
                            cartProductAdapter.setAdapterItemListener(object : IAdapterItemListener {
                                override fun onCheckedItemCountChanged(count: Int, price: Long, selectedItems: ArrayList<CartInfo?>?) {
                                    binding.totalPrice.text = "${convertToMoney(price)}đ"
                                    buyProducts = selectedItems ?: arrayListOf()
                                    binding.proceedOrder.isEnabled = count > 0
                                }

                                override fun onAddClicked() {
                                    reloadCartProducts()
                                }

                                override fun onSubtractClicked() {
                                    reloadCartProducts()
                                }

                                override fun onDeleteProduct() {
                                    reloadCartProducts()
                                }
                            })

                            binding.recyclerViewCartProduct.adapter = cartProductAdapter

                            FirebaseDatabase.getInstance().reference.child("CartInfo's")
                                .child(cart.cartId.orEmpty())
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        cartInfoList.clear()
                                        snapshot.children.mapNotNullTo(cartInfoList) { it.getValue(CartInfo::class.java) }
                                        cartProductAdapter.notifyDataSetChanged()
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun initToolbar() {
        window.apply {
            statusBarColor = Color.parseColor("#E8584D")
            navigationBarColor = Color.parseColor("#E8584D")
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "My cart"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun convertToMoney(price: Long): String {
        val temp = price.toString()
        val output = StringBuilder()
        var count = 3
        for (i in temp.indices.reversed()) {
            count--
            if (count == 0) {
                count = 3
                output.insert(0, ",${temp[i]}")
            } else {
                output.insert(0, temp[i])
            }
        }
        return if (output.startsWith(",")) output.substring(1) else output.toString()
    }
}

