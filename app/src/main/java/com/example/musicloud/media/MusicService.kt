package com.example.musicloud.media

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat


private const val LOG_TAG = "MUSIC_SERVICE"
private const val MY_MEDIA_ROOT_ID = "MY_MEDIA_ROOT_ID"

class MusicService: MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat (baseContext, LOG_TAG).apply {

            setFlags (MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)

            /* set initial playback state */
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions (
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState (stateBuilder.build())

            /* set session token so that the media client can communicate */
            setSessionToken (sessionToken)
        }
    }

    /*
    * onGetRoot() return root node of the content hierarchy.
    * If the method returns null, the connection is refused.
    * To allow the clients to connect to the media browser service, onGetRoot() must return non-null.
    * */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        /*
        * Normally, we would decide to/not to allow specific clients via ACLs.
        * However, in this case, I would allow all clients for the simplicity
        *  */
        return BrowserRoot (MY_MEDIA_ROOT_ID, null)
    }

    /*
    * onLoadChildren() provides MediaBrowserService's content hierarchy the connected clients.
    * */
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        
    }

}