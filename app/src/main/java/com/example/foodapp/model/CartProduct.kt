package com.example.foodapp.model

import java.io.Serializable


class CartProduct : Serializable {
    // Getters and setters
    var productId: String? = null
    var productName: String? = null
    var productImage1: String? = null
    var productPrice: Long = 0
    var remainAmount = 0
}

