package io.github.vinceglb.filekit.sample.shared.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

internal fun Long.formatBytes(): String {
    val bytes = this
    if (bytes < 0) {
        return "Unknown"
    }

    val units = listOf("B", "KB", "MB", "GB", "TB", "PB")
    val absBytes = abs(bytes.toDouble())

    if (absBytes < 1.0) {
        return "0 B"
    }

    var value = absBytes
    var unitIndex = 0

    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex++
    }

    val precision = if (value >= 10 || unitIndex == 0) 0 else 1
    val factor = 10.0.pow(precision)
    val rounded = (value * factor).roundToInt() / factor
    val text = if (precision == 0) rounded.toInt().toString() else rounded.toString()
    val sign = if (bytes < 0) "-" else ""

    return "$sign$text ${units[unitIndex]}"
}
