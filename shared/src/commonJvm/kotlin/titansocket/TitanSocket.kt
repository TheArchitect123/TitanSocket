package com.architect.titansocket

import io.socket.client.IO
import io.socket.engineio.client.transports.Polling
import io.socket.engineio.client.transports.WebSocket
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import io.socket.client.Socket as TitanSocketIO

actual class TitanSocket actual constructor(
    endpoint: String,
    config: TitanSocketOptions?,
    build: TitanSocketBuilder.() -> Unit
) {
    private val socketClient: TitanSocketIO

    init {
        socketClient = IO.socket(URI.create(endpoint), IO.Options().apply {
            transports = config?.transport?.let {
                when (it) {
                    TitanSocketOptions.Transport.DEFAULT -> return@let null
                    TitanSocketOptions.Transport.WEBSOCKET -> return@let arrayOf(WebSocket.NAME)
                    TitanSocketOptions.Transport.POLLING -> return@let arrayOf(Polling.NAME)
                }
            }
            query = config?.queryParams?.run {
                if (isEmpty()) return@run null

                val params: List<String> = map { (key, value) -> "$key=$value" }
                params.joinToString("&")
            }
        })

        object : TitanSocketBuilder {
            override fun subscribeOn(event: String, action: TitanSocket.(message: String) -> Unit) {
                if (event != TitanSocketEvents.MESSAGE_RECEIVED && event != TitanSocketEvents.MESSAGE_SENDING) {
                    socketClient.on(event) {
                        this@TitanSocket.action(rawDataToString(it))
                    }
                } else {
                    when (event) {
                        TitanSocketEvents.MESSAGE_RECEIVED -> { // when receiving packets from the web server
                            socketClient.onAnyIncoming {
                                this@TitanSocket.action(rawDataToString(it))
                            }
                        }

                        else -> { // on sending of packets
                            socketClient.onAnyOutgoing {
                                this@TitanSocket.action(rawDataToString(it))
                            }
                        }
                    }
                }
            }

            override fun <T> subscribeOn(
                socketEvent: TitanSocketEvent<T>,
                action: TitanSocket.(data: T) -> Unit
            ) {
                socketEvent.socketIoEvents.forEach { event ->
                    if (event != TitanSocketEvents.MESSAGE_RECEIVED && event != TitanSocketEvents.MESSAGE_SENDING) {
                        socketClient.on(event) { data ->
                            this@TitanSocket.action(socketEvent.mapper(data))
                        }
                    } else {
                        when (event) {
                            TitanSocketEvents.MESSAGE_RECEIVED -> { // when receiving packets from the web server
                                socketClient.onAnyIncoming {
                                    this@TitanSocket.action(socketEvent.mapper(it))
                                }
                            }

                            else -> { // on sending of packets
                                socketClient.onAnyOutgoing {
                                    this@TitanSocket.action(socketEvent.mapper(it))
                                }
                            }
                        }
                    }
                }
            }
        }.build()
    }

    actual fun broadcast(event: String, data: JsonObject) {
        socketClient.emit(event, JSONObject(data.toString()))
    }

    actual fun broadcast(event: String, data: JsonArray) {
        socketClient.emit(event, JSONArray(data.toString()))
    }

    actual fun broadcast(event: String, data: String) {
        socketClient.emit(event, data)
    }

    actual fun disconnectSocket() {
        socketClient.disconnect()
    }

    actual fun connectSocket() {
        socketClient.connect()
    }

    actual fun isSocketConnected(): Boolean {
        return socketClient.connected()
    }

    private companion object {
        fun rawDataToString(data: Array<out Any>): String {
            return data.last().toString()
        }
    }
}
