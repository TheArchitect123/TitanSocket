package com.architect.titansocket

interface TitanSocketLoggingBuilder {
    fun onSendRequestWebSocket(message: String, action: TitanSocket.(message: String) -> Unit)
    fun onReceiveResponseWebSocket(message: String, action: TitanSocket.(message: String) -> Unit)
}