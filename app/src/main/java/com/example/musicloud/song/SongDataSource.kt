package com.example.musicloud.song

class SongDataSource {

    private val onReadyListener = mutableListOf<(Boolean) -> Unit> ()

    /* Check whether the songs are ready to play or not */
    private var state: State = State.STATE_CREATED
        set(value) {
            if (value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
                synchronized (onReadyListener) {
                    field = value
                    onReadyListener.forEach { listener ->
                        listener (state == State.STATE_INITIALIZED)
                    }
                }
            }
            else {
                field = value
            }
        }

    fun whenReady (action: (Boolean) -> Unit): Boolean {
        return if (state == State.STATE_CREATED || state == State.STATE_INITIALIZING) {
            onReadyListener += action // inserting the action() to the onReadyListener List
            false
        } else {
            action (state == State.STATE_INITIALIZED)
            true
        }

    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}