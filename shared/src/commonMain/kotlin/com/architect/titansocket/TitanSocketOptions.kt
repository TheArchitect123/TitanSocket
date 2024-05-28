package com.architect.titansocket

data class TitanSocketOptions(
    val trustAllCerts : Boolean = false
) {
    enum class Transport {
        WEBSOCKET,
        POLLING,
        DEFAULT
    }
}

