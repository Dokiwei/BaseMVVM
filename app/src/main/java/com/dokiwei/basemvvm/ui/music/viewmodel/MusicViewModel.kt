package com.dokiwei.basemvvm.ui.music.viewmodel

import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.dokiwei.basemvvm.model.database.MusicDatabase
import com.dokiwei.basemvvm.model.entity.MusicEntity
import com.dokiwei.basemvvm.util.Conversion
import com.dokiwei.basemvvm.util.MetadataReaderUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@UnstableApi
/**
 * @author DokiWei
 * @date 2023/9/18 22:59
 */
@HiltViewModel
class MusicViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle, db: MusicDatabase
) : ViewModel() {
    companion object {
        private const val IS_PLAYING = "isPlaying"
        private const val END_TIME = "end_time"
        private const val MEDIA_PATH = "path"
        private const val MEDIA_TITLE = "title"
        private const val CURRENT_TIME = "current_time"
        private const val MEDIA_MODE_REPEAT = "media_mode_repeat"
        private const val MEDIA_MODE_SHUFFLE = "media_mode_shuffle"
    }

    private val dao = db.musicDao()

    var player: MutableStateFlow<Player?> = MutableStateFlow(null)
    lateinit var musicList: MutableList<MusicEntity>
    val isPlayerReady = MutableStateFlow(false)

    var sortDescendingMode = false
    val repeatMode = savedStateHandle.getStateFlow(MEDIA_MODE_REPEAT, -1)
    val shuffleModel = savedStateHandle.getStateFlow(MEDIA_MODE_SHUFFLE, -1)

    val isPlaying = savedStateHandle.getStateFlow(IS_PLAYING, false)
    val endTime = savedStateHandle.getStateFlow<Long>(END_TIME, 0)
    val currentTime = savedStateHandle.getStateFlow<Long>(CURRENT_TIME, 0)
    val path = savedStateHandle.getStateFlow<String?>(MEDIA_PATH, null)
    val title = savedStateHandle.getStateFlow<CharSequence>(MEDIA_TITLE, "")
    suspend fun getAllMusic() {
        withContext(Dispatchers.IO) {
            val result = dao.getAllMusic()
            musicList = if (result.isNotEmpty()) result.toMutableList()
            else {
                val insert = launch { dao.insertList(MetadataReaderUtils.getMusicDataList()) }
                insert.join()
                dao.getAllMusic().toMutableList()
            }
        }
    }

    fun setPath(path:String) {
        savedStateHandle[MEDIA_PATH] = path
    }

    fun setTitle(title: CharSequence) {
        savedStateHandle[MEDIA_TITLE] = title
    }


    private fun changeModeIndex(index: Int, max: Int): Int {
        return if (index < max) {
            index + 1
        } else {
            -1
        }
    }

    fun changeRepeatMode() {
        savedStateHandle[MEDIA_MODE_REPEAT] = changeModeIndex(repeatMode.value, 1)
        if (shuffleModel.value == 0 && repeatMode.value == 0) {
            savedStateHandle[MEDIA_MODE_SHUFFLE] = -1
        }
    }

    fun changeShuffleMode() {
        savedStateHandle[MEDIA_MODE_SHUFFLE] = changeModeIndex(shuffleModel.value, 0)
        if (shuffleModel.value == 0 && repeatMode.value == 0) {
            savedStateHandle[MEDIA_MODE_REPEAT] = -1
        }
    }

    fun setCurrentTime(long: Long) {
        savedStateHandle[CURRENT_TIME] = long
    }

    fun setEndTime(long: Long) {
        savedStateHandle[END_TIME] = long
    }

    fun setIsPlaying(boolean: Boolean) {
        savedStateHandle[IS_PLAYING] = boolean
    }


    fun restoreState(title: TextView, progress: SeekBar, imageView: ImageView, fragment: Fragment) {
        restoreTitle(title)
        restoreProgress(progress)
        restoreAlbumImage(imageView, fragment)
    }

    private fun restoreTitle(view: TextView) {
        view.text = title.value
    }

    private fun restoreProgress(view: SeekBar) {
        view.max = endTime.value.toInt()
    }

    private fun restoreAlbumImage(
        imageView: ImageView,
        fragment: Fragment
    ) {
        path.value?.let {
            val reqWidth = imageView.width
            val reqHeight = imageView.height
            Glide.with(fragment)
                .load(Conversion.pathToByteArray(it))
                .override(reqWidth, reqHeight).into(imageView)
        }
    }
}