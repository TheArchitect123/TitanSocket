package com.architect.titansocket

import platform.Foundation.NSOperationQueue
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionWebSocketDelegateProtocol
import platform.Foundation.NSURLSessionWebSocketMessage
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create

actual class TitanSocket actual constructor(
    endpoint: String,
    config: TitanSocketOptions?,
    build: TitanSocketBuilder.() -> Unit,
    loggingBuilder: Logger?
) {
    private val endpointUrl = endpoint
    private val webSocketClient = NSURLSession.sessionWithConfiguration(
        configuration = NSURLSessionConfiguration.defaultSessionConfiguration(),
        delegate = object : NSObject(), NSURLSessionWebSocketDelegateProtocol {
            override fun URLSession(
                session: NSURLSession,
                webSocketTask: NSURLSessionWebSocketTask,
                didOpenWithProtocol: String?
            ) {
                socketEventsList.singleOrNull { it.first == com.architect.titansocket.TitanSocketEvents.CONNECTION_OPENED }?.second?.invoke(
                    this,
                    "CONNECTION SOCKET IS OPEN"
                )
            }

            override fun URLSession(
                session: NSURLSession,
                webSocketTask: NSURLSessionWebSocketTask,
                didCloseWithCode: NSURLSessionWebSocketCloseCode,
                reason: NSData?
            ) {
                socketEventsList.singleOrNull { it.first == com.architect.titansocket.TitanSocketEvents.DISCONNECTION }?.second?.invoke(
                    this,
                    "CONNECTION SOCKET IS CLOSED"
                )
            }
        },
        delegateQueue = NSOperationQueue.currentQueue()
    ).webSocketTaskWithURL(NSURL.URLWithString(endpointUrl)!!)

    // notifications
    val socketEventsList = mutableListOf<Pair<String, ClientAction>>()
    val loggingSocketEventsList = mutableListOf<Pair<String, ClientAction>>()

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
    }

    actual fun broadcast(data: String) {
        val message = NSURLSessionWebSocketMessage(data)
        webSocketClient.sendMessage(message) { error ->
            if (error == null) {
                webSocketClient.receiveMessageWithCompletionHandler { message, nsError ->
                    when {
                        nsError != null -> {
                            socketEventsList.singleOrNull { it.first == TitanSocketEvents.FAILURE }?.second?.invoke(
                                this,
                                "TITAN FAILED TO SEND MESSAGE TO $endpointUrl, ${nsError.localizedDescription}"
                            )
                        }

                        message != null -> {
                            message.data?.let {
                                socketEventsList.singleOrNull { it.first == TitanSocketEvents.MESSAGE_RECEIVED }?.second?.invoke(
                                    this,
                                    NSString.create(
                                        data = it,
                                        encoding = NSUTF8StringEncoding
                                    )?.toString()!!
                                )
                            }
                        }
                    }
                }
            } else {
                socketEventsList.singleOrNull { it.first == TitanSocketEvents.FAILURE }?.second?.invoke(
                    this,
                    "TITAN FAILED TO SEND MESSAGE TO $endpointUrl, ${error.localizedDescription}"
                )
            }
        }
    }

    actual fun broadcast(data: ByteArray) {

    }


    actual fun disconnectSocket() {
        webSocketClient.cancel()
    }

    actual fun connectSocket() {
        webSocketClient.resume()
    }

    actual fun isSocketConnected(): Boolean {
        return false
    }
}