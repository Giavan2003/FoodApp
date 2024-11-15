package com.example.foodapp.model

import java.io.Serializable


class CartInfo : Serializable {
    var amount = 0
    var cartInfoId: String? = null
    var productId: String? = null

    constructor(amount: Int, cartInfoId: String?, productId: String?) {
        this.amount = amount
        this.cartInfoId = cartInfoId
        this.productId = productId
    }

    constructor()
}
