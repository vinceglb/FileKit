package io.github.vinceglb.filekit

public class FileKitNotInitializedException :
    IllegalStateException("FileKit not initialized on JVM. Please call FileKit.init(appId) first.")
