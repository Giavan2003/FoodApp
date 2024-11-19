package com.example.foodapp.activity.Home

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.foodapp.R
import com.example.foodapp.activity.MyShop.MyShopActivity
import com.example.foodapp.activity.order.OrderActivity
import com.example.foodapp.databinding.ActivityProfileBinding
import com.example.foodapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("userId") ?: ""

        initToolbar()

        getUserInfo()

        binding.cardViewOrders.setOnClickListener {
            val intent1 = Intent(this, OrderActivity::class.java)
            intent1.putExtra("userId", userId)
            startActivity(intent1)
        }

        binding.cardViewMyShop.setOnClickListener {
            val intent2 = Intent(this, MyShopActivity::class.java)
            intent2.putExtra("userId", userId)
            startActivity(intent2)
        }

        binding.change.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }

    private fun initToolbar() {
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun getUserInfo() {
        FirebaseDatabase.getInstance().getReference("Users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        binding.userName.text = it.userName
                        binding.userEmail.text = it.email
                        binding.userPhoneNumber.text = it.phoneNumber
                        Glide.with(applicationContext)
                            .load(it.avatarURL)
                            .placeholder(R.drawable.default_avatar)
                            .into(binding.userAvatar)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
    }
}
