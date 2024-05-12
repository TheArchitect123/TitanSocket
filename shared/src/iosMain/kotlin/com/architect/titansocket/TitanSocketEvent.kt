package com.architect.titansocket

actual sealed class TitanSocketEvent<T> : Mapper<T> {
    actual object Connection : TitanSocketEvent<Unit>() {
        override fun mapper(array: Array<out Any>) {
            TODO("Not yet implemented")
        }
    }

    actual object Disconnection : TitanSocketEvent<Unit>() {
        override fun mapper(array: Array<out Any>) {
            TODO("Not yet implemented")
        }
    }

    actual object Failure : TitanSocketEvent<Throwable>() {
        override fun mapper(array: Array<out Any>): Throwable {
            TODO("Not yet implemented")
        }
    }

    actual object OnReceivedMessage : TitanSocketEvent<Any>() {
        override fun mapper(array: Array<out Any>): Any {
            TODO("Not yet implemented")
        }
    }

    actual object OnSendingMessage : TitanSocketEvent<Any>() {
        override fun mapper(array: Array<out Any>): Any {
            TODO("Not yet implemented")
        }
    }
}