package com.example.foodapp.model

import java.io.Serializable


class User : Serializable {
    var userId: String? = null
    var fullName: String? = null
    var email: String? = null
    var avatarURL: String? = null
    var userName: String? = null
    var birthDate: String? = null
    var phoneNumber: String? = null
    private var admin = false
    fun isAdmin(): Boolean {
        return admin
    }

    fun setAdmin(admin: Boolean) {
        var admin = admin
        admin = admin
    }

    constructor()
    constructor(
        userId: String?,
        email: String?,
        avatarURL: String?,
        userName: String?,
        birthDate: String?,
        phoneNumber: String?
    ) {
        this.userId = userId
        this.email = email
        this.avatarURL = avatarURL
        this.userName = userName
        this.birthDate = birthDate
        this.phoneNumber = phoneNumber
    }

    constructor(
        userId: String?,
        fullName: String?,
        email: String?,
        avatarURL: String?,
        username: String?,
        birthDate: String?,
        phone: String?
    ) {
        this.userId = userId
        this.fullName = fullName
        this.email = email
        this.avatarURL = avatarURL
        userName = username
        this.birthDate = birthDate
        phoneNumber = phone
    }

    constructor(
        userId: String?,
        fullName: String?,
        username: String?,
        email: String?,
        avatarURL: String?,
        phone: String?,
        isAdmin: Boolean
    ) {
        this.userId = userId
        this.fullName = fullName
        this.email = email
        this.avatarURL = avatarURL
        userName = username
        phoneNumber = phone
        admin = isAdmin
    }
}