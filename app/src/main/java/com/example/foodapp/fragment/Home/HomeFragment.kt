package com.example.foodapp.fragment.Home

import com.example.foodapp.R
import com.example.foodapp.activity.Home.FindActivity
import com.example.foodapp.adapter.ImagesViewPageAdapter
import com.example.foodapp.adapter.ViewPager2Adapter
import com.example.foodapp.databinding.FragmentHomeBinding
import com.example.foodapp.model.DepthPageTransformer
import com.example.foodapp.model.Images

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout


class HomeFragment(private val userId: String) : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val handler = Handler()
    private lateinit var imagesList: List<Images>  // Biến này cần được khởi tạo
    private lateinit var viewPager2Adapter: ViewPager2Adapter

    private val runnable = object : Runnable {
        override fun run() {
            val currentItem = binding.viewPager2.currentItem
            binding.viewPager2.currentItem = if (currentItem == imagesList.size - 1) 0 else currentItem + 1
            handler.postDelayed(this, 3000) // Delay 3s cho việc chuyển slide
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        initUI()
        return binding.root
    }

    private fun initUI() {
        // Khởi tạo imagesList ở đây
        imagesList = getListImages()  // Khởi tạo imagesList

        binding.layoutSearchView.setOnClickListener {
            val intent = Intent(activity, FindActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        binding.tabHome.apply {
            addTab(newTab().setText("Food"))
            addTab(newTab().setText("Drink"))
        }

        val fragmentManager = activity?.supportFragmentManager ?: return
        viewPager2Adapter = ViewPager2Adapter(fragmentManager, lifecycle, userId)
        binding.viewpaperHome.adapter = viewPager2Adapter

        binding.tabHome.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewpaperHome.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.viewpaperHome.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabHome.selectTab(binding.tabHome.getTabAt(position))
            }
        })

        // Khởi tạo adapter với imagesList
        val adapter = ImagesViewPageAdapter(imagesList)
        binding.viewPager2.adapter = adapter

        binding.circleIndicator3.setViewPager(binding.viewPager2)

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 3000)
            }
        })

        binding.viewPager2.setPageTransformer(DepthPageTransformer())
    }

    private fun getListImages(): List<Images> {
        // Hàm trả về danh sách hình ảnh
        return listOf(
            Images(R.drawable.bg1),
            Images(R.drawable.bg2),
            Images(R.drawable.bg3),
            Images(R.drawable.bg4)
        )
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 3000)
    }
}

