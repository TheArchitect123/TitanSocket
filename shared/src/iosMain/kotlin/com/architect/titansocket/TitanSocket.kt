package com.architect.titansocket

import com.ttypic.objclibs.titanEngine.WebSocket
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class TitanSocket actual constructor(
    private val endpoint: String,
    config: TitanSocketOptions?,
    build: TitanSocketBuilder.() -> Unit,
    loggingBuilder: Logger?
) {
    // notifications
    private val socketEventsList = mutableListOf<Pair<String, ClientAction>>()
    private val loggingSocketEventsList = mutableListOf<Pair<String, ClientAction>>()

    // web socket service
    private val socketService = WebSocket(endpoint)

    init {
        object : TitanSocketBuilder {
            override fun subscribeOn(event: String, action: TitanSocket.(message: String) -> Unit) {
                when (event) {
                    TitanSocketEvents.MESSAGE_SENDING -> {
                        socketEventsList.add(
                            Pair(
                                TitanSocketEvents.MESSAGE_SENDING,
                                action
                            )
                        )
                    }

                    TitanSocketEvents.MESSAGE_RECEIVED -> {
                        socketEventsList.add(
                            Pair(
                                TitanSocketEvents.MESSAGE_RECEIVED,
                                action
                            )
                        )
                    }

                    TitanSocketEvents.MESSAGE_BINARY_RECEIVED -> {
                        socketEventsList.add(
                            Pair(
                                TitanSocketEvents.MESSAGE_BINARY_RECEIVED,
                                action
                            )
                        )
                    }


                    TitanSocketEvents.CONNECTION_OPENED -> {
                        socketEventsList.add(
                            Pair(
                                TitanSocketEvents.CONNECTION_OPENED,
                                action
                            )
                        )
                    }

                    TitanSocketEvents.DISCONNECTION -> {
                        socketEventsList.add(
                            Pair(
                                TitanSocketEvents.DISCONNECTION,
                                action
                            )
                        )
                    }

                    else -> { // on Failure
                        socketEventsList.add(
                            Pair(
                                TitanSocketEvents.FAILURE,
                                action
                            )
                        )
                    }
                }
            }
        }.build()

        if (loggingBuilder != null) {
            object : TitanSocketLoggingBuilder {
                override fun onSendRequestWebSocket(
                    action: TitanSocket.(message: String) -> Unit
                ) {
                    loggingSocketEventsList.add(
                        Pair(
                            TitanSocketTelemetryEvents.REQUEST_SENT,
                            action
                        )
                    )
                }

                override fun onReceiveResponseWebSocket(
                    action: TitanSocket.(message: String) -> Unit
                ) {
                    loggingSocketEventsList.add(
                        Pair(
                            TitanSocketTelemetryEvents.RESPONSE_RECEIVED,
                            action
                        )
                    )
                }

            }.loggingBuilder()
        }

        socketService.setAllowSelfSignedSSL(
            config?.trustAllCerts ?: false
        ) // if socket connection needs to allow all self signed certificates (by passes transport security on iOS)

        // generate subscriptions
        socketService.event().setOpen {
            socketEventsList.singleOrNull { q -> q.first == TitanSocketEvents.CONNECTION_OPENED }?.second?.invoke(
                this,
                "SOCKET CONNECTION IS OPEN"
            )
        }
        socketService.event().setClose { l, s, b ->
            socketEventsList.singleOrNull { q -> q.first == TitanSocketEvents.DISCONNECTION }?.second?.invoke(
                this,
                "SOCKET CONNECTION IS CLOSED"
            )
        }

        socketService.event().setError {
            socketEventsList.singleOrNull { q -> q.first == TitanSocketEvents.FAILURE }?.second?.invoke(
                this,
                "TITAN FAILED TO SEND MESSAGE TO $endpoint, ${it?.localizedDescription}"
            )
        }

        socketService.event().setMessage {
            socketEventsList.singleOrNull { q -> q.first == TitanSocketEvents.MESSAGE_RECEIVED }?.second?.invoke(
                this,
                it as String
            )
        }
    }

    actual fun broadcast(data: String) {
        socketService.send(data)
    }

    actual fun broadcast(data: ByteArray) {
        socketService.send(data)
    }

    actual fun disconnectSocket() {
        socketService.close(0, "Socket connection is closed")
    }

    actual fun connectSocket() {
        socketService.open(endpoint)
    }

    actual fun isSocketConnected(): Boolean {
        return false
    }
}