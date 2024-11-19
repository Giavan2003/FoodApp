package com.example.foodapp.model




class Message {
    var idMessage: String? = null
    var content: String? = null
    var senderId: String? = null
    var timeStamp: Long = 0
    var isSeen = false

    constructor()
    constructor(content: String?, senderId: String?, timeStamp: Long, isSeen: Boolean) {
        this.content = content
        this.senderId = senderId
        this.timeStamp = timeStamp
        this.isSeen = isSeen
    }
}