package com.architect.titansocket

import android.util.Log
import dev.icerock.moko.mvvm.livedata.postValue
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class TitanWebSocketListener(val socket: TitanSocket) : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Log.i(
            "TITAN_SOCKET",
            java.lang.String.format(
                "SocketSessionOpened",
            )
        )

        socket.onOpen.postValue(true)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        Log.i(
            "TITAN_SOCKET",
            java.lang.String.format(
                "ReceivedByteArray $bytes",
            )
        )
        socket.onBinaryReceived.postValue(bytes.toByteArray())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        Log.i(
            "TITAN_SOCKET",
            java.lang.String.format(
                "ReceivedMessageText $text",
            )
        )
        socket.onDataReceived.postValue(text)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        Log.i(
            "TITAN_SOCKET",
            java.lang.String.format(
                "ClosingWebSocketSession $reason",
            )
        )
        socket.onClosed.postValue(true)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)

        Log.i(
            "TITAN_SOCKET",
            java.lang.String.format(
                "SocketSessionFailed ${t.message}, \n ${t.stackTrace}",
            )
        )

        socket.onFailure.postValue(Exception(t.message))
    }
}