package com.dokiwei.basemvvm.util

import android.net.Uri
import android.provider.MediaStore
import com.dokiwei.basemvvm.model.entity.MusicEntity
import com.dokiwei.basemvvm.ui.app.MyApplication

/**
 * @author DokiWei
 * @date 2023/9/18 16:22
 */
object MetadataReaderUtils {

    fun getMusicDataList(): MutableList<MusicEntity> {
        val list = mutableListOf<MusicEntity>()
        MyApplication.context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null, null, null,
            MediaStore.Audio.Media.IS_MUSIC
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val title =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val author =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val album =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val albumId =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                val path =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val duration =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                val musicEntity = MusicEntity(
                    title = title,
                    author = author,
                    album = album,
                    imgId = albumId,
                    path = path,
                    duration = duration.toLong(),
                    uri = uri.toString()
                )
                list.add(musicEntity)
            }
            cursor.close()
        }
        return list
    }

}
