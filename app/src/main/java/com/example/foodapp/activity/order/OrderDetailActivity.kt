package com.example.foodapp.activity.order

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.R
import com.example.foodapp.activity.feedback.FeedBackActivity
import com.example.foodapp.adapter.orderAdapter.OrderDetailAdapter
import com.example.foodapp.databinding.ActivityOrderDetailBinding
import com.example.foodapp.dialog.LoadingDialog
import com.example.foodapp.model.Bill
import com.example.foodapp.model.BillInfo
import com.example.foodapp.model.Notification
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrderDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailBinding
    private val dsBillInfo = ArrayList<BillInfo>()
    private lateinit var currentBill: Bill
    private lateinit var loadingDialog: LoadingDialog
    private var userId: String? = null
    private lateinit var notification: Notification

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")

        // Lấy Intent
        val intent = intent
        // Khởi tạo dữ liệu
        currentBill = intent.getSerializableExtra("Bill") as Bill
        userId = intent.getStringExtra("userId")
        loadingDialog = LoadingDialog(this)
        loadingDialog.show()
    }

    override fun onStart() {
        super.onStart()
        dsBillInfo.clear()
        FirebaseDatabase.getInstance().reference.child("Bills").child(currentBill.billId!!).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentBill = snapshot.getValue(Bill::class.java)!!
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        initData()
    }

    private fun initData() {
        FirebaseDatabase.getInstance().reference.child("BillInfos").child(currentBill.billId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    val tmp = item.getValue(BillInfo::class.java)
                    dsBillInfo.add(tmp!!)
                }
                // Cập nhật giao diện sau khi đã có dữ liệu
                initUI()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun initUI() {
        val status = currentBill.orderStatus
        when (status?.toLowerCase()) {
            "completed" -> {
                binding.lnOderDetail.btn.visibility = View.VISIBLE
                binding.imgStatus.setImageResource(R.drawable.line_status_completed)
            }
            "shipping" -> binding.imgStatus.setImageResource(R.drawable.line_status_shipping)
            else -> binding.imgStatus.setImageResource(R.drawable.line_status_confirmed)
        }
        val adapter = OrderDetailAdapter(this, dsBillInfo)
        binding.lnOderDetail.ryc.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.lnOderDetail.ryc.adapter = adapter
        binding.lnOderDetail.ryc.setHasFixedSize(true)
        binding.lnOderDetail.txtTotalPrice.text = convertToMoney(currentBill.totalPrice) + "đ"
        binding.txtId.text = currentBill.billId
        binding.imgBack.setOnClickListener { finish() }
        loadingDialog.dismiss()

        // Nếu tất cả billInfo đã được feedback thì sẽ không cho người dùng feedback nữa
        if (currentBill.isCheckAllComment) {
            binding.lnOderDetail.btn.isEnabled = false
            binding.lnOderDetail.btn.setBackgroundResource(R.drawable.background_feedback_disabled_button)
        } else {
            binding.lnOderDetail.btn.isEnabled = true
            binding.lnOderDetail.btn.setBackgroundResource(R.drawable.background_feedback_enable_button)
        }

        // Set sự kiện nút chuyển qua feedback cho product
        binding.lnOderDetail.btn.setOnClickListener {
            filterItemChecked()
            val intent = Intent(this@OrderDetailActivity, FeedBackActivity::class.java)
            intent.putExtra("Current Bill", currentBill)
            intent.putExtra("List of billInfo", dsBillInfo)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }

    private fun filterItemChecked() {
        val iterator = dsBillInfo.iterator()
        while (iterator.hasNext()) {
            val billInfo = iterator.next()
            if (billInfo.isCheck) iterator.remove()
        }
    }

    private fun convertToMoney(price: Long): String {
        val temp = price.toString()
        var output = ""
        var count = 3
        for (i in temp.length - 1 downTo 0) {
            count--
            output = if (count == 0) {
                count = 3
                ",${temp[i]}$output"
            } else {
                "${temp[i]}$output"
            }
        }
        return if (output.startsWith(",")) output.substring(1) else output
    }
}
