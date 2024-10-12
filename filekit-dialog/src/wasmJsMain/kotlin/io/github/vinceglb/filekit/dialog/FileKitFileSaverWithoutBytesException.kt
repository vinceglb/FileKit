package io.github.vinceglb.filekit.dialog

public class FileKitFileSaverWithoutBytesException : IllegalArgumentException(
    "Bytes must not be null on Web platform. Use isSaveFileWithoutBytesSupported() " +
            "to check if the platform supports saving files without bytes."
)
