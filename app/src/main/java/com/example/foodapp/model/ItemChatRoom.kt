package com.example.foodapp.model


class ItemChatRoom {
    var receiver: User
    var lastMessage: Message? = null

    constructor(receiver: User) {
        this.receiver = receiver
    }

    constructor(receiver: User, lastMessage: Message?) {
        this.receiver = receiver
        this.lastMessage = lastMessage
    }

    fun getReceiver(): User {
        return receiver
    }

    fun setReceiver(receiver: User?) {
        var receiver = receiver
        receiver = receiver
    }
}