package com.example.foodapp.model


class Cart {
    var cartId: String? = null
    var totalAmount = 0
    var totalPrice = 0
    var userId: String? = null

    constructor(cartId: String?, totalAmount: Int, totalPrice: Int, userId: String?) {
        this.cartId = cartId
        this.totalAmount = totalAmount
        this.totalPrice = totalPrice
        this.userId = userId
    }

    constructor()
}
