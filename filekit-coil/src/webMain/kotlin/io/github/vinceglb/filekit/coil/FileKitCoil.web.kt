package io.github.vinceglb.filekit.coil

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.compose.AsyncImagePainter
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.key.Keyer
import coil3.map.Mapper
import coil3.request.Options
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.readBytes

/**
 * Maps a PlatformFile to a format that Coil can process.
 * This is a platform-specific implementation that handles the conversion of PlatformFile to a Coil-compatible format.
 */
public actual class PlatformFileMapper actual constructor() : Mapper<PlatformFile, Any> {
    actual override fun map(data: PlatformFile, options: Options): Any? = data
}

/**
 * Fetches image data from a PlatformFile.
 * This class is responsible for retrieving the actual image data from the platform-specific file.
 */
public actual class PlatformFileFetcher(
    private val file: PlatformFile,
    private val imageLoader: ImageLoader,
    private val options: Options
) : Fetcher {
    actual override suspend fun fetch(): FetchResult? {
        val bytes = file.readBytes()
        val data = imageLoader.components.map(bytes, options)
        val output = imageLoader.components.newFetcher(data, options, imageLoader)
        val (fetcher) = checkNotNull(output) { "Fetcher not found" }
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
            imageLoader: ImageLoader
        ): Fetcher? = PlatformFileFetcher(data, imageLoader, options)
    }
}

/**
 * Generates unique keys for PlatformFile objects in Coil's cache.
 * This ensures proper caching behavior for platform-specific files.
 */
public actual class PlatformFileKeyer actual constructor() : Keyer<PlatformFile> {
    actual override fun key(data: PlatformFile, options: Options): String? =
        "${data.hashCode()}-${data.extension}"
}

@Deprecated("Migrate to the official Coil API by registering FileKit in your ImageLoader.Builder. Read more at https://filekit.mintlify.app/integrations/coil")
@Composable
public actual fun rememberPlatformFileCoilModel(file: PlatformFile?): Any? {
    var bytes by remember(file) { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(file) {
        bytes = file?.readBytes()
    }

    return bytes
}

@Deprecated("Migrate to the official Coil API by registering FileKit in your ImageLoader.Builder. Read more at https://filekit.mintlify.app/integrations/coil")
@Composable
public actual fun AsyncImage(
    file: PlatformFile?,
    contentDescription: String?,
    imageLoader: ImageLoader,
    modifier: Modifier,
    placeholder: Painter?,
    error: Painter?,
    fallback: Painter?,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)?,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)?,
    onError: ((AsyncImagePainter.State.Error) -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
    clipToBounds: Boolean
) {
    val coilModel = rememberPlatformFileCoilModel(file)

    coil3.compose.AsyncImage(
        model = coilModel,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        placeholder = placeholder,
        error = error,
        fallback = fallback,
        onLoading = onLoading,
        onSuccess = onSuccess,
        onError = onError,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
        clipToBounds = clipToBounds,
    )
}

@Deprecated("Migrate to the official Coil API by registering FileKit in your ImageLoader.Builder. Read more at https://filekit.mintlify.app/integrations/coil")
@Composable
public actual fun AsyncImage(
    file: PlatformFile?,
    contentDescription: String?,
    modifier: Modifier,
    placeholder: Painter?,
    error: Painter?,
    fallback: Painter?,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)?,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)?,
    onError: ((AsyncImagePainter.State.Error) -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
    clipToBounds: Boolean
) {
    val coilModel = rememberPlatformFileCoilModel(file)

    coil3.compose.AsyncImage(
        model = coilModel,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = placeholder,
        error = error,
        fallback = fallback,
        onLoading = onLoading,
        onSuccess = onSuccess,
        onError = onError,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
        clipToBounds = clipToBounds,
    )
}

@Deprecated("Migrate to the official Coil API by registering FileKit in your ImageLoader.Builder. Read more at https://filekit.mintlify.app/integrations/coil")
@Composable
public actual fun AsyncImage(
    file: PlatformFile?,
    contentDescription: String?,
    imageLoader: ImageLoader,
    modifier: Modifier,
    transform: (AsyncImagePainter.State) -> AsyncImagePainter.State,
    onState: ((AsyncImagePainter.State) -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
    clipToBounds: Boolean
) {
    val coilModel = rememberPlatformFileCoilModel(file)

    coil3.compose.AsyncImage(
        model = coilModel,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        transform = transform,
        onState = onState,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
        clipToBounds = clipToBounds,
    )
}

@Deprecated("Migrate to the official Coil API by registering FileKit in your ImageLoader.Builder. Read more at https://filekit.mintlify.app/integrations/coil")
@Composable
public actual fun AsyncImage(
    file: PlatformFile?,
    contentDescription: String?,
    modifier: Modifier,
    transform: (AsyncImagePainter.State) -> AsyncImagePainter.State,
    onState: ((AsyncImagePainter.State) -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
    clipToBounds: Boolean
) {
    val coilModel = rememberPlatformFileCoilModel(file)

    coil3.compose.AsyncImage(
        model = coilModel,
        contentDescription = contentDescription,
        modifier = modifier,
        transform = transform,
        onState = onState,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
        clipToBounds = clipToBounds,
    )
}
