package com.example.musicloud.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.*
import com.example.musicloud.database.Song
import com.example.musicloud.media.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject


private const val MY_MEDIA_ROOT_ID = "MY_MEDIA_ROOT_ID"

@HiltViewModel
class HomeViewModel @Inject constructor (
    private val musicServiceConnection: MusicServiceConnection
): ViewModel () {

    private val _mediaItems = MutableLiveData <Resource<List<Song>>> ()
    val mediaItems: LiveData <Resource<List<Song>>> get() = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currentPlayingSong = musicServiceConnection.currentPlayingSong
    val ioError = musicServiceConnection.ioError
    val playbackState = musicServiceConnection.playbackState

    private val _playbackStateDuration = MutableLiveData<Long> ()
    val playbackStateDuration : LiveData<Long>
        get() = _playbackStateDuration

    private val _playbackStatePosition = MutableLiveData<Long> ()
    val playbackStatePosition : LiveData<Long>
        get() = _playbackStatePosition

    private val _navigateToSongDetailsFragment = MutableLiveData<String> ()
    val navigateToSongDetailsFragment: LiveData<String>
        get() = _navigateToSongDetailsFragment

    init {
        getMediaItems()
        updateCurrentPlaybackStatePosition()
    }

    private fun getMediaItems () {
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
                        finished = true,
                        channelTitle = it.description.subtitle.toString()
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

    fun showSongDetails (song: Song) {
        _navigateToSongDetailsFragment.value = song.songID
    }

    fun onNavigatedToSongDetailsFragment () {
        _navigateToSongDetailsFragment.value = null
    }

    fun togglePlayPause () {
        currentPlayingSong.value?.let {
            val song = mediaDataCompatToSong (it)
            if (song != null) {
                playOrPauseSong (song, true)
            }
        }
    }

    fun playOrPauseSong (mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        musicServiceConnection.sendCommand ("startNotification", null)

        if (isPrepared &&
            mediaItem.songID == currentPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControl.pause()
                    playbackState.isPlayEnabled -> {
                        musicServiceConnection.transportControl.play()
                    }
                    else -> Unit
                }
            }
        }
        else {
            musicServiceConnection.transportControl.playFromMediaId (mediaItem.songID, null)
        }
    }

    private fun songToMediaDataCompat (song: Song): MediaMetadataCompat {
        return MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.channelTitle)
            .putString(METADATA_KEY_MEDIA_ID, song.songID)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.songName)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, song.songName)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, song.thumbnailM)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.localFileURL)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.thumbnailM)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, song.channelTitle)
            .build()
    }

    fun mediaDataCompatToSong (mediaMetadataCompat: MediaMetadataCompat): Song? {
        return mediaMetadataCompat.description.mediaId?.let {
            Song(
                songID = it,
                songName = mediaMetadataCompat.description.title.toString(),
                localFileURL = mediaMetadataCompat.description.mediaUri.toString(),
                thumbnailM = mediaMetadataCompat.description.iconUri.toString(),
                thumbnailS = mediaMetadataCompat.description.iconUri.toString(),
                processing = false,
                finished = true
            )
        }
    }

    private fun updateCurrentPlaybackStatePosition () {
        viewModelScope.launch {
            while (true) {
                val currentPos = playbackState.value?.currentPlaybackPosition
                if (playbackStatePosition.value != currentPos) {
                    _playbackStatePosition.postValue (currentPos)
                    _playbackStateDuration.postValue (MusicService.currentSongDuration)
                }
                delay (100L) // update playback position every 100ms
            }
        }
    }

    fun addNewlyAddedSongs (songs: List<Song>) {

        var oldPlayList: MutableList<Song> = mutableListOf()

        mediaItems.value?.apply {
            oldPlayList = this.data as MutableList<Song>
        }

        val newList = songs + oldPlayList
        _mediaItems.postValue (Resource.success(newList))

        var i = 0
        while (i < songs.size) {
            val mediaMetadataCompat = songToMediaDataCompat (newList[i])
            musicServiceConnection.sendCommand ("add", mediaMetadataCompat)
            i++
        }
    }

    fun removeSongFromPlaylist (song: Song) {
        val metadataCompat = songToMediaDataCompat (song)
        var removedIndex: Int = -1

        var playList: MutableList<Song>? = null

        mediaItems.value?.apply {

            playList = this.data?.toMutableList()
            removedIndex = playList?.indexOfFirst {
                it.songID == song.songID
            } ?: -1

            playList?.removeAt (removedIndex)
        }


        musicServiceConnection.sendCommand ("remove", metadataCompat, removedIndex)

        _mediaItems.postValue (Resource.success (playList))

        /**
         * if remove item is at currently playing song,
         * remove it from players and set null to current playing song.
         */
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe (MY_MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback () {})
    }
}