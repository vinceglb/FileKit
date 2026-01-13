package io.github.vinceglb.filekit.sample.shared.util

import java.awt.Desktop
import java.net.URI

internal actual fun AppUrl.openUrlInBrowser() {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI(this.url))
    }
}
