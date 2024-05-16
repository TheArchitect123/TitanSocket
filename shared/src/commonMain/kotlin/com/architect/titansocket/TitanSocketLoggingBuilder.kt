package com.architect.titansocket

interface TitanSocketLoggingBuilder {
    fun onSendRequestWebSocket(action: TitanSocket.(message: String) -> Unit)
    fun onReceiveResponseWebSocket(action: TitanSocket.(message: String) -> Unit)
}