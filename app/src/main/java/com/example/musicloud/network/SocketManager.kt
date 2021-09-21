package com.example.musicloud.network

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.delay
import java.net.URISyntaxException

private const val RESPONSE_TIMEOUT = "response_timeout"

class SocketManager {

    private var socket: Socket
    private val serverSocketURL = "https://www.musicloud-api.site/api"

    init {
        try {
            socket = IO.socket (serverSocketURL)
            socket.connect()
        }
        catch (exp: URISyntaxException) {
            throw exp
        }
    }


    suspend fun connectSocketEvent (): Boolean {
        var isConnected = false
        socket.on (Socket.EVENT_CONNECT) {
            isConnected = true
        }
        delay (2000L)
        return isConnected
    }

    suspend fun trackSongProcess (taskID: String): String {

        socket.emit ("process_status", taskID)
        var taskStatus = RESPONSE_TIMEOUT
        socket.on ("process_status_resp") {
            taskStatus = it[0] as String
        }
        delay (3000L)

        return taskStatus
    }

    fun disconnectSocket () {
        socket.disconnect()
    }

}
