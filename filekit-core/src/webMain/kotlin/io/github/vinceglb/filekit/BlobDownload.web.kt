package io.github.vinceglb.filekit

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * How long the object URL of a download stays alive after the download link is clicked.
 *
 * The click only starts the download. Some browsers (Firefox, older Safari) read the blob
 * asynchronously afterwards, and revoking the URL too early makes the download fail. 40 seconds
 * matches FileSaver.js, the reference implementation of this pattern. The cost is that the blob
 * stays in memory for about 40 seconds after each download (longer if the browser throttles
 * timers, for example in a background tab) instead of until the page unloads.
 */
internal val DOWNLOAD_OBJECT_URL_REVOKE_DELAY: Duration = 40.seconds

/**
 * Triggers a browser download of [blob] named [fileName] through a temporary object URL.
 *
 * The object URL is revoked after [revokeDelay], or right away if triggering the download throws.
 */
internal fun downloadBlob(
    blob: Blob,
    fileName: String,
    revokeDelay: Duration = DOWNLOAD_OBJECT_URL_REVOKE_DELAY,
) {
    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.download = fileName

    val url = URL.createObjectURL(blob)
    try {
        anchor.href = url
        anchor.click()
    } catch (e: Throwable) {
        URL.revokeObjectURL(url)
        throw e
    }

    window.setTimeout(
        handler = {
            URL.revokeObjectURL(url)
            null
        },
        timeout = revokeDelay.inWholeMilliseconds.toInt(),
    )
}
