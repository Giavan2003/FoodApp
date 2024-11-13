package com.example.foodapp.fragment.Home

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
import com.example.foodapp.R
import com.example.foodapp.activity.Home.FindActivity
import com.example.foodapp.adapter.ImagesViewPageAdapter
import com.example.foodapp.adapter.ViewPager2Adapter
import com.example.foodapp.databinding.FragmentHomeBinding
import com.example.foodapp.model.DepthPageTransformer
import com.example.foodapp.model.Images
import com.google.android.material.tabs.TabLayout


class HomeFragment(private val userId: String) : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val handler = Handler()
    private var imagesList: List<Images>? = null
    private lateinit var viewPager2Adapter: ViewPager2Adapter

    private val runnable = Runnable {
        if (binding.viewPager2.currentItem == imagesList?.size?.minus(1)) {
            binding.viewPager2.setCurrentItem(0)
        } else {
            binding.viewPager2.currentItem = binding.viewPager2.currentItem + 1
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        initUI()
        return binding.root
    }

    private fun initUI() {
        binding.layoutSearchView.setOnClickListener {
            val intent = Intent(activity, FindActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        // Tạo Tab
        binding.tabHome.addTab(binding.tabHome.newTab().setText("Food"))
        binding.tabHome.addTab(binding.tabHome.newTab().setText("Drink"))

        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
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

        imagesList = getListImages()
        val adapter = ImagesViewPageAdapter(imagesList)
        binding.viewPager2.adapter = adapter

        // Liên kết ViewPager và indicator
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
        return listOf(
            Images(R.drawable.bg1),
            Images(R.drawable.bg2),
            Images(R.drawable.bg3),
            Images(R.drawable.bg4)
        )
    }
}
