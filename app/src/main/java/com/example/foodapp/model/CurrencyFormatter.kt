import java.text.NumberFormat
import java.util.*

class CurrencyFormatter private constructor() {

    private val VNFormatCurrency: NumberFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    companion object {
        @Volatile
        private var formatter: CurrencyFormatter? = null

        fun getFormatter(): CurrencyFormatter {
            return formatter ?: synchronized(this) {
                formatter ?: CurrencyFormatter().also { formatter = it }
            }
        }
    }

    fun format(plainText: Double): String {
        return VNFormatCurrency.format(plainText)
    }
}
