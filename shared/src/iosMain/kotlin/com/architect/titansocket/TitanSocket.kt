package com.architect.titansocket

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

actual class TitanSocket actual constructor(
    endpoint: String,
    config: TitanSocketOptions?,
    build: TitanSocketBuilder.() -> Unit,
    loggingBuilder: Logger?
) {
    actual fun broadcast(event: String, data: JsonObject) {
        //socketClient.send(JSONObject(data.toString()).toString())
    }

    actual fun broadcast(event: String, data: JsonArray) {
        //webSocket?.send(messageET.text.toString())
    }

    actual fun broadcast(event: String, data: String) {

    }

    actual fun broadcast(event: String, data: ByteArray) {

    }


    actual fun disconnectSocket() {

    }

    actual fun connectSocket() {

    }

    actual fun isSocketConnected(): Boolean {
        return false
    }
}


