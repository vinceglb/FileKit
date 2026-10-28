package io.github.vinceglb.filekit.sample.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.sample.shared.navigation.AppNavigation
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme

/**
 * @param startDemo Screen opened on top of the home screen at launch, if any.
 * @param darkTheme Whether to use the dark color scheme.
 * @param embedded Whether the sample runs embedded in another page, which hides the documentation button of feature screens.
 */
@Composable
public fun App(
    modifier: Modifier = Modifier,
    dialogSettingsTransform: (FileKitDialogSettings) -> FileKitDialogSettings = { it },
    startDemo: AppDemo? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    embedded: Boolean = false,
) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader
            .Builder(context)
            .components {
                addPlatformFileSupport()
            }.build()
    }

    CompositionLocalProvider(LocalAppEmbedded provides embedded) {
        AppTheme(darkTheme = darkTheme) {
            AppNavigation(
                modifier = modifier,
                dialogSettingsTransform = dialogSettingsTransform,
                startDemo = startDemo,
            )
        }
    }
}
