package com.example.foodapp.activity.Cart_PlaceOrder

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.Interface.IAdapterItemListener
import com.example.foodapp.R
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
    private lateinit var cartId: String
    private lateinit var binding: ActivityCartBinding

    private lateinit var cartProductAdapter: CartProductAdapter
    private var cartInfoList = mutableListOf<CartInfo>()

    private var isCheckAll = false
    private var buyProducts = arrayListOf<CartInfo>()
    private lateinit var proceedOrderLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("userId") ?: ""
        cartId = intent.getStringExtra("cartId") ?: ""
        initToolbar()
        initProceedOrderLauncher()

        binding.recyclerViewCartProduct.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@CartActivity)
        }

        cartInfoList = mutableListOf()

        getCartProducts()

        binding.proceedOrder.setOnClickListener {
            val intent = Intent(this, ProceedOrderActivity::class.java).apply {
                putExtra("buyProducts", buyProducts)
                putExtra("totalPrice", binding.totalPrice.text.toString())
                putExtra("userId", userId)
            }
            proceedOrderLauncher.launch(intent)
        }
    }

    private fun initProceedOrderLauncher() {
        proceedOrderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                finish()
            }
        }
    }

    private fun reloadCartProducts() {
        FirebaseDatabase.getInstance().reference.child("Carts").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val cart = ds.getValue(Cart::class.java)
                    if (cart?.userId == userId) {
                        FirebaseDatabase.getInstance().reference.child("CartInfos").child(cartId!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    cartInfoList.clear()
                                    for (ds in snapshot.children) {
                                        val cartInfo = ds.getValue(CartInfo::class.java)
                                        cartInfo?.let { cartInfoList.add(it) }
                                    }
                                    cartInfoList.reverse()
                                    cartProductAdapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
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
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val cart = ds.getValue(Cart::class.java)

                    // In ra thông tin userId và cartId để kiểm tra
                    Log.d("CartActivity", "userId: $userId")
                    Log.d("CartActivity", "cartId: ${cart?.cartId}")

                    if (cart?.userId == userId) {
                        // Nếu cartId không null thì tiếp tục lấy thông tin CartInfos
                        Log.d("CartActivity", "Cart found for userId: $userId, cartId: ${cart.cartId}")

                        cartInfoList.clear()  // Xóa danh sách sản phẩm cũ
                        cartProductAdapter = CartProductAdapter(this@CartActivity, cartInfoList, cart.cartId!!, isCheckAll, userId).apply {
                            setAdapterItemListener(object : IAdapterItemListener {
                                override fun onCheckedItemCountChanged(count: Int, price: Long, selectedItems: ArrayList<CartInfo?>?) {
                                    binding.totalPrice.text = "${convertToMoney(price)}đ"
                                    buyProducts = ArrayList(selectedItems?.filterNotNull() ?: listOf())

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
                        }

                        binding.recyclerViewCartProduct.adapter = cartProductAdapter

                        FirebaseDatabase.getInstance().reference.child("CartInfos").child(cart.cartId!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    cartInfoList.clear()
                                    for (ds in snapshot.children) {
                                        val cartInfo = ds.getValue(CartInfo::class.java)
                                        cartInfo?.let { cartInfoList.add(it) }
                                    }
                                    cartInfoList.reverse()
                                    cartProductAdapter.notifyDataSetChanged()

                                    // In ra số lượng cartInfo đã được thêm vào danh sách
                                    Log.d("CartActivity", "Cart info size: ${cartInfoList.size}")
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("CartActivity", "Error getting CartInfos: ${error.message}")
                                }
                            })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CartActivity", "Error getting Carts: ${error.message}")
            }
        })
    }


    private fun initToolbar() {
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "My cart"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun convertToMoney(price: Long): String {
        val temp = price.toString()
        var output = ""
        var count = 3
        for (i in temp.length - 1 downTo 0) {
            count--
            if (count == 0) {
                count = 3
                output = ",${temp[i]}$output"
            } else {
                output = "${temp[i]}$output"
            }
        }
        return if (output.startsWith(",")) output.substring(1) else output
    }
}
