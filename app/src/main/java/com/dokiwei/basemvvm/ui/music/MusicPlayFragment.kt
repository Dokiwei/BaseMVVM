package com.dokiwei.basemvvm.ui.music

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.widget.SeekBar
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.palette.graphics.Palette
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.databinding.FragmentMusicPlayBinding
import com.dokiwei.basemvvm.ui.music.adapter.MusicPlayPagerAdapter
import com.dokiwei.basemvvm.ui.music.viewmodel.MusicViewModel
import com.dokiwei.basemvvm.util.ColorUtil.paletteColor
import com.dokiwei.basemvvm.util.Constants
import com.dokiwei.basemvvm.util.Conversion
import com.dokiwei.basemvvm.util.MyCoroutineExceptionHandler
import com.mpatric.mp3agic.Mp3File
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@UnstableApi
/**
 * @author DokiWei
 * @date 2023/10/3 20:52
 */
@AndroidEntryPoint
class MusicPlayFragment : BaseFragment<FragmentMusicPlayBinding, MusicViewModel>(
    FragmentMusicPlayBinding::inflate, MusicViewModel::class.java, true
) {

    private lateinit var viewModel: MusicViewModel
    private lateinit var binding: FragmentMusicPlayBinding
    private var bitmap: Bitmap? = null

    @SuppressLint("RestrictedApi")
    override fun initFragment(
        binding: FragmentMusicPlayBinding, viewModel: MusicViewModel?, savedInstanceState: Bundle?
    ) {
        this.binding = binding
        viewModel?.let {
            this.viewModel = it
        }

        initStatusPadding(Constants.SystemHeightType.StatusBars) {
            binding.appBar.updatePadding(
                top = it
            )
        }
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                setCustomAnimations(
                    R.anim.anim_fragment_in_bottom, R.anim.anim_fragment_out_top
                )
                remove(this@MusicPlayFragment)
            }.commit()
            parentFragmentManager.popBackStack(
                "MusicPlayFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
        binding.playPause.setOnClickListener {
            viewModel?.player?.value?.apply {
                if (isPlaying) pause()
                else play()
            }
        }
        binding.next.setOnClickListener {
            viewModel?.player?.value?.apply {
                seekToNext()
                prepare()
                play()
            }
        }
        binding.prev.setOnClickListener {
            viewModel?.player?.value?.apply {
                seekToPrevious()
                prepare()
                play()
            }
        }
        binding.seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?, progress: Int, fromUser: Boolean
            ) {
                if (fromUser) viewModel?.setCurrentTime(progress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel?.player?.value?.apply {
                    when (isPlaying) {
                        true -> {
                            seekTo(viewModel.currentTime.value)
                            prepare()
                            play()
                        }

                        false -> seekTo(viewModel.currentTime.value)
                    }
                }
            }

        })
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        bitmap?.recycle()
    }

    private fun initView() {
        binding.viewPager.adapter = MusicPlayPagerAdapter(childFragmentManager, lifecycle)
        viewModel.takeIf { !it.path.value.isNullOrBlank() }?.let {
            lifecycleScope.launch(MyCoroutineExceptionHandler.handler) {
                withStarted {
                    launch {
                        it.currentTime.collectLatest { currentTime ->
                            binding.seekBar.setProgress(currentTime.toInt(), true)
                            binding.currentTime.text =
                                Conversion.longConversionToTimeString(currentTime)
                        }
                    }
                    launch {
                        it.endTime.collectLatest { endTime ->
                            binding.endTime.text = Conversion.longConversionToTimeString(endTime)
                            binding.seekBar.max = endTime.toInt()
                        }
                    }
                    launch {
                        it.isPlaying.collectLatest { isPlaying ->
                            when (isPlaying) {
                                true -> binding.playPause.setImageResource(R.drawable.ic_pause)
                                false -> binding.playPause.setImageResource(R.drawable.ic_play)
                            }
                        }
                    }
                    launch {
                        it.path.collectLatest { string ->
                            string?.let { path ->
                                setTag(path)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setTag(
        path: String
    ) {
        Mp3File(path).apply {
            if (hasId3v2Tag()) {
                val tag = id3v2Tag
                binding.toolbar.title = tag.title
                binding.toolbar.subtitle = tag.artist
                bitmap = Conversion.byteArrayToBitmap(tag.albumImage)
                bitmap?.let { img ->
                    Blurry.with(requireContext()).radius(15).sampling(2).async().animate().from(img)
                        .into(binding.bg)
                    Palette.from(img).generate { palette ->
                        val pair = paletteColor(palette)
                        pair?.let { colors ->
                            binding.playPause.setBackgroundColor(
                                colors.first
                            )
                            binding.playPause.setColorFilter(colors.second)
                            binding.seekBar.apply {
                                progressDrawable.colorFilter = PorterDuffColorFilter(
                                    colors.first, PorterDuff.Mode.SRC_ATOP
                                )
                                thumb.colorFilter = PorterDuffColorFilter(
                                    colors.first, PorterDuff.Mode.SRC_ATOP
                                )
                            }
                        }
                    }
                }
            }
        }
    }


}