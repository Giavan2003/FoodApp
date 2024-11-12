package com.example.foodapp.activity.orderSellerManagement


import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.example.foodapp.adapter.DeliveryManagement_Seller.StatusManagementPagerAdapter
import com.example.foodapp.databinding.ActivityDeliveryManagementBinding

class DeliveryManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeliveryManagementBinding
    private var userId: String? = null
    private lateinit var statusPagerAdapter: StatusManagementPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")

        // Get input
        userId = intent.getStringExtra("userId")

        statusPagerAdapter = userId?.let { StatusManagementPagerAdapter(this, it) }!!
        binding.viewPagerStatus.adapter = statusPagerAdapter

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Connect TabLayout with ViewPager
        binding.tabLayoutDelivery.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    binding.viewPagerStatus.currentItem = it.position
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.viewPagerStatus.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabLayoutDelivery.getTabAt(position)?.select()
            }
        })
    }
}
