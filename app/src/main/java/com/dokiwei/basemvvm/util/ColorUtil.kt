package com.dokiwei.basemvvm.util

import android.graphics.Color
import androidx.palette.graphics.Palette
import kotlin.math.pow

/**
 * @author DokiWei
 * @date 2023/10/5 16:45
 */
object ColorUtil {
    fun paletteColor(palette: Palette?): Pair<Int, Int>? {
        return palette?.let {
            val swatch = it.dominantSwatch ?: it.lightVibrantSwatch ?: it.lightMutedSwatch
            ?: it.vibrantSwatch ?: it.mutedSwatch ?: it.darkVibrantSwatch ?: it.darkMutedSwatch
            swatch?.let { s ->
                val color = s.rgb
                val onRgb = when {
                    calculateRelativeLuminance(color) > 0.5 -> Color.BLACK
                    else -> Color.WHITE
                }
                Pair(color, onRgb)
            }
        }
    }

    private fun calculateRelativeLuminance(color: Int): Double {
        //获取颜色的RGB分量，并转换为0到1之间的数值
        var red = Color.red(color) / 255.0
        var green = Color.green(color) / 255.0
        var blue = Color.blue(color) / 255.0
        //根据公式计算每个分量的新值
        red = if (red <= 0.03928) red / 12.92 else ((red + 0.055) / 1.055).pow(2.4)
        green = if (green <= 0.03928) green / 12.92 else ((green + 0.055) / 1.055).pow(2.4)
        blue = if (blue <= 0.03928) blue / 12.92 else ((blue + 0.055) / 1.055).pow(2.4)
        //将得到的三个新分量分别乘以系数后相加，得到相对亮度
        return red * 0.2126 + green * 0.7152 + blue * 0.0722
    }
}