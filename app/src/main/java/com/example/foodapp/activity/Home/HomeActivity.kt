package com.example.foodapp.activity.Home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.foodapp.R
import com.example.foodapp.activity.Cart_PlaceOrder.CartActivity
import com.example.foodapp.activity.MyShop.MyShopActivity
import com.example.foodapp.activity.order.OrderActivity
import com.example.foodapp.custom.CustomMessageBox.CustomAlertDialog
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ActivityHomeBinding
import com.example.foodapp.fragment.Home.FavoriteFragment
import com.example.foodapp.fragment.Home.HomeFragment
import com.example.foodapp.fragment.NotificationFragment
import com.example.foodapp.model.Cart
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import meow.bottomnavigation.MeowBottomNavigation

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var userId: String
    private lateinit var binding: ActivityHomeBinding
    private lateinit var layoutMain: LinearLayout
    private var selectionFragment: Fragment? = null

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 10023
        private const val STORAGE_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy userId từ Firebase Auth
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // In userId ra console
        Log.d("aaaaaaaaaaaaaaaaaaaaaaa", "User ID: $userId")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission(Manifest.permission.POST_NOTIFICATIONS, 101)
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 102)
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 103)
        }

        initUI()
        loadInformationForNavigationBar()
    }

    private fun initUI() {
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")
        binding.navigationLeft.bringToFront()
        createActionBar()

        layoutMain = binding.layoutMain
        supportFragmentManager.beginTransaction()
            .replace(layoutMain.id, HomeFragment(userId))
            .commit()

        setEventNavigationBottom()
        setCartNavigation()
        binding.navigationLeft.setNavigationItemSelectedListener(this)
    }

    private fun setCartNavigation() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.message_menu -> {
                    val intent = Intent(this@HomeActivity, ChatActivity::class.java)
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                }
                R.id.cart_menu -> {

                    FirebaseDatabase.getInstance().reference.child("Carts")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var cartFound = false
                                snapshot.children.forEach { ds ->
                                    val cart = ds.getValue(Cart::class.java)
                                    if (cart?.userId == userId) {
                                        cartFound = true
                                        val cartId = ds.key ?: "" // Lấy cartId từ key của dữ liệu
                                        Log.d("HomeActivity", "Cart ID: $cartId") // In cartId ra console
                                        // Mở CartActivity và truyền userId, cartId vào
                                        val intent = Intent(this@HomeActivity, CartActivity::class.java).apply {
                                            putExtra("userId", userId)
                                            putExtra("cartId", cartId) // Gửi cartId vào Intent
                                        }
                                        startActivity(intent)
                                    }
                                }
                                if (!cartFound) {
                                    // Nếu không tìm thấy giỏ hàng, không làm gì (bỏ EmptyCartActivity)
                                    Log.d("HomeActivity", "No cart found for user: $userId")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Xử lý lỗi nếu cần
                                Log.e("HomeActivity", "Firebase error: ${error.message}")
                            }
                        })
                }
            }
            true
        }
    }

    private fun setEventNavigationBottom() {
        binding.bottomNavigation.show(2, true)
        binding.bottomNavigation.add(MeowBottomNavigation.Model(1, R.drawable.ic_favourite))
        binding.bottomNavigation.add(MeowBottomNavigation.Model(2, R.drawable.ic_home))
        binding.bottomNavigation.add(MeowBottomNavigation.Model(3, R.drawable.notification_icon))

        binding.bottomNavigation.setOnClickMenuListener { model ->
            selectionFragment = when (model.id) {
                1 -> FavoriteFragment(userId)
                2 -> HomeFragment(userId)
                3 -> NotificationFragment(userId)
                else -> null
            }
            selectionFragment?.let {
                supportFragmentManager.beginTransaction().replace(layoutMain.id, it).commit()
            }
        }

        binding.bottomNavigation.setOnShowListener { model ->
            selectionFragment = when (model.id) {
                1 -> FavoriteFragment(userId)
                2 -> HomeFragment(userId)
                3 -> NotificationFragment(userId)
                else -> null
            }
            selectionFragment?.let {
                supportFragmentManager.beginTransaction().replace(layoutMain.id, it).commit()
            }
        }
    }

    private fun createActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.menu_icon)
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home_top, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            binding.drawLayoutHome.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            R.id.profileMenu -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
            R.id.orderMenu -> {
                val intent = Intent(this, OrderActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
            R.id.myShopMenu -> {
                val intent = Intent(this, MyShopActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
            R.id.logoutMenu -> {
                CustomAlertDialog(this, "Do you want to logout?").apply {
                    CustomAlertDialog.binding.btnYes.setOnClickListener {
                        SuccessfulToast(this@HomeActivity, "Logout successfully!").showToast()
                        CustomAlertDialog.alertDialog.dismiss()
                        FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(this@HomeActivity, LoginActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
                        finish()
                    }
                    CustomAlertDialog.binding.btnNo.setOnClickListener {
                        CustomAlertDialog.alertDialog.dismiss()
                    }
                }
                CustomAlertDialog.showAlertDialog()
            }
            else -> {

            }
        }
        binding.drawLayoutHome.close()
        return true
    }

    private fun loadInformationForNavigationBar() {
        // Phương thức này có thể được sử dụng để tải thêm thông tin cần thiết cho thanh điều hướng
    }
}
