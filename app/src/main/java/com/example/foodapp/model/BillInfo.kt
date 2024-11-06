package com.example.foodapp.model

import java.io.Serializable


class BillInfo : Serializable {
    var amount = 0
    var billInfoId: String? = null
    var productId: String? = null
    var isCheck = false

    constructor()
    constructor(amount: Int, billInfoId: String?, productId: String?, check: Boolean) {
        this.amount = amount
        this.billInfoId = billInfoId
        this.productId = productId
        isCheck = check
    }
}