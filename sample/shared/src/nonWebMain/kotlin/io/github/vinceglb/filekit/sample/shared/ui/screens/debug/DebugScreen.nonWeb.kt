package io.github.vinceglb.filekit.sample.shared.ui.screens.debug

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.atomicMove
import io.github.vinceglb.filekit.bookmarkData
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.fromBookmarkData
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.write
import io.github.vinceglb.filekit.writeString
import kotlin.time.Clock

internal actual suspend fun debugPlatformTest(folder: PlatformFile) {
//    val folderBookmark = file.bookmarkData()
//
//    val niceFile = FileKit.cacheDir / "test${Clock.System.now().toEpochMilliseconds()}.txt"
//    niceFile.writeString("Hey dude")
//
//    val folder = PlatformFile.fromBookmarkData(folderBookmark)
//    // val destinationFile = folder / niceFile.name
//    niceFile.atomicMove(folder)
//
//    // println("Content = ${destinationFile.readString()}")
//
//    println("Files of folder = ${folder.list().joinToString { it.name }}")

    val contentTest = "Hey dude folder".encodeToByteArray()
    val fileTest = folder / "test.txt"
    fileTest.write(contentTest)

    println("fileTest exists = ${fileTest.exists()}")
    println("fileTest content = ${fileTest.readString()}")

    val savedDir = folder / "vince-sub1" / "vince-sub2" / "vince-sub3"

    println("savedDir exists = ${savedDir.exists()}")
    println("savedDir isDirectory = ${savedDir.isDirectory()}")

    savedDir.createDirectories(mustCreate = true)

    println("savedDir exists after create = ${savedDir.exists()}")
    println("savedDir isDirectory() after create = ${savedDir.isDirectory()}")

    val content = "Hey dude subfolder".encodeToByteArray()
    val file = savedDir / "test.txt"

    println("Files of savedDir before write = ${savedDir.list().joinToString { it.name }}")
    println("file exists = ${file.exists()}")

    file.write(content)

    println("Files of savedDir after write = ${savedDir.list().joinToString { it.name }}")
    println("file exists = ${file.exists()}")
    println("file content = ${file.readString()}")
}

private val bookmarkFile = FileKit.filesDir / "bookmark.bin"

internal actual suspend fun bookmarkFolder(folder: PlatformFile) {
    val bookmark = folder.bookmarkData()
    bookmarkFile.write(bookmark.bytes)
    println("Bookmark written = ${bookmarkFile.size()}")
}

internal actual suspend fun loadBookmarkedFolder(): PlatformFile {
    println("Bookmark exists() = ${bookmarkFile.exists()}")

    val bytes = bookmarkFile.readBytes()
    val folder = PlatformFile.fromBookmarkData(bytes)

    val niceFile = FileKit.cacheDir / "test${Clock.System.now().toEpochMilliseconds()}.txt"
    niceFile.writeString("Hey dude")

    val destinationFile = folder / niceFile.name
    println("Destination exists = ${destinationFile.exists()}")

    niceFile.atomicMove(destinationFile)
    println("NiceFile path = ${niceFile.path}")

    println("Files of folder = ${folder.list().joinToString { it.name }}")
    println("DestinationFile content = ${destinationFile.readString()}")
    println("DestinationFile exists = ${destinationFile.exists()}")
    println("DestinationFile path = ${destinationFile.path}")
    return folder
}
