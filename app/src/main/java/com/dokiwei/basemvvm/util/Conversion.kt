package com.dokiwei.basemvvm.util

/**
 * @author DokiWei
 * @date 2023/9/19 13:34
 */
object Conversion {
    fun longConversionToTimeString(long: Long): String {
        val currentTimeSeconds = (long / 1000) % 60
        val currentTimeMinutes = (long / (1000 * 60)) % 60
        return String.format("%02d:%02d", currentTimeMinutes, currentTimeSeconds)
    }
}