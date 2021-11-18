package by.brust.musicplayer.ui

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import by.brust.musicplayer.mediaplayer.MediaPlayerService

class MediaController(context: Context) {
    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected = _isConnected

    private val _playbackStateCompat = MutableLiveData<PlaybackStateCompat>()
    val playbackStateCompat = _playbackStateCompat

    private var _currentPlayingTrack = MutableLiveData<MediaMetadataCompat>()
    val currentPlayingTrack = _currentPlayingTrack

    lateinit var mediaController: MediaControllerCompat

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context, ComponentName(
            context,
            MediaPlayerService::class.java
        ), mediaBrowserConnectionCallback, null
    )
        .apply { connect() }

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubsribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }


    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isConnected.postValue(true)
        }

        override fun onConnectionSuspended() {
            _isConnected.postValue(false)
        }

        override fun onConnectionFailed() {
            _isConnected.postValue(false)
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            _playbackStateCompat.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            _currentPlayingTrack.postValue(metadata)
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}
