package com.example.foodapp.model
class UserDTO {
    var userId: String? = null
    var username: String? = null
    var email: String? = null
    var password: String? = null

    constructor()
    constructor(userId: String?, username: String?, email: String?, password: String?) {
        this.userId = userId
        this.username = username
        this.email = email
        this.password = password
    }
}