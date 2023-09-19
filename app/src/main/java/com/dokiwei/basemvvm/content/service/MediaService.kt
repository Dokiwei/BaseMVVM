package com.dokiwei.basemvvm.content.service

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession.Builder
import androidx.media3.session.MediaLibraryService.MediaLibrarySession.Callback
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import com.dokiwei.basemvvm.R
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

@UnstableApi
/**
 * @author DokiWei
 * @date 2023/9/18 12:56
 */
class MediaService : MediaLibraryService() {
    private var session: MediaLibrarySession?=null
    private lateinit var player: Player
    private lateinit var playerNotificationManager: PlayerNotificationManager



    companion object {
        private const val PLAYBACK_NOTIFICATION_ID = 1
        private const val PLAYBACK_CHANNEL_ID = "playback_channel"
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


        initNotification()

    }

    private fun initNotification() {
        playerNotificationManager = PlayerNotificationManager.Builder(
            applicationContext,
            PLAYBACK_NOTIFICATION_ID, PLAYBACK_CHANNEL_ID
        ).build()
        playerNotificationManager.setPlayer(player)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
            session = null
            playerNotificationManager.setPlayer(null)
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return session
    }
}