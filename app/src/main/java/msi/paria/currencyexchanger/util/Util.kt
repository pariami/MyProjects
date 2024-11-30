package msi.paria.currencyexchanger.util

fun persianToEnglishNumber(persianNumber: String): String {
    val persianDigits = listOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    val englishDigits = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")

    return persianDigits.foldIndexed(persianNumber) { index, result, persian ->
        result.replace(persian, englishDigits[index])
    }
}

fun String.isValidDecimalInput(): Boolean {
    val decimalRegex = "^\\d*\\.?\\d*$".toRegex()
    return this.matches(decimalRegex)
}