package com.architect.titansocket

import dev.icerock.moko.mvvm.livedata.postValue
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class TitanWebSocketListener(val socket: TitanSocket): WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        socket.onOpen.postValue(true)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)

        socket.onBinaryReceived.postValue(bytes.toByteArray())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        socket.onDataReceived.postValue(text)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        socket.onClosed.postValue(true)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)

        socket.onFailure.postValue(Exception(t.message))
    }
}