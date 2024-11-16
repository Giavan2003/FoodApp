package com.example.foodapp.model

import java.io.Serializable


class User : Serializable {
    var userId: String? = null
    var fullName: String? = null
    var email: String? = null
    var password:String? = null
    var avatarURL: String? = null
    var userName: String? = null
    var phoneNumber: String? = null
    var admin = false
    var isActive = true

    constructor()
    constructor(
        userId: String?,
        fullName: String? = null,
        email: String?,
        userName: String?,
        password:String?,
        phoneNumber: String?,
    ) {
        this.userId = userId
        this.fullName = fullName
        this.email = email
        this.userName = userName
        this.password = password
        this.phoneNumber = phoneNumber
    }
}