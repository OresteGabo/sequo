package dev.orestegabo.sequo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform