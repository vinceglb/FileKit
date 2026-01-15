package io.github.vinceglb.filekit.sample.shared.util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual fun AppUrl.openUrlInBrowser() {
    NSURL.URLWithString(this.url)?.let {
        UIApplication.sharedApplication.openURL(
            url = it,
            options = emptyMap<Any?, String>(),
            completionHandler = null,
        )
    }
}
