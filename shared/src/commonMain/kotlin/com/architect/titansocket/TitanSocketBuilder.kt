package com.architect.titansocket

interface TitanSocketBuilder {
    fun subscribeOn(event: String, action: TitanSocket.(message: String) -> Unit)
    fun <T> subscribeOn(titanSocketEvent: TitanSocketEvent<T>, action: TitanSocket.(array: T) -> Unit)

    fun subscribeOn(vararg events: String, action: TitanSocket.(message: String) -> Unit) {
        events.forEach {
            subscribeOn(it, action)
        }
    }

    fun <T> subscribeOn(vararg titanSocketEvents: TitanSocketEvent<T>, action: TitanSocket.(array: T) -> Unit) {
        titanSocketEvents.forEach {
            subscribeOn(it, action)
        }
    }
}
