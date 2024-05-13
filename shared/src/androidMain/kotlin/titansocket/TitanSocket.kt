package com.architect.titansocket

import dev.gustavoavila.websocketclient.WebSocketClient
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.mvvm.livedata.postValue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

actual class TitanSocket actual constructor(
    endpoint: String,
    config: TitanSocketOptions?,
    build: TitanSocketBuilder.() -> Unit
) {
    private val socketClient: WebSocketClient

    init {
        val titanBuilder = object : TitanSocketBuilder {
            val onOpen = MutableLiveData<Boolean?>(null)
            val onClosed = MutableLiveData<Boolean?>(null)
            val onFailure = MutableLiveData<Exception?>(null)
            val onDataReceived = MutableLiveData<String?>(null)
            val onBinaryReceived = MutableLiveData<ByteArray?>(null)
            val onPingSent = MutableLiveData<String?>(null)
            val onPongReceived = MutableLiveData<String?>(null)

            override fun subscribeOn(event: String, action: TitanSocket.(message: String) -> Unit) {
                when (event) {
                    TitanSocketEvents.MESSAGE_SENDING -> {
                        onPingSent.addObserver {
                            if (it != null) {
                                this@TitanSocket.action(it)
                            }
                        }
                    }

                    TitanSocketEvents.MESSAGE_RECEIVED -> {
                        onDataReceived.addObserver {
                            if (it != null) {
                                this@TitanSocket.action(it)
                            }
                        }
                    }

                    TitanSocketEvents.CONNECTION_OPENED -> {
                        onOpen.addObserver {
                            if (it != null) {
                                this@TitanSocket.action("WEBSOCKET IS OPEN")
                            }
                        }
                    }

                    TitanSocketEvents.DISCONNECTION -> {
                        onClosed.addObserver {
                            if (it != null) {
                                this@TitanSocket.action("WEBSOCKET IS CLOSED")
                            }
                        }
                    }

                    else -> { // on Failure
                        onFailure.addObserver {
                            if (it != null) {
                                this@TitanSocket.action(
                                    it.message
                                        ?: "WEBSOCKET FAILURE HAS OCCURRED, NO EXCEPTION FOUND"
                                )
                            }
                        }
                    }
                }
            }

            override fun <T> subscribeOn(
                socketEvent: TitanSocketEvent<T>,
                action: TitanSocket.(data: T) -> Unit
            ) {

            }
        }

        socketClient =
            object : WebSocketClient(URI(endpoint)) {
                override fun onOpen() {
                    titanBuilder.onOpen.postValue(true)
                }

                override fun onTextReceived(message: String) {
                    titanBuilder.onDataReceived.postValue(message)
                }

                override fun onBinaryReceived(data: ByteArray) {
                    titanBuilder.onBinaryReceived.postValue(data)
                }

                override fun onPingReceived(data: ByteArray) {
                    titanBuilder.onPingSent.postValue(data.decodeToString())
                }

                override fun onPongReceived(data: ByteArray) {
                    titanBuilder.onPongReceived.postValue(data.decodeToString())
                }

                override fun onException(e: Exception) {
                    titanBuilder.onFailure.postValue(e)
                }

                override fun onCloseReceived(reason: Int, description: String?) {
                    titanBuilder.onClosed.postValue(true)
                }
            }

        //socketClient.setConnectTimeout(10000)
        //socketClient.setReadTimeout(60000)
        socketClient.addHeader("Origin", "*")
        socketClient.enableAutomaticReconnection(5000)
        titanBuilder.build()
    }

    actual fun broadcast(event: String, data: JsonObject) {
        socketClient.send(JSONObject(data.toString()).toString())
    }

    actual fun broadcast(event: String, data: JsonArray) {
        socketClient.send(JSONArray(data.toString()).toString())
    }

    actual fun broadcast(event: String, data: String) {
        socketClient.send(data)
    }

    actual fun disconnectSocket() {
        socketClient.close(0, 0, "")
    }

    actual fun connectSocket() {
        socketClient.connect()
    }

    actual fun isSocketConnected(): Boolean {
        return false
    }
}
