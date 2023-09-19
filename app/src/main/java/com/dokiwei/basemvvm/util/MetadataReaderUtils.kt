package com.dokiwei.basemvvm.util

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import com.dokiwei.basemvvm.model.data.MusicData

/**
 * @author DokiWei
 * @date 2023/9/18 16:22
 */
object MetadataReaderUtils {

    fun getMusicDataList(context: Context): List<MusicData> {
        val list = mutableListOf<MusicData>()
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null, null, null,
            MediaStore.Audio.Media.IS_MUSIC
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val album =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val albumId =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                val path =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val duration =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val size =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                val musicData = MusicData(
                    id = id.toInt(),
                    name = name,
                    album = album,
                    imgId = albumId,
                    path = path,
                    duration = duration.toLong(),
                    size = size.toLong(),
                    uri = uri
                )
                list.add(musicData)
            }
            cursor.close()
        }
        return list
    }

    fun getAlbumArt(context: Context, albumId: Long?): Bitmap? {
        if (albumId != null) {
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                albumId
            )
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) context.contentResolver.loadThumbnail(
                    contentUri,
                    Size(640, 480),
                    null
                )
                else {
                    val uri = ContentUris.withAppendedId(contentUri, albumId)
                    val pfd = context.contentResolver
                        .openFileDescriptor(uri, "r")
                    val fd = pfd?.fileDescriptor
                    pfd?.close()
                    BitmapFactory.decodeFileDescriptor(
                        fd, null,
                        BitmapFactory.Options()
                    )
                }
            } catch (e: Exception) {
                return null
            }
        } else return null
    }
}
