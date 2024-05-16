package com.architect.titansocket

interface TitanSocketBuilder {
    fun subscribeOn(event: String, action: TitanSocket.(message: String) -> Unit)
}

