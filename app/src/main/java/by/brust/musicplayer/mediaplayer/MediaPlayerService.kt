package by.brust.musicplayer.mediaplayer

import android.app.PendingIntent
import android.graphics.ColorSpace
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.media.MediaBrowserServiceCompat
import by.brust.musicplayer.notification.NotificationManager
import by.brust.musicplayer.repository.AudioRepository
import by.brust.musicplayer.utils.Constants
import by.brust.musicplayer.utils.Constants.ROOT_ID
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MediaPlayerService : MediaBrowserServiceCompat(){

    @Inject
    lateinit var repository: AudioRepository

    @Inject
    lateinit var exoPlayer: ExoPlayer

    var isForegroundService = false
    private var isPlayerInitialized = false

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var musicNotificationManager: NotificationManager

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var currentTrack: MediaMetadataCompat? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            repository.loadSongs()
        }
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this,0,it,PendingIntent.FLAG_IMMUTABLE)
        }
        mediaSession = MediaSessionCompat(this,Constants.SERVICE_TAG).apply{
            setSessionActivity(activityIntent)
            isActive = true
            setPlaybackState(PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_STOP
                    or PlaybackStateCompat.ACTION_PAUSE
                    or PlaybackStateCompat.ACTION_PLAY_PAUSE
                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .build())
        }
        sessionToken = mediaSession.sessionToken
        sessionToken?.let{musicNotificationManager = NotificationManager(this,it,this) }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(PlayerPlaybackPreparer())
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        exoPlayer.addListener(PlayerEventsListener())
        musicNotificationManager.showNotification(exoPlayer)
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
       return BrowserRoot(ROOT_ID,null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
       when(parentId) {
           ROOT_ID -> {
               if (repository.audioMetadata.isNotEmpty()) {
                   result.sendResult(repository.getMediaMetadata())
                   if (!isPlayerInitialized) {
                       preparePlayer(repository.audioMetadata, repository.audioMetadata[0], false)
                       isPlayerInitialized = true
                   }
               }
           }
       }
    }

    private fun preparePlayer(tracks: List<MediaMetadataCompat>, trackToPlay: MediaMetadataCompat,
        playNow: Boolean) {
        val trackIndex = if(currentTrack == null) 0 else tracks.indexOf(trackToPlay)
        exoPlayer.setMediaItems(repository.getAudio())
        exoPlayer.prepare()
        exoPlayer.seekTo(trackIndex,0L)
        exoPlayer.playWhenReady = playNow
    }

    private inner class MusicQueueNavigator: TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return repository.audioMetadata[windowIndex].description
        }
    }

    private inner class PlayerPlaybackPreparer: MediaSessionConnector.PlaybackPreparer {
        override fun onCommand(
            player: Player,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ): Boolean {
            return false
        }

        override fun getSupportedPrepareActions(): Long {
            return PlaybackStateCompat.ACTION_PREPARE_FROM_URI or
                    PlaybackStateCompat.ACTION_PLAY_FROM_URI
        }

        override fun onPrepare(playWhenReady: Boolean) = Unit

        override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) = Unit

        override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit

        override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
            val trackToPlay = repository.audioMetadata.find { uri == it.description.mediaUri }
            if (trackToPlay != null) {
                currentTrack = trackToPlay
                preparePlayer(repository.audioMetadata, trackToPlay, true)
            }
        }
    }
    private inner class PlayerEventsListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (!isPlaying) {
                stopForeground(false)
            }
        }
    }
}