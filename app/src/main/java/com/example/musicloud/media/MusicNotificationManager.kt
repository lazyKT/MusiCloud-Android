package com.example.musicloud.media

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.musicloud.R
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager


const val NOTIFICATION_ID = 10231
const val CHANNEL_ID = "MUSICLOUD"


class MusicNotificationManager (
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newSongCallback: () -> Unit
        ) {

    private var playerNotificationManager: PlayerNotificationManager? = null

    init {
        val mediaControllerCompat = MediaControllerCompat (context, sessionToken)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* for version ANDROID.O and later we need to create notification channel before building a notification */
            val name: CharSequence = CHANNEL_ID
            val importance: Int = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel (CHANNEL_ID, name, importance)
            val notificationManager = context.getSystemService (NotificationManager::class.java)
            notificationManager.createNotificationChannel (notificationChannel)
        }

        playerNotificationManager = PlayerNotificationManager.Builder (
            context,
            NOTIFICATION_ID,
            CHANNEL_ID
                )
            .setNotificationListener (notificationListener)
            .setMediaDescriptionAdapter (DescriptionAdapter(mediaControllerCompat))
            .build()
            .apply {
                setSmallIcon (R.drawable.ic_logo)
                setMediaSessionToken (sessionToken)
            }
    }

    fun showNotification (player: Player) {
        playerNotificationManager?.setPlayer (player)
    }

    private inner class DescriptionAdapter (
        private val mediaControllerCompat: MediaControllerCompat
            ): PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: Player): CharSequence {
            newSongCallback ()
            return mediaControllerCompat.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaControllerCompat.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence {
            return mediaControllerCompat.metadata.description.subtitle.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            Glide.with (context).asBitmap()
                .load (mediaControllerCompat.metadata.description.iconUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        callback.onBitmap (resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit

                })
            return null
        }
    }

}