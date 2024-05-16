package com.architect.titansocket

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class TitanWebSocketListener(private val socket: TitanSocket) : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)

        socket.socketEventsList.singleOrNull { it.first == TitanSocketEvents.CONNECTION_OPENED }?.second?.invoke(
            socket,
            "CONNECTION SOCKET IS OPEN"
        )
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)

        socket.socketEventsList.singleOrNull { it.first == TitanSocketEvents.MESSAGE_BINARY_RECEIVED }?.second?.invoke(
            socket,
            bytes.toString()
        )
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)

        socket.socketEventsList.singleOrNull { it.first == TitanSocketEvents.MESSAGE_RECEIVED }?.second?.invoke(
            socket,
            text
        )
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)

        socket.socketEventsList.singleOrNull { it.first == TitanSocketEvents.DISCONNECTION }?.second?.invoke(
            socket,
            "SOCKET CONNNECTION IS CLOSED, REASON DETECTED \"$reason\""
        )
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)

        socket.socketEventsList.singleOrNull { it.first == TitanSocketEvents.FAILURE }?.second?.invoke(
            socket,
            t.message ?: "WEB SOCKET FAILURE DETECTED"
        )
    }
}