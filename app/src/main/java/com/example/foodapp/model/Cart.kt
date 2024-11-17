package com.example.foodapp.model

data class Cart(
    var cartId: String? = null,
    var totalAmount: Int = 0,
    var totalPrice: Int = 0,
    var userId: String? = null
)
