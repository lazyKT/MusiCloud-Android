package com.example.musicloud.network

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.delay
import java.net.URISyntaxException

private const val RESPONSE_TIMEOUT = "response_timeout"

class SocketManager {

    private lateinit var socket: Socket
    private val serverSocketURL = "http://10.0.2.2:5000/api"

    init {
        try {
            socket = IO.socket (serverSocketURL)
            socket.connect()
        }
        catch (exp: URISyntaxException) {
            exp.message?.let { Log.e ("SocketManger", it) }
        }
    }


    suspend fun connectSocketEvent (): Boolean {
        var isConnected = false
        socket.on (Socket.EVENT_CONNECT) {
            isConnected = true
        }
        delay (1000L)
        return isConnected
    }

    suspend fun trackSongProcess (taskID: String): String {

        socket.emit ("process_status", taskID)
        var taskStatus = RESPONSE_TIMEOUT
        socket.on ("process_status_resp") {
            taskStatus = it[0] as String
        }
        delay (2000L)

        return taskStatus
    }

    fun disconnectSocket () {
        socket.disconnect()
        socket.on (Socket.EVENT_DISCONNECT) {
            Log.i ("SongRepository", "Socket Disconnected!")
        }
    }

}
