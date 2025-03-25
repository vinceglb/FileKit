package io.github.vinceglb.filekit.dialogs.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSItemProvider
import platform.Foundation.NSURL
import platform.LinkPresentation.LPLinkMetadata
import platform.UIKit.UIActivityItemSourceProtocol
import platform.UIKit.UIActivityType
import platform.UIKit.UIActivityViewController
import platform.darwin.NSObject

/**
 * For UIActivityViewController
 */
internal class SingleImageProvider(
    private val imageUrl: NSURL,
    private val metaTitle: String
) : NSObject(),
    UIActivityItemSourceProtocol {

    @ObjCSignatureOverride
    override fun activityViewController(
        activityViewController: UIActivityViewController,
        itemForActivityType: UIActivityType?
    ): Any? = imageUrl

    override fun activityViewControllerPlaceholderItem(
        activityViewController: UIActivityViewController
    ): Any = imageUrl

    @ExperimentalForeignApi
    override fun activityViewControllerLinkMetadata(
        activityViewController: UIActivityViewController
    ): objcnames.classes.LPLinkMetadata? {
        val metadata = LPLinkMetadata()
        metadata.title = metaTitle
        metadata.originalURL = imageUrl
        metadata.imageProvider = NSItemProvider(imageUrl)
        return metadata as objcnames.classes.LPLinkMetadata
    }
}