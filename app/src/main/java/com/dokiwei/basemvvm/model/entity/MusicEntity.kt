package com.dokiwei.basemvvm.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author DokiWei
 * @date 2023/10/4 15:00
 */
@Entity(tableName = "music")
data class MusicEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String? = null,
    val author: String? = null,
    val album: String? = null,
    val path: String? = null,
    val imgId: Long? = null,
    val duration: Long = 0,
    val uri: String,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") val lastModifiedTime: String? = null
)