package com.architect.titansocket

actual sealed class TitanSocketEvent<T> : Mapper<T> {
    actual object Connection : TitanSocketEvent<Unit>(), Mapper<Unit> by UnitMapper() {
        override val socketIoEvents: List<String> = listOf(TitanSocketEvents.CONNECTION_OPENED)
    }

    actual object Disconnection : TitanSocketEvent<Unit>(), Mapper<Unit> by UnitMapper() {
        override val socketIoEvents: List<String> = listOf(TitanSocketEvents.DISCONNECTION)
    }

    actual object Failure : TitanSocketEvent<Throwable>() {
        override val socketIoEvents: List<String> = listOf(
            TitanSocketEvents.FAILURE,
        )

        override fun mapper(array: Array<out Any>): Throwable {
            return array[0] as Throwable
        }
    }

    actual object OnReceivedMessage : TitanSocketEvent<Any>() {
        override val socketIoEvents: List<String> = listOf(TitanSocketEvents.MESSAGE_RECEIVED)
        override fun mapper(array: Array<out Any>): Any {
            return array
        }
    }

    actual object OnSendingMessage : TitanSocketEvent<Any>() {
        override val socketIoEvents: List<String> = listOf(TitanSocketEvents.MESSAGE_SENDING)
        override fun mapper(array: Array<out Any>): Any {
            return array
        }
    }

    abstract val socketIoEvents: List<String>

    private class UnitMapper : Mapper<Unit> {
        override fun mapper(array: Array<out Any>) = Unit
    }
}
