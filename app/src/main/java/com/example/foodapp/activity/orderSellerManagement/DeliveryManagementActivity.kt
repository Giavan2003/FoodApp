package com.example.foodapp.activity.orderSellerManagement


import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.example.foodapp.adapter.DeliveryManagement_Seller.StatusManagementPagerAdapter
import com.example.foodapp.adapter.DeliveryManagement_Seller.StatusOrderRecyclerViewAdapter
import com.example.foodapp.databinding.ActivityDeliveryManagementBinding
import com.example.foodapp.dialog.LoadingDialog
import com.example.foodapp.helper.FirebaseStatusOrderHelper
import com.example.foodapp.model.Bill
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeliveryManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeliveryManagementBinding
    private var userId: String? = null
    private val dsShipping: ArrayList<Bill> = ArrayList()
    private val dsComplete: ArrayList<Bill> = ArrayList()
    private val dsConfirm: ArrayList<Bill> = ArrayList()
    private lateinit var dialog: LoadingDialog
    private lateinit var statusPagerAdapter: StatusManagementPagerAdapter
    val currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")

        // Lấy userId từ Intent
        userId = intent.getStringExtra("userId")
//        dialog = LoadingDialog(this)
//        dialog.show()

        // Lấy dữ liệu từ Firebase
        initData()
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    // Hàm để khởi tạo dữ liệu
    private fun initData() {
        FirebaseDatabase.getInstance().getReference("Bills")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dsConfirm.clear()
                    dsShipping.clear()
                    dsComplete.clear()
                    for (item: DataSnapshot in snapshot.children) {
                        val tmp: Bill? = item.getValue(Bill::class.java)
                        if (tmp != null && tmp.senderId?.equals(
                                userId,
                                ignoreCase = true
                            ) == true
                        ) {
                            if (tmp.orderStatus?.equals("Confirm", ignoreCase = true) == true) {
                                dsConfirm.add(tmp)
                            } else if (tmp.orderStatus?.equals("Shipping", ignoreCase = true) == true) {
                                dsShipping.add(tmp)
                            } else {
                                dsComplete.add(tmp)
                            }
                        }
                    }
                    dsComplete.reverse()
                    dsShipping.reverse()
                    dsConfirm.reverse()
                    initUI()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun initUI() {
        val currentTab = binding.viewPagerStatus.currentItem
        statusPagerAdapter =
            StatusManagementPagerAdapter(this, dsShipping, dsComplete, dsConfirm, userId ?: "")
        binding.viewPagerStatus.adapter = statusPagerAdapter
        TabLayoutMediator(binding.tabLayoutDelivery, binding.viewPagerStatus) { tab, position ->
            when (position) {
                0 -> tab.text = "Confirm"
                1 -> tab.text = "Shipping"
                2 -> tab.text = "Completed"
            }
        }.attach()
        binding.viewPagerStatus.setCurrentItem(currentTab, false)
        // dialog.dismiss()
    }
}

