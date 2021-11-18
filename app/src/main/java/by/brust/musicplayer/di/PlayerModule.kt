package by.brust.musicplayer.di

import android.app.Application
import android.content.Context
import by.brust.musicplayer.repository.AudioRepository
import by.brust.musicplayer.repository.LocalAudioDataImpl
import by.brust.musicplayer.ui.MediaController
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PlayerModule {

    @Provides
    @Singleton
    fun provideRepository(context:Application) = AudioRepository(LocalAudioDataImpl(context))


    @Provides
    @Singleton
    fun provideExoplayer(context: Application) : ExoPlayer {
        val attr = AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        return ExoPlayer.Builder(context.baseContext).build().apply {
            setAudioAttributes(attr,true)
            setHandleAudioBecomingNoisy(true)
            playWhenReady = true
        }
    }

    @Provides
    @Singleton
    fun provideMediaController(
        @ApplicationContext
        context: Context) = MediaController(context)
}