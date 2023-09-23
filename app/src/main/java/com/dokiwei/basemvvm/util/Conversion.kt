package com.dokiwei.basemvvm.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.FileDescriptor

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

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray = ByteArrayOutputStream().run {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
        toByteArray()
    }

    fun fileDescriptorToBitmap(fd: FileDescriptor?, size: Int): Bitmap =
        BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFileDescriptor(fd, null, this)
            inSampleSize = calculateInSampleSize(this, size, size)
            inJustDecodeBounds = false
            BitmapFactory.decodeFileDescriptor(fd, null, this)
        }

    fun byteArrayToBitMap(byteArray: ByteArray, size: Int) =
        byteArrayToBitMap(byteArray, size, size)

    fun byteArrayToBitMap(byteArray: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap =
        BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
        }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 4
            }
        }
        return inSampleSize
    }
}