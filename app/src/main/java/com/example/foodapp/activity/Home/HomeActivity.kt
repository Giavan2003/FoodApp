package com.example.foodapp.activity.Home

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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.example.foodapp.R
import com.example.foodapp.activity.Cart_PlaceOrder.CartActivity
import com.example.foodapp.activity.Cart_PlaceOrder.EmptyCartActivity
import com.example.foodapp.activity.MyShop.MyShopActivity
import com.example.foodapp.activity.ProductInformation.ProductInfoActivity
import com.example.foodapp.activity.manager.ManagerProductActivity
import com.example.foodapp.activity.manager.ManagerUserActivity
import com.example.foodapp.activity.order.OrderActivity
import com.example.foodapp.activity.order.OrderDetailActivity
import com.example.foodapp.activity.orderSellerManagement.DeliveryManagementActivity
import com.example.foodapp.adapter.manager.ManagerUserAdapter
import com.example.foodapp.custom.CustomMessageBox.CustomAlertDialog
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ActivityHomeBinding
import com.example.foodapp.fragment.Home.FavoriteFragment
import com.example.foodapp.fragment.Home.HomeFragment
import com.example.foodapp.fragment.NotificationFragment
import com.example.foodapp.helper.FirebaseNotificationHelper
import com.example.foodapp.helper.FirebaseProductInfoHelper
import com.example.foodapp.helper.FirebaseUserInfoHelper
import com.example.foodapp.model.Bill
import com.example.foodapp.model.Cart
import com.example.foodapp.model.Notification
import com.example.foodapp.model.Product
import com.example.foodapp.model.User
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var userId: String
    private lateinit var binding: ActivityHomeBinding
    private lateinit var layoutMain: LinearLayout
    private var selectionFragment: Fragment? = null
    private lateinit var userReference: DatabaseReference
    private lateinit var userValueEventListener: ValueEventListener

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 10023
        private const val STORAGE_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        Log.d("key", userReference.key.toString())
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Chuyển đổi dữ liệu từ snapshot sang một class hoặc Map
                    val user = snapshot.getValue(User::class.java) // Dùng class User
                    if (user != null) {
                        if (user.admin == false) {
                            val navMenu = binding.navigationLeft.menu
                            val item_user = navMenu.findItem(R.id.manager_user)
                            val item_pro = navMenu.findItem(R.id.manager_product)
                            item_user.setVisible(false)
                            item_pro.setVisible(false)
                        }
                    }
                } else {
                    Log.e("Firebase", "User does not exist")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
        userValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isActive = snapshot.child("active").getValue(Boolean::class.java) ?: false
                if (!isActive) {
                    // Tài khoản bị khóa, thực hiện đăng xuất
                    FirebaseAuth.getInstance().signOut()
                    FailToast(this@HomeActivity, "Account blocked!").showToast()

                    startActivity(
                        Intent(this@HomeActivity, LoginActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Database error: ${error.message}")
            }
        }
        userReference.addValueEventListener(userValueEventListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission(Manifest.permission.POST_NOTIFICATIONS, 101)
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 102)
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 103)
        }

        initUI()
        loadInformationForNavigationBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        userReference.removeEventListener(userValueEventListener)
    }

    private fun initUI() {
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")
        binding.navigationLeft.bringToFront()
        createActionBar()

        layoutMain = binding.layoutMain
        val homeFragment = HomeFragment()
        val bundle = Bundle()
        bundle.putString("userId", userId) // Đặt userId vào bundle
        homeFragment.arguments = bundle // Truyền bundle vào fragment
        // Thay thế fragment trong container
        supportFragmentManager.beginTransaction()
            .replace(layoutMain.id, homeFragment) // Đảm bảo rằng fragment_container là ID của container chứa fragment
            .commit()

        setEventNavigationBottom()
        setCartNavigation()
        binding.navigationLeft.setNavigationItemSelectedListener(this)
    }

    private fun setCartNavigation() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.message_menu -> {
                    val intent = Intent(this@HomeActivity, ChatActivity::class.java).apply {
                        putExtra("userId", userId)
                    }
                    startActivity(intent)
                    true
                }

                R.id.cart_menu -> {
                    FirebaseDatabase.getInstance().reference.child("Carts")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var isCartFound = false
                                for (ds in snapshot.children) {
                                    val cart = ds.getValue(Cart::class.java)
                                    //Log.d("CartDebug", "cartId: ${cart?.cartId}")
                                    //Log.d("CartDebug", "userId: $userId")
                                    if (!cart?.cartId.isNullOrEmpty()) {
                                        FirebaseDatabase.getInstance().reference.child("CartInfo's")
                                            .child(cart?.cartId!!)
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    //Log.d("CartDebug", "so san pham: ${snapshot.childrenCount}")
                                                    if (snapshot.exists() && snapshot.childrenCount == 0L) {
                                                        startActivity(
                                                            Intent(
                                                                this@HomeActivity,
                                                                EmptyCartActivity::class.java
                                                            )
                                                        )
                                                    } else {
                                                        val intent = Intent(
                                                            this@HomeActivity,
                                                            CartActivity::class.java
                                                        ).apply {
                                                            putExtra("userId", userId)
                                                        }
                                                        startActivity(intent)
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.e(
                                                        "CartError",
                                                        "Lỗi khi tải CartInfos: ${error.message}"
                                                    )
                                                }
                                            })
                                        isCartFound = true
                                        break
                                    }
                                }
                                if (!isCartFound) {
                                    startActivity(
                                        Intent(
                                            this@HomeActivity,
                                            EmptyCartActivity::class.java
                                        )
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("CartError", "Lỗi khi tải Carts: ${error.message}")
                            }
                        })
                    true
                }

                else -> true
            }
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
                2 -> HomeFragment()
                3 -> NotificationFragment(userId)
                else -> null
            }
            selectionFragment?.let {
                val bundle = Bundle()
                bundle.putString("userId", userId)
                it.arguments = bundle
                supportFragmentManager.beginTransaction().replace(layoutMain.id, it).commit()
            }
        }

        binding.bottomNavigation.setOnShowListener { model ->
            selectionFragment = when (model.id) {
                1 -> FavoriteFragment(userId)
                2 -> HomeFragment()
                3 -> NotificationFragment(userId)
                else -> null
            }
            selectionFragment?.let {
                val bundle = Bundle()
                bundle.putString("userId", userId)
                it.arguments = bundle
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
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
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

            R.id.manager_user -> {
                val intent = Intent(this, ManagerUserActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }

            R.id.manager_product -> {
                val intent = Intent(this, ManagerProductActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }

            R.id.logoutMenu -> {
                val customAlertDialog = CustomAlertDialog(this, "Do you want to logout?").apply {
                    binding.btnYes.setOnClickListener {
                        SuccessfulToast(this@HomeActivity, "Logout successfully!").showToast()
                        alertDialog.dismiss()
                        FirebaseAuth.getInstance().signOut()
                        startActivity(
                            Intent(this@HomeActivity, LoginActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        finish()
                    }
                    binding.btnNo.setOnClickListener {
                        alertDialog.dismiss()
                    }
                }

                customAlertDialog.showAlertDialog()
            }

            else -> {

            }
        }
        binding.drawLayoutHome.close()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }


    fun loadInformationForNavigationBar() {

        FirebaseNotificationHelper(this).readNotification(
            userId,
            object : FirebaseNotificationHelper.DataStatus {
                override fun DataIsLoaded(
                    notificationList: List<Notification>,
                    notificationListToNotify: List<Notification>
                ) {
                    var count = 0
                    for (i in notificationList.indices) {
                        if (!notificationList[i].isRead) {
                            count++
                        }
                    }
                    if (count > 0) {
                        binding.bottomNavigation.setCount(3, count.toString())
                    } else if (count == 0) {
                        binding.bottomNavigation.clearCount(3)
                    }

                    for (notification in notificationListToNotify) {
                        makeNotification(notification)
                    }
                }

                override fun DataIsInserted() {}

                override fun DataIsUpdated() {}

                override fun DataIsDeleted() {}
            })

        FirebaseUserInfoHelper(this).readUserInfo(
            userId,
            object : FirebaseUserInfoHelper.DataStatus {
                override fun dataIsLoaded(user: User?) {
                    val headerView = binding.navigationLeft.getHeaderView(0)
                    val imgAvatarInNavigationBar: ShapeableImageView =
                        headerView.findViewById(R.id.imgAvatarInNavigationBar)
                    val txtNameInNavigationBar: TextView =
                        headerView.findViewById(R.id.txtNameInNavigationBar)
                    txtNameInNavigationBar.text = "Hi, ${getLastName(user?.userName ?: "")}"
                    if (!this@HomeActivity.isDestroyed && !this@HomeActivity.isFinishing) {
                        Glide.with(this@HomeActivity)
                            .load(user?.avatarURL)
                            .placeholder(R.drawable.default_avatar)
                            .into(imgAvatarInNavigationBar)
                    }

                }

                override fun dataIsInserted() {
                }

                override fun dataIsUpdated() {
                }

                override fun dataIsDeleted() {
                }
            })
    }

    private fun getLastName(userName: String): String {
        val trimmedName = userName.trim()
        val output = trimmedName.split(" ")
        return output.last()
    }

    private fun makeNotification(notification: Notification) {
        val channelId = "CHANNEL_ID_NOTIFICATION"
        val builder = NotificationCompat.Builder(applicationContext, channelId)
        val largeIcon = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.bkg)
        builder.setSmallIcon(R.drawable.bkg)
            .setContentTitle("Food services")
            .setContentText(notification.title)
            .setStyle(
                NotificationCompat.BigTextStyle().setBigContentTitle(notification.title)
                    .bigText(notification.content)
            )
            .setLargeIcon(largeIcon)
            .setColor(Color.RED)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (notification.billId != "None") {
            val bill = Bill().apply { billId = notification.billId }
            val intent = Intent(applicationContext, OrderDetailActivity::class.java).apply {
                putExtra("Bill", bill)
                putExtra("userId", userId)
                putExtra("notification", notification)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            val pendingIntent =
                PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_MUTABLE)
            builder.setContentIntent(pendingIntent)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var notificationChannel = notificationManager.getNotificationChannel(channelId)
                if (notificationChannel == null) {
                    notificationChannel = NotificationChannel(
                        channelId,
                        "Some description",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        lightColor = Color.GREEN
                        enableVibration(true)
                    }
                    notificationManager.createNotificationChannel(notificationChannel)
                }
            }

            notificationManager.notify(0, builder.build())
        } else if (notification.productId != "None") {
            val userName = arrayOfNulls<String>(1)
            FirebaseDatabase.getInstance().getReference().child("Users").child(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userName[0] = snapshot.child("userName").getValue(String::class.java)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            FirebaseProductInfoHelper(notification.productId!!).readInformationById(object :
                FirebaseProductInfoHelper.DataStatusInformationOfProduct {
                override fun DataIsLoaded(product: Product?) {
                    val intent = Intent(applicationContext, ProductInfoActivity::class.java).apply {
                        putExtra("productId", product?.productId)
                        putExtra("productName", product?.productName)
                        putExtra("productPrice", product?.productPrice)
                        putExtra("productImage1", product?.productImage1)
                        putExtra("productImage2", product?.productImage2)
                        putExtra("productImage3", product?.productImage3)
                        putExtra("productImage4", product?.productImage4)
                        putExtra("ratingStar", product?.ratingStar)
                        putExtra("productDescription", product?.description)
                        putExtra("publisherId", product?.publisherId)
                        putExtra("sold", product?.sold)
                        putExtra("productType", product?.productType)
                        putExtra("remainAmount", product?.remainAmount)
                        putExtra("ratingAmount", product?.ratingAmount)
                        putExtra("state", product?.state)
                        putExtra("userId", userId)
                        putExtra("userName", userName)
                        putExtra("notification", notification)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivity(intent)

                    val pendingIntent = PendingIntent.getActivity(
                        applicationContext,
                        0,
                        intent,
                        PendingIntent.FLAG_MUTABLE
                    )
                    builder.setContentIntent(pendingIntent)
                    val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        var notificationChannel =
                            notificationManager.getNotificationChannel(channelId)
                        if (notificationChannel == null) {
                            notificationChannel = NotificationChannel(
                                channelId,
                                "Some description",
                                NotificationManager.IMPORTANCE_HIGH
                            ).apply {
                                lightColor = Color.GREEN
                                enableVibration(true)
                            }
                            notificationManager.createNotificationChannel(notificationChannel)
                        }
                    }

                    notificationManager.notify(0, builder.build())
                }

                override fun DataIsInserted() {}

                override fun DataIsUpdated() {}

                override fun DataIsDeleted() {}
            })

        } else if (notification.confirmId != "None") {
            val intent = Intent(applicationContext, DeliveryManagementActivity::class.java).apply {
                putExtra("userId", userId)
                putExtra("notification", notification)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            val pendingIntent =
                PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_MUTABLE)
            builder.setContentIntent(pendingIntent)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var notificationChannel = notificationManager.getNotificationChannel(channelId)
                if (notificationChannel == null) {
                    notificationChannel = NotificationChannel(
                        channelId,
                        "Some description",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        lightColor = Color.GREEN
                        enableVibration(true)
                    }
                    notificationManager.createNotificationChannel(notificationChannel)
                }
            }

            notificationManager.notify(0, builder.build())
        } else if (notification.publisher != null) {
            val intent = Intent(applicationContext, ChatDetailActivity::class.java).apply {
                action = "homeActivity"
                putExtra("notification", notification)
                putExtra("publisher", notification.publisher)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var notificationChannel = notificationManager.getNotificationChannel(channelId)
                if (notificationChannel == null) {
                    notificationChannel = NotificationChannel(
                        channelId,
                        "Some description",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        lightColor = Color.GREEN
                        enableVibration(true)
                    }
                    notificationManager.createNotificationChannel(notificationChannel)
                }
            }

            notificationManager.notify(0, builder.build())
        }
    }

}
