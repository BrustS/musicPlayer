package by.brust.musicplayer.repository

import by.brust.musicplayer.model.Track

interface LocalAudioData {
    suspend fun getAllSongs(): List<Track>
}