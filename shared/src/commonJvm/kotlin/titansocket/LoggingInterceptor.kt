package com.architect.titansocket

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

internal class LoggingInterceptor(val socket: TitanSocket) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val t1 = System.nanoTime()

        val requestInfo = java.lang.String.format(
                "Sending request %s on %s%n%s%s",
                request.url, chain.connection(), request.headers, request.body
            )
        socket.loggingSocketEventsList.singleOrNull { it.first == TitanSocketTelemetryEvents.REQUEST_SENT }?.second?.invoke(
            socket,
            requestInfo
        )

        val response: Response = chain.proceed(request)
        val t2 = System.nanoTime()

        val responseText =    String.format(
            "Received response for %s in %.1fms%n%s%s",
            response.request.url, (t2 - t1) / 1e6, response.headers, response.body
        )
        socket.loggingSocketEventsList.singleOrNull { it.first == TitanSocketTelemetryEvents.RESPONSE_RECEIVED }?.second?.invoke(
            socket,
            responseText
        )

        return response
    }
}

