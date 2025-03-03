package io.github.vinceglb.filekit.exceptions

public class FileKitNotInitializedException :
    FileKitException("FileKit not initialized on JVM. Please call FileKit.init(appId) first.")
