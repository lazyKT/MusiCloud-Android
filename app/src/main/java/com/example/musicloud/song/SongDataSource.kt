package com.example.musicloud.song

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.example.musicloud.database.Song
import com.example.musicloud.database.SongDAO
import com.example.musicloud.song.State.*
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/*
* This is an abstraction layer between the original Song Source (Rooms Database) and the Music Player.
* This class loads the song information and metadata, then initializes the necessary operations
* to format the song data which will be provided to the Music Player later.
* */
class SongDataSource @Inject constructor(
    private val songDAO: SongDAO
){

    var formattedSongs = emptyList<MediaMetadataCompat>()

    suspend fun getMediaData () = withContext (Dispatchers.Main) {
        state = STATE_INITIALIZING

        val allSongs =
            withContext(Dispatchers.IO) { songDAO.getSongs() }

        formattedSongs = allSongs.map {
            MediaMetadataCompat.Builder()
                .putString (METADATA_KEY_ARTIST, it.channelTitle)
                .putString (METADATA_KEY_MEDIA_ID, it.songID)
                .putString (METADATA_KEY_TITLE, it.songName)
                .putString (METADATA_KEY_DISPLAY_TITLE, it.songName)
                .putString (METADATA_KEY_DISPLAY_ICON_URI, it.thumbnailM)
                .putString (METADATA_KEY_MEDIA_URI, it.localFileURL)
                .putString (METADATA_KEY_ALBUM_ART_URI, it.thumbnailM)
                .putString (METADATA_KEY_DISPLAY_SUBTITLE, it.channelTitle)
                .build()
        }
        state = STATE_INITIALIZED
    }

    /* creating playList */
    fun asMediaSource (dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()

        /* iterate through each song and convert it to the media source so that the exo player can play */
        formattedSongs.forEach {
            val mediaSource = ProgressiveMediaSource.Factory (dataSourceFactory)
                .createMediaSource (MediaItem.fromUri(it.getString(METADATA_KEY_MEDIA_URI)))
            concatenatingMediaSource.addMediaSource (mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItem () = formattedSongs.map {
        val description = MediaDescriptionCompat.Builder()
            .setMediaUri (it.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle (it.description.title)
            .setSubtitle (it.description.subtitle)
            .setMediaId (it.description.mediaId)
            .setIconUri (it.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem (description, FLAG_PLAYABLE)
    }.toMutableList()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit> ()

    /* Check whether the songs data are loaded and ready to play or not */
    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized (onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener (state == STATE_INITIALIZED)
                    }
                }
            }
            else {
                field = value
            }
        }

    fun whenReady (action: (Boolean) -> Unit): Boolean {
        return if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action // inserting the action() to the onReadyListener List
            false
        } else {
            action (state == STATE_INITIALIZED)
            true
        }

    }

    fun addNewSong (song: Song) {

    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}