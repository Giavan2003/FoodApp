package com.example.foodapp.activity.order

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.adapter.orderAdapter.OrderViewPaperAdapter
import com.example.foodapp.databinding.ActivityOrderBinding
import com.example.foodapp.dialog.LoadingDialog
import com.example.foodapp.model.Bill
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections


class OrderActivity : AppCompatActivity() {
    private var userId: String? = null
    private lateinit var binding: ActivityOrderBinding
    private val dsCurrentOrder: ArrayList<Bill> = ArrayList()
    private val dsHistoryOrder: ArrayList<Bill> = ArrayList()
    private lateinit var dialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")

        userId = intent.getStringExtra("userId")
        dialog = LoadingDialog(this)
        dialog.show()
        initData()

        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun initUI() {
        // Cập nhật adapter để đảm bảo đúng kiểu
        val viewPaperAdapter = OrderViewPaperAdapter(this, dsCurrentOrder, dsHistoryOrder, userId ?: "")
        binding.viewPaper2.adapter = viewPaperAdapter
        binding.viewPaper2.isUserInputEnabled = false
        TabLayoutMediator(binding.tabLayout, binding.viewPaper2) { tab, position ->
            when (position) {
                0 -> tab.text = "Current Order"
                1 -> tab.text = "History Order"
            }
        }.attach()
        dialog.dismiss()
    }

    private fun initData() {
        FirebaseDatabase.getInstance().getReference("Bills")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dsCurrentOrder.clear()
                    dsHistoryOrder.clear()
                    for (item: DataSnapshot in snapshot.children) {
                        val tmp: Bill? = item.getValue(Bill::class.java)
                        if (tmp != null && tmp.recipientId?.equals(userId, ignoreCase = true) == true) {
                            if (tmp.orderStatus?.equals("Completed", ignoreCase = true) == true) {
                                dsHistoryOrder.add(tmp)
                            } else {
                                dsCurrentOrder.add(tmp)
                            }
                        }
                    }

                    // Đảo ngược danh sách
                    dsCurrentOrder.reverse()
                    dsHistoryOrder.reverse()
                    initUI()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    companion object {
        const val CURRENT_ORDER = 10001
        const val HISTORY_ORDER = 10002
    }
}
