package by.brust.musicplayer.ui

import android.media.browse.MediaBrowser
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import by.brust.musicplayer.mediaplayer.isPaused
import by.brust.musicplayer.mediaplayer.isPlaying
import by.brust.musicplayer.mediaplayer.isPrepared
import by.brust.musicplayer.model.Track
import by.brust.musicplayer.repository.AudioRepository
import by.brust.musicplayer.utils.Constants.ROOT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val mediaController: MediaController,
    val repository: AudioRepository
): ViewModel() {
    var trackList: MutableLiveData<List<Track>> = MutableLiveData()
    val isConnected = mediaController.isConnected
    val playbackStateCompat = mediaController.playbackStateCompat
    val currentPlayingTrack = mediaController.currentPlayingTrack

    init {
        mediaController.subscribe(ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)
                    val items = children.map {
                      Track(
                          id = it.description.mediaId.toString(),
                          title = it.description.title.toString(),
                          author = it.description.subtitle.toString(),
                          imageUri = it.description.iconUri.toString(),
                          songUri = it.description.mediaUri.toString()
                      )
                    }
                    trackList.value = items
                }
            })
    }
    fun skipToNextTrack() {
        mediaController.transportControls.skipToNext()
    }
    fun skipToPreviousTrack() {
        mediaController.transportControls.skipToPrevious()
    }
    fun rewind() {
        mediaController.transportControls.rewind()
    }

    fun playOrToogleTrack(track: Track, toggle: Boolean = false) {
        if (playbackStateCompat.value?.isPrepared == true &&
            track.id == currentPlayingTrack.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) {
            playbackStateCompat.value?.let {
                when {
                    it.isPlaying ->
                        if(toggle) mediaController.transportControls.pause()
                    it.isPaused -> mediaController.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            mediaController.transportControls.playFromUri(Uri.parse(track.songUri),null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaController.unsubsribe(ROOT_ID, object: MediaBrowserCompat.SubscriptionCallback(){})
    }


}