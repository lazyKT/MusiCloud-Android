package com.example.musicloud.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicloud.database.Song
import com.example.musicloud.media.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


private const val SERVICE_TAG = "MUSIC_SERVICE"
private const val MY_MEDIA_ROOT_ID = "MY_MEDIA_ROOT_ID"
private const val NETWORK_ERROR = "NETWORK_ERROR"
private const val PLAY_SONG = 0
private const val OPEN_SONG_DETAILS = 1

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
): ViewModel() {
    private val _mediaItems = MutableLiveData <Resource<List<Song>>> ()
    val mediaItems: LiveData <Resource<List<Song>>> get() = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currentPlayingSong = musicServiceConnection.currentPlayingSong
    val playbackState = musicServiceConnection.playbackState


    init {
        _mediaItems.postValue (Resource.loading(null))

        /* load song data and subscribe to exoplayer */
        musicServiceConnection.subscribe (MY_MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback () {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    val mediaID = it.mediaId ?: ""
                    Song (
                        songID = mediaID,
                        songName = it.description.title.toString(),
                        localFileURL = it.description.mediaUri.toString(),
                        thumbnailM = it.description.iconUri.toString(),
                        thumbnailS = it.description.iconUri.toString(),
                        processing = false,
                        finished = true
                    )
                }
                _mediaItems.postValue (Resource.success (items))
            }
        })
    }

    fun skipToNextSong () {
        musicServiceConnection.transportControl.skipToNext()
    }

    fun skipToPreviousSong () {
        musicServiceConnection.transportControl.skipToPrevious()
    }

    fun seekTo (position: Long) {
        musicServiceConnection.transportControl.seekTo (position)
    }

//    fun onClickSongMediaItem (actionType: SongClickActionType) {
//        when (actionType.actionType) {
//            PLAY_SONG -> playOrPauseSong (actionType.song)
//            OPEN_SONG_DETAILS -> Unit
//            else -> Unit
//        }
//    }

    fun togglePlayPause () {
        currentPlayingSong.value?.let {
            val song = mediaDataCompatToSong (it)
            if (song != null) {
                playOrPauseSong (song, true)
            }
        }
    }

    fun playOrPauseSong (mediaItem: Song, toggle: Boolean = false) {
        Log.i ("HomeViewModel", "playOrPauseSong ${mediaItem.songName}")
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared &&
            mediaItem.songID == currentPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControl.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControl.play()
                    else -> Unit
                }
            }
        }
        else {
            musicServiceConnection.transportControl.playFromMediaId (mediaItem.songID, null)
        }
    }

    fun mediaDataCompatToSong (mediaMetadataCompat: MediaMetadataCompat) =
        mediaMetadataCompat.description.mediaId?.let {
            Song (
            songID = it,
            songName = mediaMetadataCompat.description.title.toString(),
            localFileURL = mediaMetadataCompat.description.mediaUri.toString(),
            thumbnailM = mediaMetadataCompat.description.iconUri.toString(),
            thumbnailS = mediaMetadataCompat.description.iconUri.toString(),
            processing = false,
            finished = true
            )
        }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe (MY_MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback () {})
    }
}