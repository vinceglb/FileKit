package io.github.vinceglb.filekit.dialogs

/**
 * Preferred asset representation mode for iOS photo and video picker results.
 */
public enum class FileKitAssetRepresentationMode {
    /**
     * Lets the system choose the best representation.
     */
    Automatic,

    /**
     * Uses the current representation to avoid transcoding when possible.
     */
    Current,

    /**
     * Uses the most compatible representation when possible, even if transcoding is required.
     */
    Compatible,
}
