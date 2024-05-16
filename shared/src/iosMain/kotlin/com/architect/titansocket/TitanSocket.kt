package com.architect.titansocket

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

actual class TitanSocket actual constructor(
    endpoint: String,
    config: TitanSocketOptions?,
    build: TitanSocketBuilder.() -> Unit
) {
    actual fun broadcast(event: String, data: JsonObject) {
    }

    actual fun broadcast(event: String, data: JsonArray) {
    }

    actual fun broadcast(event: String, data: String) {
    }

    actual fun connectSocket() {
    }

    actual fun disconnectSocket() {
    }

    actual fun isSocketConnected(): Boolean {
        return false
    }

    actual fun broadcast(event: String, data: ByteArray) {
    }
}


