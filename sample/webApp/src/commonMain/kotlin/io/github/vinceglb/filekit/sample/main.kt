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
import kotlinx.browser.window

/**
 * URL parameters, used when the sample is embedded in the documentation:
 * - `demo`: opens a screen directly (see [AppDemo.id]), e.g. `?demo=file-picker`.
 * - `theme`: `light` or `dark`. Defaults to the system theme.
 * - `embed`: `true` hides links to the documentation.
 *
 * The embedding page can switch the theme at runtime by posting a `filekit-demo:theme=dark` message.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val params = parseQuery(window.location.search)
    var theme by mutableStateOf(params["theme"])

    window.addEventListener("message", { event ->
        val message = (event as? org.w3c.dom.MessageEvent)?.data?.toString().orEmpty()
        if (message.startsWith(THEME_MESSAGE_PREFIX)) {
            theme = message.removePrefix(THEME_MESSAGE_PREFIX)
        }
    })

    ComposeViewport {
        App(
            startDemo = AppDemo.fromId(params["demo"]),
            darkTheme = when (theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            },
            embedded = params["embed"] == "true",
        )
    }
}

private const val THEME_MESSAGE_PREFIX = "filekit-demo:theme="

private fun parseQuery(search: String): Map<String, String> = search
    .removePrefix("?")
    .split("&")
    .filter { it.isNotEmpty() }
    .associate { part ->
        val key = part.substringBefore("=")
        val value = part.substringAfter("=", missingDelimiterValue = "")
        key to value
    }
