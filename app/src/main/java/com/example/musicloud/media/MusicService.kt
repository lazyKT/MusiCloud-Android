package com.example.musicloud.media

import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.musicloud.media.callbacks.MusicPlaybackPreparer
import com.example.musicloud.media.callbacks.MusicPlayerEventListener
import com.example.musicloud.media.callbacks.MusicPlayerNotificationListener
import com.example.musicloud.song.SongDataSource
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import javax.inject.Inject


private const val SERVICE_TAG = "MUSIC_SERVICE"
private const val MY_MEDIA_ROOT_ID = "MY_MEDIA_ROOT_ID"

@AndroidEntryPoint
class MusicService: MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var songDataSource: SongDataSource

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope (Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var musicNotificationManager:MusicNotificationManager

    var isForeGroundService = false

    private var currentPlayingSong: MediaMetadataCompat? = null

    override fun onCreate() {
        super.onCreate()

        /* trigger when we start actions from notifications */
        val activityIntent = packageManager?.getLaunchIntentForPackage (packageName)?.let {
            PendingIntent.getActivity (this, 0, it, 0)
        }

        mediaSession = MediaSessionCompat (this, SERVICE_TAG).apply {
            setSessionActivity (activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        musicNotificationManager = MusicNotificationManager (this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {

        }

        val musicPlaybackPreparer = MusicPlaybackPreparer (songDataSource = songDataSource) {
            currentPlayingSong = it
            preparePlayer (songDataSource.formattedSongs, it, true)
        }

        mediaSessionConnector = MediaSessionConnector (mediaSession)
        mediaSessionConnector.setPlaybackPreparer (musicPlaybackPreparer)
        mediaSessionConnector.setPlayer (exoPlayer)

        exoPlayer.addListener (MusicPlayerEventListener (this))
        musicNotificationManager.showNotification (exoPlayer)
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

    private fun preparePlayer (
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val currentSongIdx = if (currentPlayingSong == null) 0 else songs.indexOf (itemToPlay)
        exoPlayer.setMediaSource (songDataSource.asMediaSource (dataSourceFactory))
        exoPlayer.prepare ()
        exoPlayer.seekTo (currentSongIdx, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onDestroy() {
        super.onDestroy()
        /* cancel all coroutine jobs when the service gets destroyed */
        serviceScope.cancel()
    }
}