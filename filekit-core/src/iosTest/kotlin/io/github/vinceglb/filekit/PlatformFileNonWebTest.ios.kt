package io.github.vinceglb.filekit

import platform.Foundation.NSBundle

actual val moduleRoot = PlatformFile(nsUrl = NSBundle.mainBundle.bundleURL)
