package com.dokiwei.basemvvm.content.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
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
import androidx.media3.ui.PlayerNotificationManager
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.util.Conversion
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

@UnstableApi
/**
 * @author DokiWei
 * @date 2023/9/18 12:56
 */
class MediaService : MediaLibraryService() {
    private lateinit var session: MediaLibrarySession
    private lateinit var player: Player
    companion object {
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

        setMediaNotificationProvider(object : MediaNotification.Provider {
            override fun createNotification(
                mediaSession: MediaSession,
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {
                val pauseActionIntent = actionFactory.createMediaAction(
                    mediaSession,
                    IconCompat.createWithResource(this@MediaService, R.drawable.pause_icon),
                    "暂停",
                    COMMAND_PLAY_PAUSE
                ).actionIntent
                val nextActionIntent = actionFactory.createMediaAction(
                    mediaSession,
                    IconCompat.createWithResource(this@MediaService, R.drawable.skip_next_icon),
                    "暂停",
                    COMMAND_SEEK_TO_NEXT
                ).actionIntent
                val prevActionIntent = actionFactory.createMediaAction(
                    mediaSession,
                    IconCompat.createWithResource(this@MediaService, R.drawable.skip_previous_icon),
                    "暂停",
                    COMMAND_SEEK_TO_PREVIOUS
                ).actionIntent
                val notification = initNotification(
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

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        super.onUpdateNotification(session, true)
    }

    private fun initNotification(
        mediaSession: MediaSession,
        pauseActionIntent: PendingIntent?,
        nextActionIntent: PendingIntent?,
        prevActionIntent: PendingIntent?
    ): NotificationCompat.Builder {
        val channel = NotificationChannel(PLAYBACK_CHANNEL_ID,PLAYBACK_CHANNEL_ID,NotificationManager.IMPORTANCE_LOW).apply {
            description = "音乐通知渠道,禁止后无法后台播放音乐"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        return NotificationCompat.Builder(this, PLAYBACK_CHANNEL_ID).apply {
            // 为当前播放的曲目添加元数据
            setContentTitle(mediaSession.player.mediaMetadata.title)
            setContentText(mediaSession.player.mediaMetadata.subtitle)
            setSubText((mediaSession.player.mediaMetadata.description))
            mediaSession.player.mediaMetadata.artworkData?.let {
                setLargeIcon(Conversion.byteArrayToBitmap(it, 200))
            }
            addAction(R.drawable.skip_previous_icon, "上一首", prevActionIntent)
            addAction(
                if (player.isPlaying) R.drawable.pause_icon else R.drawable.play_icon,
                "暂停",
                pauseActionIntent
            )
            addAction(R.drawable.skip_next_icon, "下一首", nextActionIntent)
            setContentIntent(session.sessionActivity)
            setSmallIcon(
                R.drawable.music_icon
            )
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setOngoing(true)
            setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            setStyle(MediaStyle(session))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        player.release()
        session.release()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return session
    }
}