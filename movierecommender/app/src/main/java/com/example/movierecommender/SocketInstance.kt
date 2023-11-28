package com.example.movierecommender

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketHandler {

    private var mSocket: Socket? = null

    @Synchronized
    fun setSocket(uri:String) {
        try {
            if (mSocket == null)
                mSocket = IO.socket(uri)
        } catch (e: URISyntaxException) {

        }
    }

    @Synchronized
    fun getSocket(): Socket? {
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket?.let {
            if (!it.connected()) {
                it.connect()
            }
        }
    }

    @Synchronized
    fun closeConnection() {
        mSocket?.disconnect()
    }
}