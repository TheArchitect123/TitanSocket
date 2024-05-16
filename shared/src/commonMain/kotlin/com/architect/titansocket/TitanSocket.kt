package com.architect.titansocket

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

typealias Logger = TitanSocketLoggingBuilder.() -> Unit?
expect class TitanSocket(
    endpoint: String,
    config: TitanSocketOptions? = null,
    build: TitanSocketBuilder.() -> Unit,
    loggingBuilder: Logger? = null
) {
    fun broadcast(event: String, data: JsonObject)
    fun broadcast(event: String, data: JsonArray)
    fun broadcast(event: String, data: String)
    fun broadcast(event: String, data: ByteArray)
    fun connectSocket()
    fun disconnectSocket()
    fun isSocketConnected(): Boolean
}
