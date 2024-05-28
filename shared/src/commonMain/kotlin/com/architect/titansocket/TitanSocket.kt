package com.architect.titansocket

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

typealias ClientAction = TitanSocket.(message: String) -> Unit
typealias Logger = TitanSocketLoggingBuilder.() -> Unit?
expect class TitanSocket(
    endpoint: String,
    config: TitanSocketOptions? = null,
    build: TitanSocketBuilder.() -> Unit,
    loggingBuilder: Logger? = null
) {
    fun broadcast(data: String)
    fun broadcast(data: ByteArray)
    fun connectSocket()
    fun disconnectSocket()
    fun isSocketConnected(): Boolean
}
