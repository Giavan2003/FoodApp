package com.example.foodapp.activity.MyShop



import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.activity.orderSellerManagement.DeliveryManagementActivity
import com.example.foodapp.databinding.ActivityMyShopBinding

class MyShopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyShopBinding
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")

        userId = intent.getStringExtra("userId")

        binding.cardMyFood.setOnClickListener {
            val intent = Intent(this@MyShopActivity, MyFoodActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.cardDelivery.setOnClickListener {
            val intent = Intent(this@MyShopActivity, DeliveryManagementActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }
}
