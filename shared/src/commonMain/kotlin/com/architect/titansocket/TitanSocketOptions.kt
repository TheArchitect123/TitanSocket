package com.architect.titansocket

data class TitanSocketOptions(
    val queryParams: Map<String, String>?,
    val transport: Transport = Transport.DEFAULT,
    val trustAllCerts : Boolean = false
) {
    enum class Transport {
        WEBSOCKET,
        POLLING,
        DEFAULT
    }
}

