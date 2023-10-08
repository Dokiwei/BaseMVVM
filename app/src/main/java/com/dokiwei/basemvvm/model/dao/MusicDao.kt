package com.dokiwei.basemvvm.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dokiwei.basemvvm.model.entity.MusicEntity

/**
 * @author DokiWei
 * @date 2023/10/4 15:03
 */
@Dao
interface MusicDao {

    @Query("SELECT * FROM music")
    suspend fun getAllMusic(): List<MusicEntity>

    @Insert
    suspend fun insertList(music: List<MusicEntity>)

    @Insert
    suspend fun insert(music: MusicEntity)

    @Update
    suspend fun update(music: MusicEntity)

    @Query("SELECT * FROM music WHERE id = :id")
    suspend fun getMusicById(id: Int):MusicEntity
}