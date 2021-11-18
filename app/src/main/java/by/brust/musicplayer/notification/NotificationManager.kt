package by.brust.musicplayer.notification


import android.app.Notification
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import by.brust.musicplayer.R
import by.brust.musicplayer.mediaplayer.MediaPlayerService
import by.brust.musicplayer.utils.Constants
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class NotificationManager( private val context : Context,sessionToken: MediaSessionCompat.Token,
musicService: MediaPlayerService) {
    private val notificationManager = PlayerNotificationManager.Builder(context, Constants.NOTIFICATION_ID,
        Constants.NOTIFICATION_CHANNEL_ID)

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)
        notificationManager.setChannelNameResourceId(R.string.notification_channel_name)
            .setChannelDescriptionResourceId(R.string.notification_description)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.setChannelImportance(IMPORTANCE_DEFAULT)
        }

        notificationManager.setMediaDescriptionAdapter(
            object: PlayerNotificationManager.MediaDescriptionAdapter {
                private var icon: Pair<String, Bitmap>? = null

                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return mediaController.metadata.description.title.toString()
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    return mediaController.sessionActivity
                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return mediaController.metadata.description.subtitle.toString()
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    if (icon?.first == mediaController.metadata.description.iconUri.toString()) {
                        return icon?.second
                    }
                    Glide.with(context).asBitmap()
                        .load(mediaController.metadata.description.iconUri)
                        .into(object: CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                callback.onBitmap(resource)
                                icon = mediaController.metadata.description.iconUri.toString() to resource
                            }

                            override fun onLoadCleared(placeholder: Drawable?) = Unit
                        })
                    return null
                }
            }
        )

        notificationManager.setNotificationListener(object: PlayerNotificationManager.NotificationListener {
            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                super.onNotificationPosted(notificationId, notification, ongoing)
                if(ongoing)
                    musicService.apply {
                        startForeground(notificationId,notification)
                        isForegroundService = true
                    }
                else
                    musicService.apply {
                        stopForeground(false)
                        isForegroundService = false
                    }
            }

            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                super.onNotificationCancelled(notificationId, dismissedByUser)
                musicService.apply {
                    stopForeground(true)
                    isForegroundService = false
                    stopSelf()
                }
            }
        })
    }
    fun showNotification(player: Player) {
        notificationManager.build().setPlayer(player)
    }
}