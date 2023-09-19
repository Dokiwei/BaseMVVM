package com.dokiwei.basemvvm.ui.music.viewmodel

import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow

@UnstableApi
/**
 * @author DokiWei
 * @date 2023/9/18 22:59
 */
class MusicViewModel : ViewModel() {

    //播放状态
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying
    fun setIsPlaying(boolean: Boolean) {
        _isPlaying.value = boolean
    }
}