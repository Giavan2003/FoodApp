package com.example.foodapp.model

import java.io.Serializable

class User : Serializable {
    var userId: String? = null
    var fullName: String? = null
    var email: String? = null
    var password: String? = null
    var avatarURL: String? = null
    var userName: String? = null
    var phoneNumber: String? = null
    var admin = false
    var isActive = true

    constructor()


    constructor(
        userId: String?,
        fullName: String?,
        email: String?,
        password: String?,
        avatarURL: String?,
        userName: String?,
        phoneNumber: String?,
        isActive: Boolean
    ) {
        this.userId = userId
        this.fullName = fullName
        this.email = email
        this.password = password
        this.avatarURL = avatarURL
        this.userName = userName
        this.phoneNumber = phoneNumber
        this.isActive = isActive
    }
}
