package io.github.vinceglb.filekit.sample.shared.util

internal data class AppUrl(
    val url: String,
)

internal expect fun AppUrl.openUrlInBrowser()
