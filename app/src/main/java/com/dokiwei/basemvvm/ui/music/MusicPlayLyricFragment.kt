package com.dokiwei.basemvvm.ui.music

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.media3.common.util.UnstableApi
import com.dirror.lyricviewx.OnPlayClickListener
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.databinding.FragmentMusicPlayLyricBinding
import com.dokiwei.basemvvm.ui.music.viewmodel.MusicViewModel
import com.mpatric.mp3agic.Mp3File
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@UnstableApi
/**
 * @author DokiWei
 * @date 2023/10/5 22:18
 */
class MusicPlayLyricFragment : BaseFragment<FragmentMusicPlayLyricBinding, MusicViewModel>(
    FragmentMusicPlayLyricBinding::inflate, MusicViewModel::class.java
) {

    override fun initFragment(
        binding: FragmentMusicPlayLyricBinding,
        viewModel: MusicViewModel?,
        savedInstanceState: Bundle?
    ) {
        binding.lyricView.apply {
            setLabel("暂无歌词")
            setCurrentColor(Color.WHITE)
            setCurrentTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20f, resources.displayMetrics))
            setNormalColor(ContextCompat.getColor(requireContext(), R.color.lyric_normalColor))
            setNormalTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics))
            setTimelineColor(Color.WHITE)
            setTimeTextColor(Color.WHITE)
            setTimelineTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }
        binding.lyricView.setDraggable(true, object : OnPlayClickListener {
            override fun onPlayClick(time: Long): Boolean {
                viewModel?.player?.value?.apply {
                    seekTo(time)
                    return true
                }
                return false
            }

        })
        lifecycleScope.launch {
            withStarted {
                launch {
                    viewModel?.currentTime?.collectLatest { currentTime ->
                        binding.lyricView.updateTime(currentTime)
                    }
                }
                launch {
                    viewModel?.path?.collectLatest { string ->
                        string?.let { path ->
                            Mp3File(path).apply {
                                if (hasId3v2Tag()) {
                                    val tag = id3v2Tag
                                    binding.lyricView.loadLyric(tag.lyrics)
                                }

                            }
                        }
                    }
                }

            }
        }

    }
}