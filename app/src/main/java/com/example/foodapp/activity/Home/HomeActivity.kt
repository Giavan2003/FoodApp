package com.example.foodapp.activity.Cart_PlaceOrder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.foodapp.Interface.APIService
import com.example.foodapp.R
import com.example.foodapp.RetrofitClient
import com.example.foodapp.activity.Home.ChatActivity
import com.example.foodapp.activity.Home.LoginActivity
import com.example.foodapp.custom.CustomMessageBox.CustomAlertDialog
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ActivityHomeBinding
import com.example.foodapp.fragment.Home.HomeFragment
import com.example.foodapp.model.Cart
import com.example.foodapp.model.User

import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import meow.bottomnavigation.MeowBottomNavigation

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        //binding.navigationLeft.setNavigationItemSelectedListener(this@HomeActivity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this@HomeActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@HomeActivity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }

            if (ContextCompat.checkSelfPermission(this@HomeActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@HomeActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 102)
            }

            if (ContextCompat.checkSelfPermission(this@HomeActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@HomeActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 103)
            }
        }
        initUI()
        //loadInformationForNavigationBar()

    }
    private fun initUI() {
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")
        binding.navigationLeft.bringToFront()
        createActionBar()
        //layoutMain = binding.layoutMain

//        supportFragmentManager.beginTransaction()
//            .replace(layoutMain.id, HomeFragment(userId))
//            .commit()

        //setEventNavigationBottom()
        setCartNavigation()
        //binding.navigationLeft.setNavigationItemSelectedListener(this)
    }
    private fun createActionBar() {
        Log.d("Thông báo", "Đã thực hiện create action bar")
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.menu_icon)
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home_top, menu) // Thay đổi toolbar_menu bằng tên file menu của bạn
        return true
    }

    private fun setCartNavigation() {
        Log.d("Thông báo", "Đã set action bar")
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.message_menu -> {
                    val intent = Intent(this@HomeActivity, ChatActivity::class.java)
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                    true
                }
                R.id.cart_menu -> {
                    FirebaseDatabase.getInstance().getReference("Carts")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (ds in snapshot.children) {
                                    val cart = ds.getValue(Cart::class.java)
                                    if (cart?.userId == userId) {
                                        cart.cartId?.let {
                                            FirebaseDatabase.getInstance().getReference("CartInfos")
                                                .child(it)
                                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        val intent = if (snapshot.childrenCount == 0L) {
                                                            Intent(this@HomeActivity, EmptyCartActivity::class.java)
                                                        } else {
                                                            Intent(this@HomeActivity, CartActivity::class.java).apply {
                                                                putExtra("userId", userId)
                                                            }
                                                        }
                                                        startActivity(intent)
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {
                                                        // Handle error if needed
                                                    }
                                                })
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error if needed
                            }
                        })
                    true
                }
                else -> false
            }
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logoutMenu -> {
                showLogoutDialog()
                true
            }
            else -> false
        }
    }

    private fun showLogoutDialog() {
        CustomAlertDialog(this, "Do you want to logout?")
        CustomAlertDialog.binding.btnYes.setOnClickListener {
            SuccessfulToast(this, "Logout successfully!").showToast()
            CustomAlertDialog.alertDialog.dismiss()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }
        CustomAlertDialog.binding.btnNo.setOnClickListener {
            CustomAlertDialog.alertDialog.dismiss()
        }
        CustomAlertDialog.showAlertDialog()
    }
}
