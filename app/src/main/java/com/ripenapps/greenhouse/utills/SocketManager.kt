package com.ripenapps.greenhouse.utills

import io.socket.client.IO
import io.socket.client.Socket

object SocketManager {

    private var socket: Socket? = null

    init {
        val socketUrl = "http://15.184.253.191:4242/api/v1/"
//        val token = Session.token
//        try {
//            val options = IO.Options().apply {
//                auth = mapOf("token" to token)
//            }
        socket = IO.socket(socketUrl)
//        } catch (e: URISyntaxException) {
//            e.printStackTrace()
//        }
    }

    fun connect() {
        socket?.connect()
    }

    fun getSocket(): Socket {
        return socket!!
    }

    fun disconnect() {
        socket?.disconnect()
    }
}