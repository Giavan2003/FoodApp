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
    var admin = false
    var isActive = true

    constructor()
    constructor(
        userId: String?,
        email: String?,
        avatarURL: String?,
        userName: String?,
        birthDate: String?,
        phoneNumber: String?,
        isActive: Boolean
    ) {
        this.userId = userId
        this.email = email
        this.avatarURL = avatarURL
        this.userName = userName
        this.birthDate = birthDate
        this.phoneNumber = phoneNumber
        this.isActive = isActive
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
        phone: String?
    ) {
        this.userId = userId
        this.fullName = fullName
        this.email = email
        this.avatarURL = avatarURL
        userName = username
        phoneNumber = phone
    }
}