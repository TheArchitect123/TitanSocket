package com.architect.titansocket

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform