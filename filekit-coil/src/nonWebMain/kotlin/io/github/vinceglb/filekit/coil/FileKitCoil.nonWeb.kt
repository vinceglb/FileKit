package io.github.vinceglb.filekit.coil

import coil3.ImageLoader
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.key.Keyer
import coil3.map.Mapper
import coil3.request.Options
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path

/**
 * Maps a PlatformFile to a format that Coil can process.
 * This is a platform-specific implementation that handles the conversion of PlatformFile to a Coil-compatible format.
 */
public actual class PlatformFileMapper : Mapper<PlatformFile, Any> {
    actual override fun map(data: PlatformFile, options: Options): Any? = data.underlyingFile
}

/**
 * Fetches image data from a PlatformFile.
 * This class is responsible for retrieving the actual image data from the platform-specific file.
 */
public actual class PlatformFileFetcher(
    private val file: PlatformFile,
    private val imageLoader: ImageLoader,
    private val options: Options,
) : Fetcher {
    actual override suspend fun fetch(): FetchResult? {
        val underlyingFile = file.underlyingFile
        val data = imageLoader.components.map(underlyingFile, options)
        val output = imageLoader.components.newFetcher(data, options, imageLoader)
        val (fetcher) = checkNotNull(output) { "Fetcher not found for $underlyingFile" }
        return fetcher.fetch()
    }

    /**
     * Factory class for creating PlatformFileFetcher instances.
     * This factory is used by Coil to create fetchers for PlatformFile objects.
     */
    public actual class Factory actual constructor() : Fetcher.Factory<PlatformFile> {
        actual override fun create(
            data: PlatformFile,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher? = PlatformFileFetcher(data, imageLoader, options)
    }
}

/**
 * Generates unique keys for PlatformFile objects in Coil's cache.
 * This ensures proper caching behavior for platform-specific files.
 */
public actual class PlatformFileKeyer : Keyer<PlatformFile> {
    actual override fun key(data: PlatformFile, options: Options): String? = data.path
}

internal expect val PlatformFile.underlyingFile: Any
