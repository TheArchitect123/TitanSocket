package com.architect.titansocket

import com.architect.titansocket.native.NativeWebSocket
import kotlinx.cinterop.ExperimentalForeignApi
actual class TitanSocket actual constructor(
    private val endpoint: String,
    config: TitanSocketOptions?,
    build: TitanSocketBuilder.() -> Unit,
    loggingBuilder: Logger?
) {
    private val socketEventsList = mutableListOf<Pair<String, ClientAction>>()
    private val loggingSocketEventsList = mutableListOf<Pair<String, ClientAction>>()

    private val socketService = NativeWebSocket(endpoint)

    init {
        object : TitanSocketBuilder {
            override fun subscribeOn(event: String, action: TitanSocket.(message: String) -> Unit) {
                when (event) {
                    TitanSocketEvents.MESSAGE_SENDING,
                    TitanSocketEvents.MESSAGE_RECEIVED,
                    TitanSocketEvents.MESSAGE_BINARY_RECEIVED,
                    TitanSocketEvents.CONNECTION_OPENED,
                    TitanSocketEvents.DISCONNECTION ->
                        socketEventsList.add(Pair(event, action))
                    else ->
                        socketEventsList.add(Pair(TitanSocketEvents.FAILURE, action))
                }
            }
        }.build()

        if (loggingBuilder != null) {
            object : TitanSocketLoggingBuilder {
                override fun onSendRequestWebSocket(action: TitanSocket.(message: String) -> Unit) {
                    loggingSocketEventsList.add(Pair(TitanSocketTelemetryEvents.REQUEST_SENT, action))
                }
                override fun onReceiveResponseWebSocket(action: TitanSocket.(message: String) -> Unit) {
                    loggingSocketEventsList.add(Pair(TitanSocketTelemetryEvents.RESPONSE_RECEIVED, action))
                }
            }.loggingBuilder()
        }

        // Trust-all default (dev). If you prefer prod-safe default, flip to ?: false
        socketService.allowSelfSignedSSL = (config?.trustAllCerts ?: true)

        // generate subscriptions
        socketService.event().setOpen {
            socketEventsList.singleOrNull { it.first == TitanSocketEvents.CONNECTION_OPENED }
                ?.second?.invoke(this, "SOCKET CONNECTION IS OPEN - $endpoint")
        }
        socketService.event().setClose { _, _, _ ->
            socketEventsList.singleOrNull { it.first == TitanSocketEvents.DISCONNECTION }
                ?.second?.invoke(this, "SOCKET CONNECTION IS CLOSED - $endpoint")
        }
        socketService.event().setError { err ->
            socketEventsList.singleOrNull { it.first == TitanSocketEvents.FAILURE }
                ?.second?.invoke(this, "TITAN FAILED TO SEND MESSAGE TO $endpoint, ${err?.localizedDescription}")
        }
        socketService.event().setMessage { any ->
            when (any) {
                is String -> {
                    loggingSocketEventsList.singleOrNull { it.first == TitanSocketTelemetryEvents.RESPONSE_RECEIVED }
                        ?.second?.invoke(this, any)
                    socketEventsList.singleOrNull { it.first == TitanSocketEvents.MESSAGE_RECEIVED }
                        ?.second?.invoke(this, any)
                }
                is ByteArray -> {
                    loggingSocketEventsList.singleOrNull { it.first == TitanSocketTelemetryEvents.RESPONSE_RECEIVED }
                        ?.second?.invoke(this, "[${any.size} bytes]")
                    socketEventsList.singleOrNull { it.first == TitanSocketEvents.MESSAGE_BINARY_RECEIVED }
                        ?.second?.invoke(this, "[${any.size} bytes]")
                }
            }
        }
    }

    actual fun broadcast(data: String) {
        loggingSocketEventsList.singleOrNull { it.first == TitanSocketTelemetryEvents.REQUEST_SENT }
            ?.second?.invoke(this, data)
        socketService.send(data)
    }

    actual fun broadcast(data: ByteArray) {
        loggingSocketEventsList.singleOrNull { it.first == TitanSocketTelemetryEvents.REQUEST_SENT }
            ?.second?.invoke(this, "[${data.size} bytes]")
        socketService.send(data)
    }

    actual fun disconnectSocket() {
        socketService.close(1000, "Socket connection is closed")
    }

    actual fun connectSocket() {
        socketService.open(endpoint)
    }

    actual fun isSocketConnected(): Boolean {
        return socketService.isConnected()
    }
}