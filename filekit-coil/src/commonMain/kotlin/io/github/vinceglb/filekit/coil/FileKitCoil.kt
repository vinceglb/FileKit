package io.github.vinceglb.filekit.coil

import coil3.ComponentRegistry
import coil3.ImageLoader
import coil3.compose.AsyncImagePainter.State
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.key.Keyer
import coil3.map.Mapper
import coil3.request.Options
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import io.github.vinceglb.filekit.stopAccessingSecurityScopedResource

/**
 * Maps a PlatformFile to a format that Coil can process.
 * This is a platform-specific implementation that handles the conversion of PlatformFile to a Coil-compatible format.
 */
public expect class PlatformFileMapper() : Mapper<PlatformFile, Any> {
    override fun map(data: PlatformFile, options: Options): Any?
}

/**
 * Fetches image data from a PlatformFile.
 * This class is responsible for retrieving the actual image data from the platform-specific file.
 */
public expect class PlatformFileFetcher : Fetcher {
    override suspend fun fetch(): FetchResult?

    /**
     * Factory class for creating PlatformFileFetcher instances.
     * This factory is used by Coil to create fetchers for PlatformFile objects.
     */
    @Suppress("ktlint:standard:class-signature")
    public class Factory() : Fetcher.Factory<PlatformFile> {
        override fun create(data: PlatformFile, options: Options, imageLoader: ImageLoader): Fetcher?
    }
}

/**
 * Generates unique keys for PlatformFile objects in Coil's cache.
 * This ensures proper caching behavior for platform-specific files.
 */
public expect class PlatformFileKeyer() : Keyer<PlatformFile> {
    override fun key(data: PlatformFile, options: Options): String?
}

/**
 * Adds support for PlatformFile to a Coil ComponentRegistry.
 * This method registers all necessary components (Mapper, Fetcher, and Keyer) to enable
 * Coil to work with PlatformFile objects.
 *
 * @receiver The ComponentRegistry.Builder to add the components to
 */
public fun ComponentRegistry.Builder.addPlatformFileSupport() {
    add(PlatformFileKeyer())
    add(PlatformFileMapper())
    add(PlatformFileFetcher.Factory())
}

/**
 * Extension function to handle security-scoped resource access for PlatformFile.
 * This function is called when the state of the image loading changes.
 *
 * On iOS and macOS, it starts accessing the security-scoped resource when loading begins,
 * and stops accessing it when the loading is successful or fails.
 *
 * @receiver The current state of the image loading process
 * @param file The PlatformFile being accessed
 */
public fun State.securelyAccessFile(file: PlatformFile?) {
    when (this) {
        is State.Loading -> {
            file?.startAccessingSecurityScopedResource()
        }

        is State.Success -> {
            file?.stopAccessingSecurityScopedResource()
        }

        is State.Error -> {
            file?.stopAccessingSecurityScopedResource()
        }

        is State.Empty -> {}
    }
}
