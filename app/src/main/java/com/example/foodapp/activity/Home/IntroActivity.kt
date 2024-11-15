package com.example.foodapp.activity.Home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.foodapp.R
import com.example.foodapp.adapter.Home.IntroAdapter
import com.example.foodapp.databinding.ActivityIntroBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth


class IntroActivity : ComponentActivity() {
    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kiểm tra nếu Intro đã được hiển thị
        val preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val isIntroShown = preferences.getBoolean("isIntroShown", false)
        if (isIntroShown) {
            // Nếu đã hiển thị intro, chuyển sang màn hình đăng nhập
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Thiết lập binding
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dữ liệu hình ảnh cho màn hình Intro
        val images = arrayListOf(
            R.drawable.choice,  // Ảnh 1
            R.drawable.delivery,  // Ảnh 2
            R.drawable.tracking   // Ảnh 3
        )

        // Thiết lập Adapter cho ViewPager
        val introAdapter = IntroAdapter(images, this)
        binding.viewpaper.adapter = introAdapter

        // Cài đặt TabLayout với ViewPager2
        TabLayoutMediator(binding.tablayout, binding.viewpaper) { _, _ -> }.attach()

        // Hiển thị nút "Next" khi đến trang cuối cùng
        binding.viewpaper.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.btnNext.visibility = if (position == 2) View.VISIBLE else View.INVISIBLE
            }
        })

        // Cài đặt sự kiện cho nút Next
        binding.btnNext.setOnClickListener {
            // Chuyển sang màn hình LoginActivity khi nhấn "Next"
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Đăng xuất Firebase (nếu có người dùng đã đăng nhập)
        FirebaseAuth.getInstance().signOut()

        // Đánh dấu rằng Intro đã được hiển thị
        preferences.edit().putBoolean("isIntroShown", true).apply()
    }
}

