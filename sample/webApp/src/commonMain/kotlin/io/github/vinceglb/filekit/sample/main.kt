@file:Suppress("ktlint:standard:filename")

package io.github.vinceglb.filekit.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.github.vinceglb.filekit.sample.shared.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        App()
    }
}
