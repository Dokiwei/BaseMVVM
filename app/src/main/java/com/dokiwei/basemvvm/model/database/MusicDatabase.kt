package com.dokiwei.basemvvm.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dokiwei.basemvvm.model.dao.MusicDao
import com.dokiwei.basemvvm.model.entity.MusicEntity

/**
 * @author DokiWei
 * @date 2023/10/4 15:20
 */
@Database(
    entities = [MusicEntity::class],
    version = 1,
    exportSchema =false
    )
abstract class MusicDatabase:RoomDatabase() {

    abstract fun musicDao():MusicDao
}