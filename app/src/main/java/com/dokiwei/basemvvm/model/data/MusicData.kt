package com.dokiwei.basemvvm.model.data

import android.graphics.Bitmap
import android.net.Uri

/**
 * @author DokiWei
 * @date 2023/9/18 16:22
 */
data class MusicData(
    val id: Int = 0,
    val name: String? = null,
    val album: String? = null,
    val imgId: Long? = null,
    val path: String? = null,
    val duration: Long = 0,
    val size: Long = 0,
    val uri: Uri
)