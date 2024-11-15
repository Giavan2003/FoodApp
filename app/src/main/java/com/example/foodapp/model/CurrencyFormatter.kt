package com.example.foodapp.model

import java.text.NumberFormat
import java.util.Locale


class CurrencyFormatter private constructor() {
    private val VNFormatCurrency: NumberFormat

    init {
        VNFormatCurrency = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    }

    fun format(plainText: Double?): String {
        return VNFormatCurrency.format(plainText)
    }

    companion object {
        // Đảm bảo rằng chỉ có một instance của CurrencyFormatter
        private var formatter: CurrencyFormatter? = null

        fun getInstance(): CurrencyFormatter {
            if (formatter == null) {
                formatter = CurrencyFormatter()
            }
            return formatter!!
        }
    }
}


