package com.architect.titansocket

interface Mapper<T> {
    fun mapper(array: Array<out Any>): T
}
