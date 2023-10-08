package com.dokiwei.basemvvm.content.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_PLAY_PAUSE
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession.Builder
import androidx.media3.session.MediaLibraryService.MediaLibrarySession.Callback
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper.MediaStyle
import cn.lyric.getter.api.LyricListener
import cn.lyric.getter.api.data.LyricData
import cn.lyric.getter.api.tools.EventTools
import cn.lyric.getter.api.tools.Tools.registerLyricListener
import cn.lyric.getter.api.tools.Tools.unregisterLyricListener
import com.dirror.lyricviewx.LyricEntry
import com.dokiwei.basemvvm.R
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.mpatric.mp3agic.Mp3File
import java.util.regex.Pattern

@UnstableApi
/**
 * @author DokiWei
 * @date 2023/9/18 12:56
 */
class MediaService : MediaLibraryService(), Player.Listener {
    private lateinit var session: MediaLibrarySession
    private lateinit var player: Player

    companion object {
        private val PATTERN_LINE = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d{2,3}])+)(.+)")
        private val PATTERN_TIME = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})]")
        private const val PLAYBACK_NOTIFICATION_ID = 1
        private const val PLAYBACK_CHANNEL_ID = "音乐通知"
    }


    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(applicationContext).apply {
            //自动处理音频焦点
            setAudioAttributes(AudioAttributes.DEFAULT, true)
            //自动暂停播放
            setHandleAudioBecomingNoisy(true)
            setRenderersFactory(
                DefaultRenderersFactory(this@MediaService).setExtensionRendererMode(
                    DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                )
            )
        }.build()

        player.addListener(this)

        session = Builder(this, player, object : Callback {
            override fun onAddMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: MutableList<MediaItem>
            ): ListenableFuture<MutableList<MediaItem>> {
                val updatedMediaItems =
                    mediaItems.map { it.buildUpon().setUri(it.mediaId).build() }.toMutableList()
                return Futures.immediateFuture(updatedMediaItems)
            }
        }).build()
        registerLyricListener(applicationContext, EventTools.API_VERSION, object : LyricListener() {
            override fun onUpdate(lyricData: LyricData) {
            }

            override fun onStop(lyricData: LyricData) {
            }
        })
        initNotification()
    }

    private val handler = Handler(Looper.getMainLooper())
    private var path: String? = null
    private var isPlaying = false
    private var lyricsList: List<LyricEntry>? = null
    private val mediaMetadataChange = Runnable {
        when (isPlaying) {
            true -> {
                path?.let { p ->
                    val lyrics = Mp3File(p).takeIf { it.hasId3v2Tag() }?.id3v2Tag?.lyrics
                    this@MediaService.lyricsList = lyrics?.let { parseLrc(it) }
                    handler.removeCallbacks(updateLyrics)
                    handler.post(updateLyrics)
                }
            }

            false -> {
                handler.removeCallbacks(updateLyrics)
                EventTools.stopLyric(applicationContext)
            }
        }
    }
    private val updateLyrics = object : Runnable {
        override fun run() {
            lyricsList?.let { list ->
                list.filter { it.time >= player.currentPosition }
                    .minByOrNull { it.time - player.currentPosition }?.let {
                        lyricsList?.indexOf(it)
                    }?.let { index ->
                        if (index + 1 < list.size) {
                            EventTools.sendLyric(
                                applicationContext,
                                list[index].text,
                                application.packageName,
                                (list[index + 1].time - list[index].time).toInt()
                            )
                            handler.postDelayed(this, list[index + 1].time - list[index].time)
                        } else {
                            EventTools.sendLyric(
                                applicationContext, list[index].text, application.packageName
                            )
                            handler.postDelayed(this, 1000)
                        }
                    }
            }
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        handler.post {
            isPlaying = playWhenReady
            handler.removeCallbacks(mediaMetadataChange)
            handler.post(mediaMetadataChange)
        }
    }


    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        handler.post {
            player.mediaMetadata.description?.let {
                path = it.toString()
            }
            handler.removeCallbacks(mediaMetadataChange)
            handler.post(mediaMetadataChange)
        }
    }

    /**
     * 通过[MediaNotification.Provider]来创建一个[MediaNotification]
     */
    private fun initNotification() {
        setMediaNotificationProvider(object : MediaNotification.Provider {
            override fun createNotification(
                mediaSession: MediaSession,
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {
                val pauseActionIntent = actionFactory.createMediaAction(
                    mediaSession,
                    IconCompat.createWithResource(this@MediaService, R.drawable.ic_pause),
                    "暂停",
                    COMMAND_PLAY_PAUSE
                ).actionIntent
                val nextActionIntent = actionFactory.createMediaAction(
                    mediaSession,
                    IconCompat.createWithResource(this@MediaService, R.drawable.ic_next),
                    "暂停",
                    COMMAND_SEEK_TO_NEXT
                ).actionIntent
                val prevActionIntent = actionFactory.createMediaAction(
                    mediaSession,
                    IconCompat.createWithResource(this@MediaService, R.drawable.ic_prev),
                    "暂停",
                    COMMAND_SEEK_TO_PREVIOUS
                ).actionIntent
                val notification = initMediaNotification(
                    mediaSession, pauseActionIntent, nextActionIntent, prevActionIntent
                )
                val mediaNotification = MediaNotification(
                    PLAYBACK_NOTIFICATION_ID, notification.build()
                ).apply {
                    setListener(object : Listener {
                        @RequiresApi(Build.VERSION_CODES.S)
                        override fun onForegroundServiceStartNotAllowedException() {
                            super.onForegroundServiceStartNotAllowedException()
                        }
                    })
                }
                onNotificationChangedCallback.onNotificationChanged(mediaNotification)
                return mediaNotification
            }

            override fun handleCustomCommand(
                session: MediaSession, action: String, extras: Bundle
            ): Boolean {
                if (action == "COMMAND_PLAY_PAUSE") player.pause()
                return false
            }

        })
    }

    /**
     * 初始化音乐通知
     *
     * @param mediaSession 媒体元数据
     * @param pauseActionIntent 通过[MediaNotification.ActionFactory]创建暂停播放事件
     * @param nextActionIntent 通过[MediaNotification.ActionFactory]创建切换下一曲事件
     * @param prevActionIntent 通过[MediaNotification.ActionFactory]创建切换上一曲事件
     * @return 返回已初始化的音乐通知
     */
    private fun initMediaNotification(
        mediaSession: MediaSession,
        pauseActionIntent: PendingIntent?,
        nextActionIntent: PendingIntent?,
        prevActionIntent: PendingIntent?
    ): NotificationCompat.Builder {
        val channel = NotificationChannel(
            PLAYBACK_CHANNEL_ID, PLAYBACK_CHANNEL_ID, NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "音乐通知渠道,禁止后无法后台播放音乐"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        return NotificationCompat.Builder(this, PLAYBACK_CHANNEL_ID).apply {
            // 为当前播放的曲目添加元数据
            setContentTitle(mediaSession.player.mediaMetadata.title)
            setContentText(mediaSession.player.mediaMetadata.subtitle)
            setSubText((mediaSession.player.mediaMetadata.description))
            addAction(R.drawable.ic_prev, "上一首", prevActionIntent)
            addAction(
                if (player.isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                "暂停",
                pauseActionIntent
            )
            addAction(R.drawable.ic_next, "下一首", nextActionIntent)
            setContentIntent(session.sessionActivity)
            setSmallIcon(
                R.drawable.ic_music
            )
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setOngoing(true)
            setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            setStyle(MediaStyle(session))
        }
    }


    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        super.onUpdateNotification(session, true)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun parseLrc(lrcText: String): List<LyricEntry>? {
        var lyricText = lrcText
        if (TextUtils.isEmpty(lyricText)) {
            return null
        }
        if (lyricText.startsWith("\uFEFF")) {
            lyricText = lyricText.replace("\uFEFF", "")
        }
        val entryList: MutableList<LyricEntry> = ArrayList()
        val array = lyricText.split("\\n".toRegex()).toTypedArray()
        for (line in array) {
            val list = parseLine(line)
            if (!list.isNullOrEmpty()) {
                entryList.addAll(list)
            }
        }
        entryList.sort()
        return entryList
    }

    private fun parseLine(line: String): List<LyricEntry>? {
        var lyricLine = line
        if (TextUtils.isEmpty(lyricLine)) {
            return null
        }
        lyricLine = lyricLine.trim { it <= ' ' }
        // [00:17.65]让我掉下眼泪的
        val lineMatcher = PATTERN_LINE.matcher(lyricLine)
        if (!lineMatcher.matches()) {
            return null
        }
        val times = lineMatcher.group(1)
        val text = lineMatcher.group(3)
        val entryList: MutableList<LyricEntry> = ArrayList()

        // [00:17.65]
        val timeMatcher = PATTERN_TIME.matcher(times)
        while (timeMatcher.find()) {
            val min = timeMatcher.group(1).toLong()
            val sec = timeMatcher.group(2).toLong()
            val milString = timeMatcher.group(3)
            var mil = milString.toLong()
            // 如果毫秒是两位数，需要乘以 10，when 新增支持 1 - 6 位毫秒，很多获取的歌词存在不同的毫秒位数
            when (milString.length) {
                1 -> mil *= 100
                2 -> mil *= 10
                4 -> mil /= 10
                5 -> mil /= 100
                6 -> mil /= 1000
            }
            val time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil
            entryList.add(LyricEntry(time, text))
        }
        return entryList
    }

    override fun onDestroy() {
        handler.removeCallbacks(mediaMetadataChange)
        handler.removeCallbacks(updateLyrics)
        EventTools.stopLyric(applicationContext)
        unregisterLyricListener(applicationContext)
        player.removeListener(this)
        player.release()
        session.release()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return session
    }


}