package com.dokiwei.basemvvm.ui.music

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.content.service.MediaService
import com.dokiwei.basemvvm.databinding.FragmentMusicBinding
import com.dokiwei.basemvvm.model.entity.MusicEntity
import com.dokiwei.basemvvm.ui.music.adapter.MusicDataAdapter
import com.dokiwei.basemvvm.ui.music.adapter.MusicListAdapter
import com.dokiwei.basemvvm.ui.music.viewmodel.MusicViewModel
import com.dokiwei.basemvvm.util.Constants
import com.dokiwei.basemvvm.util.Conversion
import com.dokiwei.basemvvm.util.MyCoroutineExceptionHandler
import com.dokiwei.basemvvm.util.setImg
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.search.SearchView
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@UnstableApi
/**
 * @author DokiWei
 * @date 2023/9/18 16:20
 */
@AndroidEntryPoint
class MusicFragment : BaseFragment<FragmentMusicBinding, MusicViewModel>(
    FragmentMusicBinding::inflate, MusicViewModel::class.java, true
), View.OnClickListener, Player.Listener, Toolbar.OnMenuItemClickListener,
    SeekBar.OnSeekBarChangeListener, MusicDataAdapter.OnItemClickListener {

    companion object {
        private const val TAG = "MusicFragment"
    }

    private lateinit var player: Player
    private lateinit var binding: FragmentMusicBinding
    private lateinit var viewModel: MusicViewModel

    private var dataAdapter: MusicDataAdapter? = null
    private var playlistAdapter: MusicListAdapter? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var playListRecyclerView: RecyclerView? = null

    @SuppressLint("RestrictedApi")
    override fun initFragment(
        binding: FragmentMusicBinding, viewModel: MusicViewModel?, savedInstanceState: Bundle?
    ) {
        this.binding = binding
        viewModel?.let { this.viewModel = it }
        initSystemBars()
        lifecycleScope.launch {
            viewModel?.getAllMusic()
        }
        initPlayer {
            initView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.removeListener(this)
        dataAdapter = null
        playlistAdapter = null
        bottomSheetDialog = null
        playListRecyclerView?.adapter = null
        playListRecyclerView = null
        binding.root.removeAllViews()
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    private fun initSystemBars() {
        initStatusPadding(Constants.SystemHeightType.StatusBars) {
            binding.appBarLayoutFragmentMusic.updatePadding(
                top = it
            )
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (insets.getInsets(WindowInsetsCompat.Type.navigationBars()).left > 0) {
                    initStatusPadding(Constants.SystemHeightType.NavigationBars) {
                        binding.musicFragmentLayout.updatePadding(
                            left = it
                        )
                    }
                } else {
                    initStatusPadding(Constants.SystemHeightType.NavigationBars) {
                        binding.musicFragmentLayout.updatePadding(
                            right = it
                        )
                    }
                }
            } else {
                // 竖屏
                initStatusPadding(Constants.SystemHeightType.NavigationBars) {
                    binding.musicFragmentLayout.updatePadding(
                        bottom = it
                    )
                }
            }
            // 返回是否消费了插入区域的WindowInsetsCompat对象
            WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.NONE).build()
        }
    }

    private fun initPlayer(initView: () -> Unit) {
        val sessionToken = SessionToken(
            requireContext().applicationContext,
            ComponentName(requireContext(), MediaService::class.java)
        )
        val mediaControllerFuture =
            MediaController.Builder(requireContext(), sessionToken).buildAsync()
        mediaControllerFuture.addListener(
            {
                player = mediaControllerFuture.get().apply {
                    if (mediaItemCount == 0) {
                        setMediaItems(viewModel.musicList.map { musicItem ->
                            MediaItem.Builder()
                                .setMediaId(musicItem.uri)
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(musicItem.title)
                                        .setAlbumTitle(musicItem.album)
                                        .setArtist(musicItem.author)
                                        .setDescription(musicItem.path)
                                        .build()
                                )
                                .build()
                        })
                    }
                    when (isPlaying) {
                        true -> {
                            val mediaMetadata = mediaMetadata
                            mediaMetadata.title?.let { viewModel.setTitle(it) }
                            viewModel.musicList.find {
                                it.uri == currentMediaItem?.mediaId
                            }?.let { item ->
                                item.path?.let { viewModel.setPath(it) }
                            }
                            viewModel.setIsPlaying(true)
                        }

                        false -> {
                            val ct = viewModel.currentTime.value
                            val et = viewModel.endTime.value
                            val s = "${Conversion.longConversionToTimeString(ct)}/${
                                Conversion.longConversionToTimeString(et)
                            }"
                            binding.musicControllerTime.text = s
                            binding.musicControllerSeek.max = et.toInt()
                            binding.musicControllerSeek.progress = ct.toInt()
                        }
                    }
                    viewModel.restoreState(
                        binding.musicControllerTitle,
                        binding.musicControllerSeek,
                        binding.musicControllerAlbumImage,
                        this@MusicFragment
                    )


                    viewModel.player.value = this
                    viewModel.isPlayerReady.value = true
                    addListener(this@MusicFragment)
                }
                initView()
                onPlayingChange()
            }, MoreExecutors.directExecutor()
        )
    }

    /**
     * 初始化监听事件
     */
    private fun initView() {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog?.setContentView(R.layout.fragment_music_list)

        dataAdapter = MusicDataAdapter(this, binding.musicFragmentRecyclerView, R.layout.item_music)
        dataAdapter?.setOnItemClickListener(this)
        binding.musicFragmentRecyclerView.adapter = dataAdapter
        ((binding.musicFragmentRecyclerView.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
        dataAdapter?.submit { viewModel.musicList }

        player.apply {
            takeIf { isPlaying }?.let {
                currentMediaItem?.mediaId?.let { mediaId ->
                    dataAdapter?.mDiffer?.currentList?.find { item ->
                        item.uri == mediaId
                    }?.let { musicData ->
                        dataAdapter?.mDiffer?.currentList?.indexOf(musicData).takeIf { index ->
                            index != -1
                        }?.let {
                            dataAdapter?.setCurrentSelectItem(it)
                        }
                    }
                }
            }
        }
        playlistAdapter = dataAdapter?.mDiffer?.currentList?.let {
            MusicListAdapter(it).apply {
                setOnItemClickListener(onPlayListAdapterItemClickListener())
            }
        }

        bottomSheetDialog?.apply {
            playListRecyclerView = findViewById(R.id.musicFragment_list_recyclerView)
            playListRecyclerView?.adapter = playlistAdapter
            findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let {
                BottomSheetBehavior.from(it).setPeekHeight(400)
            }
        }

        val isScroll = MutableStateFlow(true)
        lifecycleScope.launch {
            while (true) {
                isScroll.collectLatest {
                    if (it) {
                        withContext(Dispatchers.Main) { binding.musicFragmentFab.hide() }
                    } else {
                        delay(1000)
                        withContext(Dispatchers.Main) {
                            binding.musicFragmentFab.show()
                        }
                    }
                }
            }
        }
        //控件监听初始化
        binding.musicFragmentRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    isScroll.value = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                isScroll.value = false
            }
        })
        binding.musicControllerPrev.setOnClickListener(this)
        binding.musicControllerNext.setOnClickListener(this)
        binding.musicControllerPause.setOnClickListener(this)
        binding.musicControllerRepeat.setOnClickListener(this)
        binding.musicControllerShuffle.setOnClickListener(this)
        binding.musicFragmentFab.setOnClickListener(this)
        binding.musicFragmentFab.setOnLongClickListener {
            binding.musicFragmentRecyclerView.smoothScrollToPosition(0)
            true
        }
        binding.musicFragmentToolbar.setNavigationOnClickListener {
            binding.musicFragmentSearchView.show()
        }
        binding.musicFragmentToolbar.setOnMenuItemClickListener(this)
        binding.musicControllerSeek.setOnSeekBarChangeListener(this)
        binding.musicControllerAlbumImage.setOnClickListener {
            childFragmentManager.apply {
                beginTransaction().apply {
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    setCustomAnimations(
                        R.anim.anim_fragment_in_bottom, R.anim.anim_fragment_out_top
                    )
                    replace(R.id.musicFragment_main_layout, MusicPlayFragment())
                    addToBackStack("MusicPlayFragment")
                }.commit()
            }
        }
        initSearchViewEvent()
    }


    /**
     * 当前音乐列表Adapter点击事件
     *
     */
    private fun onPlayListAdapterItemClickListener() =
        object : MusicListAdapter.OnItemClickListener {
            override fun onItemClick(musicEntity: MusicEntity, position: Int) {
                player.apply {
                    seekTo(position, 0)
                    prepare()
                    play()
                }
            }

            override fun omRemoveClick(position: Int) {
                player.removeMediaItem(position)
            }
        }

    /**
     * 传入一个排序后的音乐列表,对player音乐列表进行刷新后返回
     *
     * @param newList 排序过的音乐列表
     * @return 排序过的音乐列表
     */
    private fun sortedPlayerList(newList: List<MusicEntity>): List<MusicEntity> {
        player.apply {
            currentMediaItem?.mediaId?.let { mediaId ->
                val currentIndex = newList.indexOf(newList.find {
                    it.uri == mediaId
                })
                val currentWindowIndex = if (currentIndex == -1) currentMediaItemIndex
                else currentIndex
                val currentPosition = currentPosition
                val playWhenReady = playWhenReady
                setMediaItems(newList.map { item ->
                    MediaItem.Builder().setMediaId(item.uri).build()
                }, false)
                prepare()
                seekTo(currentWindowIndex, currentPosition + 200)
                this.playWhenReady = playWhenReady
            }
        }
        return newList
    }

    /**
     * 当音乐播放状态为暂停/播放时的事件
     */
    private fun onPlayingChange() {
        val albumAnim = ObjectAnimator.ofFloat(
            binding.musicControllerAlbumImage, Constants.AnimProperty.Rotation.name, 0f, 360f
        ).apply {
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            duration = 20000
        }
        lifecycleScope.launch(MyCoroutineExceptionHandler.handler) {
            withStarted {
                //播放状态
                launch {
                    viewModel.isPlaying.collectLatest { isPlaying ->
                        when (isPlaying) {
                            true -> {
                                binding.musicControllerTitle.isSelected = true
                                if (albumAnim.isPaused) {
                                    albumAnim.resume()
                                } else {
                                    albumAnim.start()
                                }
                            }

                            false -> {
                                binding.musicControllerTitle.isSelected = false
                                albumAnim.pause()
                            }
                        }
                    }
                }
                //循环模式
                launch {
                    viewModel.repeatMode.collectLatest {
                        when (it) {
                            -1 -> {
                                player.repeatMode = Player.REPEAT_MODE_OFF
                                binding.musicControllerRepeat.setImageResource(R.drawable.ic_repeat)
                            }

                            0 -> {
                                player.repeatMode = Player.REPEAT_MODE_ONE
                                binding.musicControllerRepeat.setImageResource(R.drawable.ic_repeat_one_on)
                            }

                            1 -> {
                                player.repeatMode = Player.REPEAT_MODE_ALL
                                binding.musicControllerRepeat.setImageResource(R.drawable.ic_repeat_on)
                            }
                        }
                    }
                }
                //随机模式
                launch {
                    viewModel.shuffleModel.collectLatest {
                        when (it) {
                            -1 -> {
                                player.shuffleModeEnabled = false
                                binding.musicControllerShuffle.setImageResource(R.drawable.ic_shuffle)
                            }

                            0 -> {
                                player.shuffleModeEnabled = true
                                binding.musicControllerShuffle.setImageResource(R.drawable.ic_shuffle_on)
                            }
                        }
                    }
                }
            }
            viewModel.isPlaying.collectLatest { isPlaying ->
                binding.musicControllerPause.setImg(
                    isPlaying, R.drawable.ic_pause, R.drawable.ic_play
                )
                launch {
                    while (isPlaying) {
                        val currentPosition = player.currentPosition
                        val s = "${Conversion.longConversionToTimeString(currentPosition)}/${
                            Conversion.longConversionToTimeString(viewModel.endTime.value)
                        }"
                        withContext(Dispatchers.Main) {
                            binding.musicControllerTime.text = s
                            binding.musicControllerSeek.setProgress(currentPosition.toInt(), true)
                        }
                        viewModel.setCurrentTime(currentPosition)
                        delay(1000)
                    }
                }
            }
        }
    }

    /**
     * 设置音乐控制区信息
     *
     * @param mediaMetadata
     */
    private fun setControllerInfo(
        mediaMetadata: MediaMetadata,
    ) {
        val title = mediaMetadata.title
        viewModel.apply {
            title?.let {
                setTitle(it)
                binding.musicControllerTitle.text = it
            }
            musicList.find { it.title == title }?.duration?.let {
                setEndTime(it)
                binding.musicControllerSeek.max = it.toInt()
            }
        }

        val reqWidth = binding.musicControllerAlbumImage.width
        val reqHeight = binding.musicControllerAlbumImage.height
        viewModel.musicList.find {
            it.title == title && it.album == mediaMetadata.albumTitle && it.author == mediaMetadata.artist
        }?.let { item ->
            item.path?.let {
                viewModel.setPath(it)
                Glide.with(this).load(Conversion.pathToByteArray(it)).override(reqWidth, reqHeight)
                    .into(binding.musicControllerAlbumImage)
            }
        }
    }

    /**
     * 初始化搜索界面的逻辑
     */
    private fun initSearchViewEvent() {
        binding.musicFragmentSearchView.apply {
            inflateMenu(R.menu.menu_search)
            val searchAdapter = MusicDataAdapter(
                this@MusicFragment, binding.musicFragmentSearchViewRecyclerView, R.layout.item_music
            )
            binding.musicFragmentSearchViewRecyclerView.adapter = searchAdapter
            ((binding.musicFragmentSearchViewRecyclerView.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
                false
            searchAdapter.submit { viewModel.musicList }
            searchAdapter.setOnItemClickListener(object : MusicDataAdapter.OnItemClickListener {
                override fun onItemClick(
                    musicData: MusicEntity, position: Int
                ) {
                    dataAdapter?.mDiffer?.currentList?.indexOf(dataAdapter?.mDiffer?.currentList?.find {
                        it.uri == musicData.uri
                    })?.let {
                        player.apply {
                            seekTo(it, 0)
                            prepare()
                            play()
                        }
                    }
                }
            })
            setOnMenuItemClickListener {
                editText.text.trim().takeIf { it.isNotBlank() }?.let { key ->
                    dataAdapter?.mDiffer?.currentList?.mapNotNull { musicData ->
                        musicData.title?.contains(key, true)?.takeIf { it }?.let { musicData }
                    }?.let { result ->
                        searchAdapter.submit {
                            result
                        }
                    }
                }
                true
            }
            editText.setOnEditorActionListener { v, _, event ->
                if (event != null) {
                    v.text.trim().takeIf { it.isNotBlank() }?.let { key ->
                        dataAdapter?.mDiffer?.currentList?.mapNotNull { musicData ->
                            musicData.title?.contains(key, true)?.takeIf { it }?.let { musicData }
                        }?.let { result ->
                            searchAdapter.submit {
                                result
                            }
                        }
                    }
                    true
                } else false
            }
            val onBackPressedCallback = object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    binding.musicFragmentSearchView.hide()
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner, onBackPressedCallback
            )
            addTransitionListener { _, _, newState ->
                onBackPressedCallback.isEnabled = newState == SearchView.TransitionState.SHOWN
            }
        }
    }

    /**
     * view点击事件
     *
     * @param v
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.musicControllerNext.id -> {
                player.apply {
                    seekToNext()
                    prepare()
                    play()
                }
            }

            binding.musicControllerPrev.id -> {
                player.apply {
                    seekToPrevious()
                    prepare()
                    play()
                }
            }

            binding.musicControllerPause.id -> {
                viewModel.let {
                    if (it.isPlaying.value) {
                        player.pause()
                    } else {
                        player.prepare()
                        player.play()
                    }
                }
            }

            binding.musicControllerShuffle.id -> {
                viewModel.changeShuffleMode()
            }

            binding.musicControllerRepeat.id -> {
                viewModel.changeRepeatMode()
            }

            binding.musicFragmentFab.id -> {
                dataAdapter?.selectedPosition?.takeIf { it != -1 }?.let {
                    binding.musicFragmentRecyclerView.smoothScrollToPosition(it)
                }
            }
        }
    }

    /**
     * 当音乐播放发生错误
     *
     * @param error 错误信息
     */
    override fun onPlayerError(error: PlaybackException) {
        Log.e(TAG, "onPlayerError: ${error.message}")
        Toast.makeText(requireContext(), "音乐播放出现错误:${error.message}", Toast.LENGTH_LONG)
            .show()
    }

    /**
     * 当player当前音乐的元数据发生变化,即音乐改变
     *
     * 更新当前播放列表的正在播放歌曲
     * 根据player的当前item获取主音乐列表的当前位置并更新(当前播放列表可能移除一些item,但主音乐列表不会改变)
     * 更新控制栏信息
     *
     * @param mediaMetadata 元数据
     */
    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        playlistAdapter?.setCurrentSelectItem(player.currentMediaItemIndex)
        if (player.playWhenReady){
            player.currentMediaItem?.mediaId?.let { mediaId ->
                dataAdapter?.mDiffer?.currentList?.find { item ->
                    item.uri == mediaId
                }?.let { musicData ->
                    dataAdapter?.mDiffer?.currentList?.indexOf(musicData)?.takeIf { index ->
                        index != -1
                    }?.let {
                        dataAdapter?.setCurrentSelectItem(it)
                    }
                }
            }
        }
        setControllerInfo(mediaMetadata)
    }

    /**
     * player播放状态改变
     *
     * @param playWhenReady 是否已经准备好播放或是暂停
     * @param reason 事件变化的原因
     */
    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        viewModel.setIsPlaying(playWhenReady)
    }

    /**
     * ToolBar菜单点击事件
     *
     * @param item 点击的item,由程序提供
     * @return 事件是否被消费
     */
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item?.apply {
            when (itemId) {
                R.id.action_music_list -> {
                    player.currentMediaItemIndex.let {
                        playListRecyclerView?.smoothScrollToPosition(it)
                    }
                    bottomSheetDialog?.show()
                }

                R.id.action_sort_check -> {
                    item.setChecked(true)
                    viewModel.sortDescendingMode = false
                }

                R.id.action_sort_check_descending -> {
                    item.setChecked(true)
                    viewModel.sortDescendingMode = true
                }

                R.id.action_sort_by_title -> {
                    dataAdapter?.submit { old ->
                        sortedPlayerList(if (viewModel.sortDescendingMode) old.sortedByDescending { it.title }
                        else old.sortedBy { it.title })
                    }
                    playlistAdapter?.submit { old ->
                        sortedPlayerList(if (viewModel.sortDescendingMode) old.sortedByDescending { it.title }
                        else old.sortedBy { it.title })
                    }
                    item.setChecked(true)
                    return true
                }

                R.id.action_sort_by_author -> {
                    dataAdapter?.submit { old ->
                        sortedPlayerList(if (viewModel.sortDescendingMode) old.sortedByDescending { it.author }
                        else old.sortedBy { it.author })
                    }
                    playlistAdapter?.submit { old ->
                        sortedPlayerList(if (viewModel.sortDescendingMode) old.sortedByDescending { it.author }
                        else old.sortedBy { it.author })
                    }
                    item.setChecked(true)
                    return true
                }

                R.id.action_sort_by_album -> {
                    dataAdapter?.submit { old ->
                        sortedPlayerList(if (viewModel.sortDescendingMode) old.sortedByDescending { it.album }
                        else old.sortedBy { it.album })
                    }
                    playlistAdapter?.submit { old ->
                        sortedPlayerList(if (viewModel.sortDescendingMode) old.sortedByDescending { it.album }
                        else old.sortedBy { it.album })
                    }
                    item.setChecked(true)
                    return true
                }

                R.id.action_sort_by_duration -> {
                    dataAdapter?.submit { old ->
                        sortedPlayerList(if (viewModel.sortDescendingMode) old.sortedByDescending { it.duration }
                        else old.sortedBy { it.duration })
                    }
                    playlistAdapter?.submit { old ->
                        sortedPlayerList(if (viewModel.sortDescendingMode) old.sortedByDescending { it.duration }
                        else old.sortedBy { it.duration })
                    }
                    item.setChecked(true)
                    return true
                }
            }
        }
        return false
    }

    /**
     * 音乐进度条改变
     *
     * @param seekBar
     * @param progress
     * @param fromUser
     */
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) viewModel.setCurrentTime(progress.toLong())
    }

    /**
     * 开始拖动seekBar事件
     */
    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    /**
     * 结束拖动seekBar事件
     *
     * 如果音乐正在播放则设置player的当前进度并播放,否则将进度置只修改进度
     */
    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        player.apply {
            when (isPlaying) {
                true -> seekTo(viewModel.currentTime.value)

                false -> seekTo(viewModel.currentTime.value)
            }
        }
    }

    /**
     * 主音乐列表的item点击事件
     *
     * @param musicData 点击item的数据
     * @param position 点击item所在位置
     */
    override fun onItemClick(musicData: MusicEntity, position: Int) {
        player.apply {
            seekTo(position, 0)
            prepare()
            play()
        }
    }

}