package com.example.foodapp.activity.Home

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.SearchView

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.R
import com.example.foodapp.adapter.Home.FindAdapter
import com.example.foodapp.databinding.ActivityFindBinding
import java.io.File

class FindActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFindBinding
    private lateinit var historySearch: ArrayList<String>
    private lateinit var adapter: FindAdapter
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initUI()
    }

    private fun initData() {
        val intent = intent
        userId = intent.getStringExtra("userId")
        historySearch = ArrayList()
    }

    private fun initUI() {
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")
        binding.searhView.setIconifiedByDefault(false)

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcHistorySearch.layoutManager = linearLayoutManager
        adapter = userId?.let { FindAdapter(historySearch, it, this) }!!
        binding.rcHistorySearch.adapter = adapter
        binding.rcHistorySearch.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.searhView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val sharedPreferences = getSharedPreferences("history_search", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                if (historySearch.isEmpty() || query != historySearch[0]) {
                    historySearch.add(0, query) // Add to the start
                    if (historySearch.size > 3) {
                        historySearch.removeAt(3) // Remove the oldest item if the list exceeds 3
                    }
                }
                editor.clear()
                if (historySearch.size > 2) {
                    editor.putString("3rd", historySearch[2])
                }
                if (historySearch.size > 1) {
                    editor.putString("2nd", historySearch[1])
                }
                if (historySearch.isNotEmpty()) {
                    editor.putString("1st", historySearch[0])
                }
                editor.apply()

                val intent = Intent(this@FindActivity, ResultSearchActivity::class.java)
                intent.putExtra("userId", userId)
                intent.putExtra("text", query)
                Log.d("text size", historySearch.size.toString())
                startActivityForResult(intent, 101)
                adapter.notifyDataSetChanged()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        fetchData()
    }

    private fun fetchData() {
        val sharedPrefsFile = File("${this.filesDir.parent}/shared_prefs/history_search.xml")
        val sharedPreferences = getSharedPreferences("history_search", MODE_PRIVATE)

        if (!sharedPrefsFile.exists()) {
            val editor = sharedPreferences.edit()
            editor.putString("1st", "Trà sữa")
            editor.putString("2nd", "Cơm")
            editor.putString("3rd", "Bún")
            editor.apply()
        }
        sharedPreferences.getString("1st", "")?.let { historySearch.add(it) }
        sharedPreferences.getString("2nd", "")?.let { historySearch.add(it) }
        sharedPreferences.getString("3rd", "")?.let { historySearch.add(it) }
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("requestCode", requestCode.toString())
        Log.d("resultCode", resultCode.toString())
        if (requestCode == 101 && resultCode == 101) {
            data?.getStringArrayListExtra("history_search")?.let { arr ->
                historySearch.clear()
                historySearch.addAll(arr)
                Log.d("Size return", historySearch.size.toString())
                historySearch.forEach { Log.d("value", it) }
                adapter.notifyDataSetChanged()
            }
        }
    }
}
