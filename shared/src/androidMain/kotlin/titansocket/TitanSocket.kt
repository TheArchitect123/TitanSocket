package com.architect.titansocket

import dev.icerock.moko.mvvm.livedata.MutableLiveData
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString.Companion.toByteString
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

actual class TitanSocket actual constructor(
    endpoint: String,
    config: TitanSocketOptions?,
    build: TitanSocketBuilder.() -> Unit,
    loggingBuilder: TitanSocketBuilder.() -> Unit?
) {
    private val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    )
    private val endpointUrl: String
    private val webSocketListener = TitanWebSocketListener(this)
    private var okHttpClient = OkHttpClient()
    private var webSocket: WebSocket? = null

    val onOpen = MutableLiveData<Boolean?>(null)
    val onClosed = MutableLiveData<Boolean?>(null)
    val onFailure = MutableLiveData<Exception?>(null)
    val onDataReceived = MutableLiveData<String?>(null)
    val onBinaryReceived = MutableLiveData<ByteArray?>(null)
    val onPingSent = MutableLiveData<String?>(null)

    init {
        endpointUrl = endpoint
        if (config != null && config.trustAllCerts) {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            okHttpClient = OkHttpClient.Builder()
                .hostnameVerifier(HostnameVerifier { hostname: String?, session: SSLSession? -> true })
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .addInterceptor(LoggingInterceptor())
                .addNetworkInterceptor(LoggingInterceptor())
                .build();
        }

        object : TitanSocketBuilder {
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

                    TitanSocketEvents.MESSAGE_BINARY_RECEIVED -> {
                        onBinaryReceived.addObserver {
                            if (it != null) {
                                this@TitanSocket.action(it.decodeToString())
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
        }.build()
    }

    private fun createRequest(webSocketUrl: String): Request {
        return Request.Builder()
            .url(webSocketUrl)
            .build()
    }

    actual fun broadcast(event: String, data: JsonObject) {
        //socketClient.send(JSONObject(data.toString()).toString())
    }

    actual fun broadcast(event: String, data: JsonArray) {
        //webSocket?.send(messageET.text.toString())
    }

    actual fun broadcast(event: String, data: String) {
        webSocket?.send(data)
    }

    actual fun broadcast(event: String, data: ByteArray) {
        webSocket?.send(data.toByteString())
    }


    actual fun disconnectSocket() {
        webSocket?.close(1000, "Canceled manually.")
    }

    actual fun connectSocket() {
        webSocket = okHttpClient.newWebSocket(createRequest(endpointUrl), webSocketListener)
    }

    actual fun isSocketConnected(): Boolean {
        return false
    }
}