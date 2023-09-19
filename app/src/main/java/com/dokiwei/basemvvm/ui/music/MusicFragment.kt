package com.dokiwei.basemvvm.ui.music

import android.animation.ObjectAnimator
import android.content.ComponentName
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.bumptech.glide.Glide
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.content.service.MediaService
import com.dokiwei.basemvvm.databinding.FragmentMusicBinding
import com.dokiwei.basemvvm.model.data.MusicData
import com.dokiwei.basemvvm.ui.music.adapter.MusicDataAdapter
import com.dokiwei.basemvvm.ui.music.viewmodel.MusicViewModel
import com.dokiwei.basemvvm.util.Constants
import com.dokiwei.basemvvm.util.Conversion
import com.dokiwei.basemvvm.util.MetadataReaderUtils
import com.dokiwei.basemvvm.util.MyCoroutineExceptionHandler
import com.dokiwei.basemvvm.util.setImg
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ForkJoinPool

@UnstableApi
/**
 * @author DokiWei
 * @date 2023/9/18 16:20
 */
class MusicFragment : BaseFragment<FragmentMusicBinding, MusicViewModel>(
    FragmentMusicBinding::inflate, MusicViewModel::class.java
) {

    private var player: Player? = null
    private var endTime = 0L
    private var seekBarProcess = 0

    override fun initFragment(
        binding: FragmentMusicBinding, viewModel: MusicViewModel?, savedInstanceState: Bundle?
    ) {
        val albumAnim =
            ObjectAnimator.ofFloat(binding.album, Constants.AnimProperty.Rotation.name, 0f, 360f)
                .apply {
                    repeatCount = ObjectAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    duration = 5000
                }
        //从系统的contentResolver获取到所有音乐资源信息
        val musicDataList = MetadataReaderUtils.getMusicDataList(requireContext())
        //设置一个全局的sessionToken
        val sessionToken = SessionToken(
            requireContext().applicationContext,
            ComponentName(requireContext(), MediaService::class.java)
        )
        //获取到mediaControllerFuture
        val mediaControllerFuture =
            MediaController.Builder(requireContext(), sessionToken).buildAsync()

        //监听事件初始化
        initListener(mediaControllerFuture, musicDataList, binding, albumAnim, viewModel)
    }


    //监听事件初始化
    private fun initListener(
        mediaControllerFuture: ListenableFuture<MediaController>,
        musicDataList: List<MusicData>,
        binding: FragmentMusicBinding,
        albumAnim: ObjectAnimator,
        viewModel: MusicViewModel?
    ) {
        mediaControllerFuture.addListener(
            {
                //获取player 会导致卡顿,但是为了与后台播放service进行联动必须这样获取 google推荐方式
                player = mediaControllerFuture.get()
                //初始化获取player的媒体列表
                player?.setMediaItems(musicDataList.map {
                    MediaItem.Builder().setMediaId("${it.uri}").build()
                })
                //player监听 必须在本Listener进行设置,不然获取不到player
                player?.addListener(playerListener(binding, musicDataList, albumAnim))
                //设置适配器
                val dataAdapter = MusicDataAdapter(musicDataList, this@MusicFragment)
                dataAdapter.apply {
                    binding.rvList.adapter = this
                    setOnItemClickListener(adapterListener(viewModel))
                }
            },
            //Google的提交方式 提交到其他线程会报错
            MoreExecutors.directExecutor()
        )

        //控件监听初始化
        binding.prev.setOnClickListener(onClick(binding, viewModel))
        binding.next.setOnClickListener(onClick(binding, viewModel))
        binding.pause.setOnClickListener(onClick(binding, viewModel))
        binding.seek.setOnSeekBarChangeListener(seekBarChangeListener(binding))
    }

    //滑动条监听
    private fun seekBarChangeListener(binding: FragmentMusicBinding) =
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBarProcess = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                player?.let {
                    if (it.isPlaying) {
                        it.seekTo(seekBarProcess.toLong())
                        it.prepare()
                        it.play()
                    } else {
                        binding.seek.progress = 0
                    }
                }
            }
        }

    //适配器监听
    private fun adapterListener(viewModel: MusicViewModel?) =
        object : MusicDataAdapter.OnItemClickListener {
            override fun onItemClick(musicData: MusicData, position: Int) {
                player?.apply {
                    seekTo(position, 0)
                    prepare()
                    play()
                }
                viewModel?.setIsPlaying(true)
            }
        }

    //播放控制监听
    private fun playerListener(
        binding: FragmentMusicBinding, musicDataList: List<MusicData>, albumAnim: ObjectAnimator
    ) = object : Player.Listener {

        override fun onPlayerError(error: PlaybackException) {
            Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            mediaMetadata.title?.let { title ->
                binding.name.text = title
                musicDataList.find {
                    it.name == title
                }?.duration?.let {
                    endTime = it
                    binding.seek.max = endTime.toInt()
                }
            }
            mediaMetadata.artworkData?.let {
                Glide.with(this@MusicFragment).load(BitmapFactory.decodeByteArray(it, 0, it.size))
                    .into(binding.album)
            }
        }


        override fun onIsPlayingChanged(isPlaying: Boolean) {
            binding.pause.setImg(
                isPlaying, R.drawable.pause_icon, R.drawable.play_icon
            )
            lifecycleScope.launch(MyCoroutineExceptionHandler.handler) {
                while (isPlaying) {
                    val currentPosition = player?.currentPosition
                    withContext(Dispatchers.Main) {
                        currentPosition?.let { long ->
                            val s = "${Conversion.longConversionToTimeString(long)}/${
                                Conversion.longConversionToTimeString(endTime)
                            }"
                            binding.time.text = s
                            binding.seek.setProgress(long.toInt(), true)

                        }
                    }
                    delay(1000)
                }
            }
            if (isPlaying) {
                if (albumAnim.isPaused) {
                    albumAnim.resume()
                } else {
                    albumAnim.start()
                }
            } else {
                albumAnim.pause()
            }

        }


    }

    //点击监听
    private fun onClick(binding: FragmentMusicBinding, viewModel: MusicViewModel?) =
        View.OnClickListener { v ->
            when (v.id) {
                binding.next.id -> {
                    player?.apply {
                        seekToNext()
                        prepare()
                        play()
                    }
                    viewModel?.setIsPlaying(true)
                }

                binding.prev.id -> {
                    player?.apply {
                        seekToPrevious()
                        prepare()
                        play()
                    }
                    viewModel?.setIsPlaying(true)
                }

                binding.pause.id -> {
                    viewModel?.let {
                        it.setIsPlaying(
                            if (it.isPlaying.value) {
                                player?.pause()
                                false
                            } else {
                                player?.prepare()
                                player?.play()
                                true
                            }
                        )
                    }
                }
            }
        }

    //释放资源 防止内存泄漏
    override fun onDestroy() {
        player?.release()
        super.onDestroy()
    }

}