package com.example.foodapp.Interface

import com.example.foodapp.model.Address


interface IAddressAdapterListener {
    fun onCheckedChanged(selectedAddress: Address)
    fun onDeleteAddress()
}


