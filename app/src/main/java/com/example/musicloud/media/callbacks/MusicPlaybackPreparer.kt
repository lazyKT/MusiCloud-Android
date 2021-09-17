package com.example.musicloud.media.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_URI
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.example.musicloud.song.SongDataSource
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector


/**
 * Preparing Media player and Playlist Items (including add new items to playlist)
 */
class MusicPlaybackPreparer (
    private val songDataSource: SongDataSource,
    private val onPlayerPrepared: (MediaMetadataCompat?) -> Unit
        ): MediaSessionConnector.PlaybackPreparer {


    override fun onCommand(
        player: Player,
        controlDispatcher: ControlDispatcher,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ): Boolean {

        Log.i ("MusicPlaybackPreparer", "onCommand() -> command: $command")

        if (command == "add") {
            val mediaMetadataCompat = extras?.getParcelable<MediaMetadataCompat>("newSong")
            mediaMetadataCompat?.apply {
                player.addMediaItem (0, MediaItem.fromUri(getString (METADATA_KEY_MEDIA_URI)))
                songDataSource.addNewSong (this)
                Log.i ("MusicPlaybackPreparer", "onCommand() -> Added to the playlist")
            }
        }

        return true
    }

    override fun getSupportedPrepareActions(): Long {
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }

    override fun onPrepare(playWhenReady: Boolean) = Unit

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        songDataSource.whenReady {
            val itemToPlay = songDataSource.formattedSongs.find { mediaId == it.description.mediaId }
            onPlayerPrepared (itemToPlay)
        }
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit


}