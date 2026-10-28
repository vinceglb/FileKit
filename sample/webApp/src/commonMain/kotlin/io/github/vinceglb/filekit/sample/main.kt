@file:Suppress("ktlint:standard:filename")

package io.github.vinceglb.filekit.sample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.github.vinceglb.filekit.sample.shared.App
import io.github.vinceglb.filekit.sample.shared.AppDemo
import kotlinx.browser.document
import kotlinx.browser.window

/**
 * The embed bridge in `index.html` reads the URL parameters used by the documentation embeds
 * and exposes them as `data-*` attributes on the root element:
 * - `demo`: opens a screen directly (see [AppDemo.id]), e.g. `?demo=file-picker`.
 * - `theme`: `light` or `dark`, also updated by `filekit-demo:theme=dark` messages from the embedding page.
 *   Defaults to the system theme.
 * - `embed`: `true` hides the documentation button of feature screens.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val root = document.documentElement
    var theme by mutableStateOf(root?.getAttribute("data-theme"))

    window.addEventListener("filekit-demo-theme", {
        theme = root?.getAttribute("data-theme")
    })

    ComposeViewport {
        App(
            startDemo = AppDemo.fromId(root?.getAttribute("data-demo")),
            darkTheme = when (theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            },
            embedded = root?.getAttribute("data-embed") == "true",
        )
    }
}
