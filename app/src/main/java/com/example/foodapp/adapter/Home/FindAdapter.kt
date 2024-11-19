package com.example.foodapp.adapter.Home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.activity.Home.ResultSearchActivity
import com.example.foodapp.databinding.ItemSearchBinding


class FindAdapter(
    private val ds: ArrayList<String>,
    private val userId: String,
    private val mContext: Context
) : RecyclerView.Adapter<FindAdapter.ViewHolder>() {

    private val sharedPreferences: SharedPreferences = mContext.getSharedPreferences("history_search", Context.MODE_PRIVATE)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = ds[position]
        item.let {
            holder.binding.txtSearched.text = it
            holder.binding.txtSearched.setOnClickListener {
                val intent = Intent(
                    mContext,
                    ResultSearchActivity::class.java
                )
                intent.putExtra("userId", userId)
                intent.putExtra("text", item)
                if (mContext is Activity) {
                    mContext.startActivityForResult(intent, 101)
                } else {
                    Log.e("FindAdapter", "Context is not an instance of Activity")
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return ds.size
    }

    class ViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root)
}
