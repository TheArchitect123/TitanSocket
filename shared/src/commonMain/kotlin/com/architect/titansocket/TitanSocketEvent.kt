package com.architect.titansocket

expect sealed class TitanSocketEvent<T> {
    object Connection : TitanSocketEvent<Unit>
    object Disconnection : TitanSocketEvent<Unit>
    object Failure : TitanSocketEvent<Throwable>
    object OnReceivedMessage : TitanSocketEvent<Any>
    object OnSendingMessage : TitanSocketEvent<Any>
}
