package com.example.foodapp.activity.MyShop

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.adapter.MyShopAdapter.StatisticalAdapter

import com.example.foodapp.databinding.ActivityStatisticalBinding
import com.example.foodapp.model.Bill
import com.example.foodapp.model.Product
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

import com.github.mikephil.charting.formatter.ValueFormatter

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class StatisticalActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var binding: ActivityStatisticalBinding
    private var ds = mutableListOf<Product>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userId = intent.getStringExtra("userId") ?: ""
        getRevenueByMonth()
        getRevenueByProduct()
        getTableLayout()
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun getTableLayout() {

        val adapter = StatisticalAdapter(ds, this)
        // Thêm các hàng vào TableLayout
        for (i in 0 until adapter.count) {
            val productRow = adapter.getView(i, null, binding.tableLayout)
            binding.tableLayout.addView(productRow)
        }
    }

    private fun getRevenueByMonth() {
        // Lấy tất cả các hóa đơn từ "Bills"
        FirebaseDatabase.getInstance().getReference("Bills")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalRevenue = 0L
                    val monthRevenue = mutableMapOf<String, Long>(
                        "01" to 0L, "02" to 0L, "03" to 0L, "04" to 0L, "05" to 0L,
                        "06" to 0L, "07" to 0L, "08" to 0L, "09" to 0L, "10" to 0L,
                        "11" to 0L, "12" to 0L
                    )
                    // Lặp qua các hóa đơn
                    for (billSnapshot in snapshot.children) {
                        val bill = billSnapshot.getValue(Bill::class.java)
                        // Tính doanh thu tổng cộng
                        if (bill != null) {
                            if (bill.senderId == userId){
                                totalRevenue += bill.totalPrice ?: 0
                                // Tính doanh thu theo tháng
                                val month = bill.orderDate?.substring(3,5)
                                if (month != null) {
                                    monthRevenue[month] = (monthRevenue[month] ?: 0L) + (bill.totalPrice)
                                }
                            }
                        }
                    }
                    // Cập nhật tổng doanh thu
                    binding.totalRevenue.text = "Total Revenue: $totalRevenue"
                    // Cập nhật biểu đồ LineChart
                    updateLineChart(monthRevenue)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@StatisticalActivity,
                        "Error fetching data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getRevenueByProduct() {
        FirebaseDatabase.getInstance().getReference("Products")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalRevenue = 0L
                    val proRevenue = mutableMapOf<String, Long>()
                    for (proSnapshot in snapshot.children) {
                        val pro = proSnapshot.getValue(Product::class.java)
                        // Tính doanh thu tổng cộng
                        if (pro?.productId != null && pro.publisherId == userId && pro.sold > 0) {
                            // Tính doanh thu tổng cộng
                            val revenue = pro.sold * pro.productPrice
                            totalRevenue += revenue
                            proRevenue[pro.productName!!] = revenue.toLong()
                            ds.add(pro)
                        }

                    }
                    getTableLayout()
                    updatePieChart(proRevenue)
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@StatisticalActivity,
                        "Error fetching data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateLineChart(monthRevenue: Map<String, Long>) {
        val entries = mutableListOf<Entry>()
        var index = 0f

        // Chuyển đổi dữ liệu doanh thu theo tháng thành Entry cho LineChart
        for ((_, revenue) in monthRevenue) {
            entries.add(Entry(index, revenue.toFloat()))
            index++
        }

        val lineDataSet = LineDataSet(entries, "Doanh thu theo tháng")
        lineDataSet.color = Color.BLUE
        lineDataSet.valueTextColor = Color.BLACK
        lineDataSet.valueTextSize = 12f

        val lineData = LineData(lineDataSet)
        binding.lineChart.data = lineData

        // Tắt hiển thị trục bên phải
        val rightAxis = binding.lineChart.axisRight
        rightAxis.isEnabled = false // Ẩn trục bên phải

        // Hiển thị trục bên trái
        val leftAxis = binding.lineChart.axisLeft
        leftAxis.isEnabled = true  // Bật trục bên trái
        leftAxis.textColor = Color.BLACK // Đặt màu chữ cho trục bên trái

        // Tùy chỉnh trục X nếu cần
        val xAxis = binding.lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM // Đặt trục X ở dưới
        xAxis.textColor = Color.BLACK // Màu chữ của trục X

        binding.lineChart.description.isEnabled = false // Ẩn mô tả biểu đồ
        binding.lineChart.invalidate() // Làm mới biểu đồ
    }

    private fun updatePieChart(productRevenue: Map<String, Long>) {
        val entries = mutableListOf<PieEntry>()

        // Tính tổng doanh thu
        val totalRevenue = productRevenue.values.sum()

        // Chuyển đổi doanh thu sang phần trăm và tạo các PieEntry
        for ((productId, revenue) in productRevenue) {
            val percentage = (revenue.toFloat() / totalRevenue) * 100
            entries.add(PieEntry(percentage, productId)) // Hiển thị productId hoặc tên sản phẩm
        }
        val dataSet = PieDataSet(entries, "")
        dataSet.sliceSpace = 3f
        dataSet.valueTextSize = 12f
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${"%.1f".format(value)}%" // Làm tròn 1 chữ số sau dấu thập phân
            }
        }
        val colors = generateRandomColors(entries.size)
        dataSet.colors = colors
        val pieData = PieData(dataSet)
        pieData.setValueTextColor(Color.WHITE)
        val legend = binding.pieChart.legend
        legend.isWordWrapEnabled = true
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        binding.pieChart.data = pieData
        binding.pieChart.centerText = "Doanh thu theo sản phẩm (%)"
        binding.pieChart.setCenterTextSize(14f)
        binding.pieChart.setDrawEntryLabels(false)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.isRotationEnabled = false
        binding.pieChart.invalidate() // Làm mới biểu đồ
    }
    fun generateRandomColors(count: Int): List<Int> {
        val colors = mutableListOf<Int>()
        repeat(count) {
            colors.add(Color.rgb((50..255).random(), (50..255).random(), (50..255).random()))
        }
        return colors
    }


}