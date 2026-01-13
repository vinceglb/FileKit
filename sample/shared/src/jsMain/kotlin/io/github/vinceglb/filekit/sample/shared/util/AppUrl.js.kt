package io.github.vinceglb.filekit.sample.shared.util

import kotlinx.browser.window

internal actual fun AppUrl.openUrlInBrowser() {
    window.open(this.url, "_blank")
}
