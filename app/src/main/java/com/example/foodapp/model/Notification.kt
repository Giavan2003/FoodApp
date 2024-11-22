package com.example.foodapp.model

import java.io.Serializable



data class Notification(
    var notificationId: String? = null,
    var title: String? = null,
    var content: String? = null,
    var imageURL: String? = null,
    var time: String? = null,
    var isRead: Boolean = false,
    var isNotified: Boolean = false,
    var productId: String? = null,
    var billId: String? = null,
    var confirmId: String? = null,
    var publisher: User? = null
) : Serializable {
    // Constructor mặc định
    constructor() : this(
        notificationId = null,
        title = null,
        content = null,
        imageURL = null,
        time = null,
        isRead = false,
        isNotified = false,
        productId = null,
        billId = null,
        confirmId = null,
        publisher = null
    )
}
