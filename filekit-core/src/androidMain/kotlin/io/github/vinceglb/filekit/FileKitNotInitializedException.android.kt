package io.github.vinceglb.filekit

public class FileKitNotInitializedException :
    IllegalStateException("FileKit not initialized on Android. Please call FileKit.init(activity) first.")
