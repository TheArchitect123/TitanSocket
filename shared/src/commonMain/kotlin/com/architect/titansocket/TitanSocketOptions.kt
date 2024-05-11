package com.architect.titansocket

data class TitanSocketOptions(
    val queryParams: Map<String, String>?,
    val transport: Transport = Transport.DEFAULT
) {
    enum class Transport {
        WEBSOCKET,
        POLLING,
        DEFAULT
    }
}

