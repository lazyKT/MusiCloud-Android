package com.example.musicloud.media.callbacks

import android.util.Log
import android.widget.Toast
import com.example.musicloud.media.MusicService
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

class MusicPlayerEventListener (
    private val musicService: MusicService
): Player.Listener {

    private var _playbackState: Int? = null

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText (musicService, "Unknown Error Occurred!", Toast.LENGTH_LONG).show()
    }


    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        _playbackState = playbackState
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        Log.i ("MusicPlayerEventListener", "PlayWhenReadyChangeReason : $reason")
        if (_playbackState == Player.STATE_READY && !playWhenReady) {
            musicService.stopForeground (false)
        }
    }
}