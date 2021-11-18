package by.brust.musicplayer.repository

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri
import by.brust.musicplayer.model.Track
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.MimeTypes
import javax.inject.Inject

class AudioRepository @Inject constructor(private val audioData: LocalAudioData) {

    val audioMetadata: List<MediaMetadataCompat> by lazy { getMetaData() }

    private var songs = emptyList<Track>()

    suspend fun loadSongs() {
         songs = audioData.getAllSongs()
    }

     fun getAudio(): List<MediaItem> {
        return songs.map { track ->
            MediaItem.Builder()
                .setUri(track.songUri)
                .setMimeType(MimeTypes.BASE_TYPE_AUDIO)
                .build()
        }
    }

    private fun getMetaData(): List<MediaMetadataCompat> {
        return songs.map { track ->
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,track.id)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,track.title)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,track.title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,track.author)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,track.author)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,track.imageUri)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,track.songUri)
                .build()
        }
    }
    fun getMediaMetadata() = songs.map { track ->
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(track.id)
            .setTitle(track.title)
            .setSubtitle(track.author)
            .setIconUri(track.imageUri.toUri())
            .setMediaUri(track.songUri.toUri())
            .build()
        MediaBrowserCompat.MediaItem(description,MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }.toMutableList()

}