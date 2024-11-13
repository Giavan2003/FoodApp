package com.example.foodapp.activity.Home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.foodapp.adapter.Home.LoginSignUpAdapter
import com.example.foodapp.databinding.ActivityLoginBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {
    private var binding: ActivityLoginBinding? = null
    private var myFragmentAdapter: LoginSignUpAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")
        myFragmentAdapter = LoginSignUpAdapter(this@LoginActivity)
        binding!!.viewpaper2.adapter = myFragmentAdapter
        val tabLogin = binding!!.tablayoutHome.newTab()
        tabLogin.setText("Login")
        binding!!.tablayoutHome.addTab(binding!!.tablayoutHome.newTab().setText("Login"))
        binding!!.tablayoutHome.addTab(binding!!.tablayoutHome.newTab().setText("Sign Up"))
        binding!!.tablayoutHome.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding!!.viewpaper2.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        binding!!.viewpaper2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding!!.tablayoutHome.selectTab(binding!!.tablayoutHome.getTabAt(position))
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
        }
    }
}