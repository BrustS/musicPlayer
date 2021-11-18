package by.brust.musicplayer.repository

import android.content.Context
import by.brust.musicplayer.model.Track
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class LocalAudioDataImpl(private val context: Context): LocalAudioData {
    override suspend fun getAllSongs(): List<Track> {
        val json = context.assets.open("playlist.json").bufferedReader().use {
            it.readText()
        }
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, Track::class.java)
        val jsonAdapter: JsonAdapter<List<Track>> = moshi.adapter(type)
        return jsonAdapter.fromJson(json)!!
    }
}