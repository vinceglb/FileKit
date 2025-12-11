package io.github.vinceglb.filekit.sample

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform