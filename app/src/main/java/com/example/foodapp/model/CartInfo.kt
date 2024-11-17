package com.example.foodapp.model

import java.io.Serializable

data class CartInfo(
    var amount: Int = 0,
    var cartInfoId: String? = null,
    var productId: String? = null
) : Serializable
