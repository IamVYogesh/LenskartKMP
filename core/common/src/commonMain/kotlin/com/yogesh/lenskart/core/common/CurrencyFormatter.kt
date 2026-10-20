package com.yogesh.lenskart.core.common

import kotlin.math.abs

/** 125000 -> "₹1,25,000" (Indian digit grouping). Pure common code => tested in commonTest. */
fun Long.formatInr(): String {
    val digits = abs(this).toString()
    val grouped = if (digits.length <= 3) {
        digits
    } else {
        val last3 = digits.takeLast(3)
        val rest = digits.dropLast(3).reversed().chunked(2).joinToString(",").reversed()
        "$rest,$last3"
    }
    return (if (this < 0) "-" else "") + "₹" + grouped
}
