package com.example.foodapp.activity.feedback

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.R
import com.example.foodapp.adapter.FeedBackAdapter
import com.example.foodapp.databinding.ActivityFeedBackBinding
import com.example.foodapp.model.Bill
import com.example.foodapp.model.BillInfo

class FeedBackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedBackBinding
    private lateinit var dsBillInfo: ArrayList<BillInfo>
    private lateinit var currentBill: Bill
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        @Suppress("UNCHECKED_CAST")
        dsBillInfo = intent.getSerializableExtra("List of billInfo") as ArrayList<BillInfo>
        currentBill = intent.getSerializableExtra("Current Bill") as Bill
        userId = intent.getStringExtra("userId") ?: ""

        initUI()
    }

    private fun initUI() {
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")

        val adapter = FeedBackAdapter(this, dsBillInfo, currentBill, userId)
        binding.ryc.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.ryc.setHasFixedSize(true)
        binding.ryc.adapter = adapter

        // Set sự kiện cho nút back
        binding.imgBack.setOnClickListener { finish() }
    }
}
