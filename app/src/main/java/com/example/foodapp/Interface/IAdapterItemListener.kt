package com.example.foodapp.Interface

import com.example.foodapp.model.CartInfo

interface IAdapterItemListener {
    fun onCheckedItemCountChanged(count: Int, price: Long, selectedItems: ArrayList<CartInfo?>?)
    fun onAddClicked()
    fun onSubtractClicked()
    fun onDeleteProduct()
}