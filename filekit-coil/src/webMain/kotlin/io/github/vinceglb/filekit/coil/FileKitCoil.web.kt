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
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImagePainter
import coil3.compose.EqualityDelegate
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

@Composable
public actual fun rememberPlatformFileCoilModel(file: PlatformFile?): Any? {
    var bytes by remember(file) { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(file) {
        bytes = file?.readBytes()
    }

    return bytes
}

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
    clipToBounds: Boolean,
    modelEqualityDelegate: EqualityDelegate
) {
    val model = rememberPlatformFileCoilModel(file)

    coil3.compose.AsyncImage(
        model = model,
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
        modelEqualityDelegate = modelEqualityDelegate,
    )
}
