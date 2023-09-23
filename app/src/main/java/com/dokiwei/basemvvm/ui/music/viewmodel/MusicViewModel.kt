package com.dokiwei.basemvvm.ui.music.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@UnstableApi
/**
 * @author DokiWei
 * @date 2023/9/18 22:59
 */
@HiltViewModel
class MusicViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val IS_PLAYING = "isPlaying"
        private const val MEDIA_ARTWORK_DATA = "artworkData"
        private const val MEDIA_TITLE = "title"
        private const val END_TIME = "end_time"
        private const val CURRENT_TIME = "current_time"
    }

    val isPlaying = savedStateHandle.getStateFlow(IS_PLAYING, false)
    val endTime = savedStateHandle.getStateFlow<Long?>(END_TIME, null)
    val currentTime = savedStateHandle.getStateFlow<Long?>(CURRENT_TIME, null)
    val artworkData = savedStateHandle.getStateFlow<ByteArray?>(MEDIA_ARTWORK_DATA, null)
    val title = savedStateHandle.getStateFlow<CharSequence?>(MEDIA_TITLE, null)

    fun setCurrentTime(long: Long) {
        savedStateHandle[CURRENT_TIME] = long
    }

    fun setEndTime(long: Long) {
        savedStateHandle[END_TIME] = long
    }

    fun setIsPlaying(boolean: Boolean) {
        savedStateHandle[IS_PLAYING] = boolean
    }

    fun setArtworkData(artworkData: ByteArray) {
        savedStateHandle[MEDIA_ARTWORK_DATA] = artworkData
    }

    fun setTitle(title: CharSequence) {
        savedStateHandle[MEDIA_TITLE] = title
    }
}