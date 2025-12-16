package io.github.vinceglb.filekit.sample.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("ktlint:standard:function-naming", "unused", "FunctionName")
public fun MainViewController(): UIViewController = ComposeUIViewController { App() }
