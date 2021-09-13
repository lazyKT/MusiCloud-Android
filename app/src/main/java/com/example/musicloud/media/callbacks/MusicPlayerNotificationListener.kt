package com.example.musicloud.media.callbacks

import android.app.Notification
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.musicloud.media.MusicService
import com.example.musicloud.media.NOTIFICATION_ID
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicPlayerNotificationListener (
    private val musicService: MusicService
        ): PlayerNotificationManager.NotificationListener {

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
        musicService.apply {
            stopForeground (true)
            isForeGroundService = false
            stopSelf()
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        Log.i ("MusicPlayerNotificationListener", "onNotificationPosted $notificationId, $notification")
        super.onNotificationPosted(notificationId, notification, ongoing)
        musicService.apply {
            if (ongoing && !isForeGroundService) {
                ContextCompat.startForegroundService (
                    this,
                    Intent (applicationContext, this::class.java)
                )
                startForeground (NOTIFICATION_ID, notification)
                isForeGroundService = true
            }
        }
    }
}