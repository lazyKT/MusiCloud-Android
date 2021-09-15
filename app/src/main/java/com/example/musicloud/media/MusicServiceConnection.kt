package com.example.musicloud.media

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

private const val NETWORK_ERROR = "NETWORK_ERROR"

class MusicServiceConnection (
    context: Context
) {
    private val _isConnected = MutableLiveData <Event<Resource<Boolean>>> ()
    val isConnected: LiveData <Event<Resource<Boolean>>> get() = _isConnected

    private val _networkError = MutableLiveData <Event<Resource<Boolean>>> ()
    val networkError: LiveData <Event<Resource<Boolean>>> get() = _networkError

    private val _playbackState = MutableLiveData <PlaybackStateCompat?> ()
    val playbackState: LiveData <PlaybackStateCompat?> get() = _playbackState

    private val _currentPlayingSong = MutableLiveData <MediaMetadataCompat?> ()
    val currentPlayingSong: LiveData <MediaMetadataCompat?> get() = _currentPlayingSong

    lateinit var mediaController: MediaControllerCompat

    private val mediaBrowserConnection = MediaBrowserConnectionCallback (context)

    private val mediaBrowserCompat = MediaBrowserCompat (
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBrowserConnection,
        null
    ).apply { connect() }

    val transportControl: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    fun subscribe (parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowserCompat.subscribe (parentId, callback)
    }

    fun unsubscribe (parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowserCompat.unsubscribe (parentId)
    }


    private inner class MediaBrowserConnectionCallback (
        private val context: Context
    ): MediaBrowserCompat.ConnectionCallback () {

        override fun onConnected() {
            super.onConnected()
            mediaController = MediaControllerCompat (context, mediaBrowserCompat.sessionToken).apply {
                registerCallback (MediaControllerCallback())
            }
            _isConnected.postValue (Event (Resource.success (true)))
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            _isConnected.postValue (Event (Resource.error (
                "The data is suspended",
                false
            )))
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            _isConnected.postValue (Event (Resource.error(
                "Couldn't Connect to Media Browser",
                null
            )))
        }
    }

    private inner class MediaControllerCallback
        : MediaControllerCompat.Callback () {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue (state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            _currentPlayingSong.postValue (metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when (event) {
                NETWORK_ERROR -> _networkError.postValue (
                    Event (
                        Resource.error(
                            "Couldn't Connect to the Local Data Source. Please contact to the admin.",
                            null
                        )
                    )
                )
            }
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            mediaBrowserConnection.onConnectionSuspended()
        }
    }
}