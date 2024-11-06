package com.example.foodapp.model

import java.io.Serializable


class Notification : Serializable {
    var notificationId: String? = null
    var title: String? = null
    var content: String? = null
    var imageURL: String? = null
    var time: String? = null
    var isRead = false
    var isNotified = false
    var productId: String? = null
    var billId: String? = null
    var confirmId: String? = null
    private var publisher: User? = null

    constructor(
        notificationId: String?,
        title: String?,
        content: String?,
        imageURL: String?,
        time: String?,
        read: Boolean,
        notified: Boolean,
        productId: String?,
        billId: String?,
        confirmId: String?,
        publisher: User?
    ) {
        this.notificationId = notificationId
        this.title = title
        this.content = content
        this.imageURL = imageURL
        this.time = time
        isRead = read
        isNotified = notified
        this.productId = productId
        this.billId = billId
        this.confirmId = confirmId
        this.publisher = publisher
    }

    constructor()

    fun getPublisher(): User? {
        return publisher
    }

    fun setPublisher(publisher: User?) {
        this.publisher = publisher
    }
}