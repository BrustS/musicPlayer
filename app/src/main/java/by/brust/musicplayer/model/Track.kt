package by.brust.musicplayer.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*


@JsonClass(generateAdapter = true)
data class Track (
    val id: String = UUID.randomUUID().toString(),
    @Json(name = "title")
    val title: String = "",
    @Json(name = "artist")
    val author: String = "",
    @Json(name = "bitmapUri")
    val imageUri: String ="",
    @Json(name = "trackUri")
    val songUri: String = "",
    @Json(name = "duration")
    val duration: Long = 0L
)