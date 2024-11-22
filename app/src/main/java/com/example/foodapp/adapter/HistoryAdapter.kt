package com.example.foodapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.databinding.ItemHistoryProductBinding
import com.example.foodapp.model.HistoryProduct

class HistoryAdapter(
    private val context: Context,
    private var listHistoryProducts: List<HistoryProduct>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyProduct = listHistoryProducts[position]

        with(holder.binding) {
            imgHistoryProduct.setImageResource(historyProduct.resourceId)
            txtNameHistoryProduct.text = historyProduct.nameHistoryProduct
            txtStateHistory.text = historyProduct.state
            txtPriceHistoryProduct.text = historyProduct.priceHistoryProduct
        }
    }

    override fun getItemCount(): Int {
        return listHistoryProducts.size
    }

    class HistoryViewHolder(val binding: ItemHistoryProductBinding) : RecyclerView.ViewHolder(binding.root)
}
