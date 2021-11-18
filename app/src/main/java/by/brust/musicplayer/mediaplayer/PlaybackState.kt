package by.brust.musicplayer.mediaplayer

import android.support.v4.media.session.PlaybackStateCompat

inline val PlaybackStateCompat.isPrepared
get() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
        (state == PlaybackStateCompat.STATE_PLAYING) ||
        (state == PlaybackStateCompat.STATE_PAUSED)

inline val PlaybackStateCompat.isPlaying
get() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
        (state == PlaybackStateCompat.STATE_PLAYING)
inline val PlaybackStateCompat.isPaused
get() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
        (state == PlaybackStateCompat.STATE_PAUSED)