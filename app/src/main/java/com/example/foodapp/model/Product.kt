package com.example.foodapp.model


import java.io.Serializable


class Product : Serializable {
    var productId: String? = null
    var productName: String? = null
    var productImage1: String? = null
    var productImage2: String? = null
    var productImage3: String? = null
    var productImage4: String? = null
    var productPrice = 0
    var productType: String? = null
    var remainAmount = 0
    var sold = 0
    var description: String? = null
    var ratingStar: Double? = null
    var ratingAmount = 0
    var publisherId: String? = null
    var state: String? = null
    var isChecked = false

    constructor()
    constructor(
        productName: String?,
        productImage1: String?,
        productImage2: String?,
        productImage3: String?,
        productImage4: String?,
        productPrice: Int,
        productType: String?,
        remainAmount: Int,
        sold: Int,
        description: String?,
        ratingStar: Double?,
        ratingAmount: Int,
        publisherId: String?
    ) {
        this.productName = productName
        this.productImage1 = productImage1
        this.productImage2 = productImage2
        this.productImage3 = productImage3
        this.productImage4 = productImage4
        this.productPrice = productPrice
        this.productType = productType
        this.remainAmount = remainAmount
        this.sold = sold
        this.description = description
        this.ratingStar = ratingStar
        this.ratingAmount = ratingAmount
        this.publisherId = publisherId
    }

    constructor(
        productId: String?,
        productName: String?,
        productImage1: String?,
        productImage2: String?,
        productImage3: String?,
        productImage4: String?,
        productPrice: Int,
        productType: String?,
        remainAmount: Int,
        sold: Int,
        description: String?,
        ratingStar: Double?,
        ratingAmount: Int,
        publisherId: String?,
        state: String?,
        checked: Boolean
    ) {
        this.productId = productId
        this.productName = productName
        this.productImage1 = productImage1
        this.productImage2 = productImage2
        this.productImage3 = productImage3
        this.productImage4 = productImage4
        this.productPrice = productPrice
        this.productType = productType
        this.remainAmount = remainAmount
        this.sold = sold
        this.description = description
        this.ratingStar = ratingStar
        this.ratingAmount = ratingAmount
        this.publisherId = publisherId
        this.state = state
        isChecked = checked
    }

    constructor(
        productId: String?,
        productName: String?,
        productImage1: String?,
        productImage2: String?,
        productImage3: String?,
        productImage4: String?,
        productPrice: Int,
        productType: String?,
        remainAmount: Int,
        sold: Int,
        description: String?,
        ratingStar: Double?,
        ratingAmount: Int,
        publisherId: String?,
        state: String?
    ) {
        this.productId = productId
        this.productName = productName
        this.productImage1 = productImage1
        this.productImage2 = productImage2
        this.productImage3 = productImage3
        this.productImage4 = productImage4
        this.productPrice = productPrice
        this.productType = productType
        this.remainAmount = remainAmount
        this.sold = sold
        this.description = description
        this.ratingStar = ratingStar
        this.ratingAmount = ratingAmount
        this.publisherId = publisherId
        this.state = state
    }

}



