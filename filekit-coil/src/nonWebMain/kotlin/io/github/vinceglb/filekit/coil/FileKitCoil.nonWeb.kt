package io.github.vinceglb.filekit.coil

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.AsyncImagePainter.State
import coil3.compose.LocalPlatformContext
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.key.Keyer
import coil3.map.Mapper
import coil3.request.Options
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import io.github.vinceglb.filekit.stopAccessingSecurityScopedResource

internal expect val PlatformFile.underlyingFile: Any

@Composable
public actual fun rememberPlatformFileCoilModel(file: PlatformFile?): Any? = file?.underlyingFile


@Composable
public actual fun rememberUnifiedCoilModel(model: Any?): Any? = when (model) {
    is PlatformFile -> rememberPlatformFileCoilModel(model)
    else -> model
}

@Composable
public actual fun AsyncImage(
    file: PlatformFile?,
    contentDescription: String?,
    imageLoader: ImageLoader,
    modifier: Modifier,
    placeholder: Painter?,
    error: Painter?,
    fallback: Painter?,
    onLoading: ((State.Loading) -> Unit)?,
    onSuccess: ((State.Success) -> Unit)?,
    onError: ((State.Error) -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
    clipToBounds: Boolean,
) {
    DisposableFileSecurityEffect(file)

    coil3.compose.AsyncImage(
        model = file?.underlyingFile,
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

@Composable
public actual fun AsyncImage(
    model: Any?,
    contentDescription: String?,
    imageLoader: ImageLoader,
    modifier: Modifier,
    placeholder: Painter?,
    error: Painter?,
    fallback: Painter?,
    onLoading: ((State.Loading) -> Unit)?,
    onSuccess: ((State.Success) -> Unit)?,
    onError: ((State.Error) -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
    clipToBounds: Boolean,
) {
    UnifiedDisposableFileSecurityEffect(model)

    coil3.compose.AsyncImage(
        model = when (model) {
            is PlatformFile -> model.underlyingFile
            else -> model
        },
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

@Composable
public actual fun AsyncImage(
    file: PlatformFile?,
    contentDescription: String?,
    modifier: Modifier,
    placeholder: Painter?,
    error: Painter?,
    fallback: Painter?,
    onLoading: ((State.Loading) -> Unit)?,
    onSuccess: ((State.Success) -> Unit)?,
    onError: ((State.Error) -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
    clipToBounds: Boolean
) {
    DisposableFileSecurityEffect(file)

    coil3.compose.AsyncImage(
        model = file?.underlyingFile,
        contentDescription = contentDescription,
        imageLoader = SingletonImageLoader.get(LocalPlatformContext.current),
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

@Composable
public actual fun AsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier,
    placeholder: Painter?,
    error: Painter?,
    fallback: Painter?,
    onLoading: ((State.Loading) -> Unit)?,
    onSuccess: ((State.Success) -> Unit)?,
    onError: ((State.Error) -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
    clipToBounds: Boolean
) {
    UnifiedDisposableFileSecurityEffect(model)

    coil3.compose.AsyncImage(
        model = when (model) {
            is PlatformFile -> model.underlyingFile
            else -> model
        },
        contentDescription = contentDescription,
        imageLoader = SingletonImageLoader.get(LocalPlatformContext.current),
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

@Composable
public actual fun AsyncImage(
    file: PlatformFile?,
    contentDescription: String?,
    imageLoader: ImageLoader,
    modifier: Modifier,
    transform: (State) -> State,
    onState: ((State) -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
    clipToBounds: Boolean
) {
    DisposableFileSecurityEffect(file)

    coil3.compose.AsyncImage(
        model = file?.underlyingFile,
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

@Composable
public actual fun AsyncImage(
    model: Any?,
    contentDescription: String?,
    imageLoader: ImageLoader,
    modifier: Modifier,
    transform: (State) -> State,
    onState: ((State) -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
    clipToBounds: Boolean
) {
    UnifiedDisposableFileSecurityEffect(model)

    coil3.compose.AsyncImage(
        model = when (model) {
            is PlatformFile -> model.underlyingFile
            else -> model
        },
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

@Composable
public actual fun AsyncImage(
    file: PlatformFile?,
    contentDescription: String?,
    modifier: Modifier,
    transform: (State) -> State,
    onState: ((State) -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
    clipToBounds: Boolean
) {
    DisposableFileSecurityEffect(file)

    coil3.compose.AsyncImage(
        model = file?.underlyingFile,
        contentDescription = contentDescription,
        imageLoader = SingletonImageLoader.get(LocalPlatformContext.current),
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

@Composable
public actual fun AsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier,
    transform: (State) -> State,
    onState: ((State) -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
    clipToBounds: Boolean
) {
    UnifiedDisposableFileSecurityEffect(model)

    coil3.compose.AsyncImage(
        model = when (model) {
            is PlatformFile -> model.underlyingFile
            else -> model
        },
        contentDescription = contentDescription,
        imageLoader = SingletonImageLoader.get(LocalPlatformContext.current),
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

@Composable
private fun DisposableFileSecurityEffect(file: PlatformFile?) {
    DisposableEffect(file) {
        file?.startAccessingSecurityScopedResource()
        onDispose { file?.stopAccessingSecurityScopedResource() }
    }
}

@Composable
private fun UnifiedDisposableFileSecurityEffect(model: Any?) {
    if (model is PlatformFile) {
        DisposableEffect(model) {
            model.startAccessingSecurityScopedResource()
            onDispose { model.stopAccessingSecurityScopedResource() }
        }
    }
}

/**
 * Maps a PlatformFile to a format that Coil can process.
 * This is a platform-specific implementation that handles the conversion of PlatformFile to a Coil-compatible format.
 */
public actual class PlatformFileMapper : Mapper<PlatformFile, Any> {
    override fun map(data: PlatformFile, options: Options): Any? = data.underlyingFile
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
    override suspend fun fetch(): FetchResult? {
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
        override fun create(
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
public actual class PlatformFileKeyer : Keyer<PlatformFile> {
    override fun key(data: PlatformFile, options: Options): String? {
        return data.path
    }
}
