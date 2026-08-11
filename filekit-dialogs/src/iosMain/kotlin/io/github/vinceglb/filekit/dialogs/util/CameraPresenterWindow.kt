package io.github.vinceglb.filekit.dialogs.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIScreen
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowLevelAlert
import platform.UIKit.UIWindowScene

/**
 * Hosts the camera presentation in a dedicated transparent [UIWindow].
 *
 * Since Compose Multiplatform 1.11, Compose dialogs and popups live in their own window placed
 * above modally presented view controllers. Presenting the fullscreen camera from the top-most
 * view controller of the main window puts it underneath such windows, which corrupts touch
 * handling app-wide after the dismissal. Presenting from a dedicated key window above alerts
 * avoids that; the previous key window is restored once the capture flow finishes.
 */
internal class CameraPresenterWindow {
    private val hostViewController = UIViewController()
    private var window: UIWindow? = null
    private var previousKeyWindow: UIWindow? = null

    @OptIn(ExperimentalForeignApi::class)
    fun attach(): UIViewController {
        val application = UIApplication.sharedApplication
        previousKeyWindow = application.keyWindow
        val scene = application.connectedScenes.firstNotNullOfOrNull { it as? UIWindowScene }
        val newWindow = scene?.let(::UIWindow) ?: UIWindow(frame = UIScreen.mainScreen.bounds)
        newWindow.rootViewController = hostViewController
        newWindow.windowLevel = UIWindowLevelAlert + 1.0
        newWindow.backgroundColor = UIColor.clearColor
        newWindow.makeKeyAndVisible()
        window = newWindow
        return hostViewController
    }

    fun detach() {
        window?.setHidden(true)
        window?.rootViewController = null
        window = null
        previousKeyWindow?.makeKeyAndVisible()
        previousKeyWindow = null
    }
}
