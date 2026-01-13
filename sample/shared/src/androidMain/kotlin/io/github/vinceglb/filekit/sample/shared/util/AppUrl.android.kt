package io.github.vinceglb.filekit.sample.shared.util

import android.content.Intent
import androidx.core.net.toUri
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.context

internal actual fun AppUrl.openUrlInBrowser() {
    val browserIntent = Intent(Intent.ACTION_VIEW, this.url.toUri())
    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    FileKit.context.startActivity(browserIntent)
}
