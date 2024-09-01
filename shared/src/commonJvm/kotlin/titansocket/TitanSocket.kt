@file:JvmName("TitanSocketJvm")

package com.architect.titansocket

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
    loggingBuilder: Logger?
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

    // notifications
    internal val socketEventsList = mutableListOf<Pair<String, ClientAction>>()
    internal val loggingSocketEventsList = mutableListOf<Pair<String, ClientAction>>()

    init {
        endpointUrl = endpoint
        if (config != null && config.trustAllCerts) {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            okHttpClient = OkHttpClient.Builder()
                .hostnameVerifier(HostnameVerifier { hostname: String?, session: SSLSession? -> true })
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .addInterceptor(LoggingInterceptor(this))
                .addNetworkInterceptor(LoggingInterceptor(this))
                .build();
        }

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

    private fun createRequest(webSocketUrl: String): Request {
        return Request.Builder()
            .url(webSocketUrl)
            .build()
    }

    actual fun broadcast(data: String) {
        webSocket?.send(data)
    }

    actual fun broadcast(data: ByteArray) {
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