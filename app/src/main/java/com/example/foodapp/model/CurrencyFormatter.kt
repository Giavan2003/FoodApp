package com.example.foodapp.model

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val vnFormatCurrency: NumberFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    fun format(amount: Double): String {
        return vnFormatCurrency.format(amount)
    }
}