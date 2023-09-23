package com.dokiwei.basemvvm.ui.music

import android.animation.ObjectAnimator
import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
/**
 * @author DokiWei
 * @date 2023/9/18 16:20
 */
class MusicFragment : BaseFragment<FragmentMusicBinding, MusicViewModel>(
    FragmentMusicBinding::inflate, MusicViewModel::class.java, true
) {

    private var player: Player? = null
    private var endTime = 0L
    private var seekBarProcess = 0

    override fun initFragment(
        binding: FragmentMusicBinding, viewModel: MusicViewModel?, savedInstanceState: Bundle?
    ) {
        initStatusPadding { binding.appBarLayoutFragmentMusic.updatePadding(top = it) }

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

        val albumAnim = initAlbumAnimator(binding)
        initListener(
            mediaControllerFuture, musicDataList, binding, viewModel, musicDataList.isNotEmpty()
        )

        onPlayingChange(viewModel, binding, albumAnim)
    }

    /**
     * 当音乐播放状态为暂停/播放时的事件
     *
     * @param viewModel
     * @param binding
     * @param albumAnim
     */
    private fun onPlayingChange(
        viewModel: MusicViewModel?, binding: FragmentMusicBinding, albumAnim: ObjectAnimator
    ) {
        lifecycleScope.launch(MyCoroutineExceptionHandler.handler) {
            withStarted {
                launch {
                    viewModel?.isPlaying?.collectLatest { isPlaying ->
                        if (isPlaying) {
                            binding.name.isSelected = true
                            if (albumAnim.isPaused) {
                                albumAnim.resume()
                            } else {
                                albumAnim.start()
                            }
                        } else {
                            binding.name.isSelected = false
                            albumAnim.pause()
                        }
                    }
                }
            }
            viewModel?.isPlaying?.collectLatest { isPlaying ->
                binding.pause.setImg(
                    isPlaying, R.drawable.pause_icon, R.drawable.play_icon
                )
                launch {
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
            }
        }
    }

    /**
     * 初始化专辑图片的播放动画
     */
    private fun initAlbumAnimator(binding: FragmentMusicBinding) = ObjectAnimator.ofFloat(
        binding.musicControllerAlbumFragmentMusic, Constants.AnimProperty.Rotation.name, 0f, 360f
    ).apply {
        repeatCount = ObjectAnimator.INFINITE
        interpolator = LinearInterpolator()
        duration = 5000
    }


    //监听事件初始化
    private fun initListener(
        mediaControllerFuture: ListenableFuture<MediaController>,
        musicDataList: List<MusicData>,
        binding: FragmentMusicBinding,
        viewModel: MusicViewModel?,
        dataIsNotEmpty: Boolean
    ) {
        mediaControllerFuture.addListener(
            {
                if (dataIsNotEmpty) player = mediaControllerFuture.get().apply {
                    init(viewModel, binding, musicDataList)
                }
                player?.takeIf { it.mediaItemCount == 0 }?.apply {
                    setMediaItems(musicDataList.map {
                        MediaItem.Builder().setMediaId("${it.uri}").build()
                    })
                }
                player?.addListener(playerListener(binding, musicDataList, viewModel))

                val dataAdapter = MusicDataAdapter(musicDataList, this@MusicFragment)
                binding.recyclerViewFragmentMusic.adapter = dataAdapter
                dataAdapter.setOnItemClickListener(adapterListener())

            }, MoreExecutors.directExecutor()
        )

        //控件监听初始化
        binding.prev.setOnClickListener(onClick(binding, viewModel))
        binding.next.setOnClickListener(onClick(binding, viewModel))
        binding.pause.setOnClickListener(onClick(binding, viewModel))
        binding.seek.setOnSeekBarChangeListener(seekBarChangeListener(binding))
    }

    /**
     * 在第一次加载MediaController时,如果当前音乐正在播放那么从view model中存取数据
     *
     *
     * @param viewModel
     * @param binding
     * @param musicDataList
     */
    private fun MediaController.init(
        viewModel: MusicViewModel?, binding: FragmentMusicBinding, musicDataList: List<MusicData>
    ) {
        takeUnless { isPlaying }?.apply {
            viewModel?.let {
                val ct = it.currentTime.value
                val et = it.endTime.value
                if (ct != null && et != null) {
                    val s = "${Conversion.longConversionToTimeString(ct)}/${
                        Conversion.longConversionToTimeString(et)
                    }"
                    binding.time.text = s
                    binding.seek.max = et.toInt()
                    binding.seek.progress = ct.toInt()
                }
            }
        }
        takeIf { isPlaying }?.let { _ ->
            val mediaMetadata = mediaMetadata
            mediaMetadata.title?.let { viewModel?.setTitle(it) }
            mediaMetadata.artworkData?.let {
                viewModel?.setArtworkData(
                    Conversion.bitmapToByteArray(
                        Conversion.byteArrayToBitmap(
                            it, binding.musicControllerAlbumFragmentMusic.width
                        )
                    )
                )
            }
            viewModel?.setIsPlaying(true)
        }
        also {
            viewModel?.let { vm ->
                val title = vm.title.value
                val artworkData = vm.artworkData.value
                if (title != null && artworkData != null) {
                    setMusicTitleAndAlbum(
                        title, artworkData, binding, musicDataList, viewModel
                    )
                }
            }
        }
    }

    /**
     * 进度条监听
     *
     * @param binding
     */
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

    /**
     * 适配器监听
     */
    private fun adapterListener() = object : MusicDataAdapter.OnItemClickListener {
        override fun onItemClick(musicData: MusicData, position: Int) {
            player?.apply {
                seekTo(position, 0)
                prepare()
                play()
            }
        }
    }

    /**
     * 播放控制监听
     *
     * @param binding 获取控件
     * @param musicDataList 音乐数据列表
     * @param viewModel
     */
    private fun playerListener(
        binding: FragmentMusicBinding, musicDataList: List<MusicData>, viewModel: MusicViewModel?
    ) = object : Player.Listener {

        override fun onPlayerError(error: PlaybackException) {
            Toast.makeText(requireContext(), "PlayerError:" + error.message, Toast.LENGTH_LONG)
                .show()
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            setMusicTitleAndAlbum(mediaMetadata, binding, musicDataList, viewModel)
        }


        override fun onIsPlayingChanged(isPlaying: Boolean) {
            viewModel?.setIsPlaying(isPlaying)
            if (!isPlaying) player?.currentPosition?.let {
                viewModel?.setCurrentTime(it)
                viewModel?.setEndTime(endTime)
            }


        }


    }

    private fun setMusicTitleAndAlbum(
        title: CharSequence,
        artworkData: ByteArray,
        binding: FragmentMusicBinding,
        musicDataList: List<MusicData>,
        viewModel: MusicViewModel?
    ) {
        viewModel?.setTitle(title)
        binding.name.text = title
        musicDataList.find {
            it.name == title
        }?.duration?.let {
            endTime = it
            binding.seek.max = endTime.toInt()
        }

        viewModel?.apply {
            val lastAlbum = Conversion.byteArrayToBitmap(
                artworkData, binding.musicControllerAlbumFragmentMusic.width
            ).run {
                Conversion.bitmapToByteArray(
                    this
                )
            }
            setArtworkData(lastAlbum)
        }
        val reqWidth = binding.musicControllerAlbumFragmentMusic.width
        val reqHeight = binding.musicControllerAlbumFragmentMusic.height
        Glide.with(this@MusicFragment)
            .load(Conversion.byteArrayToBitmap(artworkData, reqWidth, reqHeight))
            .override(reqWidth, reqHeight).into(binding.musicControllerAlbumFragmentMusic)
    }

    private fun setMusicTitleAndAlbum(
        mediaMetadata: MediaMetadata,
        binding: FragmentMusicBinding,
        musicDataList: List<MusicData>,
        viewModel: MusicViewModel?
    ) {
        val title = mediaMetadata.title
        val artworkData = mediaMetadata.artworkData
        if (title != null && artworkData != null) {
            setMusicTitleAndAlbum(title, artworkData, binding, musicDataList, viewModel)
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
                }

                binding.prev.id -> {
                    player?.apply {
                        seekToPrevious()
                        prepare()
                        play()
                    }
                }

                binding.pause.id -> {
                    viewModel?.let {
                        if (it.isPlaying.value) {
                            player?.pause()
                        } else {
                            player?.prepare()
                            player?.play()
                        }
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