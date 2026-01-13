package io.github.vinceglb.filekit.sample.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import io.github.vinceglb.filekit.sample.shared.navigation.AppNavigation
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme

@Composable
public fun App(modifier: Modifier = Modifier) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader
            .Builder(context)
            .components {
                addPlatformFileSupport()
            }.build()
    }

    AppTheme {
        AppNavigation(
            modifier = modifier,
        )
    }
}
