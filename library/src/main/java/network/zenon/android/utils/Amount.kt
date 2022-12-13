package network.zenon.android.utils

import kotlin.math.pow

object AmountUtils {
    fun extractDecimals(num: Double, decimals: Long): Long {
        return (num * 10.0.pow(decimals.toDouble())).toLong()
    }

    fun addDecimals(num: Long, decimals: Long): Double {
        val numberWithDecimals = num / 10.0.pow(decimals.toDouble())
        return if (numberWithDecimals == numberWithDecimals.toLong().toDouble()) {
            numberWithDecimals.toLong().toDouble()
        } else numberWithDecimals
    }
}