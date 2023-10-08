package com.dokiwei.basemvvm.util

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresApi
import com.dokiwei.basemvvm.ui.app.MyApplication
import java.io.ByteArrayOutputStream

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

    fun byteArrayToBitmap(byteArray: ByteArray)=BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    fun byteArrayToBitmap(byteArray: ByteArray, size: Int) =
        byteArrayToBitmap(byteArray, size, size)

    private fun byteArrayToBitmap(byteArray: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap =
        BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
        }

    fun pathToByteArray(path:String): ByteArray? {
        val retriever = MediaMetadataRetriever ()
        retriever.setDataSource (path)
        val art = retriever.embeddedPicture
        retriever.release()
        return art
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun albumIdToBitmap(albumId: Long?, size: Int): Bitmap? {
        if (albumId != null) {
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                albumId
            )
            return try {
                MyApplication.context.contentResolver.loadThumbnail(
                    contentUri,
                    Size(size, size),
                    null)
            } catch (e: Exception) {
                return null
            }
        } else return null
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