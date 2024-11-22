package com.example.foodapp.model

import java.io.Serializable


class Bill : Serializable {
    var addressId: String? = null
    var billId: String? = null
    var orderDate: String? = null
    var orderStatus: String? = null
    var isCheckAllComment = false
    var recipientId: String? = null
    var senderId: String? = null
    var totalPrice: Long = 0
    var imageUrl: String? = null

    constructor(
        addressId: String?,
        billId: String?,
        orderDate: String?,
        orderStatus: String?,
        checkAllComment: Boolean,
        recipientId: String?,
        senderId: String?,
        totalPrice: Long,
        imageUrl: String?
    ) {
        this.addressId = addressId
        this.billId = billId
        this.orderDate = orderDate
        this.orderStatus = orderStatus
        isCheckAllComment = checkAllComment
        this.recipientId = recipientId
        this.senderId = senderId
        this.totalPrice = totalPrice
        this.imageUrl = imageUrl
    }
    constructor()
}