package com.example.foodapp.model


class Address {
    var addressId: String? = null
    var detailAddress: String? = null
    var state: String? = null
    var receiverName: String? = null
    var receiverPhoneNumber: String? = null

    constructor()
    constructor(
        addressId: String?,
        detailAddress: String?,
        state: String?,
        receiverName: String?,
        receiverPhoneNumber: String?
    ) {
        this.addressId = addressId
        this.detailAddress = detailAddress
        this.state = state
        this.receiverName = receiverName
        this.receiverPhoneNumber = receiverPhoneNumber
    }
}

