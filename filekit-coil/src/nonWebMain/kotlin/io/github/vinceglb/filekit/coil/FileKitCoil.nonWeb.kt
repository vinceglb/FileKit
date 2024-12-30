package io.github.vinceglb.filekit.coil

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import coil3.SingletonImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import io.github.vinceglb.filekit.PlatformFile

public expect val PlatformFile.coilModel: Any

@Composable
public actual fun rememberPlatformFileCoilModel(file: PlatformFile?): Any? =
    file?.coilModel

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
    AsyncImagePlatformEffects(file)

    coil3.compose.AsyncImage(
        model = file?.coilModel,
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
internal expect fun AsyncImagePlatformEffects(file: PlatformFile?)
