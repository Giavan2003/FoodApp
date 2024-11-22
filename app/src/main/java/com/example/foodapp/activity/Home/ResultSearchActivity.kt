package com.example.foodapp.activity.Home

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.widget.SearchView

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.R
import com.example.foodapp.adapter.Home.ResultSearchAdapter
import com.example.foodapp.databinding.ActivityResultSearchBinding
import com.example.foodapp.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResultSearchActivity : AppCompatActivity() {
    private var isScrolling = true
    private var isLoading = false
    private lateinit var binding: ActivityResultSearchBinding
    private val productsReference = FirebaseDatabase.getInstance().getReference("Products")
    private val dsAll = mutableListOf<Product>()
    private var dsCurrent: MutableList<Product?> = mutableListOf()
    private val historySearch = mutableListOf<String>()
    private lateinit var adapter: ResultSearchAdapter
    private var userId: String? = null
    private var text: String? = null

    private var position = 0
    private val itemCount = 5
    private lateinit var adapterItems: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initUI()
    }

    private fun initUI() {
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")

        val items = arrayOf("Giá giảm dần", "Giá tăng dần", "Đánh giá giảm dần", "Đánh giá tăng dần")
        adapterItems = ArrayAdapter(this, R.layout.item_sort, items)
        binding.autoCompleteTxt.setAdapter(adapterItems)

        binding.autoCompleteTxt.setOnItemClickListener { _, _, position, _ ->
            val sortedList = when (position) {
                0 -> dsAll.sortedByDescending { it.productPrice }
                1 -> dsAll.sortedBy { it.productPrice }
                2 -> dsAll.sortedByDescending { it.ratingStar }
                3 -> dsAll.sortedBy { it.ratingStar }
                else -> dsAll
            }
            dsCurrent.clear()
            dsAll.clear()
            dsAll.addAll(sortedList)
            this.position = 0 // Đặt lại vị trí bắt đầu
            repeat(itemCount.coerceAtMost(dsAll.size - this.position)) {
                dsCurrent.add(dsAll[this.position++])
            }
            adapter.notifyDataSetChanged()
        }

        adapter = userId?.let { ResultSearchAdapter(dsCurrent, it, this) }!!
        binding.recycleFoodFinded.adapter = adapter
        binding.recycleFoodFinded.layoutManager = LinearLayoutManager(this)
        binding.recycleFoodFinded.setHasFixedSize(true)

        binding.recycleFoodFinded.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                if (!isLoading && isScrolling && layoutManager != null &&
                    layoutManager.findLastCompletelyVisibleItemPosition() == dsCurrent.size - 1
                ) {
                    loadMore()
                    isLoading = true
                }
            }
        })

        val closeButton = binding.searhView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setOnClickListener { finish() }

        binding.btnBack.setOnClickListener {
            val backIntent = Intent().apply {
                putStringArrayListExtra("history_search", ArrayList(historySearch))
            }
            setResult(101, backIntent)
            finish()
        }

        binding.searhView.setIconifiedByDefault(false)
        binding.searhView.setQuery(text, false)
        binding.searhView.clearFocus()

        binding.searhView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun initData() {
        intent?.let {
            userId = it.getStringExtra("userId")
            text = it.getStringExtra("text")
        }

        val sharedPreferences = getSharedPreferences("history_search", MODE_PRIVATE)
        historySearch.add(sharedPreferences.getString("1st", "") ?: "")
        historySearch.add(sharedPreferences.getString("2nd", "") ?: "")
        historySearch.add(sharedPreferences.getString("3rd", "") ?: "")


        text?.let { performSearch(it) }
    }

    private fun performSearch(query: String) {
        val sharedPreferences = getSharedPreferences("history_search", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if (historySearch.isEmpty() || query != historySearch.first()) {
            historySearch.add(0, query)
            if (historySearch.size > 3) historySearch.removeAt(3)
        }

        editor.clear()
        historySearch.getOrNull(0)?.let { editor.putString("1st", it) }
        historySearch.getOrNull(1)?.let { editor.putString("2nd", it) }
        historySearch.getOrNull(2)?.let { editor.putString("3rd", it) }
        editor.apply()

        productsReference.orderByChild("productName").startAt(query).endAt(query + "\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dsAll.clear()
                    dsCurrent.clear()

                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.let { dsAll.add(it) }
                    }
                    Log.d("size",dsAll.size.toString())
                    position = 0
                    repeat(itemCount.coerceAtMost(dsAll.size - position)) {
                        dsCurrent.add(dsAll[position++])
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ResultSearchActivity", "Search failed: ${error.message}")
                }
            })

    }

    private fun loadMore() {
        if (position < dsAll.size) {
            dsCurrent.add(null)
            adapter.notifyItemInserted(dsCurrent.size - 1)
            Handler(Looper.getMainLooper()).postDelayed({
                dsCurrent.removeAt(dsCurrent.size - 1)
                repeat(itemCount.coerceAtMost(dsAll.size - position)) {
                    dsCurrent.add(dsAll[position++])
                }
                adapter.notifyDataSetChanged()
                isLoading = false
            }, 1000)
        } else {
            isScrolling = false
            adapter.notifyDataSetChanged()
        }
    }
}
