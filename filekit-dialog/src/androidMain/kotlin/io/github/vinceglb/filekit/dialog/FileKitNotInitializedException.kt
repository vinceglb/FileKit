package io.github.vinceglb.filekit.dialog

public class FileKitNotInitializedException :
    IllegalStateException("FileKit not initialized on Android. Please call FileKit.init(activity) first.")
