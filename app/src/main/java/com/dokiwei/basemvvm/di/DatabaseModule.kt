package com.dokiwei.basemvvm.di

import android.content.Context
import androidx.room.Room
import com.dokiwei.basemvvm.model.database.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author DokiWei
 * @date 2023/10/4 15:30
 */
@Module
@InstallIn(ViewModelComponent::class)
object DatabaseModule {

    @Provides
    fun provideArticleDatabase(@ApplicationContext context: Context): MusicDatabase {
        return Room.databaseBuilder(context,MusicDatabase::class.java,"musicDatabase").build()
    }
}